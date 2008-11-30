package state;


import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

public class EstimatedMaze {

	public static int RESOLUTION = 10;

	public static int MAX_WIDTH = 28;
	public static int MAX_HEIGHT = 14;

	private double noise_obstacle = 0.1;
	private double min_wall_width = 0.4;
	public EstimatedCell[][] cells = new EstimatedCell[MAX_WIDTH * RESOLUTION][MAX_HEIGHT * RESOLUTION];

	private int counter = 0;

	public int getMazeWidth() {
		return (int) MAX_WIDTH*RESOLUTION;
	}

	public int getMazeHeight() {
		return (int) MAX_HEIGHT*RESOLUTION;
	}


	public void clearMap() {
		for (int x = 0; x < MAX_WIDTH * RESOLUTION; x++)
			for (int y = 0; y < MAX_HEIGHT * RESOLUTION; y++)
				cells[x][y].setWallProbability(0.5);
	}

	public void setResolution(int res) {
		EstimatedMaze.RESOLUTION = res;		
		clearMap();
	}

	public double getWallProbability(double x, double y) {
		x *= RESOLUTION;
		y *= RESOLUTION;

		if (x < 0 || x > MAX_WIDTH * RESOLUTION)
			return 1.0;

		if (y < 0 || y > MAX_HEIGHT * RESOLUTION)
			return 1.0;

		return cells[(int)x][(int)y].getWallProbability();
	}

	/*
	 * Get distance from IRSensor value
	 * from fabio aguiar
	 */
	public double getDist(double sensorValue)
	{
		if(sensorValue >= 95)
			return 0;

		double ret;
		ret = 10.0/((10.0*sensorValue)-0.5);
		return ret;	// originally  ret+1
	}

	public void setEstimatedState(State state) {

		double startAngle[] = new double[] {-30, -90, 30, -120};
		double xoff[] = new double[4];
		double yoff[] = new double[4];

		for (int i = 0; i < 4; i++) {
			xoff[i] = Math.cos(startAngle[i]+30) / 2;
			yoff[i] = Math.sin(startAngle[i]+30) / 2;
		}

		double maxDist = 0;

		Area areaA = new Area();
		Area areaB = new Area();
		Area areaC = new Area();

		for (int i = 0; i < 3; i++) {
			double dist = this.getDist(state.getIR(i));

			if (state.getIR(i) < 1.0)
				continue;

			double distA = dist - noise_obstacle;
			double distB = dist;
			double distC = distB + min_wall_width;

			maxDist = Math.max(maxDist, distC);

			Arc2D.Double arcA = new Arc2D.Double(state.getX()+xoff[i]-distA,
					state.getY()+yoff[i]-distA, distA*2, distA*2,
					startAngle[i]+state.getDir(), 60.0,Arc2D.PIE);			
			Arc2D.Double arcB = new Arc2D.Double(state.getX()+xoff[i]-distB,
					state.getY()+yoff[i]-distB, distB*2, distB*2,
					startAngle[i]+state.getDir(), 60.0,Arc2D.PIE);
			Arc2D.Double arcC = new Arc2D.Double(state.getX()+xoff[i]-distC,
					state.getY()+yoff[i]-distC, distC*2, distC*2,
					startAngle[i]+state.getDir(), 60.0,Arc2D.PIE);


			areaA.add(new Area(arcA));
			areaB.add(new Area(arcB));
			areaC.add(new Area(arcC));
			//			System.out.println(i+" areaA: x="+areaA.getBounds2D().getCenterX()
			//					+" y="+areaA.getBounds2D().getCenterY()
			//					+" w="+areaA.getBounds2D().getWidth()
			//					+" h="+areaA.getBounds2D().getHeight());
			//			System.out.println(i+" areaB: x="+areaB.getBounds2D().getCenterX()
			//					+" y="+areaB.getBounds2D().getCenterY()
			//					+" w="+areaB.getBounds2D().getWidth()
			//					+" h="+areaB.getBounds2D().getHeight());
			//			System.out.println(i+" areaC: x="+areaC.getBounds2D().getCenterX()
			//					+" y="+areaC.getBounds2D().getCenterY()
			//					+" w="+areaC.getBounds2D().getWidth()
			//					+" h="+areaC.getBounds2D().getHeight());
		}
		int countc = 0;
		for (int x = (int)((state.getX() - maxDist)*RESOLUTION); x < (state.getX()+maxDist) * RESOLUTION; x++) {
			for (int y = (int)((state.getY() - maxDist)*RESOLUTION); y < (state.getY()+maxDist) * RESOLUTION; y++) {

				if (x < 0 || y < 0 || x >= MAX_WIDTH * RESOLUTION || y >= MAX_HEIGHT * RESOLUTION)
					continue;

				double cx = (double)x/(double)RESOLUTION;								
				double cy = (double)y/(double)RESOLUTION;


				double multiplier = 1.0;

				if (Point2D.distance(state.getX(), state.getY(), cx, cy) < 0.5)
					multiplier = 0.85;

				// area A
				else if (areaA.contains(cx, cy))
					multiplier = 0.95;

				// area B
				else if (areaB.contains(cx, cy))
					multiplier = 1.01;

				// area C
				else if (areaC.contains(cx, cy)) {									
					multiplier = 1.04;
					countc++;
				}
				cells[x][y].setWallProbability(cells[x][y].getWallProbability() * multiplier);
			}
		}
		//System.out.println("c="+countc);

	}



