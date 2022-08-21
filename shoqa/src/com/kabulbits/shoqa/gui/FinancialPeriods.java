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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class FinancialPeriods extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static FinancialPeriods self;
	
	private DefaultTableModel model;
	private JTable table;
	private JSpinner pager;
	private JButton create, edit, refresh;

	private FinanceData data;
	
	public FinancialPeriods()
	{
		isOpen = true;
		self = this;
		data = new FinanceData();

		String title = Dic.w("financial_periods");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, true);
		add(ribbon, BorderLayout.NORTH);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) { return false; }
			@Override
			public Class<?> getColumnClass(int index) {
				if(index == 4) return Boolean.class;
				return String.class;
			}
		};
		String cols [] = {"code", "name", "start_date", "end_date", "current_period"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		create = new JButton(Dic.w("new_item"));
		edit = new JButton(Dic.w("edit"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bleft.add(pager);
		bleft.add(edit);
		bright.add(create);
		bright.add(refresh);
		
		render();
		
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render();
			}
		});
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				render();
			}
		});
		if(Data.PERM_FINANCE == 2){
			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new FinanPeriodReg();
				}
			});
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					edit();
				}
			});
			table.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						edit();
						e.consume();
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
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		
		setMinimumSize(new Dimension(600, 400));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			create.setEnabled(false);
			edit.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	
	public void render()
	{
		int page = (int) pager.getValue();
		Vector<Object[]> rows = data.financialPeriods(page);
		model.setRowCount(0);
		for(Object[] row : rows){
			model.addRow(row);
		}
	}
	
	private void edit()
	{
		if(table.getSelectedColumnCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		new FinanPeriodReg(id);
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}



















