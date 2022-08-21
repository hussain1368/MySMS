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
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.UserData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class UserSearch extends JFrame {
	
	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static UserSearch self;
	
	private JTextField word;
	private JSpinner pager;
	private JLabel numbers;
	private JButton edit, create, refresh, delete, reset;
	private DefaultTableModel model;
	private JTable table;
	
	private UserData data;
	private int page = 1;
	private int pages;
	
	public UserSearch()
	{
		isOpen = true;
		data = new UserData(true);
		
		String title = Dic.w("search_users");
		setTitle(title);
		
		JPanel top = new JPanel(new BorderLayout());
		JPanel ribbon = new Ribbon(title, true);
		JPanel bar = new JPanel(new GridBagLayout());
		top.add(ribbon, BorderLayout.NORTH);
		top.add(bar, BorderLayout.SOUTH);
		add(top, BorderLayout.NORTH);
		
		word = new JTextField();
		word.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		pager = new JSpinner();
		pager.setPreferredSize(new Dimension(50, pager.getPreferredSize().height));
		numbers = new JLabel();
		
		bar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(4, 4, 4, 4);
		cons.gridy = 0;
		
		bar.add(new JLabel(Dic.w("search_by_code_name")), cons);
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		bar.add(word, cons);
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		bar.add(pager, cons);
		bar.add(numbers, cons);
		
		model = new DefaultTableModel(){
			static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int col) {return false;}
		};
		String cols [] = {"code", "full_name", "user_name"};
		for(String col : cols){
			model.addColumn(Dic.w(col));
		}
		
		table = new JTable(model);
		Helper.tableMakUp(table);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		
		JScrollPane pane = new JScrollPane(table);
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 5, 0, 5), pane.getBorder()));
		add(pane, BorderLayout.CENTER);
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		delete = new JButton(Dic.w("delete"));
		edit = new JButton(Dic.w("edit"));
		create = new JButton(Dic.w("new_item"));
		reset = new JButton(Dic.w("change_password"));
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(create);
		bright.add(refresh);
		bleft.add(delete);
		bleft.add(edit);
		bleft.add(reset);
		
		render();
		
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				render();
			}
		});
		word.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				page = 1;
				render();
			}
		});
		pager.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				page = (int) pager.getValue();
				render();
			}
		});
		if(Data.PERM_USERS == 2){
			create.addActionListener(new OpenFrame(UserProfile.class, this));
			edit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					edit();
				}
			});
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						edit();
					}
				}
			});
			reset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(table.getSelectedRowCount() != 1) return;
					int row = table.getSelectedRow();
					int id = (int) model.getValueAt(row, 0);
					new ResetPassword(id);
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
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())
				{
				case KeyEvent.VK_ENTER:
					if(Data.PERM_USERS == 2){
						edit();
						e.consume();
					}
					break;
				case KeyEvent.VK_DELETE:
					delete();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if(page < pages) page++;
					render();
					break;
				case KeyEvent.VK_PAGE_UP:
					if(page > 1) page--;
					render();
					break;
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});
		disableButtons();

		setMinimumSize(new Dimension(500, 400));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableButtons()
	{
		if(Data.USER_ID != 1){
			delete.setEnabled(false);
		}
		if(Data.PERM_USERS != 2){
			create.setEnabled(false);
			edit.setEnabled(false);
			refresh.setEnabled(false);
			reset.setEnabled(false);
		}
	}
	
	protected void render() 
	{
		String term = word.getText().trim();
		
		int limit = Data.LIMIT;
		int total = data.countUsers(term);
		int count = (int) Math.ceil((double)total/limit);
		pages = count > 0 ? count : 1;
		
		model.setRowCount(0);
		Vector<Object []> rows = data.searchUsers(term, page);
		for(Object [] row : rows){
			model.addRow(row);
		}
		
		int val = page > pages ? pages : page;
		int max = pages > 0 ? pages : 1;
		pager.setModel(new SpinnerNumberModel(val, 1, max, 1));
		
		String text = "%d - %d / %d";
		int last = page < pages ? page*limit : total;
		text = String.format(text, page*limit - limit + 1, last, total);
		numbers.setText(text);
	}
	
	protected void delete() 
	{
		if(Data.USER_ID != 1) return;
		
		if(table.getSelectedRowCount() != 1) return;
		if(Diags.showConf(Diags.DEL_CONF, Diags.YN) != 0) return;
		int row = table.getSelectedRow();
		String id = model.getValueAt(row, 0).toString();
		if(data.deleteUser(id)){
			render();
		}
	}
	
	protected void edit()
	{
		if(table.getSelectedRowCount() != 1) return;
		int row = table.getSelectedRow();
		int id = (int) model.getValueAt(row, 0);
		OpenFrame.openFrame(UserProfile.class, new Class<?>[]{int.class}, new Object[]{id}, this);
	}
	
	private class ResetPassword extends JDialog{
		
		private static final long serialVersionUID = 1L;
		private JPasswordField password, confPass;
		private int uid;
		
		public ResetPassword(int id)
		{
			uid = id;
			
			String title = Dic.w("change_password");
			setTitle(title);
			
			JPanel ribbon = new Ribbon(title, false);
			JPanel form = new JPanel(new GridBagLayout());
			JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
			form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
			
			add(ribbon, BorderLayout.NORTH);
			add(form, BorderLayout.CENTER);
			add(bottom, BorderLayout.SOUTH);
			
			password = new JPasswordField();
			confPass = new JPasswordField();
			
			GridBagConstraints cons = new GridBagConstraints();
			cons.anchor = GridBagConstraints.BASELINE;
			cons.insets = new Insets(5, 5, 0, 5);
			
			cons.weightx = 0;
			cons.weighty = 0;
			cons.gridx = 0;
			cons.gridy = 0;
			form.add(new JLabel(Dic.w("password")), cons);
			cons.gridy++;
			cons.weighty = 1;
			form.add(new JLabel(Dic.w("conf_pass")), cons);
			
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.weightx = 1;
			cons.weighty = 0;
			cons.gridx = 1;
			cons.gridy = 0;
			form.add(password, cons);
			cons.gridy++;
			cons.weighty = 1;
			form.add(confPass, cons);
			
			JButton save = new JButton(Dic.w("save"));
			JButton cancel = new JButton(Dic.w("cancel"));
			bottom.add(cancel);
			bottom.add(save);
			
			Helper.esc(this);
			
			FocusListener reset = new Reset();
			password.addFocusListener(reset);
			confPass.addFocusListener(reset);
			
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			save.addActionListener(new ActionListener() {
				private String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					String pass = String.valueOf(password.getPassword());
					if(!pass.matches(pattern)){
						password.setBackground(Color.RED);
						return;
					}
					if(!pass.equals(String.valueOf(confPass.getPassword()))){
						confPass.setBackground(Color.RED);
						return;
					}
					if(data.changePass(uid, pass)){
						dispose();
						Diags.showMsg(Diags.SUCCESS);
					}
				}
			});
			setSize(300, 175);
			setResizable(false);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setModal(true);
			setVisible(true);
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



























