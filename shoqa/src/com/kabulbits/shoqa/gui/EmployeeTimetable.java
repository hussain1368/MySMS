package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class EmployeeTimetable extends JPanel 
{
	private static final long serialVersionUID = 1L;
	private JTable scheduleTable, coursesTable;
	private JButton refresh, delete;
	private DefaultTableModel scheduleModel, coursesModel;
	private JComboBox<Integer> shifts;
	private EmployeeData data;
	private int empid;
	
	public EmployeeTimetable(int id) 
	{
		empid = id;
		String title = Dic.w("teacher_timetable");
		
		data = new EmployeeData();

		setLayout(new BorderLayout());
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, false);
		JPanel optionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		optionBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		shifts = new JComboBox<>(new Integer [] { 1, 2, 3, 4, 5 });
		shifts.setPrototypeDisplayValue(1000000);
		
		optionBar.add(new JLabel(Dic.w("shift")));
		optionBar.add(shifts);
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(optionBar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		scheduleModel = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String times [] = {"week_days", "first", "second", "third", "fourth", "fifth", "sixth"};
		for(String time : times){
			scheduleModel.addColumn(Dic.w(time));
		}
		scheduleTable = new JTable(scheduleModel);
		Helper.tableMakUp(scheduleTable);
		scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(70);
		
		String headers [] = {"course", "subject", "teach_hours"};
		coursesModel = new DefaultTableModel()
		{
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		for(String header : headers){
			coursesModel.addColumn(Dic.w(header));
		}
		coursesTable = new JTable(coursesModel);
		Helper.tableMakUp(coursesTable);
		
		JScrollPane pane1 = new JScrollPane(scheduleTable);
		JScrollPane pane2 = new JScrollPane(coursesTable);
		
		JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		center.setBorder(new EmptyBorder(0, 4, 0, 4));
		center.setDividerLocation(225);
		center.setTopComponent(pane1);
		center.setBottomComponent(pane2);
		add(center, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);

		delete = new JButton(Dic.w("delete"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setPreferredSize(new Dimension(26, 26));
		
		bright.add(delete);
		bright.add(refresh);

		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		shifts.addActionListener(render);
		if(Data.USER_ID == 1){
			delete.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					stop();
					delete();
				}
			});
			coursesTable.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						stop();
						delete();
					}
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
		if(Data.PERM_COURSES != 2){
			refresh.setEnabled(false);
		}
	}
	public void render() 
	{
		int sh = shifts.getSelectedIndex() + 1;
		Object rows [][] = data.teacherTimetable(empid, sh);
		
		scheduleModel.setRowCount(0);
		for(Object row [] : rows){
			scheduleModel.addRow(row);
		}
		Vector<Object []> rows2 = data.teacherAssignedSubjs(empid);
		coursesModel.setRowCount(0);
		for(Object row [] : rows2){
			coursesModel.addRow(row);
		}
	}
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		if(coursesTable.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		
		int row = coursesTable.getSelectedRow();
		Object value = coursesModel.getValueAt(row, 0);
		if(value != null){
			int pk = ((Option)value).key;
			if(data.deleteAssignedSubj(pk)){
				render();
			}
		}
	}
	protected void stop() 
	{
		if(scheduleTable.isEditing()){
			TableCellEditor editor = scheduleTable.getCellEditor();
			if(editor != null){
				if(!editor.stopCellEditing()){
					editor.cancelCellEditing();
				}
			}
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


















