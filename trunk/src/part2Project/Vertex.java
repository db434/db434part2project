package part2Project;

public class Vertex
{
	private double x,y,z;
	private double nextx, nexty, nextz;
	private float totalWeight = 0;
	
	static int numVertices = 0;
	private int index;
	public int valency = 0;
	
	// Determines whether a vertex should contribute or be smoothed
	private boolean old = false;
	private boolean edge = false;
	private boolean face = false;
	public boolean contributed = false;
	
	public Vertex(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		
		index = numVertices++;
	}
	
	public void move(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	private boolean shouldContribute(Vertex v, boolean oddStep)
	{
		boolean result;
		
		if(oddStep)
		{
			if(old) 		result = v.old;
			else if(edge) 	result = v.old || v.edge;
			else 			result = v.old || v.edge;
		}
		else
		{
			if(old) 		result = v.edge || v.face;
			else if(edge) 	result = v.edge || v.face;
			else 			result = v.edge;
		}
		
		return result;
	}
	
	public void contribute(HalfEdge e, float self, float neighbour, float diagonal, boolean oddStep)
	{
		addContribution(this, self, oddStep);
		
		HalfEdge he = e;
		do
		{
			he.sym().vertex().addContribution(this, neighbour, oddStep);
			he.sym().next().vertex().addContribution(this, diagonal, oddStep);
		}
		while(!(he = he.next().sym()).equals(e));
		
		contributed = true;
	}
	
	// Add a contribution from vertex v
	public void addContribution(Vertex v, float weight, boolean oddStep)
	{
		if(v.shouldContribute(this, oddStep))
		{
			float norm = totalWeight + weight;
			nextx = nextx * (totalWeight/norm) + v.x * (weight/norm);
			nexty = nexty * (totalWeight/norm) + v.y * (weight/norm);
			nextz = nextz * (totalWeight/norm) + v.z * (weight/norm);
			totalWeight = norm;
		}
	}
	
	private boolean shouldSmooth(boolean oddStep)
	{
		boolean result;
		
		if(oddStep) result = old || edge;
		else 		result = edge || face;
		
		return result;
	}
	
	// Moves the vertex if it is meant to move this step, and clears temporary 
	// values, ready for the next step.
	public void smooth(boolean oddStep)
	{
		if(shouldSmooth(oddStep))
		{
			x = nextx;
			y = nexty;
			z = nextz;
		}
			
		nextx = 0; nexty = 0; nextz = 0; totalWeight = 0;
		contributed = false;
	}
	
	// Takes into account the valencies/multipliers of the vertices
	public static Vertex weightedAverage(Vertex v1, Vertex v2)
	{
		double mult1 = MainClass.readMult(2, v1.valency);
		double mult2 = MainClass.readMult(2, v2.valency);
		
		return new Vertex((v1.x*mult1 + v2.x*mult2)/(mult1 + mult2),
						  (v1.y*mult1 + v2.y*mult2)/(mult1 + mult2),
						  (v1.z*mult1 + v2.z*mult2)/(mult1 + mult2));
	}
	
	public static double distBetween(Vertex v1, Vertex v2)
	{
		double dx = v1.x - v2.x;
		double dy = v1.y - v2.y;
		double dz = v1.z - v2.z;
		
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	// Returns the vertex's position in the vertex vector
	public int getIndex()
	{
		// All vertices store their own index to allow quick look-up
		return index;
	}
	
	public boolean isOld()	{return old;}
	public boolean isEdge()	{return edge;}
	public boolean isFace() {return face;}
	
	public void setToOld()
	{
		old = true;
		edge = false;	face = false;
	}
	
	public void setToEdge()
	{
		edge = true;
		old = false;	face = false;
	}
	
	public void setToFace()
	{
		face = true;
		old = false;	edge = false;
	}
	
	public String toString()
	{
		return new String(x + " " + y + " " + z);
	}
}
