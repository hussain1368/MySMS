package com.kabulbits.shoqa.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class OpenFrame implements ActionListener {
	
	private Class<?> frame;
	private Class<?> [] types;
	private Object[] params;
	private Component comp;

	public OpenFrame(Class<?> frame, Component comp) {
		this.frame = frame;
		this.comp = comp;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		openFrame(this.frame, this.types, this.params, this.comp);
	}
	
	public static JFrame openFrame(Class<?> frame, Class<?> [] types, Object [] params)
	{
		return openFrame(frame, types, params, null);
	}
	
	public static JFrame openFrame(Class<?> frame, Class<?> [] types, Object [] params, Component comp)
	{
		if(comp != null){
			comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		JFrame jFrame = null;
		try{
			boolean isOpen = frame.getField("isOpen").getBoolean(frame.getField("isOpen"));
			if(!isOpen){
				jFrame = (JFrame) frame.getConstructor(types).newInstance(params);
			}else{
				jFrame = (JFrame) frame.getField("self").get(frame.getField("self"));
				jFrame.setExtendedState(JFrame.NORMAL);
				jFrame.requestFocus();
			} 
		} 
		catch (Exception e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		if(comp != null){
			comp.setCursor(Cursor.getDefaultCursor());
		}
		return jFrame;
	}
}