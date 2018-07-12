package core;

import util.Utils;

/*
 * A "lite" implementation of the MOS 6502 processor.
 *  
 * The implementation is considered "lite" due to the 8-bit limitation
 * of the simulation's address bus. From this, the effective memory size
 * of the simulation is limited to 256 bytes, or from 0x00 - 0xFF. 
 * 
 * The MOS6502Lite simply consists of a CPU, stack and RAM simulation.
 * 
 */

public class MOS6502 {

	private final CPU cpu;
	private final RAM ram;
	private final Stack stack;
	
	public MOS6502()
	{
		this.cpu = new CPU();
		this.ram = new RAM();
		this.stack = new Stack();
		
		cpu.setRam(ram);
		cpu.setStack(stack);
		ram.setCpu(cpu);
		stack.setCpu(cpu);
		
		resetCPU();
		
	}
	
	
	
	public void loadRAM(int[] program)
	{
		System.out.println("\n[MOS6502] Load program into RAM");
		
//		int writeAddress = 0;
//		for(int i : program)
//		{
//			int programByte = i;
//			ram.write(writeAddress, programByte);
//			writeAddress++;
//		}
		ram.loadProgram(program);
		ram.showTrimmedMemory();
	}
	
	public void loadDirectives(String[] directives)
	{
		System.out.println("\n[MOS6502] Load directives into RAM");
		
		ram.loadDirectives(directives);
		ram.showTrimmedMemory();
	}
	
	public void resetCPU()
	{
		cpu.reset();
	}
	
	
	public CPU getCpu()
	{
		return cpu;
	}
	
	public RAM getRam()
	{
		return ram;
	}
	
	public Stack getStack()
	{
		return stack;
	}
}
