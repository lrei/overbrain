package neural;

import java.io.*;


public class NeuralNet {
	BackPropNN bp;
	int inputNodes;
	int outputNodes;
	int midNodes;	// number of nodes (neurons) in the middle (hidden) layer
	long maxIter;	// maximum number of iterations during training
	double MSE;

	// Neural Network Configuration Options
	double alpha;
	double beta;
	double thresh;
	int numLayers;
	int layerSize[];

	public NeuralNet(int inputNodes, int outputNodes, int midNodes, int iter, double b, double a, double t, String trainFile) {

		this.inputNodes = inputNodes;
		this.outputNodes = outputNodes;
		this.midNodes = midNodes;
		maxIter = iter;

		beta = b;
		alpha = a;
		thresh = t;
		numLayers = 3;	// currently always 3

		/* Load Training Data */
		//cout << "Loading training data...";

		/* the number of possible results is determined
		 * by asking the DB how many signatures are
		 * known */

		// 32xmidNoes + midNodes*numRes = numRes * nTrainEx
		layerSize = new int [numLayers];
		layerSize[0] = inputNodes;
		layerSize[1] = midNodes;
		layerSize[2] = outputNodes;

		// Create the neural net
		bp = new BackPropNN(numLayers, layerSize, beta, alpha);

		//train(trainFile);

	}

	public void train(String filename, int num, int [] input, int [] output) throws Exception {
		double examples[][] = new double[num][input.length];
		double answers[][] = new double[num][output.length];

		// load the data
		// Open the file that is the first 
		// command line parameter
		FileInputStream fstream = new FileInputStream(filename);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		int count = 0;
		while ((strLine = br.readLine()) != null)   {
			String[] tok = strLine.split(";");
			if ((tok.length != 5) || (count == num)) {
				break;
			}
			double [] example = new double[input.length];
			double [] answer = new double[output.length];

			for (int ii = 0; ii < example.length; ii++) 
				examples[count][ii] = Double.parseDouble(tok[input[ii]]);
			for (int ii = 0; ii < answer.length; ii++) 
				answers[count][ii] = Double.parseDouble(tok[output[ii]]);

		}
		/* Train the net */
		for (int ii = 0; ii < maxIter ; ii++) {
			System.out.println("Training n="+ii);
			for(int jj = 0; jj < num; jj++)
				bp.bpgt(examples[jj], answers[jj]);

			//	    	  if ((MSE = bp.mse(answers[ii])) < thresh) {	// Training Complete
			//	    		  //cout << "MSE = " << MSE << endl;
			//	    		  break;	// Stop
			//	    	  }

		}
	}
	
	public double [] compute(double [] example) {
		double[] res = new double[outputNodes];
		bp.ffwd(example);
		
		for (int ii = 0; ii < res.length; ii++)
			res[ii] = bp.Out(ii);
		
		return res;
	}

	public static void main(String[] args) {
		NeuralNet n = new NeuralNet(3, 2, 10, 5000, 0.5, 0.7, 0.01, "train.txt");
		int [] in = {0,1,4};
		int [] out =  {2,3};
		try {
			n.train("train.txt", 9990, in, out);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		// -0.06;0.06;-0.1;-0.06;4.0
		double [] example = {-0.06, 0.06, 4.0};
		System.out.println(" "+ n.compute(example)[0]);
	}

}
