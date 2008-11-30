/*
    This file is part of ciberRatoToolsSrc.

    Copyright (C) 2001-2008 Universidade de Aveiro

    ciberRatoToolsSrc is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    ciberRatoToolsSrc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.awt.geom.Point2D;
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
import java.util.*;


import behaviour.*;

import state.EstimatedCell;
import state.EstimatedMaze;
import state.Quadtree;
import state.ReadXmlMap;
import state.State;
import state.Direction;
import state.Viewer;




import ciberIF.*;


/**
 * example of a basic agent
 * implemented using the java interface library.
 */
public class Brain {
	ciberIF cif;
	String robName;

	double irSensor0;
	double irSensor1;
	double irSensor2;

	private State state;

	private int beaconToFollow;

	
	Planner planner;
	Vector<Point2D> path;

	Behaviour controller;

	EstimatedMaze map;
	Comunicator com;

//	Viewer viewer;
	
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
		com = new Comunicator();
		//viewer = new Viewer(map.getMazeWidth(), map.getMazeHeight(), map.getCells());
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
			return;

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

		System.out.println("x="+state.getPos().getX()+" y="+state.getPos().getY()
				+ " dir="+state.getDir()+" ground="+state.getGround());
		System.out.println("time= "+state.getTime()+" Measures: ir0="
				+ state.getIR(0)+" ir1="+state.getIR(1)
				+" ir2=" + state.getIR(2)+" ir3=" + state.getIR(3));
		System.out.println("d0="+map.getDist(state.getIR(0))+" d1="+map.getDist(state.getIR(1)));


		// update map
		map.setEstimatedState(state);
//		for(int i = 0; i  < 20 ; i++)
//			for (int j = 0; j < 20; j++)
//				mymap[i][j].setWallProbability(1.5);
		//viewer.refresh(map.getCells());

		if(state.getGround() != -1) {
			state.setFound();
			state.setTarget();
		}

//		if(state.isFound() == true && state.isAnnouncing() == false) {
//			map.clearPath(state.getPath());
//			//Quadtree qt = map.toQuadtree();
//			//Planner planner = new Planner(qt, state.getPos(), state.getStartPos(), 1.0);
//			//Vector<Point2D> plan = planner.aStar();
//			plan 
//			controller = new PathBehaviour(state, plan);
//			state.setAnnouncing();
//			System.out.println("##########################");
//			System.out.println("NEW PATH SET!");
//			System.out.println(plan);
//			System.out.println("##########################");
//		}
		if(state.isFound() == true && state.isAnnouncing() == false) {
			map.clearPath(state.getPath());
			state.setAnnouncing();
			controller = new StopBehaviour();
		}

		if(state.isFound() && controller == null) {
			cif.Finish();
//			System.exit(0);
		}

		boolean avoiding = avoidObstacles();
		
		if(state.targetFound() && !state.isFound() && !state.isAnnouncing()) {
			controller = new GoToBehaviour(state, state.getTarget().getX(), state.getTarget().getY());
			state.setAnnouncing();
		}

		if (!state.isFound() && controller == null) {
			if(state.isBeaconVisible() && controller == null)
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
		if(map.isObstacle(future.getX(), future.getY()))
			System.out.println(">>>PROBLEM!<<<");

		return false;
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

