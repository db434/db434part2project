package part2Project;

import java.util.Vector;

public class Face
{
	private Vector<Vertex> vertices;
	private int divLevel = 0;					// Some faces may be divided different
	public boolean fixed = false;				// amounts to their neighbours
	
	public boolean printed = false;		// Says if this face has been printed to the file
	public static int numFaces = 0;		// Faces may get split oddly when printing them
	
	private double[] normal = null;
	
	private enum DivideBy {SIZE, CURVATURE, BOTH};
	private static DivideBy divReason = DivideBy.CURVATURE;
	private static double minDistance = 0.308;		// Update after testing
	private static double maxCurvature = 0.17;		// Update after testing
	
	
	public Face()
	{
		vertices = new Vector<Vertex>();
	}
	
	public Face(Vector<Vertex> v)
	{
		vertices = v;
	}
	
	// Prepare the face for a new subdivision step
	public void reset()
	{
		normal = null;
	}
	
	public Vertex midpoint()
	{
		Vertex v = Vertex.weightedAverage(vertices.get(0), vertices.get(1),
									  	  vertices.get(2), vertices.get(3));
		v.setToFace();
		v.valency = 4;
		
		return v;
	}
	
	private double[] getNormal()
	{
		if(normal == null)
		{
			double[] v1 = Vertex.vectorBetween(vertices.get(0), vertices.get(1));
			double[] v2 = Vertex.vectorBetween(vertices.get(0), vertices.get(2));
			double[] v3 = Vertex.vectorBetween(vertices.get(0), vertices.get(3));			
			normal = new double[3];
			
			// Take the cross products v1 x v2 and v1 x v3, and add them together
			normal[0] = v2[2]*(v1[1] - v3[1]) + v2[1]*(v3[2] - v1[2]);
			normal[1] = v2[0]*(v1[2] - v3[2]) + v2[2]*(v3[0] - v1[0]);
			normal[2] = v2[1]*(v1[0] - v3[0]) + v2[0]*(v3[1] - v1[1]);
		}
		
		return normal;
	}
	
	private static double angleBetween(Face f1, Face f2)
	{
		double[] norm1 = f1.getNormal();
		double[] norm2 = f2.getNormal();
		
		double dotProduct = norm1[0]*norm2[0] + norm1[1]*norm2[1] + norm1[2]*norm2[2];
		double area = Math.sqrt(norm1[0]*norm1[0] + norm1[1]*norm1[1] + norm1[2]*norm1[2]) *
					  Math.sqrt(norm2[0]*norm2[0] + norm2[1]*norm2[1] + norm2[2]*norm2[2]);
		
		double angle = Math.acos(dotProduct/area);
		return Math.abs(angle);
	}
	
	// Returns whether this face has been subdivided more than Face f
	public boolean divMoreThan(Face f)
	{
		return divLevel > f.divLevel;
	}
	
	// Takes one of the Face's edges, and then splits the face into four sub-faces
	public void split(HalfEdge e, HalfEdgeScheme hes)
	{
		if(!vertices.contains(e.vertex()))
		{
			MainClass.fatalException(new Exception("Face split from invalid edge."));
		}
		else if(shouldDivide(e))
		{
			defSplit(e, hes);
		}
	}
	
	private void defSplit(HalfEdge e, HalfEdgeScheme hes)
	{
		if(MainClass.adaptive) checkNeighbours(e, hes);
		
		Face NW = this, SW = new Face(), SE = new Face(), NE = new Face();
		
		HalfEdge w1 = e, s1 = w1.next(), e1 = s1.next(), n1 = e1.next();
		
		// Assuming e is on the West side of the Face, pointing South
		HalfEdge w2 = w1.split(hes, SW, NW);
		HalfEdge s2 = s1.split(hes, SE, SW);
		HalfEdge e2 = e1.split(hes, NE, SE);
		HalfEdge n2 = n1.split(hes, NW, NE);
		
		// x1 now point to the midpoints of edges, and x2 point to corners
		
		Vertex centre = midpoint();
		hes.addVertex(centre);
		
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
		
		// Update the number of times the faces have been divided
		NW.divLevel = NE.divLevel = SW.divLevel = SE.divLevel = divLevel + 1;
		NW.fixed = NE.fixed = SW.fixed = SE.fixed = fixed;
		
		if(fixed)	// Allow the points to move one more time
		{
			// Unfix old vertices so they are then fixed the correct number of times
			w2.vertex().unfix();	s2.vertex().unfix();
			e2.vertex().unfix();	n2.vertex().unfix();
			
			NW.fixPoints();	NE.fixPoints();	SW.fixPoints(); SE.fixPoints();
			NW.smoothPointsOnce();	NE.smoothPointsOnce();
			SW.smoothPointsOnce();	SE.smoothPointsOnce();
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
	
	private boolean shouldDivide(HalfEdge e)
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
				HalfEdge he = e;
				double curvature = 0;
				
				for(int i=0; i<vertices.size(); i++)
				{
					double curv = Face.angleBetween(this, he.sym().face());
					if(curv > curvature) curvature = curv;
					he = he.next();
				}
				divide = divide && (curvature > maxCurvature);
			}
			
			if(!divide)
			{
				fixEdges(e);
				
				if(!fixed)
				{
					fixed = true;
					fixPoints();	//Don't want the vertices to move any more
				}
			}
		}
		
