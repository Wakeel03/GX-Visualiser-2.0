package application;

import java.awt.Point;

public class Edge {
	public Point point1;        
    public Point point2;       
    float grad;               
    
    float currentX;             
    
    public Edge(Point a, Point b)
    {
        point1 = new Point(a);
        point2 = new Point(b);
        grad = (float)((float)(point1.y - point2.y) / (float)(point1.x - point2.x));
    }
    
    public void active()
    {
        currentX = point1.x;
    }
    
    public void update()
    {
        currentX += (float)((float)1/(float)grad);
    }
    
    public void inactive()
    {
        currentX = point2.x;
    }
    
}