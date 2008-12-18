/*
 * This is based on code from the NetSqueak Team
 */
package state;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Vector;

public class Viewer extends JFrame
{
	private class Map extends JPanel
	{
		private static final int COLOR_SCALE_FACTOR = 2;
		private static final int ROBOT_RADIUS = 40;
		int square_side;
		
		int rob_x = 0;
		int rob_y = 0;

		EstimatedCell [][] pheroMap;
		public Vector<Point2D> points; 

		public Map(int width, int height, int square_side)
		{
			this.square_side = square_side;
			//System.out.println("pheroMap size " + width + "w " + height + "h ");
			this.pheroMap = new EstimatedCell[width][height];
			points = null;
		}

		public void paint(Graphics g)
		{
			g.setColor(Color.black);
			g.fillRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());
			
			
			int color_strength = 0;
			for(int i = 0; i < pheroMap.length; i++)
				for(int j = 0; j < pheroMap[i].length; j++) {
					if(pheroMap[i][j] == null)
						System.err.println(i+" "+j);
					if(pheroMap[i][j].getWallProbability() != 0) {
						color_strength =(int) (pheroMap[i][j].getWallProbability()*100.0*COLOR_SCALE_FACTOR);
						if(color_strength > 255)
							color_strength = 255;
						
						if(pheroMap[i][j].getWallProbability() > 0.55) {
							g.setColor(new Color(255,255-color_strength, 255-color_strength));
						}
						if(pheroMap[i][j].getWallProbability() < 0.55) {
							g.setColor(new Color(255-color_strength,255-color_strength, 255-color_strength));
						}
						if(pheroMap[i][j].getWallProbability() == 0.55) {
							g.setColor(new Color(255, 255, 255));
						}
						g.fillRect(i*square_side, (pheroMap[i].length-j-1)*square_side, square_side, square_side);
					}
				}
			
			
			g.setColor(Color.black);
			for(int y = square_side*10; y < getSize().getHeight(); y+=square_side*10)
				g.drawLine(0, y, (int) getSize().getWidth()-1, y);
			for(int x = square_side*10; x < getSize().getWidth(); x+=square_side*10)
				g.drawLine(x, 0, x,  (int) getSize().getHeight());
			
			if(points != null) {
				for (Point2D pt : points) {
					g.setColor(Color.yellow);
					g.fillOval((int) (pt.getX()*10*square_side) - 12, (int) ((pheroMap[0].length - (pt.getY()*10))*square_side) - 12, 25, 25);
				}
			}
			
			g.setColor(Color.blue);
			g.fillOval((int) (rob_x)*square_side - 20, (int) ((pheroMap[0].length-10)-(rob_y))*square_side - 20,
					ROBOT_RADIUS, ROBOT_RADIUS);
			
			//g.setColor(new Color(0, 0, 0));
			//g.fillRect( 24*2*square_side, 12*2*square_side, square_side, square_side);
		}

		public void setPheroMap(EstimatedCell[][] phm)
		{
			this.pheroMap = phm;
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

	public Viewer(int width, int height, EstimatedCell [][] nmap)
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
