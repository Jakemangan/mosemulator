package gui;

import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.SystemColor;
import java.util.HashMap;

import javax.swing.UIManager;
import java.awt.FlowLayout;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import java.awt.Choice;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

import core.MOS6502;
import util.Utils;
import javax.swing.JList;
import javax.swing.AbstractListModel;

/*
 * TODO: Implement run/step/reset buttons
 * TODO: Implement multiple steps using dropdown 
 * TODO: Enable assembling of code through GUI
 * 
 */

public class GUI2 {

	private MOS6502 machine;
	
	private JFrame frmlite;
	
	private static JTextArea register1, register2TextArea, stackTextArea, editorTextArea, psrTextArea, psr2TextArea, memmapTextArea, assemblerTextArea;
	private static JButton btnRun, btnStep, btnReset;
	private static JMenuItem mntmClearMemory, mntmClearStack, mntmQuit, mntmResetCpu, mntmClearEditor, mntmRunAssembler, mntmHelp, mntmAbout, mntmClearOutputConsole;
	private static JComboBox<?> comboBox;
	private static JScrollPane memmapScrollPane, stackScrollPane;
	
	private HashMap<Integer, String> asciiMap;
	
	ImageIcon img = new ImageIcon("./source/6502.png");




	 

	/**
	 * Launch the application.
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					GUI2 window = new GUI2();
					window.frmlite.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	public GUI2() {
		initialize();
		populateAsciiMap();
		System.out.println("\n[GUI] GUI Initialised");
	}
	
	public void refreshGui()
	{
		populateMemoryMap();
		populateRegisterContents();
		populatePsrContents();
		populateStackMap();
	}
	
	/*
	 * 
	 */
	public void setSimMachine(MOS6502 machine)
	{
		this.machine = machine;
	}
	
	public void populateMemoryMap()
	{
		String memoryMap = "";
		int pc = machine.getCpu().getPc();
		
		
		for(int i = 0; i < 256; i++)
		{
			String memoryString = "";
			
			String currentHexAddress = Utils.DecToHex(i);
			int memContents = machine.getRam().getAddressContents(i);
			String memContentsHex = Utils.DecToHex(memContents);
			String instructionSymbol = machine.getCpu().determineInstructionSymbol(memContents);
			String instructionBox = "[" + instructionSymbol + "]";
			String asciiChar = " '" + determineAsciiChar(memContents) + "'";
			
			
			if(currentHexAddress.length() == 0)
			{
				currentHexAddress = "00";
			}
			
			if(currentHexAddress.length() == 1)
			{
				currentHexAddress = "0".concat(currentHexAddress);
			}
			
			if(memContentsHex.length() == 0)
			{
				memContentsHex = "00";
			}
			
			if(memContentsHex.length() == 1)
			{
				memContentsHex = "0".concat(memContentsHex);
			}
			
			
			
			memoryString = memoryString.concat("0x" + currentHexAddress + ": $" + memContentsHex + " (" + memContents + ")");
			
			int stringLength = memoryString.length();
			
			while(stringLength < 16)
			{
				memoryString = memoryString.concat(" ");
				stringLength = memoryString.length();
			}
			
			memoryString = memoryString.concat(asciiChar);
			
			while(stringLength < 22)
			{
				memoryString = memoryString.concat(" ");
				stringLength = memoryString.length();
			}
			
			
			memoryString = memoryString.concat(instructionBox);
			
			if(i == pc)
			{
				String pcStr = "  <- PC";
				memoryString = memoryString + pcStr;
			}
			
			memoryString = memoryString.concat("\n");
			
			memoryMap = memoryMap.concat(memoryString);
			
		}
		
		memmapTextArea.setText(memoryMap);
		
	}
	
	public void populateStackMap()
	{
		String stackMap = "";
		int sp = machine.getCpu().getSp();
		
		
		for(int i = 63; i > -1; i--)
		{
			String stackString = "";
			
			String currentHexAddress = Utils.DecToHex(i);
			int memContents = machine.getStack().getStackValue(i);
			String memContentsHex = Utils.DecToHex(memContents);
			String instructionSymbol = machine.getCpu().determineInstructionSymbol(memContents);
			String instructionBox = "[" + instructionSymbol + "]";
			String asciiChar = " '" + determineAsciiChar(memContents) + "'";
			
			
			if(currentHexAddress.length() == 0)
			{
				currentHexAddress = "00";
			}
			
			if(currentHexAddress.length() == 1)
			{
				currentHexAddress = "0".concat(currentHexAddress);
			}
			
			if(memContentsHex.length() == 0)
			{
				memContentsHex = "00";
			}
			
			if(memContentsHex.length() == 1)
			{
				memContentsHex = "0".concat(memContentsHex);
			}
			
			stackString = stackString.concat("0x" + currentHexAddress + ": $" + memContentsHex + " (" + memContents + ")");
			
			
			
			int stringLength = stackString.length();
			
			while(stringLength < 16)
			{
				stackString = stackString.concat(" ");
				stringLength = stackString.length();
			}
			
			stackString = stackString.concat(asciiChar);
			
			while(stringLength < 22)
			{
				stackString = stackString.concat(" ");
				stringLength = stackString.length();
			}
			
			
			stackString = stackString.concat(instructionBox);
			
			if(i == sp)
			{
				String pcStr = "  <- PC";
				stackString = stackString + pcStr;
			}
			
			stackString = stackString.concat("\n");
			
			stackMap = stackMap.concat(stackString);
			
		}
		
		stackTextArea.setText(stackMap);
		
	}
	
