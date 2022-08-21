package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentFinance extends JPanel 
{

	private static final long serialVersionUID = 1L;
	private boolean isModified = false;
	
	private JComboBox<Integer> year;
	private JComboBox<String> month;
	private JTable table;
	private DefaultTableModel model;
	private JTextField totalDebit, totalDiscount, totalPayed, totalRemain;
	private JButton browseDiscount, browsePayed, refresh, save;;
	
	private FinanceData data;
	private int sid;
	
	public StudentFinance(int id)
	{
		sid = id;
		data = new FinanceData();
		
		setLayout(new BorderLayout());
		
		String title = Dic.w("student_finance");
		JPanel ribbon = new Ribbon(title, false);
		JPanel options = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel top = new JPanel(new BorderLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(options, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new BorderLayout());
		add(center, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(bottom, BorderLayout.SOUTH);
		
		options.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		year = new JComboBox<Integer>();
		for(int y=Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		year.setPrototypeDisplayValue(10000000);
		
		String monthNames [] = {
				"total_year", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot",
		};
		
		month = new JComboBox<>();
		for(String item : monthNames){
			month.addItem(Dic.w(item));
		}
		ULocale locale = new ULocale("@calendar=persian");
		int m = Calendar.getInstance(locale).get(Calendar.MONTH) + 1;
		month.setSelectedIndex(m);
		
		month.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) month.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		month.setPrototypeDisplayValue("xxxxxxxxxxx");
		
		options.add(new JLabel(Dic.w("year")));
		options.add(year);
		options.add(new JLabel(Dic.w("month")));
		options.add(month);

		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					if(month.getSelectedIndex() == 0){
						return false;
					}
					return col == 3;
				}
				return false;
			}
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 3){
					return Boolean.class;
				}
				return String.class;
			}
		};
		String cols [] = {"cost_code", "cost_name", "monthly_payable", "involvement"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(70);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 4, 0, 4), pane.getBorder()));
		center.add(pane, BorderLayout.CENTER);
		
		JPanel summary = new JPanel(new GridBagLayout());
		Border inside = new TitledBorder(new LineBorder(Color.GRAY), Dic.w("finance_summary"));
		summary.setBorder(new CompoundBorder(new EmptyBorder(4, 3, 5, 3), inside));
		summary.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		totalDebit = new JTextField(15);
		totalDiscount = new JTextField(15);
		totalPayed = new JTextField(15);
		totalRemain = new JTextField(15);
		
		totalDebit.setEditable(false);
		totalDiscount.setEditable(false);
		totalPayed.setEditable(false);
		totalRemain.setEditable(false);
		
		totalDebit.setBackground(Color.ORANGE);
		totalDiscount.setBackground(Color.CYAN);
		totalPayed.setBackground(Color.GREEN);
		totalRemain.setBackground(Color.LIGHT_GRAY);
		
		browseDiscount = new JButton("...");
		browsePayed = new JButton("...");
		
		Dimension dim = new Dimension(25, 20);
		
		browseDiscount.setPreferredSize(dim);
		browsePayed.setPreferredSize(dim);
		
		JLabel labelDebit, labelDiscount, labelPayed, labelRemain;
		
		labelDebit = new JLabel(Dic.w("total_cost"));
		labelDiscount = new JLabel(Dic.w("discount_and_subtract"));
		labelPayed = new JLabel(Dic.w("total_payed"));
		labelRemain = new JLabel(Dic.w("total_remain"));

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		cons.anchor = GridBagConstraints.NORTHEAST;
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.gridx = 0;
		cons.gridy = 0;
		
		summary.add(labelDebit, cons);
		cons.gridy++;
		summary.add(labelDiscount, cons);
		cons.gridy++;
		summary.add(labelPayed, cons);
		cons.gridy++;
		summary.add(labelRemain, cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridx = 1;
		cons.gridy = 0;
		
		summary.add(totalDebit, cons);
		cons.gridy++;
		summary.add(totalDiscount, cons);
		cons.gridy++;
		summary.add(totalPayed, cons);
		cons.gridy++;
		summary.add(totalRemain, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.gridx = 2;
		
		cons.gridy = 1;
		summary.add(browseDiscount, cons);
		cons.gridy = 2;
		summary.add(browsePayed, cons);
		
		center.add(summary, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		save = new JButton(Dic.w("save"));
		
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
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
				render();
			}
		};
		year.addActionListener(render);
		month.addActionListener(render);
		refresh.addActionListener(render);
		
		browsePayed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new StudentPayment(sid, (int) year.getSelectedItem());
			}
		});
		browseDiscount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new StudentDiscount(sid, (int) year.getSelectedItem());
			}
		});
		if(Data.PERM_FINANCE == 2){
			table.getTableHeader().addMouseListener(new MouseAdapter(){
				private boolean checked = true;
			    public void mousePressed(MouseEvent e)
			    {
			    	if(month.getSelectedIndex() == 0) return;
			        if(model.getRowCount() == 0) return;
			        
		        	int col = table.columnAtPoint(e.getPoint());
			        if(col == 3){
			        	for(int i=0; i<model.getRowCount(); i++){
			        		model.setValueAt(checked, i, col);
			        	}
			        }
			        checked = !checked;
			    }
			});
			save.addActionListener(new BgWorker<Integer>(this){
				@Override
				protected Integer save(){
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
		}
		disableButtons();
	}
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			refresh.setEnabled(false);
			save.setEnabled(false);
		}
	}

	protected int doSave() 
	{
		int m = month.getSelectedIndex();
		if(m == 0) return 0;
		int y = (int) year.getSelectedItem();
		
		int g = data.stGradeAtYear(sid, y);
		if(g == 0){
			Diags.showErrLang("student_has_no_enrolment_error");
			return 0;
		}
		int count = 0;
		for(int i=0; i<table.getRowCount(); i++)
		{
			int id = Integer.parseInt(model.getValueAt(i, 0).toString());
			boolean assign = (boolean) model.getValueAt(i, 3);
			if(assign){
				if(data.addCostForStudent(sid, y, m, id, g)) count++;
			}else{
				if(data.removeCostFromStudent(sid, y, m, id)) count++;
			}
		}
		return count;
	}
	
	public void render() 
	{
		int y = (int) year.getSelectedItem();
		int m = month.getSelectedIndex();
		int g = data.stGradeAtYear(sid, y);
		
		Vector<Object []> rows = data.studentCostsList(sid, y, m, g);
		model.setRowCount(0);
		for(Object [] row : rows){
			model.addRow(row);
		}
		
		int cost = data.stTotalCost(sid, y, m, g);
		int discount = data.stTotalDiscount(sid, y, m);
		int payed = data.stTotalPayed(sid, y, m);
		int remain = cost - discount - payed;
		
		totalDebit.setText(String.valueOf(cost));
		totalDiscount.setText(String.valueOf(discount));
		totalPayed.setText(String.valueOf(payed));
		totalRemain.setText(String.valueOf(remain));

		isModified = false;
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}























