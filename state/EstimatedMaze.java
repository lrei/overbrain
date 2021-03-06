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


	public int getMazeWidth() {
		return (int) MAX_WIDTH*RESOLUTION;
	}

	public int getMazeHeight() {
		return (int) MAX_HEIGHT*RESOLUTION;
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

	public void updateFromState(State state) {
		double[] ir = new double[3];

		ir[0] = state.getIR(0);
		ir[1] = state.getIR(1);
		ir[2] = state.getIR(2);

		updateMap(state.getPos(), state.getDestDir(), ir);
		updateTargetProbability(state.getPos(), state.isBeaconVisible());

	}

	private void updateTargetProbability(Point2D pos, boolean beaconVisible) {
		double beaconDist = 5.0;
		for (int x = (int)((pos.getX() - beaconDist)*RESOLUTION); x < (pos.getX()+beaconDist) * RESOLUTION; x++) {
			for (int y = (int)((pos.getY() - beaconDist)*RESOLUTION); y < (pos.getY()+beaconDist) * RESOLUTION; y++) {

				if (x < 0 || y < 0 || x >= MAX_WIDTH * RESOLUTION || y >= MAX_HEIGHT * RESOLUTION)
					continue;

				double cx = (double)x/(double)RESOLUTION;								
				double cy = (double)y/(double)RESOLUTION;


				double multiplier = 1.0;

				if (beaconVisible)
					multiplier = 1.10;
				else
					multiplier = 0.90;

				cells[x][y].setTargetProbability((cells[x][y].getTargetProbability() * multiplier));

			}
		}

	}

	public Vector<Point2D> updateFromMsg(State state, String msg, int senderId) {
		if(state.getId() == senderId+1)
			return null;
		
		//System.out.println("Message: " + msg);
		String[] msgs = msg.split(":");
		String[] fields = msgs[0].split("@");

		/*
		 * Mission
		 */
		String mission = fields[0];
		if(mission.compareToIgnoreCase("F") == 0) {
			String[] points = fields[1].split("&");
			Vector<Point2D> path = new Vector<Point2D>();
			for(String pointField: points) {
				String [] point = pointField.split(";");
				Point2D p = new Point2D.Double();
				p.setLocation(Integer.valueOf(point[0]) + 0.5,
						Integer.valueOf(point[1]) + 0.5);
				path.add(p);
			}
			clearPath(path);
			return path;
		}
		else {

			/*
			 * Position
			 */
			String[] posFields = fields[1].split("&");
			Point2D pos = new Point2D.Double();

			pos.setLocation(Double.valueOf(posFields[0])/10,
					Double.valueOf(posFields[1])/10);

			double dir = Double.valueOf(posFields[2]);

			/*
			 * IR
			 */
			String[] irFields = fields[2].split("&");
			double[] ir = new double[3];

			ir[0] = Double.valueOf(irFields[0]) / 10;
			ir[1] = Double.valueOf(irFields[1]) / 10;
			ir[2] = Double.valueOf(irFields[2]) / 10;


			/*
			 * Update Map
			 */
			updateMap(pos, dir, ir);
			if(mission.compareToIgnoreCase("B") == 0) {
				updateTargetProbability(pos, true);
			}
			else
				updateTargetProbability(pos, false);

		}
		/*
		 * HOPS!
		 */
		for(int mm = 1; mm < msgs.length; mm++) {
			//System.out.println("GOT HOPPER: "+msgs[mm]);
			fields = msgs[mm].split("@");

			/*
			 * Mission
			 */
			mission = fields[0];
			/*
			 * Position
			 */
			String[] posFields = fields[1].split("&");
			Point2D pos = new Point2D.Double();

			pos.setLocation(Double.valueOf(posFields[0])/10,
					Double.valueOf(posFields[1])/10);

			double dir = Double.valueOf(posFields[2]);

			/*
			 * IR
			 */
			String[] irFields = fields[2].split("&");
			double[] ir = new double[3];

			ir[0] = Double.valueOf(irFields[0]) / 10;
			ir[1] = Double.valueOf(irFields[1]) / 10;
			ir[2] = Double.valueOf(irFields[2]) / 10;


			/*
			 * Update Map
			 */
			updateMap(pos, dir, ir);
			if(mission.compareToIgnoreCase("B") == 0) {
				updateTargetProbability(pos, true);
			}
			else
				updateTargetProbability(pos, false);

		}

		return null;
	}



	public void updateMap(Point2D pos, double dir, double [] ir) {

		double startAngle[] = new double[] {-30, -90, 30, -120};
		double xoff[] = new double[4];
		double yoff[] = new double[4];

		for (int i = 0; i < 4; i++) {
			xoff[i] = Math.cos(startAngle[i]+30) / 2;
			yoff[i] = Math.sin(startAngle[i]+30) / 2;
		}

		double maxDist = 0.5;

		Area areaA = new Area();
		Area areaB = new Area();
		Area areaC = new Area();

		for (int i = 0; i < 3; i++) {
			double dist = this.getDist(ir[i]);

			if (ir[i] < 1.0)
				continue;

			double distA = dist - noise_obstacle;
			double distB = dist;
			double distC = distB + min_wall_width;

			maxDist = Math.max(maxDist, distC);

			Arc2D.Double arcA = new Arc2D.Double(pos.getX()+xoff[i]-distA,
					pos.getY()+yoff[i]-distA, distA*2, distA*2,
					startAngle[i]+dir, 60.0,Arc2D.PIE);			
			Arc2D.Double arcB = new Arc2D.Double(pos.getX()+xoff[i]-distB,
					pos.getY()+yoff[i]-distB, distB*2, distB*2,
					startAngle[i]+dir, 60.0,Arc2D.PIE);
			Arc2D.Double arcC = new Arc2D.Double(pos.getX()+xoff[i]-distC,
					pos.getY()+yoff[i]-distC, distC*2, distC*2,
					startAngle[i]+dir, 60.0,Arc2D.PIE);


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
		for (int x = (int)((pos.getX() - maxDist)*RESOLUTION); x < (pos.getX()+maxDist) * RESOLUTION; x++) {
			for (int y = (int)((pos.getY() - maxDist)*RESOLUTION); y < (pos.getY()+maxDist) * RESOLUTION; y++) {

				if (x < 0 || y < 0 || x >= MAX_WIDTH * RESOLUTION || y >= MAX_HEIGHT * RESOLUTION)
					continue;

				double cx = (double)x/(double)RESOLUTION;								
				double cy = (double)y/(double)RESOLUTION;


				double multiplier = 1.0;

				if (Point2D.distance(pos.getX(), pos.getY(), cx, cy) < 0.5)
					multiplier = 0.95;

				// area A
				else if (areaA.contains(cx, cy))
					multiplier = 0.97;

				// area B
				else if (areaB.contains(cx, cy))
					multiplier = 0.99;

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

	public int translateCoord(double c) {
		return (int) (c*RESOLUTION);
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
		if (x < 0 || y < 0)
			return true;
		if(x > (MAX_WIDTH-1)*RESOLUTION || y > (MAX_HEIGHT-1)*RESOLUTION)
			return true;

		return cells[x][y].getWallProbability() > 0.55; 
	}


	private double reducedBlockProb(int x, int y, int nr) {
		double prob = 0;
		int n = 0;
		int inc = RESOLUTION / nr;
		for(int cx = x*RESOLUTION; cx < (x*RESOLUTION)+inc; cx++) {
			for(int cy = y*RESOLUTION; cy < (y*RESOLUTION)+inc; cy++) {
				if((cx >= cells.length) || (cy >= cells[0].length))
					continue;
				prob = prob + cells[cx][cy].getWallProbability();
				n++;
			}
		}
		prob = prob / n;
		return prob;
	}

	public EstimatedCell [][] reduce(int nr) {
		if((nr > RESOLUTION) || (nr < 1))
			return null;

		EstimatedCell [][] reducedMap = 
			new EstimatedCell[MAX_WIDTH*nr][MAX_HEIGHT*nr];

		for(int ii = 0; ii < MAX_WIDTH*nr; ii++) {
			for(int jj = 0; jj < MAX_HEIGHT*nr; jj++) {
				reducedMap[ii][jj] = new EstimatedCell(ii, jj);
				reducedMap[ii][jj].setWallProbability(reducedBlockProb(ii, jj, nr));
			}
		}
		return reducedMap;
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
				cells[x][y].setWallProbability(cells[x][y].getWallProbability() * 0.97);
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

}

