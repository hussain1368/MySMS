package com.kabulbits.shoqa.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

public class OptionChooser extends JDialog 
{
	private static final long serialVersionUID = 1L;
	private JComboBox<String> options;
	private JButton select, cancel;
	public int signal = -1;

	public OptionChooser(String title, String[] opts)
	{
		JPanel top = new Ribbon(Dic.w(title), false);
		JPanel center = new JPanel(new GridBagLayout());
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		options = new JComboBox<String>();
		for(String opt : opts){
			options.addItem(Dic.w(opt));
		}
		options.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) options.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.fill = GridBagConstraints.NONE;
		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("choose_option")), cons);
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center.add(options, cons);
		
		select = new JButton(Dic.w("select"));
		cancel = new JButton(Dic.w("cancel"));
		
		bottom.add(select);
		bottom.add(cancel);
		
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				signal = options.getSelectedIndex();
				dispose();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		Helper.esc(this);

		setSize(350, 160);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);
	}
}
