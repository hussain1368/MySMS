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
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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

public class CashTransReg extends JDialog{

	private static final long serialVersionUID = 1L;
	private JRadioButton debit, credit;
	private ButtonGroup buttons;
	private JComboBox<Option> cash;
	private JTextField amount, desc;
	private JSpinner transDate;
	private JButton save, cancel;
	
	private FinanceData data;
	
	public CashTransReg(){
		
		data = new FinanceData();
		String title = Dic.w("cash_trans_reg");
		setTitle(title);
		
		JPanel ribbon, center, bottom;
		ribbon = new Ribbon(title, false);
		center = new JPanel(new GridBagLayout());
		bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		add(ribbon, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		debit = new JRadioButton(Dic.w("debit"));
		credit = new JRadioButton(Dic.w("credit"));
		debit.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		credit.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		buttons = new ButtonGroup();
		buttons.add(debit);
		buttons.add(credit);
		
		cash = new JComboBox<>(data.cashOptions(false));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		amount = new JTextField();
		transDate = new JSpinner();
		transDate.setModel(new PDateModel(transDate));
		desc = new JTextField();
		desc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 4, 0, 4);
		
		cons.gridx = 0;
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("type_of_trans")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("cash")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("amount")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("date")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("description")), cons);
		
		cons.gridy = 0;
		cons.gridx = 1;
		center.add(credit, cons);
		cons.gridx = 2;
		center.add(debit, cons);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.gridy++;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center.add(cash, cons);
		cons.gridy++;
		center.add(amount, cons);
		cons.gridy++;
		center.add(transDate, cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(desc, cons);
		
		save = new JButton(Dic.w("save"));
		cancel = new JButton(Dic.w("cancel"));
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
		setBounds(0, 50, 450, 250);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}

	private boolean doSave()
	{
		if(!validator()) return false;
		
		int type = (credit.isSelected())? 1 : 2;
		int cash = ((Option) this.cash.getSelectedItem()).key;
		int value = Integer.parseInt(amount.getText());
		Date date = (Date) transDate.getValue();
		String desc = this.desc.getText().trim();
		
		return data.saveCashTrans(type, cash, value, date, desc);
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		buttons.clearSelection();
		this.cash.setSelectedIndex(0);
		transDate.setValue(new Date());
		amount.setText("");
		this.desc.setText("");
	}

	private boolean validator() 
	{
		if(buttons.getSelection() == null){
			Diags.showErrLang("choose_cash_trans_type_error");
			return false;
		}
		if(!Helper.isNumeric(amount.getText().trim(), true)){
			amount.setBackground(Color.RED);
			Diags.showErrLang("numeric_error");
			return false;
		}
		if(transDate.getValue() == null){
			Diags.showErrLang("cash_trans_date_required_error");
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




















