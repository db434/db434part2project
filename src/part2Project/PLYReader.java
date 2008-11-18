package part2Project;

import java.io.*;

public class PLYReader
{
	static BufferedReader in;
	
	public static HalfEdgeScheme readFile(String fileName)
	{
		HalfEdgeScheme hes = new HalfEdgeScheme();
		
		try {in = new BufferedReader(new FileReader(fileName));}
		catch(Exception e)
		{
			System.err.println(e);
			System.exit(1);
		}
		
		//do reading
		
		return hes;
	}
}
