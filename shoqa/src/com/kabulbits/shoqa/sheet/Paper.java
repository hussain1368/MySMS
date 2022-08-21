package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.SubjectData;
import com.kabulbits.shoqa.util.Dic;

public class Paper extends Report {

	private MarkData markData;
	private SubjectData subjData;
	private Sheet sheet;
	private int row = 0;
	
	public Paper()
	{
		markData = new MarkData();
		subjData = new SubjectData();
		sheet = makeSheet(Dic.w("paper_sheets"));
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 1500);
		sheet.setColumnWidth(2, 1500);
		sheet.setColumnWidth(3, 1500);
		sheet.setColumnWidth(4, 3000);
		for(int i=5; i<10; i++){
			sheet.setColumnWidth(i, 1100);
		}
		vertical = buildStyle(true);
		horizontal = buildStyle(false);
	}
	
	private int subjCount = 0;
	public void createAll(Course course, boolean allSubs)
	{
		subjCount = subjData.gradeSubjCount(course.grade);
		Vector<Object[]> studs = markData.studentsList(course.id);
		for(Object[] stud : studs){
			createOne(stud, course.year, course.grade, allSubs);
			row += 40;
		}
	}
	CellStyle vertical, horizontal;
	
	public void createOne(Object[] student, int year, int grade, boolean allSubs)
	{
		if(subjCount == 0){
			subjCount = subjData.gradeSubjCount(grade);
		}
		CellStyle title = workbook.createCellStyle();
		title.setAlignment(CellStyle.ALIGN_CENTER);
		for(int i=0; i<5; i++){
			sheet.createRow(row+i).createCell(2).setCellStyle(title);
			sheet.addMergedRegion(new CellRangeAddress(row+i, row+i, 2, 4));
		}
		sheet.getRow(row+0).getCell(2).setCellValue(Data.MINISTRY_TITLE);
		sheet.getRow(row+1).getCell(2).setCellValue(Data.HEAD_TITLE);
		sheet.getRow(row+2).getCell(2).setCellValue(Data.ADMINISTER);
		sheet.getRow(row+3).getCell(2).setCellValue(Data.SCHOOL_TITLE);
		sheet.getRow(row+4).getCell(2).setCellValue(Dic.w("paper_sheet_title"));
		sheet.getRow(row+3).getCell(2).setCellStyle(boldStyle(false, false));
		
		String str = "%s: %s   %s: %s       %s: %d   %s: %d";
		String line = String.format(str, Dic.w("name"), student[0].toString(), Dic.w("fname"), student[1].toString(), Dic.w("course"), grade, Dic.w("educ_year"), year);
		sheet.createRow(row+5).createCell(0).setCellValue(line);
		sheet.addMergedRegion(new CellRangeAddress(row+5, row+5, 0, 9));
		
		sheet.createRow(row+6);
		sheet.createRow(row+7);
		sheet.getRow(row+6).createCell(0).setCellValue(Dic.w("subjects"));
		sheet.getRow(row+6).createCell(5).setCellValue(Dic.w("attendance_rules"));
		sheet.getRow(row+6).getCell(5).setCellStyle(horizontal);
		sheet.addMergedRegion(new CellRangeAddress(row+6, row+6, 5, 9));
		
		String[] cols1 = {"half_mark", "final_mark", "total_mark", "exams", "educ_year", "present", "absent", "sick", "holiday"};
		int c = 1;
		for(String col : cols1){
			int r = c < 5 ? row+6 : row+7;
			sheet.getRow(r).createCell(c).setCellStyle(vertical);
			if(c < 5){
				sheet.getRow(r+1).createCell(c).setCellStyle(vertical);
			}
			sheet.getRow(r).getCell(c).setCellValue(Dic.w(col));
			c++;
		}
		for(int i=0; i<5; i++){
			sheet.addMergedRegion(new CellRangeAddress(row+6, row+7, i, i));
		}
		RegionUtil.setBorderBottom(CellStyle.BORDER_THICK, new CellRangeAddress(row+7, row+7, 0, 9), sheet, workbook);
		
		int sid = (int) student[4];
		Vector<Object[]> marks = markData.fullMarks(sid, year, grade, 1, allSubs);
		
		int r = row + 8;
		float[] totals = {0, 0, 0};
		int countMid = 0, countFin = 0;
		int failsMid = 0, failsFin = 0;
		int passMid = 0, passFin = 0;
		for(Object[] mark : marks){
			sheet.createRow(r);
			sheet.getRow(r).createCell(0).setCellStyle(horizontal);
			sheet.getRow(r).getCell(0).setCellValue(mark[1].toString());
			for(int i=1; i<4; i++){
				sheet.getRow(r).createCell(i).setCellStyle(horizontal);
				if(mark[i+1] != null){
					sheet.getRow(r).getCell(i).setCellValue((float)mark[i+1]);
					totals[i-1] += (float) mark[i+1];
				}
			}
			if(mark[2] != null){
				countMid++;
				if((float)mark[2] < Data.PASS_GRADE_MID){
					failsMid++;
				}
				else passMid++;
			}
			if(mark[4] != null){
				countFin++;
				if((float)mark[4] < Data.PASS_GRADE_FINAL){
					failsFin++;
				}
				else passFin++;
			}
			r++;
		}
		CellStyle resBold = boldStyle(false, true);
		String[] texts = {"total", "average", "result", "degree"};
		for(int i=0; i<4; i++){
			sheet.createRow(r+i);
			for(int j=0; j<4; j++){
				sheet.getRow(r+i).createCell(j).setCellStyle(j==0 ? resBold : horizontal);
			}
			sheet.getRow(r+i).getCell(0).setCellValue(Dic.w(texts[i]));
		}
		sheet.getRow(r).getCell(1).setCellValue(totals[0]);
		sheet.getRow(r).getCell(2).setCellValue(totals[1]);
		sheet.getRow(r).getCell(3).setCellValue(totals[2]);
		if(countMid >= subjCount){
			float avgMid = totals[0] / countMid;
			int posMid = markData.studentPosition(sid, year, grade, 1, avgMid);
			sheet.getRow(r+1).getCell(1).setCellValue(avgMid);
			sheet.getRow(r+3).getCell(1).setCellValue(posMid);
		}
		if(countFin >= subjCount){
			float avgFin = totals[2] / countFin;
			int posFin = markData.studentPosition(sid, year, grade, 2, avgFin);
			sheet.getRow(r+1).getCell(3).setCellValue(avgFin);
			sheet.getRow(r+3).getCell(3).setCellValue(posFin);
		}
		String resMid = "";
		if(passMid >= subjCount){
			resMid = "passed";
		}else if(failsMid > 0){
			resMid = "failed";
		}
		sheet.getRow(r+2).createCell(1).setCellValue(Dic.w(resMid));
		
		String resFin = "";
		if(passFin >= subjCount){
			resFin = "passed";
		}else if(failsFin > 0){
			if(failsFin >= Data.FAIL_SUBJ_COUNT){
				resFin = "failed";
			}else if(failsFin < Data.FAIL_SUBJ_COUNT){
				resFin = "eventual";
			}
		}
		sheet.getRow(r+2).createCell(3).setCellValue(Dic.w(resFin));
		
		int lastRow = r+3;

		r = row + 8;
		for(int i=0; i<3; i++){
			if(sheet.getRow(r+i) == null){
				sheet.createRow(r+i);
			}
			for(int j=4; j<10; j++){
				sheet.getRow(r+i).createCell(j).setCellStyle(horizontal);
			}
		}
		sheet.getRow(r+0).getCell(4).setCellValue(Dic.w("half_mark"));
		sheet.getRow(r+1).getCell(4).setCellValue(Dic.w("final_mark"));
		sheet.getRow(r+2).getCell(4).setCellValue(Dic.w("total_mark"));
		
		sheet.getRow(r+0).getCell(5).setCellValue(Data.ATTEND_MID);
		sheet.getRow(r+1).getCell(5).setCellValue(Data.ATTEND_FINAL);
		sheet.getRow(r+2).getCell(5).setCellValue(Data.ATTEND_MID + Data.ATTEND_FINAL);
		
		int[][] attend = markData.studentAttendSum(sid, year);
		for(int i=0; i<3; i++){
			for(int j=0; j<4; j++){
				sheet.getRow(r+i).getCell(6+j).setCellValue(attend[i][j]);
			}
		}
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, new CellRangeAddress(r+1, r+1, 4, 9), sheet, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_THICK, new CellRangeAddress(r+2, r+2, 4, 9), sheet, workbook);
		
		if(sheet.getRow(row+12) != null){
			sheet.getRow(row+12).createCell(4).setCellValue(Dic.w("paper_sign_a"));
		}
		if(sheet.getRow(row+15) != null){
			sheet.getRow(row+15).createCell(4).setCellValue(Dic.w("paper_sign_b"));
		}
		if(sheet.getRow(row+18) != null){
			sheet.getRow(row+18).createCell(4).setCellValue(Dic.w("paper_sign_c"));
		}
		CellRangeAddress range = new CellRangeAddress(row+6, lastRow, 1, 3);
		RegionUtil.setBorderLeft(CellStyle.BORDER_DOUBLE, range, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_DOUBLE, range, sheet, workbook);
		setRegionBorder(sheet, new CellRangeAddress(row+6, lastRow, 0, 9));
		
		addImage(sheet, ministryLogo, row, 0);
		addImage(sheet, schoolLogo, row, 6);
	}
	@Override
	protected void finalize() throws Throwable{
		if(subjData != null){
			subjData.closeConn();
		}
		if(markData != null){
			markData.closeConn();
		}
		super.finalize();
	}
}


























