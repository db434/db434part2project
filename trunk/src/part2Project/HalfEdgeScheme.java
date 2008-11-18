package part2Project;

import java.util.Vector;

public class HalfEdgeScheme
{
	private Vector<Vertex> vertices;
	private Vector<Edge> edges;
	private Vector<Face> faces;
	
	public HalfEdgeScheme()
	{
		vertices = new Vector<Vertex>();
		edges = new Vector<Edge>();
		faces = new Vector<Face>();
	}
	
	private void refine() {}
	private void smooth() {}
	
	public void subdivide(int degree)
	{
		refine();
		for(int i=1; i<degree; i++) smooth();
	}
	
	public void addVertex(double x, double y, double z)
	{
		vertices.add(new Vertex(x,y,z));
	}
	
	public void addEdge(Vertex v1, Vertex v2)
	{
		edges.add(new Edge(v1,v2));
	}
	
	public void addFace(Vector<Vertex> vertices)
	{
		faces.add(new Face(vertices));
	}
}
