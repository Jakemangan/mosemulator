package core;

import java.awt.EventQueue;

import javax.swing.UIManager;


/*
 * TODO: Stop branch labels from being used within the assembler
 * 
 * 31/03/2018: Fixed compiler not recognising LDY instructions
 * also changed CPU class import core.cpu.instructionTable to compiler.instructionTable, due to not requiring two separate instruction tables
 * Change should not cause issues.  
 */

public class Main {
	
	
	public static void main(String[] args)
	{	
		Simulator simulator = new Simulator();	
	
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					simulator.createAndShowGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	


}
