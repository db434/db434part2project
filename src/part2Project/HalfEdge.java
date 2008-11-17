package part2Project;

public class HalfEdge
{
	private Vertex vertex;
	private HalfEdge sym;
	private HalfEdge next;
	
	public HalfEdge(Vertex v)
	{
		vertex = v;
		sym = null;		//Parent Edge sets this
		next = null;	//Associated Face sets this
	}
	
	public Vertex vertex() {return vertex;}
	public HalfEdge sym()  {return sym;}
	public HalfEdge next() {return next;}
	
	public void setSym(HalfEdge h)  {sym = h;}
	public void setNext(HalfEdge h) {next = h;}
}
