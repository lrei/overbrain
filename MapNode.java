import java.awt.geom.Point2D;
import java.util.HashSet;

public class MapNode {
	public double occupied;
	public MapNode parent;
	public HashSet<MapNode> children;
	public double fValue;
	public double hValue;
	public double gValue;
	Point2D point;
	
	public MapNode() {
		point = new Point2D.Double();
		occupied = 1;
		parent = null;
		fValue = Double.MAX_VALUE;
		hValue = Double.MAX_VALUE;
		gValue = Double.MAX_VALUE;
	}
	
	public boolean equals(MapNode n) {
		if (point.equals(n.point))
			return true;
		else
			return false;
	}
	public int hashCode() {
		return point.hashCode();
	}
	
	public double distance(MapNode n) {
		return point.distance(n.point);
	}
	
	public Point2D getCenter() {
		Point2D center = new Point2D.Double();
		center.setLocation(point.getX()+0.5, point.getY()+0.5);
		return center;
	}
	
	//public setFValue()
}
