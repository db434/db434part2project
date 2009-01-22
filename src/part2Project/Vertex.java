package part2Project;

public class Vertex
{
	private double x,y,z;
	private double nextx, nexty, nextz;
	private double totalWeight = 0;
	
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
	
	public double getX() {return x;}
	public double getY() {return y;}
	public double getZ() {return z;}
	
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
	public void addContribution(Vertex v, double weight, boolean oddStep)
	{
		if(v.shouldContribute(this, oddStep))
		{
			double norm = totalWeight + weight;
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
	
	// A special final contribution step for vertices of valency 3
	public void valency3Smooth(HalfEdge e, double rho)
	{
		if(!contributed && valency == 3)
		{
			double delta = MainClass.readMult(4, 3);
			double self = rho;
			double edge = (1-rho)*(1-delta)/3;
			double diagonal = (1-rho)*delta/3;
			
			HalfEdge he = e;
			
			addContribution(this, self, true);
			
			for(int i=0; i<3; i++)
			{
				HalfEdge he2 = he.sym();
				Vertex v = he2.vertex();
				addContribution(v, edge, false);
				
				// Access different points depending on if the face has been divided or not
				if(v.old) 	addContribution(he2.next().vertex(), diagonal, true);
				else		addContribution(he2.ahead().next().ahead().vertex(), diagonal, true);
				
				he = he.next();
			}
		}
		
		contributed = true;
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
	
	// For vertices created in the centre of faces
	public static Vertex weightedAverage(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
	{
		double mult1 = MainClass.readMult(3, v1.valency);
		double mult2 = MainClass.readMult(3, v2.valency);
		double mult3 = MainClass.readMult(3, v3.valency);
		double mult4 = MainClass.readMult(3, v4.valency);
		double total = mult1 + mult2 + mult3 + mult4;
		
		return new Vertex((v1.x*mult1 + v2.x*mult2 + v3.x*mult3 + v4.x*mult4)/total,
						  (v1.y*mult1 + v2.y*mult2 + v3.y*mult3 + v4.y*mult4)/total,
						  (v1.z*mult1 + v2.z*mult2 + v3.z*mult3 + v4.z*mult4)/total);
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
