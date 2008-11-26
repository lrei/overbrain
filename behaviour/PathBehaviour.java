package behaviour;

import java.awt.geom.Point2D;
import java.util.Vector;

import state.State;


public class PathBehaviour extends SequenceBehaviour {
	
	public PathBehaviour(State state, Vector<Point2D> path) {
		Behaviour[] pc= new Behaviour[path.size()];
		
		for(int ii = 0; ii < path.size(); ii++)
			pc[ii] = new GoToBehaviour(state, path.get(ii).getX(), path.get(ii).getY());
		
		setControllers(pc);
	}

}
