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
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class Settings extends JFrame {

	private static final long serialVersionUID = 1L;
	public static boolean isOpen = false;
	public static Settings self;
	
	private JTextField fields [];
	private JSpinner spins [];
	private JSpinner startYear;
	private JButton refresh, save, browse1, browse2;
	
	private Data data;
	
	public Settings()
	{
		isOpen = true;
		self = this;
		
		data = new Data();
		
		String title = Dic.w("settings");
		setTitle(title);
		
		JPanel ribbon = new Ribbon(title, true);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new GridLayout(1,2));
		JPanel center1 = new JPanel(new GridBagLayout());
		JPanel center2 = new JPanel(new GridBagLayout());
		center.add(center2);
		center.add(center1);
		add(center, BorderLayout.CENTER);
		center.setBorder(new EmptyBorder(0, 5, 0, 5));
		
		center1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		center2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		Border line = new LineBorder(Color.GRAY);
		center1.setBorder(new TitledBorder(line, Dic.w("general_settings")));
		center2.setBorder(new TitledBorder(line, Dic.w("educ_year_settings")));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(4, 2, 0, 2);
		
		String labels1 [] = {"school_name", "ministry_name", "head_name", "administer_name", "school_logo", "ministry_logo"};
		fields = new JTextField[labels1.length];

		FocusListener reset = new Reset();
		for(int i=0; i<labels1.length; i++)
		{
			cons.gridy = i;
			cons.weightx = 0;
			cons.gridwidth = 1;
			cons.fill = GridBagConstraints.NONE;
			center1.add(new JLabel(Dic.w(labels1[i])), cons);
			fields[i] = new JTextField();
			fields[i].addFocusListener(reset);
			if(i < 4) fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			cons.gridwidth = i < 4 ? 2 : 1;
			center1.add(fields[i], cons);
		}
		fields[4].setEditable(false);
		fields[5].setEditable(false);
		
		browse1 = new JButton("...");
		browse2 = new JButton("...");
		Dimension dim = new Dimension(25, 20);
		browse1.setPreferredSize(dim);
		browse2.setPreferredSize(dim);
		
		cons.weightx = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.gridx = 2;
		cons.gridy = 4;
		center1.add(browse1, cons);
		cons.gridy++;
		center1.add(browse2, cons);
		
		startYear = new JSpinner(new SpinnerNumberModel(1390, 1380, 1390, 1));
		startYear.setEditor(new NumberEditor(startYear, "#"));
		
		cons.gridy++;
		cons.weighty = 1;
		
		cons.gridx = 0;
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		center1.add(new JLabel(Dic.w("starting_year")), cons);
		
		cons.gridx = 1;
		cons.weightx = 1;
		cons.gridwidth = 2;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center1.add(startYear, cons);
		
		String labels2 [] = {"midterm_full_grade", "midterm_pass_grade", "final_pass_grade", "fail_subj_count", "midterm_attendance", "final_attendance"};
		spins = new JSpinner[labels2.length];
		
		cons.gridwidth = 1;
		for(int i=0; i<labels2.length; i++)
		{
			cons.gridy = i;
			cons.weighty = i == 5 ? 1 : 0;
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			center2.add(new JLabel(Dic.w(labels2[i])), cons);
			spins[i] = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
			cons.gridx = 1;
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			center2.add(spins[i], cons);
		}
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bleft.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		
		bottom.add(bright, BorderLayout.EAST);
		bottom.add(bleft, BorderLayout.WEST);
		add(bottom, BorderLayout.SOUTH);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		save = new JButton(Dic.w("save"));
		
		bright.add(save);
		bright.add(refresh);
		
		if(Data.PERM_SETTINGS == 2){
			browse1.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					fields[4].setText(Helper.pickPhoto(self));
				}
			});
			browse2.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					fields[5].setText(Helper.pickPhoto(self));
				}
			});
			refresh.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e){
					render();
				}
			});
			save.addActionListener(new BgWorker<Boolean>(this){
				@Override
				protected Boolean save(){
					return doSave();
				}
				@Override
				protected void finish(Boolean result){
					if(result){
						Diags.showMsg(Diags.SUCCESS);
						data.loadConfigs();
					}
				}
			});
		}
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				isOpen = false;
			}
		});
		render();
		disableForm();
		
		setMinimumSize(new Dimension(600, 350));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void disableForm()
	{
		if(Data.PERM_SETTINGS != 2){
			for(JTextField field : fields){
				if(field != null){
					field.setEditable(false);
				}
			}
			for(JSpinner spin : spins){
				if(spin != null){
					spin.setEnabled(false);
				}
			}
			startYear.setEnabled(false);
			refresh.setEnabled(false);
			save.setEnabled(false);
			browse1.setEnabled(false);
			browse2.setEnabled(false);
		}
	}
	
	private void render()
	{
		int values [] = data.getYearSettings();
		if(values != null){
			for(int i=0; i<values.length; i++){
				spins[i].setValue(values[i]);
			}
		}
		String values1 [] = data.getGeneralSettings();
		if(values1 != null){
			for(int i=0; i<4; i++){
				fields[i].setText(values1[i]);
			}
		}
		try{
			int year = Integer.parseInt(values1[4]);
			startYear.setValue(year);
		} 
		catch(NumberFormatException e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		fields[4].setText("");
		fields[5].setText("");
	}
	
	private boolean doSave()
	{
		int values [] = new int[spins.length];
		for(int i=0; i<values.length; i++){
			values[i] = (int) spins[i].getValue();
		}
		boolean done = true;
		if(!data.saveYearSettings(values)) done = false;
		
		String values2 [] = new String[5];
		for(int i=0; i<4; i++){
			values2[i] = fields[i].getText();
		}
		values2[4] = startYear.getValue().toString();
		
		if(!data.saveGeneralSettings(values2)) done = false;
		
		try{
			if(fields[4].getText().length() != 0)
			{
				byte[] image = Helper.photo(fields[4].getText(), 200, 200);
				OutputStream out = new FileOutputStream(new File("images/school_logo.jpg"));
				out.write(image);
				out.close();
			}
			if(fields[5].getText().length() != 0)
			{
				byte[] image = Helper.photo(fields[5].getText(), 200, 200);
				OutputStream out = new FileOutputStream(new File("images/ministry_logo.jpg"));
				out.write(image);
				out.close();
			}
		} 
		catch (FileNotFoundException e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		} 
		catch (IOException e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		return done;
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}































