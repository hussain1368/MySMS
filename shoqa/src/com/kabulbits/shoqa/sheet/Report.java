package com.kabulbits.shoqa.sheet;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class Report{
	protected Workbook workbook;
	private Data data;
	
	public Report(){
		workbook = new XSSFWorkbook();
		data = new Data();
		data.loadConfigs();
	}
	protected Sheet makeSheet(String name){
		return makeSheet(false, name);
	}
	protected Sheet makeSheet(boolean land, String name) {
		Sheet sheet = workbook.createSheet(name);
		sheet.setRightToLeft(true);
		sheet.getPrintSetup().setLandscape(land);
		return sheet;
	}
	protected CellStyle buildStyle(boolean vertical){
		return buildStyle(vertical, true);
	}
	protected CellStyle buildStyle(boolean vertical, boolean borderd)
	{
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		if(borderd){
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setBorderRight(CellStyle.BORDER_THIN);
		}
		if(vertical){
			style.setRotation((short)180);
		}
		return style;
	}
	protected CellStyle boldStyle(boolean vertical, boolean bordered)
	{
		CellStyle style = buildStyle(vertical, bordered);
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		return style;
	}
	protected void setRegionBorder(Sheet sheet, CellRangeAddress range)
	{
		RegionUtil.setBorderTop(CellStyle.BORDER_THICK, range, sheet, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_THICK, range, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THICK, range, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THICK, range, sheet, workbook);
	}
	protected final String schoolLogo = "images/school_logo.jpg";
	protected final String ministryLogo = "images/ministry_logo.jpg";
	
	protected void addImage(Sheet sheet, String path, int row, int col)
	{
		try{
			InputStream is = new FileInputStream(path);
			byte [] bytes = IOUtils.toByteArray(is);
			addImage(sheet, bytes, row, col, .4);
			is.close();
		}
		catch(IOException e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
	}
	protected void addImage(Sheet sheet, byte [] bytes, int row, int col, double size)
	{
		try{
			int picIndex = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
			
			CreationHelper helper = workbook.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();
			
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(col);
			anchor.setRow1(row);

			Picture pic = drawing.createPicture(anchor, picIndex);
			pic.resize(size);
		}
		catch(Exception e){
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
	}
	public void simpleSheet(String title, String[] cols, Vector<?> data, int[] indexes)
	{
		Sheet sheet = makeSheet(Dic.w(title));
		CellStyle horizontal = buildStyle(false);
		CellStyle bold = boldStyle(false, true);
		
		sheet.createRow(0).createCell(1).setCellValue(Dic.w(title));
		sheet.getRow(0).getCell(1).setCellStyle(boldStyle(false, false));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 5));
		sheet.getRow(0).setHeight((short)500);
		
		int r = 1;
		int c = 0;
		sheet.createRow(r);
		for(String col : cols){
			sheet.getRow(r).createCell(c).setCellValue(Dic.w(col));
			sheet.getRow(r).getCell(c++).setCellStyle(bold);
		}
		r++;
		for(Object row : data){
			Vector<?> theRow = (Vector<?>) row;
			sheet.createRow(r);
			c = 0;
			for(int index : indexes)
			{
				sheet.getRow(r).createCell(c).setCellStyle(horizontal);
				if(theRow.get(index) != null)
				{
					if(theRow.get(index) instanceof Float){
						sheet.getRow(r).getCell(c).setCellValue((float)theRow.get(index));
					}
					else if(theRow.get(index) instanceof Integer){
						sheet.getRow(r).getCell(c).setCellValue((int)theRow.get(index));
					}
					else{
						sheet.getRow(r).getCell(c).setCellValue(theRow.get(index).toString());
					}
				}
				c++;
			}
			r++;
		}
	}
	public void build(String path){
		try{
			FileOutputStream output = new FileOutputStream(path);
			workbook.write(output);
			output.close();
		}
		catch (FileNotFoundException e){
			Diags.showErrLang("file_is_in_use");
			return;
		}
		catch (IOException e) {
			if(App.LOG){
				App.getLogger().error(e.getMessage(), e);
			}
		}
		open(path);
	}
	protected void open(String path)
	{
		if(Desktop.isDesktopSupported()){
			try{
				Desktop desktop = Desktop.getDesktop();
				desktop.open(new File(path));
			}
			catch(Exception e){
				if(App.LOG){
					App.getLogger().error(e.getMessage(), e);
				}
			}
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
