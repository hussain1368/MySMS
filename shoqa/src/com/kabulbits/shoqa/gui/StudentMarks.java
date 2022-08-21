package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.sheet.Paper;
import com.kabulbits.shoqa.sheet.StudSheet;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OptionChooser;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentMarks extends JPanel 
{
	private static final long serialVersionUID = 1L;
	private boolean isModified = false;
	public StudentProfile frame;
	public Student student;
	
	private JComboBox<Integer> grade, year;
	private JComboBox<String> term, markType;
	private JCheckBox allSubjects;
	private JTextField total, average, degree, result;
	private JButton refresh, delete, save, upgrade, print;
	private JTable table;
	private DefaultTableModel model, modelHalf, modelFull, modelSpring, modelFall;
	
	private MarkData markData;
	private int stGrade;
	private int sid;
	private boolean doRender = true;
	
	public StudentMarks(int id, int gr) {
		
		sid = id;
		stGrade = gr;
		
		markData = new MarkData();
		
		setLayout(new BorderLayout());
		JPanel ribbon = new Ribbon(Dic.w("student_marks"), false);
		JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel avgBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 5));
		JPanel center = new JPanel(new BorderLayout());
		JPanel bottom = new JPanel(new BorderLayout());
		
		center.add(topBar, BorderLayout.NORTH);
		center.add(avgBar, BorderLayout.SOUTH);
		add(ribbon, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
				
		grade = new JComboBox<>(new Integer [] {12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1});
		grade.setMaximumRowCount(12);
		
		year = new JComboBox<>();
		for(int y = Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		term = new JComboBox<String>(new String [] {
				Dic.w("total_mark"),
				Dic.w("half_mark"),
				Dic.w("final_mark"),
		});
		term.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)term.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		grade.setPrototypeDisplayValue(10000);
		year.setPrototypeDisplayValue(10000);
		
		grade.setSelectedItem(stGrade);
		year.setSelectedItem(markData.markYear(stGrade, sid, stGrade));
		
		markType = new JComboBox<String>(new String [] {
				Dic.w("normal_mark"),
				Dic.w("leveling_exam"),
				Dic.w("ability_exam"),
		});
		markType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) markType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		allSubjects = new JCheckBox(Dic.w("all_subjects"));
		allSubjects.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		topBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		topBar.add(new JLabel(Dic.w("grade")));
		topBar.add(grade);
		topBar.add(new JLabel(Dic.w("year")));
		topBar.add(year);
		topBar.add(new JLabel(Dic.w("marks_section")));
		topBar.add(term);
		topBar.add(new JLabel(Dic.w("mark_type")));
		topBar.add(markType);
		topBar.add(allSubjects);

		modelHalf = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col > 1;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index > 1){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols1 [] = {"code", "subject_name", "written", "oral", "practical", "activity", "homework", "total"};
		for(String col : cols1){
			modelHalf.addColumn(Dic.w(col));
		}
		modelFull = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
				if(Data.PERM_COURSES == 2){
					if(markType.getSelectedIndex() == 0){
						return col > 1;
					}
					return col == 4;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index){
				if(index > 5){
					return Boolean.class;
				}
				if(index > 1){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols2 [] = {"code", "subject_name", "half_mark", "final_mark", "total", "second_mark", "midterm_excused", "final_excused"};
		for(String col : cols2){
			modelFull.addColumn(Dic.w(col));
		}
		modelSpring = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
				if(Data.PERM_COURSES == 2){
					return col > 1;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index){
				if(index > 1){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols3 [] = {"code", "subject_name", "hamal", "sawr", "jawza", "saratan", "total"};
		for(String col : cols3){
			modelSpring.addColumn(Dic.w(col));
		}
		modelFall = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
				if(Data.PERM_COURSES == 2){
					return col > 1;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index){
				if(index > 1){
					return Float.class;
				}
				return Object.class;
			}
		};
		modelFall.addColumn(Dic.w("code"));
		modelFall.addColumn(Dic.w("subject_name"));
		modelFall.addColumn(Dic.w("asad"));
		modelFall.addColumn(Dic.w("sonbola"));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("mizan"), 1));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("mizan"), 2));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("aqrab"), 1));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("aqrab"), 2));
		modelFall.addColumn(Dic.w("total"));
		
		term.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchModel();
				render();
			}
		});
		TableModelListener doSum = new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
				sum();
			}
		};
		modelHalf.addTableModelListener(doSum);
		modelFull.addTableModelListener(doSum);
		modelSpring.addTableModelListener(doSum);
		modelFall.addTableModelListener(doSum);
		model = modelFull;

		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);
		table.getColumnModel().getColumn(7).setPreferredWidth(100);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		center.add(pane, BorderLayout.CENTER);
		center.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		total = new JTextField(5);
		average = new JTextField(5);
		result = new JTextField(10);
		degree = new JTextField(5);
		
		avgBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		avgBar.add(new JLabel(Dic.w("total")));
		avgBar.add(total);
		avgBar.add(new JLabel(Dic.w("average")));
		avgBar.add(average);
		avgBar.add(new JLabel(Dic.w("degree")));
		avgBar.add(degree);
		avgBar.add(new JLabel(Dic.w("result")));
		avgBar.add(result);
		
		JTextField fields [] = {total, average, degree, result};
		for(JTextField field : fields){
			field.setHorizontalAlignment(JTextField.RIGHT);
			field.setEditable(false);
			field.setBackground(Color.WHITE);
		}
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		delete = new JButton(Dic.w("delete_marks"));
		save = new JButton(Dic.w("save"));
		upgrade = new JButton(Dic.w("reg_graduation"));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(delete);
		bright.add(save);
		bright.add(refresh);
		bleft.add(print);
		
		if(stGrade == 12) {
			bleft.add(upgrade);
			upgrade.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					if(markData.graduateStudent(sid)){
						Diags.showMsg(Diags.SUCCESS);
					}
				}
			});
		}
		// Listeners
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		grade.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchModel();
				if(grade.getItemCount() == 0) return;
				doRender = false;
				int g = (int) grade.getSelectedItem();
				int y = markData.markYear(g, sid, stGrade);
				year.setSelectedItem(y);
				doRender = true;
				stop();
				render();
			}
		});
		year.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(doRender) {
					stop();
					render();
				}
			}
		});
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		markType.addActionListener(render);
		allSubjects.addActionListener(render);
		
		if(Data.PERM_COURSES == 2){
			save.addActionListener(new BgWorker<Integer>(this) {
				@Override
				protected Integer save() {
					stop();
					if(isModified){
						return doSave();
					}
					return 0;
				}
				@Override
				protected void finish(Integer result) {
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						render();
					}
				}
			});
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					delete();
				}
			});
			table.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						stop();
						delete();
					}
				}
			});
		}
		print.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				print();
			}
		});
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_COURSES != 2){
			refresh.setEnabled(false);
			save.setEnabled(false);
			upgrade.setEnabled(false);
		}
	}
	private int season = 0;
	
	protected void switchModel() 
	{
		season = term.getSelectedIndex();
		int g = (int) grade.getSelectedItem();
		if(season == 0){
			markType.setEnabled(true);
		}else{
			markType.setSelectedIndex(0);
			markType.setEnabled(false);
		}
		if(season == 0){
			model = modelFull;
		}
		else if(g > 1){
			model = modelHalf;
		}
		else if(season == 1){
			model = modelSpring;
		}
		else if(season == 2){
			model = modelFall;
		}
		table.setModel(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);
//		table.getColumnModel().getColumn(7).setPreferredWidth(100);
	}
	private boolean doSum = true;

	protected void sum() 
	{
		int row = table.getSelectedRow();
		if(row < 0) return;
		
		if(!doSum) return;
		doSum = false;
		
		int g = (int) grade.getSelectedItem();
		int last = (season == 0)? 4 : (g > 1)? 7 : (season == 1)? 6 : 8;
		
		float total = 0;
		boolean isNull = true;
		
		for(int i=2; i<last; i++){
			Object value = model.getValueAt(row, i);
			if(value != null){
				float mark = Float.parseFloat(value.toString());
				total += mark;
				isNull = false;
			}
		}
		if(!isNull){
			model.setValueAt(total, row, last);
		}
		doSum = true;
	}
	public void render()
	{
		int g = (int) grade.getSelectedItem();
		int y = (int) year.getSelectedItem();
		int type = markType.getSelectedIndex() + 1;
		
		model.setRowCount(0);
		
		float total = 0;
		float count = 0;
		boolean complete = true;
		int fails = 0;
		
		Vector<Object []> rows;
		
		if(season != 0){
			rows = markData.seasonMarks(sid, y, g, season, allSubjects.isSelected());
		}else{
			rows = markData.fullMarks(sid, y, g, type, allSubjects.isSelected());
		}
		
		int passMark = season == 1 ? Data.PASS_GRADE_MID : Data.PASS_GRADE_FINAL;
		for(Object row [] : rows)
		{
			model.addRow(row);
			// do not calculate for final term
			if(season != 2){
				int index = season != 0 ? row.length-1 : 4;
				if(row[index] == null){
					complete = false;
					continue;
				}
				float mark = Float.parseFloat(row[index].toString());
				if(mark < passMark) fails++;
				total += mark;
				count++;
			}
		}
		this.total.setText("---");
		this.average.setText("---");
		this.degree.setText("---");
		this.result.setText("---");
		
		if(season != 2){
			float avg = count == 0 ? 0 : total / count;
			this.total.setText(Float.toString(total));
			this.average.setText(String.format("%.2f", avg));
			if(complete) {
				String status = fails == 0 ? "passed" : fails < Data.FAIL_SUBJ_COUNT ? "eventual" : "failed";
				this.result.setText(Dic.w(status));
				if(type == 1 && fails == 0){
					int pos = markData.studentPosition(sid, y, g, season, avg);
					this.degree.setText(String.valueOf(pos));
				}
			}
		}
		isModified = false;
	}
	private int doSave()
	{
		int g = (int) grade.getSelectedItem();
		int y = (int) year.getSelectedItem();
		int type = markType.getSelectedIndex() + 1;
		int count = season != 0 ? saveSeason(g, y) : saveFull(g, y, type);
		return count;
	}
	private int saveSeason(int grade, int year)
	{
		int cols = model.getColumnCount();
		int rows = model.getRowCount();
		int last = cols - 1;
		
		int count = 0;
		for(int r=0; r<rows; r++)
		{
			if(model.getValueAt(r, last) == null) continue;
			int subid = Integer.parseInt(model.getValueAt(r, 0).toString());
			Object marks [] = new Object[7];
			int i = 0;
			for(int c=2; c<last; c++) {
				marks[i++] = model.getValueAt(r, c);
			}
			marks[6] = model.getValueAt(r, last);
			if(markData.saveSeasonMark(sid, marks, grade, year, season, subid)) count++;
		}
		return count;
	}
	private int saveFull(int grade, int year, int type)
	{
		int rows = model.getRowCount();
		int count = 0;
		for(int r=0; r<rows; r++)
		{
			Object marks [] = new Object[4];
			boolean next = false;
			int c = 2;
			for(int i=0; i<4; i++){
				marks[i] = model.getValueAt(r, c++);
				if(marks[i] != null) next = true;
			}
			if(!next) continue;
			
			boolean midtermExcused = (boolean) model.getValueAt(r, 6);
			boolean finalExcused = (boolean) model.getValueAt(r, 7);
			int subid = Integer.parseInt(model.getValueAt(r, 0).toString());
			
			if(markData.saveFullMark(sid, marks, midtermExcused, finalExcused, type, grade, year, subid)) count++;
		}
		return count;
	}
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() == 0) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int y = (int) year.getSelectedItem();
		int g = (int) grade.getSelectedItem();
		
		int rows[] = table.getSelectedRows();
		for(int row : rows)
		{
			int sub = Integer.parseInt(model.getValueAt(row, 0).toString());
			markData.deleteMark(sid, sub, y, g, season);
		}
		render();
	}
	private void print()
	{
		String[] opts = {"paper_sheet", "one_to_twelve_marks", "ability_exam_paper"};
		final int task = new OptionChooser("print_options", opts).signal;
		if(task != -1){
			final String path = Helper.xlsx("report", frame);
			if(path == null) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					int y = (int) year.getSelectedItem();
					int g = (int) grade.getSelectedItem();
					StudentMarks.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					if(task == 0){
						Object[] stud = {student.name, student.fname, student.gfname, student.code, student.id};
						Paper paper = new Paper();
						paper.createOne(stud, y, g, allSubjects.isSelected());
						paper.build(path);
					}else{
						StudSheet sheet = new StudSheet(sid);
						if(task == 1){
							sheet.fullMarks(stGrade);
						}else{
							sheet.abilityExam(y, g, student.name, student.fname);
						}
						sheet.build(path);
					}
					StudentMarks.this.setCursor(Cursor.getDefaultCursor());
				}
			}).start();
		}
	}
	protected void stop() 
	{
		if(table.isEditing()){
			TableCellEditor editor = table.getCellEditor();
			if(editor != null){
				if(!editor.stopCellEditing()){
					editor.cancelCellEditing();
				}
			}
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}

