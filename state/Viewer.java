/*
 * This is based on code from the NetSqueak Team
 */
package state;

import javax.swing.*;
import java.awt.*;

public class Viewer extends JFrame
{
	private class Map extends JPanel
	{
		private static final int COLOR_SCALE_FACTOR = 2;
		private static final int ROBOT_RADIUS = 50;
		int square_side;
		
		int rob_x = 0;
		int rob_y = 0;

		EstimatedCell [][] pheroMap; 

		public Map(int width, int height, int square_side)
		{
			this.square_side = square_side;
			//System.out.println("pheroMap size " + width + "w " + height + "h ");
			this.pheroMap = new EstimatedCell[width][height];
		}

		public void paint(Graphics g)
		{
			g.setColor(new Color(255,255,255));
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
			g.setColor(new Color(0, 0, 255));
			g.fillOval((int) (rob_x-0.5)*square_side, (int) (pheroMap[0].length-rob_y-1+0.5)*square_side,
					ROBOT_RADIUS, ROBOT_RADIUS);
			
			//g.setColor(new Color(0, 0, 0));
			//g.fillRect( 24*2*square_side, 12*2*square_side, square_side, square_side);
		}

		public void setPheroMap(EstimatedCell[][] phm)
		{
			this.pheroMap = phm;
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
