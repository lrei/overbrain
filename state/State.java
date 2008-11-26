package state;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import ciberIF.beaconMeasure;


public class State {
	private Vector<Point2D> path;		// from the GPS
	private int lastMapUpdate;
	private Vector<Double> direction;	// from the GPS

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

	private double time;
	
	private Point2D dest;
	private double destDir;
	
	Direction wallDir;


	public State() {
		path = new Vector<Point2D>();
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

		time = 0;
		wallDir = Direction.none;
		lastMapUpdate = 0;
	}

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

	public Double getDir() {
		return direction.get(0);
	}

	public void updateMotors(double leftIn, double rightIn) {
		Double[] update = new Double[2];
		double left = motors.get(0)[0];
		double right = motors.get(0)[0];

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
	
	public double getBeaconDir() {
		return this.beacon.get(0).beaconDir;
	}
	
	public boolean isBeaconVisible() {
		if (this.beacon.size()==0)
			return false;
		
		return this.beacon.get(0).beaconVisible;
	}
}
