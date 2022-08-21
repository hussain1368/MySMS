package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
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
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.sheet.StudentList;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.PrintRangeChooser;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentSearch extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static StudentSearch self;
	
	private JComboBox<Object> cols, grade, year;
	private JTextField word;
	private JSpinner pager;
	private JLabel numbers;
	private JComboBox<String> gender, status;
	private JCheckBox active;
	private JButton create, edit, enrol, delete, refresh, print;
	private DefaultTableModel model;
	private JTable table;
	
	private StudentData data = new StudentData();
	private String theCols[];
	private int page = 1;
	private int pages;
	
	public StudentSearch() {
		
		isOpen = true;
		self = this;
		String title = Dic.w("search_students");
		setTitle(title);

		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new GridBagLayout());
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);
		
		String attrs[] = { "st_code", "st_name", "st_lname", "st_fname", "st_idcard" };
		String labels[] = {"base_code", "name", "lname", "fname", "idcard_no"};
		this.theCols = attrs;

		cols = new JComboBox<>();
		for(String item : labels){
			cols.addItem(Dic.w(item));
		}
		cols.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cols.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

		word = new JTextField(15);
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		grade = new JComboBox<>();
		grade.addItem(Dic.w("all_grades"));
		for(int i=1; i<13; i++){
			grade.addItem(i);
		}
		
		year = new JComboBox<>();
		year.addItem(Dic.w("entry_year"));
		for(int y=Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		
		gender = new JComboBox<>();
		gender.addItem(Dic.w("both"));
		gender.addItem(Dic.w("male"));
		gender.addItem(Dic.w("female"));
		gender.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)gender.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		gender.setPrototypeDisplayValue("xxxxxxx");
		
		status = new JComboBox<>();
		status.addItem(Dic.w("all"));
		status.addItem(Dic.w("official"));
		status.addItem(Dic.w("unofficial"));
		status.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)status.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

		active = new JCheckBox(Dic.w("active"));
		active.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		pager = new JSpinner();
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		numbers = new JLabel();
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		form.add(new JLabel(Dic.w("search_by")), cons);
		form.add(cols, cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(grade, cons);
		form.add(year, cons);
		form.add(gender, cons);
		form.add(status, cons);
		form.add(active, cons);
		form.add(pager, cons);
		form.add(numbers, cons);

		model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String headers [] = {"code", "base_code", "name", "fname", "lname", "idcard_no", "grade", "entry_year", "grad_year", "status"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setMaxWidth(80);
		table.getColumnModel().getColumn(1).setMaxWidth(80);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		create = new JButton(Dic.w("new_item"));
		edit = new JButton(Dic.w("edit"));
		enrol = new JButton(Dic.w("join_to_class"));
		delete = new JButton(Dic.w("delete"));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		bright.add(create);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(enrol);
		bleft.add(edit);
		bleft.add(delete);

		setMinimumSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		setLocationRelativeTo(null);
		setVisible(true);

		render();
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				page = 1;
				render();
			}
		};
		word.addActionListener(render);
		grade.addActionListener(render);
		year.addActionListener(render);
		gender.addActionListener(render);
		status.addActionListener(render);
		active.addActionListener(render);
		
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		pager.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				page = (int) pager.getValue();
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				page = 1;
				render();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())
				{
				case KeyEvent.VK_ENTER:
					profile();
					e.consume();
					break;
				case KeyEvent.VK_DELETE:
					delete();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if(page < pages) page++;
					render();
					break;
				case KeyEvent.VK_PAGE_UP:
					if(page > 1) page--;
					render();
					break;
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					profile();
				}
			}
		});
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				profile();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		});
		if(Data.PERM_STUDENTS == 2){
			create.addActionListener(new OpenFrame(StudentProfile.class, this));
		}
		if(Data.PERM_COURSES == 2){
			enrol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					enrol();
				}
			});
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {	
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
			create.setEnabled(false);
			refresh.setEnabled(false);
		}
		if(Data.PERM_COURSES != 2){
			enrol.setEnabled(false);
		}
	}
	
	public void render() 
	{
		String col = theCols[cols.getSelectedIndex()];
		String exp = word.getText().trim();
		int grade = this.grade.getSelectedIndex();
		int year = 0;
		if(this.year.getSelectedIndex() > 0){
			year = Integer.parseInt(this.year.getSelectedItem().toString());
		}
		int index = gender.getSelectedIndex();
		String sex = index == 0 ? "b" : index == 1 ? "m" : "f";
		
		index = status.getSelectedIndex();
		int off = index == 0 ? 2 : index == 1 ? 1 : 0;
		
		int limit = Data.LIMIT;
		int total = data.countStudents(col, exp, sex, grade, year, active.isSelected(), off);
		int count = (int) Math.ceil((double)total/limit);
		pages = count > 0 ? count : 1;
		pager.setModel(new SpinnerNumberModel(page, 1, pages > 0 ? pages : 1, 1));
		
		model.setRowCount(0);
		Vector<Object[]> rows = data.searchStudents(col, exp, sex, grade, year, active.isSelected(), off, page);
		for(Object[] row : rows){
			model.addRow(row);
		}
		
		String text = "%d - %d / %d";
		int last = page < pages ? page*limit : total;
		text = String.format(text, page*limit - limit + 1, last, total);
		numbers.setText(text);
	}
	
	
	protected void enrol() 
	{
		int [] rows = table.getSelectedRows();
		if(rows.length == 0) return;
		int [] ids = new int[rows.length];

		int id = (int) model.getValueAt(rows[0], 0);
		int g = data.findGrade(id);
		
		if(g == 13){
			Diags.showErrLang("students_in_graduated_list_error");
			return;
		}
		for(int i=0; i<rows.length; i++)
		{
			id = (int) model.getValueAt(rows[i], 0);
			if(data.findGrade(id) == 13){
				Diags.showErrLang("students_in_graduated_list_error");
				return;
			}
			if(data.findGrade(id) != g){
				Diags.showErrLang("students_in_different_grades_error");
				return;
			}
			ids[i] = (int) model.getValueAt(rows[i], 0);
		}
		new StudentEnrollment(g, ids, 3);
	}

	private void profile()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id});
		setCursor(Cursor.getDefaultCursor());
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int row = table.getSelectedRow();
		String id = model.getValueAt(row, 0).toString();
		if(data.deleteStudent(id)){
			render();
		}
	}
	
	private void print()
	{
		String[] opts = {"advanced_student_sheet", "simple_list"};
		new PrintRangeChooser("print_options", "base_code", opts)
		{
			private static final long serialVersionUID = 1L;
			protected void print(final int task, final int from, final int to)
			{
				final String path = Helper.xlsx("list", StudentSearch.this);
				if(path == null) return;
				new Thread(new Runnable() {
					@Override
					public void run() {
						StudentSearch.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						StudentList list = new StudentList();
						if(task == 0){
							list.advancedSheet(from, to);
						}else{
							String[] cols = {"base_code", "name", "fname", "lname", "idcard_no", "grade", "entry_year"};
							int[] indexes = {1, 2, 3, 4, 5, 6, 7};
							Vector<?> data = model.getDataVector();
							list.simpleSheet("students_list", cols, data, indexes);
						}
						list.build(path);
						StudentSearch.this.setCursor(Cursor.getDefaultCursor());
					}
				}).start();
			}
		};
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}


