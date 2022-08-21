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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class CostsReport extends JFrame 
{
	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static CostsReport self;
	
	private JComboBox<Integer> year;
	private JComboBox<String> month, grade;
	private JTable table;
	private DefaultTableModel model;
	private JTextField totalDebit, totalDiscount, totalPayed, totalRemain;
	private JButton browseDebit, browseDiscount, browsePayed, browseRemain;
	
	private FinanceData data;

	public CostsReport()
	{
		isOpen = true;
		self = this;
		data = new FinanceData();
		
		String title = Dic.w("students_costs_report");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, true);
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
		
		grade = new JComboBox<String>();
		grade.addItem(Dic.w("all_grades"));
		for(int i=1; i<13; i++){
			grade.addItem(String.valueOf(i));
		}
		grade.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) grade.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		grade.setPrototypeDisplayValue("xxxxxxxxxx");
		
		options.add(new JLabel(Dic.w("year")));
		options.add(year);
		options.add(new JLabel(Dic.w("month")));
		options.add(month);
		options.add(new JLabel(Dic.w("grade")));
		options.add(grade);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String cols [] = {"cost_code", "cost_name", "involved_number", "total_amount"};
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
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		center.add(pane, BorderLayout.CENTER);
		
		JPanel summary = new JPanel(new GridBagLayout());
		Border inside = new TitledBorder(new LineBorder(Color.GRAY), Dic.w("finance_summary"));
		summary.setBorder(new CompoundBorder(new EmptyBorder(4, 4, 5, 4), inside));
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
		
		browseDebit = new JButton("...");
		browseDiscount = new JButton("...");
		browsePayed = new JButton("...");
		browseRemain = new JButton("...");
		
		Dimension dim = new Dimension(25, 20);
		
		browseDebit.setPreferredSize(dim);
		browseDiscount.setPreferredSize(dim);
		browsePayed.setPreferredSize(dim);
		browseRemain.setPreferredSize(dim);
		
		browseDebit.setEnabled(false);
		browseRemain.setEnabled(false);
		
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
		cons.gridy = 0;
		
		center.add(summary, BorderLayout.SOUTH);
		
		JButton refresh;
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		bottom.add(refresh);
		
		render();
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		};
		year.addActionListener(render);
		month.addActionListener(render);
		grade.addActionListener(render);
		refresh.addActionListener(render);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		
		setMinimumSize(new Dimension(600, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void render() 
	{
		int y = (int) year.getSelectedItem();
		int m = month.getSelectedIndex();
		int g = grade.getSelectedIndex();
		
		Vector<Object []> rows = data.costsReport(y, m, g);
		model.setRowCount(0);
		for(Object row [] : rows){
			model.addRow(row);
		}
		
		int cost = data.totalCost(y, m, g);
		int discount = data.totalDiscount(y, m, g);
		int payed = data.totalPayed(y, m, g);
		int remain = cost - discount - payed;
		
		totalDebit.setText(String.valueOf(cost));
		totalDiscount.setText(String.valueOf(discount));
		totalPayed.setText(String.valueOf(payed));
		totalRemain.setText(String.valueOf(remain));
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}




















