package myconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JTextField[] fields;
	private JButton save, test;
	private boolean CLIENT = true;
	private String URL = "jdbc:mysql://%s:%s?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";

	public Main()
	{
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			Font font = new Font("tahoma", Font.PLAIN, 11);
			UIManager.put("ToolTip.font", font);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		setTitle("MySQL Configuration");
		setLayout(new BorderLayout(5, 0));
		
		JPanel form = new JPanel(new GridBagLayout());
		add(form, BorderLayout.CENTER);
		add(new JLabel(new ImageIcon("images/mysql_big.png")), BorderLayout.WEST);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 4, 4, 4);
		
		String[] texts = {"Host:", "Port:", "Username:", "Password:"};
		fields = new JTextField[texts.length];
		
		FocusListener focus = new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				JTextField field = (JTextField) e.getComponent();
				field.setBackground(Color.WHITE);
				field.selectAll();
			}
		};
		for(int i=0; i<fields.length; i++)
		{
			fields[i] = new JTextField();
			fields[i].addFocusListener(focus);
			cons.gridy = i;
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.gridwidth = 1;
			cons.fill = GridBagConstraints.NONE;
			form.add(new JLabel(texts[i]), cons);
			
			cons.gridx = 1;
			cons.weightx = 1;
			cons.gridwidth = 2;
			cons.fill = GridBagConstraints.HORIZONTAL;
			form.add(fields[i], cons);
		}
		fields[0].setEditable(CLIENT);
		test = new JButton("Test");
		save = new JButton("Save");
		
		cons.gridy++;
		cons.gridwidth = 1;
		
		cons.gridx = 1;
		form.add(test, cons);
		cons.gridx = 2;
		form.add(save, cons);
		
		load();
		
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if(valid()){
					if(save()){
						JOptionPane.showMessageDialog(Main.this, "Configurations were saved!");
					}
				}
				Main.this.setCursor(Cursor.getDefaultCursor());
			}
		});
		test.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				Main.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if(valid()){
					if(test()){
						JOptionPane.showMessageDialog(Main.this, "Connection was successful!");
					}else{
						JOptionPane.showMessageDialog(Main.this, "Can not connect to MySQL!", "", JOptionPane.ERROR_MESSAGE);
					}
				}
				Main.this.setCursor(Cursor.getDefaultCursor());
			}
		});
		
		setIconImage(new ImageIcon("images/mysql.png").getImage());
		setSize(400, 190);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	Properties props = new Properties();
	File file = new File("resources/connection.properties");
	
	private void load()
	{
		try{
			props.load(new FileInputStream(file));
			fields[0].setText(props.getProperty("hostname"));
			fields[1].setText(props.getProperty("port"));
			fields[2].setText(props.getProperty("username"));
			fields[3].setText(props.getProperty("password"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private boolean valid()
	{
		for(JTextField field : fields){
			if(field.getText().trim().length() == 0){
				field.setBackground(Color.RED);
				return false;
			}
		}
		return true;
	}
	
	private boolean save()
	{
		try{
			props.setProperty("hostname", fields[0].getText().trim());
			props.setProperty("port", fields[1].getText().trim());
			props.setProperty("username", fields[2].getText().trim());
			props.setProperty("password", fields[3].getText().trim());
			props.store(new FileOutputStream(file), "Connection Settings");
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			return false;
		}
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean test()
	{
		String host = fields[0].getText().trim();
		String port = fields[1].getText().trim();
		String user = fields[2].getText().trim();
		String pass = fields[3].getText().trim();
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			String url = String.format(URL, host, port);
			Connection conn = DriverManager.getConnection(url, user, pass);
			return conn.isValid(5);
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		catch (SQLException e){
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				new Main();
			}
		});
	}
}




















