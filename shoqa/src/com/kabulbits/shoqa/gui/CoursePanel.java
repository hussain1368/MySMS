package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.SpinnerEditor;

public class CoursePanel extends JPanel 
{
	private static final long serialVersionUID = 1L;
	public boolean isModified = false;
	
	private JComboBox<Integer> grade, year;
	private JButton save, create, delete, refresh, members, attendance, schedule, costs, payments;
	private DefaultTableModel model;
	private JTable table;

	private CourseData data;
	
	public CoursePanel() {
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(100, 100));
		
		data = new CourseData();
		
		JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(top, BorderLayout.NORTH);

		grade = new JComboBox<>();
		for(int i=1; i<13; i++){
			grade.addItem(i);
		}
		year = new JComboBox<>();
		int y = Data.EDUC_YEAR;
		while (y >=Data.START_YEAR) {
			year.addItem(y--);
		}
		grade.setPrototypeDisplayValue(100000);
		year.setPrototypeDisplayValue(100000);

		top.add(new JLabel(Dic.w("grade")));
		top.add(grade);
		top.add(new JLabel(Dic.w("year")));
		top.add(year);
		
		model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_COURSES == 2){
					if(model.getValueAt(row, 0) == null){
						return col != 0;
					}
					return col == 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int col) {
				if(col == 3){
					return String.class;
				}
				return Integer.class;
			}
		};
		String headers [] = {"code", "year", "grade", "identifier", "shift"};
		for(String item : headers){
			model.addColumn(Dic.w(item));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		
		y = Data.EDUC_YEAR;
		table.getColumnModel().getColumn(3).setCellEditor(Helper.rtlEditor());		
		table.getColumnModel().getColumn(1).setCellEditor(new SpinnerEditor(y, Data.START_YEAR, y));
		table.getColumnModel().getColumn(2).setCellEditor(new SpinnerEditor(1, 1, 12));
		table.getColumnModel().getColumn(4).setCellEditor(new SpinnerEditor(1, 1, 5));
		
		String items [] = {"marks", "attendance", "timetable", "costs", "payments", "delete"};
		String icons [] = {"images/page.png", "images/tick.png", "images/time.png", "images/coins.png", "images/dollar.png", "images/delete.png"};
		JMenuItem menus [] = new JMenuItem[items.length];
		Font font = new Font("tahoma", Font.PLAIN, 11);
		Dimension dim = new Dimension(150, 0);
		final JPopupMenu menu = new JPopupMenu();
		
		for(int i=0; i<items.length; i++){
			menus[i] = new JMenuItem(Dic.w(items[i]), new ImageIcon(icons[i]));
			dim.height = menus[i].getPreferredSize().height;
			menus[i].setPreferredSize(dim);
			menus[i].setFont(font);
			menu.add(menus[i]);
		}
		if(Data.USER_ID != 1){
			menus[5].setEnabled(false);
		}
		if(Data.PERM_FINANCE == 0){
			menus[3].setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			menus[4].setEnabled(false);
		}
		members = new JButton(Dic.w("marks"));
		attendance = new JButton(Dic.w("attendance"));
		schedule = new JButton(Dic.w("timetable"));
		costs = new JButton(Dic.w("costs"));
		payments = new JButton(Dic.w("payments"));
		
		JPanel operations = new JPanel(new GridBagLayout());
		operations.setBorder(new LineBorder(Color.GRAY));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.anchor = GridBagConstraints.NORTH;
		cons.insets = new Insets(2, 2, 2, 2);
		cons.weightx = 1;
		cons.gridx = 0;
		
		operations.add(members, cons);
		operations.add(attendance, cons);
		operations.add(schedule, cons);
		operations.add(costs, cons);
		cons.weighty = 1;
		operations.add(payments, cons);
		
		JPanel center = new JPanel(new BorderLayout(5, 0));
		center.setBorder(new EmptyBorder(0, 5, 0, 5));
		center.add(new JScrollPane(table), BorderLayout.CENTER);
		center.add(operations, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		save = new JButton(Dic.w("save"));
		create = new JButton(Dic.w("new_item"));
		delete = new JButton(Dic.w("delete"));
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(save);
		bright.add(refresh);
		bleft.add(create);
		bleft.add(delete);
		
		render();

		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				isModified = true;
			}
		});
		ActionListener render = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		grade.addActionListener(render);
		year.addActionListener(render);
		
		if(Data.PERM_COURSES == 2){
			create.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					stop();
					model.addRow(new Object[]{null, Data.EDUC_YEAR, 1, "", 1});
				}
			});
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
					stop();
					return doSave();
				}
				@Override
				protected void finish(Integer result){
					if(result > 0){
						Diags.showMsg(String.format(Dic.w("items_saved"), result));
						render();
					}
				}
			});
		}
		if(Data.USER_ID == 1){
			ActionListener doDelete = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					delete();
				}
			};
			delete.addActionListener(doDelete);
			menus[5].addActionListener(doDelete);
		}
		
		final Class<?> frames [] = {CourseMarks.class, CourseAttendance.class, CourseTimetable.class, CourseCostAssignment.class, CoursePayments.class};
		final JButton buttons [] = {members, attendance, schedule, costs, payments};
		for(int i=0; i<frames.length; i++){
			final int x = i;
			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					operation(frames[x]);
				}
			};
			buttons[i].addActionListener(listener);
			menus[i].addActionListener(listener);
		}
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					operation(CourseMarks.class);
					e.consume();
				}
				else if(e.getKeyCode() == KeyEvent.VK_DELETE){
					delete();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
					operation(CourseMarks.class);
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3)
				{
					Point point = e.getPoint();
					int row = table.rowAtPoint(point);
					table.setRowSelectionInterval(row, row);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
		    }
		});
		disableButtons();
	}
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_COURSES != 2){
			create.setEnabled(false);
			save.setEnabled(false);
			refresh.setEnabled(false);
		}
		if(Data.PERM_FINANCE == 0){
			costs.setEnabled(false);
		}
		if(Data.PERM_FINANCE != 2){
			payments.setEnabled(false);
		}
	}

	public void render()
	{
		model.setRowCount(0);
		int y = (int) year.getSelectedItem();
		int g = (int) grade.getSelectedItem();
		data.searchCourse(y, g, model);
		isModified = false;
	}
	
	private int doSave()
	{
		int count = 0;
		for(int i=0; i<model.getRowCount(); i++)
		{
			if(model.getValueAt(i, 0) == null)
			{
				String y = model.getValueAt(i, 1).toString();
				String g = model.getValueAt(i, 2).toString();
				String n = model.getValueAt(i, 3).toString();
				String s = model.getValueAt(i, 4).toString();
				if(data.addCourse(y, g, n, s)) count++;
			}
			else{
				String id = model.getValueAt(i, 0).toString();
				String name = model.getValueAt(i, 3).toString();
				if(data.editCourse(id, name)) count++;
			}
		}
		return count;
	}
	
	private void delete()
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Object value = model.getValueAt(row, 0);
		if(value == null) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		if(data.deleteCourse(value.toString())){
			render();
		}
	}
	
	private void operation(Class<?> window) 
	{
		stop();
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		Object value = model.getValueAt(row, 0);
		if(value == null) return;
		int id = Integer.parseInt(value.toString());
		
		OpenFrame.openFrame(window, new Class<?>[]{int.class}, new Object[]{id}, this);
	}
	
	protected void stop() {
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
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

