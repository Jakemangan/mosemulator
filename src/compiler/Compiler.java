package compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.CompilerException;
import util.Utils;

/*
 * Compiler class
 * Compiler object is passed a String[] on creation that holds the contents of the input assembly file
 * Each element in the passed array relates to a single line in the assembly file.
 */

/*
 *  Relative addressing only works on the current instruction i.e. *+x or *-x will + or - x amount
 *  Mem+1 <- that kind of relative addressing doesn't work
 *  
 *  branch labels are defined by _label but addressing these labels in the program only requires the 
 *  branch label i.e. "BNE label" these label names should only be used after branch instructions, but
 *  this is not checked by the assembler TODO: check operand labels only exist after branch instructions
 *  i.e. branch labels can only be used alongside branch instructions
 *  
 *  TODO: for operands like "MEM+1" MEM is a branch label, the whole instruction is relative, and the +1 is being
 *  added onto the memory address being pointed to by MEM. So need to get the assembler to recognise that a label
 *  is being used, add the value onto the memory address and then output the final value
 *  So if MEM was pointed at 0x08 then MEM+1 = 0x09 and output 0x09
 *  
 *  Using labels to define a variable does not work 
 * 
 *  #HI <- doesn't work
 * 
 */

public class Compiler {

	private String outputFilepath = "./binary_output.txt";
	
	
	/*
	 * Array containing the text contents of the assembly file to be compiled.
	 */
	private String[] assemblyText; 
	
	/*
	 * 2D Array containing each assembly line divided into its components
	 */
	private String[][] assemblyLines; 
	
	/*
	 * Array holding AssemblyLine objects that each represent a line in the assembly file.
	 * Therefore the number of AssemblyLine objects in this array is equal to the number of lines in the file.  
	 */
	private AssemblyLine[] lineObjects;
	
	/*
	 * String array holding lines containing directive commands such as "dc.w", these are processed separately from the
	 * rest of the assembly program by DirectiveHandler
	 */
	private String[] directiveLines;
	
	/*
	 * ArrayList containing the names of branches present in the assembly file 
	 */
	private HashMap<Integer, String> branchTable;
	
	/*
	 * InstructionTable object, holds a large array of Instruction objects for lookup purposes
	 */
	private InstructionTable insTable;
	
	/*
	 * Current line of assembly file being accessed.
	 */
	private int currentLine;
	
	/*
	 * Number of lines in the assembly file being parsed.
	 */
	private int asmLineNumber;
	
	/*
	 * String[] array holding the binary representation of the input assembly file
	 * Each element in the array holds two 8-bit binary strings, the first representing the opcode and 
	 * the second representing the operand
	 * 
	 * This array does not include the binary represention of directive instructions
	 */
	private String[] binaryOutput;
	
	/*
	 * String[] array holding the binary representation of any directive lines that 
	 * are present in the assembly program. Each element holds two 8-bit binary strings, the
	 * first representing the memory location where the data is to be stored, the second 
	 * representing the data.
	 * 
	 * Directive instructions such as dc.b, dc.s, dc.v are identified by a '*' prefix 
	 * before binary code
	 */
	private String[] directiveBinaryOutput;
	
