package com.kabulbits.shoqa.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class CourseData extends Data {
	
	public void searchCourse(int y, int g, DefaultTableModel model) 
	{
		String sql = "SELECT * FROM course WHERE course_year = %d AND course_grade = %d";
		try {
			results = statement.executeQuery(String.format(sql, y, g));
			while (results.next()) {
				Object row [] = {
						results.getInt("course_id"),
						results.getInt("course_year"),
						results.getInt("course_grade"),
						results.getString("course_name"),
						results.getInt("course_shift"),
				};
				model.addRow(row);
			}
		}
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public Course lastCourse(){
		String q = "SELECT MAX(course_id) FROM course";
		try {
			results = statement.executeQuery(q);
			if (results.next()) {
				return findCourse(results.getInt(1));
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

	public boolean addCourse(String y, String g, String n, String s) {
		String q = "INSERT INTO course (course_year, course_grade, course_name, course_shift) VALUES (%s, %s, '%s', %s)";
		try {
			if (statement.executeUpdate(String.format(q, y, g, n, s)) == 1) {
				return true;
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean editCourse(String id, String name)
	{
		String q = "UPDATE course SET course_name = '%s' WHERE course_id = %s";
		try{
			return statement.executeUpdate(String.format(q, name, id)) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean deleteCourse(String id) {
		String q = "DELETE FROM course WHERE course_id = " + id;
		try {
			if (statement.executeUpdate(q) == 1) {
				return true;
			}
		}
		catch (SQLException e){
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

	public boolean setForbidden(int sid, int cid, int forbid) 
	{
		String sql = "UPDATE enrolment SET enrol_forbidden = %d WHERE enrol_st_id = %d AND enrol_course_id = %d";
		sql = String.format(sql, forbid, sid, cid);
		try {
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean detachStudent(int sid, int cid)
	{
		String sql = "DELETE FROM enrolment WHERE enrol_st_id = %d AND enrol_course_id = %d";	
		sql = String.format(sql, sid, cid);
		try{
			if (statement.executeUpdate(sql) == 1){
				return true;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	//=================================== time table functions ====================================
	
	public Vector<Option> teacherOptions() 
	{
		Vector<Option> options = new Vector<>();
		options.add(null);
		String sql = "SELECT emp_id, emp_name, emp_lname FROM employee WHERE emp_teacher = 1";
		try {
			results = statement.executeQuery(sql);
			while (results.next()) 
			{
				int id = results.getInt("emp_id");
				String value = String.format("(%d) %s %s", id, results.getString("emp_name"), results.getString("emp_lname"));
				Option option = new Option(id, value);
				options.add(option);
			}
		}
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return options;
	}

	public Vector<Object []> courseTeachers(int id, int grade) 
	{
		Vector<Object []> rows = new Vector<>();
		
		String sql = "SELECT sub_id, sub_name, ct_teach_hours, emp_id, emp_name, emp_lname FROM subject_grade "
				+ " INNER JOIN subjects ON sg_sub_id = sub_id AND sg_grade = %d "
				+ " LEFT JOIN course_teacher ON ct_sub_id = sg_sub_id AND ct_course_id = %d "
				+ " LEFT JOIN employee ON ct_emp_id = emp_id ORDER BY sub_pos";
		sql = String.format(sql, grade, id);
		try{
			results = statement.executeQuery(sql);
			while(results.next()) 
			{
				Option option = null;
				Object empid = results.getObject("emp_id");
				
				if(empid != null){
					int eid = Integer.parseInt(empid.toString());
					String name = results.getString("emp_name");
					String lname = results.getString("emp_lname");
					String value = String.format("(%d) %s %s", eid, name, lname);
					option = new Option(eid, value);
				}
				Object row [] = {
						results.getInt("sub_id"),
						results.getString("sub_name"),
						results.getObject("ct_teach_hours"),
						option,
						empid,
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

	public boolean assignTeacher(int sub, Object emp, Object hours, int id) 
	{
		String sql = "REPLACE INTO course_teacher (ct_course_id, ct_sub_id, ct_emp_id, ct_teach_hours) VALUES (?, ?, ?, ?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, id);
			pst.setInt(2, sub);
			pst.setObject(3, emp);
			pst.setObject(4, hours);
			
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public Object[][] courseTimetable(int id) 
	{
		Object rows [][] = new Object[6][7];
		
		String week [] = {"saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
		int i = 0;
		for(String day : week){
			rows[i++][0] = Dic.w(day);
		}
		String sql = "SELECT sub_id, sub_name, sch_day, sch_time FROM course_schedule "
				+ " INNER JOIN subjects ON sch_sub_id = sub_id WHERE 1 AND sch_course_id = " + id;
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int d = results.getInt("sch_day") - 1;
				int t = results.getInt("sch_time");
				rows[d][t] = new Option(results.getInt("sub_id"), results.getString("sub_name"));
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
	
	public String[][][] courseTimetableForPrint(int id)
	{
		String[][][] info = new String[7][7][2];
		for(int i=0; i<7; i++){
			for(int j=0; j<7; j++){
				for(int k=0; k<2; k++){
					info[i][j][k] = "";
				}
			}
		}
		String sql = "SELECT sub_id, sub_name, CONCAT(emp_name, ' ', emp_lname) AS teacher, sch_day, sch_time "
				+ " FROM course_schedule, subjects, course_teacher, employee "
				+ " WHERE sch_sub_id = sub_id AND sub_id = ct_sub_id AND ct_course_id = sch_course_id AND ct_emp_id = emp_id AND sch_course_id = " + id;
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				int d = results.getInt("sch_day");
				int t = results.getInt("sch_time");
				info[d][t][0] = results.getString("sub_name");
				info[d][t][1] = results.getString("teacher");
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return info;
	}

	public Vector<Option> subjectList(int grade, boolean nullOption) 
	{
		Vector<Option> options = new Vector<Option>();
		if(nullOption){
			options.add(null);
		}
		String sql = "SELECT sub_id,sub_name FROM subjects ";
		if(grade != 0){
			sql = sql.concat(" JOIN subject_grade ON sg_sub_id = sub_id AND sg_grade = " + grade);
		}
		sql = sql.concat(" WHERE 1 ORDER BY sub_pos");
		sql = String.format(sql, grade);
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Option option = new Option(results.getInt("sub_id"), results.getString("sub_name"));
				options.add(option);
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

	public boolean saveTimetable(int id, int day, int time, int sub) 
	{
		String sql = "REPLACE INTO course_schedule (`sch_course_id`,`sch_day`,`sch_time`,`sch_sub_id`) VALUES (?, ?, ?, ?);";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, id);
			pst.setInt(2, day);
			pst.setInt(3, time);
			pst.setInt(4, sub);
			
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	public boolean deleteTimetable(int id, int day, int time)
	{
		String sql = "DELETE FROM course_schedule WHERE sch_course_id = %d AND sch_day = %d AND sch_time = %d";
		sql = String.format(sql, id, day, time);
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
	
	public boolean isTeacherFree(int cid, int day, int time, int sub, int sh)
	{
		String sql = "SELECT sch_id, course_id, course_grade, course_name FROM course , course_schedule , course_teacher "
				+ " WHERE course_id = sch_course_id AND course_id = ct_course_id AND sch_sub_id = ct_sub_id "
				+ " AND course_id != %d AND course_shift = %d AND sch_day = %d AND sch_time = %d AND ct_emp_id = "
				+ "(SELECT ct_emp_id FROM course_teacher WHERE ct_course_id = %d AND ct_sub_id = %d)";
		sql = String.format(sql, cid, sh, day, time, cid, sub);
		try{
			results = statement.executeQuery(sql);
			if(results.next())
			{
				int sch_id = results.getInt("sch_id");
				int grade = results.getInt("course_grade");
				String name = results.getString("course_name");
				String msg = "<html>%s<br><b>%s %d (%s)</b><br>%s</html>";
				msg = String.format(msg, Dic.w("intersect_error"), Dic.w("course"), grade, name, Dic.w("schedule_delete_conf"));
				
				if(Diags.showErrConf(msg, Diags.YN) == 0){
					return statement.executeUpdate("DELETE FROM course_schedule WHERE sch_id = " + sch_id) > 0;
				}
				return false;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}
	private String[] days = {"", "saturday", "sunday", "monday", "tuesday", "wednesday", "thursday"};
	private String[] hours = {"", "first", "second", "third", "fourth", "fifth", "sixth"};
	
	public boolean isTeacherFreeR(int cid, int sub, int sh, int emp)
	{
		String sql1 = "SELECT sch_day, sch_time FROM course_schedule WHERE sch_course_id = %d AND sch_sub_id = %d";
		String sql2 = "SELECT course_id, course_grade, course_name FROM course, course_teacher, course_schedule WHERE course_id = ct_course_id "
				+ " AND course_id = sch_course_id AND ct_sub_id = sch_sub_id AND course_shift = %d AND ct_emp_id = %d AND sch_day = %d AND sch_time = %d AND course_id != %d";
		sql1 = String.format(sql1, cid, sub);
		try{
			results = statement.executeQuery(sql1);
			while(results.next())
			{
				int day = results.getInt("sch_day");
				int time = results.getInt("sch_time");
				sql2 = String.format(sql2, sh, emp, day, time, cid);
				results = statement.executeQuery(sql2);
				if(results.next())
				{
					int grade = results.getInt("course_grade");
					String name = results.getString("course_name");
					String msg = "<html>%s<br>%s <b>(%s)</b> %s <b>(%s)</b> %s <b>(%d %s)</b></html>";
					msg = String.format(msg, Dic.w("intersect_error"), Dic.w("day"), Dic.w(days[day]), Dic.w("hour"), Dic.w(hours[time]), Dic.w("course"), grade, name);
					Diags.showErr(msg);
					return false;
				}
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}

	public Vector<Object []> dayTimetable(int day, int grade, int shift)
	{
		String sql = "SELECT course_id, course_name, course_grade, sub_name, emp_id, emp_name, emp_lname, sch_time "
				+ " FROM course LEFT JOIN course_schedule ON course_id = sch_course_id AND sch_day = %d "
				+ " LEFT JOIN subjects ON sch_sub_id = sub_id "
				+ " LEFT JOIN course_teacher ON course_id = ct_course_id AND sch_sub_id = ct_sub_id "
				+ " LEFT JOIN employee ON ct_emp_id = emp_id WHERE course_shift = %d AND course_year = %d ";
		
		sql = String.format(sql, day, shift, EDUC_YEAR);
		if(grade != 0){
			sql = sql.concat(" AND course_grade = " + grade);
		}
		sql = sql.concat(" ORDER BY course_id, sch_time");
		Vector<Object []> rows = new Vector<>();
		Object row []= null;
		int course_id = 0;
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				int id = results.getInt("course_id");
				if(id != course_id){
					course_id = id;
					row = new Object[8];
					if(course_id != 0) {
						rows.add(row);
					}
					row[0] = new Course(id, results.getInt("course_grade"), results.getString("course_name"));
				}
				int t = results.getInt("sch_time");
				if(t != 0) {
					row[t] = String.format("%s (%s %s)", results.getString("sub_name"), results.getString("emp_name"), results.getString("emp_lname")).replace("null", "---");
				}
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
	
	public Vector<Object[]> coursesByShift(int shift)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT course_id, course_grade, course_name FROM course WHERE course_year = %d AND course_shift = %d ORDER BY course_grade";
		sql = String.format(sql, EDUC_YEAR, shift);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						false,
						results.getInt("course_id"),
						results.getInt("course_grade"),
						results.getString("course_name"),
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
	
	public boolean clearShiftSchedule(int shift)
	{
		String sql = "DELETE FROM course_schedule WHERE sch_course_id IN (SELECT course_id FROM course WHERE course_year = %d AND course_shift = %d)";
		sql = String.format(sql, EDUC_YEAR, shift);
		try{
			statement.executeUpdate(sql);
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}
	
	public Vector<Object[]> teachingHours(int shift)
	{
		Vector<Object[]> rows = new Vector<Object[]>();
		String sql = "SELECT emp_id, emp_name, emp_lname, SUM(ct_teach_hours) AS hours FROM course_teacher JOIN employee ON ct_emp_id = emp_id "
				+ " JOIN course ON ct_course_id = course_id AND course_year = %d AND course_shift = %d GROUP BY ct_emp_id";
		sql = String.format(sql, EDUC_YEAR, shift);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("emp_id"),
						results.getString("emp_name"),
						results.getString("emp_lname"),
						results.getInt("hours"),
						false,
				};
				rows.add(row);
			}
		}catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}
	
	public Vector<Map<String, Integer>> courseSubjHours(int id)
	{
		Vector<Map<String, Integer>> items = new Vector<>();
		String sql = "SELECT * FROM course_teacher WHERE ct_course_id = " + id;
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Map<String, Integer> item = new HashMap<>();
				item.put("subject", results.getInt("ct_sub_id"));
				item.put("hours", results.getInt("ct_teach_hours"));
				item.put("teacher", results.getInt("ct_emp_id"));
				items.add(item);
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return items;
	}
}




