	public void populateRegisterContents()
	{
		register1.setText(machine.getCpu().getRegisterContentsPrimary());
		register2TextArea.setText(machine.getCpu().getRegisterContentsSecondary());
	}
	
	public void populatePsrContents()
	{
		psrTextArea.setText(machine.getCpu().getPsrContentsPrimary());
		psr2TextArea.setText(machine.getCpu().getPsrContentsSecondary());
	}
	
	public void setCarets(int val)
	{
		memmapTextArea.setCaretPosition(val); //Scroll TextAreas back to top
		stackTextArea.setCaretPosition(val);
	}
	
	public void setMMCaret(int val)
	{
		System.out.println("[GUI] Set MemMap caret to: " + val);
		memmapTextArea.setCaretPosition(val); //Scroll TextAreas back to top
	
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		frmlite = new JFrame();
		frmlite.setTitle("6502-Lite");
		frmlite.getContentPane().setFont(new Font("Monospaced", Font.BOLD, 11));
		frmlite.setResizable(false);
		frmlite.setBounds(100, 100, 811, 752);
		frmlite.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmlite.getContentPane().setLayout(null);
		frmlite.setIconImage(img.getImage());
		
		JLabel lblAssemblerOutput = new JLabel("Output");
		lblAssemblerOutput.setBounds(10, 431, 121, 14);
		frmlite.getContentPane().add(lblAssemblerOutput);
		
		JLabel lblMemoryMap = new JLabel("Memory Map");
		lblMemoryMap.setBounds(495, 118, 94, 14);
		frmlite.getContentPane().add(lblMemoryMap);
		
		memmapScrollPane = new JScrollPane();
		memmapScrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		memmapScrollPane.setBounds(495, 143, 299, 276);
		frmlite.getContentPane().add(memmapScrollPane);
		
		memmapTextArea = new JTextArea();
		memmapTextArea.setEditable(false);
		memmapTextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		memmapTextArea.setLineWrap(true);
		memmapScrollPane.setViewportView(memmapTextArea);
		
		JScrollPane assemblerScrollPane = new JScrollPane();
		assemblerScrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		assemblerScrollPane.setBounds(10, 455, 475, 200);
		frmlite.getContentPane().add(assemblerScrollPane);
		
		assemblerTextArea = new JTextArea();
		assemblerTextArea.setEditable(false);
		assemblerTextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		assemblerTextArea.setLineWrap(true);
		assemblerScrollPane.setViewportView(assemblerTextArea);
		
		JLabel lblRegisterContents = new JLabel("Register Contents");
		lblRegisterContents.setBounds(10, 11, 159, 14);
		frmlite.getContentPane().add(lblRegisterContents);
		
		JLabel lblStackMap = new JLabel("Stack Map");
		lblStackMap.setBounds(495, 431, 81, 14);
		frmlite.getContentPane().add(lblStackMap);
		
		register1 = new JTextArea();
		register1.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		register1.setBackground(UIManager.getColor("Button.background"));
		register1.setEditable(false);
		register1.setBounds(10, 35, 81, 71);
		frmlite.getContentPane().add(register1);

		register2TextArea = new JTextArea();
		register2TextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		register2TextArea.setEditable(false);
		register2TextArea.setBackground(UIManager.getColor("Button.background"));
		register2TextArea.setBounds(101, 35, 81, 71);
		frmlite.getContentPane().add(register2TextArea);
		
		JLabel lblProcessorStatus = new JLabel("Processor Status");
		lblProcessorStatus.setBounds(495, 11, 142, 14);
		frmlite.getContentPane().add(lblProcessorStatus);
		
		psrTextArea = new JTextArea();
		psrTextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		psrTextArea.setBackground(UIManager.getColor("Button.background"));
		psrTextArea.setBounds(495, 36, 81, 103);
		frmlite.getContentPane().add(psrTextArea);
		
		psr2TextArea = new JTextArea();
		psr2TextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		psr2TextArea.setBackground(UIManager.getColor("Button.background"));
		psr2TextArea.setBounds(588, 36, 136, 102);
		frmlite.getContentPane().add(psr2TextArea);
		
		stackScrollPane = new JScrollPane();
		stackScrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		stackScrollPane.setBounds(495, 455, 299, 200);
		frmlite.getContentPane().add(stackScrollPane);
		
		stackTextArea = new JTextArea();
		stackTextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		stackTextArea.setEditable(false);
		stackScrollPane.setViewportView(stackTextArea);
		
		JScrollPane editorScrollPane = new JScrollPane();
		editorScrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		editorScrollPane.setBounds(8, 143, 475, 276);
		frmlite.getContentPane().add(editorScrollPane);
		
		editorTextArea = new JTextArea();
		editorTextArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		editorScrollPane.setViewportView(editorTextArea);
		
		JLabel lblAssemblyEditor = new JLabel("Assembly Editor");
		lblAssemblyEditor.setBounds(10, 118, 142, 14);
		frmlite.getContentPane().add(lblAssemblyEditor);
		
		JMenuBar menuBar = new JMenuBar();
		frmlite.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmQuit = new JMenuItem("Quit");
		mnFile.add(mntmQuit);
		
		JMenu mnCpu = new JMenu("CPU");
		menuBar.add(mnCpu);
		
		mntmResetCpu = new JMenuItem("Reset CPU");
		mntmResetCpu.setToolTipText("Resets the CPU back to initial values. Does not affect the stack or RAM state.");
		mnCpu.add(mntmResetCpu);
		
		JMenu mnMemory = new JMenu("Memory");
		menuBar.add(mnMemory);
		
		mntmClearMemory = new JMenuItem("Clear Memory");
		mntmClearMemory.setToolTipText("Resets the RAM back to initial values. Does not affect the CPU or stack state.");
		mnMemory.add(mntmClearMemory);
		
		mntmClearStack = new JMenuItem("Clear Stack");
		mntmClearStack.setToolTipText("Resets the stack back to initial values. Does not affect the CPU or RAM state.");
		mnMemory.add(mntmClearStack);
		
		JMenu mnAssembler = new JMenu("Assembler");
		menuBar.add(mnAssembler);
		
		mntmRunAssembler = new JMenuItem("Run Assembler");
		mntmRunAssembler.setToolTipText("Assembles code present in the editor window into binary which is then loaded into memory."
				+ "\nResets the CPU, stack and RAM to initial values.");
		mnAssembler.add(mntmRunAssembler);
		
		mntmClearEditor = new JMenuItem("Clear Editor");
		mnAssembler.add(mntmClearEditor);
		
		mntmClearOutputConsole = new JMenuItem("Clear Output Console");
		mnAssembler.add(mntmClearOutputConsole);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
		mntmHelp = new JMenuItem("Help");
		//mnHelp.add(mntmHelp);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBounds(10, 658, 784, 33);
		frmlite.getContentPane().add(buttonPanel);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnRun = new JButton("Run");
		btnRun.setToolTipText("Execute program until the terminating instruction is reached.");
		buttonPanel.add(btnRun);
		
		btnStep = new JButton("Step");
		btnStep.setToolTipText("Step through the program the selected number of times.");
		buttonPanel.add(btnStep);
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"1", "5", "10", "25"}));
		buttonPanel.add(comboBox);
		
		btnReset = new JButton("Reset");
		btnReset.setToolTipText("Reset the CPU to its initial state. Does not reset the memory or stack.");
		buttonPanel.add(btnReset);
		
		frmlite.setVisible(true);
	}
	
	public String determineAsciiChar(int value)
	{
		String asciiChar = asciiMap.get(value);
		
		if(asciiChar == null)
		{
			return " ";
		}
		
		return asciiChar;
	}
	
	public void populateAsciiMap()
	{
		asciiMap = new HashMap<Integer, String>();
		for (int i = 32; i <= 126; i++) {
		      asciiMap.put(i, Character.toString((char) i));
		}
	}
	
	public JButton getBtnRun()
	{
		return btnRun;
	}
	
	public JButton getBtnStep()
	{
		return btnStep;
	}
	
	public JButton getBtnReset()
	{
		return btnReset;
	}
	
	public JComboBox getComboBox()
	{
		return comboBox;
	}
	
	public JTextArea getEditorTextArea()
	{
		return editorTextArea;
	}
	
	public JTextArea getstackTextArea()
	{
		return stackTextArea;
	}
	
	public JTextArea getMemmapTextArea()
	{
		return memmapTextArea;
	}
	
	public JTextArea getAssemblerTextArea()
	{
		return assemblerTextArea;
	}

	public JMenuItem getMenuClearMemory()
	{
		return mntmClearMemory;
	}
	
	public JMenuItem getMenuClearStack()
	{
		return mntmClearStack;
	}
	
	public JMenuItem getMenuQuit()
	{
		return mntmQuit;
	}
	
	public JMenuItem getMenuResetCpu()
	{
		return mntmResetCpu;
	}
	
	public JMenuItem getMenuClearEditor()
	{
		return mntmClearEditor;
	}
	
	public JMenuItem getMenuRunAssembler()
	{
		return mntmRunAssembler;
	}
	
	public JMenuItem getMenuHelp()
	{
		return mntmHelp;
	}
	
	public JMenuItem getMenuAbout()
	{
		return mntmAbout;
	}
	
	public JMenuItem getMenuClearOutputConsole()
	{
		return mntmClearOutputConsole;
	}
	
	public JScrollPane getMemMapScrollPane()
	{
		return memmapScrollPane;
	}
	
	public JScrollPane getStackScrollPane()
	{
		return stackScrollPane;
	}
}
