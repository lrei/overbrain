package behaviour;

import state.State;

public class FollowBeaconBehaviour extends Behaviour {
	Behaviour b = null;
	
	public FollowBeaconBehaviour() {
		priority = 2;
	}

	@Override
	public double[] exec(State state) {
		System.out.println("Exec Follow Beacon " +state.getBeaconDir());
		
		double dir = state.getBeaconDir() + state.getDir();
		
		if(state.isFound())
			setComplete(true);
		
		if (b == null)
			b = new MoveDirectionBehaviour(dir);
		
		double[] act = b.exec(state);
		if(b.isComplete())
			setComplete(true);
	
		return act;
		
	}

}
