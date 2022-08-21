package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.util.DBEditor;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class Accounts extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static Accounts self;
	
	private JTextField word;
	private JComboBox<String> accountType;
	private JSpinner pager;
	private JButton refresh, create;
	private DefaultTableModel model;
	private JTable table;
	
	private FinanceData data;
	
	public Accounts() 
	{
		isOpen = true;
		self = this;
		data = new FinanceData();

		String title = Dic.w("revenue_and_expense");
		setTitle(title);

		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new GridBagLayout());

		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		accountType = new JComboBox<>();
		accountType.addItem(Dic.w("both"));
		accountType.addItem(Dic.w("revenue"));
		accountType.addItem(Dic.w("expense"));
		accountType.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)accountType.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		accountType.setPrototypeDisplayValue("xxxxxxxx");
		
		pager = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 5, 4, 5);
		
		form.add(new JLabel(Dic.w("account_name")), cons);
		
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		form.add(word, cons);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		form.add(accountType, cons);
		form.add(pager, cons);
		
		model = new DefaultTableModel(){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {
				if(Data.PERM_FINANCE == 2){
					return (col == 1);
				}
				return false;
			}
		};
		String cols [] = {"code", "account_name", "account_type"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(1).setPreferredWidth(200);
		if(Data.PERM_FINANCE == 2){
			table.getColumnModel().getColumn(1).setCellEditor(new DBEditor(true) 
			{
				private static final long serialVersionUID = 1L;
				@Override
				public boolean save(int row, Object value) {
					int id = (int) model.getValueAt(row, 0);
					String name = value.toString().trim();
					if(name.length() > 0){
						return data.editAccount(name, id);
					}
					return false;
				}
			});
		}
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);

		create = new JButton(Dic.w("new_item"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));

		bright.add(refresh);
		bleft.add(create);

		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
				render();
			}
		};
		refresh.addActionListener(render);
		accountType.addActionListener(render);
		
		pager.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				stop();
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				stop();
				render();
			}
		});
		if(Data.PERM_FINANCE == 2){
			create.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					stop();
					new AccountReg();
				}
			});
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();
		render();

		setSize(500, 400);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.PERM_FINANCE != 2){
			refresh.setEnabled(false);
			create.setEnabled(false);
		}
	}

	public void render() 
	{
		int type = accountType.getSelectedIndex();
		int page = (int) pager.getValue();
		Vector<Object[]> rows = data.searchAccount(word.getText().trim(), type, page);
		model.setRowCount(0);
		for(Object[] row : rows){
			model.addRow(row);
		}
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