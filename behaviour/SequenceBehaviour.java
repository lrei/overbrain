package behaviour;
import state.State;


public class SequenceBehaviour extends Behaviour {
	protected Behaviour[] controllers;
	protected int index = 0;
	
	public SequenceBehaviour() {
		this.controllers = new Behaviour[]{};
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

	public Behaviour[] getControllers() {
		return controllers;
	}

	public void setControllers(Behaviour[] controllers) {
		this.controllers = controllers;
		index = 0;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int ni) {
		index = ni;
	}
}
