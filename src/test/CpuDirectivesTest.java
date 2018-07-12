package test;

import core.CPU;
import core.RAM;
import core.Stack;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CpuDirectivesTest extends TestCase {

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuDirectivesTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuDirectivesTest.class);
	}

	public void setUp()
	{
		this.cpu = new CPU();
		this.ram = new RAM();
		this.stack = new Stack();

		cpu.setRam(ram);
		cpu.setStack(stack);
		ram.setCpu(cpu);
		stack.setCpu(cpu);

		cpu.reset();

	}

	public void testInitialState()
	{
		assertEquals(0, cpu.getA());
		assertEquals(0, cpu.getX());
		assertEquals(0, cpu.getY());
		assertEquals(0, cpu.getPc());
		assertEquals(0x3f, cpu.getSp());
		assertEquals(0x20, cpu.getProcessorStatusFlags());
	}

	
	public void testDirectiveByte()
	{
		cpu.reset();
		
		String[] directivesByteProgram = {"00001010", "00011111",   //dc.b $A, $1F 
										  "00110111", "00001111",   //dc.b $37, $10
										  "01100110", "00011100"};  //dc.b $66, $1C
		
		ram.loadDirectives(directivesByteProgram);
		ram.showTrimmedMemory();
		
		assertEquals(31, ram.read(0xA));
		assertEquals(15, ram.read(0x37));
		assertEquals(28, ram.read(0x66));
	}

	public void testDirectiveString()
	{
		cpu.reset();
		
		String[] directivesByteProgram = {"00001010", "01110100",   //dc.s $A, test 
										  "00001011", "01100101",   
										  "00001100", "01110011",	
										  "00001101", "01110100"};  
		
		ram.loadDirectives(directivesByteProgram);
		ram.showTrimmedMemory();
		
		int startingAddress = 0x0A;
		int value = 0;
		
		value = ram.read(startingAddress);
		char c = (char) value;
		assertEquals('t', c);
		startingAddress++;
		
		value = ram.read(startingAddress);
		c = (char) value;
		assertEquals('e', c);
		startingAddress++;
		
		value = ram.read(startingAddress);
		c = (char) value;
		assertEquals('s', c);
		startingAddress++;
		
		value = ram.read(startingAddress);
		c = (char) value;
		assertEquals('t', c);
		startingAddress++;
	}
	
	public void testDirectiveValueArray()
	{
		cpu.reset();
		
		String[] directivesByteProgram = {"00110011", "00011001",   //dc.v $33, $19, $55, $CF 
										  "00110100", "01010101",   
										  "00110101", "11001111"};  
		
		ram.loadDirectives(directivesByteProgram);
		ram.showTrimmedMemory();
		
		assertEquals(25, ram.read(0x33));
		assertEquals(85, ram.read(0x34));
		assertEquals(207, ram.read(0x35));
	}


	
}
