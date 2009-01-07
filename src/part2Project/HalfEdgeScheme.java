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
	
	public void subdivide(int degree)
	{
		refine();
		for(int step=1; step<=(degree/2); step++) smooth(degree, step);
		
		reset();
	}
	
	// Method to introduce more vertices to the mesh
	private void refine()
	{
		int numEdges = numEdges();	// Store the starting value, as more will be added
		for(int i=0; i<numEdges; i++)
		{
			HalfEdge e = edges.get(i);
			if(e.hasBeenSplit) continue;
			else e.face().split(e, this);
		}
	}
	
	// Method to adjust positions of vertices
	private void smooth(int degree, int step)
	{
		valencyToWeight.clear();
		
		// for all halfedges' vertices, make contributions
		for(HalfEdge e : edges)
		{
			Vertex v = e.vertex();
			if(v.isOld && !v.contributed)
			{
				float weight = getWeight(e, degree, step);
				float self = weight*weight;
				float neighbour = weight*(1-weight)/2;			//
				float diagonal = (1-weight)*(1-weight)/4;		//
				v.contribute(e, self, neighbour, diagonal);
			}			
		}
		
		for(Vertex v : vertices) v.smooth();
	}
	
	// Method to remove any temporary statuses, to prepare for the next subdivision step
	private void reset()
	{
		for(HalfEdge h : edges) h.hasBeenSplit = false;
		for(Vertex v : vertices) v.isOld = true;
	}
	
	// Store weights so they don't have to be recomputed
	private HashMap<Integer, Float> valencyToWeight = new HashMap<Integer, Float>();
	
	// TODO: Perhaps extend to return all three weights (self, neighbour, diagonal)?
	// Returns the primary weight for the edge's vertex to use
	private float getWeight(HalfEdge e, int degree, int step)
	{
		int valency = 1;
		HalfEdge he = e;
		
		while(!(he = he.next().sym()).equals(e))
		{
			valency++;
		}
		
		float weight = 0;
		float d = degree;
		float s = step;
		if(valencyToWeight.containsKey(valency))	// Already computed
		{
			weight = valencyToWeight.get(valency);
		}
		else										// Need to compute
		{
			//if(valency == 4)	// Main case
			{
				if(degree%2 == 0)	// Even degree
				{
					weight = (d+1)/(2*(d-s+1));
				}
				else
				{
					weight = s/(d-s);
				}
			}
			//else System.out.println("Valency = "+valency);
			
			valencyToWeight.put(valency, weight);
		}
		
		return weight;
	}
	
	public void addVertex(Vertex v)		{vertices.add(v);}
	public void addHalfEdge(HalfEdge e) {edges.add(e);}
	public void addFace(Face f)			{faces.add(f);}
	
	public void addVertex(double x, double y, double z)
	{
		Vertex v = new Vertex(x,y,z);
		v.isOld = true;
		vertices.add(v);
	}
	
	public HalfEdge addHalfEdge(Vertex v1, Vertex v2, Face f)
	{
		HalfEdge h = new HalfEdge(v2, f);
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
		
		Face f = new Face(verts);
		faces.add(f);
		
		// Create half-edges for each pair of adjacent vertices
		for(int i=1; i<verts.size(); i++)
		{
			halfedges.add(addHalfEdge(verts.get(i-1), verts.get(i), f));
		}
		halfedges.add(addHalfEdge(verts.lastElement(), verts.firstElement(), f));
		
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
				"HalfEdges:\t" + numEdges() + "\n" +
				"Faces:\t\t" + numFaces() + "\n");
		
		// Volume too?
	}
	
	public String faceToString(Vector<Vertex> verts)
	{
		String s = String.valueOf(verts.size());
		for(Vertex v : verts) s += " " + vertices.indexOf(v);
		
		return s;
	}
}
