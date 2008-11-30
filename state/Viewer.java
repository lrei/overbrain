
package state;

import javax.swing.*;
import java.awt.*;

public class Viewer extends JFrame
{
	private class Map extends JPanel
	{
		private static final int COLOR_SCALE_FACTOR = 2;
		int square_side;

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

			int green_strength = 0;
			for(int i = 0; i < pheroMap.length; i++)
				for(int j = 0; j < pheroMap[i].length; j++) {
					if(pheroMap[i][j] == null)
						System.err.println(i+" "+j);
					if(pheroMap[i][j].getWallProbability() != 0)
					{
						green_strength =(int) (pheroMap[i][j].getWallProbability()*100.0*COLOR_SCALE_FACTOR);
						if(green_strength > 255)
							green_strength = 255;
						//System.out.println(pheroMap[i][j].getWallProbability() + "*200 = " + green_strength);
						g.setColor(new Color(255-green_strength,255, 255-green_strength));
						g.fillRect(i*square_side, (pheroMap[i].length-j-1)*square_side, square_side, square_side);
					}
				}
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
	public void refresh(EstimatedCell [][] fm)
	{
		map.setPheroMap(fm);
		map.repaint();
	}

	public Viewer(int width, int height, EstimatedCell [][] nmap)
	{
		//set frame's title
		super("Simple Drawing Tool");
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
