package test;

import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 *  The following JUnit Test tests the following instructions:
 *  TAX - 0xAA
 *  TAY - 0xA8
 *  TXA - 0x8A
 *  TYA - 0x98
 */

public class CpuRegisterTransferTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuRegisterTransferTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuRegisterTransferTest.class);
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

	public void testInitialState() throws SimulationException
	{
		assertEquals(0, cpu.getA());
		assertEquals(0, cpu.getX());
		assertEquals(0, cpu.getY());
		assertEquals(0, cpu.getPc());
		assertEquals(0x3f, cpu.getSp());
		assertEquals(0x20, cpu.getProcessorStatusFlags());
	}

	public void testTAX() throws SimulationException
	{
		int[] program = {0xaa};

		cpu.setA(0x16);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x16, cpu.getX());
		assertFalse(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTAX_SetNegativeFlagIfXNegative() throws SimulationException
	{
		int[] program = {0xaa};

		cpu.setA(0xff);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0xff, cpu.getX());
		assertFalse(cpu.getZeroFlag());
		assertTrue(cpu.getNegativeFlag());
	}

	public void testTAX_SetZeroFlagIfXZero() throws SimulationException
	{
		int[] program = {0xaa};

		cpu.setA(0x00);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x00, cpu.getX());
		assertTrue(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTAY() throws SimulationException
	{
		int[] program = {0xa8};

		cpu.setA(0x16);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x16, cpu.getY());
		assertFalse(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTAY_SetNegativeFlagIfXNegative() throws SimulationException
	{
		int[] program = {0xa8};

		cpu.setA(0xff);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0xff, cpu.getY());
		assertFalse(cpu.getZeroFlag());
		assertTrue(cpu.getNegativeFlag());
	}

	public void testTAY_SetZeroFlagIfXZero() throws SimulationException
	{
		int[] program = {0xa8};

		cpu.setA(0x00);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x00, cpu.getY());
		assertTrue(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTXA() throws SimulationException
	{
		int[] program = {0x8A};

		cpu.setX(0x16);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x16, cpu.getA());
		assertFalse(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTXA_SetNegativeFlagIfXNegative() throws SimulationException
	{
		int[] program = {0x8A};

		cpu.setX(0xff);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0xff, cpu.getA());
		assertFalse(cpu.getZeroFlag());
		assertTrue(cpu.getNegativeFlag());
	}

	public void testTXA_SetZeroFlagIfXZero() throws SimulationException
	{
		int[] program = {0x8A};

		cpu.setX(0x00);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x00, cpu.getA());
		assertTrue(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTYA() throws SimulationException
	{
		int[] program = {0x98};

		cpu.setY(0x16);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x16, cpu.getA());
		assertFalse(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

	public void testTYA_SetNegativeFlagIfXNegative() throws SimulationException
	{
		int[] program = {0x98};

		cpu.setY(0xff);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0xff, cpu.getA());
		assertFalse(cpu.getZeroFlag());
		assertTrue(cpu.getNegativeFlag());
	}

	public void testTYA_SetZeroFlagIfXZero() throws SimulationException
	{
		int[] program = {0x98};

		cpu.setY(0x00);
		ram.loadProgram(program);
		cpu.step();

		//System.out.println("Y: " + cpu.getY());
		assertEquals(0x00, cpu.getA());
		assertTrue(cpu.getZeroFlag());
		assertFalse(cpu.getNegativeFlag());
	}

}
