package application;

//return arraylist [[scanline1, x1, x2],[scanline2, x1, x2, x3,x4],[scanline3, x1, ......, xn],..........] : n => even number
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Vector;

public class Scanline {
	 private static Vector<Point> pointsPolygon;
	 
	    public Scanline(){
	        pointsPolygon = new Vector<Point>();
	    }
	    
	    public static Point[] getArray(ArrayList<Integer> givenList, int numSides){
	        Point [] P = new Point[numSides/2 + 1];
	        int j = 0;
	        for(int i=0; i<(numSides/2); i++){
	            P[i] = new Point((int)givenList.get(j), (int)givenList.get(j+1));
	            j+=2;
	        }
	        P[numSides/2] = new Point((int)givenList.get(0), (int)givenList.get(1));
	        
	        return P;
	    }
	    
	    public static Edge[] findEdges()    {
	        Edge[] sortedEdges = new Edge[pointsPolygon.size()-1];
	        
	        for (int i = 0; i < pointsPolygon.size() - 1; i++)
	        {
	            if (pointsPolygon.elementAt(i).y < pointsPolygon.elementAt(i+1).y)
	                sortedEdges[i] = new Edge(pointsPolygon.elementAt(i), pointsPolygon.elementAt(i+1));
	            else
	                sortedEdges[i] = new Edge(pointsPolygon.elementAt(i+1), pointsPolygon.elementAt(i));
	        }
	        return sortedEdges;
	    }
	    
	    //SCAN LINE FILL
	    public static ArrayList<ArrayList<Integer>> getScanLineValue( ArrayList<Integer> givenList){
	    	pointsPolygon = new Vector<Point>();
	   	  ArrayList<ArrayList<Integer>> returnArraylist = new ArrayList<ArrayList<Integer>>();  
//	   	  ArrayList<Integer> oneScanlineList = new ArrayList<Integer>(); 
	   	  
	    	int numSides = givenList.size();
	       Point P[] =  getArray(givenList, numSides);
	       
	       for(int i=0; i<P.length; i++){
	           pointsPolygon.add(P[i]);
	       }   
	       
	        Edge[] edgesInOrder = findEdges();
	        Edge temp;
	        
	        for (int i = 0; i < edgesInOrder.length - 1; i++)
	            for (int j = 0; j < edgesInOrder.length - 1; j++)
	            {
	                if (edgesInOrder[j].point1.y > edgesInOrder[j+1].point1.y) 
	                {
	                    temp = edgesInOrder[j];
	                    edgesInOrder[j] = edgesInOrder[j+1];
	                    edgesInOrder[j+1] = temp;
	                }  
	            }
	        
	        int lineEnd = 0;
	        for (int i = 0; i < edgesInOrder.length; i++)
	        {
	            if (lineEnd < edgesInOrder[i].point2.y)
	                lineEnd = edgesInOrder[i].point2.y;
	        }
	          
	        int line = edgesInOrder[0].point1.y;
	        
	        ArrayList<Integer> list = new ArrayList<Integer>();
	        
	        for (line = edgesInOrder[0].point1.y; line <= lineEnd; line++)
	        {
	            list.clear();
//	            oneScanlineList.clear();
	            ArrayList<Integer>  oneScanlineList = new ArrayList<Integer>();
	            
	            for (int i = 0; i < edgesInOrder.length; i++)
	            {   
	                if (line == edgesInOrder[i].point1.y) 
	                {
	                    if (line == edgesInOrder[i].point2.y)
	                    {
	                        edgesInOrder[i].inactive();
	                        list.add((int)edgesInOrder[i].currentX);
	                    }
	                    else
	                    {
	                        edgesInOrder[i].active();
	                    }
	                }
	                
	                if (line == edgesInOrder[i].point2.y)
	                {
	                    edgesInOrder[i].inactive();
	                    list.add((int)edgesInOrder[i].currentX);
	                }
	                
	                if (line > edgesInOrder[i].point1.y && line < edgesInOrder[i].point2.y)
	                {
	                    edgesInOrder[i].update();
	                    list.add((int)edgesInOrder[i].currentX);
	                }
	                
	            }

	            int swapTemp;
	            for (int i = 0; i < list.size(); i++)
	                for (int j = 0; j < list.size() - 1; j++)
	                {
	                    if (list.get(j) > list.get(j+1))
	                    {
	                        swapTemp = list.get(j);
	                        list.set(j, list.get(j+1));
	                        list.set(j+1, swapTemp);
	                    }
	                
	                }
	            
	           oneScanlineList.add(line);

		        ArrayList<Integer> finalScanlineY = new ArrayList<Integer>();
	           for (int i = 0; i < list.size(); i++){
	        	   oneScanlineList.add(list.get(i));
//	        	   finalScanlineY.add(list.get(i));
	            }
	           
	           returnArraylist.add(oneScanlineList);   
//	           System.out.println("in loop: "+returnArraylist.get(line-150));
	        }
	        
	        pointsPolygon.clear();

//	        System.out.println("After loop: "+returnArraylist);
			return returnArraylist;
	    }
	    
	    

}