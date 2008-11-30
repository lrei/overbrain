package behaviour;
import state.State;


public class MoveFwdBehaviour extends Behaviour {

	@Override
	public double[] exec(State state) {
		System.out.println("Exec MoveFwd");
		double [] act = new double[2];
		
		Double[] motors = state.getMotors();
		
		double powIn = 0.15;
		
		double rot = Math.abs(motors[1] - motors[0]);
		
		if(rot > 0.20)
			powIn = 0.1;
		
		//System.out.println("rot="+rot+" powIn="+powIn);
		act[0] = act[1] = powIn;
		setComplete(true);
		return act;
	}

}
