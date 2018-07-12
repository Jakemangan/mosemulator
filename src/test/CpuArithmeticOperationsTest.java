package test;


import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * SBC decimal mode tests in this test case some of their assertFalse(cpu.getNegativeFlag()) operations
 * changed to assertTrue(...) this was because the resultant values of the SBC decimal mode operations
 * were often greater than 128 and therefore would set the negative flag due to having bit 7 set as
 * evidenced here: http://www.obelisk.me.uk/6502/reference.html#SBC
 *
 * Was the original assertFalse(...) intentional or a mistake by seth? May need to change back to orignal
 * and reconfigure SBC functionality.
 *
 */


/*
 * This JUnit Test tests the following opcodes:
 *
 * ADC - 0x69 Immediate
 * ADC - 0x6D Absolute
 * ADC - 0x7D Absolute,X
 * ADC - 0x79 Absolute,Y
 * ADC - 0x61 (Indirect,X)
 * ADC - 0x71 (Indirect),Y
 *
 * SBC - 0xE9 Immediate
 * SBC - 0xED Absolute
 * SBC - 0xFD Absolute,X
 * SBC - 0xF9 Absolute,Y
 * SBC - 0xE1 (Indirect,X)
 * SBC - 0xF1 (Indirect),Y
 *
 * CMP - 0xC9 Immediate
 * CMP - 0xCD Absolute
 * CMP - 0xDD Absolute,X
 * CMP - 0xD9 Absolute,Y
 * CMP - 0xC1 (Indirect,X)
 * CMP - 0xD1 (Indirect),Y
 *
 * CPX - 0xE0 Immediate
 * CPX - 0xEC Absolute
 *
 * CPY - 0xC0 Immediate
 * CPY - 0xCC Absolute
 */

