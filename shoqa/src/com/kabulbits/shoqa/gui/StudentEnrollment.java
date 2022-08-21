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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Ribbon;

public class StudentEnrollment extends JDialog{

	private static final long serialVersionUID = 1L;
	
	private JComboBox<Course> courses;
	private JButton save, cancel;
	private MarkData markData = new MarkData();
	
	private int stids[];
	private int cid;
	private int grade;
	private int task;
	
	private final int UPGRADE = 1;
	private final int CHANGE = 2;
	private final int ENROL = 3;

	public StudentEnrollment(int c, int ids[], int job) 
	{
		stids = ids;
		task = job;

		String title = "";
		int y = Data.EDUC_YEAR;

		save = new JButton(Dic.w("perform_operation"));
		cancel = new JButton(Dic.w("cancel"));

		switch (task){
		case UPGRADE:
			title = "upgrade_students";
			grade = c + 1;
			break;
		case CHANGE:
			title = "switch_course";
			Course cv = markData.findCourse(c);
			grade = cv.grade;
			y = cv.year;
			cid = c;
			break;
		case ENROL:
			title = "enrol_students";
			grade = c;
			break;
		}
		JPanel top = new Ribbon(Dic.w(title), false);
		JPanel center = new JPanel(new GridBagLayout());
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));

		add(top, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		center.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));

		courses = new JComboBox<>(markData.searchCourse(y, grade));
		courses.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) courses.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		JLabel tip = new JLabel(Dic.w("course"));

		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		cons.fill = GridBagConstraints.NONE;
		cons.insets = new Insets(5, 5, 5, 5);
		cons.gridy = 0;
		center.add(tip, cons);
		cons.weightx = 1;
		cons.fill = GridBagConstraints.HORIZONTAL;
		center.add(courses, cons);

		bottom.add(save);
		bottom.add(cancel);

		save.addActionListener(new BgWorker<Integer>(this){
			@Override
			protected Integer save(){
				return doSave();
			}
			@Override
			protected void finish(Integer result) {
				if(result > 0){
					dispose();
					Diags.showMsg(String.format(Dic.w("items_saved"), result));
				}
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		Helper.esc(this);

		setSize(350, 160);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setModal(true);
		setVisible(true);
	}

	private int doSave()
	{
		if (courses.getSelectedItem() == null) return 0;
		int count = 0;
		for (int i = 0; i < stids.length; i++){
			int nc = ((Course) courses.getSelectedItem()).id;
			switch (task){
			case 1:
				if (markData.upgradeStudent(stids[i], nc, grade)) count++;
				break;
			case 2:
				if (markData.switchStudent(stids[i], cid, nc)) count++;
				break;
			case 3:
				if (markData.enrolStudent(stids[i], nc, grade)) count++;
				break;
			}
		}
		return count;
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}
