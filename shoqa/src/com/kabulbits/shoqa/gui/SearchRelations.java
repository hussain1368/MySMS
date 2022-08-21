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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.swing.border.MatteBorder;
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
import com.kabulbits.shoqa.util.Ribbon;

public class SearchRelations extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static SearchRelations self;
	public StudentRelations parent;
	
	private JTextField word;
	private JLabel numbers;
	private JSpinner pager;
	private JButton refresh, delete, edit, assign, students, print;
	private JTable table;
	private DefaultTableModel model;
	
	private StudentData data;
	private int sid;
	private int page = 1;
	private int pages;
	
	public SearchRelations() {
		this(0);
	}
	
	public SearchRelations(int id)
	{
		isOpen = true;
		self = this;
		
		sid = id;
		data = new StudentData();
		
		String title = Dic.w("search_relations");
		setTitle(title);
		JPanel ribbon = new Ribbon(title, true);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new BorderLayout());
		JPanel form = new JPanel(new GridBagLayout());
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pager = new JSpinner();
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		numbers = new JLabel();

		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		cons.gridx = 0;
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		
		form.add(new JLabel(Dic.w("search_by_code_name")), cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx++;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.gridx++;
		
		form.add(pager, cons);
		cons.gridx++;
		form.add(numbers, cons);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String cols [] = {"code", "name", "lname", "phone", "job"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		
		center.add(form, BorderLayout.NORTH);
		center.add(pane, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);
		
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
		
		delete = new JButton(Dic.w("delete"));
		edit = new JButton(Dic.w("edit"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(print);
		bleft.add(edit);
		
		if(sid != 0){
			assign = new JButton(Dic.w("assign_for_student"));
			bleft.add(assign);
			assign.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(table.getSelectedRowCount() != 1) return;
					int row = table.getSelectedRow();
					int id = Integer.parseInt(model.getValueAt(row, 0).toString());
					new Assign(id);
				}
			});
		}else{
			students = new JButton(Dic.w("students"));
			bleft.add(students);
			students.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(table.getSelectedRowCount() != 1) return;
					int row = table.getSelectedRow();
					int id = (int) model.getValueAt(row, 0);
					new StudentsOfRelation(id);
				}
			});
		}
		
		setMinimumSize(new Dimension(800, 500));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		
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
				public void actionPerformed(ActionEvent e){
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
			if(assign != null){
				assign.setEnabled(false);
			}
		}
	}
	
	protected void render() {
		String term = word.getText().trim();
		
		int limit = Data.LIMIT;
		int total = data.countRelations(term);
		int count = (int) Math.ceil((double)total/limit);
		pages = count > 0 ? count : 1;
		
		model.setRowCount(0);
		Vector<Object []> rows = data.searchRelations(term, page);
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
		String id = model.getValueAt(row, 0).toString();
		
		if(data.deleteRelation(id)){
			render();
		}
	}
	
	protected void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new RegRelation(id, 0, parent);
		setCursor(Cursor.getDefaultCursor());
	}
	
	private void print()
	{
		final String path = Helper.xlsx("list", this);
		if(path == null) return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String cols [] = {"name", "lname", "phone", "job"};
				int[] indexes = {1, 2, 3, 4};
				SearchRelations.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Report report = new Report();
				Vector<?> rows = model.getDataVector();
				report.simpleSheet("students_relations", cols, rows, indexes);
				report.build(path);
				SearchRelations.this.setCursor(Cursor.getDefaultCursor());
			}
		}).start();
	}

	private class Assign extends JDialog{

		private static final long serialVersionUID = 1L;
		
		private JComboBox<String> relation;
		private int pid;
		
		public Assign(int id){
			this.pid = id;
			
			String title = Dic.w("assign_for_student");
			JPanel top = new Ribbon(title, false);
			add(top, BorderLayout.NORTH);
			
			JPanel center = new JPanel(new GridBagLayout());
			center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			center.setBorder(new EmptyBorder(2, 4, 0, 4));
			add(center, BorderLayout.CENTER);
			
			String [] items = {
					Dic.w("father"),
					Dic.w("mother"),
					Dic.w("brother"),
					Dic.w("sister"),
					Dic.w("uncle"),
					Dic.w("uncle2"),
			};
			JLabel rlabel = new JLabel(Dic.w("relation"));
			relation = new JComboBox<String>(items);
			relation.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel)relation.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			
			GridBagConstraints cons = new GridBagConstraints();
			cons.anchor = GridBagConstraints.BASELINE;
			cons.insets = new Insets(5, 5, 5, 5);
			cons.weighty = 1;
			cons.gridy = 0;
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			
			center.add(rlabel, cons);
			
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.gridx = 1;
			
			center.add(relation, cons);
			
			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
			bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
			add(bottom, BorderLayout.SOUTH);
			JButton save = new JButton(Dic.w("save"));
			JButton cancel = new JButton(Dic.w("cancel"));
			bottom.add(save);
			bottom.add(cancel);
			
			Helper.esc(this);
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) 
				{
					String relations [] = {"father", "mother", "brother", "sister", "uncle", "uncle2"};
					String rel = relations[relation.getSelectedIndex()];
					if(data.assignRelation(sid, pid, rel))
					{
						dispose();
						Diags.showMsg(Diags.SUCCESS);
						if(parent != null){
							parent.render();
						}
					}
				}
			});
			setSize(300 ,160);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setResizable(false);
			setModal(true);
			setVisible(true);
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




























