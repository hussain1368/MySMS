package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.sheet.SchedSheet;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.OptionChooser;

public class DailyTimetable extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	public Main frame;
	private DefaultTableModel model;
	private JTable table;
	private JComboBox<String> days;
	private JComboBox<Integer> shifts;
	private JComboBox<Object> grades;
	private CourseData data;

	public DailyTimetable()
	{
		data = new CourseData();
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(100, 100));
		JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(top, BorderLayout.NORTH);
		top.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		String week [] = {"saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
		days = new JComboBox<>();
		for(String day : week){
			days.addItem(Dic.w(day));
		}
		days.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) days.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		int d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		if(d > 5) d = 0;
		days.setSelectedIndex(d);
		
		shifts = new JComboBox<>(new Integer [] {1, 2, 3, 4, 5});
		grades = new JComboBox<>();
		grades.addItem("---");
		for(int i=1; i<13; i++){
			grades.addItem(i);
		}
		
		days.setPrototypeDisplayValue("xxxxxxxx");
		shifts.setPrototypeDisplayValue(10000);
		grades.setPrototypeDisplayValue(10000);
		
		top.add(new JLabel(Dic.w("day")));
		top.add(days);
		top.add(new JLabel(Dic.w("grade")));
		top.add(grades);
		top.add(new JLabel(Dic.w("shift")));
		top.add(shifts);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		String times [] = {"courses", "first", "second", "third", "fourth", "fifth", "sixth"};
		for(String time : times){
			model.addColumn(Dic.w(time));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(80);
		for(int i=1; i<table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(160);
		}
		
		JScrollPane pane = new JScrollPane(table);
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 4, 0, 4), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		JButton open = new JButton(Dic.w("course_timetable"));
		
		JButton refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setPreferredSize(new Dimension(26, 26));
		
		JButton print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		bright.add(refresh);
		bleft.add(print);
		bleft.add(open);
		
		days.addActionListener(this);
		grades.addActionListener(this);
		shifts.addActionListener(this);
		refresh.addActionListener(this);
		
		render();
		
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				schedule();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					schedule();
					e.consume();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					schedule();
				}
			}
		});
		print.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				print();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		render();
	}
	
	private void render()
	{
		int day = days.getSelectedIndex() + 1;
		int gr = grades.getSelectedIndex();
		int sh = (int) shifts.getSelectedItem();
		
		Vector<Object []> rows = data.dayTimetable(day, gr, sh);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
	}
	
	private void schedule()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Course course = (Course) model.getValueAt(row, 0);
		OpenFrame.openFrame(CourseTimetable.class, new Class<?>[]{int.class}, new Object[]{course.id}, frame);
	}
	
	private void print()
	{
		String[] opts = {"office_format", "course_format"};
		final int task = new OptionChooser("print_options", opts).signal;
		if(task != -1){
			final String path = Helper.xlsx("report", frame);
			if(path == null) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					int shift = (int) shifts.getSelectedItem();
					SchedSheet sheet = new SchedSheet(shift);
					if(task == 0){
						sheet.officeFormat();
					}else{
						sheet.courseFormat();
					}
					sheet.build(path);
					frame.setCursor(Cursor.getDefaultCursor());
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
