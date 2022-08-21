package com.kabulbits.shoqa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
 
public class Dic {
	
	private static Properties prop;
	
	public static boolean loadLang(String language)
	{
		if(prop == null){
			prop = new Properties();
		}
		String fullpath = "resources/" + language + ".properties";
		 
		try {
			InputStream input = new FileInputStream(new File(fullpath));
			prop.load(input);
		} 
		catch (FileNotFoundException e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
			return false;
		} 
		catch (IOException e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}
 
	public static String w(String key) 
	{
		if(prop == null){
			loadLang("farsi");
		}
		return prop.getProperty(key);
	}
}
