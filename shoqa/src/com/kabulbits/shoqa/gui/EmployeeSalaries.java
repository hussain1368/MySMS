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
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class EmployeeSalaries extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static EmployeeSalaries self;
	
	private JSpinner fromDate, toDate, pager;
	private JButton search, delete, create, profile, refresh, print;
	private DefaultTableModel model;
	private JTable table;
	private FinanceData data;
	
	public EmployeeSalaries()
	{
		isOpen = true;
		self = this;
		
		String title = Dic.w("employee_salaries");
		setTitle(title);
		
		data = new FinanceData();
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		fromDate = new JSpinner();
		toDate = new JSpinner();
		
		Dimension dim = new Dimension(100, toDate.getPreferredSize().height);
		fromDate.setPreferredSize(dim);
		toDate.setPreferredSize(dim);
		
		fromDate.setModel(new PDateModel(fromDate));
		toDate.setModel(new PDateModel(toDate));
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		search = new JButton(Dic.w("search"));
		
		form.add(new JLabel(Dic.w("from_date")));
		form.add(fromDate);
		form.add(new JLabel(Dic.w("to_date")));
		form.add(toDate);
		form.add(new JLabel(Dic.w("page")));
		form.add(pager);
		form.add(search);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col == 4 || col == 5 || col == 7;
				}
				return false;
			}
		};
		String headers [] = {"code", "employee_code", "name", "lname", "amount", "date", "cash", "description"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(50);
		table.getColumnModel().getColumn(5).setPreferredWidth(80);
		table.getColumnModel().getColumn(7).setPreferredWidth(180);
		
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(4).setCellEditor(new DBEditor(false) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					try {
						int val = Integer.parseInt(value.toString());
						if(val < 1) return false;
						if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) != 0) return false;
						
						int id = (int) model.getValueAt(row, 0);
						return data.editEmpReceiptAmount(val, id);
					} 
					catch (NumberFormatException e) {
						return false;
					}
				}
			});
			table.getColumnModel().getColumn(7).setCellEditor(new DBEditor(true) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					return data.editEmpReceiptDesc(id, value.toString());
				}
			});
			table.getColumnModel().getColumn(5).setCellEditor(new DateEditor()
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					int sig = Diags.showConf(Diags.SAVE_CONF, Diags.YN);
					if(sig == 0){
						int id = (int) model.getValueAt(row, 0);
						return data.editEmpReceiptDate(id, date);
					}
					return false;
				}
			});
		}
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		delete = new JButton(Dic.w("delete"));
		create = new JButton(Dic.w("new_item"));
		profile = new JButton(Dic.w("employee_finance"));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(profile);
		bleft.add(create);
		
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stop();
				render();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				stop();
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					profile();
				}else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					delete();
				}
				e.consume();
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
		if(Data.PERM_FINANCE == 2){
			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new EmployeeSalaryReg();
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
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setMinimumSize(new Dimension(900, 500));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			create.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	protected void profile() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 1);
		
		EmployeeProfile frame = (EmployeeProfile) OpenFrame.openFrame(
				EmployeeProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(1);
	}

	private void render()
	{
		Date from = (Date) fromDate.getValue();
		Date to = (Date) toDate.getValue();
		int page = (int) pager.getValue();
		
		Vector<Object []> rows = data.employeeTransactions(from, to, page);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		if(data.deleteEmpReceipt(id)){
			render();
		}
	}
	
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cols = {"name", "lname", "amount", "date", "cash", "description"};
				int[] indexes = {2, 3, 4, 5, 6, 7};
				EmployeeSalaries.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("employee_salaries", cols, rows, indexes);
				report.build(path);
				EmployeeSalaries.this.setCursor(Cursor.getDefaultCursor());
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
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

