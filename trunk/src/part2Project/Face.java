package part2Project;

import java.util.Vector;

public class Face
{
	private Vector<Vertex> vertices;
	private int divLevel = 0;					// Some faces may be divided different
												// amounts to their neighbours
	
	private enum DivideBy {SIZE, CURVATURE, BOTH};
	private static DivideBy divReason = DivideBy.SIZE;
	private static double minDistance = 0.001;	// Update after testing
	private static double minCurvature = 0;		// Update after testing
	
	
	public Face()
	{
		vertices = new Vector<Vertex>();
	}
	
	public Face(Vector<Vertex> v)
	{
		vertices = v;
	}
	
	// Takes one of the Face's edges, and then splits the face into four sub-faces
	public void split(HalfEdge e, HalfEdgeScheme hes)
	{
		if(!vertices.contains(e.vertex()))
		{
			MainClass.fatalException(new Exception("Face split from invalid edge."));
		}
		else if(shouldDivide())
		{
			Face NW = this, SW = new Face(), SE = new Face(), NE = new Face();
			
			HalfEdge w1 = e, s1 = w1.next(), e1 = s1.next(), n1 = e1.next();
			
			// Assuming e is on the West side of the Face, pointing South
			HalfEdge w2 = w1.split(hes, NW, SW);
			HalfEdge s2 = s1.split(hes, SW, SE);
			HalfEdge e2 = e1.split(hes, SE, NE);
			HalfEdge n2 = n1.split(hes, NE, NW);
			
			Vertex centre = Vertex.weightedAverage(w1.vertex(), e1.vertex());
			hes.addVertex(centre);
			centre.valency = 4;
			
			// Create all half-edges leading to and from the centre
			HalfEdge CtoN = new HalfEdge(n1.vertex(), NW);
			HalfEdge NtoC = new HalfEdge(centre, NE);
			HalfEdge CtoW = new HalfEdge(w1.vertex(), SW);
			HalfEdge WtoC = new HalfEdge(centre, NW);
			HalfEdge CtoS = new HalfEdge(s1.vertex(), SE);
			HalfEdge StoC = new HalfEdge(centre, SW);
			HalfEdge CtoE = new HalfEdge(e1.vertex(), NE);
			HalfEdge EtoC = new HalfEdge(centre, SE);
			
			// Connect all the edges up again...
			HalfEdge.symPair(CtoN, NtoC);
			HalfEdge.symPair(CtoW, WtoC);
			HalfEdge.symPair(CtoS, StoC);
			HalfEdge.symPair(CtoE, EtoC);
			
			// Associate the new faces with the corresponding edges (and vertices)
			NW.recalculateVertices(w1, WtoC, CtoN, n2);
			SW.recalculateVertices(s1, StoC, CtoW, w2);
			SE.recalculateVertices(e1, EtoC, CtoS, s2);
			NE.recalculateVertices(n1, NtoC, CtoE, e2);
			
			// Add all the new components to the HalfEdgeScheme
			hes.addHalfEdge(CtoN);	hes.addHalfEdge(NtoC);
			hes.addHalfEdge(CtoW);	hes.addHalfEdge(WtoC);
			hes.addHalfEdge(CtoS);	hes.addHalfEdge(StoC);
			hes.addHalfEdge(CtoE);	hes.addHalfEdge(EtoC);
			hes.addFace(NE); hes.addFace(SW); hes.addFace(SE);
		}
	}
	
	private void recalculateVertices(HalfEdge h1, HalfEdge h2, HalfEdge h3, HalfEdge h4)
	{
		vertices.clear();
		vertices.add(h1.vertex());
		vertices.add(h2.vertex());
		vertices.add(h3.vertex());
		vertices.add(h4.vertex());
		
		h1.setNext(h2); h2.setNext(h3); h3.setNext(h4); h4.setNext(h1);
	}
	
	private boolean shouldDivide()
	{
		boolean divide = true;
		
		if(MainClass.adaptive)
		{
			// Could check to see how small the polygon is
			if((divReason == DivideBy.SIZE) || (divReason == DivideBy.BOTH))
			{
				// See how large the two diagonals are
				divide = (Vertex.distBetween(vertices.get(0), vertices.get(2)) > minDistance
					   || Vertex.distBetween(vertices.get(1), vertices.get(3)) > minDistance);
			}
			
			// Could check how flat the polygon is
			if((divReason == DivideBy.CURVATURE) || (divReason == DivideBy.BOTH))
			{
				divide = minCurvature > 0;
			}
		}
		
		return divide;
	}
	
	public String toString()
	{
		String s = String.valueOf(vertices.size());
		for(Vertex v : vertices) s += " " + v.getIndex();
		
		return s;
	}
	
