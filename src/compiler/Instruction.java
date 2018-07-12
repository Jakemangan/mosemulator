package compiler;

/*
 * Instruction class defines an instruction object. 
 * Instructions have 5 parameters; opcode, symbol, addressing mode, bytes and cycles.
 * More than one instruction can exist per operation due to multiple addressing types.
 * I.e. ADC instruction has 4 addressing types, therefore there are 4 different ADC instructions.
 * There are 93 unique instructions and 56 operations total.
 */


public class Instruction {

	/*
	 * Hexadecimal opcode used to identify instruction, unique to each.
	 */
	private String opcode; 
	
	/*
	 * String mnemonic used to identify instruction in assembly code i.e. ADC, JMP, NOP.
	 */
	private String symbol;
	
	/*
	 * Addressing type used for the instruction.
	 */
	private String addressing;
	
	/*
	 * Number of bytes the instruction requires in memory, includes instruction operands.
	 */
	private String bytes;
	
	/*
	 * Number of clock cycles required for instruction execution.
	 * (*) = add 1 to cycles if page boundery is crossed
	 * (**) = add 1 to cycles if branch occurs on same page / add 2 to cycles if branch occurs to different page
	 */
	private String cycles;
	
	public Instruction(String opcode, String symbol, String addressing, String bytes, String cycles)
	{
		this.opcode = opcode;
		this.symbol = symbol;
		this.addressing = addressing;
		this.bytes = bytes;
		this.cycles = cycles;
	}

	public String getOpcode() {
		return opcode;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getAddressing() {
		return addressing;
	}

	public String getBytes() {
		return bytes;
	}

	public String getCycles() {
		return cycles;
	}

	public String toString()
	{
		String s = opcode + " " + symbol + " " + addressing + " " + bytes + " " + cycles;
		return s;
	}
	
}
