package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Course;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Subject;
import com.kabulbits.shoqa.db.SubjectData;
import com.kabulbits.shoqa.util.Dic;

public class MarkSheet extends Report 
{
	private SubjectData subjData;
	private MarkData markData;
	private Sheet sheet;
	
	public MarkSheet(Course course)
	{
		subjData = new SubjectData();
		markData = new MarkData();
		
		sheet = makeSheet(true, Dic.w("marks_sheet"));
		for(int i=0; i<40; i++){
			sheet.setColumnWidth(i, 850);
		}
		//building styles
		CellStyle vertical = buildStyle(true);
		CellStyle horizontal = buildStyle(false);
		
		//building rows and cells
		int x = 0;
		int y = 25;
		for(int i=0; i<7; i++){
			sheet.createRow(i);
			for(int j=y; j<y+9; j++){
				sheet.getRow(i).createCell(j).setCellStyle(horizontal);
			}
		}
		
		//sheet titles
		int c1 = 8;
		sheet.getRow(1).createCell(c1).setCellValue(Data.SCHOOL_TITLE);
		sheet.getRow(4).createCell(c1).setCellValue(Dic.w("marks_sheet_full_text"));
		sheet.addMergedRegion(new CellRangeAddress(1, 2, c1, c1+10));
		sheet.addMergedRegion(new CellRangeAddress(4, 5, c1, c1+10));
		
		CellStyle schoolTitle = workbook.createCellStyle();
		schoolTitle.setAlignment(CellStyle.ALIGN_CENTER);
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		schoolTitle.setFont(font);
		sheet.getRow(1).getCell(c1).setCellStyle(schoolTitle);
		
		CellStyle sheetTitle = workbook.createCellStyle();
		sheetTitle.setAlignment(CellStyle.ALIGN_CENTER);
		sheet.getRow(4).getCell(c1).setCellStyle(sheetTitle);
		
		//results summary
		sheet.getRow(0).getCell(y+3).setCellValue(Dic.w("half_mark"));
		sheet.getRow(0).getCell(y+5).setCellValue(Dic.w("final_mark"));
		sheet.getRow(0).getCell(y+7).setCellValue(Dic.w("total_mark"));
		String[] words = {"exam", "enroled", "exam_involved", "passed", "failed", "forbidden", "excused"};
		for(String word : words){
			sheet.getRow(x).getCell(y).setCellValue(Dic.w(word));
			sheet.addMergedRegion(new CellRangeAddress(x, x, y, y+2));
			sheet.addMergedRegion(new CellRangeAddress(x, x, y+3, y+4));
			sheet.addMergedRegion(new CellRangeAddress(x, x, y+5, y+6));
			sheet.addMergedRegion(new CellRangeAddress(x, x, y+7, y+8));
			x++;
		}
		setRegionBorder(sheet, new CellRangeAddress(0, 6, y, y+8));
		
		int entry = markData.memberCount(course.id, false);
		sheet.getRow(1).getCell(y+3).setCellValue(entry);
		sheet.getRow(1).getCell(y+5).setCellValue(entry);
		sheet.getRow(1).getCell(y+7).setCellValue(entry);
		
		int forbid = markData.memberCount(course.id, true);
		sheet.getRow(5).getCell(y+3).setCellValue(forbid);
		sheet.getRow(5).getCell(y+5).setCellValue(forbid);
		sheet.getRow(5).getCell(y+7).setCellValue(forbid);
		
		int involved = entry - forbid;
		sheet.getRow(2).getCell(y+3).setCellValue(involved);
		sheet.getRow(2).getCell(y+5).setCellValue(involved);
		sheet.getRow(2).getCell(y+7).setCellValue(involved);
		
		int passedMid = markData.passedCount(course, 1);
		int passedFin = markData.passedCount(course, 2);
		sheet.getRow(3).getCell(y+3).setCellValue(passedMid);
		sheet.getRow(3).getCell(y+5).setCellValue(passedFin);
		sheet.getRow(3).getCell(y+7).setCellValue(passedFin);
		
		int failedMid = markData.failedCount(course, 1);
		int failedFin = markData.failedCount(course, 2);
		sheet.getRow(4).getCell(y+3).setCellValue(failedMid);
		sheet.getRow(4).getCell(y+5).setCellValue(failedFin);
		sheet.getRow(4).getCell(y+7).setCellValue(failedFin);
		
		int excusedMid = markData.excusedCount(course, 1);
		int excusedFin = markData.excusedCount(course, 2);
		sheet.getRow(6).getCell(y+3).setCellValue(excusedMid);
		sheet.getRow(6).getCell(y+5).setCellValue(excusedFin);
		sheet.getRow(6).getCell(y+7).setCellValue(excusedFin);
		
		String text1 = String.format("%s (%d %s)", Dic.w("course"), course.grade, course.name);
		String text2 = String.format("%s %d", Dic.w("educ_year"), course.year);
		sheet.createRow(7).createCell(5).setCellValue(text1);
		sheet.getRow(7).createCell(10).setCellValue(text2);
		sheet.addMergedRegion(new CellRangeAddress(7, 7, 5, 8));
		sheet.addMergedRegion(new CellRangeAddress(7, 7, 10, 14));
		
		int subjCount = subjData.gradeSubjCount(course.grade);

		int r = 8;
		int c = 0;
		Row row = sheet.createRow(r);
		
		String cols [] = {"number", "name", "lname", "gfname", "base_code", "exam"};
		for(String item : cols){
			CellUtil.createCell(row, c++, Dic.w(item), vertical);
		}
		sheet.setColumnWidth(5, 2000);
		row.getCell(5).setCellStyle(horizontal);
		
		Vector<Object[]> subs = subjData.subjectsList();
		for(Object[] item : subs){
			Subject sub = (Subject) item[0];
			CellUtil.createCell(row, c++, sub.name, vertical);
		}
		String cols2 [] = {"total", "result", "degree", "educ_year", "present", "absent", "sick", "holiday", "description"};
		for(String item : cols2){
			CellUtil.createCell(row, c++, Dic.w(item), vertical);
		}
		
		Vector<Object[]> studs = markData.studentsList(course.id);
		int n = 1;
		r++;
		for(Object[] stud : studs)
		{
			sheet.createRow(r+0);
			sheet.createRow(r+1);
			sheet.createRow(r+2);
			
			for(int i=0; i<5; i++){
				CellRangeAddress nameRegion = new CellRangeAddress(r, r+2, i, i);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, nameRegion, sheet, workbook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, nameRegion, sheet, workbook);
				sheet.addMergedRegion(nameRegion);
			}
			sheet.getRow(r+0).createCell(0).setCellValue(n);
			sheet.getRow(r+0).createCell(1).setCellValue(stud[0].toString());
			sheet.getRow(r+0).createCell(2).setCellValue(stud[1].toString());
			sheet.getRow(r+0).createCell(3).setCellValue(stud[2].toString());
			sheet.getRow(r+0).createCell(4).setCellValue((int)stud[3]);
			
			sheet.getRow(r+0).getCell(1).setCellStyle(vertical);
			sheet.getRow(r+0).getCell(2).setCellStyle(vertical);
			sheet.getRow(r+0).getCell(3).setCellStyle(vertical);
			
			sheet.getRow(r+0).createCell(5).setCellValue(Dic.w("half_mark"));
			sheet.getRow(r+1).createCell(5).setCellValue(Dic.w("final_mark"));
			sheet.getRow(r+2).createCell(5).setCellValue(Dic.w("total_mark"));
			
			int sid = (int) stud[4];
			Vector<Object[]> marks = markData.fullMarks(sid, course.year, course.grade, 1, true);
			c = 6;
			float totalMid = 0;
			float totalFin = 0;
			float totalTot = 0;
			int failCountMid = 0;
			int passCountMid = 0;
			int failCountFin = 0;
			int passCountFin = 0;
			float markCountMid = 0;
			float markCountFin = 0;
			
			for(Object[] mark : marks){
				sheet.getRow(r+0).createCell(c);
				sheet.getRow(r+1).createCell(c);
				sheet.getRow(r+2).createCell(c);
				if(mark[2] != null){
					markCountMid++;
					double mid = Double.parseDouble(mark[2].toString());
					if(mid < Data.PASS_GRADE_MID){
						failCountMid++;
					}
					else passCountMid++;
					totalMid += mid;
					sheet.getRow(r+0).getCell(c).setCellValue(mid);
				}
				if(mark[3] != null){
					double fin = Double.parseDouble(mark[3].toString());
					totalFin += fin;
					sheet.getRow(r+1).getCell(c).setCellValue(fin);
				}
				if(mark[4] != null){
					markCountFin++;
					double tot = Double.parseDouble(mark[4].toString());
					if(tot < Data.PASS_GRADE_FINAL){
						failCountFin++;
					}
					else passCountFin++;
					totalTot += tot;
					sheet.getRow(r+2).getCell(c).setCellValue(tot);
				}
				c++;
			}
			sheet.getRow(r+0).createCell(c).setCellValue(totalMid);
			sheet.getRow(r+1).createCell(c).setCellValue(totalFin);
			sheet.getRow(r+2).createCell(c).setCellValue(totalTot);
			
			c++;
			String resMid = "";
			if(passCountMid >= subjCount){
				resMid = "passed";
			}else if(failCountMid > 0){
				resMid = "failed";
			}
			sheet.getRow(r+0).createCell(c).setCellValue(Dic.w(resMid));
			
			String resFin = "";
			if(passCountFin >= subjCount){
				resFin = "passed";
			}else if(failCountFin > 0){
				if(failCountFin >= Data.FAIL_SUBJ_COUNT){
					resFin = "failed";
				}else if(failCountFin < Data.FAIL_SUBJ_COUNT){
					resFin = "eventual";
				}
			}
			sheet.getRow(r+2).createCell(c).setCellValue(Dic.w(resFin));
			
			c++;
			sheet.getRow(r+0).createCell(c);
			sheet.getRow(r+2).createCell(c);
			if(markCountMid >= subjCount){
				float avgMid = totalMid / markCountMid;
				sheet.getRow(r+0).getCell(c).setCellValue(markData.studentPosition(sid, course.year, course.grade, 1, avgMid));
			}
			if(markCountFin >= subjCount){
				float avgFin = totalTot / markCountFin;
				sheet.getRow(r+2).getCell(c).setCellValue(markData.studentPosition(sid, course.year, course.grade, 2, avgFin));
			}
			
			c++;
			sheet.getRow(r+0).createCell(c).setCellValue(Data.ATTEND_MID);
			sheet.getRow(r+1).createCell(c).setCellValue(Data.ATTEND_FINAL);
			sheet.getRow(r+2).createCell(c).setCellValue(Data.ATTEND_MID + Data.ATTEND_FINAL);
			
			c++;
			int[][] attend = markData.studentAttendSum(sid, course.year);
			for(int i=0; i<3; i++){
				for(int j=0; j<4; j++){
					sheet.getRow(r+i).createCell(c+j).setCellValue(attend[i][j]);
				}
			}
			c += 4;
			sheet.getRow(r+1).createCell(c).setCellValue("");
			
			r+=3;
			n++;
		}
		
