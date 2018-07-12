package test;



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
 * JMP - 0x4C Absolute
 * JMP - 0x6C Indirect
 *
 * JSR - 0x20 Absolute
 *
 * RTS - 0x60 Implied
 */

public class CpuJumpsCallsOperationsTest extends TestCase
{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuJumpsCallsOperationsTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuJumpsCallsOperationsTest.class);
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

	public void test_JMP_Absolute() throws SimulationException
	{
        int[] program = {0x4c, 0x34};
        ram.loadProgram(program);
        cpu.step();
        assertEquals(0x34, cpu.getPc());
        // No change to status flags.
        assertEquals(0x20, cpu.getProcessorStatusFlags());
    }

	public void test_JMP_Indirect() throws SimulationException
	{
        ram.write(0x34, 0x64);

        int[] program = {0x6c, 0x34};
        ram.loadProgram(program);

        cpu.step();
        assertEquals(0x64, cpu.getPc());
        // No change to status flags.
        assertEquals(0x20, cpu.getProcessorStatusFlags());
    }

	public void test_JSR_Absolute() throws SimulationException
	{
        int[] program = {0xea, 0x00,             // NOP
                        0xea, 0x00,             // NOP
                        0x20, 0x34};            // JSR $34
        ram.loadProgram(program);

        cpu.step(3);

        // New PC should be 0x34
        assertEquals(0x34, cpu.getPc());

        // Old PC-2 should be on stack, with value 4.
        // Each opcode:operand pair increments PC by two.
        // Two NOP instructions cause PC to be incremented to 4, which is the value that is pushed to the stack
        assertEquals(0x04, cpu.stackPop());

        // No flags should have changed.
        assertEquals(0x20, cpu.getProcessorStatusFlags());
    }

	public void test_RTS_Implied() throws SimulationException
	{
        cpu.stackPush(0x22); // PC value pushed to stack

        int[] program = {0x60, 0x00};
        ram.loadProgram(program);
        cpu.step();

        assertEquals(0x24, cpu.getPc()); //Old PC value + 2

        //Flags should not be affected
        assertEquals(0x20, cpu.getProcessorStatusFlags());
    }
}
