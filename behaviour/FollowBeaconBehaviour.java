package behaviour;

import state.State;

public class FollowBeaconBehaviour extends Behaviour {

	@Override
	public double[] exec(State state) {
		System.out.println("Exec Follow Beacon");
		Behaviour b = new MoveDirectionBehaviour(state.getBeaconDir());
		setComplete(true);
		return b.exec(state);
		
	}

}
