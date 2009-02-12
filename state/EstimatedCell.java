package state;

import java.awt.geom.Rectangle2D;

/**
 * @author ZP
 */
public class EstimatedCell {

	private double x, y;
	private Rectangle2D shape;
	private double hValue = 0;
	private double fValue = 0;
	

	private int countSightings = 0;
	private double wallProbability = 0.55;
	private double targetProbability = 0.55;
	
	public EstimatedCell(double x, double y) {
		this.x = x;
		this.y = y;
		this.shape = new Rectangle2D.Double(x, y, 1, 1);
	}

	
	public int getSighting() {
		return this.countSightings;
	}
	

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Rectangle2D getShape() {
		return shape;
	}

	public void setShape(Rectangle2D shape) {
		this.shape = shape;
	}

	public double getWallProbability() {
		return wallProbability;
	}

	public void setWallProbability(double wallProbability) {
		if(wallProbability > 1.0)
			this.wallProbability = 1.0;
		else if (wallProbability < 0)
			this.wallProbability = 0;
			this.wallProbability = wallProbability;
		countSightings++;
	}
	
	public double getTargetProbability() {
		return targetProbability;
	}
	
	public void setTargetProbability(double targetProbability) {
		if(targetProbability > 1.0)
			this.targetProbability = 1.0;
		else if (targetProbability < 0)
			this.targetProbability = 0;
		else
			this.targetProbability = targetProbability;
	}
	
}
