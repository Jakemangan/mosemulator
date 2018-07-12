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
 * ASL - 0x0A A
 * ASL - 0x0E Absolute
 * ASL - 0x1E Absolute,X
 *
 * LSR - 0x4A A
 * LSR - 0x4E Absolute
 * LSR - 0x5E Absolute,X
 *
 * ROL - 0x2A A
 * ROL - 0x2E Absolute
 * ROL - 0x3E Absolute,X
 *
 * ROR - 0x6A A
 * ROR - 0x6E Absolute
 * ROR - 0x7E Absolute,X
 */

public class CpuShiftOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuShiftOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuShiftOperationsTest.class);
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
	 * ASL Tests
	 */

	public void test_ASL_Accumulator() throws SimulationException{
        int[] program = {0xa9, 0x00,  // LDA #$00
                        0x0a,  0x00,      // ASL A

                        0xa9, 0x01,  // LDA #$01
                        0x0a, 0x00,       // ASL A

                        0xa9, 0x02,  // LDA #$02
                        0x0a, 0x00,       // ASL A

                        0xa9, 0x44,  // LDA #$44
                        0x0a, 0x00,       // ASL A

                        0xa9, 0x80,  // LDA #$80
                        0x0a, 0x00};       // ASL A
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x02, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x04, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x88, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());
    }

	public void test_ASL_Absolute() throws SimulationException{
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x01);
        ram.write(0x12, 0x02);
        ram.write(0x13, 0x44);
        ram.write(0x14, 0x80);

        int[] program = {0x0e, 0x10,  // ASL $10
                        0x0e, 0x11,  // ASL $11
                        0x0e, 0x12,  // ASL $12
                        0x0e, 0x13,  // ASL $13
                        0x0e, 0x14}; // ASL $14
        ram.loadProgram(program);


        cpu.step();
        assertEquals(0x00, ram.read(0x10));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x11));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x04, ram.read(0x12));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x88, ram.read(0x13));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x14));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());
    }

	public void test_ASL_AbsoluteX() throws SimulationException{
        ram.write(0x30, 0x00);
        ram.write(0x31, 0x01);
        ram.write(0x32, 0x02);
        ram.write(0x33, 0x44);
        ram.write(0x34, 0x80);

        // Set offset in X .
        cpu.setX(0x30);

        int[] program = {0x1e, 0x00,  // ASL $00,X
                        0x1e, 0x01,  // ASL $01,X
                        0x1e, 0x02,  // ASL $02,X
                        0x1e, 0x03,  // ASL $03,X
                        0x1e, 0x04}; // ASL $04,X
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x30));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x04, ram.read(0x32));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x88, ram.read(0x33));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x34));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());
    }

	/*
	 * LSR Tests
	 */

	public void test_LSR_Accumulator() throws SimulationException {
        int[] program = {0xa9, 0x00,  // LDA #$00
                        0x4a, 0x00,       // LSR A

                        0xa9, 0x01,  // LDA #$01
                        0x4a, 0x00,       // LSR A

                        0xa9, 0x02,  // LDA #$02
                        0x4a, 0x00,       // LSR A

                        0xa9, 0x44,  // LDA #$44
                        0x4a, 0x00,       // LSR A

                        0xa9, 0x80,  // LDA #$80
                        0x4a, 0x00,       // LSR A

                        0x38, 0x00,       // SEC
                        0xa9, 0x02,  // LDA #$02
                        0x4a, 0x00};       // LSR $05
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x22, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x40, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        // Setting Carry should not affect the result.
        cpu.step(3);
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_LSR_Absolute() throws SimulationException{
        ram.write(0x10, 0x00);
        ram.write(0x11, 0x01);
        ram.write(0x12, 0x02);
        ram.write(0x13, 0x44);
        ram.write(0x14, 0x80);
        ram.write(0x15, 0x02);

        int[] program = {0x4e, 0x10,  // LSR $00
                        0x4e, 0x11,  // LSR $01
                        0x4e, 0x12,  // LSR $02
                        0x4e, 0x13,  // LSR $03
                        0x4e, 0x14,  // LSR $04
                        0x38, 0x00,   // SEC
                        0x4e, 0x15}; // LSR $05
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x10));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x11));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x01, ram.read(0x12));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x22, ram.read(0x13));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x14));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        // Setting Carry should not affect the result.
        cpu.step(2);
        assertEquals(0x01, ram.read(0x15));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_LSR_AbsoluteX() throws SimulationException{
        ram.write(0x30, 0x00);
        ram.write(0x31, 0x01);
        ram.write(0x32, 0x02);
        ram.write(0x33, 0x44);
        ram.write(0x34, 0x80);
        ram.write(0x35, 0x02);

        cpu.setX(0x30);

        int[] program = {0x5e, 0x00,  // LSR $00,X
                        0x5e, 0x01,  // LSR $01,X
                        0x5e, 0x02,  // LSR $02,X
                        0x5e, 0x03,  // LSR $03,X
                        0x5e, 0x04,  // LSR $04,X
                        0x38, 0x00,             // SEC
                        0x5e, 0x05}; // LSR $05,X
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x30));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x31));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x01, ram.read(0x32));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x22, ram.read(0x33));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x34));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        // Setting Carry should not affect the result.
        cpu.step(2);
        assertEquals(0x01, ram.read(0x35));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	/*
	 * ROL Tests
	 */

	public void test_ROL_A() throws SimulationException{
        int[] program = {0xa9, 0x00,       // LDA #$00
                        0x2a, 0x00,        // ROL A
                        0xa9, 0x01,        // LDA #$01
                        0x2a, 0x00,        // ROL A
                        0x38, 0x00,        // SEC
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00,        // ROL A
                        0x2a, 0x00};       // ROL A
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x02, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x05, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x0a, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x14, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x28, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x50, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0xa0, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x81, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_ROL_Absolute() throws SimulationException{

        ram.write(0x30, 0x00);
        ram.write(0x31, 0x01);

        int[] program = {0x2e, 0x30, // ROL $30
                        0x2e, 0x31,  // ROL $31
                        0x38, 0x00,  // SEC
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31,  // ROL $31
                        0x2e, 0x31}; // ROL $31
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x30));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x05, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x0a, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x14, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x28, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x50, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0xa0, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x81, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_ROL_AbsoluteX() throws SimulationException{
        ram.write(0x70, 0x00);
        ram.write(0x71, 0x01);

        // Set offset in X
        cpu.setX(0x70);

        int[] program = {0x3e, 0x00,  // ROL $00,X
                        0x3e, 0x01,  // ROL $01,X
                        0x38, 0x00,  // SEC
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01,  // ROL $01,X
                        0x3e, 0x01, // ROL $01,X
                        0x3e, 0x01}; // ROL $01,X
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x70));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x05, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x0a, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x14, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x28, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x50, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0xa0, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x81, ram.read(0x71));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_ROR_Accumulator() throws SimulationException{
        int[] program = {0xa9, 0x00,       // LDA #$00
                        0x6a, 0x00,        // ROR A
                        0xa9, 0x10,        // LDA #$10
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,        // ROR A
                        0x6a, 0x00,};      // ROR A
        ram.loadProgram(program);

        cpu.step(2);
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step(2);
        assertEquals(0x08, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x04, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x01, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, cpu.getA());
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x80, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x20, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x10, cpu.getA());
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_ROR_Absolute() throws SimulationException{
		ram.write(0x30, 0x00);
        ram.write(0x31, 0x10);

        int[] program = {0x6e, 0x30, // ROR $30
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31,  // ROR $31
                        0x6e, 0x31}; // ROR $31
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x00, ram.read(0x30));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x08, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x04, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x01, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x31));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x80, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x20, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x10, ram.read(0x31));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

	public void test_ROR_AbsoluteX() throws SimulationException{

        ram.write(0x40, 0x00);
        ram.write(0x41, 0x10);

        int[] program = {0x7e, 0x10,   // ROR $10
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11,   // ROR $11
                        0x7e, 0x11}; // ROR $11 
        ram.loadProgram(program);

        cpu.setX(0x30);

        cpu.step();
        assertEquals(0x00, ram.read(0x40));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x08, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x04, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x02, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x01, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x00, ram.read(0x41));
        assertTrue(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertTrue(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x80, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertTrue(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x40, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x20, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());

        cpu.step();
        assertEquals(0x10, ram.read(0x41));
        assertFalse(cpu.getZeroFlag());
        assertFalse(cpu.getNegativeFlag());
        assertFalse(cpu.getCarryFlag());
    }

}
