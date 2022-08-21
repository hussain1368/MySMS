package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.EmployeeData;
import com.kabulbits.shoqa.util.Dic;

public class EmployeeList extends Report 
{
	private EmployeeData empData;
	
	public void advancedSheet(int from, int to)
	{
		empData = new EmployeeData();
		Sheet sheet = makeSheet(true, Dic.w("advanced_employee_sheet"));
		
		sheet.setColumnWidth(0, 1000);
		for(int i=5; i<18; i++){
			sheet.setColumnWidth(i, 1500);
		}
		sheet.setColumnWidth(18, 5000);
		
		CellStyle vertical = buildStyle(true);
		CellStyle title = boldStyle(false, false);
		CellStyle bold = boldStyle(false, true);
		
		sheet.createRow(0).createCell(6).setCellValue(Data.HEAD_TITLE);
		sheet.createRow(1).createCell(6).setCellValue(Data.ADMINISTER);
		sheet.createRow(2).createCell(6).setCellValue(Dic.w("advanced_employee_sheet"));
		
		sheet.getRow(0).getCell(6).setCellStyle(title);
		sheet.getRow(1).getCell(6).setCellStyle(title);
		sheet.getRow(2).getCell(6).setCellStyle(title);
		
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 12));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 6, 12));
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 12));
		
		
		sheet.createRow(3).setHeight((short)900);
		sheet.createRow(4).setHeight((short)800);
		
		sheet.getRow(3).createCell(0).setCellValue(Dic.w("number"));
		sheet.getRow(3).getCell(0).setCellStyle(vertical);
		
		sheet.getRow(3).createCell(1).setCellValue(Dic.w("identity"));
		sheet.getRow(3).getCell(1).setCellStyle(bold);
		
		sheet.addMergedRegion(new CellRangeAddress(3, 4, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4));
		
		String[] txts = {"name", "lname", "fname", "gfname"};
		int c = 1;
		for(String txt : txts)
		{
			sheet.getRow(4).createCell(c).setCellStyle(bold);
			sheet.getRow(4).getCell(c++).setCellValue(Dic.w(txt));
		}
		String[] cols = {"job", "main_addr", "curr_addr", "idcard_no", "educ_level", "educ_field", "gradu_year", 
				"service_duration", "work_experience", "crimes", "punishments", "employment_date", "phone", "photo"
		};
		c = 5;
		for(String col : cols)
		{
			sheet.getRow(3).createCell(c).setCellStyle(vertical);
			sheet.getRow(4).createCell(c).setCellStyle(vertical);
			sheet.getRow(3).getCell(c).setCellValue(Dic.w(col));
			sheet.addMergedRegion(new CellRangeAddress(3, 4, c, c));
			c++;
		}
		
		Vector<Object[]> list = empData.empsAdvancedList(from, to);
		int r = 5, n = 1;
		for(Object[] item : list)
		{
			sheet.createRow(r).setHeight((short)2000);
			sheet.getRow(r).createCell(0).setCellStyle(bold);
			sheet.getRow(r).getCell(0).setCellValue(n++);
			int last = item.length-1;
			for(int i=0; i<last; i++)
			{
				sheet.getRow(r).createCell(i+1).setCellStyle(vertical);
				sheet.getRow(r).getCell(i+1).setCellValue(item[i].toString());
			}
			sheet.getRow(r).createCell(last+1).setCellStyle(vertical);
			if(item[last] != null){
				addImage(sheet, (byte[])item[last], r, last+1, .6);
			}
			r++;
		}
		setRegionBorder(sheet, new CellRangeAddress(3, r-1, 0, 18));
	}
	@Override
	protected void finalize() throws Throwable{
		if(empData != null){
			empData.closeConn();
		}
		super.finalize();
	}
}

























