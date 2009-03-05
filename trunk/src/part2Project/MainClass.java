package part2Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class MainClass
{
	private static ArgumentParser arg;
	private static HalfEdgeScheme hes;
	
	public static boolean adaptive;
	
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
		
		adaptive = arg.isAdaptive();
		
		int stepsSoFar = 0;
		double startTime = System.currentTimeMillis();
		double endTime = startTime + arg.getRunTime()*1000;
		
		int numVertices = 0;
		
		//Do subdivision
		while(stepsSoFar<arg.getNumSteps() && System.currentTimeMillis()<endTime)
		{	
			numVertices = hes.numVertices();
			
			hes.subdivide(arg.getDegree());			
			stepsSoFar++;
			
			if(arg.printStats())
			{
				double timeTaken = System.currentTimeMillis() - startTime;
				//startTime = System.currentTimeMillis();	// Time each cycle separately
				
				System.out.println("Step " + stepsSoFar + ":\t\t" + timeTaken + "ms\n" +
									hes.stats());
			}			
					
			if(hes.numVertices() == numVertices)
			{
				System.out.println("No further division - stopping process.");
				break;
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
	
	// Allow quick indexing into the file of multipliers
	private static int headerLength = 577;
	private static int lineLength = 236;
	private static int tableLength = 98*lineLength + 10;
	private static String multFile = System.getProperty("user.dir") + "\\bounded_curvature_tables.txt";
	
	// Store multipliers so they don't have to be reread
	private static HashMap<Integer, Double> valencyToAlpha = new HashMap<Integer, Double>();
	private static HashMap<Integer, Double> valencyToBeta = new HashMap<Integer, Double>();
	private static HashMap<Integer, Double> valencyToGamma = new HashMap<Integer, Double>();
	private static double delta = 0;
	
	// Read multiplier values from the file of tables
	public static double readMult(int table, int valency)
	{
		double multiplier;
		
		// Check inputs?
		
		if((table == 1) && valencyToAlpha.containsKey(valency))
		{
			multiplier = valencyToAlpha.get(valency);
		}
		else if((table == 2) && valencyToBeta.containsKey(valency))
		{
			multiplier = valencyToBeta.get(valency);
		}
		else if((table == 3) && valencyToGamma.containsKey(valency))
		{
			multiplier = valencyToGamma.get(valency);
		}
		else if((table == 4) && (delta != 0))
		{
			multiplier = delta;
		}
		else try
		{
			BufferedReader file = new BufferedReader(new FileReader(multFile));
			
			int position = headerLength + (valency-3)*lineLength;
			if(table>1) position += tableLength;
			if(table>2) position += tableLength+1;		// "Gamma" is longer than "Beta"
			if(table>3) position += tableLength+1;		// So is "Delta"
			
			file.skip(position);
			
			String line = file.readLine();
			String[] values = line.split("[ \n\t\r]+");
			multiplier = Double.parseDouble(values[arg.getDegree()/2]);
			
			file.close();
			
			if(table==1) valencyToAlpha.put(valency, multiplier);
			if(table==2) valencyToBeta.put(valency, multiplier);
			if(table==3) valencyToGamma.put(valency, multiplier);
			if(table==4) delta = multiplier;
		}
		catch(Exception e)
		{
			multiplier = 1;
		}
		
		return multiplier;
	}
	
	public static void fatalException(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}
