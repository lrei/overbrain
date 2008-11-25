import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ReadXmlMap {
	Quadtree qt;
	double resolution;
	Point2D.Double target;
	double radius;
	Vector<GeneralPath> polygons;
	Vector<Point2D> startPositions;
	int counter;

	public ReadXmlMap(String mapFile, String gridFile) {
		this(mapFile, gridFile, 0.5);
	}

	public ReadXmlMap(String mapFile, String gridFile, double res) {
		qt = new Quadtree();
		resolution = res;
		target = new Point2D.Double();
		counter = 0;
		readMap(mapFile);
		readGrid(gridFile);
	}
	
	public Quadtree getQuadtree() {
		return this.qt;
	}
	
	public Point2D getStart(int index) {
		
		if (index < 0 || index >= startPositions.size())
			return null;
		
		return startPositions.get(index);
	}
	
	public Point2D getTarget() {
		return target;
	}

	public void readMap(String fileName) {

		try {
			File file = new File(fileName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();


			Float w = Float.valueOf(doc.getDocumentElement().getAttribute("Width"));
			Float h = Float.valueOf(doc.getDocumentElement().getAttribute("Height"));
			
			qt.rect.setRect(0, 0, w, h);
			//System.out.println("w="+w+" h="+h+ " --- x="+qt.rect.getCenterX()+" y="+qt.rect.getCenterY());;
			
			polygons = new Vector<GeneralPath>();

			Element t = (Element) doc.getElementsByTagName("Target").item(0);
			double px = Float.valueOf(t.getAttribute("X")).doubleValue();
			double py = Float.valueOf(t.getAttribute("Y")).doubleValue();
			target.setLocation(px, py);
			radius = Float.valueOf(t.getAttribute("Radius")).doubleValue();

			NodeList wallList = doc.getElementsByTagName("Wall");

			for (int ii = 0; ii < wallList.getLength(); ii++) {
				Element wall = (Element) wallList.item(ii);
				Vector<Float> xPoints = new Vector<Float>();
				Vector<Float> yPoints = new Vector<Float>();

				GeneralPath polygon = 
					new GeneralPath(xPoints.size());


				//Float wh = Float.valueOf(wall.getAttribute("Height"));
				//System.out.println(wh);
				NodeList cornerList = wall.getElementsByTagName("Corner");

				for(int jj = 0; jj < cornerList.getLength(); jj++) {
					Element corner = (Element) cornerList.item(jj);

					xPoints.add(Float.valueOf(corner.getAttribute("X")));
					yPoints.add(Float.valueOf(corner.getAttribute("Y")));

				}

				polygon.moveTo(xPoints.get(0), yPoints.get(0));

				for (int index = 1; index < xPoints.size(); index++) {
					polygon.lineTo(xPoints.get(index), yPoints.get(index));
				};

				polygon.closePath();
				polygons.add(polygon);
			}

			// now it's time to build the quadtree
			buildQtree(qt);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readGrid(String fileName) {

		try {
			File file = new File(fileName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			
			startPositions = new Vector<Point2D>();
			
			NodeList items = doc.getElementsByTagName("Position");
			for (int ii = 0; ii < items.getLength(); ii++) {
				Point2D.Double p = new Point2D.Double();
				Element ep = (Element) items.item(ii);
				double x = Float.valueOf(ep.getAttribute("X"));
				double y = Float.valueOf(ep.getAttribute("Y"));
				p.setLocation(x, y);
				startPositions.add(p);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void buildQtree(Quadtree root) {
		double w = root.rect.getWidth()/2;
		double h = root.rect.getHeight()/2;
		
		
		boolean intersect = false;
		for (int ii = 0; ii < polygons.size(); ii++) {
			if(polygons.get(ii).intersects(root.rect)){
				intersect = true;
				break;
			}
		}
		if (intersect) {
			root.occupied = 1;
			counter++;
			
			if (w <= resolution || h <= resolution) {
				return;
			}
			root.nw = new Quadtree();
			root.ne = new Quadtree();
			root.sw = new Quadtree();
			root.se = new Quadtree();
			//root.parent = root;
			
			
			double x = root.rect.getX();
			double y = root.rect.getY();
			
			//System.out.println("x="+x +" y="+y);
			
			root.nw.rect.setRect(x, y, w, h);
			root.sw.rect.setRect(x, y+h, w, h);
			root.ne.rect.setRect(x+w, y, w, h);
			root.se.rect.setRect(x+w, y+h, w, h);
			
			buildQtree(root.nw);
			buildQtree(root.ne);
			buildQtree(root.sw);
			buildQtree(root.se);
		}
		else {
			root.occupied = 0;
		}
	}

//	public static void main(String [] args) {
//		ReadXmlMap a = new ReadXmlMap(args[0], args[1]);
//
//		Planner p = new Planner(a.getQuadtree(), a.getStart(0), a.getTarget() , 0.5);
//		Vector<Quadtree> res = p.aStar();
//		
////		for (int ii = 0; ii < res.size(); ii++)
////			System.out.println(res.get(ii).toString());
//	}
}