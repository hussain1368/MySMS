package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class EmployeeSalaryReg extends JDialog
{
	private static final long serialVersionUID = 1L;
	private JComboBox<Option> cash;
	private FilterEmployee empName;
	private JTextField amount, empID, desc;
	private JSpinner payDate;
	private JButton save, cancel;
	private FinanceData data;
	
	public EmployeeSalaryReg()
	{
		String title = Dic.w("employee_salary_reg");
		setTitle(title);

		data = new FinanceData();

		JPanel top = new Ribbon(title, false);
		JPanel center = new JPanel(new GridBagLayout());
		center.setBorder(new EmptyBorder(5, 5, 5, 5));
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		cash = new JComboBox<>(data.cashOptions(false));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		empName = new FilterEmployee();
		amount = new JTextField(20);
		empID = new JTextField();
		empID.setEditable(false);
		empID.setFocusable(false);
		desc = new JTextField();
		desc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		payDate = new JSpinner();
		payDate.setModel(new PDateModel(payDate));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		
		cons.weighty = 0;
		cons.gridx = 0;
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("employee_name")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("cash")), cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(new JLabel(Dic.w("amount")), cons);
		
		cons.weighty = 0;
		cons.gridx = 2;
		cons.gridy = 0;
		center.add(new JLabel(Dic.w("employee_code")), cons);
		cons.gridy++;
		center.add(new JLabel(Dic.w("date")), cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(new JLabel(Dic.w("description")), cons);

		cons.fill = GridBagConstraints.HORIZONTAL;
		
		cons.weighty = 0;
		cons.gridx = 1;
		cons.gridy = 0;
		center.add(empName, cons);
		cons.gridy++;
		center.add(cash, cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(amount, cons);
		
		cons.weightx = 1;
		
		cons.weighty = 0;
		cons.gridx = 3;
		cons.gridy = 0;
		center.add(empID, cons);
		cons.gridy++;
		center.add(payDate, cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(desc, cons);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		save = new JButton(Dic.w("save"));
		cancel = new JButton(Dic.w("cancel"));
		bottom.add(cancel);
		bottom.add(save);
		
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		empName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if(empName.getSelectedIndex() > -1)
				{
					int id = ((Option) empName.getSelectedItem()).key;
					empID.setText(String.valueOf(id));
				}
				else{
					empID.setText("");
				}
			}
		});
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
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		Helper.esc(this);
		setBounds(0, 50, 500, 200);
		setMinimumSize(new Dimension(500, 200));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private boolean doSave()
	{
		if(!Helper.isNumeric(empID.getText(), true))
		{
			Diags.showErrLang("choose_person_for_code");
			return false;
		}
		if(!Helper.isNumeric(amount.getText(), true))
		{
			amount.setBackground(Color.RED);
			Diags.showErrLang("numeric_error");
			return false;
		}
		try{
			int id = Integer.parseInt(empID.getText().trim());
			int val = Integer.parseInt(amount.getText().trim());
			int cashid = ((Option) cash.getSelectedItem()).key;
			String descr = desc.getText().trim();
			Date date = (Date) payDate.getValue();
			
			if(date == null){
				Diags.showErrLang("pay_date_is_required");
				return false;
			}
			return data.saveEmployeeReceipt(id, val, cashid, date, descr);
		} 
		catch (NumberFormatException e) {return false;}
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		
		empName.setSelectedIndex(-1);
		cash.setSelectedIndex(0);
		empID.setText("");
		amount.setText("");
		desc.setText("");
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

//Auto Filter Combo Box

class FilterEmployee extends JComboBox<Option> {

	private static final long serialVersionUID = 1L;
	protected JTextField textfield = (JTextField) this.getEditor().getEditorComponent();
	private EmployeeData data = new EmployeeData();
	private int pos = 0;

	public FilterEmployee()
	{
	    this.setEditable(true);
	    this.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	    ((JLabel)this.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
	    textfield.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	    
	    textfield.addKeyListener(new KeyAdapter()
	    {
	        public void keyPressed(KeyEvent e) 
	        {
	        	if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
	        		return;
	        	}
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    pos = textfield.getCaretPosition();
	                    if(textfield.getSelectedText() == null)
	                    {
	                        textfield.setCaretPosition(0);
	                        comboFilter(textfield.getText());
	                        textfield.setCaretPosition(pos);
	                    }
	                 }
	            });
	        }
	    });
	}

	public void comboFilter(String text) 
	{
	    Vector<Option> options = data.searchByName(text.trim());
	    if (options.size() > 0){
	        this.setModel(new DefaultComboBoxModel<Option>(options));
	        textfield.setCaretPosition(0);
	        this.setSelectedItem(text);
	        this.showPopup();
	    }
	    else {
	        this.hidePopup();
	    }
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

