package part2Project;

import java.util.Vector;

public class MainClass
{
	private static ArgumentParser arg;
	private static HalfEdgeScheme hes;
	
	public static final boolean debug = false;
	
	public static void main(String[] args)
	{
		arg = new ArgumentParser(args);
		
		if(!arg.getInputFile().equals(""))
		{
			System.out.printf("Attempting to read file " + arg.getInputFile() + "... ");
			
			try {hes = PLYReader.readFile(arg.getInputFile());}
			catch(Exception e)
			{
				System.out.println("Failed.");
				fatalException(e);
			}
			
			System.out.println("Complete.");
		}
		else
		{
			System.err.println("Error: no input file selected.");
			System.exit(1);
		}
		
		//Do subdivision
		
		if(!arg.getOutputFile().equals(""))
		{
			System.out.printf("Attempting to write file " + arg.getOutputFile() + "... ");
			
			try {PLYWriter.writeFile(arg.getOutputFile(), hes);}
			catch(Exception e)
			{
				System.out.println("Failed.");
				fatalException(e);
			}
			
			System.out.println("Complete.");
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
