package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Employee;
import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.sheet.EmployeeForm;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OptionChooser;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class EmployeeDetails extends JPanel 
{
	private static final long serialVersionUID = 1L;
	public EmployeeProfile frame;
	
	private JLabel pic;
	private JTextField code, fields [];
	private JSpinner birthDate, employDate, leaveDate;
	private JComboBox<String> empType, educLevel;
	private JCheckBox isTeacher;
	private JButton download, delete, save, refresh, print;
	
	private byte[] image = null;
	private EmployeeData data;
	private Employee employee;
	private int empid;
	
	public EmployeeDetails(Employee employee)
	{
		this(employee.id);
		this.employee = employee;
		fillForm(employee);
		if(Data.PERM_EMPLOYEES != 2){
			disableForm();
		}
	}

	public EmployeeDetails(int id)
	{
		empid = id;
		data = new EmployeeData();
		
		String title = Dic.w(id == 0 ? "reg_employee" : "employee_details");
		
		setLayout(new BorderLayout(0, 5));
		
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new BorderLayout());
		center.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		JPanel form = new JPanel(new GridBagLayout());
		JPanel picPanel = new JPanel(new GridBagLayout());
		
		JScrollPane pane = new JScrollPane(form);
		Border border = new CompoundBorder(new EmptyBorder(4, 4, 4, 4), pane.getBorder());
		pane.setBorder(border);
		center.add(pane, BorderLayout.CENTER);
		center.add(picPanel, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		String texts [] = {"name", "lname", "fname", "gfname", "idcard_no", "phone", "main_addr", "curr_addr", "birth_date",
				"educ_level", "educ_field", "gradu_place", "gradu_year", "service_duration", "previous_job",
				"ngo_work_experience", "educ_seminars", "national_langs", "international_langs",
				"abroad_tours", "province_tours", "crimes", "punishments"};
		
		JLabel labels [] = new JLabel[texts.length];
		fields = new JTextField [texts.length];
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(3, 3, 3, 3);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		
		cons.gridy = 0;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.gridwidth = 1;
		
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("code")), cons);
		
		code = new JTextField(10);
		code.setHorizontalAlignment(JTextField.RIGHT);
		code.setBackground(Color.WHITE);
		code.setEditable(false);
		
		cons.gridx = 1;
		form.add(code, cons);
		
		Reset reset = new Reset();
		
		for(int i=0; i<fields.length; i++)
		{
			labels[i] = new JLabel(Dic.w(texts[i]));
			
			cons.gridy++;
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.gridwidth = 1;
			cons.fill = GridBagConstraints.NONE;
			form.add(labels[i], cons);
			
			if(i == 8 || i == 9) continue;

			fields[i] = new JTextField();
			fields[i].addFocusListener(reset);
			fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			
			cons.gridx = 1;
			cons.weightx = 1;
			cons.gridwidth = 2;
			cons.fill = GridBagConstraints.HORIZONTAL;
			form.add(fields[i], cons);
		}
		
		birthDate = new JSpinner();
		birthDate.setModel(new PDateModel(birthDate));
		
		cons.gridy = 9;
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(birthDate, cons);
		
		String educOptions [] = {
				"---",
				Dic.w("illiterate"),
				Dic.w("essential_literacy"),
				Dic.w("12th_grade"),
				Dic.w("14th_grade"),
				Dic.w("bachelor"),
				Dic.w("masters"),
				Dic.w("phd"),
		};
		educLevel = new JComboBox<String>(educOptions);
		educLevel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)educLevel.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		cons.gridy = 10;
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(educLevel, cons);
		
		cons.gridy = texts.length + 1;
		
		cons.gridy++;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("employee_type")), cons);
		
		String types [] = {
				"---",
				Dic.w("instructive"),
				Dic.w("administrative"),
				Dic.w("services"),
		};
		empType = new JComboBox<String>(types);
		empType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)empType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(empType, cons);
		
		isTeacher = new JCheckBox(Dic.w("teacher"));
		isTeacher.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		cons.gridx = 2;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(isTeacher, cons);
		
		employDate = new JSpinner();
		leaveDate = new JSpinner();
		employDate.setModel(new PDateModel(employDate));
		leaveDate.setModel(new PDateModel(leaveDate));
		
		leaveDate.setValue(null);
		
		cons.gridy++;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("employment_date")), cons);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(employDate, cons);
		
		cons.gridy++;
		cons.weighty = 1;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("leave_date")), cons);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(leaveDate, cons);
		
		//=== Photo Panel ===
		pic = new JLabel(new ImageIcon("images/pic.jpg"));
		pic.setPreferredSize(new Dimension(150, 200));
		pic.setBorder(new LineBorder(Color.BLACK));
		pic.setToolTipText(Dic.w("pic_tooltip"));
		pic.setBackground(Color.WHITE);
		
		download = new JButton(new ImageIcon("images/save.png"));
		delete = new JButton(new ImageIcon("images/cross.png"));
		download.setToolTipText(Dic.w("download_pic"));
		delete.setToolTipText(Dic.w("delete_pic"));
		
		GridBagConstraints cons1 = new GridBagConstraints();
		cons1.anchor = GridBagConstraints.NORTH;
		cons1.insets = new Insets(2, 2, 2, 2);
		cons1.gridx = 0;
		cons1.weighty = 0;
		cons1.gridwidth = 2;
		picPanel.add(pic, cons1);
		
		cons1.weighty = 1;
		cons1.gridwidth = 1;
		cons1.gridy = 1;
		cons1.weightx = 1;
		cons1.fill = GridBagConstraints.HORIZONTAL;
		picPanel.add(download, cons1);
		cons1.gridx = 1;
		picPanel.add(delete, cons1);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		save = new JButton(Dic.w("save"));
		bright.add(save);
		
		if(Data.PERM_EMPLOYEES == 2){
			empType.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(empType.getSelectedIndex() == 1){
						isTeacher.setSelected(true);
					}else{
						isTeacher.setSelected(false);
					}
				}
			});
			pic.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					addImage();
				}
			});
			download.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					download();
				}
			});
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteImg();
				}
			});
			save.addActionListener(new BgWorker<Boolean>(EmployeeDetails.this) {
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
		}
		if(empid != 0)
		{
			refresh = new JButton(new ImageIcon("images/refresh.png"));
			refresh.setToolTipText(Dic.w("refresh"));
			refresh.setMargin(new Insets(2, 3, 2, 3));
			
			print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
			print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			print.setMargin(new Insets(2, 5, 2, 5));

			bright.add(refresh);
			bleft.add(print);
			
			if(Data.PERM_EMPLOYEES == 2){
				refresh.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Employee employee = data.findEmployee(empid);
						if(employee == null){
							frame.dispose();
							StudentProfile.isOpen = false;
							return;
						}
						EmployeeDetails.this.employee = employee;
						fillForm(employee);
					}
				});
			}
			print.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					print();
				}
			});
		}
	}
	
	private void disableForm()
	{
		for(int i=0; i<fields.length; i++){
			if(fields[i] != null){
				fields[i].setEditable(false);
			}
		}
		birthDate.setEnabled(false);
		employDate.setEnabled(false);
		leaveDate.setEnabled(false);
		empType.setEnabled(false);
		educLevel.setEnabled(false);
		isTeacher.setEnabled(false);
		save.setEnabled(false);
		refresh.setEnabled(false);
		delete.setEnabled(false);
		download.setEnabled(false);
	}
	
	protected void deleteImg() {
		this.image = null;
		pic.setIcon(new ImageIcon("images/pic.jpg"));
	}

	protected void download() 
	{
		if(image == null){
			return;
		}
		FileDialog diag = new FileDialog(frame, "Save", FileDialog.SAVE);
		diag.setFile(String.format("emp-%d.jpg", empid));
		diag.setVisible(true);
		String fn = diag.getFile();
		if(fn == null) return;
		if(!fn.toLowerCase().endsWith(".jpg")){
			fn += ".jpg";
		}
		String path = diag.getDirectory() + fn;
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(image);
			fos.close();
		} 
		catch (Exception e) {
			Diags.showErrLang("cant_save_file");
		}
	}
	
	public void fillForm(Employee employee)
	{
		if(employee == null) return;
		
		String values [] = employee.values();
		
		code.setText(String.valueOf(employee.id));
		
		int i = 0, j = 0;
		for(; i<fields.length; i++) {
			if(i == 8 || i == 9) continue;
			fields[i].setText(values[j++]);
		}
		educLevel.setSelectedIndex(employee.educLevel);
		empType.setSelectedIndex(employee.empType);
		isTeacher.setSelected(employee.isTeacher);
		
		birthDate.setValue(employee.birthDate);
		employDate.setValue(employee.employDate);
		leaveDate.setValue(employee.leaveDate);
		
		image = employee.image;
		if(image != null){
			pic.setIcon(new ImageIcon(image));
		}else{
			pic.setIcon(new ImageIcon("images/pic.jpg"));
		}
	}

	private boolean doSave() 
	{
		if(!validator()) return false;
		
		Employee emp = new Employee();
		String values [] = new String[fields.length];
		int i = 0;
		for(JTextField field : fields){
			if(field != null){
				values[i++] = field.getText();
			}
		}
		emp.fill(values);
		
		emp.educLevel = educLevel.getSelectedIndex();
		emp.empType = empType.getSelectedIndex();
		emp.isTeacher = isTeacher.isSelected();
		
		emp.birthDate = (Date) birthDate.getValue();
		emp.employDate = (Date) employDate.getValue();
		emp.leaveDate = (Date) leaveDate.getValue();
		
		emp.image = image;
		
		if(empid == 0){
			return data.saveEmployee(emp);
		}
		else{
			return data.updateEmployee(empid, emp);
		}
	}
	private void afterSave()
	{
		if(empid == 0){
			int sig = Diags.showOps("save_success", new String [] {"create_new", "complete_info"});
			if(sig == -1){
				if(frame != null) {
					frame.dispose();
					EmployeeProfile.isOpen = false;
				}
			}
			else if(sig == 0) {
				clearForm();
			}
			else if(sig == 1){
				if(frame != null) frame.dispose();
				new EmployeeProfile(data.insertId());
			}
		}else{
			Diags.showMsg(Diags.SUCCESS);
		}
	}
	
	private void clearForm() 
	{
		for(JTextField field : fields) {
			if(field != null){
				field.setText("");
			}
		}
		educLevel.setSelectedIndex(0);
		empType.setSelectedIndex(0);
		
		birthDate.setValue(new Date());
		employDate.setValue(new Date());
		leaveDate.setValue(null);
		
		pic.setIcon(new ImageIcon("images/pic.jpg"));
		image = null;
	}

	private int required [] = {0, 2, 3, 4, 5, 6, 7};
	private int numeric [] = {4, 12};
	
	private boolean validator()
	{
		for(int index : required)
		{
			String value = fields[index].getText().trim();
			if(value.length() < 1)
			{
				fields[index].setBackground(Color.RED);
				Diags.showErrLang("required_error");
				return false;
			}
		}
		for(int index : numeric)
		{
			String value = fields[index].getText().trim();
			if(!Helper.isNumeric(value, false))
			{
				fields[index].setBackground(Color.RED);
				Diags.showErrLang("numeric_error");
				return false;
			}
		}
		if(educLevel.getSelectedIndex() == 0)
		{
			Diags.showErrLang("educ_level_error");
			return false;
		}
		if(empType.getSelectedIndex() == 0)
		{
			Diags.showErrLang("employee_type_error");
			return false;
		}
		return true;
	}
	private void addImage()
	{
		String path = Helper.pickPhoto(frame);
		if(path == null) return;
		image = Helper.photo(path);
		if(image != null){
			pic.setIcon(new ImageIcon(image));
		}
	}
	private void print()
	{
		String[] opts = {"attributes_form", "manager_form"};
		final int task = new OptionChooser("print_options", opts).signal;
		if(task != -1){
			final String path = Helper.xlsx("form", frame);
			if(path == null) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					EmployeeDetails.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					EmployeeForm form = new EmployeeForm(employee);
					if(task == 0){
						form.simple();
					}else{
						form.manager();
					}
					form.build(path);
					EmployeeDetails.this.setCursor(Cursor.getDefaultCursor());
				}
			}).start();
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






















