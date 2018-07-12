package test;

import static org.junit.Assert.assertEquals;

import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * This JUnit Test class will test the following opcodes:
 *
 * LDA - 0xA9 Immediate
 * LDA - 0xAD Absolute
 * LDA - 0xBD Absolute,X
 * LDA - 0xB9 Absolute,Y
 * LDA - 0xA1 (Indirect,X)
 * LDA - 0xB1 (Indirect),Y
 *
 * LDX - 0xA2 Immediate
 * LDX - 0xAE Absolute
 * LDX - 0xBE Absolute,Y
 *
 * LDY - 0xA0 Immediate
 * LDY - 0xAC Absolute
 * LDY - 0xBC Absolute,X
 *
 * STA - 0x8D Absolute
 * STA - 0x9D Absolute,X
 * STA - 0x99 Absolute,Y
 * STA - 0x81 (Indirect,X)
 * STA - 0x91 (Indirect),Y
 *
 * STX - 0x8E Absolute
 *
 * STY - 0x8C Absolute
 */

public class CpuLoadStoreOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuLoadStoreOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuLoadStoreOperationsTest.class);
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
	 * LDA Immediate Tests
	 */

	public void test_LDA_Immediate_SetsAccumulator() throws SimulationException
	{
        int[] program = {0xa9, 0x12};  // LDA #$12
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x12, cpu.getA());
    }

    public void test_LDA_Immediate_SetsZeroFlagIfResultIsZero() throws SimulationException
    {
    	int[] program = {0xa9, 0x00};  // LDA #$00
    	ram.loadProgram(program);
        cpu.step();
        assertTrue(cpu.getZeroFlag());
    }

    public void test_LDA_Immediate_DoesNotSetZeroFlagIfResultNotZero() throws SimulationException
    {
    	int[] program = {0xa9, 0x12};  // LDA #$12
    	ram.loadProgram(program);
        cpu.step();
        assertFalse(cpu.getZeroFlag());
    }

    public void test_LDA_Immediate_SetsNegativeFlagIfResultIsNegative() throws SimulationException
    {
    	int[] program = {0xa9, 0x80};  // LDA #$80
    	ram.loadProgram(program);
        cpu.step();
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDA_Immediate_DoesNotSetNegativeFlagIfResultNotNegative() throws SimulationException
    {
    	int[] program = {0xa9, 0x7f};  // LDA #$7F
    	ram.loadProgram(program);
        cpu.step();
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_LDA_Absolute() throws SimulationException
    {
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x0f);
        ram.write(0x12, 0x80);

        int[] program = {0xad, 0x10, // LDA $10
                        0xad, 0x11,  // LDA $11
                        0xad, 0x12}; // LDA $12
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDA_AbsoluteX() throws SimulationException
    {
        ram.write(0x42, 0x00);
        ram.write(0x43, 0x0f);
        ram.write(0x44, 0x80);

        int[] program = {0xbd, 0x10, // LDA $10,X
                        0xbd, 0x11,  // LDA $11,X
                        0xbd, 0x12}; // LDA $12,X
        ram.loadProgram(program);

        cpu.setX(0x32);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDA_AbsoluteY() throws SimulationException
    {
        ram.write(0x42, 0x00);
        ram.write(0x43, 0x0f);
        ram.write(0x44, 0x80);

        int[] program = {0xb9, 0x10,  // LDA $10,Y
                        0xb9, 0x11,  // LDA $11,Y
                        0xb9, 0x12}; // LDA $12,Y
        ram.loadProgram(program);

        cpu.setY(0x32);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDA_IndexedIndirect() throws SimulationException
    {
        ram.write(0x8c, 0x3f);
        ram.write(0x8d, 0xc4);
        ram.write(0x3f, 0x45);

        cpu.setX(0x0c);

        int[] program = {0xa1, 0x80}; // LDA ($80,X)
        ram.loadProgram(program);
        cpu.step(1);

        assertEquals(0x45, cpu.getA());
    }

    public void test_LDA_IndirectIndexed() throws SimulationException
    {
        assertEquals(cpu.toString(), 0x00, cpu.getA());
        ram.write(0x14, 0x00);
        ram.write(0x15, 0xd8);
        ram.write(0x28, 0x03);

        cpu.setY(0x28);

        int[] program = {0xb1, 0x14}; // LDA ($14),Y
        ram.loadProgram(program);
        cpu.step(1);

        assertEquals(0x03, cpu.getA());
    }

    /*
     * LDX tests
     */

    public void test_LDX_Immediate_SetsXRegister() throws SimulationException
    {
        int[] program = {0xa2, 0x12};  // LDX #$12
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x12, cpu.getX());
    }

    public void test_LDX_Immediate_SetsZeroFlagIfResultIsZero() throws SimulationException
    {
        int[] program = {0xa2, 0x00};  // LDX #$00
        ram.loadProgram(program);
        cpu.step();
        assertTrue(cpu.getZeroFlag());
    }

    public void test_LDX_Immediate_DoesNotSetZeroFlagIfResultNotZero() throws SimulationException
    {
        int[] program = {0xa2, 0x12};  // LDX #$12
        ram.loadProgram(program);
        cpu.step();
        assertFalse(cpu.getZeroFlag());
    }

    public void test_LDX_Immediate_SetsNegativeFlagIfResultIsNegative() throws SimulationException
    {
        int[] program = {0xa2, 0x80};  // LDX #$80
        ram.loadProgram(program);
        cpu.step();
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDX_Immediate_DoesNotSetNegativeFlagIfResultNotNegative() throws SimulationException
    {
        int[] program = {0xa2, 0x7f};  // LDX #$7F
        ram.loadProgram(program);
        cpu.step();
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_LDX_Absolute() throws SimulationException
    {
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x0f);
        ram.write(0x12, 0x80);

        int[] program = {0xae, 0x10,  // LDX $10
                        0xae, 0x11, // LDX $11
                        0xae, 0x12}; // LDX $12
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getX());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDX_AbsoluteY() throws SimulationException
    {
        ram.write(0x45, 0x00);
        ram.write(0x46, 0x0f);
        ram.write(0x47, 0x80);

        int[] program = {0xbe, 0x10,  // LDX $10,Y
                        0xbe, 0x11,  // LDX $11,Y
                        0xbe, 0x12}; // LDX $12,Y
        ram.loadProgram(program);

        cpu.setY(0x35);

        cpu.step();
        assertEquals(0x00, cpu.getX());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getX());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    /*
     * LDY tests
     */

    public void test_LDY_Immediate_SetsYRegister() throws SimulationException
    {
        int[] program = {0xa0, 0x12};  // LDY #$12
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x12, cpu.getY());
    }

    public void test_LDY_Immediate_SetsZeroFlagIfArgIsZero() throws SimulationException
    {
        int[] program = {0xa0, 0x00};  // LDY #$00
        ram.loadProgram(program);

        cpu.step();
        assertTrue(cpu.getZeroFlag());
    }

    public void test_LDY_Immediate_DoesNotSetZeroFlagIfResultNotZero() throws SimulationException
    {
        int[] program = {0xa0, 0x12};  // LDY #$12
        ram.loadProgram(program);

        cpu.step();
        assertFalse(cpu.getZeroFlag());
    }

    public void test_LDY_Immediate_SetsNegativeFlagIfResultIsNegative() throws SimulationException
    {
        int[] program = {0xa0, 0x80};  // LDY #$80
        ram.loadProgram(program);

        cpu.step();
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDY_Immediate_DoesNotSetNegativeFlagIfResultNotNegative() throws SimulationException
    {
        int[] program = {0xa0, 0x7f};  // LDY #$7F
        ram.loadProgram(program);

        cpu.step();
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_LDY_Absolute() throws SimulationException
    {
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x0f);
        ram.write(0x12, 0x80);

        int[] program = {0xbc, 0x10, // LDY $10
                        0xbc, 0x11,  // LDY $11
                        0xbc, 0x12}; // LDY $12
        ram.loadProgram(program);


        cpu.step();
        assertEquals(0x00, cpu.getY());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_LDY_AbsoluteY() throws SimulationException
    {
        ram.write(0x45, 0x00);
        ram.write(0x46, 0x0f);
        ram.write(0x47, 0x80);

        int[] program = {0xbc, 0x10,  // LDY $10,X
                        0xbc, 0x11,   // LDY $11,X
                        0xbc, 0x12};  // LDY $12,X
        ram.loadProgram(program);

        cpu.setX(0x35);

        cpu.step();
        assertEquals(0x00, cpu.getY());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x0f, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x80, cpu.getY());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    /*
     * STA tests
     */

    public void test_STA_Absolute() throws SimulationException
    {
        cpu.setA(0x00);
        int[] program = {0x8d, 0x10};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, ram.read(0x10));
        // STA should have NO effect on flags.
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setA(0x0f);
        int[] program2 = {0x8d, 0x10};
        ram.loadProgram(program2);
        cpu.step();
        assertEquals(0x0f, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setA(0x80);
        int[] program3 = {0x8d, 0x10};
        ram.loadProgram(program3);
        cpu.step();
        assertEquals(0x80, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_STA_AbsoluteX() throws SimulationException
    {
        cpu.setX(0x30);

        cpu.setA(0x00);
        int[] program = {0x9d, 0x10}; // STA $10,X
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, ram.read(0x40));
        // STA should have NO affect on flags.
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();
        cpu.setX(0x30);

        cpu.setA(0x0f);
        int[] program2 = {0x9d, 0x10}; // STA $10,X
        ram.loadProgram(program2);
        cpu.step();
        assertEquals(0x0f, ram.read(0x40));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();
        cpu.setX(0x30);

        cpu.setA(0x80);
        int[] program3 = {0x9d, 0x10}; // STA $10,X
        ram.loadProgram(program3);
        cpu.step();
        assertEquals(0x80, ram.read(0x40));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_STA_AbsoluteY() throws SimulationException
    {
        cpu.setY(0x30);

        cpu.setA(0x00);
        int[] program = {0x99, 0x10}; // STA $10,Y
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, ram.read(0x40));
        // STA should have NO effect on flags
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();
        cpu.setY(0x30);


        cpu.setA(0x0f);
        int[] program2 = {0x99, 0x10}; // STA $10,Y
        ram.loadProgram(program2);
        cpu.step();
        assertEquals(0x0f, ram.read(0x40));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();
        cpu.setY(0x30);

        cpu.setA(0x80);
        int[] program3 = {0x99, 0x10}; // STA $10,Y
        ram.loadProgram(program3);
        cpu.step();
        assertEquals(0x80, ram.read(0x40));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    //TODO: STA (Indirect,X) test
    //TODO: STA (Indirect),Y test

    /*
     * STX tests
     */

    public void test_STX() throws SimulationException
    {
        cpu.setX(0x00);
        int[] program = {0x8e, 0x10};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, ram.read(0x10));
        // STX should have NO effect on flags.
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setX(0x0f);
        int[] program2 = {0x8e, 0x10};
        ram.loadProgram(program2);
        cpu.step();
        assertEquals(0x0f, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setX(0x80);
        int[] program3 = {0x8e, 0x10};
        ram.loadProgram(program3);
        cpu.step();
        assertEquals(0x80, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    /*
     * STY tests
     */

    public void test_STY() throws SimulationException
	{
        cpu.setY(0x00);
        int[] program = {0x8c, 0x10};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x00, ram.read(0x10));
        // STY should have NO effect on flags.
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setY(0x0f);
        int[] program2 = {0x8c, 0x10};
        ram.loadProgram(program2);
        cpu.step();
        assertEquals(0x0f, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.reset();

        cpu.setY(0x80);
        int[] program3 = {0x8c, 0x10};
        ram.loadProgram(program3);
        cpu.step();
        assertEquals(0x80, ram.read(0x10));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }
}
