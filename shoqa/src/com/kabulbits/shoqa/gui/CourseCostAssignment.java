package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
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
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Cost;
import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class CourseCostAssignment extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CourseCostAssignment self;
	
	private boolean isModified = false;
	private JComboBox<String> month;
	private JComboBox<Cost> costType;
	private JTextField word, amount;
	private JButton refresh, save, profile;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	private CourseData courseData;
	private Course course;
	private int cid;
	
	public CourseCostAssignment(int id) 
	{
		isOpen = true;
		self = this;
		
		data = new FinanceData();
		courseData = new CourseData();
		
		course = courseData.findCourse(id);
		if(course == null){
			dispose();
			isOpen = false;
			return;
		}
		cid = id;

		String title = Dic.w("costs_determination");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, true);

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
		
		word = new JTextField(15);
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		String monthNames [] = {
				"hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot",
		};
		
		month = new JComboBox<>();
		for(String item : monthNames){
			month.addItem(Dic.w(item));
		}
		ULocale locale = new ULocale("@calendar=persian");
		int m = Calendar.getInstance(locale).get(Calendar.MONTH);
		month.setSelectedIndex(m);
		
		month.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) month.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		month.setPrototypeDisplayValue("xxxxxxxx");
		
		costType = new JComboBox<>(data.costTypeList(course.year, course.grade));
		costType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) costType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		amount = new JTextField(6);
		amount.setEditable(false);
		amount.setBackground(Color.WHITE);
		
		JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		infoBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		infoBar.add(new JLabel(Dic.w("grade")));
		infoBar.add(tip1);
		infoBar.add(new JLabel(Dic.w("shift")));
		infoBar.add(tip2);
		infoBar.add(new JLabel(Dic.w("identifier")));
		infoBar.add(tip3);
		
		JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		searchBar.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		searchBar.add(new JLabel(Dic.w("st_code_or_name")));
		searchBar.add(word);
		searchBar.add(new JLabel(Dic.w("month")));
		searchBar.add(month);
		searchBar.add(new JLabel(Dic.w("cost_name")));
		searchBar.add(costType);
		searchBar.add(new JLabel(Dic.w("amount")));
		searchBar.add(amount);
		
		JPanel top = new JPanel(new BorderLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(infoBar, BorderLayout.CENTER);
		top.add(searchBar, BorderLayout.SOUTH);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col > 4;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 5){
					return Boolean.class;
				}
				return Object.class;
			}
		};
		String headers [] = {"code", "base_code", "name", "fname", "lname", "involvement"};
		for(String str : headers){
			model.addColumn(Dic.w(str));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(60);
		table.getColumnModel().getColumn(5).setPreferredWidth(60);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		
		save = new JButton(Dic.w("save"));
		profile = new JButton(Dic.w("student_finance"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(save);
		bright.add(refresh);
		bleft.add(profile);

		add(top, BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		setPrice();
		render();

		//Listeners
		costType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPrice();
				render();
			}
		});
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
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render();
			}
		};
		month.addActionListener(render);
		refresh.addActionListener(render);
		
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				render();
			}
		});
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		if(Data.PERM_FINANCE == 2){
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
					return doSave();
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						render();
					}
				}
			});
			table.getTableHeader().addMouseListener(new MouseAdapter() 
			{
				private boolean check = true;
			    public void mousePressed(MouseEvent e)
			    {
			        if(model.getRowCount() == 0) return;
		        	int col = table.columnAtPoint(e.getPoint());
			        if(col == 5)
			        {
			        	for(int i=0; i<model.getRowCount(); i++){
			        		model.setValueAt(check, i, col);
			        	}
			        	check = !check;
			        }	        	
			    }
			});
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setMinimumSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			refresh.setEnabled(false);
			save.setEnabled(false);
		}
	}

	protected void setPrice() 
	{
		Object item = costType.getSelectedItem();
		if(item != null){
			int value = ((Cost)item).value;
			amount.setText(String.valueOf(value));
		}
	}

	private void render() 
	{
		Object item = costType.getSelectedItem();
		if(item == null) return;
		
		int cost = ((Cost)item).id;
		int m = month.getSelectedIndex() + 1;
		String exp = word.getText().trim();
		
		model.setRowCount(0);
		Vector<Object []> rows = data.stCostCalc(cid, course.year, m, cost, exp);
		
		for(Object [] row : rows){
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
		frame.activeTab(5);
	}
	
	private int doSave()
	{
		if(!isModified) return 0;
		
		Object item = costType.getSelectedItem();
		if(item == null) return 0;
		
		int cost = ((Cost)item).id;
		int m = month.getSelectedIndex() + 1;
		int count = 0;
		for(int i=0; i<table.getRowCount(); i++)
		{
			int sid = Integer.parseInt(model.getValueAt(i, 0).toString());
			boolean assign = (boolean) model.getValueAt(i, 5);
			
			if(assign){
				if(data.addCostForStudent(sid, course.year, m, cost, course.grade)) count++;
			}else{
				if(data.removeCostFromStudent(sid, course.year, m, cost)) count++;
			}
		}
		return count;
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		if(courseData != null){
			courseData.closeConn();
		}
		super.finalize();
	}
}


