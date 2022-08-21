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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class AccountReg extends JDialog{

	private static final long serialVersionUID = 1L;
	private JTextField name;
	private JRadioButton revenue, expense;
	private ButtonGroup buttons;
	private JButton save, cancel;
	
	private FinanceData data;
	
	public AccountReg() {
		
		String title = Dic.w("create_account");
		setTitle(title);

		data = new FinanceData();

		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);

		JPanel center = new JPanel();
		center.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		name = new JTextField();
		revenue = new JRadioButton(Dic.w("revenue"));
		expense = new JRadioButton(Dic.w("expense"));
		name.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		revenue.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		expense.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		buttons = new ButtonGroup();
		buttons.add(revenue);
		buttons.add(expense);
		
		center.setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 4, 0, 4);
		
		cons.gridx = 0;
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("account_name")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("account_type")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridwidth = 2;
		cons.weightx = 1;
		cons.gridy = 0;
		cons.gridx = 1;
		center.add(name, cons);
		
		cons.weightx = 0;
		cons.gridwidth = 1;
		cons.weighty = 1;
		cons.gridy = 1;
		cons.gridx = 1;
		center.add(revenue, cons);
		cons.gridx = 2;
		center.add(expense, cons);

		add(center, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(bottom, BorderLayout.SOUTH);

		save = new JButton(Dic.w("save"));
		cancel = new JButton(Dic.w("cancel"));
		bottom.add(cancel);
		bottom.add(save);

		name.addFocusListener(new Reset());
		save.addActionListener(new BgWorker<Boolean>(this) {
			@Override
			protected Boolean save() {
				return doSave();
			}
			@Override
			protected void finish(Boolean result) {
				if(result){
					dispose();
					Diags.showMsg(Diags.SUCCESS);
				}
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		Helper.esc(this);
		setSize(300, 180);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);

	}
	
	public boolean doSave()
	{
		if(!validateForm()) return false;
		String name = this.name.getText().trim();
		int type = revenue.isSelected() ? 1 : 2;
		return data.insertAccount(name, type);
	}
	
	private boolean validateForm()
	{
		if(name.getText().trim().length() == 0){
			name.setBackground(Color.RED);
			return false;
		}
		if(buttons.getSelection() == null){
			Diags.showErrLang("choose_account_type_error");
			return false;
		}
		return true;
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
