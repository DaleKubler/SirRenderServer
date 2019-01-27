package tv.culturesport.sirrender;

/*
 * Copyright (c) 2016, Dale Kubler. All rights reserved.
 *
 */


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;


public class Console extends WindowAdapter implements WindowListener, ActionListener, Runnable
{
	private JFrame frame;
	private static JTextArea textArea;

	private JScrollPane scrollPane;
	private boolean quit;
					

	public Console()
	{
		//MasterMain.log.debug("inside new Console()");
		// Create all components and add them
		frame=new JFrame(ApplicationConstants.SIRRENDER_SERVER_CONSOLE_TITLE);
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/1.4),(int)(screenSize.height/1.4));
		int x=(int)(frameSize.width/4);
		int y=(int)(frameSize.height/4);
		frame.setBounds(x,y,frameSize.width,frameSize.height);
		
		textArea=new JTextArea();
		textArea.setEditable(false);
		
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPane = new JScrollPane();
		scrollPane.add(textArea);
		scrollPane.setViewportView(textArea);		
		
		JButton button=new JButton("Clear");
		
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
		frame.getContentPane().add(button,BorderLayout.SOUTH);
		frame.setVisible(true);		
		
		frame.addWindowListener(this);		
		button.addActionListener(this);
	}
	
	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		System.exit(0);
	}		
		
	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behavior of JFrame	
	}
	
	public synchronized void windowOpening(WindowEvent evt)
	{
		frame.setVisible(true); // display console window	
	}
	
	public synchronized void actionPerformed(ActionEvent evt)
	{
		textArea.setText("");
	}

	public JFrame getFrame() {
		return frame;
	}

	public static JTextArea getTextArea() {
		return textArea;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
	
}