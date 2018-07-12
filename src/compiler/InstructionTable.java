package compiler;


public class InstructionTable {
	
	Instruction[] instructions;

	String[] addressing = {
			"immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "accumulator", "absolute", "absolute,X", "relative", "relative", "relative", "absolute", "relative", "relative", "relative", "implied", "relative", "relative", "implied", "implied", "implied", "implied", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "immediate", "absolute", "immediate", "absolute", "absolute", "absolute,X", "implied", "implied", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "absolute", "absolute,X", "implied", "implied", "absolute", "indirect", "absolute", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "immediate", "absolute", "absolute,Y", "immediate", "absolute", "absolute,X", "accumulator", "absolute", "absolute,X", "implied", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "implied", "implied", "implied", "implied", "accumulator", "absolute", "absolute,X", "accumulator", "absolute", "absolute,X", "implied", "implied", "immediate", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "implied", "implied", "implied", "absolute", "absolute,X", "absolute,Y", "(indirect,X)", "(indirect),Y", "absolute", "absolute", "implied", "implied", "implied", "implied", "implied", "implied" 
	};
	
	String[] symbols = {
			"ADC", "ADC", "ADC", "ADC", "ADC", "ADC", "AND", "AND", "AND", "AND", "AND", "AND", "ASL", "ASL", "ASL", "BCC", "BCS", "BEQ", "BIT", "BMI", "BNE", "BPL", "BRK", "BVC", "BVS", "CLC", "CLD", "CLI", "CLV", "CMP", "CMP", "CMP", "CMP", "CMP", "CMP", "CPX", "CPX", "CPY", "CPY", "DEC", "DEC", "DEX", "DEY", "EOR", "EOR", "EOR", "EOR", "EOR", "EOR", "INC", "INC", "INX", "INY", "JMP", "JMP", "JSR", "LDA", "LDA", "LDA", "LDA", "LDA", "LDA", "LDX", "LDX", "LDX", "LDY", "LDY", "LDY", "LSR", "LSR", "LSR", "NOP", "ORA", "ORA", "ORA", "ORA", "ORA", "ORA", "PHA", "PHP", "PLA", "PLP", "ROL", "ROL", "ROL", "ROR", "ROR", "ROR", "RTI", "RTS", "SBC", "SBC", "SBC", "SBC", "SBC", "SBC", "SEC", "SED", "SEI", "STA", "STA", "STA", "STA", "STA", "STX", "STY", "TAX", "TAY", "TSX", "TXA", "TXS", "TYA" 
	};
	
	String[] opcodes = {
			"69", "6D", "7D", "79", "61", "71", "29", "2D", "3D", "39", "21", "31", "0A", "0E", "1E", "90", "B0", "F0", "2C", "30", "D0", "10", "00", "50", "70", "18", "D8", "58", "B8", "C9", "CD", "DD", "D9", "C1", "D1", "E0", "EC", "C0", "CC", "CE", "DE", "CA", "88", "49", "4D", "5D", "59", "41", "51", "EE", "FE", "E8", "C8", "4C", "6C", "20", "A9", "AD", "BD", "B9", "A1", "B1", "A2", "AE", "BE", "A0", "AC", "BC", "4A", "4E", "5E", "EA", "09", "0D", "1D", "19", "01", "11", "48", "08", "68", "28", "2A", "2E", "3E", "6A", "6E", "7E", "40", "60", "E9", "ED", "FD", "F9", "E1", "F1", "38", "F8", "78", "8D", "9D", "99", "81", "91", "8E", "8C", "AA", "A8", "BA", "8A", "9A", "98" 	
	};
	
	String[] noBytes = {
			"2", "3", "3", "3", "2", "2", "2", "3", "3", "3", "2", "2", "1", "3", "3", "2", "2", "2", "3", "2", "2", "2", "1", "2", "2", "1", "1", "1", "1", "2", "3", "3", "3", "2", "2", "2", "3", "2", "3", "3", "3", "1", "1", "2", "3", "3", "3", "2", "2", "3", "3", "1", "1", "3", "3", "3", "2", "3", "3", "3", "2", "2", "2", "3", "3", "2", "2", "2", "3", "3", "1", "3", "3", "1", "2", "3", "3", "3", "2", "2", "1", "1", "1", "1", "1", "3", "3", "1", "3", "3", "1", "1", "2", "3", "3", "3", "2", "2", "1", "1", "1", "3", "3", "3", "2", "2", "3", "3", "1", "1", "1", "1", "1", "1" 
	};
	
