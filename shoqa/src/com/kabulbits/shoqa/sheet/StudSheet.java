package com.kabulbits.shoqa.sheet;

import java.util.Vector;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.MarkData;
import com.kabulbits.shoqa.db.Student;
import com.kabulbits.shoqa.db.StudentData;
import com.kabulbits.shoqa.db.Subject;
import com.kabulbits.shoqa.db.SubjectData;
import com.kabulbits.shoqa.util.Dic;

public class StudSheet extends Report 
{
	private SubjectData subjData;
	private MarkData markData;
	private StudentData studData;
	private Sheet sheet01;
	private Sheet sheet02;
	private int sid;
	
	public StudSheet(int id)
	{
		sid = id;
		subjData = new SubjectData();
		markData = new MarkData();
		studData = new StudentData();
		
		horizontal = buildStyle(false);
		vertical = buildStyle(true);
	}
	private CellStyle horizontal;
	private CellStyle vertical;
	
	public void studDetails(Student stud)
	{
		sheet01 = makeSheet(Dic.w("student_details"));
		sheet01.setColumnWidth(0, 1000);
		sheet01.setColumnWidth(1, 3000);
		sheet01.setColumnWidth(2, 3600);
		sheet01.setColumnWidth(3, 1300);
		sheet01.setColumnWidth(4, 1300);
		for(int i=5; i<9; i++){
			sheet01.setColumnWidth(i, 3000);
		}
		sheet01.createRow(0).createCell(3).setCellValue(Data.MINISTRY_TITLE);
		sheet01.createRow(1).createCell(3).setCellValue(Data.HEAD_TITLE);
		sheet01.createRow(2).createCell(3).setCellValue(Data.SCHOOL_TITLE);
		sheet01.createRow(3).createCell(3).setCellValue(Dic.w("student_details"));
		
		CellStyle center = workbook.createCellStyle();
		center.setAlignment(CellStyle.ALIGN_CENTER);
		
		sheet01.getRow(0).getCell(3).setCellStyle(center);
		sheet01.getRow(1).getCell(3).setCellStyle(center);
		sheet01.getRow(2).getCell(3).setCellStyle(boldStyle(false, false));
		sheet01.getRow(3).getCell(3).setCellStyle(center);
		
		for(int i=0; i<4; i++){
			sheet01.getRow(i).setHeight((short)500);
			sheet01.addMergedRegion(new CellRangeAddress(i, i, 3, 6));
		}
		CellStyle bold = boldStyle(true, true);
		
		String[] txts = {"name", "lname", "fname", "gfname", "birth_date", "birth_place", "blood_group", "idcard_no", 
				"main_addr", "curr_addr", "course", "doc_no", "date", "course", "doc_no", "date", "reason"
		};
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd", new ULocale("@calendar=persian"));
		String mainAddr = studData.studMainAddr(sid);
		String currAddr = studData.studCurrAddr(sid);
		
		String[] values = {stud.name, stud.lname, stud.fname, stud.gfname, df.format(stud.birthDate), stud.birthPlace, stud.bloodGroup,
				stud.idcard, mainAddr, currAddr, String.valueOf(stud.grade), String.valueOf(stud.docNo), df.format(stud.docDate),"","","","",
		};
		int r = 4;
		for(int i=0; i<txts.length; i++)
		{
			sheet01.createRow(r);
			sheet01.getRow(r).createCell(1).setCellValue(Dic.w(txts[i]));
			sheet01.getRow(r).createCell(2).setCellValue(values[i]);
			sheet01.getRow(r).getCell(1).setCellStyle(horizontal);
			sheet01.getRow(r).getCell(2).setCellStyle(horizontal);
			r++;
		}
		sheet01.getRow(4).createCell(0).setCellValue(Dic.w("student_identity"));
		sheet01.getRow(4).getCell(0).setCellStyle(bold);
		
		sheet01.getRow(14).createCell(0).setCellValue(Dic.w("involvement"));
		sheet01.getRow(14).getCell(0).setCellStyle(bold);
		
		sheet01.getRow(17).createCell(0).setCellValue(Dic.w("detach"));
		sheet01.getRow(17).getCell(0).setCellStyle(bold);
		
		sheet01.addMergedRegion(new CellRangeAddress(4, 13, 0, 0));
		sheet01.addMergedRegion(new CellRangeAddress(14, 16, 0, 0));
		sheet01.addMergedRegion(new CellRangeAddress(17, 20, 0, 0));
		
		for(int i=4; i<9; i++){
			CellRangeAddress range = new CellRangeAddress(i, i, 2, 6);
			RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, range, sheet01, workbook);
			sheet01.addMergedRegion(range);
		}
		for(int i=9; i<21; i++){
			CellRangeAddress range = new CellRangeAddress(i, i, 2, 8);
			RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, range, sheet01, workbook);
			sheet01.addMergedRegion(range);
		}
		CellRangeAddress range = new CellRangeAddress(14, 16, 0, 8);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, range, sheet01, workbook);
		RegionUtil.setBorderTop(CellStyle.BORDER_DOUBLE, range, sheet01, workbook);
		
		String[] cols = {"parents_and_relations", "", "name_and_lname", "age", "language", "educ_level", "educ_field", "job", "living_location"};
		sheet01.createRow(22);
		for(int i=0; i<9; i++){
			sheet01.getRow(22).createCell(i).setCellValue(Dic.w(cols[i]));
			sheet01.getRow(22).getCell(i).setCellStyle(horizontal);
		}
		String[] strs = {"father", "mother", "brother", "uncle", "uncle2"};
		r = 23;
		for(String str : strs){
			sheet01.createRow(r).setHeight((short)500);
			for(int i=1; i<9; i++){
				sheet01.getRow(r).createCell(i).setCellStyle(horizontal);
			}
			sheet01.getRow(r).getCell(1).setCellValue(Dic.w(str));
			String[] info = studData.studExactRelation(sid, str);
			if(info != null){
				int c = 2;
				for(String item : info){
					sheet01.getRow(r).getCell(c++).setCellValue(item);
				}
			}
			r++;
		}
		sheet01.getRow(23).createCell(0).setCellValue(Dic.w("parents"));
		sheet01.getRow(25).createCell(0).setCellValue(Dic.w("relations"));
		
		sheet01.getRow(23).getCell(0).setCellStyle(bold);
		sheet01.getRow(25).getCell(0).setCellStyle(bold);
		
		sheet01.addMergedRegion(new CellRangeAddress(22, 22, 0, 1));
		sheet01.addMergedRegion(new CellRangeAddress(23, 24, 0, 0));
		sheet01.addMergedRegion(new CellRangeAddress(25, 27, 0, 0));
		
		setRegionBorder(sheet01, new CellRangeAddress(4, 20, 0, 8));
		setRegionBorder(sheet01, new CellRangeAddress(21, 21, 0, 8));
		setRegionBorder(sheet01, new CellRangeAddress(22, 27, 0, 8));
		
		if(stud.image != null){
			addImage(sheet01, stud.image, 4, 7, .7);
		}else{
			sheet01.getRow(4).createCell(7).setCellValue(Dic.w("photo"));
			sheet01.getRow(4).getCell(7).setCellStyle(boldStyle(false, true));
			CellRangeAddress photo = new CellRangeAddress(4, 8, 7, 8);
			setRegionBorder(sheet01, photo);
			sheet01.addMergedRegion(photo);
		}
		addImage(sheet01, schoolLogo, 0, 7);
	}
	public void fullMarks(int currGrade)
	{
		sheet02 = makeSheet(Dic.w("one_to_twelve_marks"));
		for(int i=2; i<14; i++){
			sheet02.setColumnWidth(i, 1600);
		}
		sheet02.setColumnWidth(0, 1000);
		sheet02.setColumnWidth(1, 3000);
		
		CellStyle bold = boldStyle(false, true);
		
		sheet02.createRow(0).createCell(0).setCellValue(Dic.w("one_to_twelve_marks"));
		sheet02.getRow(0).getCell(0).setCellStyle(bold);
		sheet02.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
		
		int r = 1;
		for(; r<4; r++){
			sheet02.createRow(r);
			for(int i=0; i<14; i++){
				sheet02.getRow(r).createCell(i).setCellStyle(horizontal);
			}
		}
		sheet02.getRow(1).getCell(0).setCellValue(Dic.w("number"));
		sheet02.getRow(1).getCell(1).setCellValue(Dic.w("educ_year"));
		sheet02.getRow(2).getCell(1).setCellValue(Dic.w("subjects"));
		
		sheet02.getRow(1).getCell(0).setCellStyle(boldStyle(true, true));
		sheet02.getRow(1).getCell(1).setCellStyle(bold);
		sheet02.getRow(2).getCell(1).setCellStyle(bold);
		
		sheet02.addMergedRegion(new CellRangeAddress(1, 3, 0, 0));
		sheet02.addMergedRegion(new CellRangeAddress(2, 3, 1, 1));
		
		String[] cols = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth"};
		int c = 2;
		for(String col : cols){
			sheet02.getRow(2).getCell(c++).setCellValue(Dic.w(col));
		}
		Vector<Object[]> subs = subjData.subjectsList();
		int n = 1;
		for(Object[] item : subs){
			sheet02.createRow(r);
			for(int i=0; i<14; i++){
				sheet02.getRow(r).createCell(i).setCellStyle(horizontal);
			}
			Subject sub = (Subject) item[0];
			sheet02.getRow(r).getCell(0).setCellValue(n++);
			sheet02.getRow(r).getCell(1).setCellValue(sub.name);
			r++;
		}
		CellRangeAddress range01 = new CellRangeAddress(4, r-1, 0, 13);
		RegionUtil.setBorderTop(CellStyle.BORDER_DOUBLE, range01, sheet02, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, range01, sheet02, workbook);
		
		String[] txts01 = {"total", "average", "result", "degree"};
		for(String txt : txts01){
			sheet02.createRow(r);
			for(int i=0; i<14; i++){
				sheet02.getRow(r).createCell(i).setCellStyle(bold);
			}
			sheet02.getRow(r).getCell(0).setCellValue(Dic.w(txt));
			sheet02.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
			r++;
		}
		String[] txts02 = {"educ_year", "present", "absent", "sick", "holiday"};
		for(String txt : txts02){
			sheet02.createRow(r);
			for(int i=0; i<14; i++){
				sheet02.getRow(r).createCell(i).setCellStyle(horizontal);
			}
			sheet02.getRow(r).getCell(1).setCellValue(Dic.w(txt));
			r++;
		}
		sheet02.getRow(r-5).getCell(0).setCellValue(Dic.w("attendance_rules"));
		sheet02.getRow(r-5).getCell(0).setCellStyle(boldStyle(true, true));
		sheet02.addMergedRegion(new CellRangeAddress(r-5, r-1, 0, 0));
		
		CellRangeAddress range02 = new CellRangeAddress(r-5, r-1, 0, 13);
		RegionUtil.setBorderTop(CellStyle.BORDER_DOUBLE, range02, sheet02, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, range02, sheet02, workbook);
		
		setRegionBorder(sheet02, new CellRangeAddress(0, r, 0, 13));
		
		for(int g=1; g<=currGrade; g++)
		{
			r = 4;
			float total = 0;
			int count = 0;
			int year = markData.gradePassingYear(sid, g);
			if(year == 0) continue;
			Vector<Object> scores = markData.studGradeMarks(sid, g, year);
			for(Object score : scores){
				if(score != null){
					sheet02.getRow(r).getCell(g+1).setCellValue((float)score);
					count++;
					total += (float) score;
				}
				r++;
			}
			sheet02.getRow(r++).getCell(g+1).setCellValue(total);
			float avg = 0;
			if(count > 0){
				avg = total/count;
				sheet02.getRow(r++).getCell(g+1).setCellValue(avg);
			}
			sheet02.getRow(r++).getCell(g+1).setCellValue("");
			int pos = markData.studentPosition(sid, year, g, 2, avg);
			sheet02.getRow(r++).getCell(g+1).setCellValue(pos);

			sheet02.getRow(r++).getCell(g+1).setCellValue(subjData.findConfigs(year)[5]);
			
			int[][] attend = markData.studentAttendSum(sid, year);
			for(int i=0; i<attend[2].length; i++){
				sheet02.getRow(r++).getCell(g+1).setCellValue(attend[2][i]);
			}
		}
	}
	public void abilityExam(int year, int grade, String name, String fname)
	{
		sheet01 = makeSheet(Dic.w("ability_exam"));
		for(int i=2; i<40; i++){
			sheet01.setColumnWidth(i, 1200);
		}
		for(int i=2; i<14; i++){
			sheet01.createRow(i);
		}
		sheet01.getRow(6).setHeight((short)900);
		sheet01.getRow(12).setHeight((short)900);
		
		String text = String.format("%s: %s     %s: %s     %s: %d", Dic.w("name"), name, Dic.w("fname"), fname, Dic.w("educ_year"), year);
		sheet01.createRow(1).createCell(0).setCellValue(text);
		sheet01.addMergedRegion(new CellRangeAddress(1, 1, 0, 13));
		
		sheet01.getRow(3).createCell(0).setCellValue(Dic.w("subjects"));
		sheet01.getRow(3).getCell(0).setCellStyle(horizontal);
		sheet01.addMergedRegion(new CellRangeAddress(3, 4, 0, 1));
		
		sheet01.getRow(5).createCell(0).setCellValue(Dic.w("marks"));
		sheet01.getRow(5).getCell(0).setCellStyle(vertical);
		sheet01.addMergedRegion(new CellRangeAddress(5, 6, 0, 0));
		
		sheet01.getRow(5).createCell(1).setCellValue(Dic.w("in_numbers"));
		sheet01.getRow(6).createCell(1).setCellValue(Dic.w("in_letters"));
		
		sheet01.getRow(5).getCell(1).setCellStyle(horizontal);
		sheet01.getRow(6).getCell(1).setCellStyle(horizontal);
		
		Vector<Object[]> prevGradeMarks = markData.fullMarks(sid, year, grade-1, 1, false);
		int c = 2;
		float total = 0;
		int count = 0;
		for(Object[] score : prevGradeMarks)
		{
			sheet01.getRow(3).createCell(c).setCellStyle(vertical);
			sheet01.getRow(4).createCell(c).setCellStyle(vertical);
			sheet01.getRow(5).createCell(c).setCellStyle(horizontal);
			sheet01.getRow(6).createCell(c).setCellStyle(horizontal);
			
			sheet01.getRow(3).getCell(c).setCellValue(score[1].toString());
			sheet01.addMergedRegion(new CellRangeAddress(3, 4, c, c));
			if(score[4] != null){
				sheet01.getRow(5).getCell(c).setCellValue((float)score[4]);
				total += (float) score[4];
				count++;
			}
			c++;
		}
		int sc = c;
		String[] txts01 = {"total", "result", "degree"};
		for(String txt : txts01){
			sheet01.getRow(3).createCell(c).setCellStyle(vertical);
			sheet01.getRow(4).createCell(c).setCellStyle(vertical);
			sheet01.getRow(3).getCell(c).setCellValue(Dic.w(txt));
			sheet01.addMergedRegion(new CellRangeAddress(3, 4, c, c));
			c++;
		}
		sheet01.getRow(5).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(6).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(5).getCell(sc).setCellValue(total);
		sc++;
		
		sheet01.getRow(5).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(6).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(5).getCell(sc).setCellValue("");
		sc++;
		
		float avg = 0;
		if(count > 0){
			avg = total/count;
		}
		int pos = markData.studentPosition(sid, year, grade-1, 2, avg);
		sheet01.getRow(5).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(6).createCell(sc).setCellStyle(horizontal);
		sheet01.getRow(5).getCell(sc).setCellValue(pos);
		sc++;
		
		sc = c;
		String[] txts02 = {"present", "absent", "sick", "holiday"};
		for(String txt : txts02){
			sheet01.getRow(4).createCell(c).setCellStyle(vertical);
			sheet01.getRow(4).getCell(c).setCellValue(Dic.w(txt));
			c++;
		}
		sheet01.getRow(3).createCell(sc).setCellValue(Dic.w("attendance_rules"));
		sheet01.getRow(3).getCell(sc).setCellStyle(boldStyle(false, true));
		sheet01.addMergedRegion(new CellRangeAddress(3, 3, sc, sc+3));
		
		int[][] attend = markData.studentAttendSum(sid, year);
		for(int i=0; i<attend[2].length; i++)
		{
			sheet01.getRow(5).createCell(sc).setCellStyle(horizontal);
			sheet01.getRow(6).createCell(sc).setCellStyle(horizontal);
			sheet01.getRow(5).getCell(sc).setCellValue(attend[2][i]);
			sc++;
		}
		sheet01.getRow(3).createCell(sc).setCellValue(Dic.w("photo"));
		sheet01.getRow(3).getCell(sc).setCellStyle(boldStyle(false, true));
		sheet01.addMergedRegion(new CellRangeAddress(3, 4, sc, sc));
		sheet01.addMergedRegion(new CellRangeAddress(5, 6, sc, sc));
		sheet01.setColumnWidth(sc, 2500);
		
		sheet01.getRow(10).createCell(0).setCellValue(Dic.w("subjects"));
		sheet01.getRow(10).getCell(0).setCellStyle(horizontal);
		sheet01.addMergedRegion(new CellRangeAddress(10, 10, 0, 1));
		
		sheet01.getRow(11).createCell(0).setCellValue(Dic.w("marks"));
		sheet01.getRow(11).getCell(0).setCellStyle(vertical);
		sheet01.addMergedRegion(new CellRangeAddress(11, 12, 0, 0));
		
		sheet01.getRow(11).createCell(1).setCellValue(Dic.w("in_numbers"));
		sheet01.getRow(12).createCell(1).setCellValue(Dic.w("in_letters"));
		
		sheet01.getRow(11).getCell(1).setCellStyle(horizontal);
		sheet01.getRow(12).getCell(1).setCellStyle(horizontal);
		
		Vector<Object[]> newGradeMarks = markData.fullMarks(sid, year, grade, 3, false);
		total = 0;
		c = 2;
		for(Object[] score : newGradeMarks)
		{
			sheet01.getRow(10).createCell(c).setCellStyle(vertical);
			sheet01.getRow(11).createCell(c).setCellStyle(horizontal);
			sheet01.getRow(12).createCell(c).setCellStyle(horizontal);
			
			sheet01.getRow(10).getCell(c).setCellValue(score[1].toString());
			if(score[4] != null){
				sheet01.getRow(11).getCell(c).setCellValue((float)score[4]);
				total += (float) score[4];
			}
			c++;
		}
		sheet01.getRow(10).createCell(c).setCellStyle(vertical);
		sheet01.getRow(11).createCell(c).setCellStyle(horizontal);
		sheet01.getRow(12).createCell(c).setCellStyle(horizontal);
		sheet01.getRow(10).getCell(c).setCellValue(Dic.w("total"));
		sheet01.getRow(11).getCell(c).setCellValue(total);
		c++;
		
		sheet01.getRow(10).createCell(c).setCellStyle(vertical);
		sheet01.getRow(11).createCell(c).setCellStyle(horizontal);
		sheet01.getRow(12).createCell(c).setCellStyle(horizontal);
		sheet01.getRow(10).getCell(c).setCellValue(Dic.w("result"));
		c++;
		
		sheet01.getRow(10).createCell(c).setCellStyle(horizontal);
		sheet01.getRow(10).getCell(c).setCellValue(Dic.w("description"));
		
		sheet01.addMergedRegion(new CellRangeAddress(10, 10, c, sc));
		sheet01.addMergedRegion(new CellRangeAddress(11, 12, c, sc));
		
		setRegionBorder(sheet01, new CellRangeAddress(3, 7, 0, sc));
		setRegionBorder(sheet01, new CellRangeAddress(9, 14, 0, sc));
		
		CellRangeAddress range01 = new CellRangeAddress(3, 4, 0, sc);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, range01, sheet01, workbook);
		
		CellRangeAddress range02 = new CellRangeAddress(10, 10, 0, sc);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, range02, sheet01, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_DOUBLE, range02, sheet01, workbook);
	}
	@Override
	protected void finalize() throws Throwable{
		if(markData != null){
			markData.closeConn();
		}
		if(subjData != null){
			subjData.closeConn();
		}
		if(studData != null){
			studData.closeConn();
		}
		super.finalize();
	}
}





















