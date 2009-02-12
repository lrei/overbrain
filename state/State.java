package state;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import ciberIF.beaconMeasure;


public class State {
	int id;
	private Vector<Point2D> path;		// from the GPS
	private Vector<Point2D> msgPath;
	private int lastMapUpdate;
	private Vector<Double> direction;	// from the GPS
	private Vector<Point2D> solution;
	
	String[] msgs;

	private Vector<Double[]> motors; // left = [0], right = [1]

	private boolean endLed;
	private boolean returningLed;
	private boolean visitingLed;
	private boolean startBut;
	private boolean stopBut;

	private Vector<Double> irSensor0;
	private Vector<Double> irSensor1;
	private Vector<Double> irSensor2;
	private Vector<Double> irSensor3;
	private Vector<beaconMeasure> beacon;

	private int ground;
	private boolean collision;
	private boolean foundGoal;
	private boolean announcing;

	private double time;
	
	private Point2D dest;
	private double destDir;
	
	Direction wallDir;
	private Point2D target;
	private Point2D fTarget;
	private boolean targetFound;


	public State(int id) {
		this.id = id;
		path = new Vector<Point2D>();
		msgPath = new Vector<Point2D>();
		direction = new Vector<Double>();

		motors = new Vector<Double[]>();
		Double[] initM = new Double[2];
		initM[0] = initM[1] = 0.0; motors.add(initM);

		irSensor0 = new Vector<Double>();
		irSensor1 = new Vector<Double>();
		irSensor2 = new Vector<Double>();
		irSensor3 = new Vector<Double>();
		beacon = new Vector<beaconMeasure>();

		ground = -1;
		collision = false;
		foundGoal = false;
		announcing = false;
		targetFound = false;

		time = 0;
		wallDir = Direction.none;
		lastMapUpdate = 0;
		
		msgs = new String[5];
		for(int ii = 0; ii < 5; ii++)
			msgs[ii] = null;
		
	}
	
	public int getId() { return this.id; }

	public void updateTime(double time) {
		this.time = time;
	}
	
	public void updateIR(int index, double value) {
		if(index == 0)
			this.irSensor0.add(0, value);
		if(index == 1)
			this.irSensor1.add(0, value);
		if(index == 2)
			this.irSensor2.add(0, value);
		if(index == 3)
			this.irSensor3.add(0, value);
	}
	
	public void setFound() {
		this.foundGoal = true;
	}
	public boolean isFound() {
		return this.foundGoal;
	}
	public void setAnnouncing() {
		this.announcing = true;
	}
	public boolean isAnnouncing() {
		return this.announcing;
	}
	
	
	public Point2D getStartPos() {
		return path.lastElement();
	}
	
	public void updateGround(int ground) {
		this.ground = ground;
	}
	
	public void updateBeacon(beaconMeasure beaconValue) {
			beacon.add(0, beaconValue);
	}
	
	public void updateLocation(Point2D cur) {
		path.add(0, cur);
	}
	
	public void updateDirection(double dir) {
		direction.add(0, dir);
	}
	
	public void updateBumper(boolean value) {
		collision = value;
	}
	
	public void updateButtons(boolean endLed, boolean returningLed,
			boolean visitingLed, boolean startBut, boolean stopBut) {
		this.endLed = endLed;
		this.returningLed = returningLed;
		this.visitingLed = visitingLed;
		this.startBut = startBut;
		this.stopBut = stopBut;
		
	}
	
	public void setWallDir(Direction dir) {
		this.wallDir = dir;
	}
	
	public Direction getWallDir() {
		return this.wallDir;
	}
	
	public Point2D getPos() {
		return path.get(0);
	}
	
	public double getX() {
		return getPos().getX();
	}
	public double getY() {
		return getPos().getY();
	}

	public Double getDir() {
		return direction.get(0);
	}
	
	public Vector<Point2D> getPath() {
		return path;
	}

	public void updateMotors(double leftIn, double rightIn) {
		Double[] update = new Double[2];
		double left = motors.get(0)[0];
		double right = motors.get(0)[1];

		left = (left + leftIn)/2;
		right = (right + rightIn)/2;

		update[0] = left; update[1] = right;
		motors.add(0, update);
	}

	public Double[] getMotors() {
		return motors.get(0);
	}

	public double getIR(int index) {
		if (index == 0 && !irSensor0.isEmpty())
				return irSensor0.get(0);
		else if (index == 1 && !irSensor1.isEmpty())
				return irSensor1.get(0);
		else if (index == 2 && !irSensor2.isEmpty())
				return irSensor2.get(0);
		else if (index == 3 && !irSensor3.isEmpty())
				return irSensor3.get(0);

		return 0.0;
	}

	public double getTime() {
		return time;
	}

	public boolean collision() {
		return collision;
	}
	
	public void addDest(Point2D d) {
		this.dest = d;
		double vx = dest.getX() - getPos().getX();
		double vy = dest.getY() - getPos().getY();
		this.destDir = Math.toDegrees(Math.atan2(vy, vx));
	}
	
	public double getDestDir() {
		return destDir;
	}
	
	public int getGround() {
		return ground;
	}
	
	public List<Point2D> requestPath() {
		List<Point2D> result;
		result = path.subList(0, path.size() - lastMapUpdate);
		lastMapUpdate = path.size();
		return result;
	}
	
	public boolean beenHere() {
		double radius = 0.5;
		double curX = getPos().getX();
		double curY = getPos().getY();
		
		for (int ii = 8; ii < path.size(); ii++) {
			double x = path.get(ii).getX();
			double y = path.get(ii).getY();
			
			if((curX-radius < x && x < curX+radius) && (curY-radius < y && y <curY+radius))
				return true;
		}
		
		return false;
	}
	
	public boolean beenHere(double curX, double curY) {
		double radius = 0.5;
		
		for (int ii = 3; ii < path.size(); ii++) {
			double x = path.get(ii).getX();
			double y = path.get(ii).getY();
			
			if((curX-radius < x && x < curX+radius) && (curY-radius < y && y <curY+radius))
				return true;
		}
		
		return false;
	}
	public boolean beenHere(int n, double radius) {
		
		double max = 0;
		for (int ii = 0; ii < path.size() && ii < n; ii++) {
			double val = Math.sqrt(Math.pow(path.get(ii).getX(),2)
					+ Math.pow(path.get(ii).getY(),2));
			max = Math.max(max, val);
		}
			
		if(max > radius)
			return false;
		else
			return true;
	}
	
	public double getBeaconDir() {
		return this.beacon.get(0).beaconDir;
	}
	
	public boolean isBeaconVisible() {
		if (this.beacon.size()==0)
			return false;
		
		return this.beacon.get(0).beaconVisible;
	}

	public void setTarget() {
		this.target = getPos();
	}

	/*
	 * State keeps received MSGs so that it can "hop" them if
	 * necessary and possible
	 */
	public void updateMsg(String msg, int id) {
		//id = id - 1;
		msgs[id] = msg;
	}
	
	public String getMsgFrom(int id) {
		return msgs[id];
	}
	
	private void setTargetFound(boolean b) {
		this.targetFound = true;
	}
	public boolean targetFound() {
		return this.targetFound;
	}
	public Point2D getTarget() {
		return this.fTarget;
	}

	public void clearMsg(int id) {
		//id = id - 1;
		msgs[id] = null;
		
	}

	public void setSolution(Vector<Point2D> plan) {
		this.solution = plan;
		
	}

	public Vector<Point2D> getSolution() {
		return solution;
	}


}

