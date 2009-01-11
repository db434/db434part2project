package part2Project;

public class Vertex implements Comparable<Vertex>
{
	private double x,y,z;
	private double nextx, nexty, nextz;
	private float totalWeight = 0;
	
	public boolean isOld = false;	// When smoothing, only old vertices make contributions
	public boolean contributed = false;
	
	public Vertex(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vertex(Vertex v)
	{
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
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
	
	public static Vertex average(Vertex v1, Vertex v2)
	{
		return new Vertex((v1.x + v2.x)/2,
						  (v1.y + v2.y)/2,
						  (v1.z + v2.z)/2);
	}
	
	public int compareTo(Vertex v)
	{
		// Doesn't seem to work perfectly
		return v.hashCode() - this.hashCode();
	}
	
	public String toString()
	{
		return new String(x + " " + y + " " + z);
	}
}
