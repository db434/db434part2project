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
}
