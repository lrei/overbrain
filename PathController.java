import java.awt.geom.Point2D;
import java.util.Vector;

public class PathController extends SequenceController {
	
	public PathController(State state, Vector<Point2D> path) {
		Controller[] pc= new Controller[path.size()];
		
		for(int ii = 0; ii < path.size(); ii++)
			pc[ii] = new GoToController(state, path.get(ii).getX(), path.get(ii).getY());
		
		setControllers(pc);
	}

}
