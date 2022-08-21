package com.kabulbits.shoqa.db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class EmployeeData extends Data 
{
	public void searchEmployees(DefaultTableModel model, String col, String word, int type, int page)
	{
		String sql = "SELECT * FROM employee WHERE 1 ";
		if(type != 0){
			sql = sql.concat(" AND emp_type = " + type);
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%' ", col, word));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY emp_id DESC LIMIT %d , %d", start, LIMIT));
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("emp_id"),
						results.getString("emp_name"),
						results.getString("emp_lname"),
						results.getString("emp_fname"),
						results.getString("emp_idcard"),
						results.getString("emp_phone"),
				};
				model.addRow(row);
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public int countEmployee(String col, String word, int type)
	{
		String sql = "SELECT COUNT(emp_id) AS num FROM employee WHERE 1	";
		if(type != 0){
			sql = sql.concat(" AND emp_type = " + type);
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%' ", col, word));
		}
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("num");
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}
	
	public boolean deleteEmployee(int id) 
	{
		String sql = "DELETE FROM employee WHERE emp_id = " + id;
		try {
			return (statement.executeUpdate(sql) == 1);
		}
		catch (SQLException e)
		{
			if(e.getErrorCode() == 1451){
				Diags.showErr(Diags.FK_ERROR);
			}else{
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
	
	public Vector<Option> searchByName(String word)
	{
		Vector<Option> options = new Vector<>();
		
		String sql = "SELECT emp_id, CONCAT(emp_name,' ',emp_lname) AS full_name FROM employee WHERE 1 ";
		if(word.length() > 0){
			String filter = " HAVING full_name LIKE '%%%s%%' LIMIT 15";
			sql = sql.concat(String.format(filter, word, word, word));
		}
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int id = results.getInt("emp_id");
				String name = results.getString("full_name");
				options.add(new Option(id, name));
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return options;
	}

	public boolean saveEmployee(Employee employee) 
	{
		String sql = "INSERT INTO employee (`emp_name`,`emp_lname`,`emp_fname`,`emp_gfname`,`emp_idcard`,`emp_phone`,`emp_main_address`,`emp_curr_address`,"
				+ "`emp_educ_field`,`emp_gradu_place`,`emp_gradu_year`,`emp_service_dur`,`emp_prev_job`,`emp_ngo_expr`,`emp_educ_seminars`,"
				+ "`emp_national_langs`,`emp_intern_langs`,`emp_abroad_tours`,`emp_prov_tours`,`emp_crimes`,`emp_punishments`,`emp_educ_level`,`emp_type`,`emp_teacher`,"
				+ "`emp_birth_date`,`emp_employ_date`,`emp_leave_date`,`emp_image`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			
			String values [] = employee.values();
			
			int i = 1;
			for(String val : values){
				pst.setString(i++, val);
			}
			pst.setInt(i++, employee.educLevel);
			pst.setInt(i++, employee.empType);
			
			pst.setBoolean(i++, employee.isTeacher);
			
			Date birth, employ, leave;
			
			birth = employee.birthDate != null ? new Date(employee.birthDate.getTime()) : null;
			employ = employee.employDate != null ? new Date(employee.employDate.getTime()) : null;
			leave = employee.leaveDate != null ? new Date(employee.leaveDate.getTime()) : null;
			
			pst.setDate(i++, birth);
			pst.setDate(i++, employ);
			pst.setDate(i++, leave);
			
			pst.setBytes(i++, employee.image);
			
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public Employee findEmployee(int id) 
	{
		String sql = "SELECT * FROM employee WHERE emp_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next())
			{
				Employee employee = new Employee(id);
				
				employee.name = results.getString("emp_name");
				employee.lname = results.getString("emp_lname");
				employee.fname = results.getString("emp_fname");
				employee.gfname = results.getString("emp_gfname");
				employee.idcard = results.getString("emp_idcard");
				employee.phone = results.getString("emp_phone");
				employee.mainAddress = results.getString("emp_main_address");
				employee.currAddress = results.getString("emp_curr_address");
				employee.educField = results.getString("emp_educ_field");
				employee.graduPlace = results.getString("emp_gradu_place");
				employee.graduYear = results.getString("emp_gradu_year");
				employee.serviceDuration = results.getString("emp_service_dur");
				employee.previousJob = results.getString("emp_prev_job");
				employee.ngoExpr = results.getString("emp_ngo_expr");
				employee.educSeminars = results.getString("emp_educ_seminars");
				employee.nationalLangs = results.getString("emp_national_langs");
				employee.internLangs = results.getString("emp_intern_langs");
				employee.abroudTours = results.getString("emp_abroad_tours");
				employee.provinceTours = results.getString("emp_prov_tours");
				employee.crimes = results.getString("emp_crimes");
				employee.punishments = results.getString("emp_punishments");
				
				employee.educLevel = results.getInt("emp_educ_level");
				employee.empType = results.getInt("emp_type");
				employee.isTeacher = results.getBoolean("emp_teacher");
				
				employee.birthDate = results.getDate("emp_birth_date");
				employee.employDate = results.getDate("emp_employ_date");
				employee.leaveDate = results.getDate("emp_leave_date");
				
				employee.image = results.getBytes("emp_image");
				
				return employee;
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	public boolean updateEmployee(int id, Employee employee) 
	{
		String sql = "UPDATE employee SET `emp_name`= ?,`emp_lname`= ?,`emp_fname`= ?,`emp_gfname`= ?,`emp_idcard` = ?,`emp_phone` = ?,"
				+ "`emp_main_address`= ?,`emp_curr_address`= ?,`emp_educ_field`= ?,`emp_gradu_place`= ?,`emp_gradu_year`= ?,"
				+ "`emp_service_dur`= ?,`emp_prev_job`= ?,`emp_ngo_expr`= ?,`emp_educ_seminars`= ?,`emp_national_langs`= ?,"
				+ "`emp_intern_langs`= ?,`emp_abroad_tours`= ?,`emp_prov_tours`= ?,`emp_crimes`= ?,`emp_punishments`= ?,`emp_educ_level`= ?,"
				+ "`emp_type`= ?,`emp_teacher`= ?,`emp_birth_date`= ?,`emp_employ_date`= ?,`emp_leave_date`= ?,`emp_image`= ? WHERE emp_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			
			String values [] = employee.values();
			
			int i = 1;
			for(String val : values){
				pst.setString(i++, val);
			}
			
			pst.setInt(i++, employee.educLevel);
			pst.setInt(i++, employee.empType);
			
			pst.setBoolean(i++, employee.isTeacher);
			
			Date birth, employ, leave;
			
			birth = employee.birthDate != null ? new Date(employee.birthDate.getTime()) : null;
			employ = employee.employDate != null ? new Date(employee.employDate.getTime()) : null;
			leave = employee.leaveDate != null ? new Date(employee.leaveDate.getTime()) : null;
			
			pst.setDate(i++, birth);
			pst.setDate(i++, employ);
			pst.setDate(i++, leave);
			
			pst.setBytes(i++, employee.image);
			
			pst.setInt(i++, id);
			
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public Object [][] teacherTimetable(int id, int shift)
	{
		Object rows [][] = new Object[6][7];
		
		String week [] = {"saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
		int i = 0;
		for(String day : week){
			rows[i++][0] = Dic.w(day);
		}
		String sql = "SELECT course_id, course_grade, course_name, sch_day, sch_time, sub_id, sub_name"
				+ " FROM course_schedule, course_teacher, course, subjects "
				+ " WHERE sch_course_id = ct_course_id AND sch_sub_id = ct_sub_id "
				+ " AND sch_course_id = course_id AND sch_sub_id = sub_id AND ct_emp_id = %d AND course_shift = %d";
		sql = String.format(sql, id, shift);
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				String time = String.format("%d - %s (%s)", 
						results.getInt("course_grade"), 
						results.getString("course_name"), 
						results.getString("sub_name"));
				
				int d = results.getInt("sch_day") - 1;
				int t = results.getInt("sch_time");
				rows[d][t] = time;
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}

	public Vector<Object[]> teacherAssignedSubjs(int id) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT ct_id, course_id, course_grade, course_name, sub_id, sub_name, ct_teach_hours"
				+ " FROM course_teacher, course, subjects WHERE 1 AND ct_course_id = course_id "
				+ " AND ct_sub_id = sub_id AND ct_emp_id = %d AND course_year = %d";
		
		sql = String.format(sql, id, EDUC_YEAR);
		try
		{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				String course = String.format("%d - %s", results.getInt("course_grade"), results.getString("course_name"));
				Option option = new Option(results.getInt("ct_id"), course);
				Object row [] = {
						option,
						results.getString("sub_name"),
						results.getInt("ct_teach_hours"),
				};
				rows.add(row);
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}
	public boolean deleteAssignedSubj(int id)
	{
		String sql = "DELETE FROM course_teacher WHERE ct_id = " + id;
		try{
			return statement.executeUpdate(sql) > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	private DateFormat df = new SimpleDateFormat("yyyy/MM/dd", new ULocale("@calendar=persian"));
	
	public Vector<Object[]> empsAdvancedList(int from, int to)
	{
		Vector<Object[]> list = new Vector<>();
		String sql = "SELECT * FROM employee WHERE emp_id BETWEEN %d AND %d ORDER BY emp_id";
		sql = String.format(sql, from, to);
		try{
			results = statement.executeQuery(sql);
			String[] levels = {"illiterate", "essential_literacy", "12th_grade", "14th_grade", "bachelor", "masters", "phd"};
			while(results.next())
			{
				list.add(new Object[]{
						results.getString("emp_name"),
						results.getString("emp_lname"),
						results.getString("emp_fname"),
						results.getString("emp_gfname"),
						results.getString("emp_prev_job"),
						results.getString("emp_main_address"),
						results.getString("emp_curr_address"),
						results.getString("emp_idcard"),
						Dic.w(levels[results.getInt("emp_educ_level")]),
						results.getString("emp_educ_field"),
						results.getString("emp_gradu_year"),
						results.getString("emp_service_dur"),
						results.getString("emp_ngo_expr"),
						results.getString("emp_crimes"),
						results.getString("emp_punishments"),
						df.format(results.getDate("emp_employ_date")),
						results.getString("emp_phone"),
						results.getBytes("emp_image"),
				});
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return list;
	}
}




















