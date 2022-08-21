package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.util.Dic;

public class Attendance extends Report 
{
	private MarkData markData;
	private Sheet sheet;
	
	public Attendance(Course course)
	{
		markData = new MarkData();
		
		sheet = makeSheet(true, Dic.w("course_attendance"));
		sheet.setColumnWidth(0, 1000);
		sheet.setColumnWidth(4, 1000);
		for(int i=5; i<41; i++){
			sheet.setColumnWidth(i, 500);
		}
		CellStyle horizontal = buildStyle(false);
		CellStyle vertical = buildStyle(true);
		
		CellStyle center = workbook.createCellStyle();
		center.setAlignment(CellStyle.ALIGN_CENTER);
		
		sheet.createRow(0).setHeight((short)700);
		sheet.createRow(1).setHeight((short)700);

		sheet.getRow(0).createCell(4).setCellValue(Data.SCHOOL_TITLE);
		sheet.getRow(1).createCell(4).setCellValue(Dic.w("course_attendance"));
		
		sheet.getRow(0).getCell(4).setCellStyle(boldStyle(false, false));
		sheet.getRow(1).getCell(4).setCellStyle(center);
		
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 30));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 30));
		
		String title = String.format("%s: %d (%s)", Dic.w("course"), course.grade, course.name);
		sheet.getRow(1).createCell(1).setCellValue(title);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2));
		
		sheet.createRow(3);
		sheet.createRow(4).setHeight((short)800);
		String[] cols = {"number", "name", "fname", "gfname", "base_code"};
		int c = 0;
		for(String col : cols)
		{
			sheet.getRow(3).createCell(c).setCellValue(Dic.w(col));
			CellRangeAddress range = new CellRangeAddress(3, 4, c, c);
			RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
			sheet.addMergedRegion(range);
			c++;
		}
		sheet.getRow(3).getCell(0).setCellStyle(vertical);
		sheet.getRow(3).getCell(4).setCellStyle(vertical);
		
		String[] months = {"hamal", "sawr", "jawza", "saratan", "asad", "sonbola", "mizan", "aqrab", "qaws"};
		String[] texts = {"present", "absent", "sick", "holiday"};
		c = 5;
		for(String month : months)
		{
			sheet.getRow(3).createCell(c).setCellValue(Dic.w(month));
			sheet.getRow(3).getCell(c).setCellStyle(horizontal);
			sheet.addMergedRegion(new CellRangeAddress(3, 3, c, c+3));
			for(String txt : texts)
			{
				sheet.getRow(4).createCell(c).setCellStyle(vertical);
				sheet.getRow(4).getCell(c).setCellValue(Dic.w(txt));
				c++;
			}
		}
		setRegionBorder(sheet, new CellRangeAddress(3, 4, 0, 40));
		
		Vector<Object[]> studs = markData.studentsList(course.id);
		int r = 5;
		int n = 1;
		for(Object[] stud : studs)
		{
			sheet.createRow(r).setHeight((short)500);
			int sid = (int) stud[4];
			for(int i=0; i<41; i++){
				sheet.getRow(r).createCell(i).setCellStyle(i<5 ? horizontal : vertical);
			}
			sheet.getRow(r).getCell(0).setCellValue(n++);
			sheet.getRow(r).getCell(1).setCellValue(stud[0].toString());
			sheet.getRow(r).getCell(2).setCellValue(stud[1].toString());
			sheet.getRow(r).getCell(3).setCellValue(stud[2].toString());
			sheet.getRow(r).getCell(4).setCellValue((int)stud[3]);
			
			Vector<Object[]> attend = markData.findAttendance(sid, course.year);
			c = 5;
			for(int m=0; m<10; m++){
				for(int i=1; i<attend.get(m).length; i++){
					if(attend.get(m)[i] != null){
						sheet.getRow(r).getCell(c).setCellValue((int)attend.get(m)[i]);
					}
					c++;
				}
			}
			r++;
		}
		setRegionBorder(sheet, new CellRangeAddress(3, r-1, 0, 4));
		for(int i=5; i<41; i+=4){
			setRegionBorder(sheet, new CellRangeAddress(3, r-1, i, i+3));
		}
		addImage(sheet, schoolLogo, 0, 33);
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}


























