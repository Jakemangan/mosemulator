package test;

import static org.junit.Assert.assertEquals;
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
 *
 *  The following JUnit Test tests the following instructions:
 *  TSX - 0xBA
 *  TXS - 0x9A
 *  PHA - 0x48
 *  PHP - 0x08
 *  PLA - 0x68
 *  PLP - 0x28
 */


public class CpuStackOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuStackOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuStackOperationsTest.class);
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

	public void test_TSX() throws SimulationException
	{
		int[] program = {0xBA};

		cpu.setSp(0x16);
		ram.loadProgram(program);
		cpu.step();

		assertEquals(0x16, cpu.getX());
		assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
	}

	public void test_TSX_SetNegativeFlag() throws SimulationException
	{
		int[] program = {0xBA};

		cpu.setSp(0xff);
		ram.loadProgram(program);
		cpu.step();

		assertEquals(0xff, cpu.getX());
		assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
	}

	public void test_TSX_SetZeroFlag() throws SimulationException
	{
		int[] program = {0xBA};

		cpu.setSp(0x00);
		ram.loadProgram(program);
		cpu.step();

		assertEquals(0x00, cpu.getX());
		assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
	}

	public void test_TXS() throws SimulationException
	{
		int[] program = {0x9A};

		cpu.setX(0x16);
		ram.loadProgram(program);
		cpu.step();

		assertEquals(0x16, cpu.getSp());
		assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
	}

	public void test_TXS_DoesNotAffectProcessorStatus() throws SimulationException
	{
		int[] program = {0x9A};

		cpu.setX(0x16);
		ram.loadProgram(program);
		cpu.step();

		assertEquals(0x16, cpu.getSp());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.setX(0x3f);
        cpu.setPc(0x00);
        ram.loadProgram(program);
		cpu.step();
		stack.showAllStackWithPointer(cpu.getSp());
		assertEquals(0x3f, cpu.getSp());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

	}

	public void test_PHA() throws SimulationException
	{
		cpu.reset();

		int[] program = {0x48};

		ram.loadProgram(program);
		cpu.setA(0x3A);
		cpu.step();

		assertEquals(0x3e, cpu.getSp());
		assertEquals(0x3a, cpu.stackPop());
	}

	public void test_PHP() throws SimulationException
	{
		cpu.reset();

		int[] program = {0x08};
		ram.loadProgram(program);

		cpu.setProcessorStatusFlags(0x27);
		System.out.println("Pre flags: " + cpu.getProcessorStatusFlags());
		cpu.step();
		System.out.println("Post flags: " + cpu.getProcessorStatusFlags());

		System.out.println("PHP");

		assertEquals(0x3e, cpu.getSp());
		//PHP instruction should set break flag on stack, but not in cpu
		assertEquals(0x37, cpu.stackPop());
		assertEquals(0x27, cpu.getProcessorStatusFlags());
	}

	public void test_PLA() throws SimulationException
	{
        cpu.reset();
		int[] program = {0x68};

		cpu.stackPush(0x32);
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x32, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());
    }

    public void test_PLA_SetsZeroIfAccumulatorIsZero() throws SimulationException
    {
    	cpu.reset();
		int[] program = {0x68};

		cpu.stackPush(0x00);
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getZeroFlag());
    }

    public void test_PLA_SetsNegativeIfAccumulatorIsNegative() throws SimulationException
    {
    	cpu.reset();
		int[] program = {0x68};

		cpu.stackPush(0xff);
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0xff, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());
    }

    public void test_PLP() throws SimulationException
    {
    	int[] program = {0x28};

    	cpu.stackPush(0x2f);
    	ram.loadProgram(program);

    	cpu.step();
    	assertEquals(0x2f, cpu.getProcessorStatusFlags());
    }

}
