package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.SystemColor;
import javax.swing.JTextField;

public class GuiAbout extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiAbout frame = new GuiAbout();
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
	public GuiAbout() {
		setTitle("About");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(10, 11, 404, 66);
		panel.add(panel_1);
		panel_1.setLayout(new GridLayout(3, 1, 0, 0));
		
		JLabel lbllite = new JLabel("Jake Mangan - W14014329");
		lbllite.setHorizontalAlignment(SwingConstants.CENTER);
		lbllite.setFont(new Font("Tahoma", Font.PLAIN, 11));
		panel_1.add(lbllite);
		
		JLabel lblNewLabel = new JLabel("CM0645 - Indiviual Project");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("A Simulated CPU for Teaching Processor Fundamentals \r\n");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblNewLabel_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_2.setBounds(10, 88, 404, 152);
		panel.add(panel_2);
		panel_2.setLayout(null);
		
		JTextArea txtrliteAJava = new JTextArea();
		txtrliteAJava.setEditable(false);
		txtrliteAJava.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtrliteAJava.setText("6502-Lite: A Java based application that accurately emulates the function of \r\nthe MOS-6502 microprocessor. Developed for use within an educational enviro-nment as a practical software tool to supplement student education of comput-er architecture and organisation.");
		txtrliteAJava.setLineWrap(true);
		txtrliteAJava.setBackground(SystemColor.control);
		txtrliteAJava.setBounds(10, 11, 384, 64);
		panel_2.add(txtrliteAJava);
		
		JLabel lblVersion = new JLabel("Version 1.0");
		lblVersion.setVerticalAlignment(SwingConstants.BOTTOM);
		lblVersion.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblVersion.setEnabled(false);
		lblVersion.setBounds(10, 77, 370, 64);
		panel_2.add(lblVersion);
	}
}
