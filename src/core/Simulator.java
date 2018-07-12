package core;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import compiler.Compiler;
import compiler.ReadAssemblyFile;
import exceptions.CompilerException;
import exceptions.SimulationException;
import gui.GUI2;
import gui.GuiAbout;

/*
 * The Simulator class holds the MOS6502 simulated machine and 
 * the GUI, allowing the two to communicate with each other.
 * 
 */




public class Simulator {
	
	String assemblyFilepath = "./assembly_input.txt/";
	String binaryFilepath = "./binary_output.txt/"; 
	
	private int stepsPerClick = 1;
	
	private MOS6502 mos6502;
	private GUI2 gui;
	
	private JButton btnRun;
	private JButton btnStep;
	private JButton btnReset;
	private JComboBox comboBox;
	
	private JMenuItem menuClearMemory, menuClearStack, menuQuit, menuResetCpu, menuClearEditor, menuRunAssembler, menuHelp, menuAbout, menuClearOutputConsole;
	private JScrollPane memoryScrollPane, stackScrollPane;
	private JTextArea editorTextArea;
	
	private PrintWriter writer;
	
	
	
	/*
	 * 
	 */
	public Simulator() 
	{
		mos6502 = new MOS6502();
		
	}
	
	/*
	 * Loads the program held inside the binary_output.txt file inside the
	 * project directory. Makes use of the ReadBinary class to read in the 
	 * binary patterns representing the program data, and then calls the 
	 * loadRAM method inside the mos6502 simulation.
	 * 
	 * This method will prevent the program from being loaded into memory 
	 * if the size of the program exceeds 256 bytes. 
	 */
	public void loadProgram()
	{
		ReadBinary rb = new ReadBinary(binaryFilepath);
		int[] program = rb.getProgram();
		String[] directiveValues = rb.getDirectiveValues();

		if((program.length + directiveValues.length) > 256)
			System.out.println("Program is too large to fit into 256 bytes of memory");
		else
		{
			mos6502.loadRAM(program);
			mos6502.loadDirectives(directiveValues);
		}
		
	}
	

