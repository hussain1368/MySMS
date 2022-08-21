package com.kabulbits.shoqa.util;

import java.text.ParseException;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

public class PDateModel implements SpinnerModel
{
	public JSpinner spin;
	private JFormattedTextField ftf;
	private DateFormat df;
	private Calendar cal;
	
	public PDateModel(JSpinner spin)
	{
		this.spin = spin;
		
		ULocale locale = new ULocale("@calendar=persian");
		cal = Calendar.getInstance(locale);
		df = new SimpleDateFormat("yyyy/MM/dd", locale);
		
		ftf = new JFormattedTextField(new JFormattedTextField.AbstractFormatter() 
		{
			private static final long serialVersionUID = 1L;

			public String valueToString(Object value) throws ParseException 
			{
				if(value instanceof Date)
				{
					try{
						cal.setTime((Date) value);
						return df.format(cal.getTime());
					}
					catch (Exception e){
						if(App.LOG){
							App.getLogger().error(e.getMessage(), e);
						}
					}
				}
				return null;
			}
			public Object stringToValue(String text) throws ParseException 
			{
				try {
					cal.setTime(df.parse(text));
					return cal.getTime();
				} 
				catch (ParseException e) {
					return null;
				}
			}
		});
		
		ftf.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				try {
					cal.setTime(df.parse(ftf.getText()));
				} 
				catch (ParseException e1) {}
			}
		});
		ftf.setValue(cal.getTime());
		spin.setEditor(ftf);
	}

	public void setValue(Object value) 
	{
		if(value == null){
			ftf.setValue(null);
			return;
		}
		int pos = ftf.getCaretPosition();
		
		cal.setTime((Date)value);
		ftf.setValue(cal.getTime());
		
		if(pos >= 0 && pos < 5){
			ftf.select(0, 4);
		}
		else if(pos > 5 && pos < 8){
			ftf.select(5, 7);
		}
		else if(pos > 8){
			ftf.select(8, 10);
		}
	}
	
	public Object getValue() 
	{
		try{
			ftf.commitEdit();
		}
		catch(ParseException e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		return ftf.getValue();
	}

	public Object getPreviousValue() 
	{
		int pos = ftf.getCaretPosition();
		
		if(pos >= 0 && pos < 5){
			cal.add(Calendar.YEAR, -1);
		}
		else if(pos > 5 && pos < 8){
			cal.add(Calendar.MONTH, -1);
		}
		else if(pos > 8){
			cal.add(Calendar.DATE, -1);
		}
		return cal.getTime();
	}
	
	public Object getNextValue() 
	{
		int pos = ftf.getCaretPosition();
		
		if(pos >= 0 && pos < 5){
			cal.add(Calendar.YEAR, 1);
		}
		else if(pos > 5 && pos < 8){
			cal.add(Calendar.MONTH, 1);
		}
		else if(pos > 8){
			cal.add(Calendar.DATE, 1);
		}
		return cal.getTime();
	}
	
	public void removeChangeListener(ChangeListener l) {}
	public void addChangeListener(ChangeListener l) {}
}