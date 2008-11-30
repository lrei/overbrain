import java.awt.geom.Point2D;
import java.util.Vector;

import state.State;


public class Comunicator {
	
	public Comunicator() {}
	
	public String talk(State state) {
		String msg = new String();
		Vector<Point2D> path = state.getPath();
		
		if(state.isFound())
			msg += "F";
		else
			msg += "S";
		
		for(int ii = 1; ii < path.size() && ii < 4*24; ii+=4) {
			Point2D p = path.get(ii);
			int x = (int) Math.round(p.getX());
			String sx = Integer.toString(x);
			if (sx.length() < 2)
				sx = "0"+sx;
			int y = (int) Math.round(p.getY());
			String sy = Integer.toString(y);
			if (sx.length() < 2)
				sy = "0"+sy;
			msg = msg + sx + sy;
		}
		
		return msg;
	}

}
