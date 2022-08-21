package com.kabulbits.shoqa.util;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

public class Reset extends FocusAdapter 
{
	public void focusGained(FocusEvent f) {
		JTextField tf = (JTextField) f.getSource();
		tf.setBackground(Color.WHITE);
		tf.selectAll();
	}
}
