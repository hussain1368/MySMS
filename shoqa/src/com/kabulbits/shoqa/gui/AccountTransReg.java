package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class AccountTransReg extends JDialog{

	private static final long serialVersionUID = 1L;
	private JComboBox<Option> account;
	private JComboBox<Option> cash;
	private JTextField amount, desc;
	private JSpinner transDate;
	private JButton save, cancel;
	private FinanceData data;
	
	public AccountTransReg(int type){
		
		data = new FinanceData();
		String title = Dic.w(type == 1 ? "revenue_reg" : "expense_reg");
		setTitle(title);
		
		JPanel ribbon, center, bottom;
		ribbon = new Ribbon(title, false);
		center = new JPanel(new GridBagLayout());
		bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		add(ribbon, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		account = new JComboBox<>(data.accountOptions(type));
		account.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)account.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		cash = new JComboBox<>(data.cashOptions(false));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		amount = new JTextField();
		transDate = new JSpinner();
		transDate.setModel(new PDateModel(transDate));
		desc = new JTextField();
		desc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		Component [] components = {account, cash, amount, transDate, desc};
		String labels [] = {"account_name", "cash", "amount", "date", "description"};
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 4, 0, 4);
		
		cons.gridx = 0;
		cons.gridy = 0;
		for(String item : labels){
			center.add(new JLabel(Dic.w(item)), cons);
			cons.gridy++;
		}
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx = 1;
		cons.gridy = 0;
		int i = 0;
		for(Component comp : components){
			if(++i == labels.length) cons.weighty = 1;
			center.add(comp, cons);
			cons.gridy++;
		}
		
		cancel = new JButton(Dic.w("cancel"));
		save = new JButton(Dic.w("save"));
		bottom.add(cancel);
		bottom.add(save);
		
		amount.addFocusListener(new Reset());
		save.addActionListener(new BgWorker<Boolean>(this) {
			@Override
			protected Boolean save() {
				return doSave();
			}
			@Override
			protected void finish(Boolean result) {
				if(result){
					afterSave();
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
		setBounds(0, 50, 450, 260);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}

	public boolean doSave()
	{
		if(!validator()) return false;

		int id = ((Option)account.getSelectedItem()).key;
		int cash = ((Option)this.cash.getSelectedItem()).key;
		int value = Integer.parseInt(amount.getText());
		Date date = (Date) transDate.getValue();
		String desc = this.desc.getText();
		
		return data.saveAccountTrans(id, cash, value, date, desc);
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		amount.setText("");
		this.desc.setText("");
		transDate.setValue(new Date());
		this.account.setSelectedIndex(0);
		this.cash.setSelectedIndex(0);
	}

	private boolean validator() 
	{
		if(!Helper.isNumeric(amount.getText().trim(), true)){
			amount.setBackground(Color.RED);
			Diags.showErrLang("numeric_error");
			return false;
		}
		if(transDate.getValue() == null){
			Diags.showErrLang("pay_date_is_required");
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





















