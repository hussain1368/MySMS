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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class RegTransfer extends JDialog {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static RegTransfer self;
	
	public StudentTransfers parent;
	private JRadioButton entrance, detach;
	private JTextField stCode, transNo, docNo, reason, school;
	private JSpinner transDate, docDate;
	private JTextArea desc;
	
	private StudentData data;
	private int sid = 0;
	private int tid = 0;
	private int grade;
	
	public RegTransfer(int id, StudentTransfers parent) {
		this(0, 0, parent);
		tid = id;
		fillForm();
		display();
	}
	
	public RegTransfer(int id) {
		this(0, 0, null);
		tid = id;
		fillForm();
		display();
	}
	
	public RegTransfer(int id, int g) {
		this(id, g, null);
	}
	
	public RegTransfer(int id, int g, StudentTransfers parent)
	{
		isOpen = true;
		sid = id;
		grade = g;
		data = new StudentData();
		
		this.parent = parent;
		
		String title = Dic.w(sid == 0 ? "edit_transfer" : "reg_transfer");
		setTitle(title);
		
		setLayout(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new GridBagLayout());
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		center.setBorder(new CompoundBorder(new EmptyBorder(3 ,3, 3, 3), new MatteBorder(0, 0, 1, 0, Color.GRAY)));
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(3, 3, 3, 3);
		
		entrance = new JRadioButton(Dic.w("entrance"));
		detach = new JRadioButton(Dic.w("detach"));
		ButtonGroup group = new ButtonGroup();
		group.add(entrance);
		group.add(detach);
		
		stCode = new JTextField(15);
		transNo = new JTextField();
		docNo = new JTextField();
		transDate = new JSpinner();
		transDate.setModel(new PDateModel(transDate));
		docDate = new JSpinner();
		docDate.setModel(new PDateModel(docDate));
		reason = new JTextField();
		school = new JTextField();
		desc = new JTextArea(3, 0);
		desc.setBorder(new LineBorder(Color.GRAY));
//		desc.setFont(new Font("tahoma", Font.PLAIN, 12));
		
		reason.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		school.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		desc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		entrance.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		detach.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		JLabel transType, docLabel, transLabel, reasLabel, schoolLabel, descLabel;
		transType = new JLabel(Dic.w("prev_st_code"));
		docLabel = new JLabel(Dic.w("doc_no_date"));
		transLabel = new JLabel(Dic.w("trans_no_date"));
		reasLabel = new JLabel(Dic.w("reason"));
		descLabel = new JLabel(Dic.w("description"));
		schoolLabel = new JLabel(Dic.w("school"));
		
		cons.gridx = 0;
		center.add(transType, cons);
		center.add(transLabel, cons);
		center.add(docLabel, cons);
		center.add(reasLabel, cons);
		center.add(schoolLabel, cons);
		center.add(descLabel, cons);
		
		cons.gridx = 1;
		cons.gridy = 0;
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		
		center.add(stCode, cons);
		cons.gridx = 2;
		center.add(entrance, cons);
		cons.gridx = 3;
		center.add(detach, cons);
		
		cons.gridy++;
		cons.gridx = 1;
		cons.gridwidth = 1;
		center.add(transNo, cons);
		cons.gridx = 2;
		cons.gridwidth = 2;
		center.add(transDate, cons);
		
		cons.gridy++;
		cons.gridx = 1;
		cons.gridwidth = 1;
		center.add(docNo, cons);
		cons.gridx = 2;
		cons.gridwidth = 2;
		center.add(docDate, cons);
		
		cons.gridwidth = 3;
		cons.gridx = 1;
		cons.gridy++;
		center.add(reason, cons);
		cons.gridy++;
		center.add(school, cons);
		cons.gridy++;
		cons.weighty = 1;
		center.add(desc, cons);
		
		add(center, BorderLayout.CENTER);
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout());
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom,BorderLayout.SOUTH);
		
		JButton save = new JButton(Dic.w("save"));
		JButton cancel = new JButton(Dic.w("cancel"));
		bleft.add(save);
		bleft.add(cancel);
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
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
		stCode.addFocusListener(new Reset());
		transNo.addFocusListener(new Reset());
		docNo.addFocusListener(new Reset());
		
		if(sid != 0){
			detach.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(detach.isSelected()){
						String code = Integer.toString(data.findStCode(sid));
						stCode.setText(code);
					}
				}
			});
			entrance.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(entrance.isSelected()){
						Map<String, Object> info = data.studDocInfo(sid);
						if(info != null){
							docNo.setText(info.get("doc_no").toString());
							docDate.setValue(info.get("doc_date"));
						}
					}
				}
			});
		}
		Helper.esc(this);
		if(sid != 0){
			display();
		}
	}
	private void display()
	{
		setSize(450, 330);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private void fillForm()
	{
		if(tid < 1) return;
		Map<String, Object> info = data.findTransfer(tid);
		if(info == null) {
			dispose();
			return;
		}
		String type = info.get("type").toString();
		if(type.equals("in")){
			entrance.setSelected(true);
		}else if(type.equals("out")){
			detach.setSelected(true);
		}
		if(info.get("prev_code") != null) stCode.setText(info.get("prev_code").toString());
		if(info.get("trans_no") != null) transNo.setText(info.get("trans_no").toString());
		if(info.get("doc_no") != null) docNo.setText(info.get("doc_no").toString());
		if(info.get("reason") != null) reason.setText(info.get("reason").toString());
		if(info.get("school") != null) school.setText(info.get("school").toString());
		if(info.get("desc") != null) desc.setText(info.get("desc").toString());
		
		transDate.setValue(info.get("trans_date"));
		docDate.setValue(info.get("doc_date"));
	}

	private boolean doSave()
	{
		String type = "";
		if(entrance.isSelected()) type = "in";
		if(detach.isSelected()) type = "out";
		
		if(type.length() < 1) {
			Diags.showErrLang("trans_type_error");
			return false;
		}
		JTextField valid [] = {stCode, transNo, docNo};
		
		for(JTextField field : valid){
			if(field.getText().trim().length() < 1){
				field.setBackground(Color.RED);
				Diags.showErrLang("required_error");
				return false;
			}
			if(!Helper.isNumeric(field.getText().trim(), false)){
				field.setBackground(Color.RED);
				Diags.showErrLang("numeric_error");
				return false;
			}
		}
		Map<String, Object> values = new HashMap<String, Object>();
		
		values.put("type", type);
		values.put("prev_code", stCode.getText().trim());
		values.put("trans_no", transNo.getText().trim());
		values.put("doc_no", docNo.getText().trim());
		values.put("reason", reason.getText().trim());
		values.put("school", school.getText().trim());
		values.put("desc", desc.getText().trim());
		
		values.put("trans_date", transDate.getValue());
		values.put("doc_date", docDate.getValue());
		
		if(sid != 0){
			return data.transferStudent(sid, grade, values);
		}else{
			return data.editTransfer(tid, values);
		}
	}
	private void afterSave()
	{
		if(sid != 0){
			dispose();
			Diags.showMsg(Diags.SUCCESS);
			isOpen = false;
			if(parent != null) {
				parent.render();
			}
		}
		else if(tid != 0)
		{
			dispose();
			new StudentState(0, tid);
			isOpen = false;
			if(parent != null){
				parent.render();
			}
		}
	}
}

