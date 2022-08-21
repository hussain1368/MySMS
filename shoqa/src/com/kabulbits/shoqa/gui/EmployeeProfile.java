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
import com.kabulbits.shoqa.db.Employee;
import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class EmployeeProfile extends JFrame 
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static EmployeeProfile self;

	private JTabbedPane tabs;
	private EmployeeDetails panel;
	private EmployeeFinance finance;
	private EmployeeTimetable schedule;
	private BorrowsOfPerson borrows;
	
	private EmployeeData data;
	private Employee employee;
	
	public EmployeeProfile() {
		this(0);
	}
	
	public EmployeeProfile(int id)
	{
		if(id == 0 && Data.isTrial() && new Data().recordLimit("employee"))
		{
			Diags.showErrLang("trial_limitation_error");
			dispose();
			return;
		}
		isOpen = true;
		self = this;
		
		String title = Dic.w(id == 0 ? "reg_employee" : "employee_profile");
		setTitle(title);
		
		if(id == 0) {
			panel = new EmployeeDetails(0);
			add(panel);
		}
		else
		{
			data = new EmployeeData();
			employee = data.findEmployee(id);
			
			if(employee == null) {
				dispose();
				isOpen = false;
				return;
			}
			tabs = new JTabbedPane();
			tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			tabs.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), tabs.getBorder()));
			
			panel = new EmployeeDetails(employee);
			if(Data.PERM_FINANCE != 0){
				finance = new EmployeeFinance(id);
			}
			if(Data.PERM_COURSES != 0){
				schedule = new EmployeeTimetable(id);
			}
			if(Data.PERM_LIBRARY != 0){
				borrows = new BorrowsOfPerson(id, 2);
			}
			tabs.add(Dic.w("employee_details"), panel);
			tabs.add(Dic.w("finance"), finance);
			tabs.add(Dic.w("timetable"), schedule);
			tabs.add(Dic.w("library"), borrows);
			
			if(Data.PERM_FINANCE == 0){
				tabs.setEnabledAt(1, false);
			}
			if(Data.PERM_COURSES == 0){
				tabs.setEnabledAt(2, false);
			}
			if(Data.PERM_LIBRARY == 0){
				tabs.setEnabledAt(3, false);
			}
			if(!employee.isTeacher){
				tabs.setEnabledAt(2, false);
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
			public void windowClosing(WindowEvent e){
				isOpen = false;
			}
		});
		setMinimumSize(new Dimension(600, 610));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void activeTab(int index){
		tabs.setSelectedIndex(index);
	}
	
	public void render()
	{
		switch(tabs.getSelectedIndex()){
		case 2: if(schedule != null) schedule.render(); break;
		case 3: if(borrows != null) borrows.render(); break;
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
