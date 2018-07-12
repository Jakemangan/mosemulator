package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * This JUnitTest tests the following opcodes:
 * 
 * CLC - 0x18 Implied
 * CLD - 0xD8 Implied
 * CLI - 0x58 Implied
 * CLV - 0xB8 Implied
 * SEC - 0x38 Implied
 * SED - 0xF8 Implied
 * SEI - 0x78 Implied
 */

public class CpuStatusFlagOperationsTest extends TestCase {

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;
	
	public CpuStatusFlagOperationsTest(String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		return new TestSuite(CpuStatusFlagOperationsTest.class);
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
	
	public void test_CLC() throws SimulationException
	{
        cpu.setCarryFlag();
        assertTrue(cpu.getCarryFlag());

        int[] program = {0x18};
        ram.loadProgram(program);
        
        cpu.step();

        assertFalse(cpu.getCarryFlag());
    }

    
    public void test_CLD() throws SimulationException
    {
        cpu.setDecimalModeFlag();
        assertTrue(cpu.getDecimalModeFlag());

        int[] program = {0xd8};
        ram.loadProgram(program);
        
        cpu.step();

        assertFalse(cpu.getDecimalModeFlag());
    }

    
    public void test_CLI()  throws SimulationException
    {
        cpu.setIrqDisableFlag();
        assertTrue(cpu.getIrqDisableFlag());

        int[] program = {0x58};
        ram.loadProgram(program);
        
        cpu.step();

        assertFalse(cpu.getIrqDisableFlag());
    }

    public void test_CLV()  throws SimulationException
    {
        cpu.setOverflowFlag();
        assertTrue(cpu.getOverflowFlag());

        int[] program = {0xb8};
        ram.loadProgram(program);
        
        cpu.step();

        assertFalse(cpu.getOverflowFlag());
    }
    
    public void test_SEC() throws SimulationException
    {
        int[] program = {0x38};
        ram.loadProgram(program);
        
        cpu.step();
        assertTrue(cpu.getCarryFlag());
    }

    
    public void test_SED() throws SimulationException
    {
        int[] program = {0xf8};
        ram.loadProgram(program);
        
        cpu.step();
        assertTrue(cpu.getDecimalModeFlag());
    }

    
    public void test_SEI() throws SimulationException
    {
        int[] program = {0x78};
        ram.loadProgram(program);
        
        cpu.step();
        assertTrue(cpu.getIrqDisableFlag());
    }
	
}
