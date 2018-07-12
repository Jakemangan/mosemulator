package core;

import java.util.Arrays;

import gui.GUI2;
import util.Utils;

/*
 * TODO: Add exceptions for memory access outside of mem range, might not need if RAM size is unable to be changed by user.
 */

public class RAM {

	private GUI2 gui; 
	
	private CPU cpu;
	
	private int[] memory;
	
	/*Initalise all memory addresses to 0x00*/
	private static final int DEFAULT_FILL = 0x00; 
	
	public RAM()
	{
		memory = new int[256]; 
		fill(DEFAULT_FILL);
		System.out.println("[RAM] RAM initalised and filled to DEFAULT_FILL");
	}
	
	public void write(int address, int data)
	{
		if(address > 256 || address < 0)
		{
			System.out.println("[RAM] " + address + " address outside of RAM range, write failed.");
		}
		else
		{
			memory[address] = data;
			System.out.println(data + " written to 0x" + Utils.DecToHex(address));
		}
		
	}
	
	
	public int read(int address)
	{
		if(address > 255 || address < 0)
		{
			System.out.println("[RAM] " + address + " address outside of RAM range, read failed.");
			return -1;
		}
		
		int data = memory[address];
		System.out.println("[RAM] Read address 0x" + Utils.byteToHex(address) + ": " + data);
		return data;
	}
	
	public void fill(int val)
	{
		Arrays.fill(this.memory, val);
		System.out.println("[RAM] Ram contents set to: " + val);
	}
	
	public void loadProgram(int[] program)
	{
		System.out.println("[RAM] START LOADING PROGRAM");
		
		int writeAddress = 0;
		for(int i : program)
		{
			int programByte = i;
			write(writeAddress, programByte);
			writeAddress++;
		}
		
		System.out.println("[RAM] END LOADING PROGRAM");
	}
	
	public void loadDirectives(String[] directives)
	{
		System.out.println("[RAM] START LOADING DIRECTIVES");
		
		for(int i = 0; i < directives.length; i+=2) //Incremented by two since each address:value pair takes up two array elements
		{
			//Parse binary strings to 8-bit int
			int writeAddress = Integer.parseInt(directives[i], 2);
			int writeValue = Integer.parseInt(directives[i+1], 2);
			
			if(writeAddress > 0xff) //if the write address is greater than 255 
			{
				//Prevent from writing to memory
				System.out.println("Writing value " + Utils.DecToHex(writeValue) + " to address " + Utils.DecToHex(writeAddress) + " failed due to address being greater than 0xFF (255");
				appendGuiOutput("Writing value " + Utils.DecToHex(writeValue) + " to address " + Utils.DecToHex(writeAddress) + " failed due to address being greater than 0xFF (255");
			}
			else
			{
				write(writeAddress, writeValue);
			}
		}
		
		System.out.println("[RAM] END LOADING DIRECTIVES\n");
	}
	
	
	public void showAllMemory()
	{
		System.out.println("\n[RAM] Show all memory");
		for(int i = 0; i < memory.length; i++)
		{
			String address = Utils.byteToHex(i);
			int data = memory[i];
			System.out.println("0x" + address + ": " + data);		
		}
	}
	
	/*Displays all non-zero memory addresses in RAM*/
	public void showTrimmedMemory()
	{
		System.out.println("\n[RAM] Show trimmed memory");
		for(int i = 0; i < memory.length; i++)
		{
			String address = Utils.byteToHex(i);
			int data = memory[i];
			
			if(data != 0)
			{
				System.out.println("0x" + address + ": " + data);		
			}
		}
	}
	
	public int getAddressContents(int address)
	{
		return memory[address];
	}
	
	
	
	
	
	
	/*
	 * G + S
	 */
	public int getRamSize()
	{
		return memory.length;
	}
	
	public CPU getCpu() 
	{
		return cpu;
	}
	
	public void setCpu(CPU cpu)
	{
		this.cpu = cpu;
	}
	
	public void setGui(GUI2 gui)
	{
		this.gui = gui;
	}
	
	public void setGuiOutput(String msg)
	{
		gui.getAssemblerTextArea().setText(msg);
	}
	
	public void appendGuiOutput(String msg)
	{
		gui.getAssemblerTextArea().append("\n\n" + msg);
	}
	
}
