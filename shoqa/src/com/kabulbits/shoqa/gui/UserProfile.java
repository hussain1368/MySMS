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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.UserData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class UserProfile extends JFrame{

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static UserProfile self;
	
	private JTextField fullName, userName;
	private JPasswordField password, confPass;
	private JComboBox<String> accessLevel;
	private JComboBox<?>[] accessLevels;
	
	private UserData data;
	private int uid;
	
	public UserProfile(){
		this(0);
	}
	
	public UserProfile(int id)
	{
		isOpen = true;
		self = this;
		
		uid = id;
		data = new UserData(true);
		
		String title = Dic.w(id == 0 ? "user_reg" : "user_edit");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, false);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		split.setDividerLocation(300);
		
		JPanel form = new JPanel(new GridBagLayout());
		JPanel levels = new JPanel(new GridBagLayout());
		JPanel access = new JPanel(new BorderLayout());
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
		access.add(bar, BorderLayout.NORTH);
		access.add(levels, BorderLayout.CENTER);
		split.setRightComponent(form);
		split.setLeftComponent(access);
		
		add(ribbon, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
		
		fullName = new JTextField();
		fullName.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		userName = new JTextField();
		password = new JPasswordField();
		confPass = new JPasswordField();
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(5, 5, 0, 5);
		
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.weighty = 0;
		
		form.add(new JLabel(Dic.w("full_name")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("user_name")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("password")), cons);
		cons.gridy++;
		cons.weighty = 1;
		form.add(new JLabel(Dic.w("conf_pass")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		cons.gridy = 0;
		cons.weighty = 0;
		
		form.add(fullName, cons);
		cons.gridy++;
		form.add(userName, cons);
		cons.gridy++;
		form.add(password, cons);
		cons.gridy++;
		cons.weighty = 1;
		form.add(confPass, cons);
		
		String options [] = {
				Dic.w("not_accessible"),
				Dic.w("read_only"),
				Dic.w("reg_and_edit"),
		};
		
		accessLevel = new JComboBox<String>(options);
		accessLevel.setPrototypeDisplayValue("xxxxxxxxxxxxxx");
		accessLevel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel)accessLevel.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		
		bar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bar.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
		bar.add(accessLevel);
		
		String labels [] = {"students", "employees", "courses_subjs_marks", "finance_section", "library", "users", "settings"};
		accessLevels = new JComboBox[labels.length];
		
		levels.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		cons.gridy = 0;
		cons.weighty = 0;
		
		for(int i=0; i<labels.length; i++)
		{
			cons.gridy = i;
			if(i == labels.length - 1) cons.weighty = 1;
			
			cons.fill = GridBagConstraints.NONE;
			cons.weightx = 0;
			cons.gridx = 0;
			levels.add(new JLabel(Dic.w(labels[i])), cons);
				
			accessLevels[i] = new JComboBox<String>(options);
			accessLevels[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel)accessLevels[i].getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.weightx = 1;
			cons.gridx = 1;
			levels.add(accessLevels[i], cons);
		}
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(bottom, BorderLayout.SOUTH);
		
		JButton save = new JButton(Dic.w("save"));
		bottom.add(save);
		
		if(uid != 0){
			password.setEnabled(false);
			confPass.setEnabled(false);
			JButton refresh = new JButton(new ImageIcon("images/refresh.png"));
			refresh.setToolTipText(Dic.w("refresh"));
			refresh.setMargin(new Insets(2, 3, 2, 3));
			bottom.add(refresh);
			
			Object [] info = data.findUser(uid);
			if(info == null){
				dispose();
				isOpen = false;
				return;
			}
			fillForm(info);
			
			refresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object [] info = data.findUser(uid);
					if(info == null){
						dispose();
						isOpen = false;
						return;
					}
					fillForm(info);
				}
			});
		}
		
		FocusListener reset = new Reset();
		fullName.addFocusListener(reset);
		userName.addFocusListener(reset);
		password.addFocusListener(reset);
		confPass.addFocusListener(reset);
		
		accessLevel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = accessLevel.getSelectedIndex();
				for(JComboBox<?> combo : accessLevels){
					combo.setSelectedIndex(index);
				}
			}
		});
			
		save.addActionListener(new BgWorker<Boolean>(this) {
			@Override
			protected Boolean save() {
				return doSave();
			}
			@Override
			protected void finish(Boolean result) {
				if(result){
					afterSave();
				}
			}
		});
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOpen = false;
			}
		});

		setMinimumSize(new Dimension(550, 350));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void fillForm(Object[] info) 
	{
		int i = 0;
		fullName.setText(info[i++].toString());
		userName.setText(info[i++].toString());
		for(JComboBox<?> combo : accessLevels){
			combo.setSelectedIndex((int) info[i++]);
		}
	}

	private boolean doSave()
	{
		if(!validator()) return false;
		
		String full = fullName.getText().trim();
		String user = userName.getText().trim().toLowerCase();
		String pass = String.valueOf(password.getPassword()).trim();
		
		int perms [] = new int[accessLevels.length];
		int i = 0;
		for(JComboBox<?> combo : accessLevels){
			perms[i++] = combo.getSelectedIndex();
		}
		if(uid == 0){
			return data.insertUser(full, user, pass, perms);
		}else{
			return data.editUser(uid, full, user, perms);
		}
	}

	private void afterSave() 
	{
		Diags.showMsg(Diags.SUCCESS);
		if(uid == 0){
			fullName.setText("");
			userName.setText("");
			password.setText("");
			confPass.setText("");
			accessLevel.setSelectedIndex(0);
		}
	}

	String userPattern = "^[a-zA-Z0-9_-]{3,16}$";
	String passPattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
	private boolean validator() 
	{
		if(fullName.getText().trim().length() == 0){
			fullName.setBackground(Color.RED);
			Diags.showErrLang("required_error");
			return false;
		}
		if(!userName.getText().matches(userPattern)){
			userName.setBackground(Color.RED);
			Diags.showErrLang("invalid_user_name");
			return false;
		}
		if(uid == 0){
			if(!String.valueOf(password.getPassword()).matches(passPattern)){
				password.setBackground(Color.RED);
				Diags.showErrLang("invalid_password");
				return false;
			}
			if(!String.valueOf(password.getPassword()).equals(String.valueOf(confPass.getPassword()))){
				confPass.setBackground(Color.RED);
				Diags.showErrLang("confirm_not_match");
				return false;
			}
		}
		boolean valid = false;
		for(JComboBox<?> combo : accessLevels){
			if(combo.getSelectedIndex() != 0) valid = true;
		}
		if(!valid){
			Diags.showErrLang("at_least_one_permission_required");
			return false;
		}
		return true;
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}

























