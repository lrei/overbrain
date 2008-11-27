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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;


import behaviour.Behaviour;
import behaviour.FollowBeaconBehaviour;
import behaviour.MapBehaviour;
import behaviour.MoveDirectionBehaviour;
import behaviour.WallFollowBehaviour;

import state.EstimatedMaze;
import state.State;
import state.Direction;




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

	ReadXmlMap a;
	Planner planner;
	Vector<Point2D> path;

	Behaviour controller;

	EstimatedMaze map;

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

		// MAP
		map = new EstimatedMaze();
		controller = new MoveDirectionBehaviour(180);

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

	/**
	 * basic reactive decision algorithm, decides action based on current sensor values
	 */
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
				+ " dir="+state.getDir());
		System.out.println("time= "+state.getTime()+" Measures: ir0="
				+ state.getIR(0)+" ir1="+state.getIR(1)
				+" ir2=" + state.getIR(2)+" ir3=" + state.getIR(3));


		// update map
		map.setEstimatedState(state);

		if(state.getTime()==1000) {
			try {
				PrintWriter writer = new PrintWriter("map.txt");

				for(double jj = 14; jj > 0; jj-=0.5) {
					writer.print(""+Double.toString(jj).substring(0, 3));
					for(double ii = 0; ii < 28; ii+=0.5) {
						//writer.print(map.getWallProbability(ii, jj)+" ");
					
						if(map.isObstacle(ii, jj))
							writer.print("#");
						else
							writer.print("-");
					}
					writer.println("");
				}
				writer.close();
				System.exit(0);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}

		}



		boolean avoiding = avoidObstacles();

		if(state.isBeaconVisible())
			controller = new FollowBeaconBehaviour();

		if(avoiding && controller == null && !state.collision())
			controller = new WallFollowBehaviour();


		if(controller != null && !avoiding) {
			double [] act = controller.exec(state);
			setEngine(act[0], act[1]);

			if(controller.isComplete())
				controller = null;
		}
		else if(controller == null && !avoiding)
			controller = new WallFollowBehaviour();

		else if(controller == null && !avoiding)
			controller = new MoveDirectionBehaviour(180);



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
		double turnDistance = 5.0;
		//double frontAvoidDistance = 1.0;
		double avoidDistance = 1.2;
		double powIn = 0.1;
		double backPowIn = 0.15;

		double ir0 = state.getIR(0);
		double ir1 = state.getIR(1);
		double ir2 = state.getIR(2);

		if (state.collision()) {
			cif.DriveMotors(backPowIn ,-backPowIn);
			state.updateMotors(backPowIn, -backPowIn);
			System.out.println("----- OUCH! COLLISION. ----- " +map.getWallProbability(state.getPos().getX(), state.getPos().getY()));
			return true;
		}

		// map check
		double dir = state.getDir();
		double lin = (state.getMotors()[0] + state.getMotors()[1])/2;
		double futureX = state.getPos().getX() + Math.cos(dir)*lin;
		double futureY = state.getPos().getY() + Math.sin(dir)*lin;
		System.out.println("FUTURE X="+futureX+" Y="+futureY);
		if(map.isObstacle(futureX, futureY))
			System.out.println(">>>PROBLEM!<<<");

		// get dir, get futureX and futureY
		// map.get(state.getPos().getX(), state.getPos().getY())

		if( ir0 > turnDistance 
				|| ir1 > turnDistance 
				||  ir2 > turnDistance) {
			cif.DriveMotors(backPowIn,-backPowIn);
			state.updateMotors(backPowIn, -backPowIn);
			state.setWallDir(Direction.none);
			System.out.println("----- BACKTRACK! -----");
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



	static void print_usage() {
		System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}


};