	/*
	 * int representing the line on which the end keyword is present
	 */
	private int endKeywordLine;
	
	
	
	
	
	
	public Compiler(String[] asmText) throws CompilerException
	{	
		
		insTable = new InstructionTable();
		//insTable.printTable();
		branchTable = new HashMap<Integer, String>();
		
		assemblyText = asmText; //assemblyText is an array object whereby each element in the array corresponds to a single collective line in the asm file.
		
		
		
		/*
		 * Main compiler method
		 */
		//try
		//{
			System.out.println("\n[COMPILER] BEGIN ASSEMBLY");
		
			checkBeginEnd(); //Check the assembly file contains the "Begin" and "End" keywords
			
			parseDirectiveLines();
			
			checkSyntax();
			
			/*
			 * assemblyLines[] is instantiated here.
			 * Determine if the file contains branch labels and add them to the branchTable if so
			 */
			firstPass(); 
			
			printAssemblyLines(); 
			printBranchTable();
			
			/*
			 * lineObjects[] is instantiated here.
			 * Determine the contents of each line and create AssemblyLine objects for each line then adds them to the array
			 */
			secondPass();  
						  
			lineObjects = removeBeginEnd(lineObjects);
			
			printLineObjects();
			printBranchTable();
			
			checkBranchesExist(); //Ensure all operand labels have a matching branch table entry
			checkDuplicateBranches();
			
			
			/*
			 * By this point, the assembly file has been passed through twice. 
			 * 
			 * On the first pass any branch labels in the file were determined and used to create an entry into the branchTable, 
			 * noting the line number the branch occurred on and the branch label. These are entered as a K:V pair.
			 * 
			 * The second pass iterates through the file again, this time creating an AssemblyLine object for each line in the
			 * file. Each AssemblyLine object is added to the lineObjects array. Each AssemblyLine object in the lineObjects[] 
			 * array corresponds to a single line in the file, with each AssemblyLine object containing the branch label 
			 * (if applicable) the instruction and the operand (if applicable) on that particular line. All of these line elements
			 * (the branch label, instruction and operand) inside the AssemblyLine objects are currently held in their textual form 
			 * and need to be converted into their hexadecimal equivalents.
			 */
			
			determineAddressingModes(); //determine the addressing mode specified by the operand located on each line 
			checkValidOperands();  //check all operands are valid for the instruction being used
			checkForBranchLabels();
			convertLinesToHex();
			printLineObjectsHex();
			
			handleDirectives(); //Syntax check directive lines within program and construct binary output separate from rest of program
			
			printByteCode();
			binaryOutput = constructBinaryOutput();
			printBinaryOutput();
			
			writeBinaryToFile(false);
			
			System.out.println("[COMPILER] END ASSEMBLY - FILE SUCCESSFULLY COMPILED\n");
			
		//}
		//catch(CompilerException e)
		//{
		//	System.err.println(e.getErrorMessage());
		//	System.err.println("Aborting compilation.");
		//}
	}
	
	
	
	/*
	 * 1st pass: assemblyLines array is instantiated, thenevery line in assemblyText[] is read, comments are 
	 * removed and any valid branches are added to branch table.
	 */
	public void firstPass()
	{
		int i = 0;
		asmLineNumber = assemblyText.length;
		assemblyLines = new String[asmLineNumber][];
		
		System.out.println("\nFirst Pass:\n");
		
		for(String line : assemblyText) 
		{
			System.out.println("Current line:" + line + " (" + currentLine + ")");
			currentLine = i+1;
			assemblyLines[i] = divideLine(line); //divide each line into its token components
			assemblyLines[i] = removeComments(assemblyLines[i]); //remove the comments from the current line
			determineBranches(assemblyLines[i]); //determine if the line contains any valid branches
			i++; //increment index
		}
		System.out.println("");
	}
	
	/*
	 * 2nd Pass: Instantiate lineObjects, Iterate through the assemblyText[] array, for everyline, divide into its components,
	 * remove comments, convert each instruction/operand to its hexadecimal equivalent, create an AssemblyLine
	 * object to represent that assembly line, then add the AssemblyLine to lineObjects[]
	 */
	public void secondPass() throws CompilerException
	{
		int i = 0; //reset index to zero, currentLine then also resets.
		lineObjects = new AssemblyLine[asmLineNumber];
		
		System.out.println("\n2nd Pass\n");

		for(String line : assemblyText)
		{
			currentLine = i+1; //currentLine resets and begins at 1.
			assemblyLines[i] = divideLine(line); //divide each line into its token components.
			assemblyLines[i] = removeComments(assemblyLines[i]); //remove the comments from the current line
			
			System.out.println("Checking line " + currentLine);
			
			
			AssemblyLine asmLine = createAssemblyLine(currentLine, assemblyLines[i]); //return AssemblyLine object containing branch/instruction/operand (if applicable)
			
			lineObjects[i] = asmLine; //add AssemblyLine to array
			System.out.println("New asmLine added to lineObjects[]");
			
			i++; //increment index
		}
		
		checkLinesAfterEndKeyword(); //Check no instructions exist after the end keyword
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Checks that the assemblyText array contains the begin and end keywords.
	 * If array does not contain both keywords, error is thrown and compilation halts.
	 * @exception CompilerException 
	 */
	public void checkBeginEnd() throws CompilerException
	{
		String beginLine = assemblyText[0].trim().toUpperCase();
		String endLine = assemblyText[assemblyText.length-1].trim().toUpperCase();
		
		boolean beginFound = false;
		boolean endFound = false;
		
		int lineNumber = 1;
		
		for(String line : assemblyText)
		{
			line = line.toUpperCase();
			
			if(line.contains("BEGIN"))
				beginFound = true;
		
			if(line.contains("END"))
			{
				endFound = true;
				endKeywordLine = lineNumber;
			}
			
			lineNumber++;
				
		}
				
		if(beginFound && endFound)	
		{
			System.out.println("\nFile contains BEGIN and END keywords\n");
		}
		else
		{
			throw new CompilerException("[COMPILER] File does not contains BEGIN and END keywords");
		}
		
//			//original method
//			if(beginLine.equals("BEGIN") && endLine.equals("END"))
//			{
//				System.out.println("\nFile contains BEGIN and END keywords\n");
//			}
//			else
//			{
//				throw new CompilerException("[COMPILER] File does not contains BEGIN and END keywords");
//			}
	}


	public void checkLinesAfterEndKeyword() throws CompilerException
	{
		for(AssemblyLine asmLine : lineObjects)
		{
			String line = asmLine.toStringJustLine();
			
			if(asmLine.getLineNumber() > endKeywordLine)
			{
				throw new CompilerException("Line '" + line + "' cannot exist after END keyword");
			}
		}
	}
	
	
	/*
	 * Divides a line into its constituent elements i.e. "LDA #10 ;load $0A into accumulator" becomes ["LDA", "#10", ";load",... etc
	 */
	public String[] divideLine(String line) 
	{
		
		String[] lineArray = line.trim().split("\\s+");
		for(String str : lineArray)
		{
			str = str.trim();
			//System.out.print(str + " ");
		}
		//System.out.println("");
		return lineArray;
	}
	
	
	/*
	 * Checks for strings containing an underscore ("_") and adds any matching strings into the branchTable
	 */
	public void determineBranches(String[] lineComponents)
	{
		for(String str : lineComponents)
		{
			if(str.contains("_"))
			{
				String branch = str.substring(1);
				branchTable.put(currentLine, branch.toUpperCase());
				//System.out.println("Branch found. Adding " + branch + " to table.");
			}
		}
	}
	
	/*
	 * Removes the comment strings (denoted by a ";") from the array passed.
	 */
	public String[] removeComments(String[] lineComponents)
	{
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(lineComponents));
		ArrayList<String> trimmedList = new ArrayList<String>();
		
		for(String str : list)
		{
			if(str.contains(";"))
				break;
			else
				trimmedList.add(str);
		}
		
		String[] returnList = trimmedList.toArray(new String[trimmedList.size()]);
		
		return returnList;
		
	}
	
