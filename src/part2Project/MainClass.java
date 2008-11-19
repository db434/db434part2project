package part2Project;

import java.util.Vector;

public class MainClass
{
	private static ArgumentParser arg;
	private static HalfEdgeScheme hes;
	
	public static final boolean debug = true;
	
	public static void main(String[] args)
	{
		arg = new ArgumentParser(args);
		
		if(!arg.getInputFile().equals(""))
		{
			try {hes = PLYReader.readFile(arg.getInputFile());}
			catch(Exception e) {fatalException(e);}
		}
		else
		{
			System.err.println("Error: no input file selected.");
			System.exit(1);
		}
		
		//Do subdivision
		
		if(!arg.getOutputFile().equals(""))
		{
			PLYWriter.writeFile(arg.getOutputFile(), hes);
		}
	}
	
	public static String faceToString(Vector<Vertex> vertices)
	{
		return hes.faceToString(vertices);
	}
	
	public static void fatalException(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}
