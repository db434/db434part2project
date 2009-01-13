package part2Project;

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
		
		int stepsSoFar = 0;
		double startTime = System.currentTimeMillis();
		double endTime = startTime + arg.getRunTime()*1000;
		
		//Do subdivision
		while(stepsSoFar<arg.getNumSteps() && System.currentTimeMillis()<endTime)
		{
			hes.subdivide(arg.getDegree());			
			stepsSoFar++;
			
			if(arg.printStats())
			{
				double timeTaken = System.currentTimeMillis() - startTime;
				startTime = System.currentTimeMillis();	// Update for next cycle
				
				System.out.println("Step " + stepsSoFar + ":\t\t" + timeTaken + "ms\n" +
									hes.stats());
			}			
		}
		
		if(!arg.getOutputFile().equals(""))
		{
			System.out.printf("Attempting to write file " + arg.getOutputFile() + "... ");
			
			try {PLYWriter.writeFile(arg, hes);}
			catch(Exception e)
			{
				System.out.println("Failed.");
				fatalException(e);
			}
			
			System.out.println("Complete.");
		}
	}
	
	public static void fatalException(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}
