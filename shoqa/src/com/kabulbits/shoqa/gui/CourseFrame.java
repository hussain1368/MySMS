package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class CourseFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CourseFrame self;
	private CoursePanel panel;
	
	public CourseFrame()
	{
		isOpen = true;
		self = this;
		
		String title = Dic.w("courses");
		setTitle(title);

		JPanel top = new Ribbon(title, true);
		panel = new CoursePanel();
		add(top, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				isOpen = false;
			}
		});
		
		panel.render();
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}

