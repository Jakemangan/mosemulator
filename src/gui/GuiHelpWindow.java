package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.awt.Font;
import javax.swing.JScrollPane;

public class GuiHelpWindow extends JFrame {

	private JPanel contentPane;
	
	private String addressingModeTab = "The 6502-Lite program contains the same addressing mode scheme as the original 6502 (With the exception of \n"
									 + "zero-page addressing).\n\n"
			
									 + "Implicit - Many 6502 instructions do not require an operand, as the source and destination of the information \n"
									 + "to be manipulated is implied directly by the instruction itself. Therefore no operand needs to be specified. \n"
									 + "Instructions such as INX (Increment the X register) and CLC (Clear carry flag) are implicit.\n\n"
									 
									 + "Accumulator - Some instructions have an option to directly affect the accumulator. To achieve this effect, the \n"
									 + "programmer must specify the operand as 'A'. \nE.g. 'LSR A' or 'ROR A' \n\n"
									 
									 + "Immediate - Immediate addressing allows the program to directly specify an 8-bit hexadecimal constant to be \n"
									 + "used by the instruction. Immediate addressing is indicated by the '#' symbol followed by a hexadecimal number.\n"
									 + "E.g. 'LDA #10'\n\n"
									 
									 + "Relative - Relative addressing is utilised by branch instructions (e.g. BNE, BPL). These instructions require a \n"
									 + "signed 8-bit relative offset (between -128 and + 127), which will be added to the program counter if the branch \n"
									 + "condition is true. Relative addresses are denoted by '*+' or '*-' followed by the signed 8-bit hexadecimal offset.\n"
									 + "E.g. 'BNE *+4' will cause the PC to be incremented by 0x04 if the BNE condition is met.\n\n"
									 
									 + "Absolute - Instructions using absolute addressing utilise an 8-bit hexadecimal address to identify the target \n "
									 + "location. \nE.g. 'JMP $12' will cause the program counter to jump to the address 0x12.\n\n"
									 
									 + "Absolute-X - The address to be accessed by an instruction using Absolute-X addressing is computed by taking the \n"
									 + "8-bit address specified in the instruction and adding it to the value stored in the X register. \nE.g. if the"
									 + "X register contains $30 then the instruction 'STA $20,X' will store the accumulator's value at $50 ($20 + $30).\n\n"
	
									 + "Absolute-Y - Absolute-Y addressing is the same as Absolute-X addressing except the contents of the Y register \n"
									 + "is added to the 8-bit address from the instruction.\n"
									 + "E.g. 'STA $20,Y' will store the accumulator value at $40, if we assume the Y register has value $20.\n\n"
									 
									 + "Indirect - 'JMP' is the only instruction to support indirect addressing. The instruction contains an 8-bit address\n"
									 + "which identifies the location of a second 8-bit address which is the real target of the instruction.\n"
									 + "E.g. if location $120 contains the value $FC, then the instruction 'JMP ($120)' will cause the next \n"
									 + "instruction execution to occur at $FC (the contents of $120)\n\n"
									 
									 + "Indexed-Indirect - Indexed-Indirect addressing takes the address from the instruction and combines it with the\n"
									 + "X register to give the location of the 8-bit value of the target address. \n"
									 + "E.g. 'LDA ($40,X)'\n\n"
									 
									 + "Indirect-Indexed - Indirect-Indexed addressing is the most common indirection mode utilised on the 6502. An \n"
									 + "instruction contains the location of an 8-bit address. The Y register is dynamically added to this value to \n"
									 + "generate the actual target address.\n"
									 + "E.g. LDA ($40),Y";

	private String syntaxTab = "Assembly programs must start and end with the BEGIN and END keywords.\n\n"
			
							 + "All numerical expressions entered into the program are assumed to be in hexadecimal format.\n\n"
							 
							 + "Program instructions and operands must be separated by at least one whitespace.\n\n"
							 
							 + "Comments can be included in the program. Comments are denoted by the ';' character.\n\n"
							 
							 + "Each assembly line must only contain a single instruction and operand.\n\n"
							 
							 + "Each line should follow the pattern: [instruction] [operand] [;comment]\n\n"
							 
							 + "Some instructions do not require operands, check the \"Instructions\" tab for more information.\n\n"
							 
							 + "Branch labelling is currently not supported by the assembler and therefore is unavailable.\n"
							 + "To branch to a specific address, the JMP and branch instructions can still be used, but actual values will be \nrequired.\n\n"
							 
							 + "Addressing mode syntax: \nAccumulator: A \nImmediate: #1 \nRelative: *+1\n"
							 + "Absolute: $1 \nAbsolute,X: $1,Y \nAbsolute,Y: $1,Y \nIndirect: ($1) \nIndexed-Indirect: ($1,X)\n"
							 + "Indirect-Indexed: ($1),Y\n\n"
							 
							 + "";
							 
							 
							 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					GuiHelpWindow frame = new GuiHelpWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GuiHelpWindow() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 844, 736);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setBounds(10, 142, 810, 544);
		panel.add(tabbedPane_1);
		
		JScrollPane scrollPane = new JScrollPane();
		tabbedPane_1.addTab("General Information", null, scrollPane, null);
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane_1.addTab("Addressing Modes", null, scrollPane_1, null);
		
		JTextArea txtrText = new JTextArea();
		txtrText.setEditable(false);
		txtrText.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		txtrText.setText(addressingModeTab);
		scrollPane_1.setViewportView(txtrText);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		tabbedPane_1.addTab("Instructions", null, scrollPane_2, null);
		
		JTextArea textArea_2 = new JTextArea();
		textArea_2.setEditable(false);
		textArea_2.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		scrollPane_2.setViewportView(textArea_2);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		tabbedPane_1.addTab("Syntax", null, scrollPane_3, null);
		
		JTextArea txtrText_1 = new JTextArea();
		txtrText_1.setEditable(false);
		txtrText_1.setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
		txtrText_1.setText(syntaxTab);
		scrollPane_3.setViewportView(txtrText_1);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		tabbedPane_1.addTab("Example Programs", null, scrollPane_4, null);
		
		JTextArea textArea_1 = new JTextArea();
		scrollPane_4.setViewportView(textArea_1);
	}
}