	/*
	 * Takes a AssemblyLine[] as a parameter, and converts it to an arraylist. Iterates through each AssemblyLine object in the arraylist
	 * and extracts the contents of the operand stored within it. 
	 * If that operand matches either of the "BEGIN" or "END" keywords, it is removed from the ArrayList.
	 * Converts arraylist back to array and returns the array, with the keywords extracted. 
	 */
	public AssemblyLine[] removeBeginEnd(AssemblyLine[] lineObjects)
	{
		ArrayList<AssemblyLine> temp = new ArrayList<AssemblyLine>(Arrays.asList(lineObjects));
		
		for(AssemblyLine asmLine : lineObjects)
		{
			String operand = asmLine.getOperand().toUpperCase().trim();
			
			if(operand.equals("BEGIN"))
				temp.remove(asmLine);
			
			if(operand.equals("END"))
				temp.remove(asmLine);
		}
		
		AssemblyLine[] returnArray = temp.toArray(new AssemblyLine[temp.size()]);
		System.out.println("'BEGIN' and 'END' keywords removed");
		return returnArray;
	}

	
	/*
	 * Takes String[] containing a single assembly line as input, iterates through the array
	 * and determines if each token in that line is a branch, instruction or operand
	 * returns an AssemblyLine object containing the tokenised line
	 * 
	 * (Inserted token.toUpperCase() to prevent exception on lowercase user progam instructions in GUI)
	 */
	public AssemblyLine createAssemblyLine(int lineNumber, String[] line) 
	{
	
		String branch = "";
		String instruction = "";
		String operand = "";
		
		/*
		 * Create an empty AssemblyLine object, the branch/instruction/operand is filled in by the code below.
		 * This AssemblyLine object is then returned by the method.
		 */
		AssemblyLine asmLine = new AssemblyLine(lineNumber); 
		
		for(String token : line)
		{
			//1. Branch: either ignore and fill in later, or convert to binary string equivalent using location in branchTable
			//2. Instruction: identify instruction, determine addressing mode used, convert to hex equivalent
			//3. Operand: determine if operand exists
			
			token = token.toUpperCase();
			
			boolean tokenMatched = false;
			
			if(token.contains("_") && !tokenMatched) //if token contains underscore, token is a branch label
			{
				//handleBranchLabel()
				tokenMatched = true;
				branch = token;
				System.out.println("Branch found: " + token);
			}
			
			for(String symbol : insTable.getSymbols()) //iterate through instructionTable array of instruction mnemonics
			{
				if(token.matches(symbol) && !tokenMatched) //if token matches one of these mnemonic, token is an instruction
				{
					//handleToken()
					tokenMatched = true;
					instruction = token;
					System.out.println("Instruction found: " + token);
				}
				
			}
			
			if(!tokenMatched) //anything else is an operand
			{
				tokenMatched = true;
				operand = token;
				System.out.println("Operand found: " + token);
			}
				
			
			
		}
		
		System.out.println("");
		
		asmLine.setBranchLabel(branch.toUpperCase());
		asmLine.setOpcode(instruction.toUpperCase());
		asmLine.setOperand(operand.toUpperCase());
		
		return asmLine;
	}
	
