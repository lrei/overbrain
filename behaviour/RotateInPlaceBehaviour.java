package behaviour;
import state.State;




public class RotateInPlaceBehaviour extends Behaviour {
	private double signal;

	public RotateInPlaceBehaviour(double signal) {
		this.signal = signal;
	}
	
	public double[] exec(State state) {
		System.out.println("Exec RotateInPlaceController");
		double [] act = new double[2];
		
		double left = state.getMotors()[0];
		double right = state.getMotors()[1];
		
		if(left != 0 || right != 0)
			return new StopBehaviour().exec(state);
		
		double rightIn = signal * 0.15/2;
		double leftIn = -signal * 0.15/2;
		act[0] = leftIn; act[1] = rightIn;
		setComplete(true);
		
		return act;
	}
	

}
