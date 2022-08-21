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
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.DBEditor;
import com.kabulbits.shoqa.util.DateEditor;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentsAllPayments extends JFrame 
{
	private static final long serialVersionUID = 1L;

	public static boolean isOpen = false;
	public static StudentsAllPayments self;
	
	private JSpinner fromDate, toDate, pager;
	private JComboBox<Integer> year;
	private JButton refresh, delete, profile, print;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	
	public StudentsAllPayments()
	{
		isOpen = true;
		self = this;
		
		String title = Dic.w("students_payments");
		setTitle(title);
		
		data = new FinanceData();
		
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		fromDate = new JSpinner();
		toDate = new JSpinner();
		fromDate.setModel(new PDateModel(fromDate));
		toDate.setModel(new PDateModel(toDate));
		
		Dimension dim = fromDate.getPreferredSize();
		dim.width = 100;
		fromDate.setPreferredSize(dim);
		toDate.setPreferredSize(dim);
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		year = new JComboBox<Integer>();
		year.setPrototypeDisplayValue(10000000);
		int y = Data.EDUC_YEAR;
		for(; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		JButton search = new JButton(Dic.w("search"));
		
		form.add(new JLabel(Dic.w("from_date")));
		form.add(fromDate);
		form.add(new JLabel(Dic.w("to_date")));
		form.add(toDate);
		form.add(new JLabel(Dic.w("educ_year")));
		form.add(year);
		form.add(new JLabel(Dic.w("page")));
		form.add(pager);
		form.add(search);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col == 6 || col == 7 || col == 9;
				}
				return false;
			}
		};
		String headers [] = {"code", "student_code", "base_code", "name", "lname", "fname", "amount", "date", "cash", "description"};
		for(String header : headers){
			model.addColumn(Dic.w(header));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(7).setPreferredWidth(100);
		table.getColumnModel().getColumn(9).setPreferredWidth(180);
		
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(7).setCellEditor(new DateEditor()
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					int sig = Diags.showConf(Diags.SAVE_CONF, Diags.YN);
					if(sig == 0){
						int id = (int) model.getValueAt(row, 0);
						return data.editStudPaymentDate(id, date);
					}
					return false;
				}
			});
			table.getColumnModel().getColumn(6).setCellEditor(new DBEditor(false) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					try {
						int val = Integer.parseInt(value.toString());
						if(val < 1) return false;
						
						int sig = Diags.showConf(Diags.SAVE_CONF, Diags.YN);
						if(sig != 0) return false;
						
						int id = (int) model.getValueAt(row, 0);
						return data.editStudPaymentAmount(val, id);
					} 
					catch (NumberFormatException e) {
						return false;
					}
				}
			});
			table.getColumnModel().getColumn(9).setCellEditor(new DBEditor(true) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					return data.editStudPaymentDesc(id, value.toString());
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
		profile = new JButton(Dic.w("student_payments"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(profile);
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		year.addActionListener(render);
		
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
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int id = (int) model.getValueAt(row, 0);
		if(data.deleteStPayment(id)){
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
		new StudentPayment(id, y);
		setCursor(Cursor.getDefaultCursor());
	}

	protected void render() 
	{
		int y = (int) year.getSelectedItem();
		Date from = (Date) fromDate.getValue();
		Date to = (Date) toDate.getValue();
		int page = (int) pager.getValue();
		
		Vector<Object []> rows = data.allStudentsPayments(y, from, to, page);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
	}
	
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cols = {"base_code", "name", "lname", "fname", "amount", "date", "cash", "description"};
				int[] indexes = {2, 3, 4, 5, 6, 7, 8, 9};
				StudentsAllPayments.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("students_payments", cols, rows, indexes);
				report.build(path);
				StudentsAllPayments.this.setCursor(Cursor.getDefaultCursor());
			}
		}).start();
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





















