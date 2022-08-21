package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.DateEditor;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class BookBorrows extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JSpinner fromDate, toDate;
	private JCheckBox unReturned;
	private JButton refresh, extension, delete;
	private DefaultTableModel model;
	private JTable table;
	
	private LibraryData data;
	private int bid;
	
	public BookBorrows(int id)
	{
		bid = id;
		data = new LibraryData();
		
		setLayout(new BorderLayout());
		String title = Dic.w("book_borrows_list");
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		fromDate = new JSpinner();
		toDate = new JSpinner();
		fromDate.setModel(new PDateModel(fromDate));
		toDate.setModel(new PDateModel(toDate));
		
		Dimension dim = new Dimension(100, toDate.getPreferredSize().height);
		fromDate.setPreferredSize(dim);
		toDate.setPreferredSize(dim);
		
		unReturned = new JCheckBox(Dic.w("unreturned"));
		unReturned.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		JButton search = new JButton(Dic.w("search"));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("delivery_date")));
		form.add(fromDate);
		form.add(new JLabel(Dic.w("to")));
		form.add(toDate);
		form.add(unReturned);
		form.add(search);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_LIBRARY == 2){
					return col == 5 || col == 8 || col == 9;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 9){
					return Boolean.class;
				}
				return Object.class;
			}
		};
		String cols [] = {"code", "person_code", "name", "lname", "cover_no", "delivery_date", "duration", "deadline", "return_date", "returned"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		table.getColumnModel().getColumn(7).setPreferredWidth(100);
		table.getColumnModel().getColumn(8).setPreferredWidth(100);
		
		if(Data.PERM_LIBRARY == 2){
			table.getColumnModel().getColumn(5).setCellEditor(new DateEditor() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) == 0){
						int id = (int) model.getValueAt(row, 0);
						return data.editBorrowDate(id, date);
					}
					return false;
				}
			});
			table.getColumnModel().getColumn(8).setCellEditor(new DateEditor() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) == 0)
					{
						int id = (int) model.getValueAt(row, 0);
						int cover = ((Option) model.getValueAt(row, 4)).key;
						return data.borrowReturn(id, cover, true, date);
					}
					return false;
				}
			});
			JCheckBox box = new JCheckBox();
			box.setHorizontalAlignment(JCheckBox.CENTER);
			table.getColumnModel().getColumn(9).setCellEditor(new DefaultCellEditor(box)
			{
				private static final long serialVersionUID = 1L;
				@Override
				public Object getCellEditorValue() {
					boolean value = (boolean) super.getCellEditorValue();
					int row = table.getSelectedRow();
					int id = (int) model.getValueAt(row, 0);
					int cover = ((Option) model.getValueAt(row, 4)).key;
					if(value){
						return new BorrowReturn(id, cover).result;
					}else{
						if(Diags.showConfLang("change_borrow_status_conf", Diags.YN) == 0){
							return !data.borrowReturn(id, cover, false, null);
						}
						return !value;
					}
				}
			});
		}
		JScrollPane pane = new JScrollPane(table);
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		extension = new JButton(Dic.w("extension"));
		delete = new JButton(Dic.w("delete"));
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(extension);
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		unReturned.addActionListener(render);
		
		if(Data.PERM_LIBRARY == 2){
			extension.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					extension();
				}
			});
			table.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						delete();
					}
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						extension();
						e.consume();
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
		fromDate.setValue(null);
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_LIBRARY != 2){
			refresh.setEnabled(false);
			extension.setEnabled(false);
		}
	}

	public void render() 
	{
		Date from = (Date) fromDate.getValue();
		Date to = (Date) toDate.getValue();
		
		Vector<Object[]> rows = data.bookBorrows(bid, unReturned.isSelected(), from, to);
		model.setRowCount(0);
		for(Object row[] : rows){
			model.addRow(row);
		}
	}
	
	protected void extension() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		new BorrowExtension(id);
	}

	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		int cover = 0;
		if(!(boolean) model.getValueAt(row, 9)){
			cover = ((Option) model.getValueAt(row, 4)).key;
		}
		if(data.deleteBorrow(id, cover)){
			render();
		}
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































