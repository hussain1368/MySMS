package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class CostsDefinition extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static CostsDefinition self;
	private boolean isModified = false;
	
	private JComboBox<Integer> year, grade;
	private JButton create, save, refresh;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	
	public CostsDefinition() 
	{
		isOpen = true;
		self = this;
		data = new FinanceData();
		
		String title = Dic.w("students_costs_definition");
		setTitle(title);

		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		year = new JComboBox<>();
		grade = new JComboBox<Integer>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
		year.setPrototypeDisplayValue(10000000);
		grade.setPrototypeDisplayValue(1000000);
		
		for(int y = Data.EDUC_YEAR; y >=Data.START_YEAR; y--){
			year.addItem(y);
		}
		
		form.add(new JLabel(Dic.w("educ_year")));
		form.add(year);
		form.add(new JLabel(Dic.w("grade")));
		form.add(grade);
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);

		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return (col != 0);
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int col) {
				if(col == 2){
					return Integer.class;
				}
				return String.class;
			}
		};
		String cols [] = {"code", "cost_name", "monthly_payable"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setCellEditor(Helper.rtlEditor());
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		create = new JButton(Dic.w("new_item"));
		save = new JButton(Dic.w("save"));
		
		bright.add(save);
		bright.add(refresh);
		bleft.add(create);
		
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		year.addActionListener(render);
		grade.addActionListener(render);
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
				}
			});
			create.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0){
					stop();
					addRecord();
				}
			});
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
					stop();
					if (table.getRowCount() != 0 && isModified){
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
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		render();
		
		setSize(550, 400);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			create.setEnabled(false);
			save.setEnabled(false);
			refresh.setEnabled(false);
		}
	}

	private void addRecord() {
		if (isFill()) {
			model.addRow(new Object [] {null, "", null});
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
	
	private int doSave()
	{
		int year = (int) this.year.getSelectedItem();
		int grade = (int) this.grade.getSelectedItem();
		int count = 0;
		for (int i=0; i < table.getRowCount(); i++) 
		{
			if(model.getValueAt(i, 1).toString().equals("")){
				continue;
			}
			String name = model.getValueAt(i, 1).toString();
			Object amount = model.getValueAt(i, 2);
			
			if(model.getValueAt(i, 0) == null)
			{
				if(data.insertCost(name))
				{
					int id = data.insertId();
					if(amount != null){
						data.savePrice(id, year, grade, amount.toString());
					}
					count++;
				}
			}
			else
			{
				int id = Integer.parseInt(model.getValueAt(i, 0).toString());
				if(data.editCost(name, id))
				{
					if(amount != null){
						data.savePrice(id, year, grade, amount.toString());
					}
					count++;
				}
			}

		}
		return count;
	}

	private void render() 
	{
		int year = (int) this.year.getSelectedItem();
		int grade = (int) this.grade.getSelectedItem();
		
		Vector<Object []> rows = data.studentCosts(year, grade);
		model.setRowCount(0);
		for(Object [] row : rows)
		{
			model.addRow(row);
		}
		isModified = false;
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

