package behaviour;
import state.State;


public class GoToBehaviour extends SequenceBehaviour {
	double destX;
	double destY;

	double tolerance = 0.5;

	public GoToBehaviour(State state, double destX, double destY) {
		this.destX = destX;
		this.destY = destY;

		double vx = destX - state.getPos().getX();
		double vy = destY - state.getPos().getY();
		double destDir = Math.toDegrees(Math.atan2(vy, vx));

		setControllers(new Behaviour[] {
				new RotateBehaviour(destDir),
				new MoveFwdBehaviour()});

	}

	public double [] exec(State state) {
		double [] act = new double[2];
		System.out.println("Exec GoToBehaviour "+destX+", "+destY);
		act = controllers[index].exec(state);
		if(controllers[index].isComplete())
			index++;
		if(index == controllers.length) {
			//System.out.println("Are we there yet?");
			// are we there yet?
			double vx = destX - state.getPos().getX();
			double vy = destY - state.getPos().getY();
			double destDir = Math.toDegrees(Math.atan2(vy, vx));

			if((Math.abs(vx) < tolerance) && (Math.abs(vy) < tolerance)) { // YES!
				setComplete(true);
				//System.out.println("YES! Reached "+destX+", "+destY);
			}
			else {	// No :(
				//System.out.println("NO");
				setControllers(new Behaviour[] {
						new RotateBehaviour(destDir),
						new MoveFwdBehaviour()});
			}
		}
		return act;

	}
}
