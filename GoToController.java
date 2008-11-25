
public class GoToController extends SequenceController {
	double destX;
	double destY;

	double tolerance = 0.5;

	public GoToController(State state, double destX, double destY) {
		this.destX = destX;
		this.destY = destY;

		double vx = destX - state.getPos().getX();
		double vy = destY - state.getPos().getY();
		double destDir = Math.toDegrees(Math.atan2(vy, vx));

		setControllers(new Controller[] {
				new RotateController(destDir),
				new MoveFwdController()});

	}

	public double [] exec(State state) {
		double [] act = new double[2];
		//System.out.println("Exec GoToController "+destX+", "+destY);
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
				setControllers(new Controller[] {
						new RotateController(destDir),
						new MoveFwdController()});
			}
		}
		return act;

	}
}
