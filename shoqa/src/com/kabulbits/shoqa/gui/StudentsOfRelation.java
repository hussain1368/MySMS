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
import javax.swing.JDialog;
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
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentsOfRelation extends JDialog 
{
	private static final long serialVersionUID = 1L;
	
	private JButton delete, profile, refresh;
	private DefaultTableModel model;
	private JTable table;
	
	private StudentData data;
	private int pid;
	
	public StudentsOfRelation(int id)
	{
		pid = id;
		data = new StudentData();
		
		String title = Dic.w("students_of_parent");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, false);
		add(ribbon, BorderLayout.NORTH);
		
		String headers [] = {"code", "base_code", "name", "fname", "lname", "grade", "relation"};
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;};
		};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(60);
		table.getColumnModel().getColumn(5).setPreferredWidth(40);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
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
		delete = new JButton(Dic.w("delete"));
		profile = new JButton(Dic.w("student_relations"));
		
		bright.add(delete);
		bright.add(refresh);
		bleft.add(profile);
		
		render();
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		profile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				profile();
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					profile();
				}
				else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					delete();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					profile();
				}
			}
		});
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					delete();
				}
			});
		}
		disableButtons();
		
		Helper.esc(this);
		
		setSize(550, 300);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_STUDENTS != 2){
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
		data.detachRelation(pid, id);
		render();
	}

	protected void profile() 
	{
		if(table.getSelectedRowCount() != 1) return;
		dispose();
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		StudentProfile frame = (StudentProfile)
				OpenFrame.openFrame(StudentProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
		frame.activeTab(2);
	}

	private void render() 
	{
		Vector<Object []> rows = data.studentsOfRelation(pid);
		model.setRowCount(0);
		for(Object row [] : rows){
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




















