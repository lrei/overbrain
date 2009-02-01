package state;

import Jama.*;

public class KalmanFilter {
	Matrix X, Q, H, K, P, R, U;
	
	// a is the angle in RADIANS
	public KalmanFilter(double x, double y, double phi) {
		double[][] zarray = {{x}, {y}, {phi}};
		double[][] qarray = {{0.00001, 0, 0}, {0, 0.00001, 0}, {0, 0, 0.00001}};
		double uval = 0.015*0.015;
		double rval = 0.5*0.5;
		double[][] rarray = {{rval, 0, 0}, {0, rval, 0}, {0, 0, rval}};
		double[][] uarray = {{uval, uval}, {uval, uval}};
		double[][] harray = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
		double pval = 0.0001;
		double[][] parray = {{pval, pval, pval}, {pval, pval, pval}, {pval, pval, pval}};
		
		Matrix Z = new Matrix(zarray);	// initial value
		Q = new Matrix(qarray);	// constant
		R = new Matrix(rarray);	// constant
		H = new Matrix(harray); // constant
		U = new Matrix(uarray); // constant
		P = new Matrix(parray); // initial value
		
		K = H.inverse();
		X = K.times(Z);
	}
	
	private void predict(double d, double a) {
		/*
		 *  calculate X (priori Xk-1)
		 */
		
		double curX = X.get(0, 0);
		double curY = X.get(1, 0);
		double curA = X.get(2, 0);
		
		double nextA = curA + a;
		
		if (nextA > Math.PI)
			nextA -= Math.PI * 2;
		
		if (nextA < -Math.PI)
			nextA += Math.PI * 2;
		
		
		double nextX = X.get(0, 0) + d * Math.cos(nextA);
		double nextY = X.get(1, 0) + d * Math.sin(nextA);
		
		double [][] xarray = {{nextX}, {nextY}, {nextA}};
		X = new Matrix(xarray);
		
		/* 
		 * calculate A
		 */
		
		double a1 = d*Math.sin(curA + a/2);
		double a2 = d*Math.cos(curA + a/2);
		double ax[][] = {{1, 0, -a1}, {0, 1, a2}, {0, 0, 1}};
		Matrix Ax = new Matrix(ax);
		double a3 = Math.cos(curA + a/2);
		double a4 = Math.sin(curA + a/2);
		double au[][] = {{a3, a1}, {a4, -a2}, {0, 1}};
		Matrix Au = new Matrix(au);
		
		/*
		 *  calculate P
		 */
		P = Ax.times(P.times(Ax.transpose()));
		P.plusEquals(Au.times(U.times(Au.transpose())));
		P.plusEquals(Q);
		
	}
	
	// Actually this does both predict and correct
	public double[][] correct(double x, double y, double phi, double d, double a) {
		predict(d, a);
		
		double[][] zarray = {{x}, {y}, {phi}};
		Matrix Z = new Matrix(zarray);
		
		/* 
		 * calculate K (Kalman Gain)
		 */
		Matrix PD = H.times(P.times(H.transpose()));
		Matrix D = PD.plus(R);
		K = P.times((H.transpose()).times(D.inverse()));
		
		/* 
		 * calculate X (posterior Xk-1)
		 */
		Matrix S = Z.minus(X);
		X.plusEquals(K.times(S));
		
		/* 
		 * calculate P
		 */
		Matrix M = Matrix.identity(3, 3).minus(K.times(H));
		P = M.times(P);
		
		return X.getArray();
	}
	
	public static void main(String args[]) {
		double[][] zarray = {{1}, {2}, {3}};
		Matrix N = new Matrix(zarray);
		System.out.println(N.get(1, 0));
	}

}
