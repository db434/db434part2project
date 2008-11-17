package part2Project;

public class ArgumentParser
{
	int numSteps = -1;		//Default starting values or exceptions?
	double runTime = -1;
	boolean printStats = false;
	DataStructure ds = DataStructure.WINGEDEDGE;	
	
	enum DataStructure {WINGEDEDGE/*, QUADTREE*/};
	
	public ArgumentParser(String[] args)
	{
		for(String s : args)
		{
			if(s.startsWith("steps="))
			{
				numSteps = Integer.parseInt(s.substring(6));
				System.out.println("Number of steps set to " + numSteps);
			}
			else if(s.startsWith("time="))
			{
				runTime = Double.parseDouble(s.substring(5));
				System.out.println("Run time set to " + runTime);
			}
			else if(s.equals("stats"))
			{
				printStats = true;
				System.out.println("Stats will be printed");
			}
			else if(s.startsWith("scheme="))
			{
				String scheme = s.substring(7);
				if(scheme.equals("wingededge")) ds = DataStructure.WINGEDEDGE;
				//else if(scheme.equals("quadtree")) ds = DataStructure.QUADTREE;
				System.out.println("Scheme set to " + ds);
			}
			else if(s.equals("help"))
			{
				System.out.printf(helpText());
			}
			else System.err.println("Unrecognised argument: " + s);
		}
	}
	
	private String helpText()
	{
		return new String("" +
				"steps=x\tRun the subdivision algorithm for x steps.\n" +
				"time=t\tRun the subdivision algorithm for t seconds.\n" +
				"stats\tPrint information about the mesh after each subdivision step.\n" +
				"scheme=s\tUse subdivision scheme s.\n" +
				"\tCurrent options: wingededge\n" +
				"help\tShow this information.\n");
	}
}
