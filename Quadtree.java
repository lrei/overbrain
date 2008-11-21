import java.awt.geom.Rectangle2D;

public class Quadtree {
	Rectangle2D.Double rect;
	public double occupied;
	public Quadtree nw;
	public Quadtree ne;
	public Quadtree sw;
	public Quadtree se;
	public Quadtree parent;
	public double fValue;
	public double hValue;
	public double gValue;
	
	public Quadtree() {
		nw = ne = sw = se  = null;
		rect = new Rectangle2D.Double();
		fValue = -1;
		occupied = 0;
	}
	
	public boolean contains(Point p) {
		if (rect.contains(p))
			return true;
		else
			return false;
	}
	
	public Point getCenter() {
		Point p = new Point();
		p.setLocation(rect.getCenterX(), rect.getCenterY());
		return p;
	}
	
	public String toString() {
		Point p = getCenter();
		return p.toString(); // + "["+rect.getWidth()+","+rect.getHeight()+"]";
	}
	
	public double distance(Quadtree q) {
		Point p1 = this.getCenter();
		Point p2 = q.getCenter();
		
		return p1.distance(p2);
	}

}
