package state;


import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


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

			if (state.getIR(i) < 0.7)
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
			System.out.println(i+" areaA: x="+areaA.getBounds2D().getCenterX()
					+" y="+areaA.getBounds2D().getCenterY()
					+" w="+areaA.getBounds2D().getWidth()
					+" h="+areaA.getBounds2D().getHeight());
			System.out.println(i+" areaB: x="+areaB.getBounds2D().getCenterX()
					+" y="+areaB.getBounds2D().getCenterY()
					+" w="+areaB.getBounds2D().getWidth()
					+" h="+areaB.getBounds2D().getHeight());
			System.out.println(i+" areaC: x="+areaC.getBounds2D().getCenterX()
					+" y="+areaC.getBounds2D().getCenterY()
					+" w="+areaC.getBounds2D().getWidth()
					+" h="+areaC.getBounds2D().getHeight());
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
					multiplier = 1.02;
					
				// area C
				else if (areaC.contains(cx, cy)) {									
					multiplier = 1.04;
					countc++;
				}

				cells[x][y].setWallProbability(cells[x][y].getWallProbability() * multiplier);

			}
		}
		System.out.println("c="+countc);
	}

//	public void setEstimatedState(State state) {
//		double angleOff[] = new double[] {0, -60, 60, 180};
//		for (int ii = 0; ii < 3; ii++) {
//			double objDist = this.getDist(state.getIR(ii))+0.5;
//			double objDir = state.getDir()+angleOff[ii];
//			double objX = 0.0;
//			double objY = 0.0;
//			double co = 0.0;
//			if(Math.abs(objDist) > 2.5) {
//				objX = state.getPos().getX()+2*Math.cos(Math.toRadians(objDir));
//				objY = state.getPos().getY()+2*Math.sin(Math.toRadians(objDir));
//				co = -1.0;
//			}
//			else {
//				objX = state.getPos().getX()+objDist*Math.cos(Math.toRadians(objDir));
//				objY = state.getPos().getY()+objDist*Math.sin(Math.toRadians(objDir));
//				co = 1.0;
//				System.out.println("objDist="+objDist+" objDir="+objDir+" ojbX="+objX+" objY="+objY);
//			}
//			int x = (int) Math.round(objX);
//			int y = (int) Math.round(objY);
//			double curProb = cells[x][y].getWallProbability();
//			cells[x][y].setWallProbability(curProb+co);
//		}
//	}

//		public void setEstimatedState(State state) {
//			double angleOff[] = new double[] {0, -60, 60, 180};
//			for (int ii = 0; ii < 3; ii++) {
//				if(state.getIR(ii)<1.0)
//					continue;
//				
//				double objDist = this.getDist(state.getIR(ii))+0.5;
//				double objDir = state.getDir()+angleOff[ii];
//				double objX = state.getPos().getX()+objDist*Math.cos(Math.toRadians(objDir));
//				double objY = state.getPos().getY()+objDist*Math.sin(Math.toRadians(objDir));
//				System.out.println("objDist="+objDist+" objDir="+objDir+" ojbX="+objX+" objY="+objY);
//
//				for(int x = (int) (objX-0.5)*RESOLUTION; x < (int)(objX+0.5)*RESOLUTION; x++)
//					for(int y = (int) (objY-0.4)*RESOLUTION; y < (int)(objY+0.4)*RESOLUTION; y++) {
//						double curProb = cells[x][y].getWallProbability();
//						cells[x][y].setWallProbability(curProb+1);
//					}
//		
////				for(int x = (int) (state.getPos().getX())*RESOLUTION; x < (int)(objX)*RESOLUTION; x++)
////					for(int y = (int) (objY-0.4)*RESOLUTION; y < (int)(objY+0.4)*RESOLUTION; y++) {
////						double curProb = cells[x][y].getWallProbability();
////						cells[x][y].setWallProbability(curProb+1);
////					}
//			}
//		}


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

public boolean isObstacle(double x, double y) {
	return cells[(int)(RESOLUTION * x)][(int)(RESOLUTION * y)].getWallProbability() > 0.55; 
}
public boolean isObstacle(int x, int y) {
	return cells[x][y].getWallProbability() > 0.55; 
}

//public boolean isObstacle(double x, double y) {
//	if (x < 0 || x > MAX_WIDTH || y <0 || y > MAX_HEIGHT)
//		return true;
//	return cells[(int)(RESOLUTION * x)][(int)(RESOLUTION * y)].getWallProbability() > 2; 
//}


}
