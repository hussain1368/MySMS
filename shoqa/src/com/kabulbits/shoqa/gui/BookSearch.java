package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class BookSearch extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static BookSearch self;
	
	private JComboBox<String> columns;
	private JTextField word;
	private JComboBox<Option> category;
	private JSpinner pager;
	private JLabel numbers;
	private JButton refresh, create, edit, delete, print;
	private DefaultTableModel model;
	private JTable table;
	
	private int pages;
	private int page = 1;
	private LibraryData data;
	
	public BookSearch()
	{
		isOpen = true;
		self = this;
		data = new LibraryData();
		
		String title = Dic.w("search_books");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new GridBagLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		String cols [] = {"reg_no", "title", "author", "translator", "publisher", "isbn"};
		columns = new JComboBox<>();
		for(String item : cols){
			columns.addItem(Dic.w(item));
		}
		columns.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)columns.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		columns.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		category = new JComboBox<>(data.categoryOptions());
		category.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)category.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		pager = new JSpinner();
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		numbers = new JLabel();
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(new JLabel(Dic.w("search_by")), cons);
		form.add(columns);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(new JLabel(Dic.w("category")), cons);
		form.add(category, cons);
		form.add(pager, cons);
		form.add(numbers, cons);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;};
		};
		model.addColumn(Dic.w("code"));
		for(String item : cols){
			model.addColumn(Dic.w(item));
		}
		model.addColumn(Dic.w("cover_count"));
		model.addColumn(Dic.w("available"));
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(120);
		table.getColumnModel().getColumn(5).setPreferredWidth(150);
		table.getColumnModel().getColumn(7).setPreferredWidth(60);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		edit = new JButton(Dic.w("edit"));
		delete = new JButton(Dic.w("delete"));
		create = new JButton(Dic.w("new_item"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(edit);
		bleft.add(create);
		
		render();
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		category.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				page = 1;
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				page = 1;
				render();
			}
		});
		pager.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				page = (int) pager.getValue();
				render();
			}
		});
		edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edit();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())
				{
				case KeyEvent.VK_ENTER:
					edit();
					e.consume();
					break;
				case KeyEvent.VK_DELETE:
					delete();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if(page < pages) page++;
					render();
					break;
				case KeyEvent.VK_PAGE_UP:
					if(page > 1) page--;
					render();
					break;
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					edit();
				}
			}
		});
		if(Data.PERM_LIBRARY == 2){
			create.addActionListener(new OpenFrame(BookProfile.class, this));
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
		}
		disableButtons();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		setMinimumSize(new Dimension(850, 500));
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
			create.setEnabled(false);
			refresh.setEnabled(false);
		}
	}

	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		if(data.deleteBook(id)){
			render();
		}
	}

	protected void edit() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		OpenFrame.openFrame(BookProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
	}

	private void render() 
	{
		String cols [] = {"book_reg_no", "book_title", "book_author", "book_translator", "book_publisher", "book_isbn"};
		String col = cols[columns.getSelectedIndex()];
		int cat = ((Option)category.getSelectedItem()).key;
		
		int limit = Data.LIMIT;
		int total = data.countBook(word.getText().trim(), col, cat);
		int count = (int) Math.ceil((double)total/limit);
		pages = count > 0 ? count : 1;
		
		model.setRowCount(0);
		Vector<Object []> rows = data.searchBooks(word.getText().trim(), col, cat, page);
		for(Object [] row : rows){
			model.addRow(row);
		}
		
		int val = page > pages ? pages : page;
		int max = pages > 0 ? pages : 1;
		pager.setModel(new SpinnerNumberModel(val, 1, max, 1));
		
		String text = "%d - %d / %d";
		int last = page < pages ? page*limit : total;
		text = String.format(text, page*limit - limit + 1, last, total);
		numbers.setText(text);
	}
	
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cols = {"reg_no", "title", "author", "translator", "publisher", "isbn", "cover_count", "available"};
				int[] indexes = {1, 2, 3, 4, 5, 6, 7, 8};
				BookSearch.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("book_list", cols, rows, indexes);
				report.build(path);
				BookSearch.this.setCursor(Cursor.getDefaultCursor());
			}
		}).start();
	}
	
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}





















