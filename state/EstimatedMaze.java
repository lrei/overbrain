package state;


import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;


public class EstimatedMaze {

	public static int RESOLUTION = 10;

	public static int MAX_WIDTH = 28 * 2;
	public static int MAX_HEIGHT = 14 * 2;

	private double noise_obstacle = 0.1;
	private double min_wall_width = 0.4;
	public EstimatedCell[][] cells = new EstimatedCell[MAX_WIDTH * RESOLUTION][MAX_HEIGHT * RESOLUTION];

	
	public double getMazeWidth() {
		return MAX_WIDTH;
	}

	public double getMazeHeight() {
		return MAX_HEIGHT;
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
			double dist = state.getIR(i);

			/*
			 * to reduce errors, map only objects that are close
			 */
			if (dist < 0.9)	//or=0.3
				continue;

			double distA = 1/(dist + noise_obstacle);
			double distB = 1/dist;
			double distC = distB + min_wall_width;

			maxDist = Math.max(maxDist, distC);

			double start = -startAngle[i]+state.getDir();
			
			double xA = state.getPos().getX()+xoff[i]-distA;
			double yA = state.getPos().getY()+yoff[i]-distA;
			double rA = distA*2;
			Arc2D.Double arcA = new Arc2D.Double(xA, yA, rA, rA, start , 60.0,Arc2D.PIE);
			double xB = state.getPos().getX()+xoff[i]-distB;
			double yB = state.getPos().getY()+yoff[i]-distB;
			double rB = distB*2;
			Arc2D.Double arcB = new Arc2D.Double(xB, yB, rB, rB, start, 60.0,Arc2D.PIE);
			double xC = state.getPos().getX()+xoff[i]-distC;
			double yC = state.getPos().getY()+yoff[i]-distC;
			double rC = distC*2;
			Arc2D.Double arcC = new Arc2D.Double(xC, yC, rC, rC, start, 60.0,Arc2D.PIE);

			System.out.println("Map A: x="+xA+" y="+yA+" r="+rA+" start="+start);
			System.out.println("Map B: x="+xB+" y="+yB+" r="+rB+" start="+start);
			System.out.println("Map C: x="+xC+" y="+yC+" r="+rC+" start="+start);
			areaA.add(new Area(arcA));
			areaB.add(new Area(arcB));
			areaC.add(new Area(arcC));
		}

		for (int x = (int)((state.getPos().getX() - maxDist)*RESOLUTION); x < (state.getPos().getX()+maxDist) * RESOLUTION; x++) {
			for (int y = (int)((state.getPos().getY() - maxDist)*RESOLUTION); y < (state.getPos().getY()+maxDist) * RESOLUTION; y++) {

				if (x < 0 || y < 0 || x >= MAX_WIDTH * RESOLUTION || y >= MAX_HEIGHT * RESOLUTION)
					continue;

				double cx = (double)x/(double)RESOLUTION;								
				double cy = (double)y/(double)RESOLUTION;

				double multiplier = 1.0;

				if (Point2D.distance(state.getPos().getX(), state.getPos().getY(), cx, cy) < 0.5)
					multiplier = 0.10;	// bot is already over this point (in theory)
				
				// area A
				else if (areaA.contains(cx, cy))
					multiplier = 0.95;

				// area B
				else if (areaB.contains(cx, cy))
					multiplier = 1.03;

				// area C
				else if (areaC.contains(cx, cy))							
					multiplier = 1.08;
	
				double curProb = cells[x][y].getWallProbability();
				cells[x][y].setWallProbability(curProb * multiplier);
				double nextProb = cells[x][y].getWallProbability();
				//System.out.print("Prob@("+cx+" ,"+cy+")"+curProb+"->"+nextProb+" ");
			}
		}
		//System.out.println(" ");
	}



	public EstimatedMaze() {

		for (int x = 0; x < MAX_WIDTH * RESOLUTION; x++)
			for (int y = 0; y < MAX_HEIGHT * RESOLUTION; y++)
				cells[x][y] = new EstimatedCell((double)x/(double)RESOLUTION,(double)y/(double)RESOLUTION);
	}

	public boolean isObstacle(double x, double y) {
		if (x < 0 || x > MAX_WIDTH || y <0 || y > MAX_HEIGHT)
			return true;
		return cells[(int)(RESOLUTION * x)][(int)(RESOLUTION * y)].getWallProbability() > 0.55; 
	}


}
