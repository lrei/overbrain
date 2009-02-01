

import java.awt.geom.Point2D;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
import java.util.*;
import behaviour.*;
import simViewer.*;
import state.*;
import ciberIF.*;


/**
 * example of a basic agent
 * implemented using the java interface library.
 */
public class Brain {
	ciberIF cif;
	ViewerComms sim;
	String robName;


	double irSensor0;
	double irSensor1;
	double irSensor2;

	private State state;

	private int beaconToFollow;

	
	Planner planner;
	Vector<Point2D> path;
	Vector<Point2D> res = null;

	Behaviour controller;

	EstimatedMaze map;
	Comunicator com;
	KalmanFilter filter = null;
	
	BrainViewer viewer;
	
	boolean replan;



	public static void main(String[] args) {
		String host, robName;
		int pos; 
		int arg;

		//default values
		host = "localhost";
		robName = "Brain";
		pos = 1;
		//double IRSensorAngles[] = {15, -15, 60, -60};



		// parse command-line arguments
		try {
			arg = 0;
			while (arg<args.length) {
				if(args[arg].equals("-pos")) {
					if(args.length > arg+1) {
						pos = Integer.valueOf(args[arg+1]).intValue();
						arg += 2;
					}
				}
				else if(args[arg].equals("-robname")) {
					if(args.length > arg+1) {
						robName = args[arg+1];
						arg += 2;
					}
				}
				else if(args[arg].equals("-host")) {
					if(args.length > arg+1) {
						host = args[arg+1];
						arg += 2;
					}
				}
				else throw new Exception();
			}
		}
		catch (Exception e) {
			print_usage();
			return;
		}

		// create client
		Brain client = new Brain();

		client.robName = robName;

		// register robot in simulator
		client.cif.InitRobot(robName, pos, host);
		// connect viewer
		client.sim.connect(host, 6000);

		// main loop
		client.mainLoop();

	}

	// Constructor
	Brain() {

		cif = new ciberIF();
		state = new State();
		beaconToFollow = 0;
		controller = null;

		// MAP, COM, VIEWER
		map = new EstimatedMaze();
		sim = new ViewerComms();
		com = new Comunicator();
		//filter.setProcessNoiseCovariance(1);
		viewer = new BrainViewer(map.getMazeWidth(), map.getMazeHeight(), map.getCells());
		
//		ReadXmlMap a = new ReadXmlMap(
//				"/Users/rei/workspace/Brain/CiberRTSS06_FinalLab.xml",
//				"/Users/rei/workspace/Brain/CiberRTSS06_FinalGrid.xml");
//		Planner p = new Planner(a.getMap(), a.getStart(0), a.getTarget() , 0.5);
//		res = p.aStar();
//		viewer.addPoints(res);
//
//		for (int ii = 0; ii < res.size(); ii++)
//			System.out.println(res.get(ii).toString());
		//System.exit(0);
	}

	/** 
	 * reads a new message, decides what to do and sends action to simulator
	 */
	public void mainLoop () {

		while(true) {
			cif.ReadSensors();
			decide();
		}
	}

