package com.kabulbits.shoqa.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;

public class ChartData extends Data {

	public int studentsByGender(String sex)
	{
		String sql = "select count(st_id) as sts from students where st_state = 'a' and st_gender = '%s'";
		try {
			results = statement.executeQuery(String.format(sql, sex));
			if(results.next()){
				return results.getInt("sts");
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
	
	public int [] studentsByGrade()
	{
		int [] count = new int [12];
		for(int i=0; i<count.length; i++){
			count[i] = 0;
		}
		String sql = "SELECT COUNT(st_id) AS sts , st_grade FROM students WHERE st_state = 'a' GROUP BY st_grade";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				int index = results.getInt("st_grade") - 1;
				count[index] = results.getInt("sts");
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return count;
	}
	
	public int[][] studentsByYear()
	{
		int [][] count = new int[5][4];
		int y = EDUC_YEAR - 4;
		for(int i=0; i<5; i++){
			count[i][0] = y++;
			count[i][1] = 0;
			count[i][2] = 0;
			count[i][3] = 0;
		}
		String sql1 = "SELECT COUNT(DISTINCT enrol_st_id) AS sts, course_year FROM enrolment , course "
				+ " WHERE enrol_course_id = course_id AND course_year BETWEEN %d AND %d GROUP BY course_year";
		sql1 = String.format(sql1, EDUC_YEAR - 4, EDUC_YEAR);
		
		String sql2 = "SELECT COUNT(st_id) AS sts, st_reg_year FROM students WHERE st_reg_year BETWEEN %d AND %d GROUP BY st_reg_year";
		sql2 = String.format(sql2, EDUC_YEAR - 4, EDUC_YEAR);
		
		String sql3 = "SELECT COUNT(st_id) AS sts, st_grad_year FROM students WHERE st_grad_year BETWEEN %d AND %d GROUP BY st_grad_year";
		sql3 = String.format(sql3, EDUC_YEAR - 4, EDUC_YEAR);
		try {
			results = statement.executeQuery(sql1);
			while(results.next()){
				int year = results.getInt("course_year");
				for(int j=0; j<5; j++){
					if(count[j][0] == year){
						count[j][1] = results.getInt("sts");
					}
				}
			}
			results = statement.executeQuery(sql2);
			while(results.next()){
				int year = results.getInt("st_reg_year");
				for(int j=0; j<5; j++){
					if(count[j][0] == year){
						count[j][2] = results.getInt("sts");
					}
				}
			}
			results = statement.executeQuery(sql3);
			while(results.next()){
				int year = results.getInt("st_grad_year");
				for(int j=0; j<5; j++){
					if(count[j][0] == year){
						count[j][3] = results.getInt("sts");
					}
				}
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return count;
	}
	
	public Map<Integer, Integer> employeesByType()
	{
		Map<Integer, Integer> info = new HashMap<>();
		info.put(1, 0);
		info.put(2, 0);
		info.put(3, 0);
		String sql = "SELECT COUNT(emp_id) AS emps , emp_type FROM employee GROUP BY emp_type";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				int type = results.getInt("emp_type");
				int count = results.getInt("emps");
				info.replace(type, count);
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return info;
	}
	
	public Map<Integer, Integer> employeesByYear()
	{
		Map<Integer, Integer> info = new HashMap<>();
		String sql = "SELECT COUNT(emp_id) AS emps, pyear(emp_employ_date) AS employ_year FROM employee GROUP BY employ_year ORDER BY employ_year";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				int year = results.getInt("employ_year");
				int count = results.getInt("emps");
				info.put(year, count);
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return info;
	}
	private int EVENTUAL = 1;
	private int FAILED = 2;
	
	// function only for final results
	public int failOrEventualCount(int gradeDown, int gradeUp, int failOrEventual)
	{
		String sql = "SELECT COUNT(subs) AS sts FROM("
				+ "SELECT COUNT(mark_id) AS subs FROM marks WHERE mark_year = %d AND mark_total < %d AND mark_grade BETWEEN %d AND %d "
				+ " GROUP BY mark_st_id , mark_grade) AS fails WHERE 1 ";
		sql = String.format(sql, EDUC_YEAR, PASS_GRADE_FINAL, gradeDown, gradeUp);
		if (failOrEventual == EVENTUAL){
			sql = sql.concat(" AND subs < " + FAIL_SUBJ_COUNT);
		}
		else if(failOrEventual == FAILED){
			sql = sql.concat(" AND subs >= " + FAIL_SUBJ_COUNT);
		}
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("sts");
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
	
	public int midtermFailCount(int gradeDown, int gradeUp)
	{
		String sql = "SELECT COUNT(DISTINCT mark_st_id) AS sts FROM marks WHERE mark_year = %d AND mark_half < %d AND mark_grade BETWEEN %d AND %d";
		sql = String.format(sql, EDUC_YEAR, PASS_GRADE_MID, gradeDown, gradeUp);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("sts");
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
	
	public int excusedCount(int season, int gradeDown, int gradeUp)
	{
		String col = season == 1 ? "mark_excused_midterm" : "mark_excused_final";
		String sql = "SELECT COUNT(DISTINCT mark_st_id) AS sts FROM marks WHERE mark_year = %d AND %s = 1 AND mark_grade BETWEEN %d AND %d";
		sql = String.format(sql, EDUC_YEAR, col, gradeDown, gradeUp);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("sts");
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
	
	public int countByAvg(int season, int markDown, int markUp)
	{
		String sql = "SELECT COUNT(aver) AS num FROM ("
				+ "SELECT AVG(%s) AS aver FROM marks WHERE mark_year = %d GROUP BY mark_st_id, mark_grade"
				+ ") AS avers WHERE aver BETWEEN %d AND %d";
		String col = season == 1 ? "mark_half" : "mark_total";
		sql = String.format(sql, col, EDUC_YEAR, markDown, markUp);
		try{
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
	
	public int passedCount(int season, int gradeDown, int gradeUp)
	{
		String sql = "SELECT COUNT(*) AS num FROM ("
				+ "SELECT COUNT(mark_id) AS marks , mark_grade FROM marks WHERE mark_year = %d AND %s >= %d AND mark_grade BETWEEN %d AND %d "
				+ " GROUP BY mark_st_id , mark_grade ) AS passed JOIN ("
				+ "SELECT COUNT(sg_id) AS subs , sg_grade FROM subject_grade WHERE sg_grade BETWEEN %d AND %d GROUP BY sg_grade) AS sub_count "
				+ " ON passed.mark_grade = sub_count.sg_grade AND passed.marks = sub_count.subs";
		
		String col = season == 1 ? "mark_half" : "mark_total";
		int passGrade = season == 1 ? PASS_GRADE_MID : PASS_GRADE_FINAL;
		sql = String.format(sql, EDUC_YEAR, col, passGrade, gradeDown, gradeUp, gradeDown, gradeUp);
		try{
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
}





















