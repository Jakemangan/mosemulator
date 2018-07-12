package compiler;

import exceptions.CompilerException;

public class CompilerDriver {
	
	//TODO: Converting operations and operands to bytes
	//TODO: Branching, how to branch and return to branch
	
	private String inputFile = "";
	private String outputFile = "";
	
	private Compiler c;

	public static void main(String[] args)
	{

		String inputFile = "./directivetest.txt";
		String outputFile = "./BinaryOutput.txt";
		
		ReadAssemblyFile raf = new ReadAssemblyFile(inputFile);
		
		try
		{
			Compiler c = new Compiler(raf.getAssemblyText());
		}
		catch(CompilerException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		
	
	}
	
	public CompilerDriver(String inputFile, String outputFile)
	{
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		ReadAssemblyFile raf = new ReadAssemblyFile(inputFile);
		
		//c = new Compiler(outputFile, raf.getAssemblyText());
	}
	
	public Compiler getCompiler()
	{
		return c;
	}
}
