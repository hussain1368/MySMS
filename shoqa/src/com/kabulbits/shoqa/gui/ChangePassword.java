package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.UserData;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class ChangePassword extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPasswordField oldPass, newPass, confPass;
	
	private UserData data;
	
	public ChangePassword()
	{
		data = new UserData(true);
		
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
		
		oldPass = new JPasswordField();
		newPass = new JPasswordField();
		confPass = new JPasswordField();
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE;
		cons.insets = new Insets(5, 5, 0, 5);
		
		cons.weightx = 0;
		cons.weighty = 0;
		cons.gridx = 0;
		cons.gridy = 0;
		form.add(new JLabel(Dic.w("old_pass")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("new_pass")), cons);
		cons.gridy++;
		cons.weighty = 1;
		form.add(new JLabel(Dic.w("conf_pass")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.weighty = 0;
		cons.gridx = 1;
		cons.gridy = 0;
		form.add(oldPass, cons);
		cons.gridy++;
		form.add(newPass, cons);
		cons.gridy++;
		cons.weighty = 1;
		form.add(confPass, cons);
		
		JButton save = new JButton(Dic.w("save"));
		JButton cancel = new JButton(Dic.w("cancel"));
		bottom.add(cancel);
		bottom.add(save);
		
		Helper.esc(this);
		
		FocusListener reset = new Reset();
		oldPass.addFocusListener(reset);
		newPass.addFocusListener(reset);
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
				if(!data.passwordMatch(String.valueOf(oldPass.getPassword()), Data.USER_ID)){
					oldPass.setBackground(Color.RED);
					return;
				}
				String pass = String.valueOf(newPass.getPassword());
				if(!pass.matches(pattern)){
					newPass.setBackground(Color.RED);
					return;
				}
				if(!pass.equals(String.valueOf(confPass.getPassword()))){
					confPass.setBackground(Color.RED);
					return;
				}
				if(data.changePass(Data.USER_ID, pass)){
					dispose();
					Diags.showMsg(Diags.SUCCESS);
				}
			}
		});
		
		setSize(300, 200);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