public class CpuArithmeticOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuArithmeticOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuArithmeticOperationsTest.class);
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
	 * ADC tests
	 */

	public void test_ADC_Immediate() throws SimulationException
	{
        int[] program0 = {0xa9, 0x00,  // LDA #$00
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program0);

        cpu.step(2);
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program1 = {0xa9, 0x7f,  // LDA #$7f
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program1);
        cpu.step(2);
        assertEquals(0x80, cpu.getA());

        assertTrue(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program2 = {0xa9, 0x80,  // LDA #$80
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program2);
        cpu.step(2);
        assertEquals(0x81, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program3 = {0xa9, 0xff,  // LDA #$ff
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program3);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertTrue(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.reset();
        int[] program4 = {0xa9, 0x00,  // LDA #$00
                        0x69, 0xff}; // ADC #$ff
        ram.loadProgram(program4);

        cpu.step(2);
        assertEquals(0xff, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program5 = {0xa9, 0x7f,  // LDA #$7f
                        0x69, 0xff}; // ADC #$ff
        ram.loadProgram(program5);

        cpu.step(2);
        assertEquals(0x7e, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.reset();
        int[] program6 = {0xa9, 0x80,  // LDA #$80
                        0x69, 0xff}; // ADC #$ff
        ram.loadProgram(program6);

        cpu.step(2);
        assertEquals(0x7f, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.reset();
        int[] program7 = {0xa9, 0xff,  // LDA #$ff
                        0x69, 0xff}; // ADC #$ff
        ram.loadProgram(program7);

        cpu.step(2);
        assertEquals(0xfe, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());
    }

	public void test_ADC_Immediate_DecimalMode() throws SimulationException
	{
        int[] program = {0xf8, 0x00, // SED
                        0xa9, 0x01,  // LDA #$01
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program);

        cpu.step(3);
        assertEquals(0x02, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program2 = {0xf8, 0x00, // SED
                        0xa9, 0x49,  // LDA #$49
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program2);

        cpu.step(3);
        assertEquals(0x50, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program3 = {0xf8, 0x00, // SED
                        0xa9, 0x50,  // LDA #$50
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program3);

        cpu.step(3);
        assertEquals(0x51, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program4 = {0xf8, 0x00,// SED
                        0xa9, 0x99,  // LDA #$99
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program4);

        cpu.step(3);
        assertEquals(0x00, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertTrue(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.reset();
        int[] program5 = {0xf8, 0x00, // SED
                        0xa9, 0x00,  // LDA #$00
                        0x69, 0x99}; // ADC #$99
        ram.loadProgram(program5);

        cpu.step(3);
        assertEquals(0x99, cpu.getA());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.reset();
        int[] program6 = {0xf8, 0x00, // SED
                        0xa9, 0x49,  // LDA #$49
                        0x69, 0x99}; // ADC #$99
        ram.loadProgram(program6);

        cpu.step(3);
        assertEquals(0x48, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.reset();
        int[] program7 = {0xf8, 0x00, // SED
                        0xa9, 0x50,  // LDA #$59
                        0x69, 0x99}; // ADC #$99
        ram.loadProgram(program7);

        cpu.step(3);
        assertEquals(0x49, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getCarryFlag());
    }

	public void test_ADC_Immediate_IncludesCarry() throws SimulationException
	{
        int[] program = {0xa9, 0x00,  // LDA #$01
        				0x38, 0x00,
                        0x69, 0x01}; // ADC #$01
        ram.loadProgram(program);

        cpu.step(3);
        assertEquals(0x02, cpu.getA());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getOverflowFlag());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getCarryFlag());
    }


	 public void test_ADC_Absolute() throws SimulationException
	 {
	        ram.write(0x10, 0x01);
	        ram.write(0x11, 0xff);

	        cpu.reset();
	        int[] program = {0xa9, 0x7f, // LDA #$7f
	                        0x6d, 0x11}; // ADC $11
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertEquals(0x7e, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x80,  // LDA #$80
	                        0x6d, 0x11};   // ADC $11
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertEquals(0x7f, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertTrue(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program3 = {0xa9, 0xff,  // LDA #$ff
	                        0x6d, 0x11};   // ADC $11
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertEquals(0xfe, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_ADC_Absolute_IncludesCarry() throws SimulationException
	    {
	        ram.write(0x10, 0x01);

	        int[] program = {0xa9, 0x00,     // LDA #$00
	                        0x38, 0x00,      // SEC
	                        0x6d, 0x10};     // ADC $10
	        ram.loadProgram(program);

	        cpu.step(3);
	        assertEquals(0x02, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());
	    }

	    public void test_ADC_Absolute_DecimalMode() throws SimulationException
	    {
	        ram.write(0x10, 0x01);
	        ram.write(0x11, 0x99);

	        cpu.reset();
	        int[] program = {0xf8, 0x00,      // SED
	                        0xa9, 0x00,       // LDA #$00
	                        0x6d, 0x11};      // ADC $10
	        ram.loadProgram(program);

	        cpu.step(3);
	        assertEquals(0x99, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xf8, 0x00,      // SED
	                        0xa9, 0x49,        // LDA #$49
	                        0x6d, 0x11};       // ADC $11
	        ram.loadProgram(program2);

	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program3 = {0xf8, 0x00,         // SED
	                        0xa9, 0x50,           // LDA #$59
	                        0x6d, 0x11};          // ADC $11
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x49, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteX() throws SimulationException
	    {
	        ram.write(0x40, 0x01);
	        ram.write(0x41, 0xff);

	        cpu.reset();
	        cpu.setX(0x30);


	        int[] program = {0xa9, 0x7f,    // LDA #$7f
	                        0x7d, 0x11};    // ADC $11,X
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertEquals(0x7e, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setX(0x30);
	        int[] program2 = {0xa9, 0x80,    // LDA #$80
	                        0x7d, 0x11};     // ADC $11,X
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertEquals(0x7f, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertTrue(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setX(0x30);
	        int[] program3 = {0xa9, 0xff,     // LDA #$ff
	                        0x7d, 0x11};      // ADC $11,X
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertEquals(0xfe, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteX_IncludesCarry() throws SimulationException
	    {
	        ram.write(0x40, 0x01);

	        int[] program = {0xa9, 0x00,      // LDA #$00
	                        0x38, 0x00,       // SEC
	                        0x7d, 0x10};      // ADC $10,X

	        ram.loadProgram(program);


	        cpu.setX(0x30);

	        cpu.step(3);
	        assertEquals(0x02, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteX_DecimalMode() throws SimulationException
	    {
	        ram.write(0x40, 0x01);
	        ram.write(0x41, 0x99);

	        cpu.reset();
	        cpu.setX(0x30);
	        int[] program = {0xf8, 0x00,      // SED
	                        0xa9, 0x00,       // LDA #$00
	                        0x7d, 0x11};      // ADC $10,X
	        ram.loadProgram(program);

	        cpu.step(3);
	        assertEquals(0x99, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setX(0x30);
	        int[] program2 = {0xf8, 0x00,     // SED
	                        0xa9, 0x49,       // LDA #$49
	                        0x7d, 0x11};      // ADC $11,X
	        ram.loadProgram(program2);

	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setX(0x30);
	        int[] program3 = {0xf8, 0x00,     // SED
	                        0xa9, 0x50,       // LDA #$59
	                        0x7d, 0x11};      // ADC $11,X
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x49, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteY() throws SimulationException
	    {
	        ram.write(0x40, 0x01);
	        ram.write(0x41, 0xff);

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program = {0xa9, 0x7f,           // LDA #$7f
	                        0x79, 0x11, 0xab};     // ADC $11,Y
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertEquals(0x7e, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program2 = {0xa9, 0x80,        // LDA #$80
	                        0x79, 0x11, 0xab};   // ADC $11,Y
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertEquals(0x7f, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertTrue(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program3 = {0xa9, 0xff,        // LDA #$ff
	                        0x79, 0x11, 0xab};   // ADC $11,Y
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertEquals(0xfe, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteY_IncludesCarry() throws SimulationException
	    {
	        ram.write(0x40, 0x01);

	        int[] program = {0xa9, 0x00,        // LDA #$00
	                        0x38, 0x00,         // SEC
	                        0x79, 0x10};        // ADC $10,Y
	        ram.loadProgram(program);


	        cpu.setY(0x30);

	        cpu.step(3);
	        assertEquals(0x02, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());
	    }

	    public void test_ADC_AbsoluteY_DecimalMode() throws SimulationException
	    {
	        ram.write(0x40, 0x01);
	        ram.write(0x41, 0x99);

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program = {0xf8, 0x00,       // SED
	                        0xa9, 0x00,        // LDA #$00
	                        0x79, 0x11};       // ADC $10,Y
	        ram.loadProgram(program);

	        cpu.step(3);
	        assertEquals(0x99, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program2 = {0xf8, 0x00,      // SED
	                        0xa9, 0x49,        // LDA #$49
	                        0x79, 0x11};       // ADC $11,Y
	        ram.loadProgram(program2);

	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        cpu.setY(0x30);

	        int[] program3 = {0xf8, 0x00,      // SED
	                        0xa9, 0x50,        // LDA #$59
	                        0x79, 0x11};       // ADC $11,Y
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x49, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    /*
	     * SBC tests
	     */

	    public void test_SBC_Immediate()  throws SimulationException
	    {
	        int[] program = {0xa9, 0x00,  // LDA #$00
	                        0xe9, 0x01};  // SBC #$01
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertEquals(0xfe, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x7f,  // LDA #$7f
	                        0xe9, 0x01};   // SBC #$01
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertEquals(0x7d, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program3 = {0xa9, 0x80,  // LDA #$80
	                        0xe9, 0x01};   // SBC #$01
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertEquals(0x7e, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertTrue(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program4 = {0xa9, 0xff,  // LDA #$ff
	                        0xe9, 0x01};   // SBC #$01
	        ram.loadProgram(program4);

	        cpu.step(2);
	        assertEquals(0xfd, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program5 = {0xa9, 0x02,  // LDA #$02
	                        0xe9, 0x01};   // SBC #$01
	        ram.loadProgram(program5);

	        cpu.step(2);
	        assertEquals(0x00, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_SBC_IncludesNotOfCarry_Immediate()  throws SimulationException
	    {
	        // Subtrace with Carry Flag cleared
	        int[] program = {0x18, 0x00,        // CLC
	                        0xa9, 0x05,         // LDA #$00
	                        0xe9, 0x01};        // SBC #$01
	        ram.loadProgram(program);


	        cpu.step(3);
	        assertEquals(0x03, cpu.getA());

	        cpu.reset();

	        // Subtrace with Carry Flag cleared
	        int[] program2 = {0x18, 0x00,       // CLC
	                        0xa9, 0x00,     	  // LDA #$00
	                        0xe9, 0x01};        // SBC #$01
	        ram.loadProgram(program2);

	        cpu.step(3);
	        assertEquals(0xfe, cpu.getA());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program3 = {0x38, 0x00,       // SEC
	                        0xa9, 0x05,         // LDA #$00
	                        0xe9, 0x01};        // SBC #$01
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program4 = {0x38, 0x00,       // SEC
	                        0xa9, 0x00,         // LDA #$00
	                        0xe9, 0x01};        // SBC #$01
	        ram.loadProgram(program4);

	        cpu.step(3);
	        assertEquals(0xff, cpu.getA());
	        assertFalse(cpu.getCarryFlag());

	    }

	    public void test_SBC_Immediate_DecimalMode()  throws SimulationException
	    {
	        int[] program = {0xf8, 0x00,
	                        0xa9, 0x00,
	                        0xe9, 0x01};
	        ram.loadProgram(program);


	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag()); // borrow = set flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program2 = {0xf8, 0x00,
	                        0xa9, 0x99,
	                        0xe9, 0x01};
	        ram.loadProgram(program2);

	        cpu.step(3);
	        assertEquals(0x97, cpu.getA());
	        assertTrue(cpu.getCarryFlag()); // No borrow = clear flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program3 = {0xf8, 0x00,
	                        0xa9, 0x50,
	                        0xe9, 0x01};
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());


	        cpu.reset();

	        int[] program4 = {0xf8, 0x00,         // SED
	                        0xa9, 0x02,  	        // LDA #$02
	                        0xe9, 0x01};          // SBC #$01
	        ram.loadProgram(program4);

	        cpu.step(3);
	        assertEquals(0x00, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program5 = {0xf8, 0x00,        // SED
	                        0xa9, 0x10,          // LDA #$10
	                        0xe9, 0x11};         // SBC #$11
	        ram.loadProgram(program5);

	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program6 = {0x38, 0x00,        // SEC
	                        0xf8, 0x00,         // SED
	                        0xa9, 0x05,         // LDA #$05
	                        0xe9, 0x01};        // SBC #$01
	        ram.loadProgram(program6);

	        cpu.step(4);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program7 = {0x38, 0x00,        // SEC
	                        0xf8, 0x00,          // SED
	                        0xa9, 0x00,          // LDA #$00
	                        0xe9, 0x01};         // SBC #$01
	        ram.loadProgram(program7);

	        cpu.step(4);
	        assertEquals(0x99, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());
	    }

	    public void test_SBC_Absolute()  throws SimulationException
	    {
	        ram.write(0x10, 0x01);

	        cpu.reset();
	        int[] program = {0xa9, 0xff,        // LDA #$ff
	                        0xed, 0x10};        // SBC $10
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertEquals(0xfd, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x02,        // LDA #$02
	                        0xed, 0x10};         // SBC $10
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertEquals(0x00, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_SBC_Absolute_IncludesNotOfCarry()  throws SimulationException
	    {
	        ram.write(0x10, 0x01);

	        // Subtrace with Carry Flag cleared
	        int[] program = {0x18, 0x00,              // CLC
	                        0xa9, 0x05,               // LDA #$00
	                        0xed, 0x10};              // SBC $10
	        ram.loadProgram(program);

	        cpu.step(3);
	        assertEquals(0x03, cpu.getA());

	        cpu.reset();

	        // Subtract with Carry Flag cleared
	        int[] program2 = {0x18, 0x00,          // CLC
	                        0xa9, 0x00,            // LDA #$00
	                        0xed, 0x10};           // SBC $10
	        ram.loadProgram(program2);


	        cpu.step(3);
	        assertEquals(0xfe, cpu.getA());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program3 = {0x38, 0x00,      // SEC
	                        0xa9, 0x05,        // LDA #$00
	                        0xed, 0x10};       // SBC $10
	        ram.loadProgram(program3);

	        cpu.step(3);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program4 = {0x38, 0x00,      // SEC
	                        0xa9, 0x00,        // LDA #$00
	                        0xed, 0x10};       // SBC $10
	        ram.loadProgram(program4);

	        cpu.step(3);
	        assertEquals(0xff, cpu.getA());
	        assertFalse(cpu.getCarryFlag());

	    }

	    public void test_SBC_Absolute_DecimalMode() throws SimulationException
			{
	        ram.write(0x10, 0x01);
	        ram.write(0x20, 0x11);

	        cpu.reset();

	        int[] program = {0x38, 0x00,        // SEC
	                        0xf8, 0x00,         // SED
	                        0xa9, 0x05,         // LDA #$05
	                        0xed, 0x10};        // SBC $10
	        ram.loadProgram(program);

	        cpu.step(4);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program2 = {0x38, 0x00,       // SEC
	                        0xf8, 0x00,         // SED
	                        0xa9, 0x00,         // LDA #$00
	                        0xed, 0x10};        // SBC $10
	        ram.loadProgram(program2);

	        cpu.step(4);
	        assertEquals(0x99, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());
	    }

	    public void test_SBC_AbsoluteX() throws SimulationException
			{
	        ram.write(0x40, 0x01);

	        cpu.reset();
	        int[] program = {0xa9, 0xff,        // LDA #$ff
	                        0xfd, 0x10};        // SBC $10,X
	        ram.loadProgram(program);

	        cpu.setX(0x30);
	        cpu.step(2);
	        assertEquals(0xfd, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x02,        // LDA #$02
	                        0xfd, 0x10};         // SBC $10,X
	        ram.loadProgram(program2);

	        cpu.setX(0x30);
	        cpu.step(2);
	        assertEquals(0x00, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_SBC_AbsoluteX_IncludesNotOfCarry() throws SimulationException
			{
	        ram.write(0x40, 0x01);

	        // Subtrace with Carry Flag cleared
	        int[] program = {0x18, 0x00,              // CLC
	                        0xa9, 0x05,               // LDA #$00
	                        0xfd, 0x10};              // SBC $10,X
	        ram.loadProgram(program);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x03, cpu.getA());

	        cpu.reset();

	        // Subtrace with Carry Flag cleared
	        int[] program2 = {0x18, 0x00,             // CLC
	                        0xa9, 0x00,               // LDA #$00
	                        0xfd, 0x10};              // SBC $10,X
	        ram.loadProgram(program2);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0xfe, cpu.getA());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program3 = {0x38, 0x00,             // SEC
	                        0xa9, 0x05,               // LDA #$00
	                        0xfd, 0x10};              // SBC $10,X
	        ram.loadProgram(program3);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program4 = {0x38, 0x00,             // SEC
	                        0xa9, 0x00,               // LDA #$00
	                        0xfd, 0x10};              // SBC $10,X
	        ram.loadProgram(program4);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0xff, cpu.getA());
	        assertFalse(cpu.getCarryFlag());

	    }

	    public void test_SBC_AbsoluteX_DecimalMode() throws SimulationException
			{
	        ram.write(0x40, 0x01);
	        ram.write(0x50, 0x11);

	        int[] program = {0xf8, 0x00,       // SED
	                        0xa9, 0x00,       // LDA #$00
	                        0xfd, 0x10};      // SBC $10,X
	        ram.loadProgram(program);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag()); // borrow = set flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program2 = {0xf8, 0x00,       // SED
	                        0xa9, 0x99,         // LDA #$99
	                        0xfd, 0x10};        // SBC $10,X
	        ram.loadProgram(program2);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x97, cpu.getA());
	        assertTrue(cpu.getCarryFlag()); // No borrow = clear flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program3 = {0xf8, 0x00,      // SED
	                        0xa9, 0x50,        // LDA #$50
	                        0xfd, 0x10};       // SBC $10,X
	        ram.loadProgram(program3);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());


	        cpu.reset();

	        int[] program4 = {0xf8, 0x00,       // SED
	                        0xa9, 0x02,         // LDA #$02
	                        0xfd, 0x10};        // SBC $10,X
	        ram.loadProgram(program4);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x00, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program5 = {0xf8, 0x00,       // SED
	                        0xa9, 0x10,         // LDA #$10
	                        0xfd, 0x20};        // SBC $20,X
	        ram.loadProgram(program5);

	        cpu.setX(0x30);
	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program6 = {0x38, 0x00,       // SEC
	                        0xf8, 0x00,         // SED
	                        0xa9, 0x05,         // LDA #$05
	                        0xfd, 0x10};        // SBC $10,X
	        ram.loadProgram(program6);

	        cpu.setX(0x30);
	        cpu.step(4);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program7 = {0x38, 0x00,       // SEC
	                        0xf8, 0x00,         // SED
	                        0xa9, 0x00,         // LDA #$00
	                        0xfd, 0x10};        // SBC $10,X
	        ram.loadProgram(program7);

	        cpu.setX(0x30);
	        cpu.step(4);
	        assertEquals(0x99, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());
	    }

	    public void test_SBC_AbsoluteY() throws SimulationException
			{
	        ram.write(0x40, 0x01);

	        int[] program = {0xa9, 0x00,        // LDA #$00
	                        0xf9, 0x10};        // SBC $10,Y
	        ram.loadProgram(program);

	        cpu.setY(0x30);
	        cpu.step(2);
	        assertEquals(0xfe, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertFalse(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x7f,        // LDA #$7f
	                        0xf9, 0x10};         // SBC $10,Y
	        ram.loadProgram(program2);

	        cpu.setY(0x30);
	        cpu.step(2);
	        assertEquals(0x7d, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program3 = {0xa9, 0x80,        // LDA #$80
	                        0xf9, 0x10};         // SBC $10,Y
	        ram.loadProgram(program3);

	        cpu.setY(0x30);
	        cpu.step(2);
	        assertEquals(0x7e, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertTrue(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program4 = {0xa9, 0xff,        // LDA #$ff
	                        0xf9, 0x10};         // SBC $10,Y
	        ram.loadProgram(program4);

	        cpu.setY(0x30);
	        cpu.step(2);
	        assertEquals(0xfd, cpu.getA());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();
	        int[] program5 = {0xa9, 0x02,        // LDA #$02
	                        0xf9, 0x10};          // SBC $10,Y
	        ram.loadProgram(program5);

	        cpu.setY(0x30);
	        cpu.step(2);
	        assertEquals(0x00, cpu.getA());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getCarryFlag());
	    }

	    public void test_SBC_AbsoluteY_IncludesNotOfCarry() throws SimulationException
			{
	        ram.write(0x40, 0x01);

	        // Subtract with Carry Flag cleared
	        int[] program = {0x18, 0x00,             // CLC
	                        0xa9, 0x05,              // LDA #$00
	                        0xf9, 0x10};             // SBC $10,Y
	        ram.loadProgram(program);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x03, cpu.getA());

	        cpu.reset();

	        // Subtrace with Carry Flag cleared
	        int[] program2 = {0x18, 0x00,             // CLC
	                        0xa9, 0x00,               // LDA #$00
	                        0xf9, 0x10};              // SBC $10,Y
	        ram.loadProgram(program2);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0xfe, cpu.getA());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program3 = {0x38, 0x00,             // SEC
	                        0xa9, 0x05,               // LDA #$00
	                        0xf9, 0x10};              // SBC $10,Y
	        ram.loadProgram(program3);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());

	        cpu.reset();

	        // Subtract with Carry Flag set
	        int[] program4 = {0x38, 0x00,            // SEC
	                        0xa9, 0x00,              // LDA #$00
	                        0xf9, 0x10};             // SBC $10,Y
	        ram.loadProgram(program4);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0xff, cpu.getA());
	        assertFalse(cpu.getCarryFlag());

	    }

	    public void test_SBC_AbsoluteY_DecimalMode() throws SimulationException
	    {

	    	ram.write(0x40, 0x01);
	        ram.write(0x50, 0x11);

	        cpu.reset();

	        int[] program = {0xf8, 0x00,              // SED
	                        0xa9, 0x00,               // LDA #$00
	                        0xf9, 0x10};              // SBC $10,Y
	        ram.loadProgram(program);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag()); // borrow = set flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program2 = {0xf8, 0x00,             // SED
	                        0xa9, 0x99,               // LDA #$99
	                        0xf9, 0x10};              // SBC $10,Y
	        ram.loadProgram(program2);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x97, cpu.getA());
	        assertTrue(cpu.getCarryFlag()); // No borrow = clear flag
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program3 = {0xf8, 0x00,             // SED
	                        0xa9, 0x50,               // LDA #$50
	                        0xf9, 0x10};              // SBC $10,Y
	        ram.loadProgram(program3);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x48, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());


	        cpu.reset();

	        int[] program4 = {0xf8, 0x00,              // SED
	                        0xa9, 0x02,                // LDA #$02
	                        0xf9, 0x10};               // SBC $10,Y
	        ram.loadProgram(program4);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x00, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program5 = {0xf8, 0x00,              // SED
	                        0xa9, 0x10,                // LDA #$10
	                        0xf9, 0x20};               // SBC $20,Y
	        ram.loadProgram(program5);

	        cpu.setY(0x30);
	        cpu.step(3);
	        assertEquals(0x98, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program6 = {0x38, 0x00,           // SEC
	                        0xf8, 0x00,             // SED
	                        0xa9, 0x05,             // LDA #$05
	                        0xf9, 0x10};            // SBC $10,Y
	        ram.loadProgram(program6);

	        cpu.setY(0x30);
	        cpu.step(4);
	        assertEquals(0x04, cpu.getA());
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());

	        cpu.reset();

	        int[] program7 = {0x38, 0x00,          // SEC
	                        0xf8, 0x00,            // SED
	                        0xa9, 0x00,            // LDA #$00
	                        0xf9, 0x10};           // SBC $10,Y
	        ram.loadProgram(program7);

	        cpu.setY(0x30);
	        cpu.step(4);
	        assertEquals(0x99, cpu.getA());
	        assertFalse(cpu.getCarryFlag());
	        assertTrue(cpu.getNegativeFlag());
	        assertFalse(cpu.getOverflowFlag());
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getDecimalModeFlag());
	    }

	    /*
	     * CMP tests
	     */

	    public void test_CMP_Immediate_SetsZeroAndCarryFlagsIfNumbersSame() throws SimulationException
			{
	        int[] program = {0xa9, 0x00,  // LDA #$00
	                        0xc9, 0x00}; // CMP #$00
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x01,  // LDA #$01
	                        0xc9, 0x01};   // CMP #$01
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program3 = {0xa9, 0x7f,   // LDA #$7F
	                        0xc9, 0x7f};    // CMP #$7F
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program4 = {0xa9, 0xFF,   // LDA #$FF
	                        0xc9, 0xFF};    // CMP #$FF
	        ram.loadProgram(program4);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CMP_Immediate_SetsCarryFlagIfYGreaterThanMemory() throws SimulationException
			{
	        int[] program = {0xa9, 0x0a,   // LDA #$0A
	                        0xc9, 0x08};   // CMP #$08
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $0a - $08 = positive
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0xfa,  // LDA #$FA
	                        0xc9, 0x80};   // CMP #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $fa - $80 = positive
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CMP_Immediate_DoesNotSetCarryFlagIfYGreaterThanMemory() throws SimulationException
			{
	        int[] program = {0xa9, 0x08,  // LDA #$08
	                        0xc9, 0x0a};  // CMP #$0A
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // 08 - 0a = negative
	        assertTrue(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa9, 0x70,  // LDA #$70
	                        0xc9, 0x80};  // CMP #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // 70 - 80 = negative
	        assertTrue(cpu.getNegativeFlag());
	    }

	    public void test_CMP_Absolute() throws SimulationException
			{
	        ram.write(0x10, 0x00);
	        ram.write(0x11, 0x80);
	        ram.write(0x12, 0xff);

	        cpu.setA(0x80);

	        int[] program = {0xcd, 0x10,  // CMP $10
	                        0xcd, 0x11,   // CMP $11
	                        0xcd, 0x12};  // CMP $12
	        ram.loadProgram(program);


	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m > y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // m - y < 0

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());     // m = y
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag()); // m - y == 0

	        cpu.step();
	        assertFalse(cpu.getCarryFlag());    // m < y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag());  // m - y > 0
	    }

	    public void test_CMP_AbsoluteX() throws SimulationException
			{
	        ram.write(0x40, 0x00);
	        ram.write(0x41, 0x80);
	        ram.write(0x42, 0xff);

	        cpu.setA(0x80);

	        int[] program = {0xdd, 0x10,   // CMP $10,X
	                        0xdd, 0x11,    // CMP $11,X
	                        0xdd, 0x12};   // CMP $12,X
	        ram.loadProgram(program);

	        cpu.setX(0x30);

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m > y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // m - y < 0

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m = y
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag()); // m - y == 0

	        cpu.step();
	        assertFalse(cpu.getCarryFlag());    // m < y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // $80 - $ff = $81
	    }

	    public void test_CMP_AbsoluteY() throws SimulationException
			{
	        ram.write(0x40, 0x00);
	        ram.write(0x41, 0x80);
	        ram.write(0x42, 0xff);

	        cpu.setA(0x80);

	        int[] program = {0xd9, 0x10,   // CMP $10,Y
	                        0xd9, 0x11,    // CMP $11,Y
	                        0xd9, 0x12};   // CMP $12,Y
	        ram.loadProgram(program);

	        cpu.setY(0x30);

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m > y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // m - y < 0

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m = y
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag()); // m - y == 0

	        cpu.step();
	        assertFalse(cpu.getCarryFlag());    // m < y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // $80 - $ff = $81
	    }

	    /*
	     * CPX Tests
	     */

	    public void test_CPX_Immediate_SetsZeroAndCarryFlagsIfNumbersSame() throws SimulationException
			{
	        int[] program = {0xa2, 0x00,   // LDX #$00
	                        0xe0, 0x00};   // CPX #$00
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa2, 0x01,  // LDX #$01
	                        0xe0, 0x01};   // CPX #$01
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program3 = {0xa2, 0x7f,   // LDX #$7F
	                        0xe0, 0x7f};    // CPX #$7F
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program4 = {0xa2, 0xFF,   // LDX #$FF
	                        0xe0, 0xFF};    // CPX #$FF
	        ram.loadProgram(program4);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CPX_Immediate_SetsCarryFlagIfYGreaterThanMemory() throws SimulationException
			{
	        int[] program = {0xa2, 0x0a,  // LDX #$0A
	                        0xe0, 0x08};  // CPX #$08
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $0a - $08 = positive
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa2, 0xfa,  // LDX #$FA
	                        0xe0, 0x80};   // CPX #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $fa - $80 = positive
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CPX_Immediate_DoesNotSetCarryFlagIfYGreaterThanMemory() throws SimulationException
			{
	        int[] program = {0xa2, 0x08,  // LDX #$08
	                        0xe0, 0x0a};  // CPX #$0A
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $08 - $0a = negative
	        assertTrue(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa2, 0x70,  // LDX #$70
	                        0xe0, 0x80};   // CMX #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $70 - $80 = negative
	        assertTrue(cpu.getNegativeFlag());
	    }

	    public void test_CPX_Absolute() throws SimulationException
			{
	        ram.write(0x10, 0x00);
	        ram.write(0x11, 0x80);
	        ram.write(0x12, 0xff);

	        cpu.setX(0x80);

	        int[] program = {0xec, 0x10,  // CPX $10
	                        0xec, 0x11,   // CPX $11
	                        0xec, 0x12};  // CPX $12
	        ram.loadProgram(program);

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m > y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // m - y < 0

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m = y
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag()); // m - y == 0

	        cpu.step();
	        assertFalse(cpu.getCarryFlag());    // m < y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // $80 - $ff = 81
	    }

	    /*
	     * CPY Tests
	     */

	    public void test_CPY_Immediate_SetsZeroAndCarryFlagsIfNumbersSame() throws SimulationException
			{
	        int[] program = {0xa0, 0x00,  // LDY #$00
	                        0xc0, 0x00};  // CPY #$00
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa0, 0x01,  // LDY #$01
	                        0xc0, 0x01};   // CPY #$01
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program3 = {0xa0, 0x7f,   // LDY #$7F
	                        0xc0, 0x7f};    // CPY #$7F
	        ram.loadProgram(program3);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program4 = {0xa0, 0xFF,   // LDY #$FF
	                        0xc0, 0xFF};    // CPY #$FF
	        ram.loadProgram(program4);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CPY_Immediate_SetsCarryFlagIfYGreaterThanMemory() throws SimulationException
			{
	        int[] program = {0xa0, 0x0a,  // LDY #$0A
	                        0xc0, 0x08};  // CPY #$08
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $0a - $08 = positive
	        assertFalse(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa0, 0xfa,  // LDY #$FA
	                        0xc0, 0x80};   // CPY #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertTrue(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $fa - 80 = positive
	        assertFalse(cpu.getNegativeFlag());
	    }

	    public void test_CPY_Immediate_DoesNotSetCarryFlagIfYLessThanThanMemory() throws SimulationException
			{
	        int[] program = {0xa0, 0x08,  // LDY #$08
	                        0xc0, 0x0a};  // CPY #$0A
	        ram.loadProgram(program);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // 08 - 0a = negative
	        assertTrue(cpu.getNegativeFlag());

	        cpu.reset();
	        int[] program2 = {0xa0, 0x70,  // LDY #$70
	                        0xc0, 0x80};   // CPY #$80
	        ram.loadProgram(program2);

	        cpu.step(2);
	        assertFalse(cpu.getCarryFlag());
	        assertFalse(cpu.getZeroFlag());
	        // $70 - $80 = negative
	        assertTrue(cpu.getNegativeFlag());
	    }

	    public void test_CPY_Absolute() throws SimulationException
			{
	        ram.write(0x10, 0x00);
	        ram.write(0x11, 0x80);
	        ram.write(0x12, 0xff);

	        cpu.setY(0x80);

	        int[] program = {0xcc, 0x10,  // CPY $10
	                        0xcc, 0x11,   // CPY $11
	                        0xcc, 0x12};  // CPY $12
	        ram.loadProgram(program);


	        cpu.step();
	        assertTrue(cpu.getCarryFlag());    // m > y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag()); // m - y < 0

	        cpu.step();
	        assertTrue(cpu.getCarryFlag());     // m = y
	        assertTrue(cpu.getZeroFlag());
	        assertFalse(cpu.getNegativeFlag()); // m - y == 0

	        cpu.step();
	        assertFalse(cpu.getCarryFlag());    // m < y
	        assertFalse(cpu.getZeroFlag());
	        assertTrue(cpu.getNegativeFlag());  // m - y = $81
	    }
}