	public void decide() {
		if(cif.GetStartButton() == false)
			sim.sendToServer("<Start/>");
			//return;

		System.out.println("-------------------------------");
		/*
		 * BEGIN SENSOR UPDATE
		 */

		if(cif.IsObstacleReady(0))
			state.updateIR(0, cif.GetObstacleSensor(0));
		if(cif.IsObstacleReady(1))
			state.updateIR(1, cif.GetObstacleSensor(1));
		if(cif.IsObstacleReady(2))
			state.updateIR(2, cif.GetObstacleSensor(2));
		if(cif.IsObstacleReady(3))
			state.updateIR(3, cif.GetObstacleSensor(3));

		if(cif.IsGroundReady())
			state.updateGround(cif.GetGroundSensor());

		if(cif.IsBumperReady())
			state.updateBumper(cif.GetBumperSensor());

		//	    if(cif.IsCompassReady())
		//		    compass = cif.GetCompassSensor();

		if(cif.IsBeaconReady(beaconToFollow))
			state.updateBeacon(cif.GetBeaconSensor(beaconToFollow));

		Point2D.Double pos = new Point2D.Double();
		pos.x = cif.GetX(); pos.y = cif.GetY();
		state.updateLocation(pos);
		state.updateDirection(cif.GetDir());

		state.updateTime(cif.GetTime());

		state.updateButtons(cif.GetFinished(), cif.GetReturningLed(),
				cif.GetVisitingLed(), cif.GetStartButton(), cif.GetStopButton());

		/*
		 * END SENSOR UPDATE
		 */
		
		
		// Data from Viewer
		MousePlayer me = null;
		for (MousePlayer mouse : sim.mice.values()) {
			if(mouse.getPlayerName().compareToIgnoreCase(robName) == 0)
				me = mouse;
		}
		if(me != null) {
			System.out.println("Viewer data: x = "
					+ me.getPosition().getX()
					+ " y = " + me.getPosition().getY()
					+ " dir = " + me.getDirection());
		}

		// Data From State
//		System.out.println("x="+state.getPos().getX()+" y="+state.getPos().getY()
//				+ " dir="+state.getDir()+" ground="+state.getGround());
		if(filter == null) {
			filter = new KalmanFilter(state.getPos().getX(), state.getPos().getY(), Math.toRadians(state.getDir()));
		}
		else {
			Double[] u = state.getMotors();
			double d = (u[0] + u[1]) / 2;
			double a = (u[1]-u[0]);
			double[][] k = filter.correct(state.getPos().getX(), state.getPos().getY(), Math.toRadians(state.getDir()), d, a);
			
			if(me != null) {
				System.out.println("KErrX = " + (me.getPosition().getX() - k[0][0])
						+ " KErrY = " + (me.getPosition().getY() - k[1][0])
						+ "\nGErrX = " + (me.getPosition().getX() - state.getPos().getX())
						+ "GErrY = " + (me.getPosition().getY() - state.getPos().getY()));
			}
		}
//		System.out.println("\ntime= "+state.getTime()+" Measures: ir0="
//				+ state.getIR(0)+" ir1="+state.getIR(1)
//				+" ir2=" + state.getIR(2)+" ir3=" + state.getIR(3));
//		System.out.println("d0="+map.getDist(state.getIR(0))+" d1="+map.getDist(state.getIR(1)));


		// update map
		map.setEstimatedState(state);
		viewer.refresh(map.getCells(),
				map.translateCoord(state.getX()),
				map.translateCoord(state.getY()));

		if(state.getGround() != -1) {
			state.setFound();
			state.setTarget();
		}
		
		// For static map reading
		if(res != null && !state.isFound()) {
			System.out.println("--Following Path!--");
			state.setFound();
			state.setAnnouncing();
			controller = new PathBehaviour(state, res);
		}

		if(state.isFound() == true && state.isAnnouncing() == false) {
			map.clearPath(state.getPath());
			Planner planner = new Planner(map.reduce(1), state.getPos(), state.getStartPos(), 1.0);
			Vector<Point2D> plan = planner.aStar();
			controller = new PathBehaviour(state, plan);
			viewer.addPoints(plan);
			state.setAnnouncing();
			System.out.println("##########################");
			System.out.println("NEW PATH SET!");
			for(Point2D pt : plan)
				System.out.println(pt);
			System.out.println("##########################");
		}
		if(state.isFound() == true && state.isAnnouncing() == false) {
			map.clearPath(state.getPath());
			state.setAnnouncing();
			controller = new StopBehaviour();
		}

		if(state.isFound() && controller == null) {
			System.out.println("Finished.");
			cif.Finish();
//			System.exit(0);
		}

		boolean avoiding = avoidObstacles();
		
		if(state.targetFound() && !state.isFound() && !state.isAnnouncing()) {
			controller = new GoToBehaviour(state, state.getTarget().getX(), state.getTarget().getY());
			state.setAnnouncing();
		}

		if (!state.isFound() && controller == null) {
			if(state.isBeaconVisible() && 
					Math.abs(state.getBeaconDir()) < 90 &&
					controller == null)
				controller = new FollowBeaconBehaviour();
			else
				controller = new RandomWalkBehaviour();
				
			//			else if (!state.beenHere(20, 3.0)) {
			//				controller = new ExploreBehaviour(map);
			//			}
		}
		else if(!state.isFound() && controller != null) {
			if(state.isBeaconVisible() && controller.getPriority() < 2)
				controller = new FollowBeaconBehaviour();
			else if(avoiding  && !state.collision() && !state.beenHere() && controller.getPriority() < 1)
				controller = new WallFollowBehaviour();
		}

		/*
		 * EXECUTION
		 */
		if(controller != null && !avoiding) {
			System.out.println("EXECUTING: " + controller.getName());
			double [] act = controller.exec(state);
			checkMove(act);
			setEngine(act[0], act[1]);

			if(controller.isComplete()) {
				System.out.println("Behaviour Complete");
				controller = null;
			}
		}
		//		else if(controller == null && !avoiding)
		//			controller = new WallFollowBehaviour();

		else if(controller == null && !avoiding) 
			controller = new MoveFwdBehaviour();


		/*
		 * COMMUNICATION
		 */
		// RECEIVE
//		for(int i=0; i<5; i++)
//			if(cif.NewMessageFrom(i)) {
//				System.out.println("Received msg from "+i);
//				state.updateMsg(cif.GetMessageFrom(i));
//			}

		//map.setFromMsg(state);
		//state.clearMsg();

		// 100 bytes at most
		// 1 byte for state (S)earching (F)ound
		// 4 bytes / position => 24 positions, 3 unused bytes
		// - F: number of robots in Found state => 1 byte
		// - S: direction/10 => 3 bytes (e.g. -10 => -100)
		
		//SEND
		//System.out.println("Talking!");
		//cif.Say(com.talk(state));

		/*
		 * BEGIN SENSOR REQUEST
		 */


		if(cif.GetTime() % 2 == 0) {
			cif.RequestIRSensor(0);
			if(cif.GetTime() % 8 == 0 || beaconToFollow == cif.GetNumberOfBeacons())
				cif.RequestGroundSensor();
			else
				cif.RequestBeaconSensor(beaconToFollow);
		}
		else {
			cif.RequestIRSensor(1);
			cif.RequestIRSensor(2);
			cif.RequestIRSensor(3);
		}
		/*
		 * END SENSOR REQUEST
		 */
	}

