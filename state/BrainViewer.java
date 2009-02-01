/*
 * This is based on code from the NetSqueak Team
 */
package state;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Vector;

public class BrainViewer extends JFrame
{
	private class Map extends JPanel
	{
		private static final int COLOR_SCALE_FACTOR = 2;
		private static final int ROBOT_RADIUS = 40;
		int square_side;
		
		int rob_x = 0;
		int rob_y = 0;

		EstimatedCell [][] probMap;
		public Vector<Point2D> points; 

		public Map(int width, int height, int square_side)
		{
			this.square_side = square_side;
			//System.out.println("pheroMap size " + width + "w " + height + "h ");
			this.probMap = new EstimatedCell[width][height];
			points = null;
		}

		public void paint(Graphics g)
		{
			// create the map
			g.setColor(Color.black);
			g.fillRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());
			
			// fill in map probability
			int color_strength = 0;
			for(int i = 0; i < probMap.length; i++)
				for(int j = 0; j < probMap[i].length; j++) {
					if(probMap[i][j] == null)
						System.err.println(i+" "+j);
					if(probMap[i][j].getWallProbability() != 0) {
						if(probMap[i][j].getSighting() < 1) {
							g.setColor(Color.black);
						}
						else if(probMap[i][j].getWallProbability() > 0.55) {
							g.setColor(Color.red);
						}
						else if(probMap[i][j].getWallProbability() < 0.55) {
							g.setColor(Color.green);
						}
						
						g.fillRect(i*square_side, (probMap[i].length-j-1)*square_side, square_side, square_side);
					}
				}
			
			// add cell border lines (1x1 cells)
			g.setColor(Color.yellow);
			for(int y = square_side*10; y < getSize().getHeight(); y+=square_side*10)
				g.drawLine(0, y, (int) getSize().getWidth()-1, y);
			for(int x = square_side*10; x < getSize().getWidth(); x+=square_side*10)
				g.drawLine(x, 0, x,  (int) getSize().getHeight());
			
			// add path if in use
			if(points != null) {
				for (Point2D pt : points) {
					g.setColor(Color.yellow);
					g.fillOval((int) (pt.getX()*10*square_side) - 12, (int) ((probMap[0].length - (pt.getY()*10))*square_side) - 12, 25, 25);
				}
			}
			
			// add robot
			g.setColor(Color.blue);
			g.fillOval((int) (rob_x)*square_side - 20, (int) ((probMap[0].length-10)-(rob_y))*square_side - 20,
					ROBOT_RADIUS, ROBOT_RADIUS);
			
			// add comm range around robot
			g.drawOval((int) (rob_x)*square_side -80*square_side, ((probMap[0].length-10)-(rob_y))*square_side -80*square_side, 160*square_side, 160*square_side);
		
			//g.setColor(new Color(0, 0, 0));
			//g.fillRect( 24*2*square_side, 12*2*square_side, square_side, square_side);
		}

		public void setPheroMap(EstimatedCell[][] phm)
		{
			this.probMap = phm;
		}

		public void setPoints(Vector<Point2D> pts) {
			this.points = pts;
		}

	}

	private static final int SQUARE_SIDE = 4;

	private Map map;

	public void refresh(EstimatedCell [][] fm, int x, int y)
	{
		map.rob_x = x;
		map.rob_y = y;
		map.setPheroMap(fm);
		map.repaint();
	}

	public BrainViewer(int width, int height, EstimatedCell [][] nmap)
	{
		//set frame's title
		super("Brain Viewer");
		//set frame size
		this.setSize(width*SQUARE_SIDE+30, height*SQUARE_SIDE+30);

		this.map = new Map(width, height, SQUARE_SIDE);
		map.setPheroMap(nmap);

		this.getContentPane().add(this.map);
		//make this frame visible
		this.setVisible(true);
	}
	
	public void addPoints(Vector<Point2D> pts) {
		map.setPoints(pts);
	}

	//	public static void main(String[] args)
	//	{
	//		System.out.println("ola!");
	//		Viewer pheromonesViewer = new Viewer(14*2, 28*2);
	//		System.out.println("yay!");
	//		try{
	//			Thread.sleep(1000);
	//		}catch(java.lang.InterruptedException e)
	//		{
	//			e.printStackTrace();
	//		}
	//		pheromonesViewer.dispose();
	//		System.out.println("finishing...");
	//	}


}
