
public class SequenceController extends Controller {
	protected Controller[] controllers;
	protected int index = 0;
	
	public SequenceController() {
		this.controllers = new Controller[]{};
	}
	
	public double [] exec(State state) {
		if (index >= controllers.length) {
			setComplete(true);
			double [] act = new double[2];
			act[0] = act[1] = 0.0;
			return act;
		}
		double[] act = controllers[index].exec(state);
		
		if (controllers[index].isComplete())
			index++;
		
		return act;
	}

	public Controller[] getControllers() {
		return controllers;
	}

	public void setControllers(Controller[] controllers) {
		this.controllers = controllers;
		index = 0;
	}	
}
