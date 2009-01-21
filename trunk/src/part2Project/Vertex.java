package part2Project;

public class Vertex
{
	private double x,y,z;
	private double nextx, nexty, nextz;
	private float totalWeight = 0;
	
	static int numVertices = 0;
	private int index;
	public int valency = 0;
	
	public boolean isOld = false;	// When smoothing, only old vertices make contributions
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
	
	public void contribute(HalfEdge e, float self, float neighbour, float diagonal)
	{
		this.addContribution(this, self);
		
		HalfEdge he = e;
		do
		{
			he.sym().vertex().addContribution(this, neighbour);
			he.sym().next().vertex().addContribution(this, diagonal);
		}
		while(!(he = he.next().sym()).equals(e));
		
		contributed = true;
	}
	
	// Add a contribution from vertex v
	public void addContribution(Vertex v, float weight)
	{
		float norm = totalWeight + weight;
		nextx = nextx * (totalWeight/norm) + v.x * (weight/norm);
		nexty = nexty * (totalWeight/norm) + v.y * (weight/norm);
		nextz = nextz * (totalWeight/norm) + v.z * (weight/norm);
		totalWeight = norm;
	}
	
	// Moves the vertex if it is meant to move this step, and clears temporary 
	// values, ready for the next step.
	public void smooth(boolean wantOld)
	{
		if((wantOld && isOld) || (!wantOld && !isOld))	// Could use xor, but less clear
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
	
	public String toString()
	{
		return new String(x + " " + y + " " + z);
	}
}
