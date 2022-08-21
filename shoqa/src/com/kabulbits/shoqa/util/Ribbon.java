package com.kabulbits.shoqa.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class Ribbon extends JPanel{

	private static final long serialVersionUID = 1L;
	
	public Ribbon(String title, boolean large)
	{
		setLayout(new BorderLayout());
		setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
		JLabel banner = new JLabel(title);
		banner.setFont(new Font("serif", Font.BOLD, large ? 22 : 20));
		banner.setBorder(new EmptyBorder(0, 10, 0, 10));
		banner.setForeground(Color.WHITE);
		JLabel ks = new JLabel(new ImageIcon(large ? "images/ribbon.png" : "images/ribbonm.png"));
		setBackground(new Color(48, 180, 166));
		add(banner, BorderLayout.EAST);
		add(ks, BorderLayout.WEST);
	}
}