	public void write(String filename) {
		try {
			PrintWriter writer = new PrintWriter("map.txt");

			for(int jj = 14*RESOLUTION-1; jj > 0; jj--) {
				for(int ii = 0; ii < 28*RESOLUTION-1; ii++) {
					//writer.print(map.getWallProbability(ii, jj)+" ");

					if(this.isObstacle(ii, jj))
						writer.print("#");

					//				if(cells[ii][jj].getWallProbability()>2)
					//					writer.print("#");
					else
						writer.print("-");
				}
				writer.println("");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}


	public EstimatedMaze() {

		for (int x = 0; x < MAX_WIDTH * RESOLUTION; x++)
			for (int y = 0; y < MAX_HEIGHT * RESOLUTION; y++)
				cells[x][y] = new EstimatedCell((double)x/(double)RESOLUTION,(double)y/(double)RESOLUTION);
	}


	public int getSightings(double x, double y) {
		if (x < 0 || y < 0)
			return 0;
		if(x > (MAX_WIDTH-1) || y > (MAX_HEIGHT-1))
			return 0;

		return cells[(int)(RESOLUTION * x)][(int)(RESOLUTION * y)].getSighting();
	}
	public boolean isObstacle(double x, double y) {
		if (x < 0 || y < 0)
			return true;
		if(x > (MAX_WIDTH-1) || y > (MAX_HEIGHT-1))
			return true;

		return cells[(int)(RESOLUTION * x)][(int)(RESOLUTION * y)].getWallProbability() > 0.55; 
	}
	public boolean isObstacle(int x, int y) {
		return cells[x][y].getWallProbability() > 0.55; 
	}

	public boolean isObstacle(Quadtree qt, double [][] rmap) {

		for(int x =  (int) qt.rect.getMinX(); x < qt.rect.getMaxX(); x++) {
			for(int y = (int) qt.rect.getMinY(); y < qt.rect.getMaxY(); y++) {
				if (x < 0 || y < 0)
					return true;
				if(x > (MAX_WIDTH-1) || y > (MAX_HEIGHT-1))
					return true;
				if(rmap[x][y] > 0.8)
					return true;
			}
		}
		return false;
	}

	public int reducedProb(int ii, int jj) {
		for (int x = ii*RESOLUTION; x < (ii+1)*RESOLUTION; x++) {
			for (int y = jj*RESOLUTION; y < (jj+1)*RESOLUTION; y++) {
				if (isObstacle(x, y) || this.getSightings(x/RESOLUTION, y/RESOLUTION) < 2)
					return 1;
			}
		}
		return 0;
	}

	public double [][] reduce() {
		double [][] reducedMap = new double[MAX_WIDTH][MAX_HEIGHT];
		for(int ii = 0; ii < MAX_WIDTH; ii++) {
			for(int jj = 0; jj < MAX_HEIGHT; jj++) {
				reducedMap[ii][jj] = reducedProb(ii, jj);
			}
		}
		return reducedMap;
	}
	public Quadtree toQuadtree() {
		Quadtree root = new Quadtree();
		// (14, 7)
		root.rect.setRect(0, 0, MAX_WIDTH, MAX_HEIGHT);
		double[][] rmap = reduce();
//		for (int ii = 0; ii < MAX_WIDTH; ii++) {
//			for(int jj = 0; jj < MAX_HEIGHT; jj++)
//				System.out.print(rmap[ii][jj]+" ");
//			System.out.println("");
//		}

		buildQuadtree(root, rmap);
		System.out.println("N qts="+counter);
		return root;
	}

	public void buildQuadtree(Quadtree root, double[][] rmap) {

		double w = root.rect.getWidth()/2;
		double h = root.rect.getHeight()/2;

		if (isObstacle(root, rmap)) {
			root.occupied = 1;
			counter++;

			if (w < 1 || h < 0.5) {
				return;
			}
			root.nw = new Quadtree();
			root.ne = new Quadtree();
			root.sw = new Quadtree();
			root.se = new Quadtree();
			//root.parent = root;


			double x = root.rect.getX();
			double y = root.rect.getY();

			//System.out.println("x="+x +" y="+y);

			root.nw.rect.setRect(x, y, w, h);
			root.sw.rect.setRect(x, y+h, w, h);
			root.ne.rect.setRect(x+w, y, w, h);
			root.se.rect.setRect(x+w, y+h, w, h);

			buildQuadtree(root.nw, rmap);
			buildQuadtree(root.ne, rmap);
			buildQuadtree(root.sw, rmap);
			buildQuadtree(root.se, rmap);
		}
		else {
			root.occupied = 0;
		}

	}
	/*
	 * doesn't actually "clear" so much as it reduces the prob
	 */
	public void clearPoint(Point2D p) {
		double maxDist = 0.5;
		double xc = p.getX();
		double yc = p.getY();

		for (int x = (int)((xc - maxDist)*RESOLUTION); x < (xc+maxDist) * RESOLUTION; x++) {
			for (int y = (int)((yc - maxDist)*RESOLUTION); y < (yc+maxDist) * RESOLUTION; y++) {
				if(x < 0 || y < 0)
					continue;
				if(x > (MAX_WIDTH-1)*RESOLUTION || y > (MAX_HEIGHT-1)*RESOLUTION)
					continue;
				cells[x][y].setWallProbability(cells[x][y].getWallProbability() * 0.90);
			}
		}
	}

	public void clearPath(Vector<Point2D> path) {
		for(int ii = 0; ii < path.size(); ii++)
			clearPoint(path.get(ii));
	}

	public EstimatedCell [][] getCells() {
		return cells;
	}
	
	public void setFromMsg(State state) {
		Vector<Point2D> path = state.getMsgs();
		clearPath(path);
	}
}

