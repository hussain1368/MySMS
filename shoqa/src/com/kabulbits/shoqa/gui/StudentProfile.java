package com.kabulbits.shoqa.gui;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class StudentProfile extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static StudentProfile self;
	
	private JTabbedPane tabs;
	private StudentDetails panel;
	private StudentTransfers transfers;
	private StudentRelations relations;
	private StudentMarks marks;
	private StudentAttendance attendance;
	private StudentFinance finance;
	private BorrowsOfPerson borrows;
	
	protected StudentData data;
	protected Student student;
	private int sid;
	
	public StudentProfile() {
		this(0);
	}
	
	public StudentProfile(int id)
	{
		data = new StudentData();
		if(id == 0 && Data.isTrial() && data.recordLimit("students"))
		{
			Diags.showErrLang("trial_limitation_error");
			dispose();
			return;
		}
		isOpen = true;
		self = this;
		
		sid = id;
		String title = Dic.w(id == 0 ? "reg_student" : "student_profile");
		setTitle(title);
		
		if(id == 0){
			panel = new StudentDetails(0);
			add(panel);
		}
		else
		{
			student = data.findStudent(sid, false);
			
			if(student == null){
				isOpen = false;
				dispose();
				return;
			}
			panel = new StudentDetails(student);
			
			if(Data.PERM_STUDENTS != 0){
				transfers = new StudentTransfers(student);
				relations = new StudentRelations(sid);
			}
			if(Data.PERM_COURSES != 0){
				marks = new StudentMarks(sid, student.grade);
				attendance = new StudentAttendance(sid);
				marks.frame = this;
				marks.student = student;
			}
			if(Data.PERM_FINANCE != 0) {
				finance = new StudentFinance(sid);
			}
			if(Data.PERM_LIBRARY != 0) {
				borrows = new BorrowsOfPerson(sid, 1);
			}
			tabs = new JTabbedPane();
			tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			tabs.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), tabs.getBorder()));
			
			tabs.add(Dic.w("student_details"), panel);
			tabs.add(Dic.w("transfers"), transfers);
			tabs.add(Dic.w("relations"), relations);
			tabs.add(Dic.w("marks"), marks);
			tabs.add(Dic.w("attendance"), attendance);
			tabs.add(Dic.w("finance"), finance);
			tabs.add(Dic.w("library"), borrows);
			
			if(Data.PERM_STUDENTS == 0){
				tabs.setEnabledAt(1, false);
				tabs.setEnabledAt(2, false);
			}
			if(Data.PERM_COURSES == 0){
				tabs.setEnabledAt(3, false);
				tabs.setEnabledAt(4, false);
			}
			if(Data.PERM_FINANCE == 0){
				tabs.setEnabledAt(5, false);
			}
			if(Data.PERM_LIBRARY == 0){
				tabs.setEnabledAt(6, false);
			}
			add(tabs);
			tabs.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					render();
				}
			});
		}
		panel.frame = this;
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(580, 620));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void activeTab(int index) {
		tabs.setSelectedIndex(index);
	}
	public void render()
	{
		switch(tabs.getSelectedIndex()){
		case 1: if(transfers != null) transfers.render(); break;
		case 2: if(relations != null) relations.render(); break;
		case 3: if(marks != null) marks.render(); break;
		case 4: if(attendance != null) attendance.render(); break;
		case 5: if(finance != null) finance.render(); break;
		case 6: if(borrows != null) borrows.render(); break;
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
