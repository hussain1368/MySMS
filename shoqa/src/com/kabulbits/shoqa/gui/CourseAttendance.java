package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.sheet.Attendance;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class CourseAttendance extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static CourseAttendance self;
	private boolean isModified = false;
	
	private JComboBox<String> month;
	private JTextField word;
	private JButton profile, refresh, save, regMid, print;
	private DefaultTableModel model;
	private JTable table;
	
	private MarkData markData;
	private Course course;
	private int cid;
	
	public CourseAttendance(int id)
	{
		isOpen = true;
		self = this;
		cid = id;
		
		String title = Dic.w("course_attendance");
		setTitle(title);
		
		markData = new MarkData();
		course = markData.findCourse(id);
		if(course == null){
			dispose();
			isOpen = false;
			return;
		}
		
		JPanel ribbon = new Ribbon(title, true);
		JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(infoBar, BorderLayout.CENTER);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		infoBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		infoBar.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		
		JTextField info1, info2, info3;
		info1 = new JTextField(String.valueOf(course.grade), 4);
		info2 = new JTextField(String.valueOf(course.shift), 4);
		info3 = new JTextField(String.valueOf(course.name), 8);
		
		info1.setHorizontalAlignment(JTextField.RIGHT);
		info2.setHorizontalAlignment(JTextField.RIGHT);
		info3.setHorizontalAlignment(JTextField.RIGHT);
		
		info1.setEditable(false);
		info2.setEditable(false);
		info3.setEditable(false);
		
		infoBar.add(new JLabel(Dic.w("grade")));
		infoBar.add(info1);
		infoBar.add(new JLabel(Dic.w("shift")));
		infoBar.add(info2);
		infoBar.add(new JLabel(Dic.w("identifier")));
		infoBar.add(info3);
		
		String monthKeys [] = {
				"total_year", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot",
		};
		month = new JComboBox<String>();
		month.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) month.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		month.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		for(String item : monthKeys) {
			month.addItem(Dic.w(item));
		}
		ULocale locale = new ULocale("@calendar=persian");
		int m = Calendar.getInstance(locale).get(Calendar.MONTH) + 1;
		month.setSelectedIndex(m);
		
		word = new JTextField(20);
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("st_code_or_name")));
		form.add(word);
		form.add(new JLabel(Dic.w("month")));
		form.add(month);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
				if(Data.PERM_COURSES == 2){
					if(month.getSelectedIndex() == 0){
						return false;
					}
					return col > 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index){
				if(index > 3){
					return Integer.class;
				}
				return String.class;
			}
		};
		String cols [] = {"code", "base_code", "name", "fname", "present", "absent", "sick", "holiday"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 4, 0, 4), new LineBorder(Color.GRAY)));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		save = new JButton(Dic.w("save"));
		regMid = new JButton(Dic.w("reg_midterm_attendance"));
		profile = new JButton(Dic.w("student_profile"));
		
		bright.add(regMid);
		bright.add(save);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(profile);
		
		render();
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e){
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		month.addActionListener(render);

		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				stop();
				render();
			}
		});
		if(Data.PERM_COURSES == 2){
			save.addActionListener(new BgWorker<Integer>(this) {
				@Override
				protected Integer save(){
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
			regMid.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					regMidterm();
				}
			});
		}
		profile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				profile();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					profile();
					e.consume();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					profile();
				}
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				print();
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setSize(800, 600);
		setMinimumSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private void disableButtons()
	{
		if(Data.PERM_COURSES != 2){
			refresh.setEnabled(false);
			save.setEnabled(false);
			regMid.setEnabled(false);
		}
	}
	
	private int doSave()
	{
		int y = course.year;
		int m = month.getSelectedIndex();
		if(m == 0) return 0;
		
		int count = 0;
		boolean isNull = true;
		for(int i=0; i<model.getRowCount(); i++)
		{
			Object values [] = new Object[4];
			isNull = true;
			for(int j=0; j<4; j++)
			{
				Object val = model.getValueAt(i, j+4);
				if(val != null) isNull = false;
				values[j] = val;
			}
			if(isNull) continue;
			int id = Integer.parseInt(model.getValueAt(i, 0).toString());
			if(markData.saveAttendance(id, y, m, values)) count++;
		}
		return count;
	}
	private void regMidterm()
	{
		if(model.getRowCount() == 0) return;
		int count = 0;
		for(int i=0; i<model.getRowCount(); i++)
		{
			int id = (int) model.getValueAt(i, 0);
			if(markData.setMidtermAttend(id, course.year)) count++;
		}
		Diags.showMsg(String.format(Dic.w("items_saved"), count));
	}
	private void render() 
	{
		int y = course.year;
		int m = month.getSelectedIndex();
		String exp = word.getText().trim();
		
		Vector<Object []> rows = markData.courseAttendance(cid, y, m, exp);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
		isModified = false;
	}
	private void profile()
	{
		if(table.getSelectedRowCount() != 1) return;
		int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
		
		StudentProfile frame = (StudentProfile) OpenFrame.openFrame(
				StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(4);
	}
	private void print()
	{
		final String path = Helper.xlsx("report", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				CourseAttendance.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				new Attendance(course).build(path);
				CourseAttendance.this.setCursor(Cursor.getDefaultCursor());
			}
		}).start();
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
















