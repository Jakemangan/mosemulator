package compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class OutputBinaryToFile {

	String[] binaryOutput;
	String[] directiveOutput;
	
	String filepath = "";//"./BinaryOutput.txt";
	File file;
	PrintWriter writer;
	
	/*
	 * Specifies the format of the output to the text file.
	 * True for long output i.e. 11010011 01000101 01001100 111010101 101010101 00011101 etc
	 * 
	 * False for short output i.e. 11100101 00011100
	 *                             10101011 01101010
	 *                             10101100 10101110
	 *                             etc
	 */
	boolean longOutput;
	
	public OutputBinaryToFile(String outputFile, String[] outputArray, String[] directiveOutputArray, boolean outType)
	{
		filepath = outputFile;
		binaryOutput = outputArray;
		directiveOutput = directiveOutputArray;
		longOutput = outType;
		
		makeLink();
		writeToFile();
		System.out.println("[OUTPUTBINARYTOFILE] Write to file completed\n");
		closeLink();
	}
	
	public void makeLink()
	{
		file = new File(filepath);
		
		try
		{
			writer = new PrintWriter(file);
		}
		catch(FileNotFoundException e)
		{
			System.err.println("File not found");
		}
	}
	
	public void writeToFile()
	{
		if(longOutput)
		{
			for(String str : binaryOutput)
			{
				writer.print(str + " ");
				System.out.println("Binary string \"" + str + "\" written to file");
			}
			
			for(String str : directiveOutput)
			{
				writer.print(str + " ");
				System.out.println("Directive binary string \"" + str + "\" written to file");
			}
		}
		
		if(!longOutput)
		{
			for(String str : binaryOutput)
			{
				writer.println(str);
				System.out.println("Binary string \"" + str + "\" written to file");
			}
			
			if(directiveOutput != null)
			{
				for(String str : directiveOutput)
				{
					writer.println(str);
					System.out.println("Directive binary string \"" + str + "\" written to file");
				}
			}
		}
	}
	
	public void closeLink()
	{
		writer.close();
	}
	
}
