package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;
import com.kabulbits.shoqa.util.SpinnerEditor;

public class BorrowExtension extends JDialog {

	private static final long serialVersionUID = 1;
	private boolean isModified = false;
	
	private DefaultTableModel model;
	private JTable table;
	
	private LibraryData data;
	private int bid;
	
	public BorrowExtension(int id)
	{
		String title = Dic.w("borrow_extension");
		setTitle(title);

		bid = id;
		data = new LibraryData();
		
		JPanel top = new Ribbon(title, false);
		add(top, BorderLayout.NORTH);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				return col == 1;
			}
		};
		model.addColumn("");
		model.addColumn(Dic.w("extension_duration"));
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		
		table.getColumnModel().getColumn(1).setCellEditor(new SpinnerEditor(5, 1, 100, 5));
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(bottom, BorderLayout.SOUTH);
		
		JButton save, delete, create;
		save = new JButton(new ImageIcon("images/save.png"));
		delete = new JButton(new ImageIcon("images/delete.png"));
		create = new JButton(new ImageIcon("images/add.png"));
		
		bottom.add(save);
		bottom.add(create);
		bottom.add(delete);
		
		Helper.esc(this);
		
		render();
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		
		create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				addRow();
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				delete();
			}
		});
		save.addActionListener(new BgWorker<Integer>(this){
			@Override
			protected Integer save() {
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
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getSelectedRow() == table.getRowCount() - 1) {
					stop();
					addRow();
				}
				else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					stop();
					delete();
				}
			}
		});
		setMinimumSize(new Dimension(300, 300));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private void addRow() {
		if (isFill()) {
			model.addRow(new Object [] {null, 5});
		}
	}
	private boolean isFill() {
		if (table.getRowCount() == 0) {
			return true;
		} else if (model.getValueAt(table.getRowCount() - 1, 0) != null) {
			return true;
		}
		return false;
	}
	private int doSave() 
	{
		int count = 0;
		for(int i=0; i<model.getRowCount(); i++)
		{
			Object id = model.getValueAt(i, 0);
			int dur = (int) model.getValueAt(i, 1);
			if(data.saveExtension(id, bid, dur)) count++;
		}
		return count;
	}
	private void render() 
	{
		Vector<Object []> rows = data.borrowExtensions(bid);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
		isModified = false;
	}
	protected void delete() 
	{
		int rows [] = table.getSelectedRows();
		if(rows.length == 0) return;
		
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		for(int row : rows){
			Object val = model.getValueAt(row, 0);
			if(val != null){
				data.deleteExtension((int) val);
			}
		}
		render();
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





























