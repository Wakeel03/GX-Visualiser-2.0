package application;

/*
 dummy values, test in main
int data[][] = {{15,30,400,80},
{10,40,400,200},
{10,90,100,400}};


//clipwindow = (xmin, ymax, xmax, ymin)
int output[][] = LineClipping.ClipThoseLines(0,400,200,0,data);

System.out.println(output[1][2]);

*/
public class LineClipping {

    public static final int INSIDE = 0;
    public static final int LEFT   = 1;
    public static final int RIGHT  = 2;
    public static final int BOTTOM = 4;
    public static final int TOP    = 8;


    public int xMin;
    public int xMax;
    public int yMin;
    public int yMax;
    public static double[][] points;

    private static LineClipper clipper;

    private class LineSegment {
        public double x0;
        public double y0;
        public double x1;
        public double y1;

        public LineSegment(double x0, double y0, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        @Override
        public String toString() {
            return "LineSegment(x0: " + x0 + ", y0: " + y0 + "; x1: " + x1 + ", y1: " + y1 + ")";
        }
    }

    public interface LineClipper {
        public LineSegment clip(LineSegment clip);
    }

    public class CohenSutherland implements LineClipper {

 
        private int computeOutCode(double x, double y) {
            int code = INSIDE;

            if (x < xMin) {
                code |= LEFT;
            } else if (x > xMax) {
                code |= RIGHT;
            }
            if (y < yMin) {
                code |= BOTTOM;
            } else if (y > yMax) {
                code |= TOP;
            }

            return code;
        }

    
        public LineSegment clip(LineSegment line) {
//            System.out.println("\nExecuting Cohen-Sutherland...");
            double x0 = line.x0, x1 = line.x1, y0 = line.y0, y1 = line.y1;
            int outCode0 = computeOutCode(x0, y0);
            int outCode1 = computeOutCode(x1, y1);
//            System.out.println("OutCode0: " + outCode0 + ", OutCode1: " + outCode1);
            boolean accept = false;

            while (true) {
                if ((outCode0 | outCode1) == 0) { // Bitwise OR is 0. Trivially accept
                    accept = true;
                    break;
                } else if ((outCode0 & outCode1) != 0) { // Bitwise AND is not 0. Trivially reject
                    break;
                } else {
                    double x, y;

                    // Pick at least one point outside rectangle
                    int outCodeOut = (outCode0 != 0) ? outCode0 : outCode1;

                    // Now find the intersection point;
                    // use formulas y = y0 + slope * (x - x0), x = x0 + (1 / slope) * (y - y0)
                    if ((outCodeOut & TOP) != 0) {
                        x = x0 + (x1 - x0) * (yMax - y0) / (y1 - y0);
                        y = yMax;
                    } else if ((outCodeOut & BOTTOM) != 0) {
                        x = x0 + (x1 - x0) * (yMin - y0) / (y1 - y0);
                        y = yMin;
                    } else if ((outCodeOut & RIGHT) != 0) {
                        y = y0 + (y1 - y0) * (xMax - x0) / (x1 - x0);
                        x = xMax;
                    } else {
                        y = y0 + (y1 - y0) * (xMin - x0) / (x1 - x0);
                        x = xMin;
                    }

                    // Now we move outside point to intersection point to clip
                    if (outCodeOut == outCode0) {
                        x0 = x;
                        y0 = y;
                        outCode0 = computeOutCode(x0, y0);
                    } else {
                        x1 = x;
                        y1 = y;
                        outCode1 = computeOutCode(x1, y1);
                    }
                }
            }
            if (accept) {
                return new LineSegment(x0, y0, x1, y1);
            }
            return null;
        }
        
    }

   
  //xmin, ymax, xmax, ymin
    public LineClipping(int xMin, int yMax, int xMax, int yMin, double[][]points) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        LineClipping.points = points;
        
        
        clipper = new CohenSutherland();
               
    }
    public static double[][] ClipThoseLines(int xMin, int yMax, int xMax, int yMin, double[][]points) {
    	LineClipping NewLine = new LineClipping( xMin, yMax, xMax, yMin, points);
        double[][]ClippedPoints = new double[points.length][points[0].length];

        

        double x0, y0, x1, y1;
        LineSegment line, clippedLine;
        for (int i = 0; i < points.length; i++) {
        	
        	
            x0 = points[i][0];
            x1 = points[i][2];
            y0 = points[i][1];
            y1 = points[i][3];
            line = NewLine.new LineSegment(x0, y0, x1, y1);
            clippedLine = clipper.clip(line);

//            System.out.println("Original: " + line);
//            System.out.println("Clipped: " + clippedLine);
            
            
            
            if(clippedLine == null) {
        		for(int z = 0;z<4;z++) {
        			ClippedPoints[i][z] = 0;
        		}
        	} else {
        			
            ClippedPoints[i][0] = clippedLine.x0;
            ClippedPoints[i][1] = clippedLine.x1;
            ClippedPoints[i][2] = clippedLine.y0;
            ClippedPoints[i][3] = clippedLine.y1;
        	}


            
        }
        
        return ClippedPoints;
    }

   
}
