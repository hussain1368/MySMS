package com.kabulbits.shoqa.util;

import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;

public abstract class DateEditor extends DefaultCellEditor 
{
	private static final long serialVersionUID = 1L;
	
	private JSpinner spinner;
	private ULocale locale;
	private DateFormat df;
	
	private Date oldDate;
	private int row;

	public DateEditor()
	{
		super(new JTextField());
		
		spinner = new JSpinner();
		spinner.setModel(new PDateModel(spinner));
		spinner.setBorder(new LineBorder(Color.BLACK));
		
		locale = new ULocale("@calendar=persian");
		df = new SimpleDateFormat("yyyy/MM/dd", locale);
		
		setClickCountToStart(1);
	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.row = row;
		try {
			if(value != null){
				oldDate = df.parse(value.toString());
			}else{
				oldDate = null;
			}
			spinner.setValue(oldDate);
		}
		catch(ParseException e){
			spinner.setValue(null);
		}
		return spinner;
	}
	@Override
	public Object getCellEditorValue() {
		Object value = spinner.getValue();
		if(value instanceof Date){
			Date newDate = (Date) value;
			if(save(newDate, row)){
				return df.format(newDate);
			}
		}
		if(oldDate != null){
			return df.format(oldDate);
		}
		return null;
	}
	public abstract boolean save(Date date, int row);
}
























