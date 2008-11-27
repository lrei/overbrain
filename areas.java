import java.awt.*;
import java.awt.geom.*;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class areas extends JFrame {
	Arc2D.Double arcA;
	Arc2D.Double arcB;
	Arc2D.Double arcC;


	public areas() {
		//x=25.323793509792424 y=8.852405310551108 r=2.0 start=-40.0
		double xA = 25.323793509792424;
		double yA = 8.852405310551108;
		double rA = 2.0;
		double start = -40;
		arcA = new Arc2D.Double(xA, yA, rA, rA, start , 60.0,Arc2D.PIE);
	}

	public void paint (Graphics g) {
		Graphics2D g2D = (Graphics2D) g;

		g2D.draw (arcA);

		
	}
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new areas();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
}