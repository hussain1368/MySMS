package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
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

public class Cashes extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static Cashes self;
	private boolean isModified = false;
	
	private JTable table;
	private DefaultTableModel model;
	private JButton refresh, create, save;
	
	private FinanceData data;
	
	public Cashes() 
	{
		isOpen = true;
		self = this;
		data = new FinanceData();
		
		String title = Dic.w("cashes_definition");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, true);
		add(ribbon, BorderLayout.NORTH);

		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return col != 0;
				}
				return false;
			}
		};
		model.addColumn(Dic.w("code"));
		model.addColumn(Dic.w("cash_name"));
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setCellEditor(Helper.rtlEditor());
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		add(pane, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		create = new JButton(Dic.w("new_item"));
		save = new JButton(Dic.w("save"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));

		bright.add(save);
		bright.add(refresh);
		bleft.add(create);

		render();
		
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		});
		if(Data.PERM_FINANCE == 2){
			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addRecord();
				}
			});
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save() {
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
			table.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getSelectedRow() == table.getRowCount() - 1) {
						addRecord();
					}
				}
			});
		}
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setMinimumSize(new Dimension(400, 400));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			refresh.setEnabled(false);
			create.setEnabled(false);
			save.setEnabled(false);
		}
	}

	private void addRecord() {
		if (isFill()) {
			model.addRow(new Object [] {null, ""});
		}
	}
	private boolean isFill() {
		if (table.getRowCount() == 0) {
			return true;
		} else if (table.getValueAt(table.getRowCount() - 1, 0) != null) {
			return true;
		}
		return false;
	}

	private int doSave()
	{
		int count = 0;
		for (int i=0; i < table.getRowCount(); i++) 
		{
			if(model.getValueAt(i, 1).equals("")){
				continue;
			}
			Object val = model.getValueAt(i, 0);
			int id = val == null ? 0 : (int) val;
			String name = model.getValueAt(i, 1).toString();
			if(data.saveCash(id, name)) count++;
		}
		return count;
	}

	private void render() 
	{
		Vector<Object[]> rows = data.cashes();
		model.setRowCount(0);
		for(Object[] row : rows){
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
