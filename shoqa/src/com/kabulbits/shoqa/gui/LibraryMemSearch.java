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
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class LibraryMemSearch extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static LibraryMemSearch self;
	
	private JTextField word;
	private JLabel numbers;
	private JSpinner pager;
	private JButton refresh, edit, delete, create, print;
	private JTable table;
	private DefaultTableModel model;
	
	private LibraryData data;
	private int page = 1;
	private int pages;
	
	public LibraryMemSearch()
	{
		isOpen = true;
		self = this;
		
		String title = Dic.w("external_member_search");
		setTitle(title);
		
		data = new LibraryData();
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new GridBagLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
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
		
		form.add(new JLabel(Dic.w("search_by_code_name")), cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(pager, cons);
		form.add(numbers, cons);
		
		String headers [] = {"code", "name", "lname", "fname", "phone", "job"};
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {return false;}
		};
		for(String col : headers){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		
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
			public void actionPerformed(ActionEvent e){
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
			public void actionPerformed(ActionEvent e){
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
			create.addActionListener(new OpenFrame(LibraryMemProfile.class, this));
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
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
		setMinimumSize(new Dimension(800, 500));
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

	protected void render() {
		String term = word.getText().trim();
		
		int limit = Data.LIMIT;
		int total = data.countMembers(term);
		pages = (int) Math.ceil((double)total/limit);
		
		model.setRowCount(0);
		Vector<Object []> rows = data.searchMembers(term, page);
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
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		
		if(data.deleteMember(id)){
			render();
		}
	}
	
	protected void edit() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		OpenFrame.openFrame(LibraryMemProfile.class, new Class<?>[]{int.class}, new Object []{id}, this);
	}

	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cols = {"name", "lname", "fname", "phone", "job"};
				int[] indexes = {1, 2, 3, 4, 5};
				LibraryMemSearch.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("library_members", cols, rows, indexes);
				report.build(path);
				LibraryMemSearch.this.setCursor(Cursor.getDefaultCursor());
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






















