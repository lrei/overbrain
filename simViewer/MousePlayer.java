package simViewer;

import java.awt.Color;
import java.awt.geom.Point2D;


public class MousePlayer {
	
	private static Color[] colors = new Color[] {			
			Color.cyan.darker(),	
			Color.red.darker(),
			Color.green.darker(),
			Color.blue.darker(),
			Color.orange.darker(),				
			Color.magenta.darker(),
			Color.black
	};
	
	public String getUID() {
		return getPlayerName()+getId();
	}
	
	public static int num = 0;
	private int score, time, returningTime, arrivalTime, collisions, visitedMask, id = num++;
	private String playerName = "Player "+num;
	private Point2D position = new Point2D.Double(0,0);
	public double[] distanceSensors = new double[4];	
	private double directionDegs = 0;
	private boolean colision = false;
	private String state = "Stopped";
	private double count = 0;
	private boolean ghost = false;
	
	

	public MousePlayer(Point2D initialPos) {
		this.position = initialPos;
		this.directionDegs = 0;
	}
	

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}

	public double getDirection() {
		return directionDegs;
	}

	public void setDirection(double direction) {
		this.directionDegs = direction;
	}

	public Color getColor() {
			return colors[id%colors.length];		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getReturningTime() {
		return returningTime;
	}

	public void setReturningTime(int returningTime) {
		this.returningTime = returningTime;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getCollisions() {
		return collisions;
	}

	public void setCollisions(int collisions) {
		this.collisions = collisions;
	}

	public int getVisitedMask() {
		return visitedMask;
	}

	public void setVisitedMask(int visitedMask) {
		this.visitedMask = visitedMask;
	}

	public boolean isColision() {
		return colision;
	}

	public void setColision(boolean colision) {
		this.colision = colision;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isGhost() {
		return ghost;
	}

	public void setGhost(boolean ghost) {
		this.ghost = ghost;
	}
	
}
