
import java.awt.geom.Point2D;
import java.util.Vector;

import state.*;


public class Planner {
	double ov = 0.5;	// ov defines the threshhold value between occupied and free
	double minDistance;
	EstimatedCell [][] cellMap;
	MapNode [][] map;

	MapNode target;
	MapNode start;
	
	public Planner(EstimatedCell [][] cellMap, Point2D start, Point2D target, double minDistance) {
		this.cellMap = cellMap;
		this.minDistance = minDistance;
	
		map = new MapNode[cellMap.length][cellMap[0].length];
		for(int xx = 0; xx < cellMap.length; xx++) {
			for(int yy = 0; yy < cellMap[xx].length; yy++) {
				map[xx][yy] = new MapNode();
				map[xx][yy].point.setLocation(xx, yy);
				map[xx][yy].occupied = cellMap[xx][yy].getWallProbability();
			}
		}
		
		this.start = map[(int) start.getX()][(int) start.getY()];
		this.target = map[(int) target.getX()][(int) target.getY()];
	}
	
	public Vector<Point2D> aStar() {
		Vector<MapNode> closedSet = new Vector<MapNode>(); 
		Vector<MapNode> openSet = new Vector<MapNode>();
		
		start.parent = null;
		start.gValue = 0;
		start.hValue = h(start);
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
			
			if(cur == target)	// is current node the goal?
				break;
			
			Vector<MapNode> suc = successors(cur);	// generate successors
			//printList(suc);
			for (MapNode s: suc) {
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
				s.hValue = h(s);
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
		path.add(target.getCenter());
		while (q != null && q != start) {
			path.add(0, q.getCenter());
			q = q.parent;
		}
		
		return path;
	}
	
	
	private Vector<MapNode> successors(MapNode n) {
		Vector<MapNode> suc = new Vector<MapNode>();
		Vector<MapNode> res = new Vector<MapNode>();
		int x = (int) n.point.getX();
		int y = (int) n.point.getY();
		
		if(x > 0)
			suc.add(map[x-1][y]);
		if(y > 0)
			suc.add(map[x][y-1]);
		if(x > 0 && y > 0)
			suc.add(map[x-1][y-1]);
		if(x < map.length-1)
			suc.add(map[x+1][y]);
		if(y < map[0].length-1)
			suc.add(map[x][y+1]);
		if(x < map.length-1 && y < map[0].length-1)
			suc.add(map[x+1][y+1]);
		if(x < map.length-1 && y > 0)
			suc.add(map[x+1][y-1]);
		if(x > 0 && y < map[0].length-1)
			suc.add(map[x-1][y+1]);
		
		// remove sucessors that have obstacles
		for(MapNode s: suc)
			if(s.occupied < ov)
				res.add(s);
		
		return res;
	}
	
	private double h(MapNode x) {
		return x.distance(target);
	}
	
	private MapNode getBest(Vector<MapNode> set) {
		double min = h(set.get(0));
		MapNode best = set.get(0);
		for (int ii = 1; ii < set.size(); ii++) {
			double h = 	h(set.get(ii));
			if (h < min) {
				min = h;
				best = set.get(ii);
			}
		}
		return best;
	}

}