	/*
	 * Checks that all operand branch labels used match entries into the branch table.
	 * ALL OPERAND LABELS MUST HAVE A BRANCH TABLE MATCH. 
	 * If an operand label does not match a branchTable entry, there is no equivalent branch label for the instruction
	 * to branch to. 
	 * If this occurs, a CompilerException is generated and compilation is aborted.
	 */
	public void checkBranchesExist() throws CompilerException
	{
		String[] branchLabels = getBranchLabels();
		ArrayList<Integer> errors = new ArrayList<Integer>();
		boolean labelMismatch = false;
		int errLineNumber = -1;
		
		System.out.println("");
		for(AssemblyLine asmLine : lineObjects) //iterate through each asmLine object
		{
			String operand = asmLine.getOperand(); //get the asmLine operand
			boolean localMatch = false;
			
			operand = operand.replace("(", ""); //remove special character from branch label
			operand = operand.replace(")", "");
			operand = operand.replace("X", "");
			operand = operand.replace("Y", "");
			operand = operand.replace(",", "");
			
			if(operand.matches("[a-zA-Z0-9]+")) //if operand is only alphanumerical and contains no symbols i.e is a label
			{
				System.out.println("Operand label to be matched: " + "\"" + operand + "\"");
				
				if(operand.equals("A") && operand.length() == 1)
				{
					System.out.println("Operand is an accumulator operand and does not need to be checked against branch table entries\n");
				}
				else
				{
					for(String label : branchLabels) //for every branch table entry
					{
						System.out.print(operand + " : " + label + " ");
						
						if(operand.equals(label))
						{
							System.out.println("(MATCH)");
							localMatch = true;
						}
						else
						{
							System.out.println("(NO MATCH)");
						}
					}
					System.out.println("Does " + operand + " match any branch table entries? " + localMatch + "\n");
				}
				
				
				
				if(!localMatch) //this particular asmLine operand does not match with any branch table entries
				{
					labelMismatch = true; //at least one error exists, therefore set labelMismatch to true so that the CompilerException is generated
					errLineNumber = asmLine.getLineNumber(); 
					errors.add(asmLine.getLineNumber()); //add the line number the error occurs on to the arraylist
				}
			}
			
			if(operand.equals("A") && operand.length() == 1) //accumulator operand i.e. ADC A
			{
				labelMismatch = false;
			}
		}
		
		
			
		if(labelMismatch)
		{		
			throw new CompilerException("\n[COMPILER BRANCH MATCH ERROR] "
					+ "\nNo branch table match for line " + errLineNumber + "."
					+ "\nPlease ensure all operand labels have a branch label equivalent.");
		}
	}
	
	/*
	 * Checks for duplicated branches within the assembly file. There can only be one of each specified branch label
	 */
	public void checkDuplicateBranches() throws CompilerException
	{
		String[] branchLabels = getBranchLabels();
		
		boolean hasDuplicates = false;
		
		for(int i = 0; i < branchLabels.length; i++)
			for(int j = i+1; j < branchLabels.length; j++)
				if(branchLabels[i].equals(branchLabels[j]))
				{
					throw new CompilerException("Branch table contains duplicate value: " + branchLabels[i] + 
							"\nThere can only be one instance of each branch label.");
				}
		

	}
	
