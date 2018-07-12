package compiler;

/*
 * Class representing the contents of a single line in the input assembly file.
 * 
 * One AssemblyLine object is created for each line in the file.
 * 
 * Each AssemblyLine object holds the contents of that particular assembly line 
 * i.e. line number, label (if any), opcode, operand (if any)
 * 
 * AssemblyLine class allows conversion of each line into a representation that can be 
 * used to create the final binary output.
 * 
 * Branch, instruction and operand are first placed into the object in symbolic form i.e. "loop", "ADC" and "$10"
 * Later, each form is overwritten with its hexadecimal counterpart
 */

public class AssemblyLine {

	/*
	 * List of constants representing the different addressing modes
	 */
	public static final int ADDR_IMP = 1;
	public static final int ADDR_ACC = 2;
	public static final int ADDR_IMMEDIATE = 3;
	public static final int ADDR_RELATIVE = 4;
	public static final int ADDR_ABSOLUTE = 5;
	public static final int ADDR_ABSOLUTE_X = 6;
	public static final int ADDR_ABSOLUTE_Y = 7;
	public static final int ADDR_INDIRECT = 8;
	public static final int ADDR_INDEXED_INDIRECT = 9; //($40,X)
	public static final int ADDR_INDIRECT_INDEXED = 10; //($40),Y 
	
	/*
	 * Line number in assembly file
	 */
	private int lineNumber;
	
	/*
	 * Addressing mode used within this particular line of the assembly file. Represented by integer.
	 */
	private int addressingMode;
	
	/*
	 * Hexadecimal representation of opcode
	 */
	private String opcode;
	
	/*
	 * Hexadecimal representation of instruction operand
	 */
	private String operand;
	
	/*
	 * Hexadecimal representation of branch label
	 */
	private String branchLabel;
	
	/*
	 * Hexadecimal representation of opcode in HEXADECIMAL
	 */
	private String opcodeHEX;
	
	/*
	 * Hexadecimal representation of instruction operand in HEXADECIMAL
	 */
	private String operandHEX;
	
	/*
	 * Hexadecimal representation of branch label in HEXADECIMAL
	 */
	private String branchLabelHEX;
	
	
	
	
	
	
	
	
	public AssemblyLine(int lineNum)
	{
		lineNumber = lineNum;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Getters.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	public int getAddressingMode() {
		return addressingMode;
	}

	public String getOpcode() {
		return opcode;
	}
	
	public String getOperand() {
		return operand;
	}
	
	public String getBranchLabel() {
		return branchLabel;
	}
	
	public String getOpcodeHEX() {
		return opcodeHEX;
	}

	public String getOperandHEX() {
		return operandHEX;
	}
	
	public String getBranchLabelHEX() {
		return branchLabelHEX;
	}
	
	/*
	 * Setters.
	 */
	public void setBranchLabel(String bl) {
		branchLabel = bl;
	}
	
	public void setOpcode(String opcode) {
		this.opcode = opcode; 
	}
	
	public void setOperand(String operand) {
		this.operand = operand;
	}
	
	public void setOpcodeHEX(String opcodeHEX) {
		this.opcodeHEX = opcodeHEX;
	}

	public void setOperandHEX(String operandHEX) {
		this.operandHEX = operandHEX;
	}
	
	public void setBranchLabelHEX(String branchLabelHEX) {
		this.branchLabelHEX = branchLabelHEX;
	}
	
	public void setAddressingMode(int aMode) {
		addressingMode = aMode;
	}
	
	public void setAddressingMode(String addrMode)
	{
		int returnNumber = 0;
		String addressingMode = addrMode;
		
		switch(addressingMode) {
			case "ADDR_IMP":
				returnNumber = ADDR_IMP;
				break;
			case "ADDR_ACC":
				returnNumber = ADDR_ACC;
				break;
			case "ADDR_IMMEDIATE":
				returnNumber = ADDR_IMMEDIATE;
				break;
			case "ADDR_RELATIVE":
				returnNumber = ADDR_RELATIVE;
				break;
			case "ADDR_ABSOLUTE":
				returnNumber = ADDR_ABSOLUTE;
				break;
			case "ADDR_ABSOLUTE_X":
				returnNumber = ADDR_ABSOLUTE_X;
				break;
			case "ADDR_ABSOLUTE_Y":
				returnNumber = ADDR_ABSOLUTE_Y;
				break;
			case "ADDR_INDIRECT":
				returnNumber = ADDR_INDIRECT;
				break;
			case "ADDR_INDEXED_INDIRECT":
				returnNumber = ADDR_INDEXED_INDIRECT;
				break;
			case "ADDR_INDIRECT_INDEXED":
				returnNumber = ADDR_INDIRECT_INDEXED;
				break;
		}
		//System.out.println("Set addressing mode to: " + returnNumber);
		setAddressingMode(returnNumber);
	}
	
	
	
	
	/*
	 * @override
	 * toString() override
	 */
	public String toString()
	{
		String s = lineNumber + " " + branchLabel + " " + opcode + " " + operand + " " + addressingMode;
		return s;
	}
	
	public String toStringJustLine() 
	{
		 String s = branchLabel + " " + opcode + " " + operand;
		 return s;
	}
	
	public String toStringHex()
	{
		String s = branchLabel + " " + opcode + " " + operand + "\nHEX: " + opcodeHEX + " " + operandHEX + "\n";
		return s;
	}
	
	public String toStringLong()
	{
		String s = "Line Number: " + lineNumber + " \n" +
					"Branch Label: " + branchLabel + "\n" +
					"Opcode: " + opcode + "\n" +
					"Operand: " + operand + "\n";
		return s;
	}

}























