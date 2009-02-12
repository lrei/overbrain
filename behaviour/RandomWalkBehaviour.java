package behaviour;

import java.util.Random;

import state.State;

public class RandomWalkBehaviour extends Behaviour {
	
	 public RandomWalkBehaviour() {
		 name = "RandomWalkBehavoiour";
		 priority = 5;
	 }

	@Override
	public double[] exec(State state) {
		//System.out.println("Exec Random Walk Behaviour");
		Random random = new Random();
		double diff = 0.0;
		double [] act = new double[2];


		if (random.nextInt(20) == 1) {
			diff = 0.5 * (random.nextDouble());
			if(random.nextBoolean() == true)
				diff = - diff;
		}
		

		act[0] = 0.1-diff; act[1] = 0.1+diff;	

		return act;
	}

}
