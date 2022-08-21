package com.kabulbits.shoqa.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class MarkData extends Data {
	
	public Vector<Object[]> eventualStudList(int cid, int year, int grade, boolean all)
	{
		Vector<Object[]> rows = new Vector<Object[]>();
		String sql = "SELECT DISTINCT st_id, st_code, st_name, st_fname, st_gfname FROM students JOIN marks ON st_id = mark_st_id "
				+ " WHERE mark_grade = %d AND mark_year = %d AND mark_second IS NOT NULL ";
		sql = String.format(sql, grade, year);
		if(!all){
			sql = sql.concat(String.format(" AND st_id IN (SELECT enrol_st_id FROM enrolment WHERE enrol_course_id = %d )", cid));
		}
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				rows.add(new Object[]{
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_gfname"),
						results.getInt("st_code"),
						results.getInt("st_id"),
				});
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
	
	public Vector<Object[]> studSecondMarks(int id, int year, int grade)
	{
		Vector<Object[]> rows = new Vector<Object[]>();
		String sql = "SELECT sub_name, mark_total, mark_second FROM marks JOIN subjects ON mark_sub_id = sub_id "
				+ " WHERE mark_grade = %d AND mark_year = %d AND mark_st_id = %d AND mark_second IS NOT NULL";
		sql = String.format(sql, grade, year, id);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				rows.add(new Object[]{
						results.getString("sub_name"),
						results.getFloat("mark_total"),
						results.getFloat("mark_second"),
				});
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}
	
	public Vector<Object> studGradeMarks(int sid, int grade, int year)
	{
		Vector<Object> marks = new Vector<>();
		String sql = "SELECT sub_id, mark_total, mark_second FROM subjects LEFT JOIN marks "
				+ " ON sub_id = mark_sub_id AND mark_year = %d AND mark_grade = %d AND mark_st_id = %d ORDER BY sub_pos";
		sql = String.format(sql, year, grade, sid);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				if(results.getObject("mark_second") != null){
					marks.add(results.getObject("mark_second"));
				}
				else{
					marks.add(results.getObject("mark_total"));
				}
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return marks;
	}
	
	public int gradePassingYear(int sid, int grade)
	{
		String sqlSubs = "SELECT COUNT(DISTINCT sg_sub_id) AS subs FROM subject_grade WHERE sg_grade = " + grade;
		String sqlPassed = "SELECT COUNT(DISTINCT mark_sub_id) AS passed, mark_year FROM marks WHERE 1 "
				+ " AND mark_grade = %d AND mark_st_id = %d AND ( mark_total >= %d OR mark_second >= %d) GROUP BY mark_year";
		sqlPassed = String.format(sqlPassed, grade, sid, PASS_GRADE_FINAL, PASS_GRADE_FINAL);
		int subs = 0;
		try{
			results = statement.executeQuery(sqlSubs);
			if(results.next()){
				subs = results.getInt("subs");
			}
			results = statement.executeQuery(sqlPassed);
			while(results.next()){
				if (results.getInt("passed") >= subs){
					return results.getInt("mark_year");
				}
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}
	
	public Vector<Object[]> fullMarks(int sid, int year, int grade, int type, boolean allSubs)
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String str = "SELECT sub_id, sub_name , marks.* FROM subjects ";
		StringBuffer buf = new StringBuffer(str);
		
		if(!allSubs){
			buf.append(" JOIN subject_grade ON sg_sub_id = sub_id AND sg_grade = " + grade);
		}
		String append = " LEFT JOIN marks ON sub_id = mark_sub_id AND mark_st_id = %d AND mark_year = %d AND mark_grade = %d AND mark_type = %d ORDER BY sub_pos";
		buf.append(String.format(append, sid, year, grade, type));

		String sql = buf.toString();
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("sub_id"),
						results.getString("sub_name"),
						results.getObject("mark_half"),
						results.getObject("mark_final"),
						results.getObject("mark_total"),
						results.getObject("mark_second"),
						results.getBoolean("mark_excused_midterm"),
						results.getBoolean("mark_excused_final"),
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
	
	public Vector<Object []> seasonMarks(int sid, int year, int grade, int season, boolean allSubs)
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String str = "SELECT sub_id, sub_name, mark_details.* FROM subjects ";
		StringBuffer buf = new StringBuffer(str);
		
		if(!allSubs){
			buf.append(" JOIN subject_grade ON sub_id = sg_sub_id AND sg_grade = " + grade);
		}
		String append = " LEFT JOIN mark_details ON sub_id = mk_sub_id AND mk_st_id = %d AND mk_year = %d "
				+ " AND mk_grade = %d AND mk_season = %d WHERE 1 ORDER BY sub_pos";
		buf.append(String.format(append, sid, year, grade, season));
		
		String sql = buf.toString();
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int size = grade > 1 ? 8 : season == 1 ? 7 : 9;
				Object row [] = new Object [size];
				int i = 0;
				
				row[i++] = results.getInt("sub_id");
				row[i++] = results.getString("sub_name");
				
				String cols [] = {"mk_section_1", "mk_section_2", "mk_section_3", "mk_section_4", "mk_section_5", "mk_section_6"};
				
				boolean isNull = true;
				float total = 0;
				for(String col : cols)
				{
					if(i < size-1){
						Object value = results.getObject(col);
						row[i++] = value;
						if(value != null){
							isNull = false;
							total += results.getFloat(col);
						}
					}
				}
				row[i++] = isNull ? null : total;
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
	
	public int markYear(int g, int s, int cg)
	{
		String sql1 = "SELECT mark_year AS year FROM marks WHERE mark_grade = %d AND mark_st_id = %d "
				+ "ORDER BY mark_year DESC LIMIT 1";
		String sql2 = "SELECT course_year AS year FROM enrolment , course WHERE course_id = enrol_course_id "
				+ "AND course_grade = %d AND enrol_st_id = %d ORDER BY course_year DESC LIMIT 1";
		sql1 = String.format(sql1, g, s);
		sql2 = String.format(sql2, g, s);
		try{
			results = statement.executeQuery(sql1);
			if(results.next())
				return results.getInt("year");
			results = statement.executeQuery(sql2);
			if(results.next())
				return results.getInt("year");
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return EDUC_YEAR - (cg - g);
	}
	
	public int studentPosition(int id, int year, int grade, int season, float avg)
	{
		int pass = season == 1 ? PASS_GRADE_MID : PASS_GRADE_FINAL;
		String markCol = season == 1 ? "mark_half" : "mark_total";
		String excuseCol = season == 1 ? "mark_excused_midterm" : "mark_excused_final";
		
		String sql = "SELECT aver , COUNT(DISTINCT mark_st_id) + 1 AS pos FROM "
				+ "(SELECT AVG(%s) AS aver , mark_st_id "
				// filter year, grade and subject_grade 
				+ "FROM marks , subject_grade , enrolment WHERE mark_year = %d AND mark_grade = %d "
				+ "AND mark_sub_id = sg_sub_id AND sg_grade = mark_grade "
				// filter failed, eventual and excused students
				+ " AND mark_st_id NOT IN ("
				+ "SELECT DISTINCT mark_st_id FROM marks WHERE mark_year = %d AND mark_grade = %d "
				+ "AND (%s < %d OR %s = 1))"
				// filter course of student
				+ "AND mark_total IS NOT NULL AND mark_st_id = enrol_st_id AND enrol_course_id = "
				+ "(SELECT course_id FROM course , enrolment WHERE course_year = %d AND course_grade = %d "
				+ "AND course_id = enrol_course_id AND enrol_st_id = %d LIMIT 1) GROUP BY mark_st_id "
				// filter completed students
				+ "HAVING COUNT(DISTINCT mark_sub_id) >= "
				+ "(SELECT COUNT(*) FROM subject_grade WHERE sg_grade = %d)) "
				// filter count higher averages
				+ "AS avgs WHERE aver > %.2f";
		try{
			sql = String.format(sql, markCol, year, grade, year, grade, markCol, pass, excuseCol, year, grade, id, grade, avg);
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("pos");
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}
	
	public Vector<Student> topStudents(int year, int grade, boolean byGrade)
	{
		String str = "SELECT AVG(mark_total) AS aver, st_id, st_name , st_image, st_gender FROM marks, students  "
				+ " WHERE st_id = mark_st_id AND mark_total IS NOT NULL ";
		StringBuffer buf = new StringBuffer(str);
		if(byGrade){
			buf.append(String.format(" AND mark_grade = %d ", grade));
		}else{
			String append = " AND mark_grade BETWEEN %d AND %d ";
			switch(grade){
			case 1:
				buf.append(String.format(append, 1, 3));
				break;
			case 2:
				buf.append(String.format(append, 4, 6));
				break;
			case 3:
				buf.append(String.format(append, 7, 12));
				break;
			}
		}
		String end = " AND mark_year = %d GROUP BY mark_st_id, mark_grade ORDER BY aver DESC LIMIT 10 ";
		buf.append(String.format(end, year));
		String sql = buf.toString();
		
		Vector<Student> rows = new Vector<>();
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				InputStream is = results.getBinaryStream("st_image");
				BufferedImage bufimg;
				if(is != null){
					bufimg = ImageIO.read(is);
				}else{
					String sex = results.getString("st_gender");
					bufimg = ImageIO.read(new File( sex.equals("m") ? "images/male.jpg" : "images/female.jpg"));
				}
				Student student = new Student(
						results.getInt("st_id"), 
						results.getString("st_name"), 
						results.getFloat("aver"), bufimg);
				rows.add(student);
			}
			return rows;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		catch (IOException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public boolean saveAttendance(int id, int year, int month, Object[] values) 
	{
		String sql = "REPLACE INTO student_attendance (`attend_st_id`,`attend_year`,`attend_month`,`attend_present`,`attend_absent`,`attend_sick`,`attend_holiday`) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, year);
			pst.setInt(i++, month);
			
			for(Object val : values){
				pst.setObject(i++, val);
			}
			
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	private int midMonth = 4;
	public boolean setMidtermAttend(int id, int year)
	{
		String sql = "REPLACE INTO student_attendance (`attend_st_id`,`attend_year`,`attend_month`,`attend_present`,`attend_absent`,`attend_sick`,`attend_holiday`)"
				+ " SELECT `attend_st_id`,`attend_year`, %d,`attend_present`,`attend_absent`,`attend_sick`,`attend_holiday` FROM student_attendance "
				+ " WHERE attend_st_id = %d AND attend_year = %d AND attend_month = %d";
		sql = String.format(sql, 0, id, year, midMonth);
		try{
			return statement.executeUpdate(sql) > 0;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public Vector<Object[]> findAttendance(int id, int year) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT * FROM student_attendance WHERE attend_st_id = %d AND attend_year = %d ORDER BY attend_month ASC";
		sql = String.format(sql, id, year);
		
		String months[] = {
				"hamal", "sawr", "jawza", "saratan", "asad", "sonbola", "mizan", "aqrab", "qaws", "jadi", "dalw", "hoot",
		};
		for(String month : months){
			rows.add(new Object [] {Dic.w(month), null, null, null, null});
		}
		try
		{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int m = results.getInt("attend_month") - 1;
				if (m < 0) continue;
				Object row [] = {
						Dic.w(months[m]),
						results.getObject("attend_present"),
						results.getObject("attend_absent"),
						results.getObject("attend_sick"),
						results.getObject("attend_holiday"),
				};
				rows.set(m, row);
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}
	
	public int[][] studentAttendSum(int id, int year)
	{
		int[][] values = new int[3][4];
		for(int i=0; i<3; i++){
			for(int j=0; j<4; j++){
				values[i][j] = 0;
			}
		}
		String str = "SELECT SUM(attend_present) AS present, SUM(attend_absent) AS absent, SUM(attend_sick) AS sick, "
				+ " SUM(attend_holiday) AS holiday FROM student_attendance WHERE attend_st_id = %d AND attend_year = %d ";
		str = String.format(str, id, year);
		try{
			String sql = str.concat(" AND attend_month != 0");
			results = statement.executeQuery(sql);
			if(results.next()){
				values[2][0] = results.getInt("present");
				values[2][1] = results.getInt("absent");
				values[2][2] = results.getInt("sick");
				values[2][3] = results.getInt("holiday");
			}
			sql = str.concat(" AND attend_month BETWEEN 0 AND 3");
			results = statement.executeQuery(sql);
			if(results.next()){
				values[0][0] = results.getInt("present");
				values[0][1] = results.getInt("absent");
				values[0][2] = results.getInt("sick");
				values[0][3] = results.getInt("holiday");
			}
			values [1][0] = values[2][0] - values[0][0];
			values [1][1] = values[2][1] - values[0][1];
			values [1][2] = values[2][2] - values[0][2];
			values [1][3] = values[2][3] - values[0][3];
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return values;
	}
	
	// FILTERS
	public int ALL = 0;
	public int PASSED = 1;
	public int FAILED = 2;
	public int EVENTUAL = 3;
	public int FORBIDDEN = 4;
	public int EXCUSED = 5;
	
	private String filter(int grade, int year, int season, int sub, int filter)
	{
		String markCol = season == 1 ? "mark_half" : "mark_total";
		int passGrade = season == 1 ? PASS_GRADE_MID : PASS_GRADE_FINAL;
		
		if(filter == FORBIDDEN){
			return " AND enrol_forbidden = 1 ";
		}
		if(filter == EXCUSED){
			if(season == 0){
				return " AND (`mark_excused_midterm` = 1 OR `mark_excused_final` = 1)";
			}else{
				String excuCol = season == 1 ? "mark_excused_midterm" : "mark_excused_final";
				String sqlExcused = " AND st_id IN (SELECT mark_st_id FROM marks WHERE mark_year = %d AND mark_grade = %d AND mark_sub_id = %d AND %s = 1)";
				sqlExcused = String.format(sqlExcused, year, grade, sub, excuCol);
				return sqlExcused;
			}
		}
		if(filter == PASSED)
		{
			String sqlPassed = " AND st_id in (SELECT mark_st_id FROM marks "
					+ " WHERE mark_year = %d AND mark_grade = %d AND %s >= %d "
					+ " GROUP BY mark_st_id HAVING COUNT(DISTINCT mark_sub_id) >= "
					+ " (SELECT COUNT(DISTINCT sg_sub_id) FROM subject_grade "
					+ " WHERE 1 AND sg_grade = %d))";
			
			sqlPassed = String.format(sqlPassed, year, grade, markCol, passGrade, grade);
			return sqlPassed;
		}
		if(season != 1 && filter == EVENTUAL)
		{
			String sqlEventual = "AND st_id IN (SELECT mark_st_id FROM marks "
					+ " WHERE mark_grade = %d AND mark_year = %d AND mark_total < %d "
					+ " GROUP BY mark_st_id HAVING COUNT(DISTINCT mark_sub_id) < %d) ";
			sqlEventual = String.format(sqlEventual, grade, year, passGrade, FAIL_SUBJ_COUNT);
			return sqlEventual;
		}
		if(season == 1 && filter == FAILED)
		{
			String sqlFailed = " AND st_id IN (SELECT mark_st_id FROM marks WHERE mark_year = %d AND mark_grade = %d AND %s < %d)";
			
			sqlFailed = String.format(sqlFailed, year, grade, markCol, passGrade);
			return sqlFailed;
		}
		if(season != 1 && filter == FAILED)
		{
			String sqlFailed = "AND (st_id IN (SELECT mark_st_id FROM marks "
					+ " WHERE mark_grade = %d AND mark_year = %d AND mark_total < %d "
					+ " GROUP BY mark_st_id HAVING COUNT(DISTINCT mark_sub_id) >= %d) "
					+ " OR st_id IN (SELECT mark_st_id FROM marks "
					+ " WHERE mark_grade = %d AND mark_year = %d AND mark_second < %d ))";
			
			sqlFailed = String.format(sqlFailed, grade, year, passGrade, FAIL_SUBJ_COUNT, grade, year, passGrade);
			return sqlFailed;
		}
		return "";
	}
	
	public Vector<Object []> seasonMarks(Course course, int sub, int season, String word, int status, int filter)
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		// season 1 does not have eventual
		if(season == 1 && filter == EVENTUAL) return rows;
		
		String sql = "SELECT st_id, st_code, st_name, st_fname , mark_details.* FROM enrolment "
				+ " JOIN students ON enrol_st_id = st_id LEFT JOIN mark_details ON enrol_st_id = mk_st_id AND "
				+ " mk_sub_id = %d AND mk_grade = %d AND mk_year = %d AND mk_season = %d WHERE 1 AND enrol_course_id = %d ";
		
		sql = String.format(sql, sub, course.grade, course.year, season, course.id);
		
		if(word.length() > 0) {
			String append = " AND ( st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%' ) ";
			sql = sql.concat(String.format(append, word, word, word));
		}
		if(status != 2) {
			sql = sql.concat(String.format(" AND st_official = %d ", status));
		}
		if(filter != ALL) {
			String sqlFilter = filter(course.grade, course.year, season, sub, filter);
			sql = sql.concat(sqlFilter);
		}
		sql = sql.concat(" ORDER BY st_id ");

		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int size = course.grade > 1 ? 10 : season == 1 ? 9 : 11;
				Object row [] = new Object [size];
				int i = 0;
				
				row[i++] = results.getInt("st_id");
				row[i++] = results.getInt("st_code");
				row[i++] = results.getString("st_name");
				row[i++] = results.getString("st_fname");
				
				String cols [] = {"mk_section_1", "mk_section_2", "mk_section_3", "mk_section_4", "mk_section_5", "mk_section_6"};
				
				boolean isNull = true;
				float total = 0;
				for(String col : cols)
				{
					if(i < size-1){
						Object value = results.getObject(col);
						row[i++] = value;
						if(value != null){
							isNull = false;
							total += results.getFloat(col);
						}
					}
				}
				row[i++] = isNull ? null : total;
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
	
	public Vector<Object []> fullMarks(Course course, int sub, String word, int status, int filter)
	{
		Vector<Object []> rows = new Vector<Object[]>();
		String sql = "SELECT st_id, st_code, st_name, st_fname , marks.* FROM enrolment JOIN students ON enrol_st_id = st_id "
				+ " LEFT JOIN marks ON mark_st_id = enrol_st_id AND mark_sub_id = %d AND mark_grade = %d AND mark_year = %d "
				+ " WHERE 1 AND enrol_course_id = %d ";
		sql = String.format(sql, sub, course.grade, course.year, course.id);
		
		if(word.length() > 0) {
			sql = sql.concat(String.format(" AND ( st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%' ) ", word, word, word));
		}
		if(status != 2) {
			sql = sql.concat(String.format(" AND st_official = %d ", status));
		}
		if(filter != ALL) {
			String sqlFilter = filter(course.grade, course.year, 0, sub, filter);
			sql = sql.concat(sqlFilter);
		}
		sql = sql.concat(" ORDER BY st_id ");
		
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("st_id"),
						results.getInt("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getObject("mark_half"),
						results.getObject("mark_final"),
						results.getObject("mark_total"),
						results.getObject("mark_second"),
						results.getBoolean("mark_excused_midterm"),
						results.getBoolean("mark_excused_final"),
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
	
	public boolean deleteMark(int sid, int sub, int year, int grade, int season)
	{
		String sqlHalf = "DELETE FROM mark_details WHERE `mk_st_id` = %d AND `mk_sub_id` = %d AND `mk_grade` = %d AND `mk_year` = %d AND `mk_season` = %d";
		String sqlFull = "DELETE FROM marks WHERE `mark_st_id` = %d  AND `mark_sub_id` = %d AND `mark_grade` = %d AND `mark_year` = %d ";
		try
		{
			if(season != 0){
				sqlHalf = String.format(sqlHalf, sid, sub, grade, year, season);
				return statement.executeUpdate(sqlHalf) > 0;
			}else{
				sqlFull = String.format(sqlFull, sid, sub, grade, year);
				return statement.executeUpdate(sqlFull) > 0;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean saveSeasonMark(int sid, Object [] marks, int grade, int year, int season, int sub) 
	{
		String sql = "REPLACE INTO `mark_details` (`mk_st_id`,`mk_sub_id`,`mk_grade`,`mk_year`,`mk_season`,"
				+ " `mk_section_1`,`mk_section_2`,`mk_section_3`,`mk_section_4`,`mk_section_5`,`mk_section_6`)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		String sqlUpdate = "UPDATE marks SET `%s` = ? , `mark_total` = (`%s` + ?) "
				+ " WHERE `mark_st_id` = ? AND `mark_sub_id` = ? AND `mark_grade` = ? AND `mark_year` = ?";
		sqlUpdate = season == 1 ? 
				String.format(sqlUpdate, "mark_half", "mark_final") 
				: String.format(sqlUpdate, "mark_final", "mark_half");
				
		String sqlInsert = "REPLACE INTO marks (`mark_st_id`,`mark_sub_id`,`mark_grade`,`mark_year`,`%s`) VALUES (?, ?, ?, ?, ?)";
		
		sqlInsert = String.format(sqlInsert, season == 1 ? "mark_half" : "mark_final");
		try 
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, sid);
			pst.setInt(i++, sub);
			pst.setInt(i++, grade);
			pst.setInt(i++, year);
			pst.setInt(i++, season);
			
			for(int j=0; j<marks.length-1; j++){
				pst.setObject(i++, marks[j]);
			}
			
			Object total = marks[marks.length-1];
			
			PreparedStatement pstupdate = connection.prepareStatement(sqlUpdate);
			int j = 1;
			pstupdate.setObject(j++, total);
			pstupdate.setObject(j++, total);
			pstupdate.setInt(j++, sid);
			pstupdate.setInt(j++, sub);
			pstupdate.setInt(j++, grade);
			pstupdate.setInt(j++, year);
			
			connection.setAutoCommit(false);
			pst.executeUpdate();
			
			if(pstupdate.executeUpdate() < 1)
			{
				PreparedStatement pstinsert = connection.prepareStatement(sqlInsert);
				int k = 1;
				pstinsert.setInt(k++, sid);
				pstinsert.setInt(k++, sub);
				pstinsert.setInt(k++, grade);
				pstinsert.setInt(k++, year);
				pstinsert.setObject(k++, total);
				pstinsert.executeUpdate();
			}
			connection.commit();
			connection.setAutoCommit(true);
		} 
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			try{
				connection.rollback();
				connection.setAutoCommit(true);
			} 
			catch(SQLException e1){
				if(App.LOG){
					logger.error(e1.getMessage(), e1);
				}
			}
			return false;
		}
		return true;
	}

	public boolean saveFullMark(int id, Object[] marks, boolean excMid, boolean excFin, int type, int grade, int year, int sub) 
	{
		String sql = "REPLACE INTO marks (`mark_st_id`,`mark_sub_id`,`mark_grade`,`mark_year`,`mark_half`,`mark_final`,"
				+ "`mark_total`,`mark_second`,`mark_excused_midterm`,`mark_excused_final`,`mark_type`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, sub);
			pst.setInt(i++, grade);
			pst.setInt(i++, year);
			
			for(Object mark : marks){
				pst.setObject(i++, mark);
			}
			pst.setBoolean(i++, excMid);
			pst.setBoolean(i++, excFin);
			pst.setInt(i++, type);
			pst.executeUpdate();
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}

	public Vector<Object[]> courseAttendance(int id, int year, int month, String word) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String monthSql = "SELECT st_id, st_code, st_name, st_fname, attend_present AS present, "
				+ "attend_absent AS absent, attend_sick AS sick, attend_holiday AS holiday "
				+ " FROM enrolment INNER JOIN students ON enrol_st_id = st_id AND enrol_course_id = %d "
				+ " LEFT JOIN student_attendance ON enrol_st_id = attend_st_id AND attend_year = %d AND attend_month = %d WHERE 1 ";
		
		String totalSql = "SELECT st_id, st_code, st_name, st_fname, SUM(attend_present) AS present, "
				+ " SUM(attend_absent) AS absent, SUM(attend_sick) AS sick, SUM(attend_holiday) AS holiday "
				+ " FROM enrolment INNER JOIN students ON enrol_st_id = st_id AND enrol_course_id = %d "
				+ " LEFT JOIN student_attendance ON enrol_st_id = attend_st_id AND attend_year = %d WHERE 1 AND attend_month > 0";
		
		String sql = month != 0 ? String.format(monthSql, id, year, month) : String.format(totalSql, id, year);
		
		if(word.length() > 0) {
			String filterStr = " AND (st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%') ";
			sql = sql.concat(String.format(filterStr, word, word, word));
		}
		if(month == 0) {
			sql = sql.concat(" GROUP BY enrol_st_id");
		}
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("st_id"),
						results.getInt("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getObject("present"),
						results.getObject("absent"),
						results.getObject("sick"),
						results.getObject("holiday"),
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
	
	public boolean graduateStudent(int id)
	{
		String sql = "UPDATE students SET st_state = 'g' , st_grad_year = %d WHERE st_id = %d";
		String msg = String.format("کد متعلم: %d. متعلم واجد شرایط �?راغت نیست. ادامه می دهید؟", id);
		try{
			if(!isPassed(id, 10) || !isPassed(id, 11) || !isPassed(id, 12))
			{
				if(Diags.showConf(msg, Diags.YN) != 0) return false;
			}
			return statement.executeUpdate(String.format(sql, EDUC_YEAR, id)) == 1;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean isPassed(int sid, int grade)
	{
		String sqlSubs = "SELECT COUNT(DISTINCT sg_sub_id) AS subs FROM subject_grade WHERE sg_grade = " + grade;
		
		String sqlPassed = "SELECT COUNT(DISTINCT mark_sub_id) AS passed FROM marks "
				+ " WHERE mark_grade = %d AND mark_st_id = %d AND ( mark_total >= %d OR mark_second >= %d) "
				+ " GROUP BY mark_year";
		sqlPassed = String.format(sqlPassed, grade, sid, PASS_GRADE_FINAL, PASS_GRADE_FINAL);
		int subs = 0;
		try{
			results = statement.executeQuery(sqlSubs);
			if(results.next()){
				subs = results.getInt("subs");
			}
			results = statement.executeQuery(sqlPassed);
			while(results.next()){
				if (results.getInt("passed") >= subs){
					return true;
				}
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean enrolStudent(int sid, int cid, int grade)
	{	
		String msg = String.format("<html><p>%s: <b>%d</b></p><br>", Dic.w("student_code"), sid);
		boolean error = false;
		if(isEnrolled(sid, grade)){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_already_enrolled")));
		}
		if(isPassed(sid, grade)){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_has_passed_grade")));
		}
		if(studentGrade(sid) != grade){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_grade_not_match")));
		}
		msg = msg.concat(String.format("<p>%s</p></html>", Dic.w("continue_ask")));
		if(error){
			int conf = Diags.showConf(msg, Diags.YN);
			if(conf != 0) return false;
		}
		String sql = "REPLACE INTO enrolment (enrol_course_id, enrol_st_id) VALUES (%d, %d)";
		String sql2 = "UPDATE students SET st_state = 'a' WHERE st_id = " + sid;
		try{
			if(statement.executeUpdate(String.format(sql, cid, sid)) > 0)
			{
				return statement.executeUpdate(sql2) > 0;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean upgradeStudent(int sid, int cid, int grade)
	{
		String msg = String.format("<html><p>%s: <b>%d</b></p><br>", Dic.w("student_code"), sid);
		boolean error = false;
		if(!isPassed(sid, grade-1)){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_not_passed_prev_grade")));
		}
		if(isEnrolled(sid, grade)){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_already_enrolled")));
		}
		if(studentGrade(sid) >= grade){
			error = true;
			msg = msg.concat(String.format("<p>%s</p><br>", Dic.w("student_is_from_upper_grade")));
		}
		msg = msg.concat(String.format("<p>%s</p></html>", Dic.w("continue_ask")));
		if(error){
			int conf = Diags.showConf(msg, Diags.YN);
			if(conf != 0) return false;
		}
		String sql1 = "REPLACE INTO enrolment (enrol_course_id, enrol_st_id) VALUES (%d, %d)";
		String sql2 = "UPDATE students SET st_grade = %d , st_state = 'a' WHERE st_id = %d";
		try{
			if(statement.executeUpdate(String.format(sql1, cid, sid)) > 0)
			{
				return statement.executeUpdate(String.format(sql2, grade, sid)) > 0;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean isEnrolled(int sid, int grade)
	{
		String sql = "SELECT course_id FROM course , enrolment WHERE course_id = enrol_course_id "
				+ "AND course_grade = %d AND course_year = %d AND enrol_st_id = %d";
		sql = String.format(sql, grade, EDUC_YEAR, sid);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
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
	
	public int studentGrade(int id)
	{
		String sql = "SELECT st_grade FROM students WHERE st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("st_grade");
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
	
	public boolean switchStudent(int sid, int old, int now)
	{
		String sql = "UPDATE enrolment SET enrol_course_id = %d WHERE enrol_course_id = %d AND enrol_st_id = %d";
		sql = String.format(sql, now, old, sid);
		try{
			return (statement.executeUpdate(sql) == 1);
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public Vector<Object[]> studentsList(int cid)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT st_id, st_name, st_lname, st_fname, st_gfname, st_code FROM students , enrolment WHERE enrol_st_id = st_id AND enrol_course_id = " + cid;
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				rows.add(new Object[]{
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_gfname"),
						results.getInt("st_code"),
						results.getInt("st_id"),
				});
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
	public int memberCount(int id, boolean forbidden) 
	{
		String sql = "SELECT COUNT(enrol_id) AS total FROM enrolment WHERE 1 AND enrol_course_id = " + id;
		if(forbidden){
			sql = sql.concat(" AND enrol_forbidden = 1");
		}
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("total");
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
	public int excusedCount(Course course, int season)
	{
		String sql = "SELECT COUNT(DISTINCT mark_st_id) AS total FROM marks, enrolment WHERE mark_st_id = enrol_st_id "
				+ " AND enrol_course_id = %d AND mark_year = %d AND mark_grade = %d AND %s = 1";
		String col = season == 1 ? "mark_excused_midterm" : "mark_excused_final";
		sql = String.format(sql, course.id, course.year, course.grade, col);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("total");
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
	public int failedCount(Course course, int season)
	{
		String sql = "SELECT COUNT(subs) AS total FROM ("
				+ " SELECT COUNT(mark_id) AS subs FROM marks , enrolment WHERE mark_st_id = enrol_st_id "
				+ " AND enrol_course_id = %d AND mark_year = %d AND mark_grade = %d AND %s < %d GROUP BY mark_st_id ) "
				+ " AS fails WHERE subs >= %d";
		String col = season == 1 ? "mark_half"  : "mark_total";
		int mark = season == 1 ? PASS_GRADE_MID : PASS_GRADE_FINAL;
		int count = season == 1 ? 1 : FAIL_SUBJ_COUNT;
		sql = String.format(sql, course.id, course.year, course.grade, col, mark, count);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("total");
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
	public int passedCount(Course course, int season)
	{
		String sql = "SELECT COUNT(passed) AS total FROM ("
				+ " SELECT COUNT(mark_id) AS passed FROM marks , enrolment WHERE mark_st_id = enrol_st_id "
				+ " AND enrol_course_id = %d AND mark_year = %d AND mark_grade = %d AND %s >= %d GROUP BY mark_st_id) "
				+ " AS passes WHERE passed >= (SELECT COUNT(sg_id) FROM subject_grade WHERE sg_grade = %d)";
		String col = season == 1 ? "mark_half"  : "mark_total";
		int mark = season == 1 ? PASS_GRADE_MID : PASS_GRADE_FINAL;
		sql = String.format(sql, course.id, course.year, course.grade, col, mark, course.grade);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("total");
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



























