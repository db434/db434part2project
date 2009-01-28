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
		valency3Smooth();
		
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
		self = 0; neighbour = 0;
		calculateWeights(degree, step);
		
		boolean oddStep = (step%2 == 1);
		
		// for all halfedges' vertices, make contributions
		for(HalfEdge e : edges)
		{
			Vertex v = e.vertex();
			if(!v.contributed)
			{
				int valency = v.valency;
				float alpha;
				float beta;
				float gamma;
				
				if(v.isEdge())
				{
					alpha = self;
					beta = neighbour;
					gamma = 0;
				}
				else
				{
					alpha = self*self;
					beta = self*neighbour;
					gamma = neighbour*neighbour;
				}
				
				if(false /*sometimes you don't want multipliers?*/)
				{
					alpha *= MainClass.readMult(1, valency);
					beta *= MainClass.readMult(2, valency);
					gamma *= MainClass.readMult(3, valency);
				}
								
				v.contribute(e, alpha, beta, gamma, oddStep);
			}			
		}
		
		for(Vertex v : vertices) v.smooth(oddStep);
	}
	
	private void valency3Smooth()
	{
		Vector<Vertex> valency3 = new Vector<Vertex>();
		
		for(HalfEdge e : edges)
		{
			Vertex v = e.vertex();
			
			if((v.valency == 3) && !valency3.contains(v))
			{
				v.valency3Smooth(e, rho);
				valency3.add(v);				
			}			
		}
		
		for(Vertex v : valency3)
		{
			v.smooth(true);
		}
	}
	
	// Method to remove any temporary statuses, to prepare for the next subdivision step
	private void reset()
	{
		for(HalfEdge h : edges) h.hasBeenSplit = false;
		for(Vertex v : vertices)
		{
			v.setToOld();
			v.contributed = false;
		}
		rho = 1;
	}
	
	// Store weights so they don't have to be recomputed
	private float rho = 1;
	private float self;
	private float neighbour;
	
	private void calculateWeights(int degree, int step)
	{		
		if(self == 0)
		{
			float weight;
			float d = degree;
			float s = step;			
			
			if(degree%2 == 0)	// Even degree
			{
				weight = (s+1)/(2*(d-s+1));
			}
			else
			{
				weight = s/(d-s);
			}
			
			self = weight;
			neighbour = (1-weight)/2;
			
			if((step%2) == 1)
			{
				float alpha = self*self;
				float beta = self*neighbour;
				float gamma = neighbour*neighbour;
								
				rho *= alpha/(alpha + 3*beta + 3*gamma);
			}
		}		
	}
	
	public void addVertex(Vertex v)		{vertices.add(v);}
	public void addHalfEdge(HalfEdge e) {edges.add(e);}
	public void addFace(Face f)			{faces.add(f);}
	
	public void addVertex(double x, double y, double z)
	{
		Vertex v = new Vertex(x,y,z);
		v.setToOld();
		vertices.add(v);
	}
	
	public HalfEdge addHalfEdge(Vertex v1, Vertex v2, Face f)
	{
		HalfEdge h = new HalfEdge(v2, f);
		edges.add(h);
		v2.valency++;
		
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
}
