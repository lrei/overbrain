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
import java.awt.geom.Point2D.Double;
import java.io.*;
import java.util.*;


import ciberIF.*;


/**
 * example of a basic agent
 * implemented using the java interface library.
 */
public class jClient {
	ciberIF cif;
	private String robName;
	private double irSensor0, irSensor1, irSensor2, compass;
	private beaconMeasure beacon;
	private int    ground;
	private boolean collision;
	private double x,y,dir;
	private double left, right;
	private double tolerance = 0.5;

	private int beaconToFollow;
	private double oldDir;
	private double oldAngleDiff;
	private double savedTime;
	
	Vector<Point2D> path;



	public static void main(String[] args) {
		String host, robName;
		int pos; 
		int arg;

		//default values
		host = "localhost";
		robName = "jClient";
		pos = 1;

		

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
		jClient client = new jClient();

		client.robName = robName;

		// register robot in simulator
		client.cif.InitRobot(robName, pos, host);

		// main loop
		client.mainLoop();

	}

	// Constructor
	jClient() {

		cif = new ciberIF();
		beacon = new beaconMeasure();

		beaconToFollow = 0;
		ground=-1;
		left = right = 0;
		
		/*
		 * READ THE MAP AND MAKE THE PLAN
		 */
		ReadXmlMap a = new ReadXmlMap("CiberRTSS06_FinalLab.xml","CiberRTSS06_FinalGrid.xml");
		Planner p = new Planner(a.getQuadtree(), a.getStart(0), a.getTarget() , 0.5);
		Vector<Quadtree> qpath = p.aStar();
		path = new Vector<Point2D>();
		
		for (int ii = 0; ii < qpath.size(); ii++) {	
			path.add(ii, qpath.get(ii).getCenter());
		}
		for (int ii = 0; ii < path.size(); ii++) {	
			System.out.println(path.get(ii));
		}
		

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



		if(cif.IsObstacleReady(0))
			irSensor0 = cif.GetObstacleSensor(0);
		if(cif.IsObstacleReady(1))
			irSensor1 = cif.GetObstacleSensor(1);
		if(cif.IsObstacleReady(2))
			irSensor2 = cif.GetObstacleSensor(2);
		if(cif.IsCompassReady())
			compass = cif.GetCompassSensor();
		if(cif.IsGroundReady())
			ground = cif.GetGroundSensor();

		if(cif.IsBeaconReady(beaconToFollow))
			beacon = cif.GetBeaconSensor(beaconToFollow);

		x = cif.GetX();
		y = cif.GetY();
		dir = cif.GetDir();
		if (cif.IsBumperReady())
			collision = cif.GetBumperSensor();


		System.out.println("x="+x+" y="+y+" dir="+dir);
		moveFwd(0.1);
		
		if(this.path.size() > 0) {
			Point2D.Double g = (Point2D.Double) this.path.get(0);
			System.out.println("Goal is: "+g.x+", "+g.y);
			boolean check = go(g.x, g.y);
			if(check) {
				System.out.println("---Reached Check " +g.x+", "+g.y+" at "+x+", "+y);
				this.path.remove(0);
			}
		}
		else
			stop();

	

		//System.out.println("Time is " + cif.GetTime());
		//System.out.println("Measures: ir0=" + irSensor0 + " ir1=" + irSensor1 + " ir2=" + irSensor2 + "\n");
		//System.out.println("Measures: x=" + x + " y=" + y + " dir=" + dir);

		//		if(ground==beaconToFollow)
		//			cif.Finish();
		//		else {
		//			if(irSensor0>4.0 || irSensor1>4.0 ||  irSensor2>4.0) 
		//				cif.DriveMotors(0.1,-0.1);
		//			else if(irSensor1>1.0) cif.DriveMotors(0.1,0.0);
		//			else if(irSensor2>1.0) cif.DriveMotors(0.0,0.1);
		//			else {
		//				double destX = path.get(0).getCenter().getX();
		//				double destY = path.get(0).getCenter().getX();
		//
		//				// calculate direction
		//				double vx = destX - x;
		//				double vy = destY - y;
		//				
		//				double destDir = Math.atan2(vy, vx);
		//				System.out.println("Objective: x=" + destX + " y=" + destY + " dir=" + destDir);
		//				// rotate
		//				double angleDiff = destDir - dir;
		//				if (angleDiff > Math.PI)
		//					angleDiff -= Math.PI * 2;
		//				
		//				if (angleDiff < -Math.PI)
		//					angleDiff += Math.PI * 2;
		//				
		//				
		//
		//				System.out.println("dir: " + dir);
		//			}
		//	        else if(beacon.beaconVisible && beacon.beaconDir > 20.0) 
		//		    cif.DriveMotors(0.0,0.1);
		//	        else if(beacon.beaconVisible && beacon.beaconDir < -20.0) 
		//		    cif.DriveMotors(0.1,0.0);
		//	        else cif.DriveMotors(0.1,0.1);
		//	}

		//	for(int i=0; i<5; i++)
		//		if(cif.NewMessageFrom(i))
		//			System.out.println("Message: From " + i + " to " + robName + " : \"" + cif.GetMessageFrom(i)+ "\"");
		//
		//	cif.Say(robName);

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
		}

	}
	
	public void rotate(double angle) {
		double curAngle = dir;
		double angleDiff = angle - curAngle;
		
		
		//angleDiff = Math.toRadians(angleDiff);
		if (angleDiff > Math.toDegrees(Math.PI))
			angleDiff -= Math.toDegrees(Math.PI) * 2;
		
		if (angleDiff < -Math.toDegrees(Math.PI))
			angleDiff += Math.toDegrees(Math.PI) * 2;
		
		
		double rightIn = angleDiff * 0.15/180;
		double leftIn = -angleDiff * 0.15/180;
		cif.DriveMotors(leftIn, rightIn);
		
		System.out.println("angleDiff="+angleDiff+" rightIn="
				+rightIn+" leftIn="+leftIn);
		
		left = (left + leftIn)/2;
		right = (right + rightIn)/2;
	}
	
	public void moveFwd(double percent) {
		double leftIn;
		double rightIn;
		
		leftIn = rightIn = 0.15*(percent);
		cif.DriveMotors(leftIn, rightIn);
		
		left = (left + leftIn)/2;
		right = (right + rightIn)/2;
	}
	public boolean stop() {
		double leftIn;
		double rightIn;
		
		leftIn = rightIn = 0;
		cif.DriveMotors(leftIn, rightIn);
		
		left = (left + leftIn)/2;
		right = (right + rightIn)/2;
		
		if(left == 0 && right == 0)
			return true;
		else
			return false;
	}
	
	public boolean go(double destX, double destY) {
		double vx = destX - x;
		double vy = destY - y;
		
		if((Math.abs(vx) < tolerance) && (Math.abs(vy) < tolerance))
			return true;
		
		double destDir = Math.toDegrees(Math.atan2(vy, vx));
		System.out.println("dest="+destX+" ,"+destY+" =>"+destDir);
		System.out.println("destDir="+destDir);
		
		if(cif.GetTime() % 2 == 0)
			rotate(destDir);
		else
			moveFwd(1.0);
		
		return false;
	}

	static void print_usage() {
		System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}


};

