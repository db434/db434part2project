package part2Project;

public class HalfEdge
{
	private Vertex vertex;
	private HalfEdge sym;
	private HalfEdge next;
	private Face face;
	protected boolean hasBeenSplit = false;
	
	public HalfEdge(Vertex v, Face f)
	{
		vertex = v;
		sym = null;
		next = null;	//Associated Face sets this
		face = f;
	}
	
	public Vertex vertex() 	{return vertex;}
	public HalfEdge sym()  	{return sym;}
	public HalfEdge next() 	{return next;}
	public Face face()	   	{return face;}
	
	// The next HalfEdge in a straight line (not valid around extraordinary points)
	public HalfEdge ahead()
	{
		return rotate().next;
	}
	
	// Returns the next HalfEdge which points to the same Vertex
	public HalfEdge rotate()
	{
		HalfEdge e;
		
		if(vertex.equals(next.sym.vertex)) e = next.sym;	// Normal case
		else if(vertex.equals(next.sym.sym.vertex)) e = next.sym.sym;	// If the face hasn't been divided
		else e = next.sym.next.sym.next;		// If there has been some extra subdivision
		
		boolean test = vertex.equals(e.vertex);
		if(!test) MainClass.fatalException(new Exception("Bad rotation"));
		
		return e;
	}
	
	public void setSym(HalfEdge h)  {sym = h;}
	public void setNext(HalfEdge h) {next = h;}
	
	// Make both HalfEdges each other's symmetric partner.
	public static void symPair(HalfEdge h1, HalfEdge h2)
	{
		h1.setSym(h2);
		h2.setSym(h1);
	}
	
	public Vertex midpoint()
	{
		Vertex v;
		
		// Need to take care of vertex count now - may or may not create new vertex
		/*if(sym.face.divMoreThan(face)) v = sym.vertex;
		else*/ v = Vertex.weightedAverage(vertex, sym.vertex);
		
		return v;
	}
	
	// Prepare the edge for a new subdivision step
	public void reset()
	{
		hasBeenSplit = false;
	}
	
	// Split this edge in two, returning the new half (this.ahead())
	// f1 is for the new half, and f2 is for the old half
	public HalfEdge split(HalfEdgeScheme hes, Face f1, Face f2)
	{
		HalfEdge e = new HalfEdge(vertex, f1);
		
		if(sym.face.divMoreThan(this.face))
		{
			e.sym = sym;
			sym.sym = e;
			
			vertex = sym.vertex;		// Update to the midpoint
			vertex.boundary = false;	// The mesh is now complete around vertex
			
			sym = sym.ahead();
			face = f2;
		}
		else
		{
			e.sym = sym;
			
			Vertex midpoint = midpoint();
			hes.addVertex(midpoint);
			midpoint.valency = 4;		// Is this a safe assumption?
			midpoint.setToEdge();
			midpoint.boundary = true;	// The mesh around the vertex is incomplete
			
			vertex = midpoint;
			face = f2;
		}
		
		e.hasBeenSplit = true;
		hasBeenSplit = true;
		
		hes.addHalfEdge(e);
		return e;
	}
	
	public static double angleBetween(HalfEdge e1, HalfEdge e2)
	{
		Vertex v1s = e1.sym.vertex, v1e = e1.vertex;
		Vertex v2s = e2.sym.vertex, v2e = e1.vertex;
		
		double dotProduct = (v1e.getX() - v1s.getX()) * (v2e.getX() - v2s.getX()) +
							(v1e.getY() - v1s.getY()) * (v2e.getY() - v2s.getY()) +
							(v1e.getZ() - v1s.getZ()) * (v2e.getZ() - v2s.getZ());
		
		double area = Vertex.distBetween(v1s, v1e) * Vertex.distBetween(v2s, v2e);
		
		double angle = Math.acos(dotProduct/area);
		return angle;
	}
	
	public String toString()
	{
		return sym.vertex + " to " + vertex;
	}
}