	String[] cycles = {
			"2", "4", "4*", "4*", "6", "5*", "2", "4", "4*", "4*", "6", "5*", "2", "6", "7", "2**", "2**", "2**", "4", "2**", "2**", "2**", "7", "2**", "2**", "2", "2", "2", "2", "2", "4", "4*", "4*", "6", "5*", "2", "4", "2", "4", "3", "7", "2", "2", "2", "4", "4*", "4*", "6", "5*", "6", "7", "2", "2", "3", "5", "6", "2", "4", "4*", "4*", "6", "5*", "2", "4", "4*", "2", "3", "4", "4", "4*", "2", "6", "7", "2", "2", "4", "4*", "4*", "6", "5*", "3", "3", "4", "4", "2", "6", "7", "2", "6", "7", "6", "6", "2", "4", "4*", "4*", "6", "5*", "2", "2", "2", "4", "5", "5", "6", "6", "4", "4", "2", "2", "2", "2", "2", "2" 
	};
	
	String[] symbolAddress = {
			"ADC immediate", "ADC absolute", "ADC absolute,X", "ADC absolute,Y", "ADC (indirect,X)", "ADC (indirect),Y", "AND immediate", "AND absolute", "AND absolute,X", "AND absolute,Y", "AND (indirect,X)", "AND (indirect),Y", "ASL accumulator", "ASL absolute", "ASL absolute,X", "BCC relative", "BCS relative", "BEQ relative", "BIT absolute", "BMI relative", "BNE relative", "BPL relative", "BRK implied", "BVC relative", "BVS relative", "CLC implied", "CLD implied", "CLI implied", "CLV implied", "CMP immediate", "CMP absolute", "CMP absolute,X", "CMP absolute,Y", "CMP (indirect,X)", "CMP (indirect),Y", "CPX immediate", "CPX absolute", "CPY immediate", "CPY absolute", "DEC absolute", "DEC absolute,X", "DEX implied", "DEY implied", "EOR immediate", "EOR absolute", "EOR absolute,X", "EOR absolute,Y", "EOR (indirect,X)", "EOR (indirect),Y", "INC absolute", "INC absolute,X", "INX implied", "INY implied", "JMP absolute", "JMP indirect", "JSR absolute", "LDA immediate", "LDA absolute", "LDA absolute,X", "LDA absolute,Y", "LDA (indirect,X)", "LDA (indirect),Y", "LDX immediate", "LDX absolute", "LDX absolute,Y", "LDY immediate", "LDY absolute", "LDY absolute,X", "LSR accumulator", "LSR absolute", "LSR absolute,X", "NOP implied", "ORA immediate", "ORA absolute", "ORA absolute,X", "ORA absolute,Y", "ORA (indirect,X)", "ORA (indirect),Y", "PHA implied", "PHP implied", "PLA implied", "PLP implied", "ROL accumulator", "ROL absolute", "ROL absolute,X", "ROR accumulator", "ROR absolute", "ROR absolute,X", "RTI implied", "RTS implied", "SBC immediate", "SBC absolute", "SBC absolute,X", "SBC absolute,Y", "SBC (indirect,X)", "SBC (indirect),Y", "SEC implied", "SED implied", "SEI implied", "STA absolute", "STA absolute,X", "STA absolute,Y", "STA (indirect,X)", "STA (indirect),Y", "STX absolute", "STY absolute", "TAX implied", "TAY implied", "TSX implied", "TXA implied", "TXS implied", "TYA implied"
	};
	
	public InstructionTable()
	{
		createInstructionArray();
	

	}
	
	public String[] getAddressing()
	{
		return addressing;
	}
	
	public String[] getSymbols()
	{
		return symbols;
	} 
	
	public String[] getOpcodes()
	{
		return opcodes;
	}
	
	public String[] getBytes()
	{
		return noBytes;
	}
	
	public String[] getCycles()
	{
		return cycles;
	}
	
	public String[] getSymbolAddress()
	{
		return symbolAddress;
	}
	
	public Instruction getInstruction(String opcode)
	{
		for(int i = 0; i < instructions.length; i++)
		{
			if(opcode.equals(instructions[i].getOpcode()))
			{
				return instructions[i];
			}
			
		}
		return null;
	}
	
	public Instruction getInstructionBySymbol(String symbol)
	{
		symbol = symbol.toUpperCase();
		
		for(int i = 0; i < instructions.length; i++)
		{
			if(symbol.equals(instructions[i].getSymbol()))
			{
				return instructions[i];
			}
			
		}
		return null;
	}
	
	public void createInstructionArray()
	{
		instructions = new Instruction[addressing.length];
		
		for(int i = 0; i < opcodes.length; i++)
		{
			Instruction ins = new Instruction(opcodes[i], symbols[i], addressing[i], noBytes[i], cycles[i]);
			instructions[i] = ins;
		}
	}
	
	public void printTable()
	{
		for(Instruction i : instructions)
		{
			System.out.println(i.toString());
		}
	}
	
}











