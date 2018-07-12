package core;


import compiler.InstructionTable;
import exceptions.SimulationException;
import gui.GUI2;
import util.Utils;

/**
 * 
 * The CPU class provides the actual NMOS 6502 simulation. The CPU class
 * contains a stack and RAM elements that assist with simulating the 6502's
 * internal function.
 *
 * TODO: Implement interrupts and BRK method/test (case 0x00)
 * TODO: Prevent out of bounds RAM reads through exceptions
 *
 * -Limitations/Differences to this system-
 * 8-bit PC, instead of 16-bit PC in 6502
 * Max memory size of 0xFF, instead of 0xFFFF in 6502
 * Therefore, maximum memory size of 256 x 8 = 2048 bits or 2KB (256 unique addresses, each holding a single byte) 
 * 
 * Due to the maximum memory size of 0xFF, this results in zero-page addressing within the simulation to effectively
 * act like absolute addressing, since zero-page addressing would normally access the memory addresses between 
 * 0x00 and 0xFF. Which are the only addresses available to this system. Zero-page addressing is therefore redundant, 
 * and is not included within the system's tests since the zero-page opcodes will never be executed. 
 * 
 * Since the system only contains an 8-bit PC and "address bus" this means that the little endian-ness of the original 6502
 * is not simulated in this system i.e. no 16-bit addresses = no need to swap the MSB of an address
 * 
 * Java represents integers as signed 32-bit, therefore it is required to mask off excess bits (int = int & 0xff)
 * in order to ensure excess bits do not intefere during integer arithmetic. 
 * 
 * Two bugs that are present on the original NMOS 6502; 
 * 1. ROR bug that causes the ROR instruction to behave like an ASL that does not affect the carry bit.
 * 2. Indirect instruction bug that causes the JMP instruction to not correctly fetch the target address if the 
 * indirect vector falls on a page boundary.
 * These bugs are not implemented into the simulation, for all intents and purposes the CPU implementation acts 
 * as an "idealised" NMOS 6502 i.e. a NMOS 6502 without the hardwired silicon bugs. 
 * 
 * Instructions on the original 6502 require between 1-3 bytes to program the instruction into memory.
 * Byte 1 = Instruction
 * Bytes 2-3 = 16-bit operand
 * Since the simulation utilises an 8-bit PC and address bus, each opcode therefore only requires a single byte 
 * to encode the operand. Also, each instruction that uses the implied addressing mode on the original 6502 
 * does not require any operand. For the purpose of standardisation the simulation requires that implied 
 * instructions have an operand equal to 0x00 e.g. "INC 0x00" is required instead of just "INC" in order to 
 * make use of the INC instruction. 
 * This means that each instruction in the assembly program requires an operand, which results in a 
 * maximum and minimum of 2 bytes to encode an instruction and its operand, regardless if the instruction
 * requires an operand or not.   
 * 
 * Due to the above, the PC is incremented by 2 after each fetch-execute cycle. 
 * 
 * All of the registers in the simulation wrap around to their starting value if the upper limit (0xFF) is breached.
 * This reflects how the 6502 operates.
 * 
 * Immediate addressing uses the format #number instead of #$number
 * 
 * All number inputs are assumed to be hexadecimal
 */

public class CPU {
	
	private GUI2 gui;
	
	public static final int PSR_CARRY = 0x01;
	public static final int PSR_ZERO = 0x02;
	public static final int PSR_IRQ_DISABLE = 0x04;
	public static final int PSR_DECIMAL = 0x08;
	public static final int PSR_BREAK = 0x10;
	public static final int PSR_SETBIT = 0x20; //Bit 5 of the PSR is always set
	public static final int PSR_OVERFLOW = 0x40;
	public static final int PSR_NEGATIVE = 0x80;
	
	public boolean canStep = true;
	
	
	
	
	/*
	 * Other device structures
	 */
	private RAM ram;
	private Stack stack;
	
	/*
	 * Accumulator
	 */
	private int a;
	
	/*
	 * X-index register
	 */
	private int x;
	
	/*
	 * Y-index register
	 */
	private int y;
	
	/*
	 * Stack pointer
	 */
	private int sp;
	
	/*
	 * Program counter
	 */
	private int pc;
	
	/*
	 * Instruction register - holds currently loaded instruction
	 */
	private int ir;
	
	/*
	 * Operand for instruction currently held in IR
	 */
	private int irOperand;
	
	/*
	 * Next instruction to be loaded
	 */
	private int nextIr;
	
	/*
	 * Operand for next instruction to be held in IR
	 */
	private int nextIrOperand;
	
	/*
	 * Status Register bits
	 */
	private boolean carryFlag;
	private boolean zeroFlag;
	private boolean irqDisableFlag;
	private boolean decimalModeFlag;
	private boolean breakFlag;
	private boolean overflowFlag;
	private boolean negativeFlag;
	
	
	
	/*
	 * Step counter
	 */
	private int stepCounter;
	
	/*
	 * Instruction lookup table
	 */
	private InstructionTable insTable;
	
	
	
	
	/*
	 * Constructor
	 */
	public CPU()
	{
		System.out.println("[CPU] CPU initialised");
		insTable = new InstructionTable();
	}
	
	/*
	 * Reset the CPU to known values. Called upon CPU creation.
	 */
	public void reset()
	{
		//Clear registers
		a = 0;
		x = 0;
		y = 0; 
		
		//SP points towards the bottom of the stack i.e. stack has no internal values. not sure
		sp = 63;
		
		//Program is always loaded into memory starting at 0x00
		pc = 0x00;
		
		//Clear instruction register
		ir = 0;
		irOperand = 0;
		nextIr = 0;
		nextIrOperand = 0;
		
		//Clear status register bits
		carryFlag = false;
		zeroFlag = false;
		irqDisableFlag = false;
		decimalModeFlag = false;
		breakFlag = false;
		overflowFlag = false;
		negativeFlag = false;
		
		//Reset step counter
		stepCounter = 0;
		
		//Allow the CPU to step
		canStep = true;
		
		System.out.println("\n[CPU] CPU values reset");
		
		//Peek ahead to next instruction, grabbing the opcode and operand
		peekAhead();
		
	}

