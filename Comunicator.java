
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Vector;
import java.util.Arrays;

import state.State;


public class Comunicator {
	private int id;
	private int mid;


	public Comunicator(int id) {
		this.id = id;
		mid = 0;
	}


	public String talk(State state) {
		String msg = new String();


		/*
		 * Message ID
		 */
		mid++;
		//msg += String.valueOf(mid);
		//msg += "@";
		
		/*
		 * Mission
		 */
		if(state.isFound()) {
			msg += "F";
			/*
			 * Add path from start to finish
			 */
			msg += "@";
			String solution = new String();
			for(Point2D p: state.getSolution()) {
				solution += String.valueOf((int) p.getX());
				solution += ";";
				solution += String.valueOf((int) p.getY());
				solution += "&";
			}

			int freeSpace = 100 - msg.length();

			while(freeSpace < solution.length()) {
				String nSolution = new String();
				String[] sFields = solution.split("&");
				for(int ii = 0; ii < sFields.length; ii = ii + 2) {
					nSolution += sFields[ii];
					if(ii < sFields.length)
						nSolution += "&";
				}
				solution = nSolution;
			}
			msg += solution;
		}
		else {
			if (state.isBeaconVisible())
				msg += "B";
			else
				msg += "S";

			/*
			 * Position
			 */
			msg += "@";
			// X
			msg += String.valueOf(
					(int) (state.getPos().getX() * 10));
			// Y
			msg += "&";
			msg += String.valueOf(
					(int) (state.getPos().getY() * 10));
			// DIR
			msg += "&";
			msg += String.valueOf(
					(int) state.getDir().floatValue());

			/*
			 * IR Data
			 */
			msg += "@";
			// IR 0
			msg += String.valueOf(
					(int) (state.getIR(0) * 10));
			// IR 1
			msg += "&";
			msg += String.valueOf(
					(int) (state.getIR(1) * 10));
			// IR 2
			msg += "&";
			msg += String.valueOf(
					(int) (state.getIR(2) * 10));

			/*
			 * Mission Data
			 */
			if(!state.isFound()) {
				/* HOP MSGs */
				for(int ii = 0; ii < 5; ii++) {
					if(ii == state.getId())
						continue;
					
					if(state.getMsgFrom(ii) == null)
						continue;
					String hop = state.getMsgFrom(ii).split(":")[0];
					if((msg.length() + hop.length()) < 99) {
						msg += ":";
						msg += hop;
						//System.out.println("HOPPING: " + hop);
					}
				}
			}
		}


		return msg;
	}

}
