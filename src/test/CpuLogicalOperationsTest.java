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
 * Tested Instructions
 * AND - 0x29 Immediate
 * AND - 0x2D Absolute
 * AND - 0x3D Absolute,X
 * AND - 0x39 Absolute,Y
 * AND - 0x21 (Indirect,X)
 * AND - 0x31 (Indirect,Y)
 *
 * EOR - 0x49 Immediate
 * EOR - 0x4D Absolute
 * EOR - 0x5D Absolute,X
 * EOR - 0x59 Absolute,Y
 * EOR - 0x41 (Indirect,X)
 * EOR - 0x51 (Indirect,Y)
 *
 * ORA - 0x09 Immediate
 * ORA - 0x0D Absolute
 * ORA - 0x1D Absolute,X
 * ORA - 0x19 Absolute,Y
 * ORA - 0x01 (Indirect,X)
 * ORA - 0x11 (Indirect,Y)
 *
 * BIT - 0x2C Absolute
 */

public class CpuLogicalOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuLogicalOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuLogicalOperationsTest.class);
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
	 * AND tests
	 */

	//Immediate
	public void test_AND_Immediate() throws SimulationException
	{
        int[] program = {0x29, 0x00,  // AND #$00
                        0x29, 0x11,  // AND #$11
                        0xa9, 0xaa,  // LDA #$AA
                        0x29, 0xff,  // AND #$FF
                        0x29, 0x99,  // AND #$99
                        0x29, 0x11}; // AND #$11
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());

        cpu.step();
        assertEquals(0x00, cpu.getA());

        cpu.step();
        cpu.step();
        assertEquals(0xaa, cpu.getA());

        cpu.step();
        assertEquals(0x88, cpu.getA());

        cpu.step();
        assertEquals(0x00, cpu.getA());
    }

    public void test_AND_Immediate_SetsZeroFlagIfResultIsZero() throws SimulationException
    {
        int[] program = {0xa9, 0x88,  // LDA #$88
                        0x29, 0x11}; // AND #$11
        ram.loadProgram(program);

        cpu.step();
        cpu.step();
        assertTrue(cpu.getZeroFlag());
    }

    public void test_AND_Immediate_DoesNotSetZeroFlagIfResultNotZero() throws SimulationException
    {
    	int[] program = {0xa9, 0x88,  // LDA #$88
                        0x29, 0xf1}; // AND #$F1
    	ram.loadProgram(program);

        cpu.step();
        cpu.step();
        assertFalse(cpu.getZeroFlag());
    }

    public void test_AND_Immediate_SetsNegativeFlagIfResultIsNegative() throws SimulationException
    {
    	int[] program = {0xa9, 0x88,  // LDA #$88
                        0x29, 0xf0}; // AND #$F0
    	ram.loadProgram(program);

        cpu.step();

        assertTrue(cpu.getNegativeFlag());
    }

    public void test_AND_Immediate_DoesNotSetNegativeFlagIfResultNotNegative() throws SimulationException
    {
    	int[] program = {0xa9, 0x88,  // LDA #$88
                        0x29, 0x0f}; // AND #$0F
    	ram.loadProgram(program);

        cpu.step();
        cpu.step();
        assertFalse(cpu.getNegativeFlag());
    }

	//Absolute
    public void test_AND_Absolute() throws SimulationException
    {
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x11);
        ram.write(0x12, 0xff);
        ram.write(0x13, 0x99);
        ram.write(0x14, 0x11);
        ram.write(0x15, 0x0f);

        int[] program = {0x2d, 0x10, // AND $10
                        0x2d, 0x11,  // AND $11
                        0xa9, 0xaa,  // LDA #$aa
                        0x2d, 0x12,  // AND $12
                        0x2d, 0x13,  // AND $13
                        0x2d, 0x14,   // AND $14
                        0xa9, 0xff,   // LDA #$ff
                        0x2d, 0x15,}; // AND $15

        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0xaa, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x88, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    public void test_AND_AbsoluteX() throws SimulationException
    {
        ram.write(0x30, 0x00);
        ram.write(0x31, 0x11);
        ram.write(0x32, 0xff);
        ram.write(0x33, 0x99);
        ram.write(0x34, 0x11);
        ram.write(0x35, 0x0f);
        ram.write(0x02, 0x11);

        // Set offset in X register.
        cpu.setX(0x30);

        int[] program = {0x3d, 0x00, // AND $00,X
                        0x3d, 0x01, // AND $01,X
                        0xa9, 0xaa, // LDA #$aa
                        0x3d, 0x02, // AND $02,X
                        0x3d, 0x03, // AND $03,X
                        0x3d, 0x04, // AND $04,X
                        0xa9, 0xff, // LDA #$ff
                        0x3d, 0x05, // AND $05,X
                        0xa9, 0x01, // LDA #$01
                        0x3d, 0xd2}; // AND $d2,X

        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0xaa, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x88, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

	//AbsoluteY

    public void test_AND_AbsoluteY() throws SimulationException
    {
        ram.write(0x30, 0x00);
        ram.write(0x31, 0x11);
        ram.write(0x32, 0xff);
        ram.write(0x33, 0x99);
        ram.write(0x34, 0x11);
        ram.write(0x35, 0x0f);
        ram.write(0x02, 0x11);

        // Set offset in Y register.
        cpu.setY(0x30);

        int[] program = {0x39, 0x00,  // AND $00,Y
                        0x39, 0x01,   // AND $01,Y
                        0xa9, 0xaa,   // LDA #$aa
                        0x39, 0x02,   // AND $02,Y
                        0x39, 0x03,   // AND $03,Y
                        0x39, 0x04,   // AND $04,Y
                        0xa9, 0xff,   // LDA #$ff
                        0x39, 0x05,   // AND $05,Y
                        0xa9, 0x01,   // LDA #$01
                        0x39, 0xd2};  // AND $d2,Y
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0xaa, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x88, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0x0f, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step(2);
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
    }

    //(Indirect,X)

    public void test_AND_IndexedIndirect() throws Exception {
        ram.write(0x12, 0x1f);
        ram.write(0x13, 0xc5);
        ram.write(0x1f, 0x31);

        cpu.setX(0x02);
        cpu.setA(0x15);

        int[] program = {0x21, 0x10}; // AND ($10,X)
        ram.loadProgram(program);

        cpu.step(1);

        assertEquals(0x11, cpu.getA());
        assertEquals(0x31, ram.read(0x1f));
    }

    //(Indirect),Y
    public void test_AND_IndirectIndexed() throws Exception {
        ram.write(0x14, 0x00);
        ram.write(0x15, 0xd8);
        ram.write(0x28, 0xe3);

        cpu.setY(0x28);
        cpu.setA(0x32);

        int[] program = {0x31, 0x14}; // AND ($14),Y
        ram.loadProgram(program);

        cpu.step(1);

        assertEquals(0x22, cpu.getA());
        assertEquals(0xe3, ram.read(0x28));
    }

    /*
     * EOR Tests
     */

    //Immediate

    public void test_EOR_Immediate()  throws SimulationException
    {
        int[] program = {0xa9, 0x88,  // LDA #$88
                        0x49, 0x00,  // EOR #$00
                        0x49, 0xff,  // EOR #$ff
                        0x49, 0x33}; // EOR #$33

        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x88, cpu.getA());

        cpu.step();
        assertEquals(0x77, cpu.getA());

        cpu.step();
        assertEquals(0x44, cpu.getA());
    }

    public void test_EOR_Immediate_SetsArithmeticFlags() throws SimulationException
    {
        int[] program = {0xa9, 0x77,  // LDA #$77
                        0x49, 0x77,  // EOR #$77
                        0x49, 0xff}; // EOR #$ff
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xff, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_EOR_Absolute() throws SimulationException
    {
        ram.write(0x10, 0x00);
        ram.write(0x11, 0xff);
        ram.write(0x12, 0x33);
        ram.write(0x13, 0x44);

        int[] program = {0xa9, 0x88,  // LDA #$88
                        0x4d, 0x10,  // EOR $10
                        0x4d, 0x11,  // EOR $11
                        0x4d, 0x12,  // EOR $12
                        0x4d, 0x13}; // EOR $13
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x88, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x44, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getZeroFlag());
    }

    public void test_EOR_AbsoluteX() throws SimulationException
    {
        ram.write(0x40, 0x00);
        ram.write(0x41, 0xff);
        ram.write(0x42, 0x33);
        ram.write(0x43, 0x44);

        cpu.setX(0x30);

        int[] program = {0xa9, 0x88, // LDA #$88
                        0x5d, 0x10,  // EOR $10,X
                        0x5d, 0x11,  // EOR $11,X
                        0x5d, 0x12,  // EOR $12,X
                        0x5d, 0x13}; // EOR $13,X
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x88, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x44, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getZeroFlag());
    }

    public void test_EOR_AbsoluteY() throws SimulationException
    {
        ram.write(0x40, 0x00);
        ram.write(0x41, 0xff);
        ram.write(0x42, 0x33);
        ram.write(0x43, 0x44);

        cpu.setY(0x30);

        int[] program = {0xa9, 0x88, // LDA #$88
                        0x59, 0x10,  // EOR $10,Y
                        0x59, 0x11,  // EOR $11,Y
                        0x59, 0x12,  // EOR $12,Y
                        0x59, 0x13}; // EOR $13,Y
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x88, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x44, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getZeroFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getZeroFlag());
    }

    //TODO: EOR indirect indexed test
    //TODO: EOR indexed indirect test

	/*
	 * ORA Tests
	 */

	//Immediate
	public void test_ORA_Immediate() throws SimulationException
	{
        int[] program = {0x09, 0x00,  // ORA #$00
                		 0x09, 0x11,  // ORA #$11
                		 0x09, 0x22,  // ORA #$22
                		 0x09, 0x44,  // ORA #$44
                		 0x09, 0x88}; // ORA #$88

		ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());

        cpu.step();
        assertEquals(0x11, cpu.getA());

        cpu.step();
        assertEquals(0x33, cpu.getA());

        cpu.step();
        assertEquals(0x77, cpu.getA());

        cpu.step();
        assertEquals(0xff, cpu.getA());
	}

	public void test_ORA_Immediate_SetsZeroFlagIfResultIsZero() throws SimulationException
	{
        int[] program = {0x09, 0x00};
		ram.loadProgram(program);

        cpu.step();
        assertTrue(cpu.getZeroFlag());
    }

    public void test_ORA_Immediate_DoesNotSetZeroFlagIfResultNotZero() throws SimulationException
    {
    	int[] program = {0x09, 0x01}; //ORA #$01
		ram.loadProgram(program);

        cpu.step();
        assertFalse(cpu.getZeroFlag());
    }

    public void test_ORA_Immediate_SetsNegativeFlagIfResultIsNegative() throws SimulationException
    {
    	int[] program = {0x09, 0x80};  //ORA #$80
		ram.loadProgram(program);

        cpu.step();
        assertTrue(cpu.getNegativeFlag());
    }

    public void test_ORA_Immediate_DoesNotSetNegativeFlagIfResultNotNegative() throws SimulationException
    {
    	int[] program = {0x09, 0x7f}; //ORA #$7F
		ram.loadProgram(program);

        cpu.step();
        assertFalse(cpu.getNegativeFlag());
    }

    //Absolute
    public void test_ORA_Absolute() throws SimulationException
    {
        cpu.reset();

    	// Set some initial values in memory
        ram.write(0x20, 0x00);
        ram.write(0x22, 0x11);
        ram.write(0x24, 0x22);
        ram.write(0x28, 0x44);
        ram.write(0x30, 0x88);

        int[] program = {0x0d, 0x20,  // ORA $20
                        0x0d, 0x22,  // ORA $22
                        0x0d, 0x24,  // ORA $24
                        0x0d, 0x28,  // ORA $28
                        0x0d, 0x30}; // ORA $30
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x11, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x33, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xff, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
    }

    //AbsoluteX
    public void test_ORA_AbsoluteX() throws SimulationException
    {
    	cpu.reset();

    	//Set some initial values in memory
        ram.write(0x30, 0x00);
        ram.write(0x32, 0x11);
        ram.write(0x34, 0x22);
        ram.write(0x38, 0x44);
        ram.write(0x40, 0x88);

        // Set offset in X register.
        cpu.setX(0x30);

        int[] program = {0x1d, 0x00,  // ORA $00,X
                        0x1d, 0x02,  // ORA $02,X
                        0x1d, 0x04,  // ORA $04,X
                        0x1d, 0x08, // ORA $08,X
                        0x1d, 0x10}; // ORA $10,X

        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x11, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x33, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xff, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

    }

    //AbsoluteY
    public void test_ORA_AbsoluteY() throws SimulationException
		{
       cpu.reset();

    	// Set some initial values in memory
        ram.write(0x30, 0x00);
        ram.write(0x32, 0x11);
        ram.write(0x34, 0x22);
        ram.write(0x38, 0x44);
        ram.write(0x40, 0x88);

        // Set offset in Y register.
        cpu.setY(0x30);

        int[] program = {0x19, 0x00, // ORA $00,Y
                        0x19, 0x02,  // ORA $02,Y
                        0x19, 0x04,  // ORA $04,Y
                        0x19, 0x08,  // ORA $08,Y
                        0x19, 0x10}; // ORA $10,Y

        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x11, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x33, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0x77, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());

        cpu.step();
        assertEquals(0xff, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());

    }

    //(Indirect,X)
    public void test_ORA_IndexedIndirect() throws Exception
		{
        ram.write(0x12, 0x1f);
        ram.write(0x13, 0xc5);
        ram.write(0x1f, 0x31);

        cpu.setX(0x02);
        cpu.setA(0x15);

        int[] program = {0x01, 0x10}; // ORA ($10,X)
        ram.loadProgram(program);
        cpu.step();

        assertEquals(0x35, cpu.getA());
        assertEquals(0x31, ram.read(0x1f));
    }

    //(Indirect),Y
    public void test_ORA_IndirectIndexed() throws Exception
		{
        ram.write(0x14, 0x00);
        ram.write(0x15, 0xd8);
        ram.write(0x28, 0xe3);

        cpu.setY(0x28);
        cpu.setA(0x32);

        int[] program = {0x11, 0x14}; // ORA ($14),Y
        ram.loadProgram(program);
        cpu.step();

        assertEquals(0xf3, cpu.getA());
        assertEquals(0xe3, ram.read(0x28));
    }

    /*
     * BIT tests
     */

    public void test_BIT_Absolute() throws SimulationException
		{
        ram.write(0x34, 0xc0);

        int[] program = {0xa9, 0x01, // LDA #$01
                        0x2c, 0x34, // BIT $34

                        0xa9, 0x0f, // LDA #$0f
                        0x2c, 0x34, // BIT $34

                        0xa9, 0x40, // LDA #$40
                        0x2c, 0x34, // BIT $34

                        0xa9, 0x80,  // LDA #$80
                        0x2c, 0x34,  // BIT $34

                        0xa9, 0xc0,  // LDA #$c0
                        0x2c, 0x34,  // BIT $34

                        0xa9, 0xff,  // LDA #$ff
                        0x2c, 0x34}; // BIT $34

        ram.loadProgram(program);

        cpu.step(2);
        assertTrue(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());

        cpu.step(2);
        assertTrue(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());

        cpu.step(2);
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());

        cpu.step(2);
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());

        cpu.step(2);
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());

        cpu.step(2);
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());
    }


}
