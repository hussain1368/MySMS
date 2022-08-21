package com.kabulbits.shoqa.util;

import java.awt.Color;
import java.awt.Font;
import java.util.Calendar;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.gui.Login;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;

public class App{
//	public static boolean SERVER = true;
	public static boolean BETA = true;
	public static boolean TRIAL = true;
	public static boolean LOG = false;
	
	public static void main(String args[])
	{
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			Font font = new Font("tahoma", Font.PLAIN, 11);
			UIManager.put("ToolTip.font", font);
			UIManager.put("OptionPane.buttonFont", font);
			UIManager.put("Viewport.background", Color.WHITE);
		}
		catch (Exception e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		if(BETA){
			if(trialExpired()){
				Diags.showErr("\u0645\u062A\u0627\u0633\u0641\u0627\u0646\u0647 \u062F\u0648\u0631\u0647 \u0622\u0632\u0645\u0627\u06CC\u0634\u06CC \u0634\u0645\u0627 \u0628\u0647 \u067E\u0627\u06CC\u0627\u0646 \u0631\u0633\u06CC\u062F\u0647 \u0627\u0633\u062A!");
				System.exit(0);
			}
		}
		if(Data.isTrial()){
			Diags.showMsg("\u062A\u0648\u062C\u0647: \u0627\u06CC\u0646 \u0646\u0633\u062E\u0647 \u0622\u0632\u0645\u0627\u06CC\u0634\u06CC \u0646\u0631\u0645 \u0627\u0641\u0632\u0627\u0631 \u0645\u06CC \u0628\u0627\u0634\u062F \u0648 \u062F\u0627\u0631\u0627\u06CC \u0645\u062D\u062F\u0648\u062F\u06CC\u062A \u0627\u0633\u062A. \u0628\u0631\u0627\u06CC \u0627\u0633\u062A\u0641\u0627\u062F\u0647 \u0628\u062F\u0648\u0646 \u0645\u062D\u062F\u0648\u062F\u06CC\u062A \u0646\u0633\u062E\u0647 \u0627\u0635\u0644\u06CC \u0631\u0627 \u062A\u0647\u06CC\u0647 \u0646\u0645\u0627\u06CC\u06CC\u062F!");
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Login();
			}
		});
	}
	private static int threshold = 100;
	private static boolean trialExpired()
	{
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.YEAR) > 2015 && cal.get(Calendar.MONTH) > 1){
			return true;
		}
		if(checkKey(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\NYDC")){
			return true;
		}
		return checkKey(WinReg.HKEY_CURRENT_USER, "Software\\CR7\\CS");
	}
	private static boolean checkKey(HKEY key, String path)
	{
		// Create key if not exists
		if(!Advapi32Util.registryKeyExists(key, path)){
			Advapi32Util.registryCreateKey(key, path);
		}
		// Create value if not exists
		if(!Advapi32Util.registryValueExists(key, path, "count")){
			Advapi32Util.registrySetIntValue(key, path, "count", 0);
		}
		// Get the value
		int value = Advapi32Util.registryGetIntValue(key, path, "count");
		if(value > threshold){
			return true;
		}else{
			value++;
			Advapi32Util.registrySetIntValue(key, path, "count", value);
		}
		return false;
	}
	public static Logger logger;
	public static Logger getLogger()
	{
		if(logger == null){
			logger = Logger.getLogger(App.class);
			PropertyConfigurator.configure("resources/log4j.properties");
		}
		return logger;
	}
}






















