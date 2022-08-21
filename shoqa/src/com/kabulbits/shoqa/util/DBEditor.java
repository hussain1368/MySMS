package com.kabulbits.shoqa.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public abstract class DBEditor extends DefaultCellEditor 
{
	private static final long serialVersionUID = 1L;
	private JTextField field;
	private Object value;
	private int row;
	
	public DBEditor(boolean rtl)
	{
		super(new JTextField());
		
		field = new JTextField();
		field.setBorder(new LineBorder(Color.BLACK));
		if(rtl) field.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				field.selectAll();
			}
		});
		setClickCountToStart(1);
	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = value;
		this.row = row;
		field.setText(value.toString());
		return field;
	}
	@Override
	public Object getCellEditorValue() {
		String value = field.getText();
		if(save(row, value)){
			return value;
		}
		return this.value;
	}
	
	public abstract boolean save(int row, Object value);
}



















