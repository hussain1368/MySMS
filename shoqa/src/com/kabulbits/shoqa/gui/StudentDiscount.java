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
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentDiscount extends JDialog 
{

	private static final long serialVersionUID = 1L;
	private boolean isModified = false;
	
	private JComboBox<Integer> year;
	private JTextField totalDiscount;
	private JButton save, create, refresh, delete;
	private JTable table;
	private DefaultTableModel model;
	
	private FinanceData data;
	private int sid;
	
	public StudentDiscount(int id, int Year)
	{
		sid = id;
		data = new FinanceData();
		
		String title = Dic.w("student_discount_and_subtract");
		JPanel ribbon = new Ribbon(title, false);
		JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(options, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		options.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		year = new JComboBox<Integer>();
		for(int y=Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		year.setPrototypeDisplayValue(10000000);
		year.setSelectedItem(Year);
		
		totalDiscount = new JTextField(10);
		totalDiscount.setEditable(false);
		totalDiscount.setBackground(Color.WHITE);
		
		options.add(new JLabel(Dic.w("year")));
		options.add(year);
		options.add(new JLabel(Dic.w("total")));
		options.add(totalDiscount);
		
		String headers [] = {
				Dic.w("code"),
				Dic.w("discount_amount"),
				Dic.w("month"),
				Dic.w("description"),
		};
		
		model = new DefaultTableModel(headers, 0){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col != 0;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 1){
					return Integer.class;
				}else if(index == 2){
					return Option.class;
				}
				return Object.class;
			}
		};
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(3).setPreferredWidth(220);
		
		table.getColumnModel().getColumn(3).setCellEditor(Helper.rtlEditor());
		
		String months [] = {"hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot"};
		
		JComboBox<Option> editor = new JComboBox<Option>();
		editor.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) editor.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		int m = 1;
		for(String month : months){
			editor.addItem(new Option(m++, Dic.w(month)));
		}
		table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(editor));
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 4, 0, 4), pane.getBorder()));
		
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		delete = new JButton(Dic.w("delete"));
		save = new JButton(Dic.w("save"));
		create = new JButton(Dic.w("new_item"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(save);
		bright.add(refresh);
		bleft.add(create);
		bleft.add(delete);
		
		Helper.esc(this);
		
		render();
		
		// Listeners
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		year.addActionListener(render);
		refresh.addActionListener(render);
		
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		
		if(Data.PERM_FINANCE == 2){
			table.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getSelectedRow() == table.getRowCount() - 1) {
						stop();
						addRecord();
					}
					else if(e.getKeyCode() == KeyEvent.VK_DELETE){
						stop();
						delete();
					}
				}
			});
			create.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					addRecord();
				}
			});
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
				protected void finish(Integer result){
					if(result > 0){
						String msg = String.format(Dic.w("items_saved"), result);
						Diags.showMsg(msg);
						render();
					}
				}
			});
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					delete();
				}
			});
		}
		disableButtons();
		
		setBounds(0, 50, 550, 300);
		setMinimumSize(new Dimension(550, 300));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			save.setEnabled(false);
			create.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Object value = model.getValueAt(row, 0);
		if(value == null) return;
		
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int id = Integer.parseInt(value.toString());
		if(data.deleteDiscount(id)){
			render();
		}
	}

	private int doSave() 
	{
		int y = (int) year.getSelectedItem();
		int count = 0;
		for(int i=0; i<model.getRowCount(); i++)
		{
			Object theAmount = model.getValueAt(i, 1);
			Object theMonth = model.getValueAt(i, 2);
			
			if(theAmount == null || theMonth == null){
				Diags.showErrLang("amount_and_month_is_required");
				continue;
			}
			
			int amount = Integer.parseInt(theAmount.toString());
			int m = ((Option) theMonth).key;
			String desc = model.getValueAt(i, 3).toString();
			Object val = model.getValueAt(i, 0);
			
			if(val == null){
				if(data.insertDiscount(sid, y, m, amount, desc)) count++;
			}else{
				int id = Integer.parseInt(val.toString());
				if(data.editDiscount(id, amount, m, desc)) count++;
			}
		}
		return count;
	}

	protected void render() 
	{
		int y = (int) year.getSelectedItem();
		
		Vector<Object []> rows = data.studentDiscounts(sid, y);
		model.setRowCount(0);
		
		int total = 0;
		for(Object [] row : rows)
		{
			model.addRow(row);
			total += Integer.parseInt(row[1].toString());
		}
		totalDiscount.setText(String.valueOf(total));
		isModified = false;
	}

	private void addRecord() {
		if (isFill()) {
			model.addRow(new Object [] {null, "", null, ""});
		}
	}

	private boolean isFill() {
		if (table.getRowCount() == 0) {
			return true;
		} else if (!model.getValueAt(table.getRowCount() - 1, 1).toString().equals("")) {
			return true;
		}
		return false;
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
