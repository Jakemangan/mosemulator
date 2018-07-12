package core;

/*
 * The Bus class ties together the simulated CPU and 
 * memory, allowing the components to talk to each other. 
 */

public class Bus {

	/*
	 * The simulation is limited to 256 bytes of memory not only 
	 * in memory range but also in bus size.
	 * For this reason the valid memory range of the simulation
	 * is from 0x00 - 0xFF
	 */
	private int startAddress = 0x00;
	private int endAddress = 0xFF;
	
	private CPU cpu;
	
	private RAM ram;
	
	/*
	 * Constructor
	 */
	public Bus()
	{

	}

	public void addCpu(CPU cpu)
	{
		this.cpu = cpu;;
	}
	
	public void addRam(RAM ram)
	{
		this.ram = ram;
	}
	
	public int getStartAddress() {
		return startAddress;
	}

	public int getEndAddress() {
		return endAddress;
	}

}
