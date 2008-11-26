package behaviour;

public class MoveDirectionBehaviour extends SequenceBehaviour {
	
	public MoveDirectionBehaviour(double destDir) {
		setControllers(new Behaviour[] {
				new RotateBehaviour(destDir),
				new MoveFwdBehaviour()});
	}

}
