package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.LibraryData;
import com.kabulbits.shoqa.util.BgWorker;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Helper;
import com.kabulbits.shoqa.util.Dic;
import com.kabulbits.shoqa.util.Reset;
import com.kabulbits.shoqa.util.Ribbon;

public class LibraryMemDetails extends JPanel {

	private static final long serialVersionUID = 1L;
	public LibraryMemProfile frame;
	
	private JTextField fields [];
	private JLabel pic;
	private JButton download, delete, save, refresh;
	
	private LibraryData data;
	private byte [] image = null;
	private int mid;

	public LibraryMemDetails(int id, Object info []){
		this(id);
		fillForm(info);
		if(Data.PERM_LIBRARY != 2){
			disableForm();
		}
	}
	
	public LibraryMemDetails(int id)
	{
		mid = id;
		data = new LibraryData();
		
		String title = Dic.w(id == 0 ? "external_member_reg" : "external_member_details");
		setLayout(new BorderLayout(0, 5));
		
		JPanel ribbon = new Ribbon(title, false);
		JPanel center = new JPanel(new BorderLayout(2, 0));
		JPanel form = new JPanel(new GridBagLayout());
		JPanel picPanel = new JPanel(new GridBagLayout());
		center.add(form, BorderLayout.CENTER);
		center.add(picPanel, BorderLayout.WEST);
		add(ribbon, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		
		String texts [] = {"code", "name", "lname", "fname", "phone", "job", "introducer", "job_at_school"};
		fields = new JTextField[texts.length];
		
		Reset reset = new Reset();
		ActionListener doSave = new BgWorker<Boolean>(this) {
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
		
		form.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(3, 3, 3, 3);
		cons.anchor = GridBagConstraints.BASELINE_LEADING;
		
		for(int i=0; i<fields.length; i++)
		{
			fields[i] = new JTextField();
			fields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			if(Data.PERM_LIBRARY == 2){
				fields[i].addActionListener(doSave);
			}
			fields[i].addFocusListener(reset);
			
			cons.gridy = i;
			
			cons.gridx = 0;
			cons.weightx = 0;
			cons.fill = GridBagConstraints.NONE;
			form.add(new JLabel(Dic.w(texts[i])), cons);
			
			cons.gridx = 1;
			cons.weightx = 1;
			cons.fill = GridBagConstraints.HORIZONTAL;
			if(i == fields.length - 1) cons.weighty = 1;
			form.add(fields[i], cons);
		}
		fields[0].setEditable(false);
		fields[0].setBackground(Color.WHITE);
		
		//-----------------Photo Panel--------------
		pic = new JLabel(new ImageIcon("images/pic.jpg"));
		pic.setPreferredSize(new Dimension(150, 200));
		pic.setBorder(new LineBorder(Color.BLACK));
		pic.setToolTipText(Dic.w("pic_tooltip"));
		pic.setBackground(Color.WHITE);
		
		download = new JButton(new ImageIcon("images/save.png"));
		delete = new JButton(new ImageIcon("images/cross.png"));
		download.setToolTipText(Dic.w("download_pic"));
		delete.setToolTipText(Dic.w("delete_pic"));
		
		GridBagConstraints cons1 = new GridBagConstraints();
		cons1.anchor = GridBagConstraints.NORTH;
		cons1.insets = new Insets(2, 2, 2, 2);
		cons1.gridx = 0;
		cons1.weighty = 0;
		cons1.gridwidth = 2;
		picPanel.add(pic, cons1);
		
		cons1.weighty = 1;
		cons1.gridwidth = 1;
		cons1.gridy = 1;
		cons1.weightx = 1;
		cons1.fill = GridBagConstraints.HORIZONTAL;
		picPanel.add(download, cons1);
		cons1.gridx = 1;
		picPanel.add(delete, cons1);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		add(bottom, BorderLayout.SOUTH);
		
		save = new JButton(Dic.w("save"));
		bottom.add(save);
		
		if(mid != 0){
			refresh = new JButton(new ImageIcon("images/refresh.png"));
			refresh.setToolTipText(Dic.w("refresh"));
			refresh.setMargin(new Insets(2, 3, 2, 3));
			bottom.add(refresh);
			
			refresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Object info [] = data.findMember(mid);
					if(info == null){
						LibraryMemProfile.isOpen = false;
						frame.dispose();
					}else{
						fillForm(info);
					}
				}
			});
		}
		if(Data.PERM_LIBRARY == 2){
			pic.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					addImage();
				}
			});
			download.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					download();
				}
			});
			delete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteImg();
				}
			});
			save.addActionListener(doSave);
		}
	}
	
	private void disableForm()
	{
		for(JTextField field : fields){
			if(field != null){
				field.setEditable(false);
			}
		}
		download.setEnabled(false);
		delete.setEnabled(false);
		save.setEnabled(false);
		if(refresh != null){
			refresh.setEnabled(false);
		}
	}
	
	protected void deleteImg() {
		this.image = null;
		pic.setIcon(new ImageIcon("images/pic.jpg"));
	}
	
	private void addImage()
	{
		String path = Helper.pickPhoto(frame);
		if(path == null) return;
		image = Helper.photo(path);
		if(image != null){
			pic.setIcon(new ImageIcon(image));
		}
	}
	
	protected void download() 
	{
		if(image == null) {
			return;
		}
		FileDialog diag = new FileDialog(frame, "Save", FileDialog.SAVE);
		diag.setFile(String.format("mem-%d.jpg", mid));
		diag.setVisible(true);
		String fn = diag.getFile();
		if(fn == null) return;
		if(!fn.toLowerCase().endsWith(".jpg")){
			fn += ".jpg";
		}
		String path = diag.getDirectory() + fn;
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(image);
			fos.close();
		} 
		catch (Exception e) {
			Diags.showErrLang("cant_save_file");
		}
	}
	
	private boolean doSave()
	{
		if(!validator()) return false;
		Object info [] = new Object[fields.length];
		info[0] = image;
		for(int i=1; i<fields.length; i++){
			info[i] = fields[i].getText().trim();
		}
		if(mid == 0){
			return data.insertMember(info);
		}else{
			return data.editMember(mid, info);
		}
	}
	private void afterSave()
	{
		Diags.showMsg(Diags.SUCCESS);
		if(mid == 0){
			for(JTextField field : fields){
				field.setText("");
			}
			deleteImg();
		}
	}

	
	private void fillForm(Object[] info) 
	{
		for(int i=1; i<fields.length; i++){
			fields[i].setText(info[i].toString());
		}
		fields[0].setText(String.valueOf(mid));
		this.image = (byte []) info[0];
		if(image != null){
			pic.setIcon(new ImageIcon(this.image));
		}else{
			pic.setIcon(new ImageIcon("images/pic.jpg"));
		}
	}
	
	private int required [] = {1, 3, 4, 6};
	
	private boolean validator() 
	{
		for(int index : required){
			if(fields[index].getText().trim().length() == 0)
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





