	/*
	 * Determine addressing modes
	 * TODO: indexed indirect may only be applicable to the X register, while indirect indexed may only be applicable to the Y register
	 * if this is so, change the code below to throw errors when indexed indirect is attempted on Y register and when indiredct index is
	 * used with X
	 */
	public void determineAddressingModes()
	{
		System.out.println("Determining addressing modes");
		for(AssemblyLine asmLine : lineObjects) //for every AssemblyLine object in the array
		{
			String operand = asmLine.getOperand();
			operand = operand.toUpperCase();
			String addrMode = "";
			
			if(operand.equals(""))
			{
				operand = "NO OPERAND";
				addrMode = "ADDR_IMP";
			}
			else
			if(operand.contains("A") && operand.length() == 1)
			{
				addrMode = "ADDR_ACC";
			}
			else
			if(operand.contains("#"))
			{
				addrMode = "ADDR_IMMEDIATE";
			}
			else
			if(operand.contains("+") || operand.contains("-"))
			{
				addrMode = "ADDR_RELATIVE";
			}
			else
			if(operand.contains("$") && !operand.contains("X") && !operand.contains("Y") && !operand.contains("(") && !operand.contains(")"))
			{
				addrMode = "ADDR_ABSOLUTE";
			}
			else
			if(!operand.contains("$") && !operand.contains("X") && !operand.contains("Y") && !operand.contains("(") && !operand.contains(")"))
			{
				addrMode = "ADDR_ABSOLUTE";
			}
			else
			if(operand.contains("$") && operand.contains("X") && !operand.contains("Y") && !operand.contains("(") && !operand.contains(")"))
			{
				addrMode = "ADDR_ABSOLUTE_X";
			}
			else
			if(operand.contains("$") && !operand.contains("X") && operand.contains("Y") && !operand.contains("(") && !operand.contains(")"))
			{
				addrMode = "ADDR_ABSOLUTE_Y";
			}
			else
			if(operand.contains("(") && operand.contains(")") && !operand.contains("X") && !operand.contains("Y"))
			{
				addrMode = "ADDR_INDIRECT";
			}
			else
			if(operand.contains("(") && operand.contains(")"))
			{
				if(operand.contains("X"))
				{
					int indexOfClosingBracket = operand.indexOf(")");
					int indexOfX = operand.indexOf("X");
					
					if(indexOfClosingBracket > indexOfX)
					{
						addrMode = "ADDR_INDEXED_INDIRECT";
					}
					else
					if(indexOfClosingBracket < indexOfX)
					{
						addrMode = "ADDR_INDIRECT_INDEXED";
					}
				}
				else
				if(operand.contains("Y"))
				{
					int indexOfClosingBracket = operand.indexOf(")");
					int indexOfY = operand.indexOf("Y");
					
					if(indexOfClosingBracket > indexOfY)
					{
						addrMode = "ADDR_INDEXED_INDIRECT";
					}
					else
					if(indexOfClosingBracket < indexOfY)
					{
						addrMode = "ADDR_INDIRECT_INDEXED";
					}
				}
					
			}
		
			System.out.println(operand + " (" + addrMode + ")");
			asmLine.setAddressingMode(addrMode);
		}
	}
	
	/*
	 * Check each operand addressing mode against the instruction table to ensure each operand is valid for the specified instruction
	 * Each instruction only supports certain addressing modes, if the specified addressing mode is not supported by the instruction
	 * then the instruction will fail. 
	 */
	public void checkValidOperands() throws CompilerException
	{
		System.out.println("\nCheck operands are valid for instructions:");
		
		String[] addressingModes = insTable.getAddressing();
		String[] symbols = insTable.getSymbols();
		
		for(AssemblyLine asmLine : lineObjects)
		{
			String opcode = asmLine.getOpcode();
			System.out.println("OPCODE IS OPCODE: " + opcode);
			int addressingMode = asmLine.getAddressingMode();
			
			ArrayList<String> validAddressingModes = new ArrayList<String>(); //AL to hold the string representations of valid addressing modes for an instruction
			ArrayList<Integer> validAddressingModesInt = new ArrayList<Integer>(); //AL to hold the integer representations of valid addressing modes for an instruction, same as above just int form
			
			for(int i = 0; i < symbols.length; i++)
			{
				if(opcode.equals(symbols[i])) //if there's a match between the opcode in asmLine and a symbol in the symbols[] array
				{
					validAddressingModes.add(addressingModes[i]);
					String mode = addressingModes[i];
					int modeNumber = convertModeToNumber(mode);
					validAddressingModesInt.add(modeNumber);
				}
			}
			
			
			/*
			 * Print to console
			 */
			System.out.println("\n" + opcode);
			System.out.print("Valid addressing modes: ");
			for(String str : validAddressingModes)
			{
				System.out.print(str + " ");
			}
			
			System.out.println("");
			for(Integer i : validAddressingModesInt)
			{
				System.out.print(i.toString() + " ");
			}
			System.out.println("");
			/*
			 * End print
			 */
			
			System.out.println("Addressing Mode: " + addressingMode);
			
			boolean addressingModeValid = false;
			for(Integer i : validAddressingModesInt)
			{
				if(addressingMode == i)
				{
					addressingModeValid = true;
				}
			}
			
			if(addressingModeValid)
			{
				System.out.println("Addressing mode " + addressingMode + " (" + Utils.convertAddrModeToString(addressingMode) 
									+ ") is valid for instruction " + opcode);
			}
			else
			if(!addressingModeValid)
			{
				throw new CompilerException("Addressing mode " + addressingMode + " (" + Utils.convertAddrModeToString(addressingMode) 
				+ ") is not valid for instruction " + opcode);
			}
			
		}
	}
	
