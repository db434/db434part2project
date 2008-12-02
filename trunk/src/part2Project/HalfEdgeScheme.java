package part2Project;

import java.util.*;

public class HalfEdgeScheme
{
	private Vector<Vertex> vertices;
	private Vector<HalfEdge> edges;
	private Vector<Face> faces;
	
	public HalfEdgeScheme()
	{
		vertices = new Vector<Vertex>();
		edges = new Vector<HalfEdge>();
		faces = new Vector<Face>();
	}
	
	private class Pair
	{
		Vertex v1, v2;
		
		Pair(Vertex vertex1, Vertex vertex2)
		{
			v1 = vertex1;
			v2 = vertex2;
		}
		
		public boolean equals(Object o)
		{
			return (o instanceof Pair) && ((Pair)o).v1.equals(v1)
									   && ((Pair)o).v2.equals(v2);
		}
		
		public int hashCode()
		{
			return v1.hashCode() + v2.hashCode();
		}
	}
	
	private Map<Pair, HalfEdge> edgeMap = new HashMap<Pair, HalfEdge>();
	
	public int numVertices() {return vertices.size();}
	public int numEdges()	 {return edges.size();}
	public int numFaces()	 {return faces.size();}
	
	public Vertex getVertex(int index) 		{return vertices.get(index);}
	public HalfEdge getHalfEdge(int index)  {return edges.get(index);}
	public Face getFace(int index)	   		{return faces.get(index);}
	
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
	
	public HalfEdge addHalfEdge(Vertex v1, Vertex v2)
	{
		HalfEdge h = new HalfEdge(v2);
		edges.add(h);
		
		Pair p = new Pair(v2,v1);
		
		// Match this half-edge up with the symmetric one, if it exists
		if(edgeMap.containsKey(p))
		{
			HalfEdge h2 = edgeMap.remove(p);
			h.setSym(h2);
			h2.setSym(h);
		}
		else
		{
			edgeMap.put(new Pair(v1,v2), h);
		}
		
		return h;
	}
	
	public void addFace(Vector<Integer> indices)
	{
		Vector<Vertex> verts = new Vector<Vertex>();
		Vector<HalfEdge> halfedges = new Vector<HalfEdge>();
		for(Integer i : indices) verts.add(vertices.get(i));
		
		faces.add(new Face(verts));
		
		// Create half-edges for each pair of adjacent vertices
		for(int i=1; i<verts.size(); i++)
		{
			halfedges.add(addHalfEdge(verts.get(i-1), verts.get(i)));
		}
		halfedges.add(addHalfEdge(verts.lastElement(), verts.firstElement()));
		
		// Go through the half-edges, linking them to the "next" half-edges
		for(int i=1; i<halfedges.size(); i++)
		{
			halfedges.get(i-1).setNext(halfedges.get(i));
		}
		halfedges.lastElement().setNext(halfedges.firstElement());
	}
	
	// Look for inconsistencies/boundaries and deal with them
	public void tidy() throws Exception
	{
		// For now, just report an error. Could perhaps create dummy faces later.
		if(!edgeMap.isEmpty()) throw new Exception("Boundary detected in mesh.");
	}
	
	public String stats()
	{
		return new String("" +
				"Vertices:\t" + numVertices() + "\n" +
				"Faces:\t" + numFaces() + "\n");
		
		// Volume too?
	}
	
	public String faceToString(Vector<Vertex> verts)
	{
		String s = String.valueOf(verts.size());
		for(Vertex v : verts) s += " " + vertices.indexOf(v);
		
		return s;
	}
}
