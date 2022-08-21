package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;
import com.kabulbits.shoqa.util.SpinnerEditor;

public class CourseTimetable extends JFrame 
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CourseTimetable self;
	
	private JButton saveTeachers, refreshTeachers, saveSchedule, refreshSchedule;
	private DefaultTableModel teachersModel, timeModel;
	private JTable teachersTable, timeTable;

	private CourseData data;
	private Course course;
	
	public CourseTimetable(int id) 
	{
		isOpen = true;
		self = this;
		
		data = new CourseData();
		course = data.findCourse(id);
		if(course == null){
			dispose();
			isOpen = false;
			return;
		}

		String title = Dic.w("course_timetable_and_teachers");
		setTitle(title);

		JPanel ribbon = new Ribbon(title, false);
		JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());

		top.add(ribbon, BorderLayout.NORTH);
		top.add(infoBar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);

		infoBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		JTextField tip1, tip2, tip3;

		tip1 = new JTextField(String.valueOf(course.grade), 4);
		tip2 = new JTextField(String.valueOf(course.shift), 4);
		tip3 = new JTextField(String.valueOf(course.name), 8);

		tip1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tip2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tip3.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		tip1.setEditable(false);
		tip2.setEditable(false);
		tip3.setEditable(false);

		infoBar.add(new JLabel(Dic.w("grade")));
		infoBar.add(tip1);
		infoBar.add(new JLabel(Dic.w("shift")));
		infoBar.add(tip2);
		infoBar.add(new JLabel(Dic.w("identifier")));
		infoBar.add(tip3);

		teachersModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col == 2 || col == 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 2){
					return Integer.class;
				}else if(index == 3){
					return Option.class;
				}
				return Object.class;
			}
		};
		String headers [] = {"subject_code", "subject_name", "teach_hours", "teacher_name", "teacher_code"};
		for(String item : headers){
			teachersModel.addColumn(Dic.w(item));
		}
		teachersTable = new JTable(teachersModel);
		Helper.tableMakUp(teachersTable);

		teachersTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		teachersTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		teachersTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		teachersTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		teachersTable.getColumnModel().getColumn(4).setPreferredWidth(40);
		
		if(Data.PERM_COURSES == 2)
		{
			teachersTable.getColumnModel().getColumn(2).setCellEditor(new SpinnerEditor(1, 0, 10));
			final JComboBox<Option> teacherList = new JComboBox<>(data.teacherOptions());
			teacherList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel) teacherList.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			teachersTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(teacherList) 
			{
				private static final long serialVersionUID = 1L;
				private Object value;
				@Override
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean isSelected, int row, int column) {
					this.value = value;
					return super.getTableCellEditorComponent(table, value, isSelected, row, column);
				}
				@Override
				public Object getCellEditorValue() 
				{
					Object value = super.getCellEditorValue();
					int row = teachersTable.getSelectedRow();
					if (value != null)
					{
						int id = ((Option) value).key;
						int sub = (int) teachersModel.getValueAt(row, 0);
						if(data.isTeacherFreeR(course.id, sub, course.shift, id)){
							teachersModel.setValueAt(id, row, 4);
							return value;
						}
						else{
							return this.value;
						}
					}
					teachersModel.setValueAt(null, row, 4);
					return null;
				}
			});
		}
		timeModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col != 0;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 0){
					return String.class;
				}
				return Option.class;
			}
		};
		String headers1 [] = {"week_days", "first", "second", "third", "fourth", "fifth", "sixth"};
		for(String item : headers1){
			timeModel.addColumn(Dic.w(item));
		}
		timeTable = new JTable(timeModel);
		Helper.tableMakUp(timeTable);
		timeTable.getColumnModel().getColumn(0).setMinWidth(100);
		
		if(Data.PERM_COURSES == 2){
			JComboBox<Option> subjList = new JComboBox<>(data.subjectList(course.grade, true));
			subjList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel) subjList.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

			for (int i = 1; i < timeTable.getColumnCount(); i++) 
			{
				timeTable.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(subjList) 
				{
					private static final long serialVersionUID = 1L;
					private Object value;
					@Override
					public Component getTableCellEditorComponent(JTable table,
							Object value, boolean isSelected, int row, int column) {
						this.value = value;
						return super.getTableCellEditorComponent(table, value, isSelected, row, column);
					}
					@Override
					public Object getCellEditorValue() 
					{
						int day = timeTable.getSelectedRow() + 1;
						int time = timeTable.getSelectedColumn();

						Object value = super.getCellEditorValue();
						if (value != null){
							int sub = ((Option)value).key;
							if(data.isTeacherFree(course.id, day, time, sub, course.shift)){
								return value;
							}else{
								return this.value;
							}
						}
						return null;
					}
				});
			}
		}

		JPanel buttons1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		saveTeachers = new JButton(Dic.w("save"));
		refreshTeachers = new JButton(new ImageIcon("images/refresh.png"));
		refreshTeachers.setToolTipText(Dic.w("refresh"));
		refreshTeachers.setMargin(new Insets(2, 3, 2, 3));
		buttons1.add(saveTeachers);
		buttons1.add(refreshTeachers);

		JScrollPane pane1 = new JScrollPane(teachersTable);
		JPanel teachersPanel = new JPanel(new BorderLayout());
		teachersPanel.setPreferredSize(new Dimension(500, 250));
		teachersPanel.add(pane1, BorderLayout.CENTER);
		teachersPanel.add(buttons1, BorderLayout.SOUTH);

		JPanel buttons2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		saveSchedule = new JButton(Dic.w("save"));
		refreshSchedule = new JButton(new ImageIcon("images/refresh.png"));
		refreshSchedule.setToolTipText(Dic.w("refresh"));
		refreshSchedule.setMargin(new Insets(2, 3, 2, 3));
		buttons2.add(saveSchedule);
		buttons2.add(refreshSchedule);

		JScrollPane pane2 = new JScrollPane(timeTable);
		JPanel timesPanel = new JPanel(new BorderLayout());
		timesPanel.setPreferredSize(new Dimension(500, 250));
		timesPanel.add(pane2, BorderLayout.CENTER);
		timesPanel.add(buttons2, BorderLayout.SOUTH);
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setBorder(new EmptyBorder(0, 4, 0, 4));
		split.setDividerLocation(250);
		split.setTopComponent(teachersPanel);
		split.setBottomComponent(timesPanel);
		add(split, BorderLayout.CENTER);
		
		renderTeachers();
		renderSchedule();
		
		if(Data.PERM_COURSES == 2){
			refreshTeachers.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					renderTeachers();
				}
			});
			saveTeachers.addActionListener(new BgWorker<Integer>(this) {
				@Override
				protected Integer save(){
					stop();
					return saveTeachers();
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						renderTeachers();
					}
				}
			});
			refreshSchedule.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					renderSchedule();
				}
			});
			saveSchedule.addActionListener(new BgWorker<Integer>(this) {
				@Override
				protected Integer save() {
					stop();
					return saveSchedule();
				}
				@Override
				protected void finish(Integer result) {
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						renderSchedule();
					}
				}
			});
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();

		setMinimumSize(new Dimension(650, 600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_COURSES != 2){
			refreshTeachers.setEnabled(false);
			saveTeachers.setEnabled(false);
			refreshSchedule.setEnabled(false);
			saveSchedule.setEnabled(false);
		}
	}
	
	private int saveSchedule() 
	{
		int rows = timeModel.getRowCount();
		int cols = timeModel.getColumnCount();
		int count = 0;
		for(int i=0; i<rows; i++)
		{
			for(int j=1; j<cols; j++) {
				Object value = timeModel.getValueAt(i, j);
				if(value != null){
					int sub = ((Option)value).key;
					if(data.saveTimetable(course.id, i+1, j, sub)) count++;
				}else{
					if(data.deleteTimetable(course.id, i+1, j)) count++;
				}
				
			}
		}
		return count;
	}

	private void renderSchedule() 
	{
		Object rows [][] = data.courseTimetable(course.id);
		timeModel.setRowCount(0);
		for(Object row [] : rows){
			timeModel.addRow(row);
		}
	}

	private int saveTeachers()
	{
		int rows = teachersModel.getRowCount();
		int count = 0;
		for(int i=0; i<rows; i++)
		{
			int sub = Integer.parseInt(teachersModel.getValueAt(i, 0).toString());
			Object empid = teachersModel.getValueAt(i, 4);
			Object hours = teachersModel.getValueAt(i, 2);
			
			if(data.assignTeacher(sub, empid, hours, course.id)) count++;
		}
		return count;
	}

	private void renderTeachers()
	{
		Vector<Object []> rows = data.courseTeachers(course.id, course.grade);
		teachersModel.setRowCount(0);
		for (Object row [] : rows){
			teachersModel.addRow(row);
		}
	}
	
	protected void stop() 
	{
		if(teachersTable.isEditing()){
			TableCellEditor editor = teachersTable.getCellEditor();
			if(editor != null){
				if(!editor.stopCellEditing()){
					editor.cancelCellEditing();
				}
			}
		}
		if(timeTable.isEditing()){
			TableCellEditor editor = timeTable.getCellEditor();
			if(editor != null){
				if(!editor.stopCellEditing()){
					editor.cancelCellEditing();
				}
			}
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





