	public void parseDirectiveLines()
	{
		ArrayList<String> directiveLinesAL = new ArrayList<String>();
		ArrayList<String> assemblyTextAL = new ArrayList<String>(Arrays.asList(assemblyText));
		
		for(int i = 0; i < assemblyTextAL.size(); i++)
		{
			String line = assemblyTextAL.get(i);
			System.out.println("Parsing directives from line: " + line);
			
			if(line.contains("dc.b") ||  //Lines contains directive to define byte constant 
			   line.contains("dc.v") ||  //Lines contains directive to define byte array constant
			   line.contains("dc.s"))    //Lines contains directive to define string consant
			{
				System.out.println("Directive found in line:" + line);
				
				if(line.contains(";"))
				{
					int commentStart = line.indexOf(';');
					line = line.substring(0, commentStart);
				}
				
				System.out.println("Line after comment removal: " + line);
				
				directiveLinesAL.add(line);
				assemblyTextAL.remove(i);
				i--;
			}
		}
		
		assemblyText = assemblyTextAL.toArray(new String[assemblyTextAL.size()]);
		directiveLines = directiveLinesAL.toArray(new String[directiveLinesAL.size()]);
		
		System.out.println("\nRemoved the following lines from assemblyText[] and added into directoryLines[]:");
		for(String line : directiveLines)
		{
			System.out.println(line);
		}
		
		System.out.println("\nassemblyText[] contents:");
		for(String line : assemblyText)
		{
			System.out.println(line);
		}
		System.out.println("");
		
	}
	
	public void handleDirectives() throws CompilerException
	{
		System.out.println("HANDLE DIRECTIVES START");
		
		DirectiveHandler dh = new DirectiveHandler(directiveLines);
		dh.constructDirectiveOutput();
		directiveBinaryOutput = dh.getDirectiveBinaryOutput();
		
		System.out.println("HANDLE DIRECTIVES END\n");
	}
	
	/*
	 * Utility method for checkValidOperands() method
	 * Takes an addressing mode as a string input and returns a number associated with that addressing mode
	 * List of addressing mode numbers is located in AssemblyLine class
	 */
	private int convertModeToNumber(String mode)
	{
		int returnNumber = 0;
		
		if(mode.equals("implied"))
		{
			returnNumber = 1;
		}
		
		if(mode.equals("accumulator"))
		{
			returnNumber = 2;
		}
		
		if(mode.equals("immediate"))
		{
			returnNumber = 3;
		}
		
		if(mode.equals("relative"))
		{
			returnNumber = 4;
		}
		
		if(mode.equals("absolute"))
		{
			returnNumber = 5;
		}
		
		if(mode.equals("absolute,X"))
		{
			returnNumber = 6;
		}
		
		if(mode.equals("absolute,Y"))
		{
			returnNumber = 7;
		}
		
		if(mode.equals("indirect"))
		{
			returnNumber = 8;
		}
		
		if(mode.equals("(indirect,X)"))
		{
			returnNumber = 9;
		}
		
		if(mode.equals("(indirect),Y"))
		{
			returnNumber = 10;
		}
		
		return returnNumber;
	}
	
	
	/*
	 * Takes the contents of each AssemblyLine object and uses the instruction, operand and addressing mode to determine the hexadecimal
	 * equivalent of that line. Each line will return 2 hexadecimal pairs, the first representing the opcode + addressing mode and the 
	 * second representing the operand. 
	 * 
	 * Instructions will never exceed two bytes in size due to the upper limit of 8-bit memory addressing within the simulator. Memory
	 * addresses higher than 0xFF are not supported, and thus all operands will fit within an 8-bit pattern. 
	 */
	public void convertLinesToHex()
	{
		HexHandler hh = new HexHandler(branchTable);
		
		for(AssemblyLine asmLine : lineObjects)
		{
			System.out.println("");
			hh.setBranchLabel(asmLine.getBranchLabel());
			hh.setInstruction(asmLine.getOpcode());
			hh.setOperand(asmLine.getOperand());
			hh.setAddressingMode(asmLine.getAddressingMode());
			hh.printContents();
			hh.convertInstructionToHex();
			hh.convertOperandToHex();
			
			String opcodeHex = hh.getOpcodeHex();
			String operandHex = hh.getOperandHex();
			
			if(opcodeHex.length() == 1)
			{
				opcodeHex = "0".concat(opcodeHex);
			}
			
			if(operandHex.length() == 1)
			{
				operandHex = "0".concat(operandHex);
			}
			
			asmLine.setOpcodeHEX(opcodeHex);
			asmLine.setOperandHEX(operandHex);
		}
	}
	
