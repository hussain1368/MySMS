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

public class MarkDetails extends Report
{
	private SubjectData subjData;
	private MarkData markData;
	private Course course;
	private Sheet sheet;
	
	public MarkDetails(Course course)
	{
		this.course = course;
		markData = new MarkData();
		subjData = new SubjectData();
	}
	public void upperGrades(int sub, String subjName, int status, int season)
	{
		sheet = makeSheet(Dic.w("details_sheet_title"));
		for(int i=3; i<9; i++){
			sheet.setColumnWidth(i, 1000);
		}
		sheet.setColumnWidth(0, 1000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3000);
		
		CellStyle horizontal = buildStyle(false);
		CellStyle vertical = buildStyle(true);
		
		sheet.createRow(0).createCell(2).setCellValue(Data.SCHOOL_TITLE);
		sheet.createRow(1).createCell(2).setCellValue(Dic.w("details_sheet_title"));
		
		sheet.getRow(0).getCell(2).setCellStyle(boldStyle(false, false));
		CellStyle title = workbook.createCellStyle();
		title.setAlignment(CellStyle.ALIGN_CENTER);
		sheet.getRow(1).getCell(2).setCellStyle(title);
		
		sheet.getRow(0).createCell(0).setCellValue(String.format("%s: %s", Dic.w("subject"), subjName));
		sheet.getRow(1).createCell(0).setCellValue(String.format("%s: %d (%s)", Dic.w("course"), course.grade, course.name));
		
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 5));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));
		
		sheet.getRow(0).setHeight((short)500);
		sheet.getRow(1).setHeight((short)500);
		
		String[] cols = {"number", "name", "fname", "written", "oral", "practical", "activity", "homework", "total", "in_letters"};
		sheet.createRow(3);
		for(int i=0; i<cols.length; i++){
			sheet.getRow(3).createCell(i).setCellValue(Dic.w(cols[i]));
			sheet.getRow(3).getCell(i).setCellStyle(vertical);
		}
		sheet.getRow(3).getCell(1).setCellStyle(horizontal);
		sheet.getRow(3).getCell(2).setCellStyle(horizontal);
		sheet.getRow(3).getCell(9).setCellStyle(horizontal);
		
		if(season == 0) season = 2;
		Vector<Object[]> rows = markData.seasonMarks(course, sub, season, "", status, 0);
		int r = 4;
		int n = 1;
		for(Object[] row : rows)
		{
			sheet.createRow(r);
			sheet.getRow(r).createCell(0).setCellValue(n++);
			sheet.getRow(r).createCell(1).setCellValue(row[2].toString());
			sheet.getRow(r).createCell(2).setCellValue(row[3].toString());
			sheet.getRow(r).getCell(0).setCellStyle(horizontal);
			sheet.getRow(r).getCell(1).setCellStyle(horizontal);
			sheet.getRow(r).getCell(2).setCellStyle(horizontal);
			
			for(int i=3; i<9; i++){
				sheet.getRow(r).createCell(i).setCellStyle(horizontal);
				if(row[i+1] != null){
					sheet.getRow(r).getCell(i).setCellValue((float)row[i+1]);
				}
			}
			sheet.getRow(r).createCell(9).setCellStyle(horizontal);
			r++;
		}
		setRegionBorder(sheet, new CellRangeAddress(3, r-1, 0, 9));
	}
	public void firstGrade()
	{
		sheet = makeSheet(true, Dic.w("monthly_evaluation_sheet"));
		for(int i=0; i<17; i++){
			sheet.setColumnWidth(i, 1200);
		}
		sheet.setColumnWidth(5, 2400);
		sheet.setColumnWidth(10, 1700);
		sheet.setColumnWidth(17, 1700);
		for(int i=18; i<26; i++){
			sheet.setColumnWidth(i, 900);
		}
		sheet.setColumnWidth(17, 2300);
		
		CellStyle horizontal = buildStyle(false);
		CellStyle vertical = buildStyle(true);
		CellStyle bold = boldStyle(false, false);
		
		sheet.createRow(0);
		sheet.createRow(1);
		sheet.createRow(2);
		
		sheet.getRow(0).setHeight((short)500);
		sheet.getRow(0).createCell(7).setCellValue(Dic.w("monthly_evaluation_sheet"));
		sheet.getRow(0).getCell(7).setCellStyle(bold);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 18));
		
		sheet.getRow(1).createCell(0).setCellValue(Dic.w("number"));
		sheet.getRow(1).createCell(1).setCellValue(Dic.w("identity"));
		sheet.getRow(1).createCell(4).setCellValue(Dic.w("base_code"));
		sheet.getRow(1).createCell(5).setCellValue(Dic.w("subjects"));
		sheet.getRow(1).createCell(6).setCellValue(Dic.w("monthly_marks"));
		
		sheet.getRow(1).getCell(0).setCellStyle(vertical);
		sheet.getRow(1).getCell(4).setCellStyle(vertical);
		sheet.getRow(1).getCell(1).setCellStyle(bold);
		sheet.getRow(1).getCell(5).setCellStyle(bold);
		sheet.getRow(1).getCell(6).setCellStyle(bold);
		
		sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(1, 2, 4, 4));
		
		CellRangeAddress range0 = new CellRangeAddress(1, 2, 5, 5);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range0, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range0, sheet, workbook);
		sheet.addMergedRegion(range0);
		
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 6, 16));
		
		sheet.getRow(2).createCell(1).setCellValue(Dic.w("name"));
		sheet.getRow(2).createCell(2).setCellValue(Dic.w("fname"));
		sheet.getRow(2).createCell(3).setCellValue(Dic.w("gfname"));
		
		sheet.getRow(2).getCell(1).setCellStyle(vertical);
		sheet.getRow(2).getCell(2).setCellStyle(vertical);
		sheet.getRow(2).getCell(3).setCellStyle(vertical);
		
		String[] cols1 = {"hamal", "sawr", "jawza", "saratan", "midterm_total", "asad", "sonbola", "mizan"};
		int c = 6;
		for(String col : cols1){
			sheet.getRow(2).createCell(c).setCellValue(Dic.w(col));
			sheet.getRow(2).getCell(c++).setCellStyle(vertical);
		}
		sheet.getRow(2).createCell(15).setCellValue(Dic.w("aqrab"));
		sheet.getRow(2).getCell(15).setCellStyle(vertical);
		CellRangeAddress range1 = new CellRangeAddress(2, 2, 13, 14);
		CellRangeAddress range2 = new CellRangeAddress(2, 2, 15, 16);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, range1, sheet, workbook);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, range2, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range2, sheet, workbook);
		sheet.addMergedRegion(range1);
		sheet.addMergedRegion(range2);
		
		String[] cols2 = {"final_total", "general_total", "result", "degree", "educ_year"};
		c = 17;
		for(String col : cols2){
			sheet.getRow(1).createCell(c).setCellStyle(vertical);
			sheet.getRow(1).getCell(c).setCellValue(Dic.w(col));
			CellRangeAddress range = new CellRangeAddress(1, 2, c, c);
			RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
			sheet.addMergedRegion(range);
			c++;
		}
		sheet.getRow(1).createCell(22).setCellValue(Dic.w("attendance_rules"));
		sheet.getRow(1).getCell(22).setCellStyle(bold);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 22, 25));
		
		String[] cols3 = {"present", "absent", "sick", "holiday"};
		c = 22;
		for(String col : cols3){
			sheet.getRow(2).createCell(c).setCellStyle(vertical);
			sheet.getRow(2).getCell(c).setCellValue(Dic.w(col));
			c++;
		}
		sheet.getRow(1).createCell(26).setCellValue(Dic.w("description"));
		sheet.getRow(1).getCell(26).setCellStyle(horizontal);
		sheet.addMergedRegion(new CellRangeAddress(1, 2, 26, 26));
		
		setRegionBorder(sheet, new CellRangeAddress(1, 2, 0, 26));
		
		int subjCount = subjData.gradeSubjCount(course.grade);
		Vector<Object[]> studs = markData.studentsList(course.id);
		
		int r = 3;
		int n = 1;
		for(Object[] stud : studs)
		{
			int sid = (int) stud[4];
			sheet.createRow(r);
			sheet.getRow(r).createCell(0).setCellValue(n++);
			for(int i=0; i<3; i++){
				sheet.getRow(r).createCell(i+1).setCellValue(stud[i].toString());
				sheet.getRow(r).getCell(i+1).setCellStyle(vertical);
			}
			sheet.getRow(r).createCell(4).setCellValue((int)stud[3]);
			sheet.getRow(r).getCell(4).setCellStyle(vertical);
			
			Vector<Object[]> midMarks = markData.seasonMarks(sid, course.year, course.grade, 1, false);
			int totalMid = 0, totalFin = 0;
			float[] totals = new float[midMarks.size()];
			boolean finish = true;
			int fails = 0;
			int passes = 0;
			int rs = r;
			int a = 0;
			for(Object[] scores : midMarks)
			{
				if(sheet.getRow(r) == null){
					sheet.createRow(r);
				}
				sheet.getRow(r).createCell(5).setCellValue(scores[1].toString());
				sheet.getRow(r).getCell(5).setCellStyle(horizontal);
				for(int i=2; i<7; i++){
					sheet.getRow(r).createCell(i+4).setCellStyle(horizontal);
					if(scores[i] != null){
						sheet.getRow(r).getCell(i+4).setCellValue((float)scores[i]);
					}
				}
				if(scores[6] != null){
					totalMid += (float) scores[6];
					totals[a] = (float) scores[6];
				}
				else{
					finish = false;
					totals[a] = -1;
				}
				a++;
				r++;
			}
			Vector<Object[]> finMarks = markData.seasonMarks(sid, course.year, course.grade, 2, false);
			r = rs;
			a = 0;
			for(Object[] scores : finMarks)
			{
				if(sheet.getRow(r) == null){
					sheet.createRow(r);
				}
				for(int i=2; i<9; i++){
					sheet.getRow(r).createCell(i+9).setCellStyle(horizontal);
					if(scores[i] != null){
						sheet.getRow(r).getCell(i+9).setCellValue((float)scores[i]);
					}
				}
				if(scores[8] != null){
					totalFin += (float) scores[8];
					if(totals[a] != -1){
						float totMark = totals[a] + (float) scores[8];
						if(totMark < Data.PASS_GRADE_FINAL){
							fails++;
						}
						else passes++;
					}
				}
				else finish = false;
				r++;
				a++;
			}
			sheet.getRow(rs).createCell(18).setCellStyle(vertical);
			sheet.getRow(rs).createCell(19).setCellStyle(vertical);
			sheet.getRow(rs).createCell(20).setCellStyle(vertical);
			
			float total = totalMid + totalFin;
			sheet.getRow(rs).getCell(18).setCellValue(total);
			
			String result = "";
			if(passes >= subjCount){
				result = "passed";
			}
			else if(fails > 0){
				if(fails >= Data.FAIL_SUBJ_COUNT){
					result = "failed";
				}
				else{
					result = "eventual";
				}
			}
			sheet.getRow(rs).getCell(19).setCellValue(Dic.w(result));
			if(finish){
				float subs = finMarks.size();
				if(subs > 0){
					float avg = total/subs;
					int pos = markData.studentPosition(sid, course.year, course.grade, 2, avg);
					sheet.getRow(rs).getCell(20).setCellValue(pos);
				}
			}
			sheet.getRow(rs).createCell(21).setCellValue(Data.ATTEND_MID + Data.ATTEND_FINAL);
			sheet.getRow(rs).getCell(21).setCellStyle(vertical);
			int[][] attend = markData.studentAttendSum(sid, course.year);
			for(int i=0; i<4; i++){
				sheet.getRow(rs).createCell(i+22).setCellValue(attend[2][i]);
				sheet.getRow(rs).getCell(i+22).setCellStyle(vertical);
			}
			sheet.createRow(r);
			for(int i=5; i<18; i++){
				sheet.getRow(r).createCell(i).setCellStyle(horizontal);
			}
			for(int i=0; i<5; i++){
				CellRangeAddress range = new CellRangeAddress(rs, r, i, i);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
				sheet.addMergedRegion(range);
			}
			for(int i=18; i<27; i++){
				CellRangeAddress range = new CellRangeAddress(rs, r, i, i);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workbook);
				sheet.addMergedRegion(range);
			}
			setRegionBorder(sheet, new CellRangeAddress(rs, r, 0, 26));
			
			r++;
		}
		setRegionBorder(sheet, new CellRangeAddress(1, r-1, 6, 10));
		setRegionBorder(sheet, new CellRangeAddress(1, r-1, 18, 20));
		
		CellRangeAddress range001 = new CellRangeAddress(1, r-1, 10, 10);
		CellRangeAddress range002 = new CellRangeAddress(1, r-1, 17, 17);
		
		RegionUtil.setBorderLeft(CellStyle.BORDER_DOUBLE, range001, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_DOUBLE, range002, sheet, workbook);
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
































