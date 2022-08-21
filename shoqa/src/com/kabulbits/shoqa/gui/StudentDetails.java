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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.jdesktop.xswingx.PromptSupport;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.sheet.StudSheet;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OptionChooser;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentDetails extends JPanel{

	private static final long serialVersionUID = 1L;
	public StudentProfile frame;
	
	private JLabel pic;
	private JTextField fields[];
	private JButton updateCode;
	private JSpinner birthDate, docDate;
	private JTextField birthPlace;
	private JTextField mainVill;
	private JTextField currVill;
	private JComboBox<Integer> grade;
	private JComboBox<Course> course;
	private JComboBox<String> blood;
	private JComboBox<Option> mainProv;
	private JComboBox<Option> mainDist;
	private JComboBox<Option> currProv;
	private JComboBox<Option> currDist;
	private JRadioButton male, female;
	private JCheckBox unofficial;
	private ButtonGroup buttons;
	private JButton save, status, refresh, download, delete, print;
	
	private StudentData data = new StudentData();
	private MarkData markData = new MarkData();
	private Student student;
	private byte[] image = null;
	private Course curCourse;
	private Vector<Option> provinces;
	private int sid;
	
	public StudentDetails(Student student)
	{
		this(student.id);
		this.student = student;
		fillForm();
		if(Data.PERM_STUDENTS != 2){
			disableForm();
		}
	}
	
	public StudentDetails(int id)
	{
		this.sid = id;
		initPanel();
		if (sid == 0) {
			fields[0].setText(String.valueOf(data.maxStudentCode()));
			putCourses(1);
		}
	}

	private void initPanel()
	{
		String title = Dic.w(sid == 0 ? "reg_student" : "student_details");
		
		setLayout(new BorderLayout(0, 5));
		
		JPanel top = new Ribbon(title, false);
		add(top, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		String texts [] = {"base_code", "doc_no", "name", "lname", "fname", "gfname", 
				"mother_lang", "idcard_no", "phone_no", "father_job", "mother_job", "blood_group"};
		
		JLabel [] labels = new JLabel[11];
		fields = new JTextField[11];
		
		Reset reset = new Reset();
		ActionListener register = new BgWorker<Boolean>(this){
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
		};
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(3, 3, 3, 3);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		
		for (int i = 0; i < fields.length; i++) 
		{
			labels[i] = new JLabel(Dic.w(texts[i]));
			fields[i] = new JTextField();
			fields[i].addActionListener(register);
			fields[i].addFocusListener(reset);
			fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			fields[i].setHorizontalAlignment(JTextField.RIGHT);
			
			cons.gridy = i;
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			form.add(labels[i], cons);
			
			cons.gridx = 1;
			cons.weightx = 1;
			if(i > 1) cons.gridwidth = 3;
			cons.fill = GridBagConstraints.HORIZONTAL;
			form.add(fields[i], cons);
		}
		docDate = new JSpinner();
		docDate.setModel(new PDateModel(docDate));
		cons.gridy = 1;
		cons.gridx = 2;
		cons.gridwidth = 1;
		form.add(docDate, cons);
		
		unofficial = new JCheckBox(Dic.w("unofficial"));
		unofficial.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		cons.gridy = 1;
		cons.gridx = 3;
		cons.gridwidth = 1;
		form.add(unofficial, cons);
		
		String [] bloods = {"--", "A", "B", "AB", "O", "A-", "B-", "AB-", "O-"};
		blood = new JComboBox<String>(bloods);
		blood.setPrototypeDisplayValue("xxxxxxx");
		JLabel blabel = new JLabel(Dic.w("blood_group"));
		
		cons.gridy = 11;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(blabel, cons);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		form.add(blood, cons);
		
		JLabel glabel = new JLabel(Dic.w("gender"));
		male = new JRadioButton(Dic.w("male"));
		female = new JRadioButton(Dic.w("female"));
		male.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		female.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		buttons = new ButtonGroup();
		buttons.add(male);
		buttons.add(female);
		
		cons.gridwidth = 1;
		cons.gridy = 12;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(glabel, cons);
		
		cons.gridx = 1;
		form.add(male, cons);
		cons.gridx = 2;
		cons.weightx = 1;
		form.add(female, cons);
		
		birthDate = new JSpinner();
		birthDate.setModel(new PDateModel(birthDate));
		birthPlace = new JTextField();
		birthPlace.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		PromptSupport.setPrompt(Dic.w("birth_place"), birthPlace);
		JLabel birthLabel = new JLabel(Dic.w("birth"));
		
		cons.gridy = 13;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(birthLabel, cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		form.add(birthDate, cons);
		cons.gridx = 2;
		cons.gridwidth = 2;
		form.add(birthPlace, cons);
		
		provinces = data.provinces();
		
		mainProv = new JComboBox<>(provinces);
		mainProv.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)mainProv.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		mainProv.setMaximumRowCount(15);
		
		mainDist = new JComboBox<>();
		mainDist.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)mainDist.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		mainDist.setMaximumRowCount(15);
		
		mainDist.addItem(new Option(0 ,Dic.w("district")));
		mainVill = new JTextField(15);
		mainVill.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		PromptSupport.setPrompt(Dic.w("village"), mainVill);
		JLabel mainAddrLabel = new JLabel(Dic.w("main_addr"));
		
		cons.gridy = 14;
		cons.gridwidth = 1;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(mainAddrLabel, cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		form.add(mainProv, cons);
		cons.gridx = 2;
		form.add(mainDist, cons);
		cons.gridx = 3;
		form.add(mainVill, cons);
		
		currProv = new JComboBox<>(provinces);
		currProv.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)currProv.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		currProv.setMaximumRowCount(15);
		
		currDist = new JComboBox<>();
		currDist.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)currDist.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		currDist.setMaximumRowCount(15);		
		
		currDist.addItem(new Option(0, Dic.w("district")));
		currVill = new JTextField(15);
		currVill.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		PromptSupport.setPrompt(Dic.w("village"), currVill);
		JLabel currAddrLabel = new JLabel(Dic.w("curr_addr"));
		
		cons.gridy = 15;
		cons.gridwidth = 1;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(currAddrLabel, cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		form.add(currProv, cons);
		cons.gridx = 2;
		form.add(currDist, cons);
		cons.gridx = 3;
		form.add(currVill, cons);
		
		provinces = data.provinces();
		
		grade = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
		grade.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)grade.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

		course = new JComboBox<Course>();
		course.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)course.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		JLabel gradeLabel = new JLabel(Dic.w("course"));
		
		cons.gridy = 16;
		cons.weighty = 1;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(gradeLabel, cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx = 1;
		form.add(grade, cons);
		cons.gridx = 2;
		form.add(course, cons);
		updateCode = new JButton(new ImageIcon("images/refreshs.png"));
		updateCode.setMargin(new Insets(1, 3, 1, 3));
		updateCode.setToolTipText(Dic.w("new_base_code"));
		cons.gridx = 2;
		cons.gridy = 0;
		cons.weightx = 0;
		cons.weighty = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(updateCode, cons);
		
		//---------------photo panel----------------------
		
		JPanel picPanel = new JPanel(new GridBagLayout());
		
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

		JPanel center = new JPanel(new BorderLayout());
		center.add(form, BorderLayout.CENTER);
		center.add(picPanel, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);

		// Bottom Panel
		JPanel bottom = new JPanel(new BorderLayout());	
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		save = new JButton(Dic.w("save"));
		bright.add(save);
		
		if(sid != 0){
			status = new JButton(Dic.w("set_status"));
			
			refresh = new JButton(new ImageIcon("images/refresh.png"));
			refresh.setToolTipText(Dic.w("refresh"));
			refresh.setMargin(new Insets(2, 3, 2, 3));
			
			print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
			print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			print.setMargin(new Insets(2, 5, 2, 5));
			
			bright.add(refresh);
			bleft.add(print);
			bleft.add(status);
			
			status.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new StudentState(sid, 0);
				}
			});
			refresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					student = data.findStudent(sid, false);
					if(student == null){
						StudentProfile.isOpen = false;
						frame.dispose();
						return;
					}
					fillForm();
				}
			});
			print.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					print();
				}
			});
		}
		save.addActionListener(register);
		
		updateCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id = Integer.toString(data.maxStudentCode());
				fields[0].setText(id);
			}
		});
		
		if(Data.PERM_STUDENTS == 2){
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
		}
				
		grade.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(grade.getSelectedIndex() < 12){
					putCourses((int)grade.getSelectedItem());
				}
			}
		});
		mainProv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int prov = ((Option) mainProv.getSelectedItem()).key;
				mainDist.removeAllItems();
				
				if(prov < 1){
					mainDist.addItem(new Option(0, Dic.w("district")));
					return;
				}
				Vector<Option> dists = data.districts(prov);
				for(Option item : dists) {
					mainDist.addItem(item);
				}
			}
		});
		currProv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int prov = ((Option) currProv.getSelectedItem()).key;
				currDist.removeAllItems();
				
				if(prov < 1) {
					currDist.addItem(new Option(0, Dic.w("district")));
					return;
				}
				Vector<Option> dists = data.districts(prov);
				for(Option item : dists) {
					currDist.addItem(item);
				}

			}
		});
		unofficial.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(unofficial.isSelected())
				{
					fields[0].setText("");
					fields[1].setText("");
					docDate.setValue(null);
					fields[0].setEnabled(false);
					fields[1].setEnabled(false);
					docDate.setEnabled(false);
					updateCode.setEnabled(false);
				}
				else
				{
					docDate.setValue(new Date());
					fields[0].setEnabled(true);
					fields[1].setEnabled(true);
					docDate.setEnabled(true);
					updateCode.setEnabled(true);
				}
			}
		});
	}

	protected void deleteImg() {
		this.image = null;
		pic.setIcon(new ImageIcon("images/pic.jpg"));
	}

	public void fillForm() 
	{
		String info [] = student.array();
		
		for (int i = 0; i < fields.length; i++) {
			fields[i].setText(info[i]);
		}
		
		blood.setSelectedItem(student.bloodGroup);
		if(student.gender != null){
			if(student.gender.equals("f")){
				female.setSelected(true);
			}
		}
		if(student.gender.equals("m")){
			male.setSelected(true);
		}
		else if(student.gender.equals("f")){
			female.setSelected(true);
		}
		
		docDate.setValue(student.docDate);
		birthDate.setValue(student.birthDate);
			
		birthPlace.setText(student.birthPlace);
		
		mainProv.setSelectedItem(new Option(student.mainProv));
		if(student.mainProv > 0){
			Vector<Option> mainDistricts = data.districts(student.mainProv);
			for(Option item : mainDistricts){
				mainDist.addItem(item);
			}
		}
		else{
			mainDist.addItem(new Option(0, Dic.w("district")));
		}

		mainDist.setSelectedItem(new Option(student.mainDist));
		mainVill.setText(student.mainVill);
		
		currProv.setSelectedItem(new Option(student.currProv));
		if(student.currProv > 0){
			Vector<Option> currDistricts = data.districts(student.currProv);
			for(Option item : currDistricts){
				currDist.addItem(item);
			}
		}else{
			currDist.addItem(new Option(Dic.w("district")));
		}
		
		currDist.setSelectedItem(new Option(student.currDist));
		currVill.setText(student.currVill);
		
		grade.setSelectedItem(student.grade);
		putCourses(student.grade);
		
		image = student.image;
		
		if(image != null){
			pic.setIcon(new ImageIcon(image));
		}else{
			pic.setIcon(new ImageIcon("images/pic.jpg"));
		}
		
		if(student.official == 0){
			unofficial.setSelected(true);
		}else{
			unofficial.setSelected(false);
		}
	}
	
	public void disableForm()
	{
		for(JTextField field : fields){
			if(field != null){
				field.setEditable(false);
			}
		}
		updateCode.setEnabled(false);
		unofficial.setEnabled(false);
		docDate.setEnabled(false);
		
		blood.setEnabled(false);
		birthDate.setEnabled(false);
		birthPlace.setEditable(false);
		
		male.setEnabled(false);
		female.setEnabled(false);
		
		mainProv.setEnabled(false);
		mainDist.setEnabled(false);
		mainVill.setEditable(false);
		
		currProv.setEnabled(false);
		currDist.setEnabled(false);
		currVill.setEditable(false);
		
		grade.setEnabled(false);
		course.setEnabled(false);
		
		delete.setEnabled(false);
		download.setEnabled(false);
		save.setEnabled(false);
		refresh.setEnabled(false);
		status.setEnabled(false);
	}
	
	protected void download() 
	{
		if(image == null) return;
		
		FileDialog diag = new FileDialog(frame, "Save", FileDialog.SAVE);
		diag.setFile(String.format("st-%d.jpg", sid));
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
	
	private void putCourses(int g)
	{
		int y = Data.EDUC_YEAR;
		Vector<Course> courses = data.searchCourse(y, g);
		course.removeAllItems();
		course.addItem(null);
		for(Course item : courses){
			course.addItem(item);
		}
		curCourse = data.studentCourse(sid, g, y);
		course.setSelectedItem(curCourse);
	}
	
	private boolean doSave()
	{
		if (!validator()) {
			return false;
		}
		Student stud = new Student(sid);
		
		String values[] = new String[fields.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = fields[i].getText().trim();
		}
		stud.fill(values);
		
		stud.docDate = (Date) docDate.getValue();
		
		stud.bloodGroup = blood.getSelectedItem().toString();
		stud.gender = (male.isSelected())? "m" : "f";
		
		stud.birthDate = (Date) birthDate.getValue();
		stud.birthPlace = birthPlace.getText().trim();
		
		stud.mainProv = ((Option) mainProv.getSelectedItem()).key;
		stud.mainDist = ((Option) mainDist.getSelectedItem()).key;
		stud.mainVill = mainVill.getText().trim();
		
		stud.currProv = ((Option) currProv.getSelectedItem()).key;
		stud.currDist = ((Option) currDist.getSelectedItem()).key;
		stud.currVill = currVill.getText().trim();

		stud.grade = (int) grade.getSelectedItem();
		stud.official = unofficial.isSelected() ? 0 : 1;
		
		stud.image = image;
		if(sid == 0){
			return data.insertStudent(stud);
		}else{
			return data.editStudent(sid, stud);
		}
	}
	
	private void afterSave()
	{
		if(sid == 0){
			int id = data.insertId();
			int g = (int)grade.getSelectedItem();
			if(course.getSelectedItem() != null)
			{
				int cid = ((Course)course.getSelectedItem()).id;
				markData.enrolStudent(id, cid, g);
			}
			if(g > 1) {
				int res = Diags.showConfLang("transfer_conf", Diags.YN);
				if(res == 0) {
					new RegTransfer(id, g);
				}
			}
			int sig = Diags.showOps("save_success", new String [] {"create_new", "complete_info"});
			if(sig == -1){
				if(frame != null){
					frame.dispose();
					StudentProfile.isOpen = false;
				}
			}
			else if(sig == 0){
				clearForm();
			}
			else if(sig == 1){
				if(frame != null){
					frame.dispose();
					StudentProfile.isOpen = false;
				}
				new StudentProfile(id);
			}
		}else{
			if(course.getSelectedItem() != null)
			{
				int cid = ((Course)course.getSelectedItem()).id;
				int g = (int)grade.getSelectedItem();
				
				if(curCourse == null){
					markData.enrolStudent(sid, cid, g);
				}else{
					markData.switchStudent(sid, curCourse.id, cid);
				}
			}
			Diags.showMsg(Diags.SUCCESS);
		}
	}
	
	private void clearForm() 
	{
		for(JTextField field : fields)
		{
			field.setText("");
		}
		fields[0].setText(String.valueOf(data.maxStudentCode()));
		unofficial.setSelected(false);

		birthPlace.setText("");
		mainVill.setText("");
		currVill.setText("");
		
		Date date = new Date();
		docDate.setValue(date);
		birthDate.setValue(date);
		
		buttons.clearSelection();
		blood.setSelectedIndex(0);
		mainProv.setSelectedIndex(0);
		currProv.setSelectedIndex(0);
		grade.setSelectedIndex(0);
		mainDist.addItem(new Option(Dic.w("district")));
		currDist.addItem(new Option(Dic.w("district")));
		
		pic.setIcon(new ImageIcon("images/pic.jpg"));
		image = null;
	}

	private int [] required;
	private int [] numeric;
	
	private boolean validator()
	{
		if(unofficial.isSelected()){
			required = new int [] {2, 4, 5, 7};
			numeric = new int [] {7};
		}else{
			required = new int [] {0, 1, 2, 4, 5, 7};
			numeric = new int [] {0, 1, 7};
		}
		
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
		if(buttons.getSelection() == null)
		{
			Diags.showErrLang("choose_gender_error");
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
		String[] opts = {"only_details", "details_and_marks"};
		final int task = new OptionChooser("print_options", opts).signal;
		if(task != -1){
			final String path = Helper.xlsx("form", frame);
			if(path == null) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					StudentDetails.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					StudSheet sheet = new StudSheet(sid);
					sheet.studDetails(student);
					if(task == 1){
						sheet.fullMarks(student.grade);
					}
					sheet.build(path);
					StudentDetails.this.setCursor(Cursor.getDefaultCursor());
				}
			}).start();
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}
