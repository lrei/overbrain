package state;

public class ProbMap {
	double [][] map;
	int width;
	int height;
	double minRadius=0.6;

	public ProbMap(int width, int height) {
		this.width = width * 10;
		this.height = height * 10;
		map = new double[this.width][this.height];

		for(int ii = 0; ii < this.width; ii++)
			for(int jj = 0; jj < this.height; jj++)
				map[ii][jj] = 0.5;
	}
	
	public double [][] getMap() {
		return map;
	}
	
	public int getWidth() {
		return width/10;
	}
	public int getHeight() {
		return height/10;
	}

	public void set(double x, double y, double mult) {
		int ix = (int) Math.round(x*10);
		int iy = (int) Math.round(y*10);

		if(ix >= width || ix < 0 || iy >= height || iy <0)
			return;


		map[ix][iy] = map[ix][iy] * mult;

		if (map[ix][iy] > 1)
			map[ix][iy] = 0.99;
	}

	public double get(double x, double y) {
		int ix = (int) Math.round(x*10);
		int iy = (int) Math.round(y*10);

		if(ix >= width-1 || ix < 0 || iy >= height-1 || iy < 0)
			return 1.0;

		return map[ix][iy];
	}
	
	public double getRadius(double x, double y) {
		double res = 0.0;
		
		for(double cx = x-minRadius; cx < x+minRadius; cx+=0.1)
			for(double cy = y-minRadius; cy < y+minRadius; cy+=0.1)
				res = Math.max(res, get(cx, cy));
		
		return res;
	}
	
}
