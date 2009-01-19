package part2Project;

import java.io.*;
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
		valencyToAlpha.clear();
		
		// for all halfedges' vertices, make contributions
		for(HalfEdge e : edges)
		{
			Vertex v = e.vertex();
			if(!v.contributed)
			{
				int valency = generateWeights(e, degree, step);
				float self = valencyToAlpha.get(valency);
				float neighbour = valencyToBeta.get(valency);
				float diagonal = valencyToGamma.get(valency);
				v.contribute(e, self, neighbour, diagonal);
			}			
		}
		
		// Want new points to move on even steps, and old ones to move on odd steps
		boolean wantOld = (step%2 == 1);
		for(Vertex v : vertices) v.smooth(wantOld);
	}
	
	// Method to remove any temporary statuses, to prepare for the next subdivision step
	private void reset()
	{
		for(HalfEdge h : edges) h.hasBeenSplit = false;
		for(Vertex v : vertices) v.isOld = true;
	}
	
	// Store weights so they don't have to be recomputed
	private HashMap<Integer, Float> valencyToAlpha = new HashMap<Integer, Float>();
	private HashMap<Integer, Float> valencyToBeta = new HashMap<Integer, Float>();
	private HashMap<Integer, Float> valencyToGamma = new HashMap<Integer, Float>();
	
	private static int headerLength = 577;
	private static int lineLength = 236;
	private static int tableLength = 97*lineLength + 10;	// Minus one line, to account for reading one
	
	// Returns the valency of the edge's vertex, to allow access to the HashMaps
	private int generateWeights(HalfEdge e, int degree, int step)
	{
		int valency = 1;
		HalfEdge he = e;
		
		while(!(he = he.next().sym()).equals(e))
		{
			valency++;
		}
		
		float weight;
		
		if(valencyToAlpha.containsKey(valency))	// Already computed
		{
			weight = valencyToAlpha.get(valency);
		}
		else if((degree%2) == 0)					// The file doesn't contain even degree data
		{
			calculateWeight(valency, degree, step);
		}
		else try									// Read the file of weights
		{System.out.println("===== "+valency+" =====");	
			BufferedReader file = new BufferedReader(new FileReader(
					System.getProperty("user.dir") + "\\bounded_curvature_tables.txt"));
			
			// Read the alpha (self) value
			file.skip(headerLength + (valency-3)*lineLength);
			String line = file.readLine();
			String[] values = line.split("[ \n\t\r]+");
			
			// The first value seems to be blank, so don't subtract 1 here.
			weight = Float.parseFloat(values[degree/2]);		System.out.println(weight);	
			
			valencyToAlpha.put(valency, weight);
			
			// Read the beta (neighbour) value
			file.skip(tableLength);
			line = file.readLine();
			values = line.split("[ \n\t\r]+");
			weight = Float.parseFloat(values[degree/2]);System.out.println(weight);	
			valencyToBeta.put(valency, weight);
			
			// Read the gamma (diagonal) value
			file.skip(tableLength+1);
			line = file.readLine();
			values = line.split("[ \n\t\r]+");
			weight = Float.parseFloat(values[degree/2]);System.out.println(weight);	
			valencyToGamma.put(valency, weight);			
		}
		catch(Exception excpt)						// Calculate the weight crudely
		{
			calculateWeight(valency, degree, step);
		}
		
		return valency;
	}
	
	private void calculateWeight(int valency, int degree, int step)
	{
		float weight;
		float d = degree;
		float s = step;
		
		//if(valency == 4)	// Main case
		{
			if(degree%2 == 0)	// Even degree
			{
				weight = (s+1)/(2*(d-s+1));
			}
			else
			{
				weight = s/(d-s);
			}
		}
		//weight *= 4/valency;
		
		valencyToAlpha.put(valency, weight*weight);
		valencyToBeta.put(valency, weight*(1-weight)/2);
		valencyToBeta.put(valency, (1-weight)*(1-weight)/4);
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
}
