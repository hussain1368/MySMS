package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Option;
import com.kabulbits.shoqa.db.UserData;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JTextField username;
	private JPasswordField password;
	private JComboBox<Integer> educYear;
	private JComboBox<Option> financePeriod;
	private JComboBox<String> language;
	
	private UserData data;
	private int failCount = 5;
	private int CURR_FINAN_PRD;
	private int CURR_EDUC_YEAR;
	
	private File configFile;
	private Properties config;
	
	public Login()
	{
		loadLang();
		data = new UserData(false);
		if(!data.setupConn()){
			System.exit(0);
		}
		
		JPanel form = new JPanel(new GridBagLayout());
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(form, BorderLayout.CENTER);
		
		JLabel kabul = new JLabel(new ImageIcon("images/kb.jpg"));
		kabul.setBorder(new MatteBorder(0, 1, 0, 0, Color.BLACK));
		add(kabul, BorderLayout.EAST);
		
		username = new JTextField();
		password = new JPasswordField();

		FocusListener reset = new Reset();
		username.addFocusListener(reset);
		password.addFocusListener(reset);
		
		educYear = new JComboBox<Integer>();
		financePeriod = new JComboBox<>();
		language = new JComboBox<>();
		language.addItem(Dic.w("farsi"));
		language.addItem(Dic.w("pashto"));
		if(config.getProperty("language").equals("pashto")){
			language.setSelectedIndex(1);
		}
		
		data.loadConfigs();
		int y = Calendar.getInstance(new ULocale("@calendar=persian")).get(Calendar.YEAR) + 1;
		for(int i = y; i >=Data.START_YEAR; i--){
			educYear.addItem(i);
		}
		CURR_EDUC_YEAR = Data.EDUC_YEAR;
		educYear.setSelectedItem(CURR_EDUC_YEAR);
		
		Vector<Option> options = data.financePeriods();
		for(Option option : options){
			financePeriod.addItem(option);
		}
		CURR_FINAN_PRD = Data.FINAN_PRD;
		financePeriod.setSelectedItem(new Option(CURR_FINAN_PRD));
		
		JButton login, cancel;
		login = new JButton(Dic.w("login"), new ImageIcon("images/login.png"));
		cancel = new JButton(Dic.w("cancel"), new ImageIcon("images/cancel.png"));
		login.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		cancel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		login.setIconTextGap(10);
		cancel.setIconTextGap(10);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(2, 5, 2, 5);
		
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.weighty = 0;
		cons.gridx = 0;
		cons.gridy = 0;
		
		form.add(new JLabel(Dic.w("user_name")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("password")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("educ_year")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("financial_period")), cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("language")), cons);
		
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.weighty = 0;
		cons.gridwidth = 2;
		cons.gridx = 1;
		cons.gridy = 0;
		
		form.add(username, cons);
		cons.gridy++;
		form.add(password, cons);
		cons.gridy++;
		form.add(educYear, cons);
		cons.gridy++;
		form.add(financePeriod, cons);
		cons.gridy++;
		form.add(language, cons);
		
		cons.gridy++;
//		cons.fill = GridBagConstraints.NONE;
		cons.anchor = GridBagConstraints.SOUTH;
//		cons.weightx = 0;
		cons.weighty = 1;
		cons.gridwidth = 1;
		
		form.add(login, cons);
		cons.gridx++;
		form.add(cancel, cons);

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		ActionListener doLogin = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login();
			}
		};
		login.addActionListener(doLogin);
		
		setIconImage(new ImageIcon("images/icon.png").getImage());
		setSize(480, 260);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
//	private String ipPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private void login() 
	{
		String user = username.getText().trim().toLowerCase();
		String pass = String.valueOf(password.getPassword());
		if(user.length() == 0){
			username.setBackground(Color.RED);
			return;
		}
		if(pass.length() == 0){
			password.setBackground(Color.RED);
			return;
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if(data.authenticate(user, pass)){
			if(saveStartupConfigs()){
				saveLang();
				new Main().pack();
				dispose();
			}
		}else{
			Diags.showErrLang("username_or_password_is_wrong");
			if(failCount > 0){
				username.grabFocus();
				failCount--;
			}else{
				System.exit(0);
			}
		}
		setCursor(Cursor.getDefaultCursor());
	}
	
	private boolean saveStartupConfigs()
	{
		int year = (int) educYear.getSelectedItem();
		int period = ((Option)financePeriod.getSelectedItem()).key;
		boolean changeState = false;
		
		if(year != CURR_EDUC_YEAR){
			if(Data.PERM_STUDENTS != 2 || Data.PERM_COURSES != 2){
				Diags.showErrLang("educ_year_change_error");
				return false;
			}
			changeState = true;
		}
		if(period != CURR_FINAN_PRD){
			if(Data.PERM_FINANCE != 2){
				Diags.showErrLang("finan_period_change_error");
				return false;
			}
			changeState = true;
		}
		if(changeState){
			data.setEducYear(year);
			data.setCurrentFinanPeriod(period);
		}
		return true;
	}
	
	private boolean saveLang()
	{
		try{
			String lang = language.getSelectedIndex() == 0 ? "farsi" : "pashto";
			config.setProperty("language", lang);
			OutputStream output = new FileOutputStream(configFile);
			config.store(output, "App Configs");
			Dic.loadLang(lang);
			output.close();
		}
		catch(Exception e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}
	
	private void loadLang()
	{
		try{
			configFile = new File("resources/config.properties");
			InputStream input = new FileInputStream(configFile);
			config = new Properties();
			config.load(input);
			if (!Dic.loadLang(config.getProperty("language"))){
				System.exit(0);
			}
		}
		catch(Exception e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
			System.exit(0);
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

























