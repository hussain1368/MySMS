package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class BorrowReturn extends JDialog{

	private static final long serialVersionUID = 1L;
	
	private JSpinner returnDate;
	public boolean result = false;
	private LibraryData data;
	private int bid;
	private int coverid;
	
	public BorrowReturn(int id, int cover)
	{
		bid = id;
		coverid = cover;
		data = new LibraryData();
		
		JPanel top = new Ribbon(Dic.w("book_return"), false);
		JPanel center = new JPanel(new GridBagLayout());
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		returnDate = new JSpinner();
		returnDate.setModel(new PDateModel(returnDate));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5, 5, 5, 5);
		center.add(new JLabel(Dic.w("return_date")), cons);
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center.add(returnDate, cons);
		
		JButton save = new JButton(Dic.w("save"));
		JButton cancel = new JButton(Dic.w("cancel"));
		bottom.add(cancel);
		bottom.add(save);
		
		Helper.esc(this);
		
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Date date = (Date) returnDate.getValue();
				if(date == null){
					returnDate.getEditor().setBackground(Color.RED);
					return;
				}
				result = data.borrowReturn(bid, coverid, true, date);
				dispose();
			}
		});
		returnDate.getEditor().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				returnDate.getEditor().setBackground(Color.WHITE);
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		setSize(300, 150);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
