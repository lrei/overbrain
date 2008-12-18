
import java.awt.geom.Point2D;
import java.util.Vector;

import state.Quadtree;


public class Planner {
	double ov = 0.5;	// ov defines the threshhold value between occupied and free
	double minDistance;
	Quadtree root;
	Quadtree start;
	Quadtree target;
	Point2D targetPoint;
	Point2D startPoint;
	
	public Planner(Quadtree root, Point2D start, Point2D target, double minDistance) {
		this.root = root;
		this.start = findPoint(root, start);
		if(this.start == null) {
			System.out.println("Starting point not found inside map.");
			System.exit(0);
		}
		//System.out.println("############");
		targetPoint = target;
		this.target = findPoint(root, target);
		if(this.target == null)  {
			System.out.println("Target not found inside map.");
			System.exit(0);
		}
		this.minDistance = minDistance;
	}
	
	public void printList(Vector<Quadtree> l) {
//		String res = "";
//		for (int ii = 0; ii < l.size(); ii++)
//			res += l.get(ii).toString() ;
		System.out.println(l.toString());
	}
	
	public Vector<Point2D> aStar() {
		Vector<Quadtree> closedSet = new Vector<Quadtree>(); 
		Vector<Quadtree> openSet = new Vector<Quadtree>();
		

		start.parent = null;
		start.gValue = 0;
		start.hValue = h(root, target);
		start.fValue = start.gValue + start.hValue;
		
		openSet.add(start);
		
		while(openSet.size() != 0) {
//			System.out.println("---------------");
//			System.out.println("openSet");
//			printList(openSet);
//			System.out.println("closedSet");
//			printList(closedSet);
			
			// get the best node of the openSet
			Quadtree cur = getBest(openSet);
			openSet.remove(cur);
			
			if(cur == target)	// is current node the goal?
				break;
			
			Vector<Quadtree> suc = successors(cur);	// generate successors
			//printList(suc);
			for (int ii = 0; ii < suc.size(); ii++) {
				Quadtree s = suc.get(ii);
				// is it on the open list?
				if (openSet.contains(s)) { 
					double sf = s.hValue + cur.gValue + cur.distance(s);
					if (s.fValue > sf) { // current has a better score
						//System.out.println("f=" +s.fValue+", newf ="+sf);
						openSet.remove(s);
						//System.out.println("Removing " + s.toString() + " from openSet");
					}
					else
						continue; // discard successor
				}
				// is it on the closed list?
				if (closedSet.contains(s)) {
					double sf = s.hValue + cur.gValue + cur.distance(s);
					if (s.fValue > sf) { // current has a better score
						closedSet.remove(s);
						//System.out.println("Removing " + s.toString() + " from closedSet");
					}
					else
						continue; // discard successor
				}
				// calculate h and f
				s.hValue = h(s, target);
				s.gValue = cur.gValue + cur.distance(s);
				s.fValue = s.hValue + s.gValue;
				
				s.parent = cur; // set current node as the parent
				
				openSet.add(s); // add the successor to the open list
				//System.out.println("Adding " + s.toString() + " to openSet");
			}
			closedSet.add(cur); // add current node to the closed list
			//System.out.println("Adding " + cur.toString() + " to closedSet");
		}
		
		return this.reconstruct();
		
	}
	
	private Vector<Point2D> reconstruct() {
		Vector<Point2D> path = new Vector<Point2D>();
		
		Quadtree q = target;
		path.add(targetPoint);
		while (q != null && q != start) {
			path.add(0, q.getCenter());
			q = q.parent;
		}
		
		return path;
	}
	
	
	private Vector<Quadtree> successors(Quadtree q) {
		Vector<Quadtree> suc = new Vector<Quadtree>();
		Point2D.Double p = new Point2D.Double();
		Quadtree a;
		double x = q.rect.getCenterX();
		double y = q.rect.getCenterY();
		double w = q.rect.getWidth() + minDistance;
		double h = q.rect.getHeight() + minDistance;
		
		//System.out.println("x=" + x + " y=" + y + " w=" + w);
		
		p.setLocation(x+w, y);
		a = findPoint(root, p);
		if (a != null)
			suc.add(a);
		p.setLocation(x-w, y);
		a = findPoint(root, p);
		if (a != null)
			suc.add(a);
		p.setLocation(x, y+h);
		a = findPoint(root, p);
		if (a != null)
			suc.add(a);
		p.setLocation(x, y-h);
		a = findPoint(root, p);
		if (a != null)
			suc.add(a);
		
		
		return suc;
	}
	
	private double h(Quadtree x, Quadtree target) {
		return x.distance(target);
	}
	
	private Quadtree getBest(Vector<Quadtree> set) {
		double min = h(set.get(0), target);
		Quadtree best = set.get(0);
		for (int ii = 1; ii < set.size(); ii++) {
			double h = 	h(set.get(ii), target);
			if (h < min) {
				min = h;
				best = set.get(ii);
			}
		}
		return best;
	}
	
	public Quadtree findPoint(Quadtree q, Point2D p) {
		//System.out.println("findPoint " + p.getX()+", "+p.getY());
		if (q == null) {
			//System.out.println("EMPTY");
			return null;
		}
		else if (q.contains(p) == false) {
			//System.out.println(q.toString() + " does not contain point");
			return null;
		}
		else if (q.occupied < ov) {
			//System.out.println("FOUND: " + q.toString());
			return q;
		}
		else {
			//System.out.println("Looking inside " + q.toString());
			// NW
			Quadtree a = findPoint(q.nw, p);
			if (a != null) {
				//System.out.println("FOUND!");
				return a;
			}
			// NE
			a = findPoint(q.ne, p);
			if (a != null) {
				//System.out.println("FOUND!");
				return a;
			}
			// SW
			a = findPoint(q.sw, p);
			if (a != null) {
				//System.out.println("FOUND!");
				return a;
			}
			// SE
			a = findPoint(q.se, p);
			if (a != null) {
				//System.out.println("FOUND!");
				return a;
			}
			
			return root;
		}
		//System.out.println("NOF FOUND!!!");
		
		//return null;
	}

}
