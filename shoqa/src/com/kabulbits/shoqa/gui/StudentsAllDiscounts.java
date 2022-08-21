package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.DBEditor;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentsAllDiscounts extends JFrame 
{
	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static StudentsAllDiscounts self;
	
	private JComboBox<Integer> year;
	private JComboBox<String> month;
	private JSpinner pager;
	private JButton refresh, delete, profile, print;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	
	public StudentsAllDiscounts()
	{
		isOpen = true;
		self = this;
		
		String title = Dic.w("students_discount_and_subtract");
		setTitle(title);
		
		data = new FinanceData();
		
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		year = new JComboBox<Integer>();
		year.setPrototypeDisplayValue(10000000);
		int y = Data.EDUC_YEAR;
		for(; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		
		String months [] = {"total_year", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola",
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot" };
		
		month = new JComboBox<String>();
		for(String item : months){
			month.addItem(Dic.w(item));
		}
		ULocale locale = new ULocale("@calendar=persian");
		int m = Calendar.getInstance(locale).get(Calendar.MONTH) + 1;
		month.setSelectedIndex(m);
		
		month.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) month.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("educ_year")));
		form.add(year);
		form.add(new JLabel(Dic.w("month")));
		form.add(month);
		form.add(new JLabel(Dic.w("page")));
		form.add(pager);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col > 5;
				}
				return false;
			};
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 6){
					return Option.class;
				}
				if(index == 7){
					return Integer.class;
				}
				return String.class;
			}
		};
		String headers [] = {"code", "student_code", "base_code", "name", "lname", "fname", "month", "amount", "description"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(8).setPreferredWidth(180);
		
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(7).setCellEditor(new DBEditor(false) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					try {
						int val = Integer.parseInt(value.toString());
						if(val < 1) return false;
						if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) != 0 ) return false;
						
						int id = (int) model.getValueAt(row, 0);
						return data.editDiscountAmount(id, val);
					}
					catch (NumberFormatException e) {
						return false;
					}
				}
			});
			table.getColumnModel().getColumn(8).setCellEditor(new DBEditor(true) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					return data.editDiscountDesc(id, value.toString());
				}
			});
			JComboBox<Option> monthsList = new JComboBox<Option>();
			monthsList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel) monthsList.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			for(int i=1; i<months.length; i++){
				monthsList.addItem(new Option(i, Dic.w(months[i])));
			}
			table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(monthsList)
			{
				private static final long serialVersionUID = 1L;
				private Object value;
				private int row;
				@Override
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean isSelected, int row, int column) {
					this.value = value;
					this.row = row;
					return super.getTableCellEditorComponent(table, value, isSelected, row, column);
				}
				@Override
				public Object getCellEditorValue() {
					Object value = super.getCellEditorValue();
					if(value instanceof Option){
						int m = ((Option)value).key;
						int id = (int) model.getValueAt(row, 0);
						if(data.editDiscountMonth(id, m)){
							return value;
						}
					}
					return this.value;
				}
			});
		}
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
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
		
		delete = new JButton(Dic.w("delete"));
		profile = new JButton(Dic.w("student_discount_and_subtract"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(profile);
		
		render();
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		year.addActionListener(render);
		month.addActionListener(render);
		
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stop();
				render();
			}
		});
		profile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				profile();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				print();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				stop();
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					profile();
					e.consume();
				}else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					delete();
					e.consume();
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
		});
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					delete();
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
		
		setMinimumSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			refresh.setEnabled(false);
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
	
	private void render()
	{
		int year = (int) this.year.getSelectedItem();
		int month = this.month.getSelectedIndex();
		int page = (int) pager.getValue();
		
		Vector<Object []> rows = data.allStudentsDiscounts(year, month, page);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
	}
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int id = (int) model.getValueAt(row, 0);
		if(data.deleteDiscount(id)){
			render();
		}
	}

	protected void profile() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		
		int y = (int) year.getSelectedItem();
		int id = (int) model.getValueAt(row, 1);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new StudentDiscount(id, y);
		setCursor(Cursor.getDefaultCursor());
	}
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable(){
			@Override
			public void run(){
				String[] cols = {"base_code", "name", "lname", "fname", "month", "amount", "description"};
				int[] indexes = {2, 3, 4, 5, 6, 7, 8};
				StudentsAllDiscounts.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("students_discount_and_subtract", cols, rows, indexes);
				report.build(path);
				StudentsAllDiscounts.this.setCursor(Cursor.getDefaultCursor());
			}
		}).start();
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}





















