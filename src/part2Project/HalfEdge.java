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
	public HalfEdge ahead()	{return next.sym.next;}
	
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
		return Vertex.weightedAverage(vertex, sym.vertex);
	}
	
	// Split this edge in two, returning the new half (this.ahead())
	// f1 is for the new half, and f2 is for the old half
	public HalfEdge split(HalfEdgeScheme hes, Face f1, Face f2)
	{
		HalfEdge e = new HalfEdge(vertex, f1);
		
		if(sym.hasBeenSplit)
		{
			e.sym = sym;
			sym.sym = e;
			
			vertex = sym.vertex;	// Update to the midpoint
			vertex.boundary = false;	// The mesh is now complete around vertex
			
			sym = sym.ahead();
			face = f2;
		}
		else
		{
			e.sym = sym;
			
			Vertex midpoint = midpoint();
			hes.addVertex(midpoint);
			midpoint.valency = 4;	// Is this a safe assumption?
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
	
	public String toString()
	{
		return sym.vertex + " to " + vertex;
	}
}
