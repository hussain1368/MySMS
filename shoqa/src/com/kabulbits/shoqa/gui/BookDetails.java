package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class BookDetails extends JPanel {

	private static final long serialVersionUID = 1L;
	public BookProfile frame;
	
	private JTextField regNo, fields [];
	private JComboBox<Option> category;
	private JSpinner publishDate;
	private JButton newCat, updateCats, updateRegNo, addCover, delCover, save, refresh;
	private DefaultTableModel model;
	private JTable table;
	
	private int bid;
	private LibraryData data;

	public BookDetails(int id, Object [] info){
		this(id);
		fillForm(info);
		if(Data.PERM_LIBRARY != 2){
			disableForm();
		}
	}
	
	public BookDetails(int id)
	{
		bid = id;
		data = new LibraryData();
		
		setLayout(new BorderLayout(0, 5));
		String title = Dic.w(id == 0 ? "book_register" : "book_details");
		
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new BorderLayout(2, 0));
		JPanel sidebar = new JPanel(new BorderLayout(0, 2));
		JPanel form = new JPanel(new GridBagLayout());
		center.add(form, BorderLayout.CENTER);
		center.add(sidebar, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		center.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(3, 3, 3, 3);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.gridy = 0;
				
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("reg_no")), cons);
		
		regNo = new JTextField();
		regNo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		Reset reset = new Reset();
		regNo.addFocusListener(reset);
		
		Dimension dim = new Dimension(24, 22);
		updateRegNo = new JButton(new ImageIcon("images/refreshs.png"));
		updateRegNo.setPreferredSize(dim);
		
		cons.gridwidth = 2;
		cons.gridx = 1;
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(regNo, cons);
		
		cons.gridwidth = 1;
		cons.gridx = 3;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(updateRegNo, cons);
		
		cons.gridy++;
		cons.gridwidth = 1;
		
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(new JLabel(Dic.w("category")), cons);
		
		category = new JComboBox<>(data.categoryOptions());
		category.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)category.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(category, cons);
		
		updateCats = new JButton(new ImageIcon("images/reload.png"));
		newCat = new JButton(new ImageIcon("images/plus.png"));
		updateCats.setPreferredSize(dim);
		newCat.setPreferredSize(dim);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.gridx = 2;
		form.add(updateCats, cons);
		cons.gridx = 3;
		form.add(newCat, cons);
		
		String texts [] = {"title", "author", "translator", "publisher", "publish_period", 
				"publish_date", "publish_place", "price", "isbn", "dioyee", "book_subject"};
		
		fields = new JTextField[texts.length];
		
		for(int i=0; i<fields.length; i++)
		{
			cons.gridy++;
			
			cons.gridwidth = 1;
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			form.add(new JLabel(Dic.w(texts[i])), cons);
			
			if(i == 5) continue;
			
			fields[i] = new JTextField();
			fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			fields[i].addFocusListener(reset);
			
			cons.gridwidth = 3;
			cons.gridx = 1;
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			if(i == fields.length - 1) cons.weighty = 1;
			form.add(fields[i], cons);
		}
		publishDate = new JSpinner();
		publishDate.setModel(new PDateModel(publishDate));
		cons.gridy = 7;
		cons.weighty = 0;
		form.add(publishDate, cons);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_LIBRARY == 2){
					return true;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				return Option.class;
			}
		};
		model.addColumn(Dic.w("cover_no"));
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		if(Data.PERM_LIBRARY == 2){
			JTextField editor = new JTextField();
			editor.setBorder(new LineBorder(Color.BLACK));
			editor.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			editor.addFocusListener(reset);
			
			DefaultCellEditor coverEditor = new DefaultCellEditor(editor){
				private static final long serialVersionUID = 1L;
				private int id = 0;
				@Override
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean isSelected, int row, int column) {
					if(value != null){
						this.id = ((Option)value).key;
					}
					return super.getTableCellEditorComponent(table, value, isSelected, row, column);
				}
				@Override
				public Object getCellEditorValue() {
					Object value = super.getCellEditorValue();
					return new Option(id, value == null ? "" : value.toString());
				}
			};
			coverEditor.setClickCountToStart(1);
			table.getColumnModel().getColumn(0).setCellEditor(coverEditor);
		}
		
		JScrollPane pane = new JScrollPane(table);
		pane.setPreferredSize(new Dimension(120, 0));
		
		JPanel buttons = new JPanel(new GridLayout(1, 2, 2, 0));
		sidebar.add(pane, BorderLayout.CENTER);
		sidebar.add(buttons, BorderLayout.SOUTH);
		
		addCover = new JButton(new ImageIcon("images/add.png"));
		delCover = new JButton(new ImageIcon("images/delete.png"));
		buttons.add(addCover);
		buttons.add(delCover);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		add(bottom, BorderLayout.SOUTH);
		
		save = new JButton(Dic.w("save"));
		bottom.add(save);
		
		if(bid != 0)
		{
			refresh = new JButton(new ImageIcon("images/refresh.png"));
			refresh.setToolTipText(Dic.w("refresh"));
			refresh.setMargin(new Insets(2, 3, 2, 3));
			bottom.add(refresh);
			
			refresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object info [] = data.findBook(bid);
					if(info == null){
						frame.dispose();
						BookProfile.isOpen = false;
						return;
					}
					fillForm(info);
				}
			});
		}
		else{
			regNo.setText(String.valueOf(data.maxBookRegNo()));
		}
		
		if(Data.PERM_LIBRARY == 2){
			updateRegNo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					regNo.setText(String.valueOf(data.maxBookRegNo()));
				}
			});
			newCat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new BookCategories();
				}
			});
			updateCats.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Vector<Option> options = data.categoryOptions();
					category.removeAllItems();
					for(Option option : options){
						category.addItem(option);
					}
				}
			});
			addCover.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					addRow();
				}
			});
			delCover.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					deleteCover();
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
						deleteCover();
					}
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
	}
	
	private void disableForm()
	{
		for(JTextField field : fields){
			if(field != null){
				field.setEditable(false);
			}
		}
		regNo.setEditable(false);
		category.setEnabled(false);
		publishDate.setEnabled(false);
		newCat.setEnabled(false);
		updateCats.setEnabled(false);
		updateRegNo.setEnabled(false);
		addCover.setEnabled(false);
		delCover.setEnabled(false);
		save.setEnabled(false);
		if(refresh != null){
			refresh.setEnabled(false);
		}
	}
	
	protected void addRow() {
		model.addRow(new Object[]{new Option(0, "")});
	}
	
	private void deleteCover()
	{
		int rows [] = table.getSelectedRows();
		if(rows.length == 0) return;
		
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int count = 0;
		for(int i=rows.length-1; i>=0; i--){
			Option option = (Option) model.getValueAt(rows[i], 0);
			if(option.key != 0){
				if(data.deleteCover(option.key)) count++;
			}else{
				count++;
			}
			model.removeRow(rows[i]);
		}
		if(count > rows.length){
			Diags.showErrLang("some_items_cant_be_deleted");
		}
	}
	
	public void fillForm(Object info [])
	{
		int i = 0;
		regNo.setText(info[i++].toString());
		category.setSelectedItem(new Option((int)info[i++]));
		
		for(JTextField field : fields){
			if(field != null){
				field.setText(info[i++].toString());
			}
		}
		publishDate.setValue(info[i++]);
		render();
	}
	
	private void clearForm()
	{
		regNo.setText(String.valueOf(data.maxBookRegNo()));
		category.setSelectedIndex(0);
		publishDate.setValue(new Date());
		
		for(JTextField field : fields){
			if(field != null){
				field.setText("");
			}
		}
		model.setRowCount(0);
	}
	
	private void render()
	{
		stop();
		Vector<Object []> rows = data.bookCovers(bid);
		model.setRowCount(0);
		for(Object [] row : rows){
			model.addRow(row);
		}
	}
	
	private boolean doSave()
	{
		if(!validator()) return false;
		Object values [] = new Object [fields.length + 2];
		
		int i = 0;
		values[i++] = regNo.getText().trim();
		values[i++] = ((Option)category.getSelectedItem()).key;
		
		for(JTextField field : fields){
			if(field != null){
				values[i++] = field.getText().trim();
			}
		}
		values[i++] = publishDate.getValue();
		
		if(bid == 0){
			if(data.insertBook(values))
			{
				int id = data.insertId();
				for(int r=0; r<table.getRowCount(); r++)
				{
					Object value = model.getValueAt(r, 0);
					if(value == null) continue;
					String name = ((Option)value).value;
					if(name.trim().length() == 0) continue;
					data.insertCover(id, name);
				}
				return true;
			}
		}else{
			if(data.editBook(bid, values)){
				for(int r=0; r<table.getRowCount(); r++)
				{
					Option option = (Option) model.getValueAt(r, 0);
					if(option.value.trim().length() == 0) continue;
					if(option.key == 0){
						data.insertCover(bid, option.value);
					}else{
						data.editCover(option.key, option.value);
					}
				}
				return true;
			}
		}
		return false;
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		if(bid == 0){
			clearForm();
		}else{
			render();
		}
	}

	private boolean validator()
	{
		JTextField required [] = {regNo, fields[0], fields[1], fields[7]};
		JTextField numeric [] = {regNo, fields[7]};
		
		for(JTextField field : required){
			if(field.getText().trim().length() == 0){
				field.setBackground(Color.RED);
				Diags.showErrLang("required_error");
				return false;
			}
		}
		for(JTextField field : numeric){
			if(!Helper.isNumeric(field.getText().trim(), false)){
				field.setBackground(Color.RED);
				Diags.showErrLang("numeric_error");
				return false;
			}
		}
		Object value = category.getSelectedItem();
		boolean valid = false;
		if(value != null){
			int cat = ((Option)value).key;
			if(cat != 0) valid = true;
		}
		if(!valid){
			Diags.showErrLang("category_is_required");
			return false;
		}
		return true;
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





























