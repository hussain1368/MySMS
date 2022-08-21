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
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class CoursePayments extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CoursePayments self;
	
	private boolean isModified = false;
	private JSpinner paymentDate;
	private JTextField word;
	private JComboBox<Option> cash;
	private JComboBox<String> month;
	private DefaultTableModel model;
	private JTable table;

	private FinanceData data;
	private Course course;
	private int cid;
	
	public CoursePayments(int id)
	{
		isOpen = true;
		self = this;
		
		cid = id;
		course = new CourseData().findCourse(id);
		if(course == null){
			dispose();
			isOpen = false;
			return;
		}
		data = new FinanceData();
		
		String title = Dic.w("students_payments_reg");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel searchBar = new JPanel(new BorderLayout());
		JPanel sright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel sleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		sright.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		sleft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		infoBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		infoBar.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		
		searchBar.add(sright, BorderLayout.EAST);
		searchBar.add(sleft, BorderLayout.WEST);
		top.add(ribbon, BorderLayout.NORTH);
		top.add(infoBar, BorderLayout.CENTER);
		top.add(searchBar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
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
		
		word = new JTextField(18);
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		String months [] = {
				"total_year", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot",
		};
		
		month = new JComboBox<>();
		for(String item : months){
			month.addItem(Dic.w(item));
		}
		ULocale locale = new ULocale("@calendar=persian");
		Calendar cal = Calendar.getInstance(locale);
		int m = cal.get(Calendar.MONTH) + 1;
		month.setSelectedIndex(m);
		
		month.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) month.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		month.setPrototypeDisplayValue("xxxxxxxxxxxx");

		cash = new JComboBox<>(data.cashOptions(false));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		paymentDate = new JSpinner();
		paymentDate.setModel(new PDateModel(paymentDate));
		paymentDate.setPreferredSize(new Dimension(90, paymentDate.getPreferredSize().height));
		
		if(cal.get(Calendar.YEAR) != course.year){
			cal.set(Calendar.YEAR, course.year);
			paymentDate.setValue(cal.getTime());
		}
		
		sright.add(new JLabel(Dic.w("st_code_or_name")));
		sright.add(word);
		sright.add(new JLabel(Dic.w("month")));
		sright.add(month);
		
		sleft.add(new JLabel(Dic.w("cash")));
		sleft.add(cash);
		sleft.add(new JLabel(Dic.w("payment_date")));
		sleft.add(paymentDate);
		
		String headers [] = {"code", "base_code", "name", "fname", "lname", "total_cost", "paying_amount", "description"};
		
		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 6){
					return Integer.class;
				}
				return String.class;
			}
			@Override
			public boolean isCellEditable(int row, int col) {
				return col > 5;
			}
		};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(7).setPreferredWidth(180);
		table.getColumnModel().getColumn(7).setCellEditor(Helper.rtlEditor());
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		JButton save, profile, payments, refresh;
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		save = new JButton(Dic.w("save"));
		profile = new JButton(Dic.w("student_finance"));
		payments = new JButton(Dic.w("student_payments"));
		
		bright.add(save);
		bright.add(refresh);
		bleft.add(payments);
		bleft.add(profile);
		
		render();
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				stop();
				render();
			}
		});
		
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		month.addActionListener(render);
		refresh.addActionListener(render);
		
		profile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				profile();
			}
		});
		payments.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				payments();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				stop();
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					payments();
					e.consume();
				}
			}
		});
		table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				stop();
				if(e.getClickCount() == 2){
					payments();
				}
			}
		});
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
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(950, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private int doSave() {
		Date date = (Date) paymentDate.getValue();
		if(date == null){
			Diags.showErrLang("pay_date_is_required");
			return 0;
		}
		int cashid = ((Option)cash.getSelectedItem()).key;
		int rows = model.getRowCount();
		
		int count = 0;
		for(int i=0; i<rows; i++){
			Object value = model.getValueAt(i, 6);
			if(value == null) continue;
			
			int val = Integer.parseInt(value.toString());
			String descr = model.getValueAt(i, 7) == null ? "" : model.getValueAt(i, 7).toString(); 
			int id = (int) model.getValueAt(i, 0);
			
			if(data.saveStudentPayment(id, val, cashid, course.year, date, descr)) count++;
		}
		return count;
	}
	
	protected void payments() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new StudentPayment(id, course.year);
		setCursor(Cursor.getDefaultCursor());
	}

	private void profile()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		
		StudentProfile frame = (StudentProfile) 
				OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(5);
	}
	
	private void render()
	{
		int m = month.getSelectedIndex();
		String exp = word.getText().trim();
		Vector<Object []> rows = data.studentsPayable(cid, course.year, m, course.grade, exp);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
		isModified = false;
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
		super.finalize();
	}
}