		// setting styles and borders
		for(int i=9; i<r; i++){
			if(sheet.getRow(i) != null){
				sheet.getRow(i).setHeight((short)500);
			}
		}
		for(int i=9; i<r; i++){
			for(int j=5; j<=c; j++){
				if(sheet.getRow(i) != null){
					if(sheet.getRow(i).getCell(j) != null){
						sheet.getRow(i).getCell(j).setCellStyle(horizontal);
					}
				}
			}
		}
		for(int i=9; i<r; i+=3)
		{
			setRegionBorder(sheet, new CellRangeAddress(i, i+2, 0, c));
			
			CellRangeAddress totalRegion = new CellRangeAddress(i+2, i+2, 5, c);
			RegionUtil.setBorderTop(CellStyle.BORDER_DOUBLE, totalRegion, sheet, workbook);
		}
		CellRangeAddress region0 = new CellRangeAddress(8, 9, 0, c);
		RegionUtil.setBorderTop(CellStyle.BORDER_THICK, region0, sheet, workbook);
		
		CellRangeAddress region1 = new CellRangeAddress(8, r-1, 5, 5);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THICK, region1, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THICK, region1, sheet, workbook);
		
		CellRangeAddress region2 = new CellRangeAddress(8, r-1, c, c);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THICK, region2, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THICK, region2, sheet, workbook);
		
		CellRangeAddress region3 = new CellRangeAddress(8, r-1, c-6, c-6);
		RegionUtil.setBorderRight(CellStyle.BORDER_THICK, region3, sheet, workbook);
		
		CellRangeAddress region4 = new CellRangeAddress(8, r-1, c-9, c-9);
		RegionUtil.setBorderRight(CellStyle.BORDER_THICK, region4, sheet, workbook);
		
		addImage(sheet, ministryLogo, 1, 5);
		addImage(sheet, schoolLogo, 1, 20);
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

















