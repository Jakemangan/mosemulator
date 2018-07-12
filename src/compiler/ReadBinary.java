package compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/* ReadBinary class accesses and reads the chosen .txt file, loading the "binary" code within into an array */

/* For use after the assembler program has translated the assembly code into binary. 
 * 
 * assembly file -> assembler -> binary file -> read binary into simulator -> parse instructions from binary -> execute instructions in order
 *
 * Binary instructions are first read into an ArrayList, then a String[] is initalised using the length of the array
 * then the ArrayList elements are used to populate the String[]
 * */

public class ReadBinary {
	
	private String binaryPath = "./binary.txt";
	private String[] instructions;
	private File binaryFile;
	private Scanner scan;

	public ReadBinary()
	{
		binaryFile = new File(binaryPath);
		
		makeLink();
		extractBinary();
		closeLink();
		
	}
	
	public void extractBinary()
	{
		extractIndividualInstructions();
	}
	
	public void extractIndividualInstructions()
	{
		ArrayList<String> binaryList = new ArrayList<String>(); //arraylist to hold 8-bit binary strings
		String allBinary = scan.next(); //extremely long string containing all binary in binary.txt
		
		int index = 0;
		while(index < allBinary.length())
		{
			binaryList.add(allBinary.substring(index, Math.min(index + 8, allBinary.length())));
			index += 8;
		}
		
		instructions = binaryList.toArray(new String[binaryList.size()]);
		
	}
	
	
	public String[] getInstructions()
	{
		return instructions;
	}
	
	public void makeLink()
	{
		try
		{
			scan = new Scanner(binaryFile);
		}
		catch(FileNotFoundException e)
		{
			System.err.println("BinaryFile not found");
		}
	}
	
	public void closeLink()
	{
		scan.close();
	}
	
}
