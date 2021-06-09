package application;

import java.awt.*;
import java.awt.geom.*;
import java.lang.Math;
import java.util.ArrayList; 

public class Transformations extends Frame {

	public void paint(Graphics g) {
		Graphics2D ga = (Graphics2D)g;
		ga.setPaint(Color.black);
		
		
		//for triangle
		int[][] points={ {300,50},{250,200},{375,225} };
		int size=points.length;
		
		ArrayList<ArrayList<Integer>> pointss = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> pointX = new ArrayList<Integer>();
		ArrayList<Integer> pointY = new ArrayList<Integer>();
		
		for(int i=0;i<points.length;i++) {
			pointX.add(Integer.valueOf(points[i][0]));
			pointY.add(Integer.valueOf(points[i][1]));
		}
		
		pointss.add(pointX);
		pointss.add(pointY);
		
		GeneralPath triangle = new GeneralPath();
		
		triangle.moveTo (points[0][0], points[0][1]); //must have a starting point.
		
		for (int index = 0; index < points.length; index++) {
			triangle.lineTo(points[index][0], points[index][1]);
		};
		triangle.closePath();
	
		ga.draw(triangle);
		
	}
	
	public static ArrayList<Double> Translate(ArrayList<Double> points, float x, float y){
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		for (int i = 0; i < points.size(); i++) {
			if (i % 2 == 0)
				finalPoints.add(points.get(i) + x);
			else
				finalPoints.add(points.get(i) + y);
		}
		return finalPoints;
	}
	
	public static ArrayList<Double> RotateStandard(ArrayList<Double> points, float angleDegrees){
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		double angleRadian = Math.toRadians(Double.valueOf(angleDegrees));
		
		/* matrix:
		 * cos(a) -sin(a) 0
		 * sin(a) cos(a)  0
		 *   0      0     1
		 */
		for (int i=0;i<points.size();i++) {
			
			if(i%2==0)
				finalPoints.add(points.get(i)*Math.cos(angleRadian)-points.get(i+1)*Math.sin(angleRadian));		
			else
				finalPoints.add(points.get(i-1)*Math.sin(angleRadian)+points.get(i)*Math.cos(angleRadian));

		}

		return finalPoints;
	}
	
	public static ArrayList<Double> RotateFixedPoint(ArrayList<Double> points, float angleDegrees, 
			int centerX, int centerY){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();

		double angleRadian = Math.toRadians(Double.valueOf(angleDegrees));
		/* matrix:
		 * cos(a) -sin(a) -Xr.cos(a)+Yr.sin(a)+Xr
		 * sin(a) cos(a)  -Xr.sin(a)-Yr.sin(a)+Yr
		 *   0      0     			1	
		 */
		for (int i=0;i<points.size();i++) {
			if(i%2==0)//considering x
				finalPoints.add(points.get(i)*Math.cos(angleRadian)-points.get(i+1)*Math.sin(angleRadian)
						-centerX*Math.cos(angleRadian)+centerY*Math.sin(angleRadian)+centerX);
			else//considering y
				finalPoints.add(points.get(i-1)*Math.sin(angleRadian)+points.get(i)*Math.cos(angleRadian)
						-centerX*Math.sin(angleRadian)-centerY*Math.cos(angleRadian)+centerY);

		}
		return finalPoints;
	}
	
	public static ArrayList<Double> scaleStandard(ArrayList<Double> points, double sfX, double sfY){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();	
		/* matrix:
		 * sfX  0  0
		 *  0  sfY 0
		 *  0   0  1     			1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(points.get(i)*Double.valueOf(sfX));
			else
				finalPoints.add(points.get(i)*Double.valueOf(sfY));
	
		}
		return finalPoints;
	}

	public static ArrayList<Double> scaleFixedPoint(ArrayList<Double> points, double sfX, double sfY, double centerX, double centerY){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * sfX      0 		Xf(1-Sx)
		 *   0     sfY  	Yf(1-Sy)
		 *   0      0     		1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(points.get(i)*Double.valueOf(sfX)+centerX*(1 - sfX));
			else
				finalPoints.add(points.get(i)*Double.valueOf(sfY)+centerY*(1 - sfY));
	
		}
		return finalPoints;
	}
	
	public static ArrayList<Double> shearX(ArrayList<Double> points, double shX, double totalIncrements){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	 1     shx 			0
		 *   0      1  	    	0
		 *   0      0     		1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(points.get(i)+((1.0 / totalIncrements) * points.get(i+1)*Double.valueOf(shX)));
			else
				finalPoints.add(points.get(i));
	
		}
		return finalPoints;
	}
	
	public static ArrayList<Double> shearY(ArrayList<Double> points, double shY, double totalIncrements){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	 1     	0 			0
		 *  shY     1  	    	0
		 *   0      0     		1	
		 */
//		for (int i=0;i<points.size();i++) {	
//			if(i%2==0)
//				finalPoints.add(((1.0 / totalIncrements) * points.get(i)*Double.valueOf(shY))+points.get(i+1));
//			else
//				finalPoints.add(points.get(i));
//	
//		}
		
		for (int i=0;i<points.size();i++) {	
			if(i%2==1)
				finalPoints.add(points.get(i)+((1.0 / totalIncrements) * points.get(i-1)*Double.valueOf(shY)));
			else
				finalPoints.add(points.get(i));
	
		}
		
		return finalPoints;
	}
	public static ArrayList<Double> reflectX(ArrayList<Double> points){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	 1      0 		0
		 *   0     -1  	    0
		 *   0      0     	1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(points.get(i));
			else
				finalPoints.add(-(points.get(i)));
	
		}
		
		return finalPoints;
	}
	public static ArrayList<Double> reflectY(ArrayList<Double> points){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	-1      0 		0
		 *   0      1  	    0
		 *   0      0     	1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(-(points.get(i)));
			else
				finalPoints.add(points.get(i));
	
		}
		
		return finalPoints;
	}
	
	public static ArrayList<Double> reflectXY(ArrayList<Double> points){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	-1      0 		0
		 *   0     -1  	    0
		 *   0      0     	1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(-(points.get(i)));
			else
				finalPoints.add(-(points.get(i)));
	
		}
		
		return finalPoints;
	}
	
	public static ArrayList<Double> reflectYX(ArrayList<Double> points){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	 0      1 		0
		 *   1      0  	    0
		 *   0      0     	1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(points.get(i+1));
			else
				finalPoints.add(points.get(i-1));
	
		}
		
		return finalPoints;
	}
	
	public static ArrayList<Double> reflectYNegX(ArrayList<Double> points){
		
		ArrayList<Double> finalPoints = new ArrayList<Double>();
		
		/* matrix:
		 * 	 0     -1 		0
		 *  -1      0  	    0
		 *   0      0     	1	
		 */
		for (int i=0;i<points.size();i++) {	
			if(i%2==0)
				finalPoints.add(-(points.get(i+1)));
			else
				finalPoints.add(-(points.get(i-1)));
	
		}
		
		return finalPoints;
	}
	
	
}