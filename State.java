import java.awt.geom.Point2D;
import java.util.Vector;

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;


public class State {
	private Vector<Point2D> path;		// from the GPS
	private Vector<Double> direction;	// from the GPS

	private Vector<Double[]> motors; // left = [0], right = [1]

	private boolean endLed;
	private boolean returningLed;
	private boolean visitingLed;

	private Vector<Double> irSensor0;
	private Vector<Double> irSensor1;
	private Vector<Double> irSensor2;
	private Vector<Double> irSensor3;
	private double irLatency = 1.0;
	private Vector<beaconMeasure> beacon;

	private int ground;
	private boolean collision;

	private double time;

	private boolean startBut;
	private boolean stopBut;
	
	private Point2D dest;
	private double destDir;


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

		ground = 0;
		collision = false;

		time = 0;
	}

	public void updateSensors(ciberIF cif) {

		time = cif.GetTime();

		if(cif.IsObstacleReady(0)) {
			cif.RequestIRSensor(0);
			irSensor0.add(0, cif.GetObstacleSensor(0));
		}
		if(cif.IsObstacleReady(1)) {
			cif.RequestIRSensor(1);
			irSensor1.add(0, cif.GetObstacleSensor(1));
		}
		if(cif.IsObstacleReady(2)) {
			cif.RequestIRSensor(2);
			irSensor2.add(0, cif.GetObstacleSensor(2));
		}
		if(cif.IsObstacleReady(3)) {
			cif.RequestIRSensor(3);
			irSensor3.add(0, cif.GetObstacleSensor(3));
		}
		//		if(cif.IsCompassReady())
		//			compass = cif.GetCompassSensor();
		if(cif.IsGroundReady())
			ground = cif.GetGroundSensor();

		if(cif.IsBeaconReady(0))
			beacon.add(0,cif.GetBeaconSensor(0));

		Point2D.Double loc = new Point2D.Double();
		loc.x = cif.GetX();
		loc.y = cif.GetY();
		path.add(0, loc);

		direction.add(0, cif.GetDir());

		if (cif.IsBumperReady())
			collision = cif.GetBumperSensor();
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

}
