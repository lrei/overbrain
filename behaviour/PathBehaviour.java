package behaviour;

import java.awt.geom.Point2D;
import java.util.Vector;

import state.State;


public class PathBehaviour extends SequenceBehaviour {
	Vector<Point2D> bpath;


	public PathBehaviour(State state, Vector<Point2D> path) {
		name = new String("Path Behaviour");
		priority = 0;

		bpath = path;

		Behaviour[] pc = new Behaviour[path.size()];

		for(int ii = 0; ii < path.size(); ii++)
			pc[ii] = new GoToBehaviour(state, path.get(ii).getX(), path.get(ii).getY());

		setControllers(pc);
	}

	public double [] exec(State state) {
		double tol = 1.0;
		int step = getIndex();

		for(int ii = bpath.size()-1; ii > step; ii--) {
			if(bpath.get(ii).distance(state.getPos()) < tol) {
				setIndex(ii);
				System.out.println("Skipping");
			}
		}

		return super.exec(state);
	}

}
