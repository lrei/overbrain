


public class RotateInPlaceController extends Controller {
	private double signal;

	public RotateInPlaceController(double signal) {
		this.signal = signal;
	}
	
	public double[] exec(State state) {
		System.out.println("Exec RotateInPlaceController");
		double [] act = new double[2];
		
		double left = state.getMotors()[0];
		double right = state.getMotors()[1];
		
		if(left != 0 || right != 0)
			return new StopController().exec(state);
		
		double rightIn = signal * 0.15/2;
		double leftIn = -signal * 0.15/2;
		act[0] = leftIn; act[1] = rightIn;
		setComplete(true);
		
		return act;
	}
	

}
