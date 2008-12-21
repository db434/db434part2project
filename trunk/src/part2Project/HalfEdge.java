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
		sym = null;		//Parent Edge sets this
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
		return Vertex.average(vertex, sym.vertex);
	}
	
	public HalfEdge split(HalfEdgeScheme hes, Face f1, Face f2)
	{
		HalfEdge e = new HalfEdge(vertex, f1);
		
		if(sym.hasBeenSplit)
		{
			e.sym = sym;
			sym.sym = e;
			
			vertex = sym.vertex;	// Update to the midpoint
			sym = sym.ahead();
			face = f2;
		}
		else
		{
			e.sym = sym;
			
			Vertex midpoint = Vertex.average(vertex, sym.vertex);
			hes.addVertex(midpoint);
			
			vertex = midpoint;
			face = f2;
		}
		
		e.hasBeenSplit = true;
		hasBeenSplit = true;
		
		hes.addHalfEdge(e);
		return e;
	}
}
