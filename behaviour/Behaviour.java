package behaviour;
import state.State;


public abstract class Behaviour {

	protected boolean complete = false;
	
	public abstract double [] exec(State state);
	
	public boolean isComplete() {
		return complete;
	}
	
	protected void setComplete(boolean complete) {
		this.complete = complete;
	}
	

}
