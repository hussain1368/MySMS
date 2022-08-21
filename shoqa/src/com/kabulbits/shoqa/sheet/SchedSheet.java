package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import com.kabulbits.shoqa.db.CourseData;
import com.kabulbits.shoqa.util.Dic;

public class SchedSheet extends Report
{
	private CourseData data;
	private Sheet sheet;
	private int shift;
	
	public SchedSheet(int shift)
	{
		this.shift = shift;
		data = new CourseData();
		sheet = makeSheet(Dic.w("timetable"));
		horizontal = buildStyle(false);
		horizontal.setWrapText(true);
		vertical = buildStyle(true);
	}
	private CellStyle horizontal;
	private CellStyle vertical;
	String[] week = {"saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
	
	public void officeFormat()
	{
		sheet.setColumnWidth(0, 1000);
		sheet.setColumnWidth(1, 1000);
		
		Vector<Object[]> courses = data.coursesByShift(shift);
		
		CellStyle hBold = boldStyle(false, true);
		CellStyle vBold = boldStyle(true, true);
		
		sheet.createRow(0).setHeight((short)500);
		sheet.createRow(1).setHeight((short)500);
		
		sheet.getRow(0).createCell(0).setCellValue(Dic.w("week_days"));
		sheet.getRow(0).createCell(1).setCellValue(Dic.w("hours"));
		
		sheet.getRow(0).getCell(0).setCellStyle(vBold);
		sheet.getRow(0).getCell(1).setCellStyle(vBold);
		
		sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
		
		int r = 2;
		for(String day : week)
		{
			int rs = r;
			for(int i=1; i<7; i++)
			{
				sheet.createRow(r);
				sheet.getRow(r).createCell(1).setCellValue(i);
				sheet.getRow(r).getCell(1).setCellStyle(horizontal);
				r++;
			}
			sheet.getRow(rs).createCell(0).setCellValue(Dic.w(day));
			sheet.getRow(rs).getCell(0).setCellStyle(vertical);
			sheet.addMergedRegion(new CellRangeAddress(rs, r-1, 0, 0));
		}
		
		int c = 2;
		for(Object[] course : courses)
		{
			int id = (int) course[1];
			String text = String.format("%s %s (%s)", Dic.w("course"), course[2].toString(), course[3].toString());
			sheet.getRow(0).createCell(c).setCellValue(text);
			sheet.getRow(1).createCell(c).setCellValue(Dic.w("subject"));
			sheet.getRow(1).createCell(c+1).setCellValue(Dic.w("teacher"));
			
			sheet.getRow(0).getCell(c).setCellStyle(hBold);
			sheet.getRow(1).getCell(c).setCellStyle(horizontal);
			sheet.getRow(1).getCell(c+1).setCellStyle(horizontal);
			
			sheet.addMergedRegion(new CellRangeAddress(0, 0, c, c+1));
			sheet.setColumnWidth(c, 2500);
			sheet.setColumnWidth(c+1, 2500);
			
			String[][][] times = data.courseTimetableForPrint(id);
			r = 2;
			for(int d=1; d<7; d++){
				for(int t=1; t<7; t++){
					sheet.getRow(r).createCell(c).setCellValue(times[d][t][0]);
					sheet.getRow(r).createCell(c+1).setCellValue(times[d][t][1]);
					sheet.getRow(r).getCell(c).setCellStyle(horizontal);
					sheet.getRow(r).getCell(c+1).setCellStyle(horizontal);
					r++;
				}
			}
			c += 2;
		}
		setRegionBorder(sheet, new CellRangeAddress(0, 1, 0, c-1));
		for(int i=2; i<35; i+=6){
			setRegionBorder(sheet, new CellRangeAddress(i, i+5, 0, c-1));
		}
	}
	
	public void courseFormat()
	{
		for(int i=1; i<15; i++){
			sheet.setColumnWidth(i, 3000);
		}
		CellStyle bold = boldStyle(false, true);
		Vector<Object[]> courses = data.coursesByShift(shift);
		
		String[] hours = {"first", "second", "third", "fourth", "fifth", "sixth"};
		int r = 0;
		for(Object[] course : courses)
		{
			String text = String.format("%s %s (%s)", Dic.w("course"), course[2].toString(), course[3].toString());
			sheet.createRow(r++).createCell(0).setCellValue(text);
			
			int rs = r;
			sheet.createRow(r);
			sheet.getRow(r).createCell(0).setCellValue(Dic.w("week_days"));
			sheet.getRow(r).getCell(0).setCellStyle(bold);
			int c = 1;
			for(String hour : hours)
			{
				sheet.getRow(r).createCell(c).setCellValue(Dic.w(hour));
				sheet.getRow(r).getCell(c).setCellStyle(bold);
				sheet.addMergedRegion(new CellRangeAddress(r, r, c, c+1));
				c += 2;
			}
			r++;
			
			int id = (int) course[1];
			String[][][] times = data.courseTimetableForPrint(id);
			for(int d=1; d<7; d++){
				sheet.createRow(r);
				sheet.getRow(r).createCell(0).setCellValue(Dic.w(week[d-1]));
				sheet.getRow(r).getCell(0).setCellStyle(bold);
				c = 1;
				for(int t=1; t<7; t++){
					sheet.getRow(r).createCell(c).setCellValue(times[d][t][0]);
					sheet.getRow(r).getCell(c).setCellStyle(horizontal);
					c++;
					sheet.getRow(r).createCell(c).setCellValue(times[d][t][1]);
					sheet.getRow(r).getCell(c).setCellStyle(horizontal);
					c++;
				}
				r++;
			}
			setRegionBorder(sheet, new CellRangeAddress(rs, r-1, 0, 12));
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


























