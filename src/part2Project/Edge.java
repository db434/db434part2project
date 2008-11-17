package part2Project;

public class Edge
{
	private HalfEdge h1,h2;
	
	public Edge(Vertex v1, Vertex v2)
	{
		h1 = new HalfEdge(v1);
		h2 = new HalfEdge(v2);
		
		h1.setSym(h2);
		h2.setSym(h1);
	}
}
