package com.kabulbits.shoqa.util;

import java.awt.ComponentOrientation;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Diags 
{
	public static final int YN = JOptionPane.YES_NO_OPTION;
	public static final String SUCCESS = Dic.w("save_success");
	public static final String SAVE_CONF = Dic.w("save_conf");
	public static final String DEL_CONF = Dic.w("delete_conf");
	public static final String FK_ERROR = Dic.w("fk_error");
	public static final String ERROR = "An Error Was Occured!";
	private static JLabel msg = new JLabel();
	
	static{
		msg.setHorizontalAlignment(JLabel.RIGHT);
		msg.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	}
	
	public static void showMsg(String text)
	{
		msg.setText(text);
		JOptionPane.showMessageDialog(null, msg, null, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void showMsgLang(String key)
	{
		showMsg(Dic.w(key));
	}

	public static void showWarn(String key)
	{
		msg.setText(Dic.w(key));
		JOptionPane.showMessageDialog(null, msg, null, JOptionPane.WARNING_MESSAGE);
	}
	
	public static void showErr(String text)
	{
		msg.setText(text);
		JOptionPane.showMessageDialog(null, msg, null, JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showErrLang(String key)
	{
		showErr(Dic.w(key));
	}
	
	public static int showErrConf(String text, int options)
	{
		msg.setText(text);
		return JOptionPane.showConfirmDialog(null, msg, null, options, JOptionPane.ERROR_MESSAGE);
	}
	
	public static int showConf(String text, int options)
	{
		msg.setText(text);
		return JOptionPane.showConfirmDialog(null, msg, null, options);
	}
	
	public static int showConfLang(String key, int options)
	{
		return showConf(Dic.w(key), options);
	}
	
	public static int showOps(String key, String opts [])
	{
		msg.setText(Dic.w(key));
		String[] options = new String[opts.length];
		for(int i=0; i<opts.length; i++){
			options[i] = Dic.w(opts[i]);
		}
		return JOptionPane.showOptionDialog(null, msg, "", 0, JOptionPane.QUESTION_MESSAGE, null, options, null);
	}
}
