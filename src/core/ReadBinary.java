package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadBinary {

	private String filepath = "";
	
	private Scanner scan;
	private File file;
	private String binary;
	private ArrayList<String> programAL;
	private int[] program;
	private ArrayList<String> directiveValues;
	
	public ReadBinary(String filepathToRead)
	{
		filepath = filepathToRead;
		
		programAL = new ArrayList<String>();
		directiveValues = new ArrayList<String>();
		
		makeLink();
		read();
		closeLink();
		
		makeProgram();
		
		printPrograms();
		
		for(int i = 0; i < program.length; i++)
		{
			int programByte = program[i];
			System.out.println(programByte);
		}
	}
	
	public void makeProgram()
	{
		program = new int[programAL.size()];

		int index = 0;
		for(String str : programAL)
		{
			int programByte = Integer.parseInt(str, 2);
			program[index] = programByte;
			index++;
		}
		
		System.out.println("\n[ReadBinary] Program converted to integer format");
	}
	
	public void makeLink()
	{
		file = new File(filepath);
		
		try
		{
			scan = new Scanner(file);
		}
		catch(FileNotFoundException e)
		{
			System.err.println("[ReadBinary] Binary file not found");
		}
	}
	
	/*
	 * TODO: At the moment this method relies on both parts of a directive string 
	 * being prefixed with * i.e. *00000001 *00000001
	 * Change this so that only the line has to be prefixed with an asterix
	 * instead of both string parts. 
	 * This is in part due to directiveHandler.convertLineToBinary() -> String outputBinary = "*" + currentLocationBinary + " *" + dataBinary;
	 *
	 */
	public void read()
	{
		System.out.println("");
		
		while(scan.hasNext())
		{
			String s = scan.next();
			if(s.contains("*")) //String is a directive string
			{
				s = s.replace("*", "");
				directiveValues.add(s);
			}
			else //Normal program string
			{
				programAL.add(s);
			}
			System.out.println("[ReadBinary] Read in: " + s);
		}
	}
	
	public void closeLink()
	{
		scan.close();
	}
	
	public int[] getProgram()
	{
		return program;
	}
	
	public String[] getDirectiveValues()
	{
		String[] directiveValuesArray = directiveValues.toArray(new String[directiveValues.size()]);
		return directiveValuesArray;
	}
	
	public void printPrograms()
	{
		System.out.println("\nCore Program:");
		for(String str : programAL)
		{
			System.out.println(str);
		}
		
		System.out.println("\nDirective Program:");
		for(String str : directiveValues)
		{
			System.out.println(str);
		}
		System.out.println("");
	}
}
