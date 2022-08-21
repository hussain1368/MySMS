package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;
import com.kabulbits.shoqa.util.SpinnerEditor;

public class TimetableWizard extends JFrame{

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static TimetableWizard self;
	
	private JRadioButton [] radios;
	private ButtonGroup group;
	private JButton next, back, finish, cancel;
	private CardLayout card;
	private JPanel right;
	
	private StepOne stepOne;
	private StepTwo stepTwo;
	private StepThree stepThree;
	private StepFour stepFour;
	
	private CourseData data;
	
	private int shift;
	private Vector<?> courses;

	public TimetableWizard()
	{
		isOpen = true;
		self = this;
		data = new CourseData();
		
		String title = Dic.w("timetable_wizard");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, false);
		JPanel center = new JPanel(new BorderLayout());
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		add(ribbon, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		JPanel left = new JPanel(new GridBagLayout());
		card = new CardLayout();
		right = new JPanel(card);
		center.add(left, BorderLayout.WEST);
		center.add(right, BorderLayout.CENTER);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(5, 5, 0, 5);
		cons.gridx = 1;
		
		String texts [] = {"courses_selection", "teachers_assignment", "teachers_availablity", "build_timetable"};
		radios = new JRadioButton[texts.length];
		group = new ButtonGroup();
		
		for(int i=0; i<texts.length; i++)
		{
			radios[i] = new JRadioButton(Dic.w(texts[i]));
			radios[i].setEnabled(false);
			group.add(radios[i]);
			if(i == 3) cons.weighty = 1;
			left.add(radios[i], cons);
		}
		radios[0].setSelected(true);
		radios[0].setEnabled(true);
		left.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));
		
		stepOne = new StepOne();
		stepTwo = new StepTwo();
		stepThree = new StepThree();
		stepFour = new StepFour();
		
		right.add(stepOne, "0");
		right.add(stepTwo, "1");
		right.add(stepThree, "2");
		right.add(stepFour, "3");
		
		next = new JButton("Next");
		back = new JButton("Back");
		finish = new JButton("Finish");
		cancel = new JButton("Cancel");
		finish.setEnabled(false);
		
		bottom.add(back);
		bottom.add(next);
		bottom.add(finish);
		bottom.add(cancel);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.BLACK));
		
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DIRECTION = FORWARD;
				STEP++;
				switchPanel();
			}
		});
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DIRECTION = BACKWARD;
				STEP--;
				switchPanel();
			}
		});
		finish.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isOpen = false;
				dispose();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeConf();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeConf();
			}
		});
		
		setMinimumSize(new Dimension(650, 500));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		
		switchPanel();
	}
	
	private int STEP = 0;
	private int FORWARD = 1;
	private int BACKWARD = 2;
	private int DIRECTION = 1;
	
	private void switchPanel()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if(STEP == 1){
			this.shift = stepOne.SHIFT;
			if(DIRECTION == FORWARD){
				String msg = String.format(Dic.w("all_timetable_deletion_conf"), this.shift);
				if(Diags.showConf(msg, Diags.YN) != 0){
					this.STEP = 0;
					setCursor(Cursor.getDefaultCursor());
					return;
				}
				data.clearShiftSchedule(this.shift);
				courses = stepOne.getData();
			}
		}
		for(JRadioButton radio : radios){
			radio.setEnabled(false);
			radio.setSelected(false);
		}
		next.setEnabled(false);
		if(STEP == 0 || STEP == 3){
			back.setEnabled(false);
		}else{
			back.setEnabled(true);
		}
		radios[STEP].setEnabled(true);
		radios[STEP].setSelected(true);
		card.show(right, String.valueOf(STEP));

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switch(STEP){
				case 0:
					courses = null;
					stepOne.render();
					break;
				case 1:
					stepTwo.render();
					break;
				case 2:
					stepThree.render();
					break;
				case 3:
					stepFour.doBuild();
					break;
				}
			}
		});
		setCursor(Cursor.getDefaultCursor());
	}
	
	private class StepOne extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private JComboBox<Integer> shifts;
		private DefaultTableModel model;
		private JTable table;

		public int SHIFT = 1;
		
		public StepOne(){
			setLayout(new BorderLayout());
			
			JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			top.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			add(top, BorderLayout.NORTH);
			
			shifts = new JComboBox<>(new Integer[]{1,2,3,4,5});
			shifts.setPrototypeDisplayValue(10000);
			top.add(new JLabel(Dic.w("shift")));
			top.add(shifts);
			
			model = new DefaultTableModel(){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					return col == 0;
				}
				@Override
				public Class<?> getColumnClass(int index) {
					if(index == 0){
						return Boolean.class;
					}
					return Object.class;
				}
			};
			String cols [] = {"select", "code", "grade", "identifier"};
			for(String col : cols){
				model.addColumn(Dic.w(col));
			}
			table = new JTable(model);
			Helper.tableMakUp(table);
			table.getTableHeader().setReorderingAllowed(false);
			table.getColumnModel().getColumn(0).setMaxWidth(80);
			
			JScrollPane pane = new JScrollPane(table);
			pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 5, 5), pane.getBorder()));
			add(pane, BorderLayout.CENTER);
			
			table.getTableHeader().addMouseListener(new MouseAdapter() {
				private boolean checked = true;
			    public void mousePressed(MouseEvent e)
			    {
			        if(model.getRowCount() == 0) return;
		        	int col = table.columnAtPoint(e.getPoint());
			        if(col == 0) {
			        	for(int i=0; i<model.getRowCount(); i++){
			        		model.setValueAt(checked, i, col);
			        	}
			        	checked = !checked;
			        }	        	
			    }
			});
			shifts.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					render();
				}
			});
			model.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					checkRows();
				}
			});
		}
		
		public void render()
		{
			SHIFT = (int) shifts.getSelectedItem();
			Vector<Object[]> rows = data.coursesByShift(SHIFT);
			model.setRowCount(0);
			for(Object [] row : rows){
				model.addRow(row);
			}
		}
		public Vector<?> getData()
		{
			Vector<?> rows = model.getDataVector();
			Vector<Object> data = new Vector<>();
			for(int i=0; i<rows.size(); i++)
			{
				Vector<?> row = (Vector<?>) rows.get(i);
				if((boolean) row.get(0)){
					row.remove(0);
					data.add(row);
				}
			}
			model.setRowCount(0);
			return data;
		}
		public void checkRows()
		{
			boolean choosen = false;
			for(int i=0; i<model.getRowCount(); i++)
			{
				if((boolean)model.getValueAt(i, 0)){
					choosen = true;
					break;
				}
			}
			next.setEnabled(choosen);
		}
	}
	
	private class StepTwo extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private DefaultTableModel coursesModel, teachersModel;
		private JTable coursesTable, teachersTable;
		
		private int row = 0;
		private int id = 0;
		
		public StepTwo(){
			setLayout(new BorderLayout());
			
			JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			add(form, BorderLayout.NORTH);
			JButton select, save;
			select = new JButton(Dic.w("select"));
			save = new JButton(Dic.w("save"));
			form.add(save);
			form.add(select);
			
			coursesModel = new DefaultTableModel(){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {return false;}
				@Override
				public Class<?> getColumnClass(int index) {
					if(index == 3){
						return Boolean.class;
					}
					return Object.class;
				}
			};
			String cols [] = {"code", "grade", "identifier"};
			for(String col : cols){
				coursesModel.addColumn(Dic.w(col));
			}
			coursesModel.addColumn("");
			coursesTable = new JTable(coursesModel);
			Helper.tableMakUp(coursesTable);
			JScrollPane pane1 = new JScrollPane(coursesTable);
			pane1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			
			teachersModel = new DefaultTableModel();
			String cols2 [] = {"code", "subject_name", "hours", "teacher_name", "teacher_code"};
			for(String col : cols2){
				teachersModel.addColumn(Dic.w(col));
			}
			teachersTable = new JTable(teachersModel){
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {
					return col == 2 || col == 3;
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
			Helper.tableMakUp(teachersTable);
			teachersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			teachersTable.getColumnModel().getColumn(1).setPreferredWidth(100);
			teachersTable.getColumnModel().getColumn(3).setPreferredWidth(150);
			teachersTable.getColumnModel().getColumn(2).setCellEditor(new SpinnerEditor(1, 0, 10));
			
			final JComboBox<Option> teacherList = new JComboBox<>(data.teacherOptions());
			teacherList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel) teacherList.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			
			teachersTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(teacherList)
			{
				private static final long serialVersionUID = 1L;
				@Override
				public Object getCellEditorValue() 
				{
					Object val = super.getCellEditorValue();
					int row = teachersTable.getSelectedRow();
					if (val != null) {
						int id = ((Option) val).key;
						teachersModel.setValueAt(id, row, 4);
					} else {
						teachersModel.setValueAt(null, row, 4);
					}
					return val;
				}
			});
			
			JScrollPane pane2 = new JScrollPane(teachersTable);
			pane2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			pane2.setBorder(new MatteBorder(0, 1, 1, 1, Color.GRAY));
			
			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			split.setDividerLocation(150);
			split.setTopComponent(pane1);
			split.setBottomComponent(pane2);
			add(split, BorderLayout.CENTER);
			split.setBorder(new EmptyBorder(0, 5, 5, 5));
			
			select.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					renderTeachers();
				}
			});
			coursesTable.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						stop();
						renderTeachers();
					}
				}
			});
			coursesTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						stop();
						renderTeachers();
					}
				}
			});
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					saveTeachers();
				}
			});
		}
		
		public void render()
		{
			coursesModel.setRowCount(0);
			teachersModel.setRowCount(0);
			if(courses == null) return;
			for(int i=0; i<courses.size(); i++)
			{
				Vector<?> row = (Vector<?>) courses.get(i);
				coursesModel.addRow(row);
				coursesModel.setValueAt(false, i, 3);
			}
		}
		private void renderTeachers()
		{
			if(coursesTable.getSelectedRowCount() != 1) return;
			int r = coursesTable.getSelectedRow();
			int id = (int) coursesModel.getValueAt(r, 0);
			int grade = (int) coursesModel.getValueAt(r, 1);
			
			this.row = r;
			this.id = id;
			Vector<Object []> rows = data.courseTeachers(id, grade);
			teachersModel.setRowCount(0);
			
			int count = 0;
			for (Object row [] : rows){
				if(row[2] != null && row[4] != null){
					count++;
				}
				teachersModel.addRow(row);
			}
			if(rows.size() == count){
				coursesModel.setValueAt(true, this.row, 3);
			}
			checkRows();
		}
		protected void saveTeachers()
		{
			int rows = teachersModel.getRowCount();
			int count = 0;
			Object values [][] = new Object[rows][3];
			for(int i=0; i<rows; i++)
			{
				values[i][0] = teachersModel.getValueAt(i, 0);
				values[i][1] = teachersModel.getValueAt(i, 2);
				values[i][2] = teachersModel.getValueAt(i, 4);
				
				if(values[i][1] == null || values[i][2] == null){
					Diags.showErrLang("you_must_fill_out_all_the_options");
					return;
				}
			}
			for(Object vals[] : values) {
				int sub = (int) vals[0];
				Object tid = vals[2];
				Object hours = vals[1];
				if(data.assignTeacher(sub, tid, hours, this.id)) count++;
			}
			Diags.showMsg(String.format(Dic.w("items_saved"), count));
			coursesModel.setValueAt(true, this.row, 3);
			checkRows();
		}
		private void checkRows()
		{
			boolean complete = true;
			for(int i=0; i<coursesTable.getRowCount(); i++){
				if(!(boolean)coursesModel.getValueAt(i, 3)){
					complete = false;
					break;
				}
			}
			next.setEnabled(complete);
		}
		protected void stop() 
		{
			if(teachersTable.isEditing()){
				TableCellEditor editor = teachersTable.getCellEditor();
				if(editor != null){
					if(editor.stopCellEditing()){
						editor.cancelCellEditing();
					}
				}
			}
		}
	}

	private class StepThree extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private DefaultTableModel personsModel, hoursModel;
		private JTable personsTable, hoursTable;
		
		public StepThree(){
			setLayout(new BorderLayout());
			
			JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			add(form, BorderLayout.NORTH);
			JButton display, save;
			display = new JButton(Dic.w("display"));
			save = new JButton(Dic.w("save"));
			form.add(save);
			form.add(display);
			
			personsModel = new DefaultTableModel(){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col){return false;}
				@Override
				public Class<?> getColumnClass(int index) {
					if(index == 4){
						return Boolean.class;
					}
					return Object.class;
				}
			};
			String cols [] = {"code", "name", "lname", "teach_hours"};
			for(String col : cols){
				personsModel.addColumn(Dic.w(col));
			}
			personsModel.addColumn("");
			personsTable = new JTable(personsModel);
			Helper.tableMakUp(personsTable);
			personsTable.getTableHeader().setReorderingAllowed(false);
			
			JScrollPane pane1 = new JScrollPane(personsTable);
			
			hoursModel = new DefaultTableModel(){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					return col > 0;
				}
				@Override
				public Class<?> getColumnClass(int index) {
					if(index > 0){
						return Boolean.class;
					}
					return String.class;
				}
			};
			hoursModel.addColumn("");
			for(int i=1; i<7; i++){
				hoursModel.addColumn(i);
			}
			String week [] = {"saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
			for(String day : week){
				Object row [] = {Dic.w(day), false, false, false, false, false, false};
				hoursModel.addRow(row);
			}
			hoursTable = new JTable(hoursModel);
			Helper.tableMakUp(hoursTable);
			hoursTable.getTableHeader().setReorderingAllowed(false);
			((JLabel) hoursTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
			
			hoursTable.getTableHeader().addMouseListener(new MouseAdapter() {
				private boolean checked [];
				{
					checked = new boolean [] { true, true, true, true, true, true, true};
				}
			    public void mousePressed(MouseEvent e)
			    {
			        if(hoursModel.getRowCount() == 0) return;
		        	int col = hoursTable.columnAtPoint(e.getPoint());
			        if(col != 0) {
			        	for(int i=0; i<hoursModel.getRowCount(); i++){
			        		hoursModel.setValueAt(checked[col], i, col);
			        	}
			        	checked[col] = !checked[col];
			        }	        	
			    }
			});
			JScrollPane pane2 = new JScrollPane(hoursTable);
			pane2.setBorder(new MatteBorder(0, 1, 1, 1, Color.GRAY));
			
			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			split.setTopComponent(pane1);
			split.setBottomComponent(pane2);
			split.setDividerLocation(170);
			split.setBorder(new EmptyBorder(0, 5, 5, 5));
			
			add(split, BorderLayout.CENTER);
			
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveHours();
				}
			});
			display.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showHours();
				}
			});
			personsTable.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						showHours();
					}
				}
			});
			personsTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						showHours();
					}
				}
			});
			teachersHours = new HashMap<>();
		}
		
		public void render()
		{
			Vector<Object[]> rows = data.teachingHours(shift);
			personsModel.setRowCount(0);
			for(Object[] row : rows){
				personsModel.addRow(row);
			}
			//fill false
			for(int i=0; i<6; i++){
				for(int j=1; j<7; j++){
					hoursModel.setValueAt(false, i, j);
				}
			}
		}
		public void saveHours()
		{
			if(personsTable.getSelectedRowCount() != 1){
				Diags.showErrLang("please_choose_only_one_person");
				return;
			}
			int row = personsTable.getSelectedRow();
			int id = (int) personsModel.getValueAt(row, 0);
			int assigned = (int) personsModel.getValueAt(row, 3);
			
			teachersHours.remove(id);
			boolean times [][] = new boolean[6][6];
			int count = 0;
			for(int h=0; h<6; h++){
				for(int d=0; d<6; d++){
					boolean val = (boolean) hoursModel.getValueAt(d, h+1);
					if(val) count++;
					times[h][d] = val;
				}
			}
			if(count < assigned){
				Diags.showErrLang("number_of_times_is_less_than_assigned");
				times = null;
				personsModel.setValueAt(false, row, 4);
			}
			else{
				teachersHours.put(id, times);
				Diags.showMsg(Diags.SUCCESS);
				personsModel.setValueAt(true, row, 4);
			}
			checkRows();
		}
		public void showHours()
		{
			if(personsTable.getSelectedRowCount() != 1) return;
			int row = personsTable.getSelectedRow();
			int id = (int) personsModel.getValueAt(row, 0);
			
			boolean times [][] = teachersHours.get(id);
			boolean isNull = (times == null);
			for(int h=0; h<6; h++){
				for(int d=0; d<6; d++){
					hoursModel.setValueAt(isNull ? false : times[h][d], d, h+1);
				}
			}
			checkRows();
		}
		public void checkRows()
		{
			boolean complete = true;
			for(int i=0; i<personsModel.getRowCount(); i++)
			{
				if(!(boolean)personsModel.getValueAt(i, 4)){
					complete = false;
					break;
				}
			}
			next.setEnabled(complete);
		}
	}
	//boolean[hour][day]
	private Map<Integer, boolean[][]> teachersHours;
	
	public class StepFour extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private JLabel status;
		private JProgressBar progBar;
		private DefaultListModel<String> model;
		private JList<String> list;
		
		public StepFour(){
			setLayout(new BorderLayout());
			
			JPanel bar = new JPanel(new GridBagLayout());
			add(bar, BorderLayout.NORTH);
			bar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			
			status = new JLabel();
			progBar = new JProgressBar();
			progBar.setMinimum(0);
			progBar.setValue(0);
			
			GridBagConstraints cons = new GridBagConstraints();
			cons.anchor = GridBagConstraints.BASELINE_LEADING;
			cons.insets = new Insets(5, 5, 0, 5);
			cons.gridx = 0;
			cons.gridy = 0;
			
			bar.add(status, cons);
			cons.gridy = 1;
			cons.weighty = 1;
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			bar.add(progBar, cons);
			
			model = new DefaultListModel<String>();
			list = new JList<String>(model);

			list.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			JScrollPane pane = new JScrollPane(list);
			pane.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), pane.getBorder()));
			add(pane, BorderLayout.CENTER);
			
			emptyHours = new boolean[6][6];
		}
		private boolean emptyHours [][];
		
		private void fillTrue(){
			for(int i=0; i<6; i++){
				for(int j=0; j<6; j++){
					emptyHours[i][j] = true;
				}
			}
		}
		
		private String label = Dic.w("saved_times_num");
		private String text = Dic.w("course_completed");
		private int totalCount = 0;
		private int progVal = 0;
		private int maxVal = 0;
		
		public SwingWorker<Boolean, Integer> progress;
		public boolean isPaused = false;
		
		public void doBuild()
		{
			maxVal = courses.size() * 40;
			progBar.setMaximum(maxVal);
			
			progress = new SwingWorker<Boolean, Integer>() {
				@Override
				protected Boolean doInBackground() throws Exception 
				{
					for(int i=0; i<courses.size(); i++)
					{
						progVal = i*40;
						publish(progVal);

						fillTrue();
						Vector<?> course = (Vector<?>) courses.get(i);
						int id = (int) course.get(0);
						Vector<Map<String, Integer>> subjects = data.courseSubjHours(id);
						for(Map<String, Integer> subject : subjects)
						{
							int sub = subject.get("subject");
							int emp = subject.get("teacher");
							int subjHours = subject.get("hours");
							boolean teacherFree [][] = teachersHours.get(emp);
							boolean dayFree [] = {true, true, true, true, true, true};
							int chance = 3;
							A: while(subjHours > 0 && chance > 0)
							{
								if(!isPaused){
									chance--;
									for(int h=0; h<6; h++){
										for(int d=0; d<6; d++){
											if(emptyHours[h][d] && teacherFree[h][d] && dayFree[d]){
												if(data.saveTimetable(id, d+1, h+1, sub))
												{
													totalCount++;
													progVal++;
													publish(progVal);
													
													emptyHours[h][d] = false;
													teacherFree[h][d] = false;
													dayFree[d] = false;
													teachersHours.get(emp)[h][d] = false;
													subjHours--;
													if(subjHours == 0) break A;
												}
											}
										}
									}//week loop
								}
							}//chance loop
						}//subject loop
						model.addElement(String.format(text, course.get(1).toString(), course.get(2).toString()));
//						Thread.sleep(3000);
					}//course loop
					return true;
				}
				protected void process(java.util.List<Integer> chunks) 
				{
					progBar.setValue(progVal);
					status.setText(String.format(label, totalCount));
				};
				protected void done() 
				{
					progBar.setValue(maxVal);
					finish.setEnabled(true);
					cancel.setEnabled(false);
					done = true;
				};
			};
			progress.execute();
		}
	}
	
	private boolean done = false;
	private void closeConf(){
		if(done){
			isOpen = false;
			dispose();
		}else{
			stepFour.isPaused = true;
			if(Diags.showConfLang("are_you_sure_to_stop_process", Diags.YN) == 0){
				isOpen = false;
				dispose();
				if(stepFour.progress != null){
					stepFour.progress.cancel(true);
				}
			}else{
				stepFour.isPaused = false;
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






























