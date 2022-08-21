package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.util.Dic;

public class Eventuals extends Report {

	private MarkData markData;
	private Sheet sheet;
	
	public Eventuals(Course course, boolean all)
	{
		markData = new MarkData();
		sheet = makeSheet(Dic.w("eventual_sheet"));
		for(int i=0; i<6; i++){
			sheet.setColumnWidth(i, 1500);
		}
		for(int i=7; i<20; i++){
			sheet.setColumnWidth(i, 2600);
		}
		sheet.setColumnWidth(6, 3500);
		
		sheet.createRow(0).createCell(4).setCellValue(Data.HEAD_TITLE);
		sheet.createRow(1).createCell(4).setCellValue(Data.ADMINISTER);
		sheet.createRow(2).createCell(4).setCellValue(Data.SCHOOL_TITLE);
		sheet.createRow(3).createCell(4).setCellValue(Dic.w("eventual_sheet_title"));
		
		CellStyle title = workbook.createCellStyle();
		title.setAlignment(CellStyle.ALIGN_CENTER);
		for(int i=0; i<4; i++){
			sheet.getRow(i).getCell(4).setCellStyle(title);
			sheet.getRow(i).setHeight((short)400);
			sheet.addMergedRegion(new CellRangeAddress(i, i, 4, 8));
		}
		CellStyle bold = workbook.createCellStyle();
		bold.setAlignment(CellStyle.ALIGN_CENTER);
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		bold.setFont(font);
		sheet.getRow(2).getCell(4).setCellStyle(bold);
		
		CellStyle vertical = buildStyle(true);
		CellStyle horizontal = buildStyle(false);
		horizontal.setAlignment(CellStyle.ALIGN_CENTER);
		horizontal.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
		Row row = sheet.createRow(4);
		row.setHeight((short)1000);
		String[] cols = {"number", "name", "fname", "gfname", "base_code", "course"};
		for(int i=0; i<cols.length; i++){
			row.createCell(i).setCellValue(Dic.w(cols[i]));
			row.getCell(i).setCellStyle(vertical);
		}
		
		Vector<Object[]> studs = markData.eventualStudList(course.id, course.year, course.grade, all);
		int studCount = studs.size();
		int rows = studCount * 5;
		for(int i=5; i<rows+5; i++){
			sheet.createRow(i);
		}
		int r = 5;
		int n = 1;
		int subs = 0;
		for(Object[] stud : studs)
		{
			sheet.getRow(r).createCell(0).setCellValue(n);
			sheet.getRow(r).createCell(1).setCellValue(stud[0].toString());
			sheet.getRow(r).createCell(2).setCellValue(stud[1].toString());
			sheet.getRow(r).createCell(3).setCellValue(stud[2].toString());
			sheet.getRow(r).createCell(4).setCellValue((int)stud[3]);
			sheet.getRow(r).createCell(5).setCellValue(course.grade);
			
			sheet.getRow(r+3).setHeight((short)500);
			sheet.getRow(r+4).setHeight((short)500);
			
			for(int i=0; i<6; i++)
			{
				CellRangeAddress range = new CellRangeAddress(r, r+4, i, i);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workbook);
				sheet.addMergedRegion(range);
			}
			sheet.getRow(r).getCell(1).setCellStyle(vertical);
			sheet.getRow(r).getCell(2).setCellStyle(vertical);
			sheet.getRow(r).getCell(3).setCellStyle(vertical);
			
			String[] texts = {"subjects", "year_mark", "the_second_mark", "examiner_sign", "viewer_sign"};
			for(int i=0; i<5; i++){
				sheet.getRow(r+i).createCell(6).setCellValue(Dic.w(texts[i]));
			}
			Vector<Object[]> marks = markData.studSecondMarks((int)stud[4], course.year, course.grade);
			if(marks.size() > subs){
				subs = marks.size();
			}
			for(int i=0; i<marks.size(); i++){
				sheet.getRow(r+0).createCell(7+i).setCellValue(marks.get(i)[0].toString());
				sheet.getRow(r+1).createCell(7+i).setCellValue((float)marks.get(i)[1]);
				sheet.getRow(r+2).createCell(7+i).setCellValue((float)marks.get(i)[2]);
			}
			
			r += 5;
			n++;
		}
		sheet.setColumnWidth(7+subs, 1500);

		sheet.getRow(4).createCell(6).setCellValue(Dic.w("marks_and_subjects"));
		sheet.getRow(4).createCell(7).setCellValue(Dic.w("details"));
		sheet.getRow(4).createCell(7+subs).setCellValue(Dic.w("result"));
		sheet.getRow(4).createCell(8+subs).setCellValue(Dic.w("description"));
		
		sheet.getRow(4).getCell(6).setCellStyle(horizontal);
		sheet.getRow(4).getCell(7).setCellStyle(horizontal);
		sheet.getRow(4).getCell(7+subs).setCellStyle(vertical);
		sheet.getRow(4).getCell(8+subs).setCellStyle(horizontal);
		
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 7, 6+subs));
		setRegionBorder(sheet, new CellRangeAddress(4, 4, 0, 8+subs));
		
		sheet.createRow(r+1).setHeight((short)2000);
		for(int i=0; i<2; i++)
		{
			sheet.addMergedRegion(new CellRangeAddress(r+i, r+i, 0, 4));
			sheet.addMergedRegion(new CellRangeAddress(r+i, r+i, 5, 6));
			sheet.addMergedRegion(new CellRangeAddress(r+i, r+i, 7, subs+6));
			sheet.addMergedRegion(new CellRangeAddress(r+i, r+i, subs+7, subs+8));
			
			setRegionBorder(sheet, new CellRangeAddress(r+i, r+i, 0, subs+8));
		}
		for(int i=5; i<r; i++){
			sheet.getRow(i).createCell(subs+7).setCellStyle(vertical);
			for(int j=6; j<subs+7; j++){
				if(sheet.getRow(i).getCell(j) == null){
					sheet.getRow(i).createCell(j).setCellStyle(horizontal);
				}else{
					sheet.getRow(i).getCell(j).setCellStyle(horizontal);
				}
			}
		}
		for(int i=5; i<r; i+=5)
		{
			setRegionBorder(sheet, new CellRangeAddress(i, i+4, 0, subs+8));
			CellRangeAddress range = new CellRangeAddress(i, i+4, subs+7, subs+7);
			RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
			RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workbook);
			sheet.addMergedRegion(range);
		}
		CellRangeAddress theRange = new CellRangeAddress(4, rows+4, 6, 6);
		RegionUtil.setBorderLeft(CellStyle.BORDER_DOUBLE, theRange, sheet, workbook);
		
		addImage(sheet, schoolLogo, 0, 9);
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}






























