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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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

public class AccountTransaction extends JFrame{

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static AccountTransaction self;
	
	private JSpinner fromDate, toDate;
	private JRadioButton revenue, expense, both;
	private JSpinner pager;
	private JButton search, delete, payment, receipt, refresh, print;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	
	public AccountTransaction(){
		
		isOpen = true;
		self = this;
		data = new FinanceData();

		String title = Dic.w("revenue_and_expense");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		fromDate = new JSpinner();
		toDate = new JSpinner();
		fromDate.setModel(new PDateModel(fromDate));
		toDate.setModel(new PDateModel(toDate));
		
		Dimension dim = fromDate.getPreferredSize();
		dim.width = 100;
		fromDate.setPreferredSize(dim);
		toDate.setPreferredSize(dim);

		search = new JButton(Dic.w("search"));
		revenue = new JRadioButton(Dic.w("revenue"));
		expense = new JRadioButton(Dic.w("expense"));
		both = new JRadioButton(Dic.w("both"), true);
		ButtonGroup buttons = new ButtonGroup();
		buttons.add(expense);
		buttons.add(revenue);
		buttons.add(both);
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		revenue.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		expense.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		both.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("from_date")));
		form.add(fromDate);
		form.add(new JLabel(Dic.w("to_date")));
		form.add(toDate);
		form.add(revenue);
		form.add(expense);
		form.add(both);
		form.add(new JLabel(Dic.w("page")));
		form.add(pager);
		form.add(search);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col == 1 || col == 2 || col == 6;
				}
				return false;
			}
		};
		String cols [] = {"code", "amount", "date", "account_name", "cash", "type_of_trans", "description"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(6).setPreferredWidth(180);
		
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(1).setCellEditor(new DBEditor(false) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					try {
						int val = Integer.parseInt(value.toString());
						if(val < 1) return false;
						if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) != 0) return false;
						int id = (int) model.getValueAt(row, 0);
						return data.editAccountTrans("ac_trans_value", val, id);
					} 
					catch (NumberFormatException e) {
						return false;
					}
				}
			});
			table.getColumnModel().getColumn(6).setCellEditor(new DBEditor(true) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					return data.editAccountTrans("ac_trans_desc", value.toString().trim(), id);
				}
			});
			table.getColumnModel().getColumn(2).setCellEditor(new DateEditor() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) == 0){
						int id = (int) model.getValueAt(row, 0);
						return data.editAccountTrans("ac_trans_date", date, id);
					}
					return false;
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
		
		payment = new JButton(Dic.w("expense_reg"));
		receipt = new JButton(Dic.w("revenue_reg"));
		delete = new JButton(Dic.w("delete"));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		bleft.add(print);
		bleft.add(payment);
		bleft.add(receipt);
		bright.add(delete);
		bright.add(refresh);
		
		render();
		
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		revenue.addActionListener(render);
		expense.addActionListener(render);
		both.addActionListener(render);
		
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stop();
				render();
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
			receipt.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AccountTransReg(1);
				}
			});
			payment.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AccountTransReg(2);
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
					stop();
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						delete();
						e.consume();
					}
				}
			});
		}
		disableButtons();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(800, 500));
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
			payment.setEnabled(false);
			receipt.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	private void render()
	{
		Date from = (Date) fromDate.getValue();
		Date to = (Date) toDate.getValue();
		
		int type = 0;
		if(revenue.isSelected()) type = 1;
		if(expense.isSelected()) type = 2;
		int page = (int) pager.getValue();
		
		Vector<Object[]> rows = data.accountTransactions(from, to, type, page);
		model.setRowCount(0);
		for(Object[] row : rows){
			model.addRow(row);
		}
	}
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		if(data.deleteAccountTrans(id)){
			render();
		}
	}
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run(){
				String[] cols = {"amount", "date", "account_name", "cash", "type_of_trans", "description"};
				int[] indexes = {1, 2, 3, 4, 5, 6};
				AccountTransaction.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("revenue_and_expense", cols, rows, indexes);
				report.build(path);
				AccountTransaction.this.setCursor(Cursor.getDefaultCursor());
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
