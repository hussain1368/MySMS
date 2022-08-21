package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentTransfers extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static StudentTransfers self;
	
	private JButton transfer, allTransfers, delete, edit, refresh;
	private DefaultTableModel model;
	private JTable table;
	
	private StudentData data;
	public Student student;
	private int sid;
	
	public StudentTransfers(Student student)
	{
		this.student = student;
		this.sid = student.id;
		
		data = new StudentData();
		
		String title = Dic.w("student_transfers");
		setLayout(new BorderLayout());
		
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);

		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String cols [] = {"code", "year", "grade", "trans_type", "trans_no", "date", "doc_no", "date", "prev_st_code", "school", "reason", "description"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(8).setPreferredWidth(100);
		table.getColumnModel().getColumn(9).setPreferredWidth(150);
		table.getColumnModel().getColumn(11).setPreferredWidth(200);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		allTransfers = new JButton(Dic.w("all_transfers"));
		transfer = new JButton(Dic.w("reg_transfer"));
		delete = new JButton(Dic.w("delete"));
		edit = new JButton(Dic.w("edit"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(allTransfers);
		bright.add(transfer);
		bright.add(refresh);
		bleft.add(edit);
		bleft.add(delete);
		
		if(Data.PERM_STUDENTS == 2)
		{
			refresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					render();
				}
			});
			transfer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					transferStudent();
				}
			});
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					edit();
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
		}
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
		}
		allTransfers.addActionListener(new OpenFrame(TransferredStudents.class, this));
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
			transfer.setEnabled(false);
			edit.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	protected void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		new RegTransfer(id, this);
	}

	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		
		int sig = Diags.showConf(Diags.DEL_CONF, Diags.YN);
		if(sig != 0) return;
		
		if(data.deleteTransform(id)){
			new StudentState(sid, 0);
			render();
		}
	}

	protected void transferStudent() {
		new RegTransfer(sid, student.grade, this);
	}
	
	public void render()
	{
		model.setRowCount(0);
		Vector<String []> rows = data.studentTransfers(sid);
		for(String [] row : rows){
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
