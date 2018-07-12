package test;


import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * This JUnit Test tests the following opcodes:
 *
 * INC - 0xEE Absolute
 * INC - 0xFE Absolute,X
 *
 * DEC - 0xCE Absolute
 * DEC - 0xDE Absolute,X
 *
 * INX - 0xE8 Implied
 *
 * INY - 0xC8 Implied
 *
 * DEX - 0xCA Implied
 *
 * DEY - 0x88 Implied
 */

public class CpuIncrementDecrementTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuIncrementDecrementTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuIncrementDecrementTest.class);
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

	/*
	 * INC tests
	 */

	public void test_INC_Absolute() throws SimulationException
	{
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x7f);
        ram.write(0x12, 0xff);

        int[] program = {0xee, 0x10, // INC $10
                        0xee, 0x11,  // INC $11
                        0xee, 0x12}; // INC $12
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x01, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, ram.read(0x11));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x12));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

	public void test_INC_AbsoluteX() throws SimulationException
	{
        ram.write(0x30, 0x00);
        ram.write(0x31, 0x7f);
        ram.write(0x32, 0xff);

        cpu.setX(0x20);

        int[] program = {0xfe, 0x10,  // INC $10,X
                        0xfe, 0x11,   // INC $11,X
                        0xfe, 0x12};  // INC $12,X
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x01, ram.read(0x30));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x32));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

	/*
	 * DEC Tests
	 */

	public void test_DEC_Absolute() throws SimulationException
	{
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x01);
        ram.write(0x12, 0x80);
        ram.write(0x13, 0xff);

        int[] program = {0xce, 0x10,  // DEC $10
                        0xce, 0x11,  // DEC $11
                        0xce, 0x12,  // DEC $12
                        0xce, 0x13}; // DEC $13
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0xff, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x11));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x7f, ram.read(0x12));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xfe, ram.read(0x13));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

	public void test_DEC() throws SimulationException
	{
        ram.write(0x40, 0x00);
        ram.write(0x41, 0x01);
        ram.write(0x42, 0x80);
        ram.write(0x43, 0xff);

        int[] program = {0xde, 0x10, // DEC $10,X
                        0xde, 0x11,  // DEC $11,X
                        0xde, 0x12,  // DEC $12,X
                        0xde, 0x13}; // DEC $13,X
        ram.loadProgram(program);

        cpu.setX(0x30);

        cpu.step();
        assertEquals(0xff, ram.read(0x40));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x41));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x7f, ram.read(0x42));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xfe, ram.read(0x43));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

	/*
	 * INX Tests
	 */

	public void test_INX() throws SimulationException
	{
        int[] program = {0xe8};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x01, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

    }


    public void test_INX_SetsNegativeFlagWhenNegative() throws SimulationException
		{
        int[] program = {0xe8};
        ram.loadProgram(program);
        cpu.setX(0x7f);
        cpu.step();
        assertEquals(0x80, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }


    public void test_INX_SetsZeroFlagWhenZero() throws SimulationException
		{
        int[] program = {0xe8};
        ram.loadProgram(program);
        cpu.setX(0xff);
        cpu.step();
        assertEquals(0x00, cpu.getX());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    /*
     * INY Tests
     */


    public void test_INY() throws SimulationException
		{
        int[] program = {0xc8};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x01, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }


    public void test_INY_SetsNegativeFlagWhenNegative() throws SimulationException
		{
        int[] program = {0xc8};
        ram.loadProgram(program);
        cpu.setY(0x7f);
        cpu.step();
        assertEquals(0x80, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }


    public void test_INY_SetsZeroFlagWhenZero() throws SimulationException
		{
        int[] program = {0xc8};
        ram.loadProgram(program);
        cpu.setY(0xff);
        cpu.step();
        assertEquals(0x00, cpu.getY());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    /*
     * DEX Tests
     */

    public void test_DEX() throws SimulationException
		{
        int[] program = {0xca};
        ram.loadProgram(program);
        cpu.setX(0x02);
        cpu.step();
        assertEquals(0x01, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }


    public void test_DEX_SetsZeroFlagWhenZero() throws SimulationException
		{
        int[] program = {0xca};
        ram.loadProgram(program);
        cpu.setX(0x01);
        cpu.step();
        assertEquals(0x00, cpu.getX());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }


    public void test_DEX_SetsNegativeFlagWhen() throws SimulationException
		{
        int[] program = {0xca};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0xff, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    /*
     * DEY Tests
     */


    public void test_DEY() throws SimulationException
		{
        int[] program = {0x88};
        ram.loadProgram(program);
        cpu.setY(0x02);
        cpu.step();
        assertEquals(0x01, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }


    public void test_DEY_SetsZeroFlagWhenZero() throws SimulationException
		{
        int[] program = {0x88};
        ram.loadProgram(program);
        cpu.setY(0x01);
        cpu.step();
        assertEquals(0x00, cpu.getY());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }


    public void test_DEY_SetsNegativeFlagWhen() throws SimulationException
		{
        int[] program = {0x88};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0xff, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }
}
