package test;

import static org.junit.Assert.assertEquals;

import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CpuSystemFunctionsTest extends TestCase {

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;
	
	public CpuSystemFunctionsTest(String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		return new TestSuite(CpuSystemFunctionsTest.class);
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
	
	public void test_NOP_Implied() throws SimulationException
	{
        
		int[] program = {0xea, 0x00};
        ram.loadProgram(program);
        
        cpu.step();
        // Should just not change anything except PC, which should be incremented by 2
        assertEquals(0, cpu.getA());
        assertEquals(0, cpu.getX());
        assertEquals(0, cpu.getY());
        assertEquals(0x02, cpu.getPc());
        assertEquals(0x3f, cpu.getSp());
        assertEquals(0x20, cpu.getProcessorStatusFlags());
    }
	
	public void test_RTI() throws SimulationException
	{
        cpu.stackPush(0x11); // PC lo
        cpu.stackPush(0x29); // status

        int[] program = {0xEF, 0xEF,
        				 0x40, 0x00};
        ram.loadProgram(program);
        cpu.step(2);

        assertEquals(0x11, cpu.getPc());
        assertEquals(0x29, cpu.getProcessorStatusFlags());
    }
	
}