	/*
	 * Peeks ahead to the next IR and next IR operand contents, these registers
	 * are only used to populate GUI elements with information. 
	 */
	public void peekAhead()
	{
		nextIr = ram.read(pc);
		nextIrOperand = ram.read(pc + 1);
		System.out.println("[CPU] peekAhead - nextIr set to: " + nextIr);
		System.out.println("[CPU] peekAhead - nextIrOperand set to: " + nextIrOperand);
	}
	
	public void step(int num) throws SimulationException
	{
		for(int i = 0; i < num; i++)
		{
			step();
		}
	}
	
	public void step() throws SimulationException
	{
		System.out.println("\n[CPU] ---Step---");
		
		if(canStep)
		{
			System.out.println("[CPU] Starting fetch");
			//Fetch instruction pointed at by PC 
			ir = ram.read(pc);
			irOperand = ram.read(pc + 1); //2 byte pair for instruction and operand
			
			int irAddressMode = (ir >> 2) & 0x07; //Bits 3-5 of IR denotes addressing mode of instruction
			int irOpMode = ir & 0x03;
			
			System.out.println("[CPU] IR value: " + ir + " (Address mode: " + irAddressMode + ") (Op mode: " + irOpMode + ")" 
									+ " (Instruction: " + determineInstructionSymbol(ir) + ")");
			System.out.println("[CPU] IR operand value: " + irOperand);
			//System.out.println("[CPU] IR has address mode " + irAddressMode);
			//ystem.out.println("[CPU] IR has op mode " + irOpMode);
			
			incrementPc(); //2 bytes per instruction:operand pair
			incrementPc();
			System.out.println("[CPU] PC incremented to: " + pc);
			
			stepCounter++;
		
			
			int effectiveAddress = 0;
			int tmp;
			
			//if terminating instruction is reached, finish execution
			if(checkTerminatingInstruction()) 
			{
				System.out.println("[CPU] End of program reached. Execution finished.");
				//setGuiOutput("End of program reached. Execution finished.");
			}
			else //otherwise continue with program 
			{
				
				System.out.println("[CPU] Gettting effective address");
				//Get the data from the effective address
				
				//If the instruction held in the IR matches any of the implied instruction opcodes, 
				//the addressing mode used is implied, therefore no effective address needs to be 
				//calculated, no further action needs to be taken.
				
				if(ir == 0x08 ||      //PHP
						ir == 0x28 || //PLP
						ir == 0x48 || //PHA
						ir == 0x68 || //PLA
						ir == 0x88 || //DEY
						ir == 0xA8 || //TAY
						ir == 0xC8 || //INY
						ir == 0xE8 || //INX
						ir == 0x18 || //CLC
						ir == 0x38 || //SLC
						ir == 0x58 || //CLI
						ir == 0x78 || //SEI
						ir == 0x98 || //TYA
						ir == 0xB8 || //CLV
						ir == 0xD8 || //CLD
						ir == 0xF8 || //SED
						ir == 0x8A || //TXA
						ir == 0x9A || //TXS
						ir == 0xAA || //TAX
						ir == 0xBA || //TSX
						ir == 0xCA || //DEX
						ir == 0xEA)   //NOP
				{
					System.out.println("[CPU] Addressing mode: Implied");
				}
				else
				{
					switch(irOpMode) 
					{
						case 0: //case 0 (i.e. irOpMode 0) uses the same addressing mode numbering as case 2, therefore case 0 just uses case 2
							System.out.println("[CPU] IR Op-mode case 0 - using case 2");
						case 2:
							System.out.println("[CPU] IR Op-mode case 2");
							switch(irAddressMode)
							{
								case 0: //#Immediate
									System.out.println("[CPU] Addressing mode case 0: Immediate");
									break;
								case 1: //Zero page - treated like absolute
									System.out.println("[CPU] Addressing mode case 1: Zero page (treated like absolute)");
									effectiveAddress = irOperand;
									break;
								case 2: //Accumulator - ignored
									System.out.println("[CPU] Addressing mode case 2: Accumulator (ignored)");
									break;
								case 3: //Absolute
									System.out.println("[CPU] Addressing mode case 3: Absolute");
									effectiveAddress = irOperand;
									break;
								case 5:  //Zero page,Y / Zero page,X - treated like absolute,x and absolute,y
									if(ir == 0x96 || ir == 0xb6)
									{
										System.out.println("[CPU] Addressing mode case 5: Zero-page,Y (treated like absolute,y");
										effectiveAddress = yAddress(irOperand);
									}
									else
									{
										System.out.println("[CPU] Addressing mode case 5: Zero-page,X (treated like absolute,x");
										effectiveAddress = xAddress(irOperand);
									}
									break;
								case 7: //Absolute,Y / Absolute, X
									if(ir == 0xbe)
									{
										System.out.println("[CPU] Addressing mode case 7: Absolute,Y");
										effectiveAddress = yAddress(irOperand);
									}
									else
									{
										System.out.println("[CPU] Addressing mode case 7: Absolute,X");
										effectiveAddress = xAddress(irOperand);
									}
									break;
							}
							break;
						case 1:
							System.out.println("[CPU] IR Op-mode case 1");
							switch(irAddressMode)
							{
								case 0: // (Zero page, X) - treated like (Absolute,X)
									System.out.println("[CPU] Addressing mode case 0: (Zero-page,X)");
									tmp = xAddress(irOperand);
									effectiveAddress = ram.read(tmp); 
									break;
								case 1: // Zero page - treated like Absolute
									System.out.println("[CPU] Addressing mode case 1: Zero-page (treated like Absolute)");
									effectiveAddress = irOperand;
									break;
								case 2: // #Immediate
									System.out.println("[CPU] Addressing mode case 2: Immediate");
									effectiveAddress = -1;
									break;
								case 3: //Absolute
									System.out.println("[CPU] Addressing mode case 3: Absolute");
									effectiveAddress = irOperand;
									break;
								case 4: //(Zero page),Y - treated like (Absolute),Y
									System.out.println("[CPU] Addressing mode case 4: (Zero-page),Y (treated like (Absolute),Y )");
									tmp = ram.read(irOperand);
									effectiveAddress = yAddress(tmp);
									break;
								case 5: //Zero page, X - treated like Absolute, X
									System.out.println("[CPU] Addressing mode case 5: Zero-page,X (treated like Absolute,X)");
									effectiveAddress = xAddress(irOperand);
									break;
								case 6: //Absolute, Y 
									System.out.println("[CPU] Addressing mode case 6: Absolute,Y");
									effectiveAddress = yAddress(irOperand); //why & 0xff?
									break;
								case 7: //Absolute, X
									System.out.println("[CPU] Addressing mode case 7: Absolute,X");
									effectiveAddress = xAddress(irOperand);
									break;
							}
							break;
					}		
				}
				
				System.out.println("[CPU] Effective address: " + effectiveAddress);
				
				//Execute
				switch(ir)
				{
					//Load/Store Operations
					case 0xA9: //LDA - Load Accumulator - Immediate
						a = irOperand;
						setArithmeticFlags(a);
						break;
					case 0xA5: //LDA - Load Accumulator - Zero-page
					case 0xB5: //LDA - Load Accumulator - Zero-page,X
					case 0xAD: //LDA - Load Accumulator - Absolute
					case 0xBD: //LDA - Load Accumulator - Absolute,X 
					case 0xB9: //LDA - Load Accumulator - Absolute,Y
					case 0xA1: //LDA - Load Accumulator - (Indirect,X)
					case 0xB1: //LDA - Load Accumulator - (Indirect),Y
						a = ram.read(effectiveAddress);
						setArithmeticFlags(a);
						break;
						
					case 0xA2: //LDX - Load X Register - Immediate
						x = irOperand;
						setArithmeticFlags(x);
						break;
					case 0xA6: //LDX - Load X Register - Zero-page
					case 0xB6: //LDX - Load X Register - Zero-page,Y
					case 0xAE: //LDX - Load X Register - Absolute
					case 0xBE: //LDX - Load X Register - Absolute,Y
						x = ram.read(effectiveAddress);
						setArithmeticFlags(x);
						break;
						
					case 0xA0: //LDY - Load Y Register - Immediate
						y = irOperand;
						setArithmeticFlags(y);
						break;
					case 0xA4: //LDY - Load Y Register - Zero-page
					case 0xB4: //LDY - Load Y Register - Zero-page,X
					case 0xAC: //LDY - Load Y Register - Absolute
					case 0xBC: //LDY - Load Y Register - Absolute,X
						y = ram.read(effectiveAddress);
						setArithmeticFlags(y);
						break;
						
					case 0x85: //STA - Store Accumulator in Memory - Zero-page
					case 0x95: //STA - Store Accumulator in Memory - Zero-page,X
					case 0x8D: //STA - Store Accumulator in Memory - Absolute
					case 0x9D: //STA - Store Accumulator in Memory - Absolute,X
					case 0x99: //STA - Store Accumulator in Memory - Absolute,Y
					case 0x81: //STA - Store Accumulator in Memory - (Indirect,X)
					case 0x91: //STA - Store Accumulator in Memory - (Indirect),Y
						ram.write(effectiveAddress, a);
						break;
						
					case 0x86: //STX - Store X Register in Memory - Zero-page
					case 0x96: //STX - Store X Register in Memory - Zero-page,Y
					case 0x8E: //STX - Store X Register in Memory - Absolute
						ram.write(effectiveAddress, x);
						break;
						
					case 0x84: //STY - Store Y Register in Memory - Zero-page
					case 0x94: //STY - Store Y Register in Memory - Zero-page,X
					case 0x8C: //STY - Store Y Register in Memory - Absolute
						ram.write(effectiveAddress, y);
						break;
						
						
						
				
				
					//Register Transfers
					case 0xAA: //TAX - Transfer Accumulator to X - Implied
						x = a;
						setArithmeticFlags(x);
						System.out.println("TAX (implied) instruction executed");
						break;
					case 0xA8: //TAY - Transfer Accumulator to Y - Implied
						y = a;
						setArithmeticFlags(y);
						System.out.println("TAY (implied) instruction executed");
						break;
					case 0x8A: //TXA - Transfer X to Accumulator - Implied
						a = x;
						setArithmeticFlags(a);
						System.out.println("TXA (implied) instruction executed");
						break;
					case 0x98: //TYA - Transfer Y to Accumulator - Implied
						a = y;
						setArithmeticFlags(a);
						System.out.println("TYA (implied) instruction executed");
						break;
				
				
					//Stack Operations
					case 0xBA: //TSX - Transfer Stack Pointer to X - Implied
						x = getSp();
						setArithmeticFlags(x);
						break;
					case 0x9A: //TXS - Transfer X to Stack Pointer - Implied
						sp = x;
						break;
					case 0x48: //PHA - Push Accumulator to Stack - Implied
						stackPush(a);
						break;
					case 0x08: //PHP - Push Processor Status to Stack - Implied
						stackPush(getProcessorStatusFlags() | 0x10);
						break;
					case 0x68: //PLA - Pull Accumulator from Stack - Implied
						a = stackPop();
						setArithmeticFlags(a);
						break;
					case 0x28: //PLP - Pull Processor Status from Stack - Implied
						setProcessorStatusFlags(stackPop());
						break;
						
				
				
					//Logical Operations
					case 0x29: //AND - Logical AND - Immediate
						a &= irOperand;
						setArithmeticFlags(a);
						break;
					case 0x25: //AND - Logical AND - Zero-page 
					case 0x35: //AND - Logical AND - Zero-page,X 
					case 0x2D: //AND - Logical AND - Absolute
					case 0x3D: //AND - Logical AND - Absolute,X 
					case 0x39: //AND - Logical AND - Absolute,Y
					case 0x21: //AND - Logical AND - (Indirect,X)
					case 0x31: //AND - Logical AND - (Indirect),Y
						a &= ram.read(effectiveAddress);
						setArithmeticFlags(a);
						break;
						
					case 0x49: //EOR - Exclusive OR - Immediate
						a ^= irOperand;
						setArithmeticFlags(a);
						break;
					case 0x45: //EOR - Exclusive OR - Zero-page 
					case 0x55: //EOR - Exclusive OR - Zero-page,X 
					case 0x4D: //EOR - Exclusive OR - Absolute
					case 0x5D: //EOR - Exclusive OR - Absolute,X 
					case 0x59: //EOR - Exclusive OR - Absolute,Y
					case 0x41: //EOR - Exclusive OR - (Indirect,X)
					case 0x51: //EOR - Exclusive OR - (Indirect),Y
						a ^= ram.read(effectiveAddress);
						setArithmeticFlags(a);
						break;
						
					case 0x09: //ORA - Logical Inclusive OR - Immediate
						a |= irOperand;
						setArithmeticFlags(a);
						break;
					case 0x05: //ORA - Logical Inclusive OR - Zero-page 
					case 0x15: //ORA - Logical Inclusive OR - Zero-page,X 
					case 0x0D: //ORA - Logical Inclusive OR - Absolute
					case 0x1D: //ORA - Logical Inclusive OR - Absolute,X 
					case 0x19: //ORA - Logical Inclusive OR - Absolute,Y
					case 0x01: //ORA - Logical Inclusive OR - (Indirect,X)
					case 0x11: //ORA - Logical Inclusive OR - (Indirect),Y
						a |= ram.read(effectiveAddress);
						setArithmeticFlags(a);
						break;
						
					case 0x24: //BIT - Bit Test - Zero-page
					case 0x2C: //BIT - Bit Test - Absolute
						tmp = ram.read(effectiveAddress);
						setZeroFlag((a & tmp) == 0);
						setOverflowFlag((tmp & PSR_OVERFLOW) != 0);
						setNegativeFlag((tmp & PSR_NEGATIVE) != 0);
						break;
						
						
				
				
					//Arithmetic Operations
					case 0x69: //ADC - Add with Carry - Immediate 
						if(decimalModeFlag)
						{
							a = adcDecimal(a, irOperand);
						}
						else
						{
							a = adc(a, irOperand);
						}
						break;
					case 0x65: //ADC - Add with Carry - Zero-page 
					case 0x75: //ADC - Add with Carry - Zero-page,X 
					case 0x6D: //ADC - Add with Carry - Absolute
					case 0x7D: //ADC - Add with Carry - Absolute,X 
					case 0x79: //ADC - Add with Carry - Absolute,Y
					case 0x61: //ADC - Add with Carry - (Indirect,X)
					case 0x71: //ADC - Add with Carry - (Indirect),Y
						if(decimalModeFlag)
						{
							a = adcDecimal(a, ram.read(effectiveAddress));
						}
						else
						{
							a = adc(a, ram.read(effectiveAddress));
						}
						break;
						
					case 0xE9: //SBC - Subtract with Carry - Immediate
						if(decimalModeFlag)
						{
							a = sbcDecimal(a, irOperand);
						}
						else
						{
							a = sbc(a, irOperand);
						}
						break;
					case 0xE5: //SBC - Subtract with Carry - Zero-page 
					case 0xF5: //SBC - Subtract with Carry - Zero-page,X 
					case 0xED: //SBC - Subtract with Carry - Absolute
					case 0xFD: //SBC - Subtract with Carry - Absolute,X 
					case 0xF9: //SBC - Subtract with Carry - Absolute,Y
					case 0xE1: //SBC - Subtract with Carry - (Indirect,X);
					case 0xF1: //SBC - Subtract with Carry - (Indirect),Y
						if(decimalModeFlag)
						{
							a = sbcDecimal(a, ram.read(effectiveAddress));
						}
						else
						{
							a = sbc(a, ram.read(effectiveAddress));
						}
						break;
						
					case 0xC9: //CMP - Compare - Immediate
						cmp(a, irOperand);
						break;
					case 0xC5: //CMP - Compare - Zero-page 
					case 0xD5: //CMP - Compare - Zero-page,X 
					case 0xCD: //CMP - Compare - Absolute
					case 0xDD: //CMP - Compare - Absolute,X 
					case 0xD9: //CMP - Compare - Absolute,Y
					case 0xC1: //CMP - Compare - (Indirect,X)
					case 0xD1: //CMP - Compare - (Indirect),Y
						cmp(a, ram.read(effectiveAddress));
						break;
					
					case 0xE0: //CPX - Compare X Register - Immediate
						cmp(x, irOperand);
						break;
					case 0xE4: //CPX - Compare X Register - Zero-page
					case 0xEC: //CPX - Compare X Register - Absolute
						cmp(x, ram.read(effectiveAddress));
						break;
						
					case 0xC0: //CPY - Compare Y Register - Immediate
						cmp(y, irOperand);
						break;
					case 0xC4: //CPY - Compare Y Register - Zero-page
					case 0xCC: //CPY - Compare Y Register - Absolute
						cmp(y, ram.read(effectiveAddress));
						break;
						
				
				
					//Increment & Decrement Operations
					case 0xE6: //INC - Increment Memory - Zero-page 
					case 0xF6: //INC - Increment Memory - Zero-page,X 
					case 0xEE: //INC - Increment Memory - Absolute
					case 0xFE: //INC - Increment Memory - Absolute,X 
						tmp = (ram.read(effectiveAddress) + 1) & 0xff;
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
					case 0xE8: //INX - Increment X Register - Implied
						x = ++x & 0xff;
						setArithmeticFlags(x);
						break;
						
					case 0xC8: //INY - Increment Y Register - Implied
						y = ++y & 0xff;
						setArithmeticFlags(y);
						break;
						
					case 0xC6: //DEC - Decrement Memory - Zero-page 
					case 0xD6: //DEC - Decrement Memory - Zero-page,X 
					case 0xCE: //DEC - Decrement Memory - Absolute
					case 0xDE: //DEC - Decrement Memory - Absolute,X 
						tmp = (ram.read(effectiveAddress) - 1) & 0xff;
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
					case 0xCA: //DEX - Decrement X Register - Implied
						x = --x & 0xff;
						setArithmeticFlags(x);
						break;
						
					case 0x88: //DEY - Decrement Y Register - Implied
						y = --y & 0xff;
						setArithmeticFlags(y);
						break;
				
						
						
						
						
					//Shift operations
					case 0x0A: //ASL - Arithmetic Shift Left - Accumulator
						a = asl(a);
						setArithmeticFlags(a);
						break;
					case 0x06: //ASL - Arithmetic Shift Left - Zero-page 
					case 0x16: //ASL - Arithmetic Shift Left - Zero-page,X 
					case 0x0E: //ASL - Arithmetic Shift Left - Absolute
					case 0x1E: //ASL - Arithmetic Shift Left - Absolute,X
						tmp = asl(ram.read(effectiveAddress));
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
					case 0x4A: //LSR - Rotate Right - Accumulator
						a = lsr(a);
						setArithmeticFlags(a);
						break;
					case 0x46: //LSR - Rotate Right - Zero-page 
					case 0x56: //LSR - Rotate Right - Zero-page,X 
					case 0x4E: //LSR - Rotate Right - Absolute
					case 0x5E: //LSR - Rotate Right - Absolute,X 
						tmp = lsr(ram.read(effectiveAddress));
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
					case 0x2A: //ROL - Rotate Left - Accumulator
						a = rol(a);
						setArithmeticFlags(a);
						break;
					case 0x26: //ROL - Rotate Left - Zero-page 
					case 0x36: //ROL - Rotate Left - Zero-page,X 
					case 0x2E: //ROL - Rotate Left - Absolute
					case 0x3E: //ROL - Rotate Left - Absolute,X
						tmp = rol(ram.read(effectiveAddress));
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
					case 0x6A: //ROR - Rotate Right - Accumulator
						a = ror(a);
						setArithmeticFlags(a);
						break;
					case 0x66: //ROR - Rotate Right - Zero-page 
					case 0x76: //ROR - Rotate Right - Zero-page,X 
					case 0x6E: //ROR - Rotate Right - Absolute
					case 0x7E: //ROR - Rotate Right - Absolute,X
						tmp = ror(ram.read(effectiveAddress));
						ram.write(effectiveAddress, tmp);
						setArithmeticFlags(tmp);
						break;
						
				
				
					//Jumps and Calls
					case 0x4C: //JMP - Jump - Absolute
						pc = irOperand; //set PC to absolute value held in operand
						break;
					case 0x6C: //JMP - Jump - Indirect
						int address = irOperand;
						pc = ram.read(address);
						break;
				
					case 0x20: //JSR - Jump to Subroutine - Absolute
						stackPush(pc - 2 & 0xFF); //minus 2 due to incrementation of PC by 2 after every fetch-execute cycle
						pc = irOperand; //set PC to absolute address held in irOperand
						break;
	
					case 0x60: //RTS - Return from Subroutine - Implied
						int returnAddress = stackPop();
						setPc((returnAddress + 2) & 0xFF); //Set the PC to the address pushed to stack + 2 for next instruction. Also peek at the next instruction. 
						break;
				
						
						
						
						
						
					//Branch instructions
					case 0x90: //BCC - Branch on Carry Clear - Relative
						if(!getCarryFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0xB0: //BCS - Branch on Carry Set - Relative
						if(getCarryFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0xF0: //BEQ - Branch on Result Zero - Relative
						if(getZeroFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0x30: //BMI - Branch on Result Minus - Relative
						if(getNegativeFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0xD0: //BNE - Branch on Result Not Zero - Relative
						if(!getZeroFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0x10: //BPL - Branch on Result Plus - Relative
						if(!getNegativeFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0x50: //BVC - Branch on Overflow Clear - Relative
						if(!getOverflowFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
					case 0x70: //BVS - Branch on Overflow Set - Relative
						if(getOverflowFlag())
						{
							pc = relativeAddress(irOperand);
						}
						break;
						
						
						
						
						
						
						
						
					//Status Flag Changes
					case 0x18: //CLC - Clear Carry Flag - Implied 
						clearCarryFlag();
						break;
					case 0xD8: //CLD - Clear Decimal Mode - Implied 
						clearDecimalModeFlag();
						break;
					case 0x58: //CLI - Clear Interrupt Disable - Implied
						clearIrqDisableFlag();
						break;
					case 0xB8: //CLV - Clear Overflow Flag - Implied
						clearOverflowFlag();
						break;
					case 0x38: //SEC - Set Carry Flag - Implied
						setCarryFlag();
						break;
					case 0xF8: //SED - Set Decimal Mode Flag - Implied
						setDecimalModeFlag();
						break;
					case 0x78: //SEI - Set Interrupt Disable - Implied
						setIrqDisableFlag();
						break;
						
						
						
					//System Functions
					case 0x00: //BRK - Force Interrupt - Implied
						//TODO: Implement interrupt functionality
						//For now this operation does nothing. 
						break;
					case 0xEA: //NOP - No Operation - Implied
						//No action taken
						break;
					case 0x40: //RTI - Return from Interrupt - Implied
						int status = stackPop();
						setProcessorStatusFlags(status);
						int newPcVal = stackPop();
						setPc(newPcVal);
						break;
				}
				
			}
		}
		else
		{
			System.out.println("[CPU] Terminating keyword END has been reached. CPU can no longer step");
			setGuiOutput("Terminating keyword END has been reached. CPU can no longer step");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Not used
	 */
	
//	public void getNextInstruction()
//	{
//		ir = nextIr;
//		irOperand = nextIrOperand;
//		System.out.println("[CPU] ir set to: " + ir);
//		System.out.println("[CPU] irOperand set to: " + irOperand);
//	}
	
	/*
	 * Checks the current instruction for the BEGIN (0xEFEF) and END (0xFFFF) keywords
	 * If the current contents of the IR and IR-operand combine to produce the keyword 
	 * values, then the instruction held on that line is one of the keywords.
	 * 
	 * If the BEGIN keyword is found, peekAhead() is called.
	 * 
	 * If the END keyword is found, the runloop terminates by setting canStep to false.
	 *  
	 * @return true if terminating instruction, false if not
	 */
	public boolean checkTerminatingInstruction()
	{
		if(ir == 239 && irOperand == 239) //BEGIN keyword, peek ahead to next instruction
		{
			System.out.println("[CPU] Instruction is BEGIN keyword. Getting next instruction.");
			peekAhead();
			return false;
		}
		else if(ir == 255 && irOperand == 255)
		{
			System.out.println("[CPU] Instruction is END keyword. Terminating runloop.");
			canStep = false;
			return true;
		}
		else
		{
			System.out.println("[CPU] Instruction is not a terminating instruction");
			return false;
		}
	}
	
	public void stackPush(int data)
	{
		System.out.println("SP: " + sp);
		
		stack.push(sp, data);
		
		if(sp == 0) //stack is full
		{
			sp = 63;
			System.out.println("[CPU] SP reached bottom of stack. Setting SP to top (0x3f).");
		}
		else
		{
			sp--;
		}
		
	}
	
	public int stackPop()
	{
		System.out.println("SP: " + sp);
		
		if(sp == 63) //stack is empty
		{
			sp = 0;
			System.out.println("[CPU] SP reached top of stack. Setting SP to bottom (0x00).");
		}
		else
		{
			sp++;
		}
		
		return stack.pop(sp);
	}
	
	/*
	 * ADC (decimal, uses BCD) adds the contents of a memory location to the accumulator along with the carry bit.
	 * Sets carry bit if overflow occurs
	 */
	private int adcDecimal(int a, int op)
	{
//		System.out.printf("[CPU] ADC-Decimal: %04X + %04X + %d",
//	            a, op, getCarryBit());
//	        int tmp =
//	            10*(( a & 0xf0 ) >> 4 ) +
//	                ( a & 0x0f ) +
//	            10*(( op & 0xf0 ) >> 4 ) +
//	                ( op & 0x0f ) +
//	            getCarryBit();
//	        setCarryFlag( tmp > 0x99 );
//	        int result = 0;
//	        int digit = 0;
//	        while( tmp > 0 ) {
//	            result |= (tmp % 10) << (4*digit++);
//	            tmp /= 10;
//	        }
//	        setZeroFlag( result == 0 );
//	        setOverflowFlag( false );
//	        negativeFlag = (result & 0x80) != 0;
//	        System.out.printf(", result: %04X\n", result);
//	        return result;
		
		
		System.out.println("[CPU] ADC-Decimal: " + a + " + " + op + " + " + getCarryBit());
		int l, h, result;
        l = (a & 0x0f) + (op & 0x0f) + getCarryBit();
        if ((l & 0xff) > 9) l += 6;
        h = (a >> 4) + (op >> 4) + (l > 15 ? 1 : 0);
        if ((h & 0xff) > 9) h += 6;
        result = (l & 0x0f) | (h << 4);
        result &= 0xff;
        setCarryFlag(h > 15);
        setZeroFlag(result == 0);
        setOverflowFlag(false); // BCD never sets overflow flag

        negativeFlag = (result & 0x80) != 0; // N Flag is valid on CMOS 6502/65816
           
        System.out.println("[CPU] ADC-Decimal result: " + result);
        return result;
	}
	
	/*
	 * Add with carry
	 */
	private int adc(int a, int op)
	{
		System.out.println("[CPU] ADC: " + a + " + " + op + " + " + getCarryBit());
		int result = (op & 0xff) + (a & 0xff) + getCarryBit();
        int carry = (op & 0x7f) + (a & 0x7f) + getCarryBit();
        setCarryFlag((result & 0x100) != 0);
        setOverflowFlag(carryFlag ^ ((carry & 0x80) != 0));
        result &= 0xff;
        setArithmeticFlags(result);
        System.out.println("[CPU] ADC result: " + result);
        return result;
	}
	
	private int sbcDecimal(int ac, int op)
	{
		System.out.println("[CPU] SBC-Decimal: " + a + " - " + op + " - " + getCarryBit());
		
		int l, h, result;
        l = (a & 0x0f) - (op & 0x0f) - (carryFlag ? 0 : 1);
        if ((l & 0x10) != 0) l -= 6;
        h = (a >> 4) - (op >> 4) - ((l & 0x10) != 0 ? 1 : 0);
        if ((h & 0x10) != 0) h -= 6;
        result = (l & 0x0f) | (h << 4) & 0xff;
        setCarryFlag((h & 0xff) < 15);
        setZeroFlag(result == 0);
        setOverflowFlag(false); // BCD never sets overflow flag

        negativeFlag = (result & 0x80) != 0; // N Flag is valid on CMOS 6502/65816

        
        System.out.println("[CPU] SBC-Decimal result: " + result);
        return (result & 0xff);
	}
	
	private int sbc(int a, int op) {
		System.out.println("[CPU] SBC: " + a + " - " + op + " - " + getCarryBit());
		int result;
        result = adc(a, ~op);
        setArithmeticFlags(result);
        System.out.println("[CPU] SBC result: " + result);
        return result;
    }
	
	private void cmp(int register, int operand)
	{
		int val = (register - operand) & 0xff;
		setCarryFlag(register >= operand);
		setZeroFlag(val == 0);
		setNegativeFlag((val & 0x80) != 0);
	}
	
	/*
	 * Shifts the passed parameter left by one bit.
	 * Sets the carry flag to the high bit of the initial value.
	 */
	private int asl(int val)
	{
		setCarryFlag((val & 0x80) != 0);
		return (val << 1) & 0xff;
	}
	
	/*
	 * Shifts the passed parameter right by one bit.
	 * Sets the carry flag to the low bit of the initial value
	 */
	private int lsr(int val)
	{
		setCarryFlag((val & 0x01) != 0);
		return (val & 0xff) >>> 1;
	}
	
	/*
	 * Rotates the passed int left by one bit.
	 * Sets bit 0 to the value of the carry flag, and sets the carry flag to the original value of bit 7
	 */
	private int rol(int val)
	{
		int result = ((val << 1) | getCarryBit()) & 0xff;
		setCarryFlag((val & 0x80) != 0);
		System.out.println("[CPU] " + val + " -> ROL -> " + result);
		return result;
	}
	
	/*
	 * Rotates the passed int right by one bit.
	 * Sets bit 7 to the value of the carry flag, and sets the carry flag to the original value of bit 1. 
	 */
	private int ror(int val)
	{
		int result = ((val >>> 1) | (getCarryBit() << 7)) & 0xff;
		setCarryFlag((val & 0x01) != 0);
		System.out.println("[CPU] " + val + " -> ROR -> " + result);
		return result;
	}
	
	
	
	
	/*
	 * Given an address, return the Absolute,X offset address.
	 */
	private int xAddress(int addr)
	{
		return (addr + x) & 0xff;
	}
	
	/*
	 * Given an address, return the Absolute,Y offset address.
	 */
	private int yAddress(int addr)
	{
		return (addr + y) & 0xff;
	}
	
	/*
	 * Given a byte, return the offset address from the PC
	 */
	private int relativeAddress(int offset)
	{
		int relAddress = (pc + (byte) offset) & 0xff;
		byte relAddressSigned = (byte) relAddress;
		System.out.println("[CPU Relative address = " + relAddress + " / " + relAddressSigned + " (" + pc + " + " + offset + ")");
		return relAddress;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * Set the zero and negative flags based on the value of 
	 * the variable passed
	 */
	private void setArithmeticFlags(int val)
	{
		zeroFlag = (val == 0);
		negativeFlag = (val & 0x80) != 0;
	}
	
	public int getProcessorStatusFlags()
	{
		int statusFlags = 0;
		
		if(carryFlag)
		{
			statusFlags |= PSR_CARRY;
		}
		
		if(zeroFlag)
		{
			statusFlags |= PSR_ZERO;
		}

		if(irqDisableFlag)
		{
			statusFlags |= PSR_IRQ_DISABLE;
		}
		
		if(decimalModeFlag)
		{
			statusFlags |= PSR_DECIMAL;
		}
		
		if(breakFlag)
		{
			statusFlags |= PSR_BREAK;
		}
		
		statusFlags |= PSR_SETBIT; //Bit 5 of the PSR is always set
		
		if(overflowFlag)
		{
			statusFlags |= PSR_OVERFLOW;
		}
		
		if(negativeFlag)
		{
			statusFlags |= PSR_NEGATIVE;
		}
		
		return statusFlags;
	}
	
	public void setProcessorStatusFlags(int status)
	{
		if((status & PSR_CARRY) != 0)
			setCarryFlag();
		else
			clearCarryFlag();
		
		if((status & PSR_ZERO) != 0)
			setZeroFlag();
		else
			clearZeroFlag();
		
		if((status & PSR_IRQ_DISABLE) != 0)
			setIrqDisableFlag();
		else
			clearIrqDisableFlag();
		
		if((status & PSR_DECIMAL) != 0)
			setDecimalModeFlag();
		else
			clearDecimalModeFlag();
		
		if((status & PSR_BREAK) != 0)
			setBreakFlag();
		else
			clearBreakFlag();
		
		if((status & PSR_OVERFLOW) != 0)
			setOverflowFlag();
		else
			clearOverflowFlag();
			
		if((status & PSR_NEGATIVE) != 0)
			setNegativeFlag();
		else
			clearNegativeFlag();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 *Getters / Setters
	 */
	
	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getSp() {
		return sp;
	}

	public void setSp(int sp) {
		System.out.println("[CPU] SP set to " + sp);
		this.sp = sp;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
		
		peekAhead();
	}
	
	public void incrementPc() {
		if(pc == 0xff)
		{
			pc = 0;
		}
		else
			pc++;
	}

	public int getIr() {
		return ir;
	}

	public void setIr(int ir) {
		this.ir = ir;
	}

	public int getNextIr() {
		return nextIr;
	}

	public void setNextIr(int nextIr) {
		this.nextIr = nextIr;
	}
	
	
	
	/*
	 * Flag related methods
	 */

	public boolean getCarryFlag() 
	{
		return carryFlag;
	}
	
	public int getCarryBit()
	{
		return (carryFlag ? 1 : 0);
	}
	
	public void setCarryFlag(boolean flag)
	{
		carryFlag = flag;
	}

	public void setCarryFlag() {
		carryFlag = true;
	}
	
	public void clearCarryFlag() {
		carryFlag = false;
	}

	public boolean getNegativeFlag() {
		return negativeFlag;
	}
	
	public void setNegativeFlag(boolean flag)
	{
		negativeFlag = flag;
	}

	public void setNegativeFlag() {
		negativeFlag = true;
	}
	
	public void clearNegativeFlag() {
		negativeFlag = false;
	}

	public boolean getZeroFlag() {
		return zeroFlag;
	}
	
	public void setZeroFlag(boolean flag)
	{
		zeroFlag = flag;
	}

	public void setZeroFlag() {
		zeroFlag = true;
	}
	
	public void clearZeroFlag() {
		zeroFlag = false;
	}

	public boolean getOverflowFlag() {
		return overflowFlag;
	}
	
	public void setOverflowFlag(boolean flag)
	{
		overflowFlag = flag;
	}

	public void setOverflowFlag() {
		overflowFlag = true;
	}
	
	public void clearOverflowFlag() {
		overflowFlag = false;
	}

	public boolean getIrqDisableFlag() {
		return irqDisableFlag;
	}
	
	public void setIrqDisableFlag(boolean flag)
	{
		irqDisableFlag = flag;
	}

	public void setIrqDisableFlag() {
		irqDisableFlag = true;
	}
	
	public void clearIrqDisableFlag() {
		irqDisableFlag = false;
	}

	public boolean getDecimalModeFlag() {
		return decimalModeFlag;
	}
	
	public void setDecimalModeFlag(boolean flag)
	{
		decimalModeFlag = flag;
	}

	public void setDecimalModeFlag() {
		decimalModeFlag = true;
	}
	
	public void clearDecimalModeFlag() {
		decimalModeFlag = false;
	}

	public boolean getBreakFlag() {
		return breakFlag;
	}
	
	public void setBreakFlag(boolean flag)
	{
		breakFlag = flag;
	}

	public void setBreakFlag() {
		breakFlag = true;
	}
	
	public void clearBreakFlag() {
		breakFlag = false;
	}
	
	
	
	
	

	public RAM getRam() {
		return ram;
	}
	
	public void setRam(RAM ram) {
		this.ram = ram;
	}
	
	public Stack getStack() {
		return stack;
	}
	
	public void setStack(Stack stack) {
		this.stack = stack;
	}

	public int getIrOperand() {
		return irOperand;
	}

	public void setIrOperand(int irOperand) {
		this.irOperand = irOperand;
	}

	public int getNextIrOperand() {
		return nextIrOperand;
	}

	public void setNextIrOperand(int nextIrOperand) {
		this.nextIrOperand = nextIrOperand;
	}

	public int getStepCounter() {
		return stepCounter;
	}

	public void setStepCounter(int stepCounter) {
		this.stepCounter = stepCounter;
	}
	
	public boolean getCanStep()
	{
		return canStep;
	}
	
	public void setGui(GUI2 gui)
	{
		this.gui = gui;
	}
	
	public void setGuiOutput(String msg)
	{
		gui.getAssemblerTextArea().setText(msg);
	}
	
	
	
	public String getRegisterContentsPrimary()
	{
		String a = "$00";
		String x = "$00";
		String y = "$00";
		
		/*
		 * The below statements ensure the strings have the 
		 * format 0xXX, even when the value is equal to 0
		 * or less than 16 i.e. will only have one character
		 * e.g. 0xB, 0x0, 0x9 etc.
		 */
		
		if(getA() > 0)
			a = "$" + Utils.DecToHex(getA());
		
		if(getX() > 0)
			x = "$" +  Utils.DecToHex(getX());
		
		if(getY() > 0)
			y = "$" + Utils.DecToHex(getY());
		
		if(a.length() == 2)
			a = "$0" + a.substring(1); 
		
		if(x.length() == 2)
			x = "$0" + x.substring(1);
		
		if(y.length() == 2)
			y = "$0" + y.substring(1);
		
		String registerContents = "A:   " + a + "\nX:   " + x + "\nY:   " + y;
		
		return registerContents;
	}
	
	public String getRegisterContentsSecondary()
	{
		String ir = "$00";
		String sp = "$00";
		String pc = "$00";
		
		/*
		 * The below statements ensure the strings have the 
		 * format 0xXX, even when the value is equal to 0
		 * or less than 16 i.e. will only have one character
		 * e.g. 0xB, 0x0, 0x9 etc.
		 */
		if(getIr() > 0)
			ir = "$" + Utils.DecToHex(getIr());
		
		if(getSp() > 0)
			sp = "$" + Utils.DecToHex(getSp());
		
		if(getPc() > 0)
			pc = "$" + Utils.DecToHex(getPc());
		
		if(ir.length() == 2)
			ir = "$0" + ir.substring(1); 
		
		if(sp.length() == 2)
			sp = "$0" + sp.substring(1);
		
		if(pc.length() == 2)
			pc = "$0" + pc.substring(1);
		
		
		String registerContents = "IR:   " + ir + "\nPC:   " + pc + "\nSP:   " + sp;
		
		return registerContents;
	}
	
	public String getPsrContentsPrimary()
	{
		String c = getCarryFlag() ? "1" : "0";
		String v = getOverflowFlag() ? "1" : "0";
		String z = getZeroFlag() ? "1" : "0";
		String n = getNegativeFlag() ? "1" : "0";
		
		String flags = "C:   " + c + "\n" 
					 + "V:   " + v + "\n"
					 + "Z:   " + z + "\n"
					 + "N:   " + n + "\n";
		
		return flags;
		
	}
	
	public String getPsrContentsSecondary()
	{
		String irq = getIrqDisableFlag() ? "1" : "0";
		String brk = getBreakFlag() ? "1" : "0";
		String dec = getDecimalModeFlag() ? "1" : "0";
		
		String flags = "IRQ-Disable:    " + irq + "\n" 
					 + "Break:          " + brk + "\n"
					 + "Decimal Mode:   " + dec + "\n";
		
		return flags;
		
	}
	
	
	//registerTextArea.setText("A:  0      PC: 0       \nY:  0      IR: 0       \nX:  0      SP: 0");
	
	
	
	/*
	 * Print methods 
	 */

	public void printRegisterContents()
	{
		System.out.println("\n[CPU] Print Register Contents");
		System.out.println("[CPU] A: " + getA() +
				" X: " + getX() + 
				" Y: " + getY() +
				" SP: " + getSp() + 
				" PC: " + getPc() +
				" IR: " + getIr());
	}
	
	/*
	 * Prints the symbol mneumonic of the value currently held in the ir 
	 */
	public String determineInstructionSymbol(int ir)
	{
		String[] opcodes = insTable.getOpcodes();
		String[] symbols = insTable.getSymbols();
		
		String opcodeHex = Utils.DecToHex(ir);
		String symbol = "";
		
		if(opcodeHex.length() == 1)
		{
			opcodeHex = "0".concat(opcodeHex);
		}
		
		if(opcodeHex.length() == 0)
		{
			opcodeHex = "00".concat(opcodeHex);
		}
		
		if(ir == 239)
		{
			symbol = "*BEGIN*";
			return symbol;
		}
		
		if(ir == 255)
		{
			symbol = "*END*";
			return symbol;
		}
		
		for(int i = 0; i < opcodes.length; i++)
		{
			if(opcodeHex.equals(opcodes[i]))
			{
				symbol = symbols[i];
			}
		}
		
		if(symbol == "")
		{
			symbol = "N/A";
		}
		
		return symbol;
	}
}