	/*
	 * The actual creation of the gui is done through the GUI2 class. This method
	 * links several Swing objects to the actual Swing objects contained within
	 * the GUI2 class, this allows the Simulator class to directly modify the 
	 * contents of the GUI by calling methods on the Swing variables contained within
	 * the Simulator class.
	 * 
	 * This method also sets up ActionListeners on each component of the GUI that requires them,
	 * and will handle each user input accordingly.
	 */
	public void createAndShowGUI()
	{
		gui = new GUI2();
		gui.setSimMachine(mos6502);
		gui.refreshGui();
		gui.setCarets(0);
		
		btnRun = gui.getBtnRun();
		btnStep = gui.getBtnStep();
		btnReset = gui.getBtnReset();
		comboBox = gui.getComboBox();
		
		menuHelp = gui.getMenuHelp();
		menuAbout = gui.getMenuAbout();
		menuClearMemory = gui.getMenuClearMemory();
		menuClearStack = gui.getMenuClearStack();
		menuQuit = gui.getMenuQuit();
		menuResetCpu = gui.getMenuResetCpu();
		menuClearEditor = gui.getMenuClearEditor();
		menuRunAssembler = gui.getMenuRunAssembler();
		menuClearOutputConsole = gui.getMenuClearOutputConsole();
		
		memoryScrollPane = gui.getMemMapScrollPane();
		stackScrollPane = gui.getStackScrollPane();
		
		editorTextArea = gui.getEditorTextArea();
		
		
		/*
		 * Add listener to comboBox, change stepsPerClick to value selected in comboBox
		 */
		comboBox.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					JComboBox comboBox = (JComboBox) e.getSource();
					stepsPerClick = Integer.parseInt((String) comboBox.getSelectedItem());
					System.out.println("Steps per click set to:" + stepsPerClick);
				}
				catch(NumberFormatException ex)
				{
					stepsPerClick = 1;
					comboBox.setSelectedIndex(0);
				}
			}
		});
		
		/*
		 * Add listener to step button, causes stepsPerClick number of steps when clicked
		 */
		btnStep.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				Simulator.this.handleStep(stepsPerClick);
			}
		});
		
		/*
		 * Add listener to reset button, reset button causes CPU reset to inital vals, does not clear memory or stack
		 */
		btnReset.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				Simulator.this.handleCpuReset();
			}
		});
		
		btnRun.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				Simulator.this.handleRun();
			}
		});
		
		
		
		/*
		 * Menu Listeners
		 */
		
		menuHelp.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{		
				//Show Help box
			}
		});
		
		menuAbout.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				GuiAbout about = new GuiAbout();
				about.setLocationRelativeTo(null);
				about.setVisible(true);
				//Show About box
			}
		});
		
		menuClearMemory.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				clearRam();
				gui.refreshGui();
				gui.setCarets(0);
			}
		});
		
		menuClearStack.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				clearStack();
				gui.refreshGui();
				gui.setCarets(0);
			}
		});
		
		menuQuit.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
				System.out.println("[Simulator] Program terminated.");
			}
		});
		
		menuResetCpu.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				Simulator.this.handleCpuReset();
			}
		});
		
		menuClearEditor.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				gui.getEditorTextArea().setText("");
			}
		});
		
		menuRunAssembler.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				Simulator.this.handleRunAssembler();
			}
		});
		
		menuClearOutputConsole.addActionListener(new ActionListener() {
			//@Override
			public void actionPerformed(ActionEvent e)
			{
				gui.getAssemblerTextArea().setText("");
			}
		});
		
		mos6502.getCpu().setGui(gui); //Set reference to GUI in CPU
		mos6502.getRam().setGui(gui); //Set referebce to GUI in RAM
	}

	/*
	 * The handler that is called by user generated actions on the step button.
	 * 
	 * handleStep simply calls the step() method the number of times that is 
	 * passed through to the method as a parameter.
	 * 
	 * The GUI is refreshed after the passed number of steps has been completed.
	 * 
	 * TODO: Handle the JTextAreas moving along with the PC and SP arrows.
	 */
	private void handleStep(int steps)
	{
		int pc = 0;
		
		try
		{
			for(int i = 0; i < steps; i++)
			{
				step();
				pc = mos6502.getCpu().getPc();			
			}			
			gui.refreshGui();
			gui.setCarets(0);
		}
		catch(Exception ex)
		{
			System.out.println("[Simulator] Exception raised during step: " + ex);
			ex.printStackTrace();
		}
	}
	
	/*
	 * Handler that is called by user generated actions on the reset button.
	 * 
	 * handleCpuReset generates a "soft" reset of the cpu. Re-initalising its values
	 * and clearing the stack contents. Simulation RAM is unaffected by this method.
	 * 
	 * The GUI is refreshed after system has been reset.
	 */
	private void handleCpuReset()
	{
		try
		{
			resetCpu();
			clearStack();
			gui.refreshGui();
			gui.setCarets(0);
		}
		catch(Exception ex)
		{
			System.out.println("[Simulator] Exception raised during CPU reset: " + ex);
			ex.printStackTrace();
		}
	}
	
	/*
	 * Handler that is called by user generated actions on the run button.
	 * 
	 * handleRun() uses the CPU's canStep boolean variable to determine if the system
	 * is capable of stepping further. If canStep == true, the handler will call the
	 * step method until canStep == false, which occurs when the terminating keyword 
	 * END has been reached by the CPU.
	 * 
	 * The GUI is refreshed after the run sequence has been completed. 
	 * 
	 * The run handler includes a failsafe that prevents the run method from causing
	 * an infinite loop if the END keyword is never reached. The run loop will break
	 * if the number of executed steps exceeds 256.
	 */
	private void handleRun()
	{
		boolean canStep = mos6502.getCpu().getCanStep();
		int stepCounter = 0;
		
		try
		{
			while(canStep)
			{
				step();
				canStep = mos6502.getCpu().getCanStep();
				stepCounter++;
				
				if(stepCounter >= 256)
				{
					gui.getAssemblerTextArea().setText("Run loop reached limit of 256 steps and has stopped.");
					break;
				}
					
			}
			
			if(canStep == false)
				gui.getAssemblerTextArea().setText("Terminating instruction END has been reached. Terminating runloop.");
			
			gui.refreshGui();
			gui.setCarets(0);
		}
		catch(Exception ex)
		{
			System.out.println("[Simulator] Exception raised during CPU run: " + ex);
			ex.printStackTrace();
		}
	}
	
	/*
	 * Handler that is called by user generated actions on the "Run Assembler" menu item.
	 * 
	 * This method allows the user to input 6502-lite assembly code directly into the program's 
	 * GUI and load it into the system's RAM. This process will only succeed if the user's 
	 * program passes the compiler's syntax checks and therefore is syntactically valid.
	 * 
	 * Any CompilerExceptions raised by the compiler are caught by the method, the exception's
	 * error message is then relayed to the assembly output textarea and is used to present the
	 * error message to the user, informing them of their syntax error. 
	 * 
	 * Once the program passes the syntax checks and is successfully compiled, the program then 
	 * calls the loadProgrma() method to read in the binary output from the compiler and load 
	 * it into the simulation's RAM. The GUI is then refreshed to display the RAM's updated
	 * state. 
	 */
	private void handleRunAssembler()
	{
		clearRam();
		clearStack();
		resetCpu();
		
		System.out.println("\n[Simulator] RAM cleared");
		
		String assemblyContent = editorTextArea.getText(); //Get text from editor window 
		
		if(assemblyContent.equals("")) //no text in editor window
		{
			gui.getAssemblerTextArea().setText("Cannot run assembler: No text present in editor window. ");
		}
		else
		{
			createInputFile(assemblyContent); //Create assembly_input.txt file using text in editor window
			try
			{
				ReadAssemblyFile raf = new ReadAssemblyFile(assemblyFilepath); //Read assembly file into program
				System.out.println("[Simulator] BEGIN ASSEMBLER RUN");
				Compiler c = new Compiler(raf.getAssemblyText()); //Compile assembly file into binary and output to binary_output.txt
				System.out.println("[Simulator] END ASSEMBLER RUN");
				loadProgram(); //Load binary from binary_output.txt into memory
				System.out.println("\n[Simulator] Program successfully assembled and loaded into memory.");
				gui.getAssemblerTextArea().setText("Program successfully assembled into binary and loaded into memory.");
			}
			catch(CompilerException ex)
			{
				String msg = ex.getErrorMessage() + "\nAborting program assembly.";
				gui.getAssemblerTextArea().setText(msg);
			}
			
			gui.refreshGui();
			gui.setCarets(0);
		}
		
	}
	
	/*
	 * Utility method for the handleRunAssembler() method.
	 * 
	 * Generates a blank text file using the filepath stored in the
	 * AssemblyFilepath string. This is the textfile that the user's 
	 * custom program is loaded into before being processed by the 
	 * compiler.
	 */
	private void createInputFile(String content)
	{
		
		File file = new File(assemblyFilepath);
		
		try
		{
			writer = new PrintWriter(file);
		}
		catch(FileNotFoundException e)
		{
			System.err.println("[Simulation] createInputFile - File not found.");
		}
		
		writer.print(content);
		writer.close();
		System.out.println("\n[Simulation] assembly_input file created successfully.");
	}
	
	/*
	 * Calls the simulation's actual step method. If an exception is raised by the step
	 * method the exception error message is used to update the output TextArea with the
	 * reason for the exception. 
	 * 
	 */
	private void step()
	{
		try
		{
			mos6502.getCpu().step();
		}	
		catch(SimulationException ex)
		{
			String msg = ex.getErrorMessage() + "\nAborting program assembly";
			gui.getAssemblerTextArea().setText(msg);
		}
		
		
		//Limit number of UI updates?
	}

	/*
	 * Three utility methods that clear the simulation's RAM, stack 
	 * and reset the CPU respectively.
	 */
	private void clearRam()
	{
		mos6502.getRam().fill(0x00);
	}
	
	private void clearStack()
	{
		mos6502.getStack().fill(0x00);
	}
	
	private void resetCpu()
	{
		mos6502.getCpu().reset();
	}
}
