package rotate;

import org.encog.neural.data.NeuralData;

public class RotateOutput implements NeuralData {
	double [] data;;
	
	public RotateOutput() {
		data = new double [2];
	}

	public double[] getData() {
		return data;
	}

	public double getData(int arg0) {
		return data[arg0];
	}

	public void setData(double[] arg0) {
		data = arg0; 
		
	}

	public void setData(int arg0, double arg1) {
		data[arg0] = arg1;
	}

	public int size() {
		return data.length;
	}
	public String toString() {
		return "{"+data[0]+","+data[1]+"}";
	}

}
