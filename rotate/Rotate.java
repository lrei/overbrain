package rotate;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.joone.engine.*;
import org.joone.engine.learning.*;
import org.joone.io.*;
import org.joone.net.NeuralNet;


public class Rotate {
	Vector<RotateData> data;
	NeuralNet nnet;
	BasicNeuralDataSet trainingSet;

	public Rotate() {

	}

	public void readData(String filename) {
		data = new Vector<RotateData>();
		trainingSet = new BasicNeuralDataSet();
		// Open File
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			//Read File Line By Line
			boolean firstLine = true;
			int count = 0;
			while ((strLine = br.readLine()) != null)   {
				count++;
				if(firstLine) {	//skip firstline
					firstLine = false;
					//System.out.println("skipping firstline");
					continue;
				}
				RotateData d = new RotateData();
				//NeuralData example = new RotateInput();
				//NeuralData answer = new RotateOutput();

				String[] tok = strLine.split(",");
				if (tok.length != 5) {
					break;
				}
				double [] input = new double[3];
				double [] output = new double[2];
				input[0] = Double.parseDouble(tok[0]);
				input[1] = Double.parseDouble(tok[1]);
				input[2] = Double.parseDouble(tok[4]);
				output[0] = Double.parseDouble(tok[2]);
				output[1] = Double.parseDouble(tok[3]);

//				System.out.println(count+" : "+d.left+","+d.right+","
//						+d.leftIn+","+d.rightIn+","+d.angleDif);
//				example.setData(0, d.angleDif);
//				example.setData(1, d.left);
//				example.setData(2, d.right);
//
//				answer.setData(0, d.leftIn);
//				answer.setData(1, d.rightIn);
				
				BasicNeuralData example = new BasicNeuralData(input);
				BasicNeuralData answer = new BasicNeuralData(output);
				BasicNeuralDataPair pair = new BasicNeuralDataPair(example, answer);
				trainingSet.add(pair);

			}
			
			//Close the input stream
			in.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	public void train(int maxEpoch, double err) {
		network = new BasicNetwork();
		network.addLayer(new FeedforwardLayer(3));	// input
		network.addLayer(new FeedforwardLayer(10));
		//network.addLayer(new FeedforwardLayer(4));
		network.addLayer(new FeedforwardLayer(2));	// output
		network.reset();

		// train the neural network
		final Train train = new Backpropagation(network, trainingSet,0.7, 0.9);

		maxEpoch = 100;
		int epoch = 1;
		do {
			train.iteration();
		//	System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			
			epoch++;
		} while ((epoch < maxEpoch) && (train.getError() > err));
		

		for(NeuralDataPair pair: trainingSet ) {
			final NeuralData output = network.compute(pair.getInput());
			
			System.out.println("input: "+pair.getInput().getData(0) + ", "
					+ pair.getInput().getData(1) + ", " + pair.getInput().getData(2)
					+ "\nactual=" + output.getData(0) + ", " + output.getData(1)
					+ "\nideal=" + pair.getIdeal().getData(0) + ", "
					+ pair.getIdeal().getData(1)+"\n");
		}
	}


	public double [] compute(double angleDif, double left, double right) {
		NeuralData input = new RotateInput();;
		input.setData(0, angleDif);
		input.setData(1, left);
		input.setData(2, right);
		NeuralData res = network.compute(input);
		return res.getData();
	}

	public void save(String filename) throws IOException {
		File theFile;                           // simply a file name
		FileOutputStream outStream;             // generic stream to the file
		ObjectOutputStream objStream = null;   // stream for objects to the file

		theFile = new File(filename);

		// setup a stream to a physical file on the filesystem
		outStream = new FileOutputStream(theFile);

		// attach a stream capable of writing objects to the stream that is
		// connected to the file
		objStream = new ObjectOutputStream(outStream);

		objStream.writeObject(network);

		objStream.close();
	}

	public void load(String filename) throws IOException, ClassNotFoundException{
		File theFile;
		FileInputStream inStream;
		ObjectInputStream objStream;
		
		theFile = new File(filename);
		
		inStream = new FileInputStream(theFile);

		// attach a stream capable of writing objects to the stream that is
		// connected to the file
		objStream = new ObjectInputStream(inStream);

		network = (BasicNetwork) objStream.readObject();

		// close down the streams
		objStream.close();
		inStream.close();
	}
	
	public static void main(String args[]) {
		Rotate RotateController = new Rotate();
		RotateController.readData("train.txt");
		RotateController.train(50000, 0.01);
//		try {
//			RotateController.save("rotate.nn");
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
//		double[] a = RotateController.compute(4.0, 0.05, 0.05);
//		System.out.println(a[0]+"  "+a[1]);
//		try {
//			RotateController.load("rotate.nn");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			System.exit(0);
//		}
		double[] a = RotateController.compute(4.0, 0.05, 0.05);
		System.out.println(a[0]+"  "+a[1]);
	}

}
