
import java.awt.geom.Point2D;
import java.util.Vector;

import state.Quadtree;



public class MPlanner {
	double ov = 0.5;	// ov defines the threshhold value between occupied and free
	double minDistance;
	double [][] map;
	Point2D targetPoint;
	Point2D startPoint;
	MapNode start;
	MapNode target;
	
	public MPlanner(double [][] map, Point2D start, Point2D target, double minDistance) {
		this.map = map;
		this.startPoint = start;
		this.targetPoint = target;
		this.minDistance = minDistance;
		this.start = new MapNode();
		this.target = new MapNode();
		this.start.point.setLocation((int) startPoint.getX(), (int) startPoint.getY());
		this.target.point.setLocation((int) targetPoint.getX(), (int) targetPoint.getY());
	} 
	
	public void printList(Vector<MapNode> l) {
//		String res = "";
//		for (int ii = 0; ii < l.size(); ii++)
//			res += l.get(ii).toString() ;
		System.out.println(l.toString());
	}
	
	public Vector<Point2D> aStar() {
		Vector<MapNode> closedSet = new Vector<MapNode>(); 
		Vector<MapNode> openSet = new Vector<MapNode>();
		

		start.parent = null;
		start.gValue = 0;
		start.hValue = h(start.point);
		start.fValue = start.gValue + start.hValue;
		
		openSet.add(start);
		
		while(openSet.size() != 0) {
//			System.out.println("---------------");
//			System.out.println("openSet");
//			printList(openSet);
//			System.out.println("closedSet");
//			printList(closedSet);
			
			// get the best node of the openSet
			MapNode cur = getBest(openSet);
			openSet.remove(cur);
			
			if(cur.point.distance(targetPoint) < 1)	// is current node the goal?
				break;
			
			Vector<MapNode> suc = successors(cur);	// generate successors
			printList(suc);
			for (int ii = 0; ii < suc.size(); ii++) {
				MapNode s = suc.get(ii);
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
				s.hValue = h(s.point);
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
		
		MapNode q = target;
		path.add(targetPoint);
		while (q != null && q != start) {
			path.add(0, q.getCenter());
			q = q.parent;
		}
		
		return path;
	}
	
	
	private Vector<MapNode> successors(MapNode q) {
		Vector<MapNode> suc = new Vector<MapNode>();
		
		int x = (int) q.point.getX();
		int y = (int) q.point.getY();
		
		if (x-1 >= 0) {
			MapNode n = new MapNode();
			n.point.setLocation(x-1, y);
			if (map[x-1][y] < 0.6)
				suc.add(n);
		}
		if (y-1 >= 0) {
			MapNode n = new MapNode();
			n.point.setLocation(x, y-1);
			if (map[x][y-1] < 0.6)
				suc.add(n);
		}
		if (y-1 >= 0 && x-1 >= 0) {
			MapNode n = new MapNode();
			n.point.setLocation(x-1, y-1);
			if (map[x-1][y-1] < 0.6)
				suc.add(n);
		}
		if (y+1 < map[0].length) {
			MapNode n = new MapNode();
			n.point.setLocation(x, y+1);
			if (map[x][y+1] < 0.6)
				suc.add(n);
		}
		if (x+1 < map.length) {
			MapNode n = new MapNode();
			n.point.setLocation(x+1, y);
			if (map[x+1][y] < 0.6)
				suc.add(n);
		}
		if (x+1 < map.length && y+1 < map[0].length) {
			MapNode n = new MapNode();
			n.point.setLocation(x+1, y+1);
			if (map[x+1][y+1] < 0.6)
				suc.add(n);
		}
			
		
		return suc;
	}
	
	private double h(Point2D loc) {
		return loc.distance(targetPoint);
	}
	
	private MapNode getBest(Vector<MapNode> set) {
		double min = h(set.get(0).point);
		MapNode best = set.get(0);
		for (int ii = 1; ii < set.size(); ii++) {
			double h = 	h(set.get(ii).point);
			if (h < min) {
				min = h;
				best = set.get(ii);
			}
		}
		return best;
	}
}