package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentRelations extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JButton regRelation, edit, delete, refresh;
	private DefaultTableModel model;
	private JTable table;
	
	private StudentData data;
	private int sid;
	
	public StudentRelations(int id)
	{
		sid = id;
		data = new StudentData();
		
		String title = Dic.w("student_relations");
		
		setLayout(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		String cols [] = {"code", "name", "lname", "phone", "job", "relation"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		regRelation = new JButton(Dic.w("reg_relation"));
		edit = new JButton(Dic.w("edit"));
		delete = new JButton(Dic.w("delete"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(regRelation);
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
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					edit();
				}
			});
			regRelation.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int sig = Diags.showOps("choose_relation", new String[]{"create_new", "search"});
					if(sig == 0){
						new RegRelation(0, sid, StudentRelations.this);
					}
					else if(sig == 1)
					{
						if(!SearchRelations.isOpen){
							SearchRelations searchRel = new SearchRelations(sid);
							searchRel.parent = StudentRelations.this;
						}else{
							SearchRelations.self.requestFocus();
							SearchRelations.self.setExtendedState(JFrame.NORMAL);
						}
					}
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
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
			regRelation.setEnabled(false);
			edit.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	protected void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int sig = Diags.showConf(Diags.DEL_CONF, Diags.YN);
		if(sig != 0) return;
		
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		if(data.detachRelation(id, sid)){
			render();
		}
	}

	private void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = Integer.parseInt(model.getValueAt(row, 0).toString());
		new RegRelation(id, sid, StudentRelations.this);
	}

	public void render() {
		
		model.setRowCount(0);
		Vector<String []> rows = data.studentRelations(sid);
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





















