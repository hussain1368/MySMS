package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentAttendance extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private boolean isModified = false;
	
	private JComboBox<Integer> year;
	private JButton refresh, save, regMid;
	private JTable table;
	private DefaultTableModel model;
	
	private MarkData markData;
	private int sid;
	
	public StudentAttendance(int id)
	{
		sid = id;
		markData = new MarkData();
		String title = Dic.w("student_attendance");
		
		setLayout(new BorderLayout());
		
		JPanel ribbon = new Ribbon(title, false);
		JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(options, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		options.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		year = new JComboBox<Integer>();
		for(int y=Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		year.setPrototypeDisplayValue(10000000);
		
		options.add(new JLabel(Dic.w("year")));
		options.add(year);
		
		model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					return col != 0 && row != 12;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 0){
					return String.class;
				}
				return Integer.class;
			}
		};
		String headers [] = {"month", "present", "absent", "sick", "holiday"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		Helper.singleClick(table);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 4, 0, 4), new LineBorder(Color.GRAY)));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		save = new JButton(Dic.w("save"));
		regMid = new JButton(Dic.w("reg_midterm_attendance"));
		
		bottom.add(regMid);
		bottom.add(save);
		bottom.add(refresh);
		
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		year.addActionListener(render);
		refresh.addActionListener(render);
		
		if(Data.PERM_COURSES == 2){
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save() {
					stop();
					if(isModified){
						return doSave();
					}
					return 0;
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						render();
					}
				}
			});
			regMid.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stop();
					regMidterm();
				}
			});
		}
		disableButtons();
	}
	
	private void disableButtons()
	{
		if(Data.PERM_COURSES != 2){
			refresh.setEnabled(false);
			save.setEnabled(false);
			regMid.setEnabled(false);
		}
	}
	
	protected int doSave() 
	{
		int rows = model.getRowCount() - 2;
		int y = (int) year.getSelectedItem();
		
		int count = 0;
		boolean isNull = true;
		for(int i=0; i<rows; i++)
		{
			Object values [] = new Object [4];
			isNull = true;
			for(int j=0; j<4; j++)
			{
				Object val = model.getValueAt(i, j+1);
				if(val != null) isNull = false;
				values[j] = val;
			}
			if(isNull) continue;
			
			int m = i+1;
			if(markData.saveAttendance(sid, y, m, values)) count++;
		}
		return count;
	}
	private void regMidterm()
	{
		int year = (int) this.year.getSelectedItem();
		if(markData.setMidtermAttend(sid, year)){
			Diags.showMsg(Diags.SUCCESS);
		}
	}
	public void render() 
	{
		int y = (int) year.getSelectedItem();
		Vector<Object []> rows = markData.findAttendance(sid, y);
		
		int present = 0, absent = 0, sick = 0, holiday = 0;
		
		model.setRowCount(0);
		for(Object row [] : rows)
		{
			model.addRow(row);
			if(row[1] != null) present += Integer.parseInt(row[1].toString());
			if(row[2] != null) absent += Integer.parseInt(row[2].toString());
			if(row[3] != null) sick += Integer.parseInt(row[3].toString());
			if(row[4] != null) holiday += Integer.parseInt(row[4].toString());
		}
		Object totalYear [] = {
				Dic.w("total_year"), present, absent, sick, holiday
		};
		model.addRow(totalYear);
		isModified = false;
	}
	protected void stop() 
	{
		if(table.isEditing()){
			TableCellEditor editor = table.getCellEditor();
			if(editor != null){
				if(!editor.stopCellEditing()){
					editor.cancelCellEditing();
				}
			}
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}
