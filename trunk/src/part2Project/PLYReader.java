package part2Project;

import java.io.*;
import java.util.Vector;

public class PLYReader
{	
	public static HalfEdgeScheme readFile(String fileName) throws Exception
	{
		HalfEdgeScheme hes = new HalfEdgeScheme();
		BufferedReader in;
		
		in = new BufferedReader(new FileReader(fileName));
		
		String line;
		int vertices=0, faces=0;
		
		while((line = in.readLine()) != null)
		{
			//Deal with format? eg. "format ascii 1.0"
			
			if(line.startsWith("element"))
			{
				line = line.substring(8);
				
				if(line.startsWith("vertex"))
				{
					vertices = Integer.parseInt(line.substring(7));
				}
				else if(line.startsWith("face"))
				{
					faces = Integer.parseInt(line.substring(5));
				}
			}
			else if(line.startsWith("end_header")) break;
		}
		
		for(int i=0; i<vertices; i++)
		{
			line = in.readLine();
			String[] coords = line.split(" ");
			hes.addVertex(Double.parseDouble(coords[0]),
						  Double.parseDouble(coords[1]),
						  Double.parseDouble(coords[2]));
		}
		
		for(int i=0; i<faces; i++)
		{
			line = in.readLine();
			String[] values = line.split(" ");
			
			int numVertices = Integer.parseInt(values[0]);
			Vector<Integer> indices = new Vector<Integer>();
			for(int j=1; j <= numVertices; j++)
			{
				indices.add(Integer.parseInt(values[j]));
			}
			
			hes.addFace(indices);
		}
		
		hes.tidy();
		
		in.close();
		
		return hes;
	}
}