	/*
	 * Checks each AssemblyLine object for valid syntax
	 * If an AssemblyLine object does not contain valid syntax a CompilerException is thrown.
	 * 
	 * Syntax rules:
	 *    All instructions must be three letters long and match an instruction in the instruction table.
	 * 
	 * 	  Branch labels cannot contain any characters aside from alphanumeric
	 *  
	 *    Operands cannot contain symbols outside of "( ) $ # , + -"
	 *    Numerical operands must contain $ or # if absolute
	 *    Opening parentheses in operands must be closed. 
	 *    Operands can only contain one pair of parentheses
	 *    $ and # must be followed by numeric characters in operands.
	 *    Commas must exist between a value and a value OR a parenthesis and a value i.e. (xxx,yyy) or (xxx),yyy
	 *  
	 * 
	 */
	public void checkSyntax() throws CompilerException
	{
		System.out.println("[COMPILER] START SYNTAX CHECK");
		
		
		ArrayList<Integer> errors = new ArrayList<Integer>();
		ArrayList<String> tokenisedAsmFile = new ArrayList<String>();
		
		/*
		 * Take each line in the assembly file, tokenise it, remove comments, remove blank tokens and add
		 * into tokenisedAsmFile AL for syntax checking.
		 * 
		 * Syntax checking will check one token at a time.
		 */
		for(String line : assemblyText)
		{
			String[] splitLine = line.split("\\s+");
			splitLine = removeComments(splitLine); 
			
			for(int i = 0; i < splitLine.length; i++)
			{
				if(splitLine[i].length() > 0)
				{
					tokenisedAsmFile.add(splitLine[i]);
				}
			}
		}
		
		//Remove BEGIN keyword from token list
		if(tokenisedAsmFile.get(0).toUpperCase().equals("BEGIN"))
		{
			tokenisedAsmFile.remove(0); //first token in list is BEGIN
			System.out.println("BEGIN keyword removed");
		}
		
		//Remove END keyword from token list
		if(tokenisedAsmFile.get((tokenisedAsmFile.size()-1)).toUpperCase().equals("END"))
		{
			tokenisedAsmFile.remove(tokenisedAsmFile.size()-1); //last token in list is END
			System.out.println("END keyword removed");
		}
		
		System.out.println("");
		
		SyntaxChecker sc = new SyntaxChecker();
		boolean tokenHasError = false;
		
		int currentToken = 0;
		for(String token : tokenisedAsmFile)
		{
			System.out.println(token);
			tokenHasError = sc.checkTokenSyntax(currentToken, token);
			
			if(tokenHasError)
			{
				String errorMsg = sc.getErrorMessage();
				throw new CompilerException("[COMPILER EXCEPTION] Syntax Error: " + errorMsg);
			}
			
			currentToken++;
		}
		
		System.out.println("[COMPILER] END SYNTAX CHECK\n");
		
	}
	
	public void checkForBranchLabels() throws CompilerException
	{
		String[] branchLabels = getBranchLabels();
		
		for(AssemblyLine asmLine : lineObjects)
		{
			String operand = asmLine.getOperand();
			
			for(String label : branchLabels)
			{
				if(operand.contains(label))
				{
					throw new CompilerException("[COMPILER EXCEPTION] Branch labels are not yet supported by the assembler. "
							+ "\nBranch label \"" + label + "\" on line " + asmLine.getLineNumber() + " operand is invalid and must be removed.");
				}
			}
		}
	}
	
	public void printByteCode()
	{
		System.out.println("[COMPILER] Print byte code:");
		System.out.println("EF EF"); //BEGIN keyword bytecode 
		
		for(AssemblyLine asmLine : lineObjects)
		{
			String opcodeHex = asmLine.getOpcodeHEX();
			String operandHex = asmLine.getOperandHEX();
			
			System.out.println(opcodeHex + " " + operandHex);
		}
		
		System.out.println("FF FF"); //END keyword bytecode
	}
	
