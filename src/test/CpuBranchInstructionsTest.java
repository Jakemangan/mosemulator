package test;


import core.CPU;
import core.RAM;
import core.Stack;
import exceptions.SimulationException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CpuBranchInstructionsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuBranchInstructionsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuBranchInstructionsTest.class);
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

	public void test_BPL() throws SimulationException
	{
        // Positive Offset
        int[] program = {0x10, 0x05};  // BPL $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setNegativeFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program2 = {0x10, 0x05};  // BPL $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearNegativeFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0x10, 0xfb};  // BPL $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setNegativeFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program4 = {0x10, 0xfb};  // BPL $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearNegativeFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());
    }

    public void test_BMI() throws SimulationException
		{
        // Positive Offset
        int[] program = {0x30, 0x05};  // BMI $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setNegativeFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        cpu.reset();
        int[] program2 = {0x30, 0x05};  // BMI $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearNegativeFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0x30, 0xfb};  // BMI $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setNegativeFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());

        cpu.reset();
        int[] program4 = {0x30, 0xfb};  // BMI $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearNegativeFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());
    }


    public void test_BVC() throws SimulationException
		{
        // Positive Offset
        int[] program = {0x50, 0x05};  // BVC $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setOverflowFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program2 = {0x50, 0x05};  // BVC $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearOverflowFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0x50, 0xfb};  // BVC $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setOverflowFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program4 = {0x50, 0xfb};  // BVC $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearOverflowFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());
    }


    public void test_BVS() throws SimulationException
		{
        // Positive Offset
        int[] program = {0x70, 0x05};  // BVS $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setOverflowFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        cpu.reset();
        int[] program2 = {0x70, 0x05};  // BVS $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearOverflowFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0x70, 0xfb};  // BVS $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setOverflowFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());

        cpu.reset();
        int[] program4 = {0x70, 0xfb};  // BVS $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearOverflowFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());
    }



    public void test_BCC() throws SimulationException
		{
        // Positive Offset
        int[] program = {0x90, 0x05};  // BCC $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setCarryFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program2 = {0x90, 0x05};  // BCC $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearCarryFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0x90, 0xfb};  // BCC $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setCarryFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program4 = {0x90, 0xfb};  // BCC $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearCarryFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());
    }



    public void test_BCS() throws SimulationException
		{
        // Positive Offset
        int[] program = {0xb0, 0x05};  // BCS $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setCarryFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        cpu.reset();
        int[] program2 = {0xb0, 0x05};  // BCS $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearCarryFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0xb0, 0xfb};  // BCS $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setCarryFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());

        cpu.reset();
        int[] program4 = {0xb0, 0xfb};  // BCS $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearCarryFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());
    }

  /* BNE - Branch if Not Equal to Zero - 0xd0 */

    public void test_BNE() throws SimulationException
		{
        // Positive Offset
        int[] program = {0xd0, 0x05};  // BNE $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setZeroFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program2 = {0xd0, 0x05};  // BNE $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearZeroFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0xd0, 0xfb};  // BNE $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setZeroFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        cpu.reset();
        int[] program4 = {0xd0, 0xfb};  // BNE $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearZeroFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());
    }

  /* BEQ - Branch if Equal to Zero     - 0xf0 */

    public void test_BEQ() throws SimulationException
		{
        // Positive Offset
        int[] program = {0xf0, 0x05};  // BEQ $05 ; *=$02+$05 ($07)
        ram.loadProgram(program);
        cpu.setZeroFlag();
        cpu.step();
        assertEquals(0x07, cpu.getPc());

        cpu.reset();
        int[] program2 = {0xf0, 0x05};  // BEQ $05 ; *=$02+$05 ($07)
        ram.loadProgram(program2);
        cpu.clearZeroFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());

        // Negative Offset
        cpu.reset();
        int[] program3 = {0xf0, 0xfb};  // BEQ $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program3);
        cpu.setZeroFlag();
        cpu.step();
        assertEquals(0xfd, cpu.getPc());

        cpu.reset();
        int[] program4 = {0xf0, 0xfb};  // BEQ $fb ; *=$02-$05 ($fd)
        ram.loadProgram(program4);
        cpu.clearZeroFlag();
        cpu.step();
        assertEquals(0x02, cpu.getPc());
    }
}
