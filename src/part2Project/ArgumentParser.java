package part2Project;

public class ArgumentParser
{
	private int numSteps = Integer.MAX_VALUE;		//Default starting values or exceptions?
	private int degree = 3;
	private double runTime = Double.MAX_VALUE;
	private boolean printStats = false;
	private boolean adaptive = false;
	private Scheme scheme = Scheme.WINGEDEDGE;
	private String inFile = "", outFile = "";
	
	enum Scheme {WINGEDEDGE/*, QUADTREE*/};
	
	public ArgumentParser(String[] args)
	{
		for(String s : args)
		{
			if(s.startsWith("steps="))
			{
				numSteps = Integer.parseInt(s.substring(6));
				if(MainClass.debug) System.out.println("Number of steps set to " + numSteps);
			}
			else if(s.startsWith("degree="))
			{
				degree = Integer.parseInt(s.substring(7));
				if(MainClass.debug) System.out.println("Degree set to " + degree);
			}
			else if(s.startsWith("time="))
			{
				runTime = Double.parseDouble(s.substring(5));
				if(MainClass.debug) System.out.println("Run time set to " + runTime);
			}
			else if(s.equals("stats"))
			{
				printStats = true;
				if(MainClass.debug) System.out.println("Stats will be printed");
			}
			else if(s.equals("adaptive"))
			{
				adaptive = true;
				if(MainClass.debug) System.out.println("Using adaptive algorithm");
			}
			else if(s.startsWith("scheme="))
			{
				String s2 = s.substring(7);
				if(s2.equals("wingededge")) scheme = Scheme.WINGEDEDGE;
				//else if(scheme.equals("quadtree")) ds = DataStructure.QUADTREE;
				if(MainClass.debug) System.out.println("Scheme set to " + scheme);
			}
			else if(s.startsWith("read="))
			{
				inFile = System.getProperty("user.dir") + "\\PLY Files\\" + s.substring(5);
				//if(MainClass.debug) System.out.println("Reading file: " + inFile);
			}
			else if(s.startsWith("write="))
			{
				outFile = System.getProperty("user.dir") + "\\PLY Files\\" + s.substring(6);
				//if(MainClass.debug) System.out.println("Writing file: " + outFile);
			}
			else if(s.equals("help"))
			{
				System.out.printf(helpText());
				System.exit(0);
			}
			else
			{
				System.err.println("Unrecognised argument: " + s);
				System.out.println(helpText());
			}
		}
	}
	
	public int getNumSteps() 		{return numSteps;}
	public int getDegree()			{return degree;}
	public double getRunTime()		{return runTime;}
	public boolean printStats()		{return printStats;}
	public boolean isAdaptive()		{return adaptive;}
	public Scheme getScheme()		{return scheme;}
	public String getInputFile() 	{return inFile;}
	public String getOutputFile()	{return outFile;}
	
	private String helpText()
	{
		return new String("" +
				"steps=x\tRun the subdivision algorithm for x steps.\n" +
				"degree=n\tCreate a surface with nth degree smoothness." +
				"time=t\tRun the subdivision algorithm for t seconds.\n" +
				"stats\tPrint information about the mesh after each subdivision step.\n" +
				"scheme=s\tUse subdivision scheme s.\n" +
				"\tCurrent options: wingededge\n" +
				"help\tShow this information.\n");
	}
}
