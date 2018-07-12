package compiler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.Utils;

public class HexHandler {

	/*
	 * HashMap containing key:value pairs of lineNumber:branchLabel.
	 */
	private HashMap<Integer, String> branchTable;
	
	/*
	 * Line number each branch label is held on.
	 */
	private int[] branchLinenumbers; 
	
	/*
	 * Holds each branch label found in branch table.
	 */
	private String[] branchLabels; 
	
	/*
	 * Hexadecimal representation of the memory address each branch is pointing at.
	 */
	private String[] branchLocations; 
	
	private InstructionTable insTable;
	
	private String branchLabel;
	private String instruction;
	private String operand;
	private int addressingMode;
	
	private int branchTableSize;
	
	private String opcodeHex;
	private String operandHex;
	
	public HexHandler(HashMap<Integer, String> branchTable)
	{
		System.out.println("\n---HEXHANDLER---");
		insTable = new InstructionTable();
		
		this.branchTable = branchTable;
		branchTableSize = branchTable.size();
		
		//Debug
		System.out.println("Branch table entrySet: " + branchTable.entrySet());
		System.out.println("Branch table keySet: " + branchTable.keySet());
		System.out.println("Branch table values: " + branchTable.values());
		
		populateBranchArrays();
		determineBranchAddresses();
	}

	
	/*
	 * Populates the two arrays branchLocations[] and branchLabels[] with the key:value pairs held in the branchTable hashmap
	 * Therefore the two values held in branchLabel[i] and branchLocation[i] correspond to a single key:value pair
	 * i.e. branchLocation[0] = 5, branchLabel[0] = loop
	 * 
	 *  Key = line numbers
	 *  Value = branch label
	 */
	private void populateBranchArrays()
	{
		branchLinenumbers = new int[branchTableSize];
		branchLabels = new String[branchTableSize];
		
		int index = 0;
		Iterator<?> it = branchTable.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			branchLabels[index] = pair.getValue().toString();
			branchLinenumbers[index] = Integer.parseInt(pair.getKey().toString());
			index++;
		}
	}
	
	/*
	 * Determines the actual addresses each branch label is pointing at.
	 * Each opcode:operand pair in the instruction file takes up 2 bytes of memory. 1 byte for opcode and 1 byte for operand
	 * Therefore each line in the assembly file requires 2 bytes of memory space.
	 * 
	 * Branch labels point at the opcode of each line, therefore the memory address each branch label is pointing at
	 * can be calculated by (branchLinenumber*2)-1
	 * *2 because each line requires 2 bytes, therefore the line number is half of the memory address for that line
	 * -1 because the branch label points at the opcode, not the operand
	 * 
	 * This method calculates the hexadecimal representation of the memory address each branch label is pointing at. 
	 */
	public void determineBranchAddresses()
	{
		System.out.println("");
		branchLocations = new String[branchTableSize];
		
		for(int i = 0; i < branchTableSize; i++) 
		{
			int lineNumber = branchLinenumbers[i];
			int decimalMemoryAddress = (lineNumber * 2) - 1;
			String hexMemoryAddress = Utils.DecToHex(decimalMemoryAddress);
			
			branchLocations[i] = hexMemoryAddress;
			
			System.out.println("Branch label " + branchLabels[i] + " is pointing at memory address 0x" + branchLocations[i]);
		}
		System.out.println("");
	}
	
	
	
	public void convertInstructionToHex()
	{
		String[] symbolAddress = insTable.getSymbolAddress(); //Array holding string pairs of instruction symbol and addressing mode
		String[] opcodes = insTable.getOpcodes();
		
		String addressingModeStr = convertModeToString(addressingMode); //convert addressingmode int to string representation
		String instructionAddressPair = instruction.concat(" " + addressingModeStr); //concat instruction mnemonic with addressing mode string
		
		System.out.println("InstructionAddress pair: " + instructionAddressPair);
		
		for(int i = 0; i < symbolAddress.length; i++)
		{
			if(instructionAddressPair.equals(symbolAddress[i]))
			{
				opcodeHex = opcodes[i];
				System.out.println(instructionAddressPair + " is equivalent to opcode: " + opcodeHex);
			}
		}
	}
	
	public void convertOperandToHex()
	{

		
		if(addressingMode == 1) //implicit addressing
		{
			operandHex = "00"; //implicit addressing has no operand, therefore 0x00 is used instead
			System.out.println("Implicit addressing, " + operandHex + " used as operand");
		}
		
		if(addressingMode == 2) //accumulator addressing
		{
			operandHex = "00"; //accumulator addressing uses A as operand, but this information is contained within the operand, therefore 0x00 is used instead
			System.out.println("Accumulator addressing, " + operandHex + " used as operand");
		}
		
		if(addressingMode == 3) //immediate addressing
		{
			System.out.println("Operand: " + operand);
			System.out.println("Immediate addressing");
			String operandEdit = operand.replace("#", ""); //remove # from string
			operandHex = operandEdit; //once # is removed only the operand remains i.e. #10 -> 10 which is 0xA
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 4) //relative addressing
		{
			System.out.println("Operand: " + operand);
			System.out.println("Relative addressing");
			
			boolean positiveAddressing = false; //true for positive, false for negative
			
			if(operand.contains("+"))
			{
				positiveAddressing = true;
				System.out.println("Positive operand");
			}
			
			if(operand.contains("-"))
			{
				positiveAddressing = false;
				System.out.println("Negative operand");
			}
			
			
			
			String operandEdit = operand;
			operandEdit = operandEdit.replace("*", "");
			operandEdit = operandEdit.replace("+", "");
			operandEdit = operandEdit.replace("-", "");
			
			if(positiveAddressing)
			{
				operandHex = operandEdit;
			}
			else
			if(!positiveAddressing) //negative addressing
			{
				int decimalOperand = Utils.HexToDec(operandEdit); //parse int from operand after special chars have been removed
				decimalOperand = -decimalOperand; //negate int 
				String byteString = Integer.toBinaryString(decimalOperand); //convert int to 2's complement form since its negative
				byteString = byteString.substring(byteString.length()-8, byteString.length()); //trim excess bits
				int decimal = Integer.parseInt(byteString, 2); //parse int from the byte
				operandEdit = Integer.toString(decimal, 16); //convert 2's complement int to hexadecimal
				operandEdit = operandEdit.toUpperCase();
				
				operandHex = operandEdit;
				
				
			}
			
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 5) //absolute addressing
		{
			System.out.println("Operand: " + operand);
			System.out.println("Absolute addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 6) //absolute,X
		{
			System.out.println("Operand: " + operand);
			System.out.println("Absolute,X addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			operandEdit = operandEdit.replace(",", "");
			operandEdit = operandEdit.replace("X", "");
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 7) //absolute,Y
		{
			System.out.println("Operand: " + operand);
			System.out.println("Absolute,Y addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			operandEdit = operandEdit.replace(",", "");
			operandEdit = operandEdit.replace("Y", "");
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 8) //indirect
		{
			System.out.println("Operand: " + operand);
			System.out.println("Indirect addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			operandEdit = operandEdit.replace("(", "");
			operandEdit = operandEdit.replace(")", "");
			
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 9) //indexed indirect
		{
			System.out.println("Operand: " + operand);
			System.out.println("Indexed indirect addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			operandEdit = operandEdit.replace("(", "");
			operandEdit = operandEdit.replace(")", "");
			operandEdit = operandEdit.replace(",", "");
			operandEdit = operandEdit.replace("X", "");
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
		
		if(addressingMode == 10) //indirect indexed
		{
			System.out.println("Operand: " + operand);
			System.out.println("Indirect indexed addressing");
			String operandEdit = operand;
			operandEdit = operandEdit.replace("$", "");
			operandEdit = operandEdit.replace("(", "");
			operandEdit = operandEdit.replace(")", "");
			operandEdit = operandEdit.replace(",", "");
			operandEdit = operandEdit.replace("Y", "");
			
			for(int i = 0; i < branchTableSize; i++) //iterate through branch table
			{
				if(operandEdit.equals(branchLabels[i])) //if operand matches branch table entry, operand is a branch label
				{
					operandEdit = branchLocations[i]; //replace operand with memory address the branch label points to
				}
			}
			
			operandHex = operandEdit;
			System.out.println("Resultant hex operand: " + operandHex);
		}
	}
	
	
	/*
	 * Takes integer input and returns corresponding addressing mode
	 */
	public String convertModeToString(int mode)
	{
		String returnMode = "";
		
		if(mode == 1)
		{
			returnMode = "implied";
		}
		
		if(mode == 2)
		{
			returnMode = "accumulator";
		}
		
		if(mode == 3)
		{
			returnMode = "immediate";
		}
		
		if(mode == 4)
		{
			returnMode = "relative";
		}
		
		if(mode == 5)
		{
			returnMode = "absolute";
		}
		
		if(mode == 6)
		{
			returnMode = "absolute,X";
		}
		
		if(mode == 7)
		{
			returnMode = "absolute,Y";
		}
		
		if(mode == 8)
		{
			returnMode = "indirect";
		}
		
		if(mode == 9)
		{
			returnMode = "(indirect,X)";
		}
		
		if(mode == 10)
		{
			returnMode = "(indirect),Y";
		}
		
		return returnMode;
	}
	
	
	
	/*
	 * 
	 * 
	 * Print Methods 
	 * 
	 * 
	 */
	
	public void printContents()
	{
		String s = getBranchLabel() + " " + getInstruction() + " " + getOperand() + " " + getAddressingMode();
		
		System.out.println("Hexhandler current contents:" + "\n" + s);
		
	}
	
	
	
	/*
	 *
	 * 
	 * GETTERS / SETTERS
	 * 
	 * 
	 */
	
	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getOperand() {
		return operand;
	}

	public void setOperand(String operand) {
		this.operand = operand;
	}

	public int getAddressingMode() {
		return addressingMode;
	}

	public void setAddressingMode(int addressingMode) {
		this.addressingMode = addressingMode;
	}

	public String getBranchLabel() {
		return branchLabel;
	}

	public void setBranchLabel(String branchLabel) {
		this.branchLabel = branchLabel;
	}
	
	public String getOpcodeHex()
	{
		return opcodeHex;
	}
	
	public String getOperandHex()
	{
		return operandHex;
	}
	
}
