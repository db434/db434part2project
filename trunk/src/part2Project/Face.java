package part2Project;

import java.util.Vector;

public class Face
{
	private Vector<Vertex> vertices;
	
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
		else
		{
			Face NW = this, SW = new Face(), SE = new Face(), NE = new Face();
			
			HalfEdge w1 = e, s1 = w1.next(), e1 = s1.next(), n1 = e1.next();
			
			// Assuming e is on the West side of the Face, pointing South
			HalfEdge w2 = w1.split(hes, NW, SW);
			HalfEdge s2 = s1.split(hes, SW, SE);
			HalfEdge e2 = e1.split(hes, SE, NE);
			HalfEdge n2 = n1.split(hes, NE, NW);
			
			Vertex centre = Vertex.average(w1.vertex(), e1.vertex());
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
	
	public String toString()
	{
		// Try to do this a better way
		return MainClass.faceToString(vertices);		
	}
}
