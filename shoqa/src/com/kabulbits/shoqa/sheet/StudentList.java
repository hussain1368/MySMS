package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.util.Dic;

public class StudentList extends Report
{
	private StudentData data;
	private Sheet sheet;
	
	public void advancedSheet(int from, int to)
	{
		data = new StudentData();
		sheet = makeSheet(true, Dic.w("advanced_student_sheet"));
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 1100);
		sheet.setColumnWidth(7, 1400);
		sheet.setColumnWidth(8, 1400);
		sheet.setColumnWidth(15, 3000);
		
		CellStyle title = boldStyle(false, false);
		CellStyle vBold = boldStyle(true, true);
		CellStyle hBold = boldStyle(false, true);
		CellStyle horizontal = buildStyle(false);
		hBold.setWrapText(true);
		vBold.setWrapText(true);
		horizontal.setWrapText(true);
		
		sheet.createRow(0).createCell(0).setCellValue(Data.MINISTRY_TITLE);
		sheet.createRow(1).createCell(0).setCellValue(Data.HEAD_TITLE);
		sheet.createRow(2).createCell(0).setCellValue(Data.SCHOOL_TITLE);
		sheet.createRow(3).createCell(0).setCellValue(Dic.w("advanced_student_sheet"));
		
		for(int i=0; i<4; i++)
		{
			sheet.getRow(i).getCell(0).setCellStyle(title);
			sheet.addMergedRegion(new CellRangeAddress(i, i, 0, 17));
		}
		
		sheet.createRow(6).setHeight((short)500);
		sheet.createRow(7).setHeight((short)500);
		
		sheet.getRow(6).createCell(0).setCellValue(Dic.w("base_code"));
		sheet.getRow(6).createCell(1).setCellValue(Dic.w("entry_doc_number_and_date"));
		sheet.getRow(6).createCell(2).setCellValue(Dic.w("grade"));
		sheet.getRow(6).createCell(3).setCellValue(Dic.w("identity"));
		
		sheet.getRow(7).createCell(1).setCellStyle(hBold);
		
		sheet.getRow(6).getCell(0).setCellStyle(hBold);
		sheet.getRow(6).getCell(1).setCellStyle(hBold);
		sheet.getRow(6).getCell(2).setCellStyle(hBold);
		sheet.getRow(6).getCell(3).setCellStyle(hBold);
		
		sheet.addMergedRegion(new CellRangeAddress(6, 7, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(6, 7, 1, 1));
		sheet.addMergedRegion(new CellRangeAddress(6, 7, 2, 2));
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 3, 5));
		
		sheet.getRow(7).createCell(3).setCellValue(Dic.w("name"));
		sheet.getRow(7).createCell(4).setCellValue(Dic.w("fname"));
		sheet.getRow(7).createCell(5).setCellValue(Dic.w("gfname"));
		
		sheet.getRow(7).getCell(3).setCellStyle(horizontal);
		sheet.getRow(7).getCell(4).setCellStyle(horizontal);
		sheet.getRow(7).getCell(5).setCellStyle(horizontal);
		
		int c = 6;
		String[] txts01 = {"idcard_no", "age", "mother_lang", "father_job", "mother_job"};
		for(String txt : txts01)
		{
			sheet.getRow(6).createCell(c).setCellValue(Dic.w(txt));
			sheet.getRow(6).getCell(c).setCellStyle(vBold);
			CellRangeAddress range = new CellRangeAddress(6, 7, c, c);
			RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workbook);
			sheet.addMergedRegion(range);
			c++;
		}
		sheet.getRow(6).createCell(11).setCellValue(Dic.w("main_addr"));
		sheet.getRow(6).getCell(11).setCellStyle(hBold);
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 11, 13));
		
		sheet.getRow(7).createCell(11).setCellValue(Dic.w("province"));
		sheet.getRow(7).createCell(12).setCellValue(Dic.w("district"));
		sheet.getRow(7).createCell(13).setCellValue(Dic.w("village"));
		
		sheet.getRow(7).getCell(11).setCellStyle(horizontal);
		sheet.getRow(7).getCell(12).setCellStyle(horizontal);
		sheet.getRow(7).getCell(13).setCellStyle(horizontal);
		
		String[] txt02 = {"curr_addr", "leave_doc_number_and_date", "reason", "description"};
		c = 14;
		for(String txt : txt02)
		{
			sheet.getRow(6).createCell(c).setCellValue(Dic.w(txt));
			sheet.getRow(6).getCell(c).setCellStyle(hBold);
			CellRangeAddress range = new CellRangeAddress(6, 7, c, c);
			RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workbook);
			sheet.addMergedRegion(range);
			c++;
		}
		CellRangeAddress head = new CellRangeAddress(6, 7, 0, 17);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, head, sheet, workbook);
		
		Vector<Object[]> rows = data.studAdvancedList(from, to);
		int r = 8;
		for(Object[] row : rows)
		{
			sheet.createRow(r);
			for(int i=0; i<row.length; i++)
			{
				sheet.getRow(r).createCell(i).setCellStyle(horizontal);
				
				if(row[i] instanceof Integer){
					sheet.getRow(r).getCell(i).setCellValue((int)row[i]);
				}else{
					sheet.getRow(r).getCell(i).setCellValue(row[i].toString());
				}
			}
			sheet.getRow(r).createCell(15).setCellStyle(horizontal);
			sheet.getRow(r).createCell(16).setCellStyle(horizontal);
			sheet.getRow(r).createCell(17).setCellStyle(horizontal);
			r++;
		}
		CellRangeAddress range0 = new CellRangeAddress(6, r-1, 3, 14);
		RegionUtil.setBorderRight(CellStyle.BORDER_DOUBLE, range0, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_DOUBLE, range0, sheet, workbook);
		
		CellRangeAddress range1 = new CellRangeAddress(6, r-1, 16, 16);
		RegionUtil.setBorderRight(CellStyle.BORDER_DOUBLE, range1, sheet, workbook);
		
		setRegionBorder(sheet, new CellRangeAddress(6, r-1, 0, 17));
		addImage(sheet, ministryLogo, 0, 3);
		addImage(sheet, schoolLogo, 0, 14);
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}
