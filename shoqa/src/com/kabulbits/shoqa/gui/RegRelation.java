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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.PDateModel;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class RegRelation extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public static boolean isOpen = false;
	public static RegRelation self;
	public StudentRelations parent;
	
	private JTextField [] fields;
	private JSpinner birthDate;
	private JComboBox<String> relation;
	
	private StudentData data;
	private int stid;
	private int pid;
	
	public RegRelation(int id, int sid, StudentRelations parent)
	{
		isOpen = true;
		self = this;
		this.parent = parent;
		data = new StudentData();
		
		this.pid = id;
		this.stid = sid;
		
		String title = Dic.w("reg_relation");
		JPanel ribbon = new Ribbon(title, true);
		add(ribbon, BorderLayout.NORTH);
		
		JPanel center = new JPanel(new GridBagLayout());
		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		center.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY), new EmptyBorder(3, 3, 3, 3)));
		GridBagConstraints cons = new GridBagConstraints();
		add(center, BorderLayout.CENTER);
		
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.insets = new Insets(3, 3, 3, 3);
		cons.weighty = 0;
		
		fields = new JTextField [11];
		String texts [] = {"code", "name", "lname", "language", "birth_date", "edu_level",
				"edu_field", "job", "living_location", "job_location", "phone"};
		
		birthDate = new JSpinner();
		birthDate.setModel(new PDateModel(birthDate));
		
		ActionListener listener = new BgWorker<Boolean>(this) {
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
		};
		cons.gridx = 1;
		cons.gridy = 4;
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		
		center.add(birthDate, cons);
		
		Reset reset = new Reset();
		cons.gridy = 0;
		for(int i=0; i<fields.length; i++)
		{
			fields[i] = new JTextField();
			fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			fields[i].addFocusListener(reset);
			fields[i].addActionListener(listener);
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			
			center.add(new JLabel(Dic.w(texts[i])), cons);
			
			if(i == fields.length-1 && stid == 0){
				cons.weighty = 1;
			}
			
			cons.gridx = 1;
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			
			if(i != 4){
				center.add(fields[i], cons);
			}
			
			cons.gridy++;
		}
		fields[0].setEditable(false);
		
		String [] items = {
				Dic.w("father"),
				Dic.w("mother"),
				Dic.w("brother"),
				Dic.w("sister"),
				Dic.w("uncle"),
				Dic.w("uncle2"),
		};
		if(stid != 0){
			JLabel rlabel = new JLabel(Dic.w("relation"));
			relation = new JComboBox<String>(items);
			relation.setPrototypeDisplayValue("xxxxxxxxxxxx");
			relation.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			((JLabel)relation.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
			
			cons.weighty = 1;
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			center.add(rlabel, cons);
			
			cons.gridx = 1;
			cons.weightx = 1;
			
			center.add(relation, cons);
		}
		if(pid != 0){
			fillForm();
		}
		
		JPanel bottom = new JPanel(new BorderLayout());
		JPanel bright = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel bleft = new JPanel(new FlowLayout(FlowLayout.LEFT));
		bottom.add(bleft, BorderLayout.WEST);
		bottom.add(bright, BorderLayout.EAST);
		add(bottom, BorderLayout.SOUTH);
		
		JButton save = new JButton(Dic.w("save"));
		JButton refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setToolTipText(Dic.w("refresh"));
		refresh.setMargin(new Insets(2, 3, 2, 3));
		
		bright.add(save);
		bright.add(refresh);
		
		save.addActionListener(listener);
		
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillForm();
			}
		});
		Helper.esc(this);
		
		setSize(400, 450);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModal(true);
		setVisible(true);
	}

	private void fillForm() 
	{
		Object info [] = data.findRelation(pid);
		if(info == null) {
			dispose();
			return;
		}
		for(int i=0; i< info.length; i++) {
			fields[i].setText(info[i].toString());
		}
		birthDate.setValue((Date)info[4]);
		if(stid != 0){
			String rel = data.findStudentRel(stid, pid);
			String relations [] = {"father", "mother", "brother", "sister", "uncle", "uncle2"};
			for(int i=0; i<relations.length; i++) {
				if(relations[i].equals(rel)){
					relation.setSelectedIndex(i);
				}
			}
		}
	}

	private boolean doSave() 
	{
		if(!valid()) return false;
		
		String values [] = new String[10];
		for(int i=1; i<fields.length; i++) {
			values[i-1] = fields[i].getText().trim();
		}
		Date date = (Date)birthDate.getValue();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		values[3] = df.format(date);
		String relations [] = {"father", "mother", "brother", "sister", "uncle", "uncle2"};
		
		if(pid != 0){
			if(data.editRelation(values, pid)){
				if(stid != 0){
					String rel = relations[relation.getSelectedIndex()];
					return data.setStudentRel(stid, pid, rel);
				}
				return true;
			}
		}else{
			String rel = relations[relation.getSelectedIndex()];
			return data.insertRelation(values, stid, rel);
		}
		return false;
	}
	private void afterSave()
	{
		dispose();
		Diags.showMsg(Diags.SUCCESS);
		if(parent != null){
			parent.render();
		}
	}

	int required [] = {1, 5, 10};
	private boolean valid()
	{
		for(int index : required)
		{
			if(fields[index].getText().trim().length() < 1)
			{
				fields[index].setBackground(Color.RED);
				Diags.showErrLang("required_error");
				return false;
			}
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
