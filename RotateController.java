
public class RotateController extends Controller {
	private double destDir;

	public RotateController(double destDir) {
		this.destDir = destDir;
	}
	
	public double[] exec(State state) {
		double [] act = new double[2];
		
		
		double curAngle = state.getDir();
		double angleDiff = destDir - curAngle;
		
		//System.out.println("Exec RotateController " +destDir+"=>"+angleDiff);
		

		if (angleDiff > Math.toDegrees(Math.PI))
			angleDiff -= Math.toDegrees(Math.PI) * 2;
		
		if (angleDiff < -Math.toDegrees(Math.PI))
			angleDiff += Math.toDegrees(Math.PI) * 2;
		
		//if(Math.abs(angleDiff) > 90)
		//	return new RotateInPlaceController(Math.signum(angleDiff)).exec(state);
		
		double rightIn = angleDiff * 0.15/180;
		double leftIn = -angleDiff * 0.15/180;
		act[0] = leftIn; act[1] = rightIn;
		
//		if(Math.abs(angleDiff) < 10)
//			setComplete(true);
		setComplete(true);
		return act;
	}
	

}
