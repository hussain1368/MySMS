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
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class LibraryMemProfile extends JFrame{

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static LibraryMemProfile self;
	
	private JTabbedPane tabs;
	private LibraryMemDetails details;
	private BorrowsOfPerson borrows;
	
	private LibraryData data;

	public LibraryMemProfile(){
		this(0);
	}
	public LibraryMemProfile(int id)
	{
		if(id == 0 && Data.isTrial() && new Data().recordLimit("library_member"))
		{
			Diags.showErrLang("trial_limitation_error");
			dispose();
			return;
		}
		isOpen = true;
		self = this;
		
		String title = Dic.w(id == 0 ? "external_member_reg" : "external_member_profile");
		setTitle(title);
		
		if(id == 0){
			details = new LibraryMemDetails(id);
			add(details);
		}else{
			data = new LibraryData();
			Object [] info = data.findMember(id);
			if(info == null){
				dispose();
				isOpen = false;
				return;
			}
			tabs = new JTabbedPane();
			tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			tabs.setBorder(new CompoundBorder(new EmptyBorder(4, 4, 4, 4), tabs.getBorder()));
			
			details = new LibraryMemDetails(id, info);
			borrows = new BorrowsOfPerson(id, 3);
			
			tabs.add(Dic.w("external_member_details"), details);
			tabs.add(Dic.w("books"), borrows);
			
			add(tabs);
			tabs.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					render();
				}
			});
		}
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(550, 500));
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
		case 1: if(borrows != null) borrows.render(); break;
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
