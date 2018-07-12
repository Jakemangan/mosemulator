package compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * ReadFile class reads the chosen assembly file and produces a String array
 * Each element in the array corresponds to a single line in the assembly file
 */

public class ReadAssemblyFile {
	
	private File assemblyFile;
	private String filepath = "";//"asmprogram1.txt";
	private Scanner scan;
	private String[] assemblyLines;
	
	public ReadAssemblyFile(String inputFile)
	{
		filepath = inputFile;
		
		assemblyFile = new File(filepath);
		
		makeLink();
		parseLines();
		closeLink();
		
		System.out.println("\n---Begin Assembly File---");
		for(String str : assemblyLines)
		{
			System.out.println(str);
		}
		System.out.println("---End Assembly File---\n");
		
	}
	
	
	
	
	
	
	
	
	public void parseLines()
	{
		ArrayList<String> asmLines = new ArrayList<String>();
		
		while(scan.hasNextLine())
		{
			String line = scan.nextLine();
			asmLines.add(line);
		}
		
		assemblyLines = asmLines.toArray(new String[asmLines.size()]);
	}
	
	
	public void makeLink()
	{
		try
		{
			scan = new Scanner(assemblyFile);
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Assembly file not found");
		}
	}

	public void closeLink()
	{
		scan.close();
	}
	
	public String[] getAssemblyText()
	{
		return assemblyLines;
	}
	
}