	public void setEngine(double leftIn, double rightIn) {
		cif.DriveMotors(leftIn, rightIn);
		state.updateMotors(leftIn, rightIn);
	}


	public boolean avoidObstacles() {
		double turnDistance = 4.0;
		//double frontAvoidDistance = 1.0;
		double avoidDistance = 1.5;
		double powIn = 0.1;
		double backPowIn = 0.15;

		double ir0 = state.getIR(0);
		double ir1 = state.getIR(1);
		double ir2 = state.getIR(2);

		if (state.collision()) {
			setEngine(backPowIn ,-backPowIn);
			System.out.println("----- OUCH! COLLISION. ----- " +map.getWallProbability(state.getPos().getX(), state.getPos().getY()));
			return true;
		}


		if( ir0 > turnDistance 
				|| ir1 > turnDistance 
				||  ir2 > turnDistance) {
			setEngine(backPowIn ,-backPowIn);
			state.setWallDir(Direction.none);
			System.out.println("----- BACKTRACK! -----");
			return true;
		}
		else if(ir0 > avoidDistance) {
			double dir = state.getDir();
			if((-180 < dir && dir < -90) || (0 < dir && dir < 90))
				setEngine(powIn ,0);		// turn right
			else if ((-90 < dir && dir < 0) || (90 < dir && dir < 180))
				setEngine(0 , powIn);	// turn left
			else
				// TODO fix this so as to make sense from a global perspective
				setEngine(powIn ,0);		// turn right
			return true;
		}
		else if(ir1 > avoidDistance) {
			cif.DriveMotors(powIn, 0.0);
			state.updateMotors(powIn, 0.0);
			state.setWallDir(Direction.left);
			System.out.println("----- LEFT! -----");
			return true;
		}
		else if(ir2 > avoidDistance) {
			cif.DriveMotors(0.0, powIn);
			state.updateMotors(0.0, powIn);
			state.setWallDir(Direction.right);
			System.out.println("----- RIGHT! -----");
			return true;
		}

		return false;
	}

	public double [] calcEngine(double leftIn, double rightIn) {
		double res[] = new double[2];
		res[0] = (state.getMotors()[0] + leftIn)/2;
		res[1] = (state.getMotors()[1] + rightIn)/2;

		return res;
	}

	boolean checkMove(double [] act) {
		// map check
		Point2D future = calcFuture(act);
		System.out.println("FUTURE X="+future.getX()+" Y="+future.getY());
		if(map.isObstacle(future.getX(), future.getY())) {
			System.out.println(">>>PROBLEM!<<<");
			return false;
		}

		return true;
	}

	public Point2D calcFuture(double [] act) {
		Point2D.Double future = new Point2D.Double();
		double[] motor = calcEngine(act[0], act[1]);
		double dir = state.getDir()+Math.toDegrees(motor[1]-motor[0]);
		double lin = (act[0] + act[1])/2;
		double futureX = state.getPos().getX() + Math.cos(Math.toRadians(dir))*lin;
		double futureY = state.getPos().getY() + Math.sin(Math.toRadians(dir))*lin;
		future.setLocation(futureX, futureY);
		return future;
	}



	static void print_usage() {
		System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}


};

