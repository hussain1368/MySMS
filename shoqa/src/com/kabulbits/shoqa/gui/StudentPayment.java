package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.DBEditor;
import com.kabulbits.shoqa.util.DateEditor;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentPayment extends JDialog
{
	private static final long serialVersionUID = 1L;
	private JComboBox<Option> cash;
	private JTextField value, desc;
	private JSpinner dateNew, dateFrom, dateTo;
	private JButton save, delete, refresh;
	private JTable table;
	
	private DefaultTableModel model;
	private FinanceData data;
	private int sid;
	private int year;
	
	public StudentPayment(int id, int y)
	{
		sid = id;
		year = y;
		
		String title = Dic.w("student_payments");
		setTitle(title);
		
		data = new FinanceData();
		
		setLayout(new BorderLayout());
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		JPanel newPayment = new JPanel(new GridBagLayout());
		JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(newPayment, BorderLayout.CENTER);
		top.add(searchBar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		newPayment.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		Border inside = new TitledBorder(new LineBorder(Color.GRAY, 1, true), Dic.w("new_payment"));
		Border outside = new EmptyBorder(0, 3, 0, 3);
		
		newPayment.setBorder(new CompoundBorder(outside, inside));
		newPayment.setFocusCycleRoot(true);
		
		cash = new JComboBox<>(data.cashOptions(false));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		value = new JTextField();
		desc = new JTextField();
		desc.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		dateNew = new JSpinner();
		dateNew.setModel(new PDateModel(dateNew));
		
		save = new JButton(Dic.w("save"), new ImageIcon("images/save.png"));
		save.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		save.setMargin(new Insets(1, 5, 1, 5));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.gridx = 0;
		newPayment.add(new JLabel(Dic.w("amount")), cons);
		newPayment.add(new JLabel(Dic.w("date")), cons);
		
		cons.gridx = 2;
		newPayment.add(new JLabel(Dic.w("description")), cons);
		newPayment.add(new JLabel(Dic.w("cash")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		newPayment.add(value, cons);
		newPayment.add(dateNew, cons);
		
		cons.weightx = 2;
		cons.gridx = 3;
		cons.gridwidth = 2;
		newPayment.add(desc, cons);
		cons.gridwidth = 1;
		newPayment.add(cash, cons);
		
		cons.gridx = 4;
		cons.gridy = 1;
		cons.weightx = 0;
		newPayment.add(save, cons);
		
		dateFrom = new JSpinner();
		dateTo = new JSpinner();
		dateFrom.setModel(new PDateModel(dateFrom));
		dateTo.setModel(new PDateModel(dateTo));
		
		JTextField theYear = new JTextField(String.valueOf(y), 5);
		theYear.setEditable(false);
		
		JButton search = new JButton(Dic.w("search"));
		
		ULocale locale = new ULocale("@calendar=persian");
		Calendar cal = Calendar.getInstance(locale);
		cal.set(Calendar.YEAR, y);
		
		dateNew.setValue(cal.getTime());
		dateFrom.setValue(cal.getTime());
		dateTo.setValue(cal.getTime());
		
		Dimension dim = new Dimension(90, dateFrom.getPreferredSize().height);
		dateFrom.setPreferredSize(dim);
		dateTo.setPreferredSize(dim);
		
		searchBar.add(new JLabel(Dic.w("from_date")));
		searchBar.add(dateFrom);
		searchBar.add(new JLabel(Dic.w("to_date")));
		searchBar.add(dateTo);
		searchBar.add(new JLabel(Dic.w("educ_year")));
		searchBar.add(theYear);
		searchBar.add(search);
		
		String headers [] = {"code", "amount", "date", "cash", "description"};
		
		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col == 1 || col == 2 || col == 4;
				}
				return false;
			}	
		};
		for(String header : headers){
			model.addColumn(Dic.w(header));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(60);
		table.getColumnModel().getColumn(3).setPreferredWidth(60);
		table.getColumnModel().getColumn(4).setPreferredWidth(180);
		
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(1).setCellEditor(new DBEditor(false) 
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
			table.getColumnModel().getColumn(4).setCellEditor(new DBEditor(true) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					return data.editStudPaymentDesc(id, value.toString());
				}
			});
			table.getColumnModel().getColumn(2).setCellEditor(new DateEditor()
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
		}
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(bottom, BorderLayout.SOUTH);
		
		delete = new JButton(Dic.w("delete"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bottom.add(delete);
		bottom.add(refresh);
		
		if(Data.PERM_FINANCE == 2){
			value.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					value.setBackground(Color.WHITE);
				}
			});
			save.addActionListener(new BgWorker<Boolean>(this) {
				@Override
				protected Boolean save() {
					stop();
					return doSave();
				}
				@Override
				protected void finish(Boolean result) {
					if(result){
						afterSave();
					}
				}
			});
		}
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				delete();
			}
		});
		
		if(Data.USER_ID == 1){
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
		disableForm();
		
		Helper.esc(this);
		
		setBounds(0, 50, 600, 400);
		setMinimumSize(new Dimension(600, 400));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private void disableForm()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			cash.setEnabled(false);
			value.setEnabled(false);
			desc.setEnabled(false);
			dateNew.setEnabled(false);
			save.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
		
	private void render()
	{
		Date start = (Date) dateFrom.getValue();
		Date end = (Date) dateTo.getValue();

		Vector<Object []> rows = data.studentPayments(sid, start, end);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
	}
	
	private boolean doSave()
	{
		try{
			int val = Integer.parseInt(value.getText().trim());
			Date date = (Date) dateNew.getValue();
			if(date == null){
				Diags.showErrLang("pay_date_is_required");
				return false;
			}
			int cashid = ((Option) cash.getSelectedItem()).key;
			String descr = desc.getText().trim();
			return data.saveStudentPayment(sid, val, cashid, year, date, descr);
		} 
		catch (NumberFormatException e) {
			value.setBackground(Color.RED);
			Diags.showErrLang("numeric_error");
			return false;
		}	
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		cash.setSelectedIndex(0);
		value.setText("");
		desc.setText("");
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		if(data.deleteStPayment(id)){
			render();
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
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
