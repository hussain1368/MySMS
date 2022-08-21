package com.kabulbits.shoqa.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;

public class SpinnerEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;
	private JSpinner spinner;

	public SpinnerEditor(int val, int min, int max){
		this(val, min, max, 1);
	}
	
    public SpinnerEditor(int val, int min, int max, int step)
    {
    	super(new JTextField());
    	spinner = new JSpinner(new SpinnerNumberModel(val, min, max, step));
    	spinner.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    	spinner.setEditor(new NumberEditor(spinner, "#"));
    	spinner.setBorder(new LineBorder(Color.BLACK));
    	
    	setClickCountToStart(1);
    }

    public Component getTableCellEditorComponent(
    	JTable table, Object value, boolean isSelected, int row, int column)
    {
    	if(value != null){
    		spinner.setValue(Integer.parseInt(value.toString()));
    	}else{
    		spinner.setValue(0);
    	}
    	return spinner;
    }

    public Object getCellEditorValue()
    {
    	return spinner.getValue();
    }
}

