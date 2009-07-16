package behaviour;
import state.State;


public class RotateBehaviour extends Behaviour {
	private double destDir;

	public RotateBehaviour(double destDir) {
		this.destDir = destDir;
		name = "RotateBehavior";
	}
	
	public double[] exec(State state) {
		//System.out.println("Exec Rotate Behaviour" + destDir);
		double [] act = new double[2];
		
		
		double curAngle = state.getDir();
		double angleDiff = destDir - curAngle;
		
		//System.out.println("Exec RotateController " +destDir+"=>"+angleDiff);
		

		if (angleDiff > Math.toDegrees(Math.PI))
			angleDiff -= Math.toDegrees(Math.PI) * 2;
		
		if (angleDiff < -Math.toDegrees(Math.PI))
			angleDiff += Math.toDegrees(Math.PI) * 2;
		
		if(Math.abs(angleDiff) < 10) { // just go forward
			setComplete(true);
			act[0] = 0.15; act[1] = 0.15;
			return act;
		}
		
		
		//if(Math.abs(angleDiff) > 90)
		//	return new RotateInPlaceController(Math.signum(angleDiff)).exec(state);
		
		double rightIn = angleDiff * 0.15/180;
		double leftIn = -angleDiff * 0.15/180;
		act[0] = leftIn; act[1] = rightIn;
		System.out.println("rotate with "+ leftIn +" "+rightIn);
		
//		if(Math.abs(angleDiff) < 10)
//			setComplete(true);
		
			
		setComplete(true);
		return act;
	}
	

}
