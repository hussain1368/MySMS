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

public class BookProfile extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static BookProfile self;
	
	private JTabbedPane tabs;
	private BookDetails details;
	private BookBorrows borrows;
	
	private LibraryData data;
	
	public BookProfile(){
		this(0);
	}
	
	public BookProfile(int id)
	{
		if(id == 0 && Data.isTrial() && new Data().recordLimit("book"))
		{
			Diags.showErrLang("trial_limitation_error");
			dispose();
			return;
		}
		isOpen = true;
		self = this;
		
		String title = Dic.w(id == 0 ? "book_register" : "book_profile");
		setTitle(title);
		
		if(id == 0){
			details = new BookDetails(0);
			add(details);
		}else{
			data = new LibraryData();
			Object info [] = data.findBook(id);
			if(info == null){
				dispose();
				isOpen = false;
				return;
			}
			
			tabs = new JTabbedPane();
			tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			tabs.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), tabs.getBorder()));
			
			details = new BookDetails(id, info);
			details.frame = this;
			borrows = new BookBorrows(id);
			
			tabs.add(details, Dic.w("book_details"));
			tabs.add(borrows, Dic.w("borrows_list"));
			
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
		
		setMinimumSize(new Dimension(550, 550));
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
