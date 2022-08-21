package com.kabulbits.shoqa.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.imgscalr.Scalr;

public class Helper {
	
	public static boolean isNumeric(String str, boolean required)
	{
		if(!required && str.length() == 0) return true;
	    return str.trim().matches("^[0-9]+$");
	}
	
	public static void tableMakUp(JTable table)
	{
		table.setShowGrid(false);
		table.setRowHeight(22);
		table.setShowHorizontalLines(true);
		table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class)).setHorizontalAlignment(JLabel.RIGHT);
	}
	
	public static DefaultCellEditor rtlEditor()
	{
		final JTextField textField = new JTextField();
		textField.setBorder(new LineBorder(Color.BLACK));
		textField.setHorizontalAlignment(JTextField.RIGHT);
		textField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				textField.selectAll();
			}
		});
		DefaultCellEditor editor = new DefaultCellEditor(textField);
		editor.setClickCountToStart(1);
		return editor;
	}
	
	public static void singleClick(JTable table)
	{
		FocusListener focus = new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				((JTextField) e.getSource()).selectAll();
			}
		};
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) 
		{
			if(table.getModel().isCellEditable(0, i))
			{
				DefaultCellEditor editor = (DefaultCellEditor) table.getDefaultEditor(table.getColumnClass(i));
				editor.setClickCountToStart(1);
				Component comp = editor.getComponent();
				
				if(comp instanceof JTextField){
					final JTextField field = (JTextField) comp;
					field.addFocusListener(focus);
				}
			}
		}
	}

	public static void esc(final JDialog dialog)
	{
		dialog.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
	
	public static String pickPhoto(JFrame frame)
	{
		FileDialog diag = new FileDialog(frame);
        diag.setResizable(true);
        diag.setVisible(true);
        
		String fn = diag.getFile();
		if(fn == null) return null;
		fn = fn.trim().toLowerCase();
		String [] exts = {".jpg", ".jpeg", ".png", ".gif"};
		boolean yes = false;
		for(int i=0; i<exts.length; i++){
			if(fn.endsWith(exts[i])){
				yes = true;
				break;
			}
		}
		if(!yes){
			Diags.showErrLang("file_not_image_error");
			return null;
		}
		return diag.getDirectory()+fn;
	}
	
	public static byte[] photo(String path) {
		return photo(path, 150, 200);
	}

	public static byte[] photo(String path, int width, int height)
	{
		try 
		{
			BufferedImage large = ImageIO.read(new File(path));
			int w = large.getWidth();
			int h = large.getHeight();
			Scalr.Mode mode = h > w ? Scalr.Mode.FIT_TO_WIDTH : Scalr.Mode.FIT_TO_HEIGHT;
			BufferedImage small = Scalr.resize(large, mode, width, height);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(small, "png", bos);
			bos.flush();
			byte[] bytes = bos.toByteArray();
			bos.close();
			return bytes;
		} 
		catch (Exception e) {
			Diags.showErrLang("invalid_image_error");
			return null;
		}
	}
	
	public static BufferedImage circle(BufferedImage image, int cornerRadius)
	{
	    int w = image.getWidth();
	    int h = image.getHeight();
	    BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2 = output.createGraphics();

	    g2.setComposite(AlphaComposite.Src);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setColor(Color.WHITE);
	    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.drawImage(image, 0, 0, null);
	    g2.dispose();

	    return output;
	}
	
	public static String xlsx(String name, JFrame frame)
	{
		FileDialog diag = new FileDialog(frame, "Save", FileDialog.SAVE);
		diag.setFile(name + ".xlsx");
		diag.setVisible(true);
		String fn = diag.getFile();
		if(fn == null) return null;
		if(!fn.toLowerCase().endsWith(".xlsx")){
			fn += ".xlsx";
		}
		return diag.getDirectory()+fn;
	}
}


