	// Split a quadrilateral when outputting it, so that there are no gaps between
	// it and adjacent faces
	public String toString(HalfEdge e)
	{
		String s = "";
		HalfEdge he = e;
		
		while(!(he.vertex()).equals(vertices.firstElement()))
		{
			he = he.next();		// Rotate to line up with vertices in vector
		}
		
		// Indices of corner vertices
		int c1 = vertices.get(0).getIndex();
		int c2 = vertices.get(1).getIndex();
		int c3 = vertices.get(2).getIndex();
		int c4 = vertices.get(3).getIndex();
		
		// Indices of (potential) side vertices
		int s41 = 0, s12 = 0, s23 = 0, s34 = 0;
		
		int situation = 0;		// Used to decide how to split the quadrilateral into triangles
		
		// Find which sides have been split extra times, and extract relevant information
		if(divLevel < he.sym().face().divLevel)
		{
			situation += 1;
			s41 = he.sym().vertex().getIndex();			// c1------s12------c2
		}												//  |				|
		he = he.next();									//  |				|
		if(divLevel < he.sym().face().divLevel)			// s41			   s23
		{												//  |				|
			situation += 2;								//  |				|
			s12 = he.sym().vertex().getIndex();			// c4------s34------c3
		}
		he = he.next();
		if(divLevel < he.sym().face().divLevel)
		{
			situation += 4;
			s23 = he.sym().vertex().getIndex();
		}
		he = he.next();
		if(divLevel < he.sym().face().divLevel)
		{
			situation += 8;
			s34 = he.sym().vertex().getIndex();
		}
		
		switch(situation)
		{
			case(0):		// Extra points: none
			{
				s = "4 "+c1+" "+c2+" "+c3+" "+c4;
				break;
			}
			case(1):		// Extra points: s41
			{
				s = "3 "+s41+" "+c1+" "+c2+"\n" +
					"3 "+s41+" "+c2+" "+c3+"\n" +
					"3 "+s41+" "+c3+" "+c4;
				break;
			}
			case(2):		// Extra points: s12
			{
				s = "3 "+s12+" "+c4+" "+c1+"\n" +
					"3 "+s12+" "+c2+" "+c3+"\n" +
					"3 "+s12+" "+c3+" "+c4;
				break;
			}
			case(3):		// Extra points: s41 s12
			{
				s = "3 "+s41+" "+c1+" "+s12+"\n" +
					"3 "+s41+" "+s12+" "+c3+"\n" +
					"3 "+s12+" "+c2+" "+c3+"\n" +
					"3 "+s41+" "+c3+" "+c4;
				break;
			}
			case(4):		// Extra points: s23
			{
				s = "3 "+s23+" "+c1+" "+c2+"\n" +
					"3 "+s23+" "+c3+" "+c4+"\n" +
					"3 "+s23+" "+c4+" "+c1;
				break;
			}
			case(5):		// Extra points: s41 s23
			{
				s = "4 "+s41+" "+c1+" "+c2+" "+s23+"\n" +
					"4 "+s23+" "+c3+" "+c4+" "+s41;
				break;
			}
			case(6):		// Extra points: s12 s23
			{
				s = "3 "+s12+" "+c2+" "+s23+"\n" +
					"3 "+s12+" "+s23+" "+c4+"\n" +
					"3 "+s23+" "+c3+" "+c4+"\n" +
					"3 "+s12+" "+c4+" "+c1;
				break;
			}
			case(7):		// Extra points: s41 s12 s23
			{
				s = "3 "+s12+" "+c2+" "+s23+"\n" +
					"3 "+s12+" "+s23+" "+c3+"\n" +
					"3 "+s12+" "+c3+" "+c4+"\n" +
					"3 "+s12+" "+c4+" "+s41+"\n" +
					"3 "+s12+" "+s41+" "+c1;
				break;
			}
			case(8):		// Extra points: s34
			{
				s = "3 "+s34+" "+c1+" "+c2+"\n" +
					"3 "+s34+" "+c2+" "+c3+"\n" +
					"3 "+s34+" "+c4+" "+c1;
				break;
			}
			case(9):		// Extra points: s41 s34
			{
				s = "3 "+s34+" "+c4+" "+s41+"\n" +
					"3 "+s34+" "+s41+" "+c2+"\n" +
					"3 "+s41+" "+c1+" "+c2+"\n" +
					"3 "+s34+" "+c2+" "+c3;
				break;
			}
			case(10):		// Extra points: s12 s34
			{
				s = "4 "+s12+" "+c2+" "+c3+" "+s34+"\n" +
					"4 "+s34+" "+c4+" "+c1+" "+s12;
				break;
			}
			case(11):		// Extra points: s41 s12 s34
			{
				s = "3 "+s41+" "+c1+" "+s12+"\n" +
					"3 "+s41+" "+s12+" "+c2+"\n" +
					"3 "+s41+" "+c2+" "+c3+"\n" +
					"3 "+s41+" "+c3+" "+s34+"\n" +
					"3 "+s41+" "+s34+" "+c4;
				break;
			}
			case(12):		// Extra points: s23 s34
			{
				s = "3 "+s23+" "+c3+" "+s34+"\n" +
					"3 "+s23+" "+s34+" "+c1+"\n" +
					"3 "+s34+" "+c4+" "+c1+"\n" +
					"3 "+s23+" "+c1+" "+c2;
				break;
			}
			case(13):		// Extra points: s41 s23 s34
			{
				s = "3 "+s34+" "+c4+" "+s41+"\n" +
					"3 "+s34+" "+s41+" "+c1+"\n" +
					"3 "+s34+" "+c1+" "+c2+"\n" +
					"3 "+s34+" "+c2+" "+s23+"\n" +
					"3 "+s34+" "+s23+" "+c3;
				break;
			}
			case(14):		// Extra points: s12 s23 s34
			{
				s = "3 "+s23+" "+c3+" "+s34+"\n" +
					"3 "+s23+" "+s34+" "+c4+"\n" +
					"3 "+s23+" "+c4+" "+c1+"\n" +
					"3 "+s23+" "+c1+" "+s12+"\n" +
					"3 "+s23+" "+s12+" "+c2;
				break;
			}
			case(15):		// Extra points: s41 s12 s23 s34
			{
				s = "3 "+s12+" "+c2+" "+s23+"\n" +
					"3 "+s23+" "+c3+" "+s34+"\n" +
					"3 "+s34+" "+c4+" "+s41+"\n" +
					"3 "+s41+" "+c1+" "+s12+"\n" +
					"4 "+s12+" "+s23+" "+s34+" "+s41;
				break;
			}
		}
		
		return s;
	}
}
