package part2Project;

public class Vertex
{
	private double x,y,z;
	private double nextx, nexty, nextz;
	private float totalWeight = 0;
	
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
	
	public void addContribution(Vertex v, float weight)
	{
		float norm = totalWeight + weight;
		nextx = nextx * (totalWeight/norm) + v.x * (weight/norm);
		nexty = nexty * (totalWeight/norm) + v.y * (weight/norm);
		nextz = nextz * (totalWeight/norm) + v.z * (weight/norm);
		totalWeight = norm;
	}
	
	public void smooth()
	{
		x = nextx;
		y = nexty;
		z = nextz;
		totalWeight = 0;
	}
	
	public static Vertex average(Vertex v1, Vertex v2)
	{
		return new Vertex((v1.x + v2.x)/2,
						  (v1.y + v2.y)/2,
						  (v1.z + v2.z)/2);
	}
	
	public String toString()
	{
		return new String(x + " " + y + " " + z);
	}
}