		return divide;
	}
	
	// Divide neighbouring faces if they have been divided less than this one, and
	// this one is about to be divided again. Makes navigation easier.
	// e is an edge of this face
	private void checkNeighbours(HalfEdge e, HalfEdgeScheme hes)
	{
		HalfEdge he = e;	// Loops around this face
		
		for(int i=0; i<4; i++)
		{
			Face neighbour = he.sym().face();
			if(neighbour.divLevel < divLevel) neighbour.defSplit(he.sym(), hes);
			
			if(!he.vertex().boundary)	// If there is a diagonal face...
			{
				Face diagonal = he.ahead().sym().face();
				if((diagonal.divLevel < divLevel)) diagonal.defSplit(he.ahead().sym(), hes);
			}
			
			he = he.next();
		}
	}
	
	private void fixPoints()
	{
		for(Vertex v : vertices) v.fix();
	}
	
	private void fixEdges(HalfEdge e)
	{
		e.hasBeenSplit = true;
		e.next().hasBeenSplit = true;
		e.next().next().hasBeenSplit = true;
		e.next().next().next().hasBeenSplit = true;
	}
	
	private void smoothPointsOnce()
	{
		for(Vertex v : vertices) v.tempSmooth();
	}
	
	public String toString()
	{
		numFaces++;
		
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
				s = noExtra(c1, c2, c3, c4);
				break;
			}
			case(1):		// Extra points: s41
			{
				s = oneExtra(c1,c2,c3,c4,s41);
				break;
			}
			case(2):		// Extra points: s12
			{
				s = oneExtra(c2,c3,c4,c1,s12);
				break;
			}
			case(3):		// Extra points: s41 s12
			{
				s = twoExtraAdj(c1,s12,c2,c3,c4,s41);
				break;
			}
			case(4):		// Extra points: s23
			{
				s = oneExtra(c3,c4,c1,c2,s23);
				break;
			}
			case(5):		// Extra points: s41 s23
			{
				s = twoExtraOpp(c1,c2,s23,c3,c4,s41);
				break;
			}
			case(6):		// Extra points: s12 s23
			{
				s = twoExtraAdj(c2,s23,c3,c4,c1,s12);
				break;
			}
			case(7):		// Extra points: s41 s12 s23
			{
				s = threeExtra(c1,s12,c2,s23,c3,c4,s41);
				break;
			}
			case(8):		// Extra points: s34
			{
				s = oneExtra(c4,c1,c2,c3,s34);
				break;
			}
			case(9):		// Extra points: s41 s34
			{
				s = twoExtraAdj(c4,s41,c1,c2,c3,s34);
				break;
			}
			case(10):		// Extra points: s12 s34
			{
				s = twoExtraOpp(c2,c3,s34,c4,c1,s12);
				break;
			}
			case(11):		// Extra points: s41 s12 s34
			{
				s = threeExtra(c4,s41,c1,s12,c2,c3,s34);
				break;
			}
			case(12):		// Extra points: s23 s34
			{
				s = twoExtraAdj(c3,s34,c4,c1,c2,s23);
				break;
			}
			case(13):		// Extra points: s41 s23 s34
			{
				s = threeExtra(c3,s34,c4,s41,c1,c2,s23);
				break;
			}
			case(14):		// Extra points: s12 s23 s34
			{
				s = threeExtra(c2,s23,c3,s34,c4,c1,s12);
				break;
			}
			case(15):		// Extra points: s41 s12 s23 s34
			{
				s = fourExtra(c1,s12,c2,s23,c3,s34,c4,s41);
				break;
			}
		}
		
		return s;
	}
	
	/* 
	 * Printing methods.
	 * Take indices of vertices, and print tessellated polygons.
	 * Indices must be given in clockwise order.
	 */
	
	private String noExtra(int v1, int v2, int v3, int v4)
	{
		numFaces += 1;
		return 	"4 "+v1+" "+v2+" "+v3+" "+v4;
	}
	
	private String oneExtra(int v1, int v2, int v3, int v4, int e1)
	{
		numFaces += 3;
		return 	"3 "+e1+" "+v1+" "+v2+"\n" +
				"3 "+e1+" "+v2+" "+v3+"\n" +
				"3 "+e1+" "+v3+" "+v4;
	}
	
	private String twoExtraAdj(int v1, int e1, int v2, int v3, int v4, int e2)
	{
		numFaces += 4;
		return 	"3 "+e2+" "+v1+" "+e1+"\n" +
				"3 "+e2+" "+e1+" "+v3+"\n" +
				"3 "+e1+" "+v2+" "+v3+"\n" +
				"3 "+e2+" "+v3+" "+v4;
	}
	
	private String twoExtraOpp(int v1, int v2, int e1, int v3, int v4, int e2)
	{
		numFaces += 2;
		return 	"4 "+e1+" "+e2+" "+v1+" "+v2+"\n" +
				"4 "+e1+" "+v3+" "+v4+" "+e2;
	}
	
	private String threeExtra(int v1, int e1, int v2, int e2, int v3, int v4, int e3)
	{
		numFaces += 5;
		return 	"3 "+e1+" "+v2+" "+e2+"\n" +
				"3 "+e1+" "+e2+" "+v3+"\n" +
				"3 "+e1+" "+v3+" "+v4+"\n" +
				"3 "+e1+" "+v4+" "+e3+"\n" +
				"3 "+e1+" "+e3+" "+v1;
	}
	
	private String fourExtra(int v1, int e1, int v2, int e2, int v3, int e3, int v4, int e4)
	{
		numFaces += 5;
		return 	"3 "+e1+" "+v2+" "+e2+"\n" +
				"3 "+e2+" "+v3+" "+e3+"\n" +
				"3 "+e3+" "+v4+" "+e4+"\n" +
				"3 "+e4+" "+v1+" "+e1+"\n" +
				"4 "+e1+" "+e2+" "+e3+" "+e4;
	}
	
}
