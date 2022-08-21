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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class FinanPeriodReg extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField periodName;
	private JSpinner startDate, endDate;
	
	private FinanceData data;
	private int pid;

	public FinanPeriodReg(){
		this(0);
	}
	
	public FinanPeriodReg(int id){
		pid = id;
		data = new FinanceData();
		
		String title = Dic.w(id == 0 ? "create_financial_period" : "edit_financial_period");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel form = new JPanel(new GridBagLayout());
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(form, BorderLayout.CENTER);
		
		periodName = new JTextField();
		periodName.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		periodName.addFocusListener(new Reset());
		
		startDate = new JSpinner();
		endDate = new JSpinner();
		startDate.setModel(new PDateModel(startDate));
		endDate.setModel(new PDateModel(endDate));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 4, 0, 4);
		
		cons.gridx = 0;
		cons.gridy = 0;
		form.add(new JLabel(Dic.w("period_name")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("start_date")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("end_date")), cons);
		
		cons.weightx = 1;
		cons.gridx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		
		cons.gridy = 0;
		form.add(periodName, cons);
		cons.gridy++;
		form.add(startDate, cons);
		cons.gridy++;
		cons.weighty = 1;
		form.add(endDate, cons);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		add(bottom, BorderLayout.SOUTH);
		
		JButton save, cancel;
		save = new JButton(Dic.w("save"));
		cancel = new JButton(Dic.w("cancel"));
		
		bottom.add(cancel);
		bottom.add(save);
		
		if(pid != 0){
			Object info [] = data.findFinanPeriod(pid);
			if(info == null){
				dispose();
				return;
			}
			periodName.setText(info[0].toString());
			startDate.setValue(info[1]);
			endDate.setValue(info[2]);
		}
		save.addActionListener(new BgWorker<Boolean>(this){
			@Override
			protected Boolean save(){
				return doSave();
			}
			@Override
			protected void finish(Boolean result){
				if(result){
					dispose();
					Diags.showMsg(Diags.SUCCESS);
				}
			}
		});
		cancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		});
		
		Helper.esc(this);
		setSize(350, 200);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}

	private boolean doSave() 
	{
		if(!validator()) return false;
		
		String name = periodName.getText().trim();
		Date start = (Date) startDate.getValue();
		Date end = (Date) endDate.getValue();
		if(pid == 0){
			if(data.createFinanPeriod(name, start, end)){
				if(Diags.showConfLang("period_set_active_conf", Diags.YN) == 0){
					int id = data.insertId();
					return data.setCurrentFinanPeriod(id);
				}
				return true;
			}
		}else{
			return data.editFinanPeriod(pid, name, start, end);
		}
		return false;
	}

	private boolean validator() 
	{
		if(periodName.getText().trim().length() == 0){
			periodName.setBackground(Color.RED);
			Diags.showErrLang("required_error");
			return false;
		}
		Date start = (Date) startDate.getValue();
		Date end = (Date) endDate.getValue();
		if(start == null || end == null){
			Diags.showErrLang("start_and_end_dates_are_required");
			return false;
		}
		if(start.equals(end)){
			Diags.showErrLang("start_and_end_date_cant_be_the_same");
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

























