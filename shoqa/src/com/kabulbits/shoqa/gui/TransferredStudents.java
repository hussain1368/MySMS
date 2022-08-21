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
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.sheet.Report;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class TransferredStudents extends JFrame implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static TransferredStudents self;
	
	private JButton delete, refresh, edit, profile, print;
	private JComboBox<Integer> year;
	private JComboBox<Object> grade, transType;
	private JSpinner curPage;
	private JTextField word;
	private JLabel numbers;
	private JTable table;
	private DefaultTableModel model;
	
	private StudentData data = new StudentData();
	private int page = 1;
	private int pages;
	
	public TransferredStudents() 
	{
		isOpen = true;
		self = this;

		String title = Dic.w("students_transfers");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel searchBar = new JPanel(new GridBagLayout());
		searchBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		top.add(ribbon, BorderLayout.NORTH);
		top.add(searchBar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		curPage = new JSpinner();
		curPage.setPreferredSize(new Dimension(50, curPage.getPreferredSize().height));
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		numbers = new JLabel();
		
		year = new JComboBox<Integer>();
		year.setPrototypeDisplayValue(100000);
		year.setMaximumRowCount(10);
		int y = Data.EDUC_YEAR;
		for(; y>1370; y--){
			year.addItem(y);
		}
		grade = new JComboBox<Object>();
		grade.setMaximumRowCount(13);
		grade.setPrototypeDisplayValue("xxxxxxxxx");
		grade.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)grade.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		grade.addItem(Dic.w("all_grades"));
		for(int i=1; i<13; i++){
			grade.addItem(i);
		}
		
		transType = new JComboBox<Object>();
		transType.setPrototypeDisplayValue("xxxxxxx");
		transType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)transType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		transType.addItem(Dic.w("both"));
		transType.addItem(Dic.w("entrance"));
		transType.addItem(Dic.w("detach"));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		searchBar.add(new JLabel(Dic.w("code_name_school")), cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		
		searchBar.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		searchBar.add(transType, cons);
		searchBar.add(year, cons);
		searchBar.add(grade, cons);
		searchBar.add(curPage, cons);
		searchBar.add(numbers, cons);

		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) { return false; }
		};
		String cols [] = {"code", "student_code", "trans_no", "date", "doc_no", "date", "prev_st_code", 
				"trans_type", "base_code", "name", "grade", "year", "reason", "school", "description"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(1).setPreferredWidth(80);
		table.getColumnModel().getColumn(2).setPreferredWidth(90);
		table.getColumnModel().getColumn(6).setPreferredWidth(100);
		table.getColumnModel().getColumn(9).setPreferredWidth(110);
		table.getColumnModel().getColumn(12).setPreferredWidth(150);
		table.getColumnModel().getColumn(13).setPreferredWidth(150);
		table.getColumnModel().getColumn(14).setPreferredWidth(180);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pane.setBorder(new CompoundBorder(new EmptyBorder(2, 4, 2, 4), pane.getBorder()));
		add(pane, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		print = new JButton(Dic.w("print"), new ImageIcon("images/excel.png"));
		print.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		print.setMargin(new Insets(2, 5, 2, 5));
		
		delete = new JButton(Dic.w("delete"));
		edit = new JButton(Dic.w("edit"));
		profile = new JButton(Dic.w("student_transfers"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(profile);
		bleft.add(edit);

		setMinimumSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);

		render();
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				page = 1;
				render();
			}
		};
		transType.addActionListener(render);
		year.addActionListener(render);
		grade.addActionListener(render);
		
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				page = 1;
				render();
			}
		});
		curPage.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				page = (int) curPage.getValue();
				render();
			}
		});
		print.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				print();
			}
		});
		profile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				profile();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())
				{
				case KeyEvent.VK_ENTER:
					if(Data.PERM_STUDENTS == 2){
						edit();
						e.consume();
					}
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
		
		if(Data.PERM_STUDENTS == 2){
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						edit();
					}
				}
			});
			
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					edit();
				}
			});
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
	}
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
			refresh.setEnabled(false);
			edit.setEnabled(false);
		}
	}
	protected void profile() 
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 1);
		StudentProfile frame = (StudentProfile)
				OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(1);
	}
	public void render() 
	{
		String term = word.getText().trim();
		int type = transType.getSelectedIndex();
		int y = (int) year.getSelectedItem();
		int g = grade.getSelectedIndex();
		
		int limit = Data.LIMIT;
		int total = data.countTransform(term, type, y, g);
		pages = (int) Math.ceil((double)total/limit);
		
		Vector<Object []> rows = data.transferredStudents(term, type, y, g, page);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
		curPage.setModel(new SpinnerNumberModel(page, 1, pages > 0 ? pages : 1, 1));
		
		String text = "%d - %d / %d";
		int last = page < pages ? page*limit : total;
		text = String.format(text, page*limit - limit + 1, last, total);
		numbers.setText(text);
	}
	
	protected void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new RegTransfer(id);
		setCursor(Cursor.getDefaultCursor());
	}
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		new StudentState(0, id);
		if(data.deleteTransform(id)){
			render();
		}
	}
	public void actionPerformed(ActionEvent e) {
		page = 1;
		render();
	}
	
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String[] cols = {"student_code", "trans_no", "date", "doc_no", "date", "prev_st_code", 
						"trans_type", "base_code", "name", "grade", "year", "reason", "school", "description"};
				int[] indexes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
				TransferredStudents.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("students_transfers", cols, rows, indexes);
				report.build(path);
				TransferredStudents.this.setCursor(Cursor.getDefaultCursor());
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























