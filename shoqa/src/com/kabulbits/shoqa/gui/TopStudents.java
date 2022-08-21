package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.imgscalr.Scalr;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.OpenFrame;

public class TopStudents extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	private JRadioButton byPeriod, byGrade;
	private JComboBox<Integer> year, grade;
	private JComboBox<String> period;
	private JButton refresh;
	private DefaultListModel<Student> model;
	private JList<Student> list;
	
	private MarkData markData;
	
	public TopStudents(){
		
		setLayout(new BorderLayout(0, 5));
		
		markData = new MarkData();
		
		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(new TitledBorder(Dic.w("top_students")));
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(form, BorderLayout.NORTH);
		
		year = new JComboBox<>();
		grade = new JComboBox<>();
		for(int y=Data.EDUC_YEAR; y>=Data.START_YEAR; y--){
			year.addItem(y);
		}
		for(int i=1; i<13; i++){
			grade.addItem(i);
		}
		
		period = new JComboBox<String>();
		period.addItem(Dic.w("elementary"));
		period.addItem(Dic.w("intermediate"));
		period.addItem(Dic.w("highschool"));
		period.setEnabled(false);
		
		byGrade = new JRadioButton(Dic.w("by_grade"), true);
		byPeriod = new JRadioButton(Dic.w("by_period"));
		byGrade.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		byPeriod.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		ButtonGroup group = new ButtonGroup();
		group.add(byPeriod);
		group.add(byGrade);
		
		refresh = new JButton(new ImageIcon("images/refresh.png"));
		refresh.setPreferredSize(new Dimension(25, 23));
		
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(2, 2, 2, 2);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		
		cons.gridy = 0;
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.gridx = 0;
		
		form.add(byGrade, cons);
		cons.gridy++;
		form.add(byPeriod, cons);
		cons.gridy++;
		form.add(new JLabel(Dic.w("educ_year")), cons);
		
		cons.gridx = 2;
		form.add(refresh, cons);
		
		cons.gridy = 0;
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 1;
		cons.gridx = 1;
		
		cons.gridwidth = 2;
		form.add(grade, cons);
		cons.gridy++;
		form.add(period, cons);
		cons.gridy++;
		cons.gridwidth = 1;
		form.add(year, cons);
		
		model = new DefaultListModel<>();
		list = new JList<>(model);
		list.setOpaque(false);
		
		list.setCellRenderer(new ListCellRenderer<Student>() {
			public Component getListCellRendererComponent(
					JList<? extends Student> list, Student value, int index,
					boolean isSelected, boolean cellHasFocus) {
				ListItem item = new ListItem(value);
				if(isSelected) item.select();
				return item;
			}
		});
		JScrollPane pane = new JScrollPane(list)
		{
			private static final long serialVersionUID = 1L;
			{
				setOpaque(false);
				getViewport().setOpaque(false);
			}
			@Override
			protected void paintComponent(Graphics g) {
				try {
					BufferedImage buf = ImageIO.read(new File("images/background.png"));
					int pw = buf.getWidth();
					int ph = buf.getHeight();
					int w = getWidth();
					int h = getHeight();
					int x = (w - pw)/2;
					int y = (h - ph)/2;
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, w, h);
					g.drawImage(buf, x, y, pw, ph, this);
				} 
				catch (IOException e) {
					if(App.LOG){
						App.getLogger().error(e.getMessage(), e);
					}
				}
				super.paintComponent(g);
			}
		};
		pane.setBorder(new CompoundBorder(new EmptyBorder(0, 2, 0, 2), pane.getBorder()));
		pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(pane, BorderLayout.CENTER);
		
		if(Data.PERM_STUDENTS != 0){
			list.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						profile();
					}
				}
			});
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						profile();
					}
				}
			});
			byGrade.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(byGrade.isSelected()){
						grade.setEnabled(true);
						period.setEnabled(false);
					}else{
						grade.setEnabled(false);
						period.setEnabled(true);
					}
				}
			});
			refresh.addActionListener(this);
		}
		disableForm();
	}
	private void disableForm()
	{
		if(Data.PERM_STUDENTS == 0){
			year.setEnabled(false);
			grade.setEnabled(false);
			period.setEnabled(false);
			byPeriod.setEnabled(false);
			byGrade.setEnabled(false);
			refresh.setEnabled(false);
		}
	}
	private void profile(){
		int row = list.getSelectedIndex();
		if(row < 0) return;
		Student student = model.getElementAt(row);
		
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		OpenFrame.openFrame(StudentProfile.class, new Class[]{int.class}, new Object[]{student.id});
		setCursor(Cursor.getDefaultCursor());
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		render();
	}
	public void render(){
		int y = (int) year.getSelectedItem();
		int g = (int) grade.getSelectedItem();
		int p = period.getSelectedIndex() + 1;
		int n = byGrade.isSelected() ? g : p;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		model.clear();
		for(Student row : markData.topStudents(y, n, byGrade.isSelected())){
			model.addElement(row);
		}
		setCursor(Cursor.getDefaultCursor());
	}
	private class ListItem extends JPanel
	{
		private static final long serialVersionUID = 1L;
		JLabel stid, name, avg, photo;
		
		ListItem(Student student)
		{
			setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK), new EmptyBorder(4, 4, 4, 4)));
			setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			setLayout(new GridBagLayout());
			setOpaque(false);

			this.stid = new JLabel(String.format("<html>%s: <b>%d</b></html>", Dic.w("base_code"), student.id));
			this.name = new JLabel(String.format("<html>%s: <b>%s</b></html>", Dic.w("name"), student.name));
			this.avg = new JLabel(String.format("<html>%s: <b>%.2f</b></html>", Dic.w("average"), student.avg));
			this.photo = new JLabel();
			BufferedImage buf = student.bufImage;
			if(buf != null){
				int dim = 70;
				int width = buf.getWidth();
				int height =  buf.getHeight();
				if(height >= width){
					buf = Scalr.resize(buf, Scalr.Mode.FIT_TO_WIDTH, dim);
					int w = buf.getWidth();
					int h = buf.getHeight();
					buf = buf.getSubimage(0, (h-dim)/2, w, dim);
				}
				else if(width > height){
					buf = Scalr.resize(buf, Scalr.Mode.FIT_TO_HEIGHT, dim);
					int w = buf.getWidth();
					int h = buf.getHeight();
					buf = buf.getSubimage((w-dim)/2, 0, dim, h);
				}
				buf = Helper.circle(buf, 100);
				this.photo.setIcon(new ImageIcon(buf));
			}
			this.photo.setBorder(new EmptyBorder(0, 10, 0, 0));
			
			GridBagConstraints cons = new GridBagConstraints();
			cons.anchor = GridBagConstraints.BASELINE_LEADING;
			cons.gridx = 1;
			cons.weighty = 1;
			cons.weightx = 1;
			add(this.stid, cons);
			add(this.name, cons);
			add(this.avg, cons);
			cons.gridx = 0;
			cons.gridy = 0;
			cons.gridheight = 3;
			cons.weightx = 0;
			add(photo, cons);
		}
		public void select(){
			setOpaque(true);
			setBackground(new Color(56, 117, 180));
			stid.setForeground(Color.WHITE);
			name.setForeground(Color.WHITE);
			avg.setForeground(Color.WHITE);
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}




















