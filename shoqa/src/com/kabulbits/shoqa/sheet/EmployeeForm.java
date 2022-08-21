package com.kabulbits.shoqa.sheet;

import java.util.Calendar;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.db.Employee;
import com.kabulbits.shoqa.util.Dic;

public class EmployeeForm extends Report 
{
	private Employee emp;
	private Sheet sheet;
	
	public EmployeeForm(Employee emp){
		this.emp = emp;
	}
	
	public void manager()
	{
		sheet = makeSheet(true, Dic.w("manager_form"));
		CellStyle vertical = buildStyle(true);
		CellStyle center = buildStyle(false, false);
		
		sheet.createRow(5).createCell(9).setCellValue(Data.MINISTRY_TITLE);
		sheet.createRow(6).createCell(9).setCellValue(Dic.w("head_of_plan"));
		sheet.createRow(7).createCell(9).setCellValue(Data.SCHOOL_TITLE);
		sheet.createRow(8).createCell(9).setCellValue(Dic.w("manager_identity"));
		
		sheet.getRow(5).getCell(9).setCellStyle(center);
		sheet.getRow(6).getCell(9).setCellStyle(center);
		sheet.getRow(7).getCell(9).setCellStyle(boldStyle(false, false));
		sheet.getRow(8).getCell(9).setCellStyle(center);
		
		for(int i=5; i<9; i++){
			sheet.addMergedRegion(new CellRangeAddress(i, i, 9, 16));
		}
		String[] labels = {"name", "lname", "fname", "gfname", "main_addr", "curr_addr", "age", "phone", "idcard_no", "educ_level",
				"gradu_place", "gradu_year", "service_duration", "previous_job", "national_langs", "international_langs", "educ_seminars",
				"abroad_tours", "province_tours", "ngo_work_experience", "crimes", "punishments", "sign_place"
		};
		Calendar cal = Calendar.getInstance();
		int now = cal.get(Calendar.YEAR);
		cal.setTime(emp.birthDate);
		int age = now - cal.get(Calendar.YEAR);
		String[] levels = {"illiterate", "essential_literacy", "12th_grade", "14th_grade", "bachelor", "masters", "phd"};
		
		String[] values = {emp.name, emp.lname, emp.fname, emp.gfname, emp.mainAddress, emp.currAddress, String.valueOf(age), emp.phone, 
				emp.idcard, Dic.w(levels[emp.educLevel]), emp.graduPlace, emp.graduYear, emp.serviceDuration, emp.previousJob, 
				emp.nationalLangs, emp.internLangs, emp.educSeminars, emp.abroudTours, emp.provinceTours, emp.ngoExpr, emp.crimes, emp.punishments, ""
		};
		sheet.createRow(10);
		sheet.createRow(11);
		
		for(int i=0; i<values.length; i++)
		{
			sheet.setColumnWidth(i, (short)1500);
			sheet.getRow(10).createCell(i).setCellStyle(vertical);
			sheet.getRow(11).createCell(i).setCellStyle(vertical);
			sheet.getRow(10).getCell(i).setCellValue(Dic.w(labels[i]));
			sheet.getRow(11).getCell(i).setCellValue(values[i]);
		}
		sheet.setColumnWidth(22, (short)3000);
		setRegionBorder(sheet, new CellRangeAddress(10, 11, 0, 22));
		
		addImage(sheet, ministryLogo, 0, 12);
	}
	public void simple()
	{
		sheet = makeSheet(true, Dic.w("attributes_form"));
		CellStyle horizontal = buildStyle(false);
		
		sheet.createRow(0).createCell(1).setCellValue(Data.SCHOOL_TITLE);
		sheet.createRow(1).createCell(1).setCellValue(Dic.w("attributes_form_title"));
		
		sheet.getRow(0).getCell(1).setCellStyle(boldStyle(false, false));
		sheet.getRow(1).getCell(1).setCellStyle(buildStyle(false, false));
		
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 2));
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2));
		
		sheet.getRow(0).setHeight((short)500);
		sheet.getRow(1).setHeight((short)500);
		
		String[] labels = {"name_and_lname", "fname", "gfname", "main_addr", "curr_addr", "idcard_no", "phone", "educ_level", "educ_field",
				"gradu_place", "gradu_year", "service_duration", "international_langs", "educ_seminars", "abroad_tours", "province_tours", "crimes", "punishments"
		};
		String fullName = String.format("%s %s", emp.name, emp.lname);
		String[] levels = {"illiterate", "essential_literacy", "12th_grade", "14th_grade", "bachelor", "masters", "phd"};
		
		String[] values = {fullName, emp.fname, emp.gfname, emp.mainAddress, emp.currAddress, emp.idcard, emp.phone, 
				Dic.w(levels[emp.educLevel]), emp.educField, emp.graduPlace, emp.graduPlace, emp.serviceDuration,
				emp.internLangs, emp.educSeminars, emp.abroudTours, emp.provinceTours, emp.crimes, emp.punishments
		};
		sheet.createRow(3).setHeight((short)2000);
		for(int i=0; i<4; i++){
			sheet.getRow(3).createCell(i).setCellStyle(horizontal);
			sheet.setColumnWidth(i, 7000);
		}
		sheet.getRow(3).getCell(0).setCellValue(Dic.w("complete_identity"));
		sheet.getRow(3).getCell(1).setCellValue(Dic.w("details"));
		
		int r = 4;
		for(int i=0; i<labels.length; i++)
		{
			sheet.createRow(r).setHeight((short)500);
			sheet.getRow(r).createCell(0).setCellStyle(horizontal);
			sheet.getRow(r).createCell(1).setCellStyle(horizontal);
			sheet.getRow(r).getCell(0).setCellValue(Dic.w(labels[i]));
			sheet.getRow(r).getCell(1).setCellValue(values[i]);
			r++;
		}
		CellRangeAddress range = new CellRangeAddress(4, 21, 2, 3);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, range, sheet, workbook);
		sheet.addMergedRegion(range);
		
		if(emp.image != null){
			addImage(sheet, emp.image, 3, 3, .6);
		}else{
			sheet.getRow(3).getCell(3).setCellValue(Dic.w("photo"));
		}
		addImage(sheet, schoolLogo, 0, 3);
	}
}































