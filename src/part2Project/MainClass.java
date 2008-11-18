package part2Project;

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
			//try and catch filenotfound stuff? or do this in reader
			hes = PLYReader.readFile(arg.getInputFile());
		}
		else
		{
			System.err.println("Error: no input file seleced.");
			System.exit(1);
		}
		
		//Do subdivision
		
		if(!arg.getOutputFile().equals(""))
		{
			PLYWriter.writeFile(arg.getOutputFile(), hes);
		}
	}
}
