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
import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.sheet.EmployeeList;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.PrintRangeChooser;
import com.kabulbits.shoqa.util.Ribbon;

public class EmployeeSearch extends JFrame {
	
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static EmployeeSearch self;
	
	private JComboBox<String> columns;
	private JTextField word;
	private JComboBox<String> empType;
	private JSpinner pager;
	private JLabel numbers;
	private JButton create, edit, delete, refresh, print;
	private DefaultTableModel model;
	private JTable table;
	
	private EmployeeData data;
	private String theCols[];
	private int page = 1;
	private int pages;
	
	public EmployeeSearch() {
		
		isOpen = true;
		self = this;
		data = new EmployeeData();
		
		String title = Dic.w("search_employees");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new GridBagLayout());
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);

		String attribs [] = { "emp_id", "emp_name", "emp_lname", "emp_fname", "emp_idcard"};
		String labels [] = {"code", "name", "lname", "fname", "idcard_no"};
		this.theCols = attribs;

		columns = new JComboBox<>();
		columns.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)columns.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		for(String item : labels){
			columns.addItem(Dic.w(item));
		}
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		String types [] = {
				Dic.w("all"),
				Dic.w("instructive"),
				Dic.w("administrative"),
				Dic.w("services"),
		};
		empType = new JComboBox<String>(types);
		empType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)empType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		pager = new JSpinner();
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		numbers = new JLabel();
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		
		form.add(new JLabel(Dic.w("search_by")), cons);
		form.add(columns, cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(empType, cons);
		form.add(pager, cons);
		form.add(numbers, cons);

		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String headers [] = {"code", "name", "lname", "fname", "idcard_no", "phone"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);

		table.getColumnModel().getColumn(0).setMaxWidth(60);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		create = new JButton(Dic.w("new_item"));
		edit = new JButton(Dic.w("edit"));
		delete = new JButton(Dic.w("delete"));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		bright.add(create);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(edit);
		bleft.add(delete);

		render();

		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				page = 1;
				render();
			}
		};
		columns.addActionListener(render);
		empType.addActionListener(render);
		
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		pager.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				page = (int) pager.getValue();
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				page = 1;
				render();
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) 
			{
				switch(e.getKeyCode()){
				case KeyEvent.VK_ENTER:
					profile();
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
					profile();
				}
			}
		});
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				profile();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		});
		if(Data.PERM_EMPLOYEES == 2){
			create.addActionListener(new OpenFrame(EmployeeProfile.class, this));
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setMinimumSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_EMPLOYEES != 2){
			create.setEnabled(false);
			refresh.setEnabled(false);
		}
	}

	public void render() 
	{
		String col = theCols[columns.getSelectedIndex()];
		String exp = word.getText().trim();
		int type = empType.getSelectedIndex();
		
		int total = data.countEmployee(col, exp, type);
		int limit = Data.LIMIT;
		int count = (int) Math.ceil((double) total/limit);
		pages = count > 0 ? count : 1;
		pager.setModel(new SpinnerNumberModel(page, 1, pages > 0 ? pages : 1, 1));
		
		model.setRowCount(0);
		data.searchEmployees(model, col, exp, type, page);
		
		String text = "%d - %d / %d";
		int last = page < pages ? page*limit : total;
		text = String.format(text, page*limit - limit + 1, last, total);
		numbers.setText(text);
	}
	
	private void profile()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		OpenFrame.openFrame(EmployeeProfile.class, new Class<?> [] {int.class}, new Object [] {id});
		setCursor(Cursor.getDefaultCursor());
	}
	
	private void print()
	{
		String[] opts = {"advanced_employee_sheet", "simple_list"};
		new PrintRangeChooser("print_options", "employee_code", opts)
		{
			private static final long serialVersionUID = 1L;
			protected void print(final int task, final int from, final int to)
			{
				final String path = Helper.xlsx("list", EmployeeSearch.this);
				if(path == null) return;
				new Thread(new Runnable() {
					@Override
					public void run() {
						EmployeeSearch.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						EmployeeList list = new EmployeeList();
						if(task == 0){
							list.advancedSheet(from, to);
						}else{
							String[] cols = {"code", "name", "lname", "fname", "idcard_no", "phone"};
							int[] indexes = {0, 1, 2, 3, 4, 5};
							Vector<?> data = model.getDataVector();
							list.simpleSheet("employees_list", cols, data, indexes);
						}
						list.build(path);
						EmployeeSearch.this.setCursor(Cursor.getDefaultCursor());
					}
				}).start();
			}
		};
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		if(data.deleteEmployee(id)){
			render();
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
