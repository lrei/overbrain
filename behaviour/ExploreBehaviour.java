package behaviour;

import java.awt.geom.Point2D;

import state.EstimatedMaze;
import state.State;


public class ExploreBehaviour extends Behaviour {
	EstimatedMaze map;
	Behaviour b;

	
	public ExploreBehaviour(EstimatedMaze map) {
		this.map = map;
		b = null;
	}


	public double[] exec(State state) {
		System.out.println("Exec Explore Behaviour");
		if(b == null) {
			Point2D [] next = new Point2D.Double[4];
			double x = state.getPos().getX();
			double y = state.getPos().getY();

			next[0] = new Point2D.Double();
			next[0].setLocation(x+1, y+1);
			int best = f(state, map, next[0]);
			int res = 0;
			next[1] = new Point2D.Double();
			next[1].setLocation(x-4, y+4);
			next[2] = new Point2D.Double();
			next[2].setLocation(x-4, y-4);
			next[3] = new Point2D.Double();
			next[3].setLocation(x+4, y-4);
			
			for(int ii = 1; ii < next.length; ii++)
				if(f(state, map, next[ii]) < best)
					res = ii;
		
			b = new GoToBehaviour(state, next[res].getX(), next[res].getY());
		}
		if(b.isComplete())
			setComplete(true);
		return b.exec(state);
	}

	private int f(State state, EstimatedMaze map, Point2D n) {
		int val = 0;
		double x = n.getX();
		double y = n.getY();

		if (map.isObstacle(x, y))
			return Integer.MAX_VALUE;

		if (state.beenHere(x, y))
			val += 1000;

		val += map.getSightings(x, y) * 20;

		double vx = x - state.getPos().getX();
		double vy = y - state.getPos().getY();
		double destDir = Math.toDegrees(Math.atan2(vy, vx));

		val += Math.abs(destDir - state.getDir())*10;

		return val;
	}


}
