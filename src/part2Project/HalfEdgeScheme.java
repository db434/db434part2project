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
	
	public int numVertices() {return vertices.size();}
	public int numEdges()	 {return edges.size();}
	public int numFaces()	 {return faces.size();}
	
	public Vertex getVertex(int index) {return vertices.get(index);}
	public Edge getEdge(int index)	   {return edges.get(index);}
	public Face getFace(int index)	   {return faces.get(index);}
	
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
	
	public void addFace(Vector<Integer> indices)
	{
		Vector<Vertex> verts = new Vector<Vertex>();
		
		for(Integer i : indices) verts.add(vertices.get(i));
		
		faces.add(new Face(verts));
	}
	
	public String faceToString(Vector<Vertex> verts)
	{
		String s = String.valueOf(verts.size());
		for(Vertex v : verts) s += " " + vertices.indexOf(v);
		
		return s;
	}
}
