package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;
	public JFrame self = this;
	private Data db;

	public Main()
	{
		String title = Dic.w("app_title");
		setTitle(title);
		
		db = new Data();
		db.loadConfigs();
		
		String mainLabels [] = {"students_and_employees", "subjects_and_courses", "financial_definitions", "financial_transactions", "library", "users", "settings"};

		String subLabels [][] = {
				{"reg_student", "search_students", "search_relations", "students_transfers", "reg_employee", "search_employees"},
				{"subjects_definition", "search_and_create_courses", "timetable_wizard", "remove_timetables", "reset_students_status"},
				{"financial_periods", "cashes_definition", "students_costs_definition", "revenue_and_expense"},
				{"financial_period_report", "students_costs_report", "students_payments", "students_discount_and_subtract", "employee_salaries", "cashes_debit_credit", "revenue_and_expense"},
				{"borrow_reg", "search_borrowed_books", "external_member_reg", "external_member_search", "book_register", "search_books"},
				{"user_reg", "search_users"},
				{"settings"},
		};
		Class<?> Frames [][] = { 
				{
					StudentProfile.class,
					StudentSearch.class, 
					SearchRelations.class, 
					TransferredStudents.class,
					EmployeeProfile.class,
					EmployeeSearch.class,
				},
				{
					Subjects.class, 
					CourseFrame.class, 
					TimetableWizard.class,
					null, null,
				},
				{
					FinancialPeriods.class,
					Cashes.class,
					CostsDefinition.class,
					Accounts.class,
				},
				{
					FinanPeriodReport.class,
					CostsReport.class,
					StudentsAllPayments.class,
					StudentsAllDiscounts.class,
					EmployeeSalaries.class,
					CashTransaction.class,
					AccountTransaction.class,
				},
				{
					BookBorrowReg.class,
					BorrowSearch.class,
					LibraryMemProfile.class,
					LibraryMemSearch.class,
					BookProfile.class,
					BookSearch.class,
				},
				{
					UserProfile.class,
					UserSearch.class,
				},
				{
					Settings.class,
				}
		};
		String menuicons [][] = {
				{"images/user.png", "", "", "", "images/adim.png", "",},
				{"images/book_open.png", "", "images/time.png", "", ""},
				{"", "", "", ""},
				{"images/dollar.png", "images/statistics.png", "images/coin_gold.png", "", "images/coin_silver.png", "images/cash.png", "images/coin_cooper.png"},
				{"images/bookmark.png", "", "images/member.png", "", "images/book_add.png", ""},
				{"images/user_red.png", ""},
				{"images/settings.png"},
		};
		JMenuBar menuBar = new JMenuBar();
		menuBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		JMenu [] menus = new JMenu[mainLabels.length];
		Font font = new Font("tahoma", Font.PLAIN, 11);
		setJMenuBar(menuBar);
		
		for (int i = 0; i < mainLabels.length; i++) 
		{
			menus[i] = new JMenu(Dic.w(mainLabels[i]));
			menus[i].setFont(font);
			menus[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			menuBar.add(menus[i]);
			
			for(int j = 0; j < subLabels[i].length; j++)
			{
				JMenuItem item;
				item = new JMenuItem(Dic.w(subLabels[i][j]));
				item.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				item.setHorizontalAlignment(JMenuItem.RIGHT);
				item.setPreferredSize(new Dimension(200, 20));
				item.setFont(font);
				menus[i].add(item);
				if(Frames[i][j] != null){
					item.addActionListener(new OpenFrame(Frames[i][j], Main.this));
				}
				if(menuicons[i][j].length() != 0){
					item.setIcon(new ImageIcon(menuicons[i][j]));
				}
			}
		}
		menus[1].getItem(3).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Diags.showWarn("remove_timetables_warn");
				if(Diags.showConfLang("remove_timetables_conf", Diags.YN) == 0){
					if(db.truncateSchedules()){
						Diags.showMsgLang("all_timetables_removed");
					}
				}
			}
		});
		menus[1].getItem(4).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Diags.showWarn("reset_students_state_warn");
				if(Diags.showConfLang("reset_students_state_conf", Diags.YN) == 0){
					if(db.resetStudentsState()){
						Diags.showMsgLang("all_students_set_passive");
					}
				}
			}
		});
		menus[0].getItem(0).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menus[0].getItem(1).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		menus[0].getItem(4).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		menus[0].getItem(5).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		
		// tool bar
		JToolBar toolBar = new JToolBar(); 
		toolBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		String [] icons = {
				"images/user.png", 
				"images/adim.png", 
				"images/time.png", 
				"images/dollar.png", 
				"images/statistics.png", 
				"images/bookmark.png", 
				"images/member.png", 
				"images/book_add.png", 
				"images/user_red.png", 
				"images/settings.png", 
				"images/db-upload.png", 
				"images/db-download.png", 
		};
		String [] tips = {"reg_student", "reg_employee", "timetable_wizard", "financial_period_report", "students_costs_report",
				"borrow_reg", "external_member_reg", "book_register", "user_reg", "settings", "backup", "restore"};
		
		Class<?> windows [] = {
				StudentProfile.class,
				EmployeeProfile.class,
				TimetableWizard.class,
				FinanPeriodReport.class,
				CostsReport.class,
				BookBorrowReg.class,
				LibraryMemProfile.class,
				BookProfile.class,
				UserProfile.class,
				Settings.class,
				null,
				null,
		};
		
		JButton [] tools = new JButton[icons.length];
		for(int i=0; i<icons.length; i++)
		{
			tools[i] = new JButton(new ImageIcon(icons[i]));
			tools[i].setBorderPainted(false);
			tools[i].setToolTipText(Dic.w(tips[i]));
			toolBar.add(tools[i]);
			if(windows[i] != null){
				tools[i].addActionListener(new OpenFrame(windows[i], Main.this));
			}
		}
		
		// apply permissions
		if(Data.PERM_STUDENTS == 0){
			menus[0].getItem(1).setEnabled(false);
			menus[0].getItem(2).setEnabled(false);
			menus[0].getItem(3).setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
			menus[0].getItem(0).setEnabled(false);
			tools[0].setEnabled(false);
		}
		
		if(Data.PERM_EMPLOYEES == 0){
			menus[0].getItem(5).setEnabled(false);
		}
		if(Data.PERM_EMPLOYEES != 2){
			menus[0].getItem(4).setEnabled(false);
			tools[1].setEnabled(false);
		}
		
		if(Data.PERM_COURSES == 0){
			menus[1].setEnabled(false);
		}
		if(Data.PERM_COURSES != 2){
			menus[1].getItem(2).setEnabled(false);
			menus[1].getItem(3).setEnabled(false);
			menus[1].getItem(4).setEnabled(false);
			tools[2].setEnabled(false);
		}
		
		if(Data.PERM_FINANCE == 0){
			menus[2].setEnabled(false);
			menus[3].setEnabled(false);
			tools[3].setEnabled(false);
			tools[4].setEnabled(false);
		}
		
		if(Data.PERM_LIBRARY == 0){
			menus[4].setEnabled(false);
		}
		if(Data.PERM_LIBRARY != 2){
			menus[4].getItem(0).setEnabled(false);
			menus[4].getItem(2).setEnabled(false);
			menus[4].getItem(4).setEnabled(false);
			tools[5].setEnabled(false);
			tools[6].setEnabled(false);
			tools[7].setEnabled(false);
		}
		
		if(Data.PERM_USERS == 0){
			menus[5].setEnabled(false);
		}
		if(Data.PERM_USERS != 2){
			menus[5].getItem(0).setEnabled(false);
			tools[8].setEnabled(false);
		}
		
		if(Data.PERM_SETTINGS == 0){
			menus[6].setEnabled(false);
			tools[9].setEnabled(false);
			tools[10].setEnabled(false);
			tools[11].setEnabled(false);
		}
		//==========
		final JPanel fxpanel = new ChartsPanel();
		final JTabbedPane tabs = new JTabbedPane();
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 0), new LineBorder(Color.GRAY)));
		split.setTopComponent(fxpanel);
		split.setBottomComponent(tabs);
		split.setDividerLocation(350);
		
		JLabel ks = new JLabel(new ImageIcon("images/banner.png"));
		ks.setBorder(new CompoundBorder(new EmptyBorder(0, 2, 0, 2), new LineBorder(Color.GRAY)));
		final TopStudents topStus = new TopStudents();

		JPanel fast = new JPanel();
		JPanel sidebar = new JPanel(new BorderLayout(0, 3));
		sidebar.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 0, 5), sidebar.getBorder()));
		
		sidebar.add(ks, BorderLayout.NORTH);
		sidebar.add(topStus, BorderLayout.CENTER);
		sidebar.add(fast, BorderLayout.SOUTH);
		
		setLayout(new BorderLayout(5, 0));
		add(toolBar, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
		add(sidebar, BorderLayout.EAST);
		
		DailyTimetable schedule = null;
		CoursePanel courses = null;
		if(Data.PERM_COURSES != 0){
			schedule = new DailyTimetable();
			schedule.frame = this;
			courses = new CoursePanel();
		}else{
			tabs.setEnabled(false);
		}
		tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tabs.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), tabs.getBorder()));
		tabs.add(Dic.w("timetable"), schedule);
		tabs.add(Dic.w("courses"), courses);
		
		String [] text = {"student_code", "employee_code"};
		final JTextField [] fff = new JTextField [2];
		JButton [] bbb = new JButton [2];
		
		fast.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		fast.setBorder(new TitledBorder(Dic.w("fast_access")));
		fast.setLayout(new GridBagLayout());
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 2, 4, 2);
		
		for(int i=0; i<text.length; i++)
		{
			fff[i] = new JTextField();
			bbb[i] = new JButton(new ImageIcon("images/zoom.png"));
			bbb[i].setMargin(new Insets(2, 4, 2, 4));
			
			cons.gridy = i;
			fast.add(new JLabel(Dic.w(text[i])), cons);
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.weightx = 1;
			fast.add(fff[i], cons);
			cons.fill = GridBagConstraints.NONE;
			cons.weightx = 0;
			fast.add(bbb[i], cons);
		}
		if(Data.PERM_STUDENTS == 0){
			fff[0].setEditable(false);
			bbb[0].setEnabled(false);
		}else{
			ActionListener findst = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(db.findPerson("students", "st_id", fff[0].getText()))
					{
						Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, 
								new Object[]{Integer.parseInt(fff[0].getText())});
						Main.this.setCursor(Cursor.getDefaultCursor());
					}
					else{
						Diags.showErrLang("wrong_person_id_error");
					}
				}
			};
			fff[0].addActionListener(findst);
			bbb[0].addActionListener(findst);
		}
		if(Data.PERM_EMPLOYEES == 0){
			fff[1].setEditable(false);
			bbb[1].setEnabled(false);
		}else{
			ActionListener findemp = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(db.findPerson("employee", "emp_id", fff[1].getText()))
					{
						Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						OpenFrame.openFrame(EmployeeProfile.class, new Class<?>[]{int.class}, 
								new Object[]{Integer.parseInt(fff[1].getText())});
						Main.this.setCursor(Cursor.getDefaultCursor());
					}
					else{
						Diags.showErrLang("wrong_person_id_error");
					}
				}
			};
			fff[1].addActionListener(findemp);
			bbb[1].addActionListener(findemp);
		}
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		ULocale locale = new ULocale("@calendar=persian");
		DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", locale);
		java.text.DateFormat gdf = new java.text.SimpleDateFormat("yyyy/MM/dd");
		
		Date date = new Date();
		String shamsi = sdf.format(date);
		String greg = gdf.format(date);
		
		bright.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		String shamStr = String.format("<html>%s: <b>%s</d></html>", Dic.w("shamsi_date"), shamsi);
		String gregStr = String.format("<html>%s: <b>%s</d></html>", Dic.w("greg_date"), greg);
		String educYear = String.format("<html>%s: <b>%d</d></html>", Dic.w("educ_year"), Data.EDUC_YEAR);
		String finanPrd = String.format("<html>%s: <b>%s</d></html>", Dic.w("financial_period"), db.finanPeriodName());
		
		bright.add(new JLabel(shamStr));
		bright.add(new JLabel(" | "));
		bright.add(new JLabel(gregStr));
		bright.add(new JLabel(" | "));
		bright.add(new JLabel(educYear));
		bright.add(new JLabel(" | "));
		bright.add(new JLabel(finanPrd));
		
		JButton user = new JButton(Data.USER_FULLNAME, new ImageIcon("images/password.png"));
		user.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		user.setToolTipText(Dic.w("change_password"));
		user.setMargin(new Insets(2, 4, 2, 4));

		bleft.add(user);
		
		user.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangePassword();
			}
		});
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				if(Diags.showConfLang("exit_conf", Diags.YN) == 0){
					System.exit(0);
				}
			}
		});
		getInputContext().selectInputMethod(new Locale("FA", "IR"));

		setMinimumSize(new Dimension(950, 650));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setIconImage(new ImageIcon("images/icon.png").getImage());
		setVisible(true);
	}
	@Override
	protected void finalize() throws Throwable{
		if(db != null){
			db.closeConn();
		}
		super.finalize();
	}
}
























