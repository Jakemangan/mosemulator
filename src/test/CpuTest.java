package test;

import core.CPU;
import core.RAM;
import core.Stack;
import junit.framework.*;


public class CpuTest extends TestCase{

	protected CPU cpu;
	protected RAM ram;
	protected Stack stack;

	public CpuTest(String testName)
	{
		super(testName);
	}

	public static Test suite()
	{
		return new TestSuite(CpuTest.class);
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

	public void testReset()
	{
		assertEquals(0, cpu.getA());
		assertEquals(0, cpu.getX());
		assertEquals(0, cpu.getY());
		assertEquals(0, cpu.getPc());
		assertEquals(0, cpu.getIr());
		assertEquals(0, cpu.getIrOperand());
		assertEquals(0, cpu.getNextIr());
		assertEquals(0, cpu.getNextIrOperand());
		assertFalse(cpu.getCarryFlag());
		assertFalse(cpu.getZeroFlag());
		assertFalse(cpu.getIrqDisableFlag());
		assertFalse(cpu.getOverflowFlag());
		assertFalse(cpu.getNegativeFlag());
		assertFalse(cpu.getBreakFlag());
		assertFalse(cpu.getDecimalModeFlag());
	}

	public void testStack()
	{
		cpu.reset();

		cpu.stackPush(0xff);
		assertEquals(0xff, cpu.stackPop());

		for(int i = 0; i < 0x40; i++)
		{
			cpu.stackPush(i);
		}

		for(int i = 0x3f; i <= 0x00; i--)
		{
			assertEquals(i, cpu.stackPop());
		}


	}

	public void testStackPush()
	{
		System.out.println("testStackPush");
		cpu.reset();

		assertEquals(0x3f, cpu.getSp());
		assertEquals(0x00, stack.getStackValue(cpu.getSp()));

		cpu.stackPush(0x06);
		assertEquals(0x3e, cpu.getSp());
		assertEquals(0x06, stack.getStackValue(cpu.getSp()+1));

		cpu.stackPush(0x05);
		assertEquals(0x3d, cpu.getSp());
		assertEquals(0x06, stack.getStackValue(cpu.getSp()+2));
		assertEquals(0x05, stack.getStackValue(cpu.getSp()+1));

		cpu.stackPush(0x04);
		assertEquals(0x3c, cpu.getSp());
		assertEquals(0x06, stack.getStackValue(cpu.getSp()+3));
		assertEquals(0x05, stack.getStackValue(cpu.getSp()+2));
		assertEquals(0x04, stack.getStackValue(cpu.getSp()+1));

		stack.showAllStackWithPointer(cpu.getSp());

	}

	public void testStackPop()
	{
		cpu.reset();
		cpu.stackPush(0x01);
		cpu.stackPush(0x02);
		cpu.stackPush(0x03);
		cpu.stackPush(0x04);

		stack.showAllStack();

		assertEquals(0x04, cpu.stackPop());
		assertEquals(0x03, cpu.stackPop());
		assertEquals(0x02, cpu.stackPop());
		assertEquals(0x01, cpu.stackPop());
	}

	public void testStackPointerWraps()
	{
		cpu.reset();
		cpu.setSp(0x01);

		cpu.stackPush(0xff);
		assertEquals(0x00, cpu.getSp());
		cpu.stackPush(0xff);
		assertEquals(0x3f, cpu.getSp());
		cpu.stackPop();
		assertEquals(0x00, cpu.getSp());
		cpu.stackPop();
		assertEquals(0x01, cpu.getSp());
		cpu.stackPop();
		assertEquals(0x02, cpu.getSp());
		cpu.stackPush(0xff);
		assertEquals(0x01, cpu.getSp());
		cpu.stackPush(0xff);
		assertEquals(0x00, cpu.getSp());
		cpu.stackPush(0xff);
		assertEquals(0x3f, cpu.getSp());
		cpu.stackPush(0xff);
		assertEquals(0x3e, cpu.getSp());

		stack.showAllStackWithPointer(cpu.getSp());
	}
}
