package behaviour;

import java.awt.geom.Point2D;

import state.EstimatedMaze;
import state.State;


public class ExploreBehaviour2 extends Behaviour {
	EstimatedMaze map;
	Behaviour b;

	
	public ExploreBehaviour2(EstimatedMaze map) {
		name = new String("Explore Behaviour");
		this.map = map;
		b = null;
	}


	public double[] exec(State state) {
		System.out.println("Exec Explore Behaviour");
		if(b == null) {
			Point2D [] next = new Point2D.Double[4];
			double x = state.getPos().getX();
			double y = state.getPos().getY();

			int best = f(state, map, state.getPos());
			int res = 0;
			next[0] = new Point2D.Double();
			next[0].setLocation(x+1, y+1);
			next[1] = new Point2D.Double();
			next[1].setLocation(x+1, y-1);
			next[2] = new Point2D.Double();
			next[2].setLocation(x-1, y-1);
			next[3] = new Point2D.Double();
			next[3].setLocation(x-1, y+1);
			
			for(int ii = 0; ii < next.length; ii++)
				if(f(state, map, next[ii]) < best)
					res = ii;
			System.out.println("Selected Point " + next[res] + " with val = " + best);
			b = new GoToBehaviour(state, next[res].getX(), next[res].getY());
		}
		double[] act = b.exec(state);
		if(b.isComplete())
			setComplete(true);
		return act;
	}

	private int f(State state, EstimatedMaze map, Point2D n) {
		int val = 0;
		double x = n.getX();
		double y = n.getY();

		if (map.isObstacle(x, y))
			return Integer.MAX_VALUE-1;

//		if (state.beenHere(x, y))
//			val += 1000;

		val += map.getSightings(x, y) * 1000;

		double vx = x - state.getPos().getX();
		double vy = y - state.getPos().getY();
		double destDir = Math.toDegrees(Math.atan2(vy, vx));

		val += Math.abs(destDir - state.getDir())*10;

		return val;
	}


}
