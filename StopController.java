public class StopController extends Controller {
	public StopController() {
	}

	@Override
	public double[] exec(State state) {
		double [] act = new double[2];

		double left = state.getMotors()[0];
		double right = state.getMotors()[1];
		
		double leftIn = -left; double rightIn = -right;
		act[0] = leftIn; act[1] = rightIn;
		setComplete(true);
		return act;
	}

}
