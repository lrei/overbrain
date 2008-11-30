package behaviour;

import state.Direction;
import state.State;

public class WallFollowBehaviour extends Behaviour {
	
	public WallFollowBehaviour() {}

	@Override
	public double[] exec(State state) {
		System.out.println("Exec Wall Follow Behaviour: ir1="+state.getIR(1)
				+" ir2="+state.getIR(2)+" direction="+state.getWallDir());
		double act[] = new double[2];
		
		// TODO : check complete

		if(state.getIR(1) >= 1.0 && state.getIR(0) < 2.0 && state.getWallDir() == Direction.left) {
			// wall to the left
			act[0] = 0.0; act[1] = 0.15;
			System.out.println("Follow Wall @ Left - Slow");
		}
		else if(state.getIR(1) >= 1.0 && state.getIR(0) > 2.0 && state.getWallDir() == Direction.left) {
			// wall to the left
			act[0] = 0.0; act[1] = 0.15;
			System.out.println("Follow Wall @ Left - Fast");
		}
		else if(state.getIR(2) >= 1.0 && state.getIR(0) < 2.0 && state.getWallDir() == Direction.right) {
			// wall to the right
			act[0] = 0.15; act[1] = 0.0;
			System.out.println("Follow Wall @ Right - Slow");
		}
		else if(state.getIR(2) >= 1.0 && state.getIR(0) > 2.0 && state.getWallDir() == Direction.right) {
			// wall to the right
			act[0] = 0.15; act[1] = 0.0;
			System.out.println("Follow Wall @ Right - Fast");
		}
		else {
			state.setWallDir(Direction.none);
			setComplete(true);
			act[0] = 0.15; act[1] = 0.15;
		}

		return act;
	}
}
