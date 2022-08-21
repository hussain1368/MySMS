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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.kabulbits.shoqa.db.SubjectData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class RegSubject extends JDialog{

	private static final long serialVersionUID = 1L;
	public Subjects parent;
	
	private JTextField name;
	private JCheckBox [] grades;
	private JCheckBox allgrades;
	private JButton save;
	
	private SubjectData data;
	private int subid, position;
	private boolean [] oldgrade;
	private boolean go = true;
	
	public RegSubject(int id, int pos, Subjects parent)
	{
		String title = Dic.w(id == 0 ? "new_subject" : "edit_subject");
		setTitle(title);
		
		this.subid = id;
		this.position = pos;
		this.parent = parent;
		this.data = new SubjectData();
		
		JPanel top = new Ribbon(title, false);
		JPanel center = new JPanel(new BorderLayout());
		JPanel npanel = new JPanel(new GridBagLayout());
		JPanel gpanel = new JPanel(new GridLayout(3, 4, 20, 20));
		JPanel bottom = new JPanel(new FlowLayout());
		
		center.add(gpanel, BorderLayout.CENTER);
		center.add(npanel, BorderLayout.NORTH);
		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		
		npanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		name = new JTextField();
		allgrades = new JCheckBox(Dic.w("all_grades"));
		allgrades.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		name.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		
		npanel.add(new JLabel(Dic.w("subject_name")), cons);
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		npanel.add(name, cons);
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		npanel.add(allgrades, cons);
		
		gpanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		gpanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 1, true), Dic.w("involved_in_grades")));
		grades = new JCheckBox [12];
		String labels [] = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth"};
		for(int i=0; i<grades.length; i++){
			grades[i] = new JCheckBox(Dic.w(labels[i]));
			grades[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			gpanel.add(grades[i]);
		}
		
		save = new JButton(Dic.w("save"), new ImageIcon("images/save.png"));
		save.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		save.setMargin(new Insets(2, 5, 2, 5));
		save.setEnabled(id != 0);
		bottom.add(save);
		
		save.addActionListener(new BgWorker<Boolean>(this){
			@Override
			protected Boolean save() {
				return doSave();
			}
			@Override
			protected void finish(Boolean result){
				if(result){
					dispose();
					Diags.showMsg(Diags.SUCCESS);
					if(RegSubject.this.parent != null){
						RegSubject.this.parent.render();
					}
				}
			}
		});
		name.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent arg0) {
				save.setEnabled(check() && name.getText().trim().length() > 0);
			}
		});
		allgrades.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				go = false;
				if(allgrades.isSelected()){
					for(JCheckBox box : grades){
						box.setSelected(true);
					}
					save.setEnabled(name.getText().trim().length() > 0);
				}else{
					for(JCheckBox box : grades){
						box.setSelected(false);
					}
					save.setEnabled(false);
				}
				go = true;
			}
		});
		for(JCheckBox box : grades){
			box.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					if(go){
						save.setEnabled(check() && name.getText().trim().length() > 0);
					}
				}
			});
		}
		Helper.esc(this);
		
		if(id != 0){
			name.setText(data.subjectName(id));
			oldgrade = data.subjectGrades(id);
			for(int i=0; i<oldgrade.length; i++){
				grades[i].setSelected(oldgrade[i]);
			}
		}
		setMinimumSize(new Dimension(350, 250));
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);
	}
	
	private boolean check(){
		for(JCheckBox box : grades){
			if(box.isSelected()){
				return true;
			}
		}
		return false;
	}

	private boolean doSave()
	{
		if(subid != 0){
			if(Diags.showConfLang("edit_subj_conf", Diags.YN) != 0) return false;
		}
		String subname = name.getText().trim();
		if(subid == 0)
		{
			Vector<Integer> thegrades = new Vector<>();
			for(int i=0; i<grades.length; i++){
				if(grades[i].isSelected()){
					thegrades.add(i+1);
				}
			}
			return data.addSubject(subname, position, thegrades);
		}
		else
		{
			boolean newgrades [] = new boolean[12];
			for(int i=0; i<grades.length; i++){
				newgrades[i] = grades[i].isSelected();
			}
			return data.editSubject(subname, subid, oldgrade, newgrades);
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
