package part2Project;

public class Vertex
{
	private double x,y,z;
	
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
