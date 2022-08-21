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
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class BookCategories extends JDialog {

	private static final long serialVersionUID = 1L;
	private boolean isModified = false;
	
	private DefaultTableModel model;
	private JTable table;
	
	private LibraryData data;

	public BookCategories()
	{
		data = new LibraryData();
		
		String title = Dic.w("book_categories");
		setTitle(title);
		
		JPanel top = new Ribbon(title, false);
		add(top, BorderLayout.NORTH);
		
		String headers [] = {"", Dic.w("category_name")};
		model = new DefaultTableModel(headers, 0){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				return col == 1;
			}
		};
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setCellEditor(Helper.rtlEditor());
		
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
		
		render();
		
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				if(isModified) save();
			}
		});
		create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				addRecord();
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				delete();
			}
		});
		
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
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		
		setMinimumSize(new Dimension(350, 350));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	protected void delete() 
	{
		if(table.getSelectedRowCount() != 1) return;
		
		int row = table.getSelectedRow();
		Object value = model.getValueAt(row, 0);
		if(value == null) return;
		
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int id = (int) value;
		if(data.deleteCategory(id)){
			render();
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
		} else if (!model.getValueAt(table.getRowCount() - 1, 1).toString().equals("")) {
			return true;
		}
		return false;
	}

	protected void save() 
	{
		int count = 0;
		for(int i=0; i<model.getRowCount(); i++)
		{
			Object id = model.getValueAt(i, 0);
			String name = model.getValueAt(i, 1).toString().trim();
			if(name.length() == 0) continue;
			if(data.saveCategory(id, name)) count++;
		}
		Diags.showMsg(String.format(Dic.w("items_saved"), count));
		render();
	}

	private void render() 
	{
		Vector<Object []> rows = data.bookCategories();
		model.setRowCount(0);
		for(Object row [] : rows){
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



















