package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Subject;
import com.kabulbits.shoqa.db.SubjectData;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class Subjects extends JFrame{

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static Subjects self;
	
	private JTable table;
	private DefaultTableModel model;
	private JButton create, edit, moveup, movedown, refresh;
	
	private SubjectData data;
	
	public Subjects() {
		
		isOpen = true;
		self = this;
		
		data = new SubjectData();
		
		String title = Dic.w("subjects_definition");
		setTitle(title);

		JPanel top = new Ribbon(title, true);
		add(top, BorderLayout.NORTH);
		
		model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {return false;}
		};
		model.addColumn(Dic.w("subject_name"));
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		movedown = new JButton(new ImageIcon("images/down.png"));
		movedown.setToolTipText(Dic.w("move_down"));
		movedown.setMargin(new Insets(2, 5, 2, 5));
		
		moveup = new JButton(new ImageIcon("images/up.png"));
		moveup.setToolTipText(Dic.w("move_up"));
		moveup.setMargin(new Insets(2, 5, 2, 5));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		create = new JButton(Dic.w("new_item"));
		edit = new JButton(Dic.w("edit"));
		
		bright.add(movedown);
		bright.add(moveup);
		bright.add(refresh);
		
		bleft.add(edit);
		bleft.add(create);

		render();
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		if(Data.PERM_COURSES == 2){
			create.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					create();
				}
			});
			edit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					edit();
				}
			});
			table.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						edit();
						e.consume();
					}
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						edit();
					}
				}
			});
			moveup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) 
				{
					int row = table.getSelectedRow();
					if(row < 1) return;
					moveup.setEnabled(false);
					Subject value = (Subject) model.getValueAt(row, 0);
					Subject prev = (Subject) model.getValueAt(row-1, 0);
					if(data.move(value.id, value.position, prev.id, prev.position)){
						int temp = value.position;
						value.position = prev.position;
						prev.position = temp;
						model.moveRow(row, row, row-1);
						table.setRowSelectionInterval(row-1, row-1);
					}
					moveup.setEnabled(true);
				}
			});
			movedown.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) 
				{
					int row = table.getSelectedRow();
					if(row < 0 || row >= table.getRowCount()-1) return;
					movedown.setEnabled(false);
					Subject value = (Subject) model.getValueAt(row, 0);
					Subject next = (Subject) model.getValueAt(row+1, 0);
					if(data.move(value.id, value.position, next.id, next.position)){
						int temp = value.position;
						value.position = next.position;
						next.position = temp;
						model.moveRow(row, row, row+1);
						table.setRowSelectionInterval(row+1, row+1);
					}
					movedown.setEnabled(true);
				}
			});
		}
		disableButtons();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		setMinimumSize(new Dimension(400, 450));
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private void disableButtons()
	{
		if(Data.PERM_COURSES != 2){
			create.setEnabled(false);
			moveup.setEnabled(false);
			movedown.setEnabled(false);
			refresh.setEnabled(false);
			edit.setEnabled(false);
		}
	}
	
	private void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Subject subject = (Subject) model.getValueAt(row, 0);
		new RegSubject(subject.id, subject.position, this);
	}

	public void create() 
	{
		int row = table.getSelectedRow();
		int num = table.getRowCount();
		int pos = 1;
		if(num > 0){
			if(row > -1){
				Subject subject = (Subject) model.getValueAt(row, 0);
				pos = subject.position;
			}else{
				Subject subject = (Subject) model.getValueAt(num-1, 0);
				pos = subject.position + 1;
			}
		}
		new RegSubject(0, pos, this);
	}
	
	public void render(){
		model.setRowCount(0);
		Vector<Object[]> rows = data.subjectsList();
		for(Object[] row : rows){
			model.addRow(row);
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