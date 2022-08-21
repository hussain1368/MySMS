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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.sheet.Eventuals;
import com.kabulbits.shoqa.sheet.MarkDetails;
import com.kabulbits.shoqa.sheet.MarkSheet;
import com.kabulbits.shoqa.sheet.Paper;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.OptionChooser;
import com.kabulbits.shoqa.util.Ribbon;

public class CourseMarks extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CourseMarks self;
	private boolean isModified = false;
	
	private JComboBox<String> term, studentType, passedStatus;
	private JTextField resultFields [];
	private JTextField word;
	private JComboBox<Option> subjects;
	private JCheckBox allSubs;
	private JButton detach, change, profile, upgrade, repeat, forbid, save, delete, refresh, print;
	private DefaultTableModel model, modelHalf, modelFull, modelSpring, modelFall;
	private JTable table;
	
	private CourseData data;
	private MarkData markData;
	private int cid;
	private Course course;
	
	public CourseMarks(int id) {
		
		isOpen = true;
		self = this;
		
		cid = id;
		data = new CourseData();
		markData = new MarkData();
		course = data.findCourse(cid);
		if(course == null){
			dispose();
			isOpen = false;
			return;
		}
		String title = Dic.w("course_marks");
		setTitle(title);

		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		JPanel form = new JPanel(new GridLayout(1, 3, 4, 0));
		JPanel formLeft = new JPanel(new GridBagLayout());
		JPanel formCenter = new JPanel(new GridBagLayout());
		JPanel formRight = new JPanel(new GridBagLayout());
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		form.add(formRight);
		form.add(formCenter);
		form.add(formLeft);
		
		top.setBorder(new EmptyBorder(0, 3, 0, 3));
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.CENTER);
		top.add(bar, BorderLayout.SOUTH);
		
		Border line = new LineBorder(Color.GRAY);
		formRight.setBorder(new TitledBorder(line, Dic.w("course_details")));
		formCenter.setBorder(new TitledBorder(line, Dic.w("options")));
		formLeft.setBorder(new TitledBorder(line, Dic.w("results_summary")));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		formRight.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		formCenter.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		formLeft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(2, 2, 2, 2);
		
		//=========================================================================
		
		JTextField [] infoFields = {
				new JTextField(String.valueOf(course.grade), 10),
				new JTextField(String.valueOf(course.shift), 10),
				new JTextField(String.valueOf(course.name), 10),
		};
		String texts [] = {"grade", "shift", "identifier"};
		
		for(int i=0; i<infoFields.length; i++)
		{
			infoFields[i].setEditable(false);
			infoFields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			cons.gridy = i;
			cons.fill = GridBagConstraints.NONE;
			cons.weightx = 0;
			formRight.add(new JLabel(Dic.w(texts[i])), cons);
			cons.weightx = 1;
			formRight.add(infoFields[i], cons);
		}
		
		//========================================================================
		
		term = new JComboBox<String>(new String [] {
				Dic.w("total_mark"),
				Dic.w("half_mark"),
				Dic.w("final_mark"),
		});
		studentType = new JComboBox<String>(new String [] {
				Dic.w("both"),
				Dic.w("official"),
				Dic.w("unofficial"),
		});
		passedStatus = new JComboBox<String>(new String [] {
				Dic.w("all_students"),
				Dic.w("passed"),
				Dic.w("failed"),
				Dic.w("eventual"),
				Dic.w("forbidden"),
				Dic.w("excused"),
		});
		JComboBox<?> combos [] = {term, studentType, passedStatus};
		for(JComboBox<?> combo : combos){
			combo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel) combo.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		}
		
		cons.weighty = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.gridx = 0;
		cons.gridy = 0;
		
		formCenter.add(new JLabel(Dic.w("marks_section")), cons);
		cons.gridy++;
		formCenter.add(new JLabel(Dic.w("student_type")), cons);
		cons.gridy++;
		formCenter.add(new JLabel(Dic.w("status")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		cons.gridy = 0;
		
		formCenter.add(term, cons);
		cons.gridy++;
		formCenter.add(studentType, cons);
		cons.gridy++;
		formCenter.add(passedStatus, cons);
		cons.gridy++;
		
		//=====================================================================
		
		String labels [] = {"enroled", "exam_involved", "passed", "failed", "forbidden", "excused"};
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		
		for(int i=0; i<labels.length; i++){
			cons.gridy = i < 3 ? i : i-3;
			cons.gridx = i < 3 ? 0 : 2;
			formLeft.add(new JLabel(Dic.w(labels[i])), cons);
		}
		resultFields = new JTextField[labels.length];
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		
		for(int i=0; i<resultFields.length; i++)
		{
			resultFields[i] = new JTextField();
			resultFields[i].setHorizontalAlignment(JTextField.RIGHT);
			resultFields[i].setEditable(false);
			
			cons.gridy = i < 3 ? i : i-3;
			cons.gridx = i < 3 ? 1 : 3;
			formLeft.add(resultFields[i], cons);
		}
		
		//=====================================================
		
		word = new JTextField(20);
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		subjects = new JComboBox<>(data.subjectList(course.grade, false));
		subjects.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)subjects.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		subjects.setPreferredSize(new Dimension(125, subjects.getPreferredSize().height));
		subjects.setMaximumRowCount(10);
		
		allSubs = new JCheckBox(Dic.w("all_subjects"));
		allSubs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		bar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bar.add(new JLabel(Dic.w("st_code_or_name")));
		bar.add(word);
		bar.add(new JLabel(Dic.w("subject")));
		bar.add(subjects);
		bar.add(allSubs);
		
		//======================table models===============
		
		modelHalf = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col > 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index > 3){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols1 [] = {"code", "base_code", "name", "fname", "written", "oral", "practical", "activity", "homework", "total"};
		for(String col : cols1){
			modelHalf.addColumn(Dic.w(col));
		}
		modelFull = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col > 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index > 7){
					return Boolean.class;
				}
				if(index > 3){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols2 [] = {"code", "base_code", "name", "fname", "half_mark", "final_mark", "total", "second_mark", "midterm_excused", "final_excused"};
		for(String col : cols2){
			modelFull.addColumn(Dic.w(col));
		}
		modelSpring = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col > 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index > 3){
					return Float.class;
				}
				return Object.class;
			}
		};
		String cols3 [] = {"code", "base_code", "name", "fname", "hamal", "sawr", "jawza", "saratan", "total"};
		for(String col : cols3){
			modelSpring.addColumn(Dic.w(col));
		}
		modelFall = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col > 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index > 3){
					return Float.class;
				}
				return Object.class;
			}
		};
		modelFall.addColumn(Dic.w("code"));
		modelFall.addColumn(Dic.w("base_code"));
		modelFall.addColumn(Dic.w("name"));
		modelFall.addColumn(Dic.w("fname"));
		modelFall.addColumn(Dic.w("asad"));
		modelFall.addColumn(Dic.w("sonbola"));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("mizan"), 1));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("mizan"), 2));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("aqrab"), 1));
		modelFall.addColumn(String.format("%s (%d)", Dic.w("aqrab"), 2));
		modelFall.addColumn(Dic.w("total"));
		
		ActionListener switchModel = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				season = term.getSelectedIndex();
				if(season == 0){
					model = modelFull;
				}
				else if(course.grade > 1){
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
				render();
			}
		};
		term.addActionListener(switchModel);
		
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
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		
		String items [] = {"student_marks", "upgrade", "repeat_grade", "set_forbidden", "change_course", "detach_from_course", "delete_marks"};
		String icons [] = {"images/tick.png", "", "", "", "", "", "images/delete.png"};
		JMenuItem menus [] = new JMenuItem[items.length];
		Font font = new Font("tahoma", Font.PLAIN, 11);
		Dimension dim = new Dimension(150, 0);
		final JPopupMenu menu = new JPopupMenu();
		
		for(int i=0; i<items.length; i++){
			menus[i] = new JMenuItem(Dic.w(items[i]), new ImageIcon(icons[i]));
			dim.height = menus[i].getPreferredSize().height;
			menus[i].setPreferredSize(dim);
			menus[i].setFont(font);
			menu.add(menus[i]);
		}
		if(Data.PERM_COURSES != 2){
			for(int i=1; i<items.length-1; i++){
				menus[i].setEnabled(false);
			}
		}
		if(Data.USER_ID != 1){
			menus[6].setEnabled(false);
		}
		JPanel center = new JPanel(new BorderLayout(5, 0));
		JPanel sideBar = new JPanel(new GridBagLayout());
		JScrollPane pane = new JScrollPane(table);
		
		center.setBorder(new EmptyBorder(0, 5, 0, 5));
		center.add(pane, BorderLayout.CENTER);
		center.add(sideBar, BorderLayout.WEST);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		profile = new JButton(Dic.w("student_marks"));
		upgrade = new JButton(Dic.w(course.grade == 12 ?  "reg_graduation" : "upgrade"));
		repeat = new JButton(Dic.w("repeat_grade"));
		forbid = new JButton(Dic.w("set_forbidden"));
		change = new JButton(Dic.w("change_course"));
		detach = new JButton(Dic.w("detach_from_course"));
		
		sideBar.setBorder(new TitledBorder(line, Dic.w("operations"), TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
		
		GridBagConstraints conss = new GridBagConstraints();
		conss.fill = GridBagConstraints.HORIZONTAL;
		conss.anchor = GridBagConstraints.FIRST_LINE_START;
		conss.insets = new Insets(2, 2, 2, 2);
		conss.weightx = 1;
		conss.gridx = 0;
		
		sideBar.add(profile, conss);
		sideBar.add(upgrade, conss);
		sideBar.add(repeat, conss);
		sideBar.add(forbid, conss);
		sideBar.add(change, conss);
		conss.weighty = 1;
		sideBar.add(detach, conss);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		delete = new JButton(Dic.w("delete_marks"));
		save = new JButton(Dic.w("save"));
	
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setMargin(new Insets(2, 5, 2, 5));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		bright.add(delete);
		bright.add(save);
		bright.add(refresh);
		bleft.add(print);
		
		//====================================================================
		
		render();

		allSubs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int g = allSubs.isSelected() ? 0 : course.grade;
				subjects.removeAllItems();
				Vector<Option> options = data.subjectList(g, false);
				for(Option option : options){
					subjects.addItem(option);
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
		studentType.addActionListener(render);
		passedStatus.addActionListener(render);
		subjects.addActionListener(render);
		
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				stop();
				render();
			}
		});
		ActionListener doProfile = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				profile();
			}
		};
		profile.addActionListener(doProfile);
		menus[0].addActionListener(doProfile);
		
		if(Data.PERM_COURSES == 2){
			ActionListener doUpgrade = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					if(course.grade == 12){
						graduate();
					}else{
						enrol(UPGRADE);
					}
				}
			};
			upgrade.addActionListener(doUpgrade);
			menus[1].addActionListener(doUpgrade);
			
			ActionListener doRepeat = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					enrol(ENROL);
				}
			};
			repeat.addActionListener(doRepeat);
			menus[2].addActionListener(doRepeat);
			
			ActionListener doForbid = new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
					stop();
					return forbid();
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
					}
				}
			};
			forbid.addActionListener(doForbid);
			menus[3].addActionListener(doForbid);
			
			ActionListener doChange = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					stop();
					enrol(CHANGE);
				}
			};
			change.addActionListener(doChange);
			menus[4].addActionListener(doChange);
			
			ActionListener doDetach = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					stop();
					detach();
				}
			};
			detach.addActionListener(doDetach);
			menus[5].addActionListener(doDetach);
			
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
					stop();
					if(isModified){
						return doSave();
					}
					return 0;
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						render();
					}
				}
			});
		}
		if(Data.USER_ID == 1){
			ActionListener doDelete = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					delete();
				}
			};
			delete.addActionListener(doDelete);
			menus[6].addActionListener(doDelete);
		}
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() ==  KeyEvent.VK_ENTER){
					stop();
					profile();
					e.consume();
				}
				else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					stop();
					delete();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					stop();
					profile();
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					Point point = e.getPoint();
					if(table.getSelectedRowCount() < 2)
					{
						int row = table.rowAtPoint(point);
						table.setRowSelectionInterval(row, row);
					}
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
		    }
		});
		print.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				stop();
				print();
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				isOpen = false;
			}
		});
		disableButtons();
		
		setSize(1000, 600);
		setMinimumSize(new Dimension(900, 600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_COURSES != 2)
		{
			detach.setEnabled(false);
			change.setEnabled(false);
			upgrade.setEnabled(false);
			repeat.setEnabled(false);
			forbid.setEnabled(false);
			save.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	private int season = 0;
	private boolean doSum = true;

	protected void sum() 
	{
		int row = table.getSelectedRow();
		if(row < 0) return;
		if(!doSum) return;
		doSum = false;
		
		int last = (season == 0)? 6 : (course.grade > 1)? 9 : (season == 1)? 8 : 10;
		float total = 0;
		boolean isNull = true;
		
		for(int i=4; i<last; i++){
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
	public int ALL = 0;
	public int PASSED = 1;
	public int FAILD = 2;
	public int EVENTUAL = 3;
	public int FORBIDDEN = 4;
	public int EXCUSED = 5;

	public void render() 
	{
		String exp = word.getText().trim();
		Object item = subjects.getSelectedItem();
		int sub = item != null ? ((Option)item).key : 0;
		
		model.setRowCount(0);
		Vector<Object []> rows;
		
		int filter = passedStatus.getSelectedIndex();
		
		int index = studentType.getSelectedIndex();
		int status = index == 1 ? 1 : index == 2 ? 0 : 2;
		
		if(season == 0){
			rows = markData.fullMarks(course, sub, exp, status, filter);
		}else{
			rows = markData.seasonMarks(course, sub, season, exp, status, filter);
		}
		for(Object [] row : rows){
			model.addRow(row);
		}
		int entry = markData.memberCount(course.id, false);
		int forbid = markData.memberCount(course.id, true);
		int passed = markData.passedCount(course, season);
		int fail = markData.failedCount(course, season);
		int excused = markData.excusedCount(course, season);
		
		resultFields[0].setText(String.valueOf(entry));
		resultFields[1].setText(String.valueOf(entry-forbid));
		resultFields[2].setText(String.valueOf(passed));
		resultFields[3].setText(String.valueOf(fail));
		resultFields[4].setText(String.valueOf(forbid));
		resultFields[5].setText(String.valueOf(excused));
		isModified = false;
	}
	private void profile()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		JFrame frame = OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		((StudentProfile)frame).activeTab(3);
	}
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		Object item = subjects.getSelectedItem();
		if(item == null) return;
		int sub = ((Option)item).key;
		if(table.getSelectedRowCount() == 0) return;
		if(Diags.showConfLang("delete_marks_conf", Diags.YN) != 0) return;
		
		int rows[] = table.getSelectedRows();
		for(int row : rows) {
			int sid = (int) model.getValueAt(row, 0);
			markData.deleteMark(sid, sub, course.year, course.grade, season);
		}
		render();
	}
	private int doSave()
	{
		Object item = subjects.getSelectedItem();
		if(item == null) return 0;
		int sub = ((Option)item).key;
		int count = season != 0 ? saveSeason(sub) : saveFull(sub);
		return count;
	}
	private int saveSeason(int sub)
	{
		int cols = model.getColumnCount();
		int rows = model.getRowCount();
		int last = cols - 1;
		
		int count = 0;
		
		for(int r=0; r<rows; r++)
		{
			if(model.getValueAt(r, last) == null) continue;
			int id = (int) model.getValueAt(r, 0);
			
			Object marks [] = new Object[7];
			int i = 0;
			for(int c=4; c<last; c++) {
				marks[i++] = model.getValueAt(r, c);
			}
			marks[6] = model.getValueAt(r, last);
			if(markData.saveSeasonMark(id, marks, course.grade, course.year, season, sub)) count++;
		}
		return count;
	}
	private int saveFull(int sub)
	{
		int rows = model.getRowCount();
		int count = 0;
		
		for(int r=0; r<rows; r++) 
		{
			boolean next = false;
			Object marks [] = new Object[4];
			int i = 0, c = 4;
			for(; i<4; i++){
				marks[i] = model.getValueAt(r, c++);
				if(marks[i] != null) next = true;
			}
			if(!next) continue;
			
			boolean midtermExcused = (boolean) model.getValueAt(r, c++);
			boolean finalExcused = (boolean) model.getValueAt(r, c++);
			int id = (int) model.getValueAt(r, 0);
			if(markData.saveFullMark(id, marks, midtermExcused, finalExcused, 1, course.grade, course.year, sub)) count++;
		}
		return count;
	}
	private final int UPGRADE = 1;
	private final int CHANGE = 2;
	private final int ENROL = 3;
	
	private void enrol(int task)
	{
		if(table.getSelectedRowCount() == 0) return;
		int [] rows = table.getSelectedRows();
		int [] ids = new int[rows.length];
		
		for(int i = ids.length-1; i>=0; i--){
			ids[i] = (int) model.getValueAt(rows[i], 0);
		}
		int g = (task == CHANGE)? cid : course.grade;
		new StudentEnrollment(g, ids, task);
	}
	private int forbid()
	{
		int [] rows = table.getSelectedRows();
		if(rows.length == 0) return 0;
		
		String ops [] = {"remove_forbidden", "reg_forbidden"};
		int forbid = Diags.showOps("reg_or_remove_forbiddens", ops);
		if(forbid < 0) return 0;
		
		int count = 0;
		for(int row : rows){
			int id = (int) model.getValueAt(row, 0);
			if(data.setForbidden(id, course.id, forbid)) count++;
		}
		return count;
	}
	private void graduate()
	{
		if(table.getSelectedRowCount() == 0) return;
		int [] rows = table.getSelectedRows();
		
		int count = 0;
		for(int row : rows) {
			int id = Integer.parseInt(model.getValueAt(row, 0).toString());
			if(markData.graduateStudent(id)) count++;
		}
		Diags.showMsg(String.format(Dic.w("items_saved"), count));
	}
	private void detach()
	{
		int [] rows = table.getSelectedRows();
		if(rows.length == 0) return;
		if(Diags.showConfLang("students_detach_conf", Diags.YN) != 0) return;
		
		for(int row : rows) {
			int id = Integer.parseInt(model.getValueAt(row, 0).toString());
			data.detachStudent(id, cid);
		}
		render();
		isModified = false;
	}
	private void print()
	{
		String[] options = {"students_list", "details_or_monthly_sheet", "marks_sheet", "paper_sheets", "eventual_sheet"};
		final int signal = new OptionChooser("print_options", options).signal;
		if(signal != -1){
			int task = -1;
			if(signal == 4){
				task = Diags.showOps("choose_one_option", new String[]{"only_this_course", "all_courses"});
				if(task == -1) return;
			}
			final boolean allCourses = (task == 1);
			final String path = Helper.xlsx("report", self);
			if(path == null) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					CourseMarks.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					switch(signal){
					case 0:
						Vector<?> data = model.getDataVector();
						String[] cols = {"base_code", "name", "fname"};
						int[] indexes = {1, 2, 3};
						Report printer = new Report();
						printer.simpleSheet("course_students_list", cols, data, indexes);
						printer.build(path);
						break;
					case 1:
						MarkDetails mdetails = new MarkDetails(course);
						if(course.grade == 1){
							mdetails.firstGrade();
						}else{
							Option option = (Option)subjects.getSelectedItem();
							int index = studentType.getSelectedIndex();
							int status = index == 1 ? 1 : index == 2 ? 0 : 2;
							mdetails.upperGrades(option.key, option.value, status, season);
						}
						mdetails.build(path);
						break;
					case 2:
						new MarkSheet(course).build(path);
						break;
					case 3:
						Paper paper = new Paper();
						paper.createAll(course, allSubs.isSelected());
						paper.build(path);
						break;
					case 4:
						new Eventuals(course, allCourses).build(path);
						break;
					}
					CourseMarks.this.setCursor(Cursor.getDefaultCursor());
				}
			}).start();
		}
	}
	protected void stop() {
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
		if(data != null){
			data.closeConn();
		}
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}






















