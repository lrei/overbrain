
public abstract class Controller {

	protected boolean complete = false;
	
	public abstract double [] exec(State state);
	
	public boolean isComplete() {
		return complete;
	}
	
	protected void setComplete(boolean complete) {
		this.complete = complete;
	}
	

}
