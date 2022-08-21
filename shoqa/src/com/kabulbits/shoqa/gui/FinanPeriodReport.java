package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import com.kabulbits.shoqa.db.FinanceData;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class FinanPeriodReport extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static FinanPeriodReport self;
	
	private JComboBox<Option> cash;
	private JTextField [] income, outcome;
	private JTextField balance;
	
	private FinanceData data;
	
	public FinanPeriodReport(){
		isOpen = true;
		self = this;
		data = new FinanceData();
		
		String title = Dic.w("financial_period_report");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel form = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		top.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		
		top.add(ribbon, BorderLayout.NORTH);
		top.add(form, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		cash = new JComboBox<>(data.cashOptions(true));
		cash.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)cash.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		form.add(new JLabel(Dic.w("cash")));
		form.add(cash);
		
		JPanel center = new JPanel(new GridLayout(1, 2, 4, 0));
		JPanel centerRight = new JPanel(new GridBagLayout());
		JPanel centerLeft = new JPanel(new GridBagLayout());
		center.add(centerRight);
		center.add(centerLeft);
		add(center, BorderLayout.CENTER);
		
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		centerRight.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		centerLeft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		center.setBorder(new EmptyBorder(4, 4, 4, 4));
		
		Border line = new LineBorder(Color.GRAY);
		centerRight.setBorder(new TitledBorder(line, Dic.w("income_summary")));
		centerLeft.setBorder(new TitledBorder(line, Dic.w("outcome_summary")));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(5, 5, 0, 5);
		
		income = new JTextField[4];
		String texts1 [] = {"students_payments", "revenue", "cash_credit", "total"};
		cons.gridx = 0;
		cons.gridy = 0;
		cons.weighty = 0;
		for(String text : texts1){
			centerRight.add(new JLabel(Dic.w(text)), cons);
			cons.gridy++;
		}
		outcome = new JTextField[4];
		String texts2 [] = {"employee_salaries", "expense", "cash_debit", "total"};
		cons.gridx = 0;
		cons.gridy = 0;
		cons.weighty = 0;
		for(String text : texts2){
			centerLeft.add(new JLabel(Dic.w(text)), cons);
			cons.gridy++;
		}
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		
		cons.gridx = 1;
		cons.gridy = 0;
		cons.weighty = 0;
		for(int i=0; i<income.length; i++){
			cons.gridy = i;
			if(i == 3) cons.weighty = 1;
			income[i] = new JTextField();
			income[i].setEditable(false);
			centerRight.add(income[i], cons);
		}
		
		cons.gridx = 1;
		cons.gridy = 0;
		cons.weighty = 0;
		for(int i=0; i<outcome.length; i++){
			cons.gridy = i;
			if(i == 3) cons.weighty = 1;
			outcome[i] = new JTextField();
			outcome[i].setEditable(false);
			centerLeft.add(outcome[i], cons);
		}
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bleft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		JButton refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		balance = new JTextField(10);
		balance.setEditable(false);
		
		bright.add(refresh);
		bleft.add(new JLabel(Dic.w("balance")));
		bleft.add(balance);
		
		render();
		
		ActionListener render = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				render();
			}
		};
		refresh.addActionListener(render);
		cash.addActionListener(render);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		setMinimumSize(new Dimension(500, 300));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	private int REVENUE = 1;
	private int EXPENSE = 2;

	private void render() 
	{
		int cash = ((Option)this.cash.getSelectedItem()).key;
		
		int fees = data.totalStudFees(cash);
		int revenue = data.totalAccountTrans(cash, REVENUE);
		int credit = data.totalCashTrans(cash, REVENUE);
		int in = fees + revenue + credit;
		
		int salary = data.totalEmpSalary(cash);
		int expense = data.totalAccountTrans(cash, EXPENSE);
		int debit = data.totalCashTrans(cash, EXPENSE);
		int out = salary + expense + debit;
		
		income[0].setText(String.valueOf(fees));
		income[1].setText(String.valueOf(revenue));
		income[2].setText(String.valueOf(credit));
		income[3].setText(String.valueOf(in));
		
		outcome[0].setText(String.valueOf(salary));
		outcome[1].setText(String.valueOf(expense));
		outcome[2].setText(String.valueOf(debit));
		outcome[3].setText(String.valueOf(out));
		
		balance.setText(String.valueOf(in - out));
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}





















