package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.DateEditor;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class BorrowSearch extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static BorrowSearch self;
	
	private JSpinner deliverDateFrom, deliverDateTo, returnDateFrom, returnDateTo, deadlineDateFrom, deadlineDateTo;
	private JComboBox<String> personType, status;
	private JSpinner pager;
	private JButton refresh, book, person, extension, delete, print;
	private DefaultTableModel model;
	private JTable table;
	
	private LibraryData data;
	
	public BorrowSearch()
	{
		isOpen = true;
		self = this;
		
		data = new LibraryData();
		
		String title = Dic.w("search_borrowed_books");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.CENTER);
		top.add(bar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		deliverDateFrom = new JSpinner();
		deliverDateTo = new JSpinner();
		returnDateFrom = new JSpinner();
		returnDateTo = new JSpinner();
		deadlineDateFrom = new JSpinner();
		deadlineDateTo = new JSpinner();
		JSpinner spinners [] = {deliverDateFrom, deliverDateTo, returnDateFrom, returnDateTo, deadlineDateFrom, deadlineDateTo};
		for(JSpinner spinner : spinners){
			spinner.setModel(new PDateModel(spinner));
			Dimension dim = new Dimension(100, spinner.getPreferredSize().height);
			spinner.setPreferredSize(dim);
			spinner.setValue(null);
		}
		deliverDateFrom.setValue(new Date());
		deliverDateTo.setValue(new Date());
		
		personType = new JComboBox<>();
		personType.addItem("---");
		personType.addItem(Dic.w("student"));
		personType.addItem(Dic.w("employee"));
		personType.addItem(Dic.w("external_member"));
		personType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)personType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		personType.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		status = new JComboBox<>();
		status.addItem("---");
		status.addItem(Dic.w("unreturned"));
		status.addItem(Dic.w("returned"));
		status.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)status.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		status.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		JButton search = new JButton(Dic.w("search"));
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		JPanel form1 = new JPanel(new GridBagLayout());
		JPanel form2 = new JPanel(new GridBagLayout());
		JPanel form3 = new JPanel(new GridBagLayout());
		form1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form3.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form1.setBorder(new TitledBorder(Dic.w("delivery_date")));
		form2.setBorder(new TitledBorder(Dic.w("deadline_date")));
		form3.setBorder(new TitledBorder(Dic.w("return_date")));
		form.add(form1);
		form.add(form2);
		form.add(form3);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(2, 2, 2, 2);
		cons.gridx = 0;
		cons.gridy = 0;
		form1.add(new JLabel(Dic.w("from_date")), cons);
		cons.gridy = 1;
		form1.add(new JLabel(Dic.w("to_date")), cons);
		
		cons.gridx = 1;
		cons.gridy = 0;
		form1.add(deliverDateFrom, cons);
		cons.gridy = 1;
		form1.add(deliverDateTo, cons);
		
		cons.gridx = 0;
		cons.gridy = 0;
		form2.add(new JLabel(Dic.w("from_date")), cons);
		cons.gridy = 1;
		form2.add(new JLabel(Dic.w("to_date")), cons);
		
		cons.gridx = 1;
		cons.gridy = 0;
		form2.add(deadlineDateFrom, cons);
		cons.gridy = 1;
		form2.add(deadlineDateTo, cons);
		
		cons.gridx = 0;
		cons.gridy = 0;
		form3.add(new JLabel(Dic.w("from_date")), cons);
		cons.gridy = 1;
		form3.add(new JLabel(Dic.w("to_date")), cons);
		
		cons.gridx = 1;
		cons.gridy = 0;
		form3.add(returnDateFrom, cons);
		cons.gridy = 1;
		form3.add(returnDateTo, cons);
		
		bar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bar.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		bar.add(new JLabel(Dic.w("person_type")));
		bar.add(personType);
		bar.add(new JLabel(Dic.w("status")));
		bar.add(status);
		bar.add(new JLabel(Dic.w("page")));
		bar.add(pager);
		bar.add(search);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_LIBRARY == 2){
					return col == 8 || col == 11 || col == 12;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 12){
					return Boolean.class;
				}
				return Object.class;
			}
		};
		String cols [] = {"code", "book_code", "person_code", "name", "lname", "book_title", "reg_no", "cover_no", "delivery_date", "duration", "deadline", "return_date", "returned"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setMaxWidth(60);
		
		if(Data.PERM_LIBRARY == 2){
			table.getColumnModel().getColumn(8).setCellEditor(new DateEditor() {
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
			table.getColumnModel().getColumn(11).setCellEditor(new DateEditor() {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(Date date, int row) {
					if(Diags.showConf(Diags.SAVE_CONF, Diags.YN) == 0){
						int id = (int) model.getValueAt(row, 0);
						int cover = ((Option) model.getValueAt(row, 7)).key;
						return data.borrowReturn(id, cover, true, date);
					}
					return false;
				}
			});
			JCheckBox box = new JCheckBox();
			box.setHorizontalAlignment(JCheckBox.CENTER);
			table.getColumnModel().getColumn(12).setCellEditor(new DefaultCellEditor(box){
				private static final long serialVersionUID = 1L;
				@Override
				public Object getCellEditorValue() {
					boolean value = (boolean) super.getCellEditorValue();
					int row = table.getSelectedRow();
					int id = (int) model.getValueAt(row, 0);
					int cover = ((Option) model.getValueAt(row, 7)).key;
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
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		book = new JButton(Dic.w("book_details"));
		person = new JButton(Dic.w("person_details"));
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
		bleft.add(print);
		bleft.add(book);
		bleft.add(person);
		bleft.add(extension);
		
		deliverDateFrom.setValue(null);
		render();
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		search.addActionListener(render);
		refresh.addActionListener(render);
		personType.addActionListener(render);
		status.addActionListener(render);
		
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stop();
				render();
			}
		});
		person.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				person();
			}
		});
		book.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				book();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				print();
			}
		});
		if(Data.PERM_LIBRARY == 2){
			extension.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					if(table.getSelectedRowCount() != 1) return;
					int row = table.getSelectedRow();
					int id = (int) model.getValueAt(row, 0);
					new BorrowExtension(id);
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
			table.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						delete();
					}
				}
			});
		}
		addWindowListener(new WindowAdapter() {
			@Override
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
		if(Data.PERM_LIBRARY != 2){
			refresh.setEnabled(false);
			extension.setEnabled(false);
		}
	}

	private void render() 
	{
		int ptype = personType.getSelectedIndex();
		int state = status.getSelectedIndex() - 1;
		int page = (int) pager.getValue();
		
		Date deliverFrom = (Date) deliverDateFrom.getValue();
		Date deliverTo = (Date) deliverDateTo.getValue();
		Date deadFrom = (Date) deadlineDateFrom.getValue();
		Date deadTo = (Date) deadlineDateTo.getValue();
		Date returnFrom = (Date) returnDateFrom.getValue();
		Date returnTo = (Date) returnDateTo.getValue();
		
		Vector<Object[]> rows = data.searchBorrow(ptype, state, page, deliverFrom, deliverTo, deadFrom, deadTo, returnFrom, returnTo);
		model.setRowCount(0);
		for(Object row[] : rows){
			model.addRow(row);
		}
	}
	
	private void person()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Option option = (Option) model.getValueAt(row, 3);
		Class<?> cls = option.type == 1 ? StudentProfile.class : option.type == 2 ? EmployeeProfile.class : LibraryMemProfile.class;
		
		JFrame frame = OpenFrame.openFrame(cls, new Class<?>[]{int.class}, new Object[]{option.key}, this);
		switch(option.type){
		case 1:
			((StudentProfile)frame).activeTab(6);
			break;
		case 2:
			((EmployeeProfile)frame).activeTab(3);
			break;
		case 3:
			((LibraryMemProfile)frame).activeTab(1);
			break;
		}
	}
	
	private void book()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 1);
		BookProfile frame = (BookProfile)
				OpenFrame.openFrame(BookProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(1);
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		int cover = 0;
		if(!(boolean) model.getValueAt(row, 12)){
			cover = ((Option) model.getValueAt(row, 7)).key;
		}
		if(data.deleteBorrow(id, cover)){
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
				String[] cols = {"name", "lname", "book_title", "reg_no", "cover_no", "delivery_date", "duration", "deadline", "return_date"};
				int[] indexes = {3, 4, 5, 6, 7, 8, 9, 10, 11};
				BorrowSearch.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("borrows_list", cols, rows, indexes);
				report.build(path);
				BorrowSearch.this.setCursor(Cursor.getDefaultCursor());
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



























