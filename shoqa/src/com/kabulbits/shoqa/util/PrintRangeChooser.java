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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.MatteBorder;

public abstract class PrintRangeChooser extends JDialog 
{
	private static final long serialVersionUID = 1L;
	private JComboBox<String> options;
	private JSpinner start, end;
	private JButton select, cancel;

	public PrintRangeChooser(String title, String label, String[] opts)
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
		
		start = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
		end = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 1));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.fill = GridBagConstraints.NONE;
		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("choose_option")), cons);
		cons.weightx = 1;
		cons.gridwidth = 3;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center.add(options, cons);
		
		cons.gridy = 1;
		cons.weighty = 1;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		
		cons.gridx = 0;
		center.add(new JLabel(Dic.w(label)), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx++;
		center.add(start, cons);
		cons.gridx++;
		center.add(new JLabel(Dic.w("to")), cons);
		cons.gridx++;
		center.add(end, cons);
		
		select = new JButton(Dic.w("select"));
		cancel = new JButton(Dic.w("cancel"));
		
		bottom.add(select);
		bottom.add(cancel);
		
		options.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(options.getSelectedIndex() == 0){
					start.setEnabled(true);
					end.setEnabled(true);
				}else{
					start.setEnabled(false);
					end.setEnabled(false);
				}
			}
		});
		select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int task = options.getSelectedIndex();
				int from = (int) start.getValue();
				int to = (int) end.getValue();
				dispose();
				print(task, from, to);
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		Helper.esc(this);

		setSize(350, 170);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);
	}
	protected abstract void print(int task, int from, int to);
}
