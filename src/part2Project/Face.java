package part2Project;

import java.util.Vector;

public class Face
{
	private Vector<Vertex> vertices;
	
	public Face(Vector<Vertex> v)
	{
		vertices = v;
	}
	
	public String toString()
	{
		// Try to do this a better way
		return MainClass.faceToString(vertices);		
	}
}
