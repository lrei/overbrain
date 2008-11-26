package behaviour;

import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import state.ProbMap;
import state.State;

public class MapBehaviour {

	double noise_obstacle = 0.1;
	double minWall = 0.5;	// it's actually 0.4

	public ProbMap exec(State state, ProbMap map) {
		double distanceSensors[] = new double[4];

		// GPS MAP UPDATE
		List<Point2D> n = state.requestPath();
		double maxDist = 0.5;	// robot body
		for(int ii = 0; ii < n.size(); ii++) {
			for (double cx = (n.get(ii).getX() - maxDist); cx < (n.get(ii).getX()+maxDist); cx+=0.1) {
				for (double cy = (n.get(ii).getY() - maxDist); cy < (n.get(ii).getY()+maxDist); cy+=0.1) {

					if(cy > map.getHeight() || cy < 0 || cx > map.getWidth() || cx < 0)
						continue;

					double multiplier = 0.5;
					map.set(cx, cy, multiplier);
				}
			}
		}

		// IRSENSOR MAP UPDATE
		for (int ii = 0; ii < 4; ii++) {
			distanceSensors[ii] = state.getIR(ii);
		}

		double startAngle[] = new double[] {-30, -90, 30, -120};
		double xoff[] = new double[4];
		double yoff[] = new double[4];

		for (int ii = 0; ii < 4; ii++) {
			xoff[ii] = Math.cos(startAngle[ii]+30) / 2;
			yoff[ii] = Math.sin(startAngle[ii]+30) / 2;
		}

		maxDist = 0;
		Area areaA = new Area();
		Area areaB = new Area();
		Area areaC = new Area();

		for (int ii = 0; ii < 3; ii++) {
			double dist = distanceSensors[ii];

			if (dist < 0.3)
				continue;

			double distA = 1/(dist + noise_obstacle);
			double distB = 1/dist;
			double distC = distB + minWall;
			maxDist = Math.max(maxDist, distC);

			Arc2D.Double arcA = new Arc2D.Double(state.getPos().getX()+xoff[ii]-distA, state.getPos().getY()+yoff[ii]-distA, distA*2, distA*2, startAngle[ii]+Math.toDegrees(-state.getDir()), 60.0,Arc2D.PIE);			
			Arc2D.Double arcB = new Arc2D.Double(state.getPos().getX()+xoff[ii]-distB, state.getPos().getY()+yoff[ii]-distB, distB*2, distB*2, startAngle[ii]+Math.toDegrees(-state.getDir()), 60.0,Arc2D.PIE);
			Arc2D.Double arcC = new Arc2D.Double(state.getPos().getX()+xoff[ii]-distC, state.getPos().getY()+yoff[ii]-distC, distC*2, distC*2, startAngle[ii]+Math.toDegrees(-state.getDir()), 60.0,Arc2D.PIE);

			areaA.add(new Area(arcA));
			areaB.add(new Area(arcB));
			areaC.add(new Area(arcC));
		}

		for (double cx = (state.getPos().getX() - maxDist); cx < (state.getPos().getX()+maxDist); cx+=0.1) {
			for (double cy = ((state.getPos().getY() - maxDist)); cy < (state.getPos().getY()+maxDist); cy+=0.1) {

				if(cy > map.getHeight() || cy < 0 || cx > map.getWidth() || cx < 0)
					continue;

				double multiplier = 1.0;

				if (Point2D.distance(state.getPos().getX(), state.getPos().getY(), cx, cy) < 0.5)
					multiplier = 0.85;

				// area A
				else if (areaA.contains(cx, cy)) 
					multiplier = 0.95;

				// area B
				else if (areaB.contains(cx, cy)) 
					multiplier = 0.97;

				// area C
				else if (areaC.contains(cx, cy)) 
					multiplier = 1.04;


				map.set(cx, cy, multiplier);

			}
		}

		return map;

	}
}
