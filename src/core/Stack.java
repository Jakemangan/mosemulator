package core;

import java.util.Arrays;

import util.Utils;

/*
 * 64KB stack structure
 * FIFO, grows from higher memory to lower memory. 
 * POP = increase SP. PUSH = decrease SP.
 * 
 * TODO: May need to remove stackpointer from this class and implement a register version instead. Otherwise 
 * inconsistencies between what is represented by a register class and what is just an int.
 */

public class Stack {

	private CPU cpu;
	
	private int[] stack;
	
	/*Initalise all memory addresses to 0x00*/
	private static final int DEFAULT_FILL = 0x00; 
	
	public Stack()
	{
		stack = new int[64]; 
		fill(DEFAULT_FILL);
	}
	
	public void push(int sp, int data)
	{	
		stack[sp] = data;
		System.out.println(data + " pushed to stack position " + (sp));
	}
	
	public int pop(int sp)
	{
		int popVal = stack[sp];
		stack[sp] = 0;
		System.out.println(popVal + " popped from stack position " + (sp));
		return popVal;
	}
	
	public void fill(int val)
	{
		Arrays.fill(this.stack, val);
		System.out.println("[Stack] Stack contents set to: " + val);
	}

	public void showAllStack()
	{
		System.out.println("[Stack] Show all stack");
		for(int i = 0; i < stack.length; i++)
		{
			String address = Utils.byteToHex(i);
			int data = stack[i];
			System.out.println(address + ": " + data);		
		}
	}
	
	public void showAllStackWithPointer(int sp)
	{
		System.out.println("[Stack] Show all stack");
		for(int i = 0; i < stack.length; i++)
		{
			if(i == sp)
			{
				String address = Utils.byteToHex(i);
				int data = stack[i];
				System.out.println(address + ": " + data + " <- SP");
			}
			else
			{
				String address = Utils.byteToHex(i);
				int data = stack[i];
				System.out.println(address + ": " + data);
			}
		}
	}
	
	public int getStackValue(int sp)
	{
		return stack[sp];
	}
	
	public CPU getCpu() 
	{
		return cpu;
	}
	
	public void setCpu(CPU cpu)
	{
		this.cpu = cpu;
	}
	
}
