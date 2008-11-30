package state;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Quadtree {
	public Rectangle2D.Double rect;
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
	
	public boolean contains(Point2D p) {
		if (rect.contains(p))
			return true;
		else
			return false;
	}
	
	public Point2D getCenter() {
		Point2D.Double p = new Point2D.Double();
		p.setLocation(rect.getCenterX(), rect.getCenterY());
		return p;
	}
	
	public String toString() {
		Point2D p = getCenter();
		return p.getX() +", "+p.getY() + "["+rect.getWidth()+","+rect.getHeight()+"]";
	}
	
	public double distance(Quadtree q) {
		Point2D p1 = this.getCenter();
		Point2D p2 = q.getCenter();
		
		return p1.distance(p2);
	}

}
