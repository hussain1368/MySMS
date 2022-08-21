package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.xswingx.PromptSupport;

import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Ribbon;

public class BookBorrowReg extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static BookBorrowReg self;
	
	private JSpinner deliveryDate, duration;
	private JTextField wordPerson, wordBook;
	private JComboBox<String> personType;
	private JComboBox<Option> category;
	private DefaultTableModel modelPerson, modelBook;
	private JTable tablePerson, tableBook;
	
	private LibraryData data;
	
	public BookBorrowReg(){
		this(0);
	}
	public BookBorrowReg(int id)
	{
		isOpen = true;
		self = this;
		
		data = new LibraryData();
		
		String title = Dic.w("borrow_reg");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		deliveryDate = new JSpinner();
		duration = new JSpinner();
		deliveryDate.setModel(new PDateModel(deliveryDate));
		duration.setModel(new SpinnerNumberModel(5, 1, 100, 5));
		Dimension dim = new Dimension(90, duration.getPreferredSize().height);
		deliveryDate.setPreferredSize(dim);
//		duration.setPreferredSize(dim);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("delivery_date")));
		form.add(deliveryDate);
		form.add(new JLabel(Dic.w("duration")));
		form.add(duration);
		
		final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JPanel left = new JPanel(new BorderLayout());
		JPanel right = new JPanel(new BorderLayout());
		split.setLeftComponent(left);
		split.setRightComponent(right);
		split.setDividerLocation(420);
		add(split, BorderLayout.CENTER);
		
		JPanel rightForm = new JPanel(new GridBagLayout());
		rightForm.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		right.add(rightForm, BorderLayout.NORTH);
		
		wordPerson = new JTextField();
		wordPerson.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		PromptSupport.setPrompt(Dic.w("type_person_code_or_name"), wordPerson);
		
		personType = new JComboBox<>();
		personType.addItem(Dic.w("student"));
		personType.addItem(Dic.w("employee"));
		personType.addItem(Dic.w("external_member"));
		personType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)personType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		personType.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(5, 5, 5, 5);
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		rightForm.add(wordPerson, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		rightForm.add(personType, cons);
		
		JPanel leftForm = new JPanel(new GridBagLayout());
		leftForm.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		left.add(leftForm, BorderLayout.NORTH);
		
		wordBook = new JTextField();
		wordBook.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		PromptSupport.setPrompt(Dic.w("type_book_reg_no_or_title"), wordBook);
		
		category = new JComboBox<>(data.categoryOptions());
		category.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)category.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		leftForm.add(wordBook, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		leftForm.add(category, cons);
		
		String cols1 [] = {"code", "name", "lname", "fname"};
		modelPerson = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {return false;}
		};
		for(String col : cols1){
			modelPerson.addColumn(Dic.w(col));
		}
		tablePerson = new JTable(modelPerson);
		Helper.tableMakUp(tablePerson);
		tablePerson.getColumnModel().getColumn(0).setMaxWidth(60);
		
		JScrollPane rightPane = new JScrollPane(tablePerson);
		rightPane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), rightPane.getBorder()));
		right.add(rightPane, BorderLayout.CENTER);
		
		String cols2 [] = {"cover_code", "cover_no", "reg_no", "title"};
		modelBook = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {return false;}
		};
		for(String col : cols2){
			modelBook.addColumn(Dic.w(col));
		}
		tableBook = new JTable(modelBook);
		Helper.tableMakUp(tableBook);
		tableBook.getColumnModel().getColumn(0).setPreferredWidth(60);
		tableBook.getColumnModel().getColumn(2).setPreferredWidth(60);
		tableBook.getColumnModel().getColumn(3).setPreferredWidth(150);
		
		JScrollPane leftPane = new JScrollPane(tableBook);
		leftPane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), leftPane.getBorder()));
		left.add(leftPane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(bottom, BorderLayout.SOUTH);
		
		JButton save = new JButton(Dic.w("save"));
		bottom.add(save);
		
		wordPerson.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				renderPersons();
			}
		});
		personType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renderPersons();
			}
		});
		wordBook.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				renderBooks();
			}
		});
		category.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				renderBooks();
			}
		});
		save.addActionListener(new BgWorker<Boolean>(this){
			@Override
			protected Boolean save(){
				return doSave();
			}
			@Override
			protected void finish(Boolean result){
				if(result){
					Diags.showMsg(Diags.SUCCESS);
					renderBooks();
				}
			}
		});
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(800, 400));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	protected void renderBooks() 
	{
		String term = wordBook.getText().trim();
		int cat = ((Option)category.getSelectedItem()).key;
		
		Vector<Object[]> rows = data.bookList(cat, term);
		modelBook.setRowCount(0);
		for(Object row [] : rows){
			modelBook.addRow(row);
		}
	}
	
	protected void renderPersons() 
	{
		String term = wordPerson.getText().trim();
		int type = personType.getSelectedIndex();
		
		Vector<Object []> rows = null;
		switch(type){
		case 0:
			rows = data.studentList(term);
			break;
		case 1:
			rows = data.employeeList(term);
			break;
		case 2:
			rows = data.memberList(term);
			break;
		}
		modelPerson.setRowCount(0);
		if(rows != null){
			for(Object [] row : rows){
				modelPerson.addRow(row);
			}
		}
	}
	private boolean doSave()
	{
		int rows1 = tablePerson.getSelectedRowCount();
		int rows2 = tableBook.getSelectedRowCount();
		if(rows1 != 1 || rows2 != 1){
			Diags.showErrLang("select_exactly_one_row_error");
			return false;
		}
		Date date = (Date) deliveryDate.getValue();
		if(date == null){
			Diags.showErrLang("delivery_date_is_required");
			return false;
		}
		int dur = (int) duration.getValue();
		
		int type = personType.getSelectedIndex();
		int rowp = tablePerson.getSelectedRow();
		int rowb = tableBook.getSelectedRow();
		
		int pid = (int) modelPerson.getValueAt(rowp, 0);
		int bid = (int) modelBook.getValueAt(rowb, 0);
		
		if(data.borrowExists(pid, type)){
			if(Diags.showConfLang("person_already_have_borrow", Diags.YN) != 0){
				return false;
			}
		}
		return data.insertBorrow(bid, pid, type, date, dur);
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

