class StudentState extends JDialog{

	private static final long serialVersionUID = 1L;
	private JRadioButton active, passive;
	private StudentData data;
	private int stid = 0, trid = 0;
	
	public StudentState(int sid, int tid)
	{
		data = new StudentData();
		stid = sid;
		trid = tid;
		
		String title = Dic.w("set_st_status");
		JPanel top = new Ribbon(title, false);
		add(top, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new GridBagLayout());
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		active = new JRadioButton(Dic.w("active"));
		passive = new JRadioButton(Dic.w("passive"));
		active.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		passive.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		ButtonGroup group = new ButtonGroup();
		group.add(active);
		group.add(passive);
		
		JLabel label = new JLabel(Dic.w("st_status"));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(4, 4, 4, 4);
		cons.weighty = 1;
		cons.weightx = 1;
		cons.gridy = 0;
		
		center.add(label, cons);
		center.add(active, cons);
		center.add(passive, cons);
		
		add(center, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		JButton save , cancel;
		save = new JButton(Dic.w("save"));
		cancel = new JButton(Dic.w("cancel"));
		bottom.add(save);
		bottom.add(cancel);
		
		add(bottom, BorderLayout.SOUTH);
		
		Helper.esc(this);
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		setSize(300 ,160);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		setVisible(true);
	}

	protected void save() 
	{
		String state = "";
		if(active.isSelected()){
			state = "a";
		}else if(passive.isSelected()){
			state = "p";
		}
		if(state.length() < 1) return;
		
		if(data.updateState(stid, trid, state))
		{
			dispose();
			Diags.showMsg(Diags.SUCCESS);
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
