	/*
	 * Takes the hexadecimal representation of the asm file and converts each byte of data into its binary form.
	 * Each line in the asm file is 2 bytes.
	 * The two bytes per line are couple together and added into a single element in the arraylist
	 * 
	 * Binary representations of the BEGIN and END keyword (0xEF and 0xFF hex respectively) are added onto
	 * the beginning and end of the binary output. These remain fixed no matter the contents of the assembly
	 * file.
	 */
	public String[] constructBinaryOutput()
	{
		System.out.println("");
		
		ArrayList<String> binaryOut = new ArrayList<String>();
		
		String beginBinary = Utils.hexToBinary("EF"); //Hexadecimal representation of BEGIN keyword is 0xEF
		String endBinary = Utils.hexToBinary("FF"); //Hexadecimal representation of END keyword is 0xFF
		
		String twoByteString = beginBinary.concat(" " + beginBinary);
		
		binaryOut.add(twoByteString);
		
		for(AssemblyLine asmLine : lineObjects)
		{
			String opcodeHex = asmLine.getOpcodeHEX();
			String operandHex = asmLine.getOperandHEX();
			
			System.out.println("Hex opcode/operand: " + opcodeHex + " " + operandHex);
			
			String opcodeBinary = Utils.hexToBinary(opcodeHex);
			String operandBinary = Utils.hexToBinary(operandHex);
			
			System.out.println("Hex opcode " + asmLine.getOpcodeHEX() + " converted into binary " + opcodeBinary);
			System.out.println("Hex operand " + asmLine.getOperandHEX() + " converted into binary " + operandBinary);
			
			twoByteString = opcodeBinary.concat(" " + operandBinary);
			
			System.out.println("2 Byte instruction: " + twoByteString);
			
			binaryOut.add(twoByteString);
			
			System.out.println("");
		}
		
		twoByteString = endBinary.concat(" " + endBinary);
		
		binaryOut.add(twoByteString);
		
		String[] binaryOutputArray = binaryOut.toArray(new String[binaryOut.size()]);
		
		return binaryOutputArray;
	}
	
	public void writeBinaryToFile(boolean outputType)
	{
		System.out.println("");
		OutputBinaryToFile outputBinary = new OutputBinaryToFile(outputFilepath, binaryOutput, directiveBinaryOutput, outputType);
	}
	
	public void setOutputFile(String outputFile)
	{
		this.outputFilepath = outputFile;
	}
	
	
	///////////////////
	///PRINT METHODS///
	///////////////////
	
	/*
	 * Prints contents of assemblyLines[]
	 */
	public void printAssemblyLines()
	{
		System.out.println("Print Assembly Lines");
		System.out.println("AssemblyText array size: " + assemblyText.length);
		System.out.println("AssemblyLines array size: " + assemblyLines.length);
		for(int i = 0; i < assemblyLines.length; i++)
		{
			System.out.print("Line " + (i+1) + ": ");
			for(int j = 0; j < assemblyLines[i].length; j++)
			{
				System.out.print(assemblyLines[i][j] + " ");
			}
			System.out.println("");
		}
		System.out.println("");
	}
	
	/*
	 * Prints the contents of the branch table (K,V) = lineNumber : branchName
	 */
	public void printBranchTable()
	{
		System.out.println("");
		System.out.println("Branch Table Entries: ");
		
		Iterator<?> it = branchTable.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			System.out.println("Line " + pair.getKey() + ": " + pair.getValue());
		}
	}
	
	/*
	 * Prints the contents of the lineObjects[] array by using each AssemblyLine object's toString method
	 */
	public void printLineObjects()
	{
		System.out.println("\nContents of lineObjects[]: ");
		for(AssemblyLine asmLine : lineObjects)
		{
			System.out.println(asmLine.toString());	
		}
	}
	
	public void printLineObjectsHex()
	{
		System.out.println("\nContents of lineObjects[] (hex included): ");
		for(AssemblyLine asmLine : lineObjects)
		{
			System.out.println(asmLine.toStringHex());	
		}
	}
	
	
	public String[] getBranchLabels()
	{
		ArrayList<String> branchLabels = new ArrayList<String>();
		
		Iterator<?> it = branchTable.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			branchLabels.add(pair.getValue().toString());
		}
		
		String[] branches = branchLabels.toArray(new String[branchLabels.size()]);
		
		return branches;
	}
	
	public void printBinaryOutput()
	{
		System.out.println("Binary output:");
		for(String str : binaryOutput)
		{
			System.out.println(str);
		}
	}
		
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

































