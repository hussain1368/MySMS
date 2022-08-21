package com.kabulbits.shoqa.db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class StudentData extends Data {

	public int maxStudentCode() {
		String sql = "SELECT MAX(st_code) AS max_id FROM students";
		try {
			results = statement.executeQuery(sql);
			if (results.next()) {
				int id = results.getInt(1);
				if (id < 1)
					return 1;
				else
					return id + 1;
			}
		} 
		catch(Exception e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}
	
	public boolean insertStudent(Student st) 
	{
		String sql = "INSERT INTO students (st_code,st_doc_no,st_name,st_lname,st_fname,st_gfname,st_mother_lang,"
				+ "st_idcard,st_phone,st_father_job,st_mother_job, st_doc_date, st_blood,st_gender,st_birth_date,st_birth_place,"
				+ "st_main_prov, st_main_dist, st_main_vill, st_curr_prov, st_curr_dist, st_curr_vill,"
				+ "st_grade, st_official, st_reg_year, st_image, st_state)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'a')";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			
			if(st.code != 0){
				pst.setInt(1, st.code);
			}else{
				pst.setObject(1, null);
			}
			
			int i = 1;
			String [] data = st.array();
			for(; i<data.length; i++) {
				pst.setString(i+1, data[i]);
			}
			i++;
			
			Date doc = st.docDate != null ? new Date(st.docDate.getTime()) : null;
			Date birth = st.birthDate != null ? new Date(st.birthDate.getTime()) : null;
			
			pst.setDate(i++, doc);
			pst.setString(i++, st.bloodGroup);
			pst.setString(i++, st.gender);
			pst.setDate(i++, birth);
			pst.setString(i++, st.birthPlace);
			pst.setInt(i++, st.mainProv);
			pst.setInt(i++, st.mainDist);
			pst.setString(i++, st.mainVill);
			pst.setInt(i++, st.currProv);
			pst.setInt(i++, st.currDist);
			pst.setString(i++, st.currVill);
			pst.setInt(i++, st.grade);
			pst.setInt(i++, st.official);
			pst.setInt(i++, Data.EDUC_YEAR);
			pst.setBytes(i++, st.image);
			
			return (pst.executeUpdate() == 1);
		} 
		catch (SQLException e) {
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("st_code_duplicate_error");
			}else{
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
	
	public Vector<String []> studentTransfers(int sid)
	{
		Vector<String[]> rows = new Vector<String[]>();
		String sql = "SELECT * FROM student_transfer WHERE trans_st_id = " + sid;
		try
		{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				String type = results.getString("trans_type");
				Date transDate = results.getDate("trans_date");
				Date docDate = results.getDate("trans_doc_date");
				
				String row [] = {
						results.getString("trans_id"),
						results.getString("trans_year"),
						results.getString("trans_grade"),
						type.equals("in") ? Dic.w("entrance") : Dic.w("detach"),
						results.getString("trans_no"),
						transDate == null ? "" : df.format(transDate),
						results.getString("trans_doc_no"),
						docDate == null ? "" : df.format(docDate),
						results.getString("trans_prev_st_code"),
						results.getString("trans_school"),
						results.getString("trans_reason"),
						results.getString("trans_desc"),
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
	
	public Course studentCourse(int s, int g, int y)
	{
		String sql = "SELECT course.* FROM course , enrolment WHERE course_id = enrol_course_id "
				+ "AND course_year = %d AND course_grade = %d AND enrol_st_id = %d";
		sql = String.format(sql, y, g, s);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				int id = results.getInt("course_id");
				int year = results.getInt("course_year");
				int grade = results.getInt("course_grade");
				String name = results.getString("course_name");
				return new Course(id, year, grade, name);
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

	public Vector<Object[]> searchStudents(String col, String word, String sex, int grade, int year, boolean active, int official, int page) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT st_id, st_code, st_name, st_fname, st_lname, st_idcard, st_grade, "
				+ "st_reg_year, st_grad_year, st_state FROM students WHERE 1 ";
		if(grade != 0){
			sql = sql.concat(String.format(" AND st_grade = %d ", grade));
		}
		if(year != 0){
			sql = sql.concat(String.format(" AND st_reg_year = %d ", year));
		}
		if(!sex.equals("b")){
			sql = sql.concat(String.format(" AND st_gender = '%s' ", sex));
		}
		if(active){
			sql = sql.concat(" AND st_state = 'a' ");
		}
		if(official != 2){
			sql = sql.concat(String.format(" AND st_official = %d ", official));
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%' ", col, word));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY st_code DESC, st_id DESC LIMIT %d, %d", start, LIMIT));
		
		try{
			results = statement.executeQuery(sql);
			while (results.next()) 
			{
				String s = results.getString("st_state");
				String state = s.equals("a") ? "active" : s.equals("p") ? "passive" : s.equals("g") ? "graduated" : "";
				Object row [] = {
						results.getInt("st_id"),
						results.getInt("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_lname"),
						results.getString("st_idcard"),
						results.getInt("st_grade"),
						results.getString("st_reg_year"),
						results.getString("st_grad_year"),
						Dic.w(state),
				};
				rows.add(row);
			}
		} 
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return rows;
	}
	
	public int countStudents(String col, String word, String sex, int grade, int year, boolean active, int official)
	{
		String sql = "SELECT COUNT(st_id) as sts FROM students WHERE 1 ";
		
		if(grade != 0){
			sql = sql.concat(String.format(" AND st_grade = %d ", grade));
		}
		if(year != 0){
			sql = sql.concat(String.format(" AND st_reg_year = %d ", year));
		}
		if(!sex.equals("b")){
			sql = sql.concat(String.format(" AND st_gender = '%s' ", sex));
		}
		if(active){
			sql = sql.concat(" AND st_state = 'a' ");
		}
		if(official != 2){
			sql = sql.concat(String.format(" AND st_official = %d ", official));
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%' ", col, word));
		}
		try {
			results = statement.executeQuery(sql);
			if (results.next()) {
				return results.getInt("sts");
			}
		}
		catch (SQLException e) {
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}

	public Student findStudent(int id, boolean brief) 
	{
		String sql_1 = "SELECT * FROM students WHERE st_id = " + id;
		String sql_2 = "SELECT st_id, st_code, st_name, st_lname, st_fname, st_grade FROM students WHERE st_id = " + id;
		try {
			results = statement.executeQuery(brief ? sql_2 : sql_1);
			if (results.next()) {
				
				Student student = new Student(id);
				student.code = results.getInt("st_code");
				student.name = results.getString("st_name");
				student.lname = results.getString("st_lname");
				student.fname = results.getString("st_fname");
				student.grade = results.getInt("st_grade");
				
				if(brief) return student;
				
				student.docNo = results.getInt("st_doc_no");
				student.gfname = results.getString("st_gfname");
				student.motherLang = results.getString("st_mother_lang");
				student.idcard = results.getString("st_idcard");
				student.phone = results.getString("st_phone");
				student.fatherJob = results.getString("st_father_job");
				student.motherJob = results.getString("st_mother_job");
				student.bloodGroup = results.getString("st_blood");
				student.docDate = results.getDate("st_doc_date");
				student.gender = results.getString("st_gender");
				student.birthDate = results.getDate("st_birth_date");
				student.birthPlace = results.getString("st_birth_place");
				student.mainProv = results.getInt("st_main_prov");
				student.mainDist = results.getInt("st_main_dist");
				student.mainVill = results.getString("st_main_vill");
				student.currProv = results.getInt("st_curr_prov");
				student.currDist = results.getInt("st_curr_dist");
				student.currVill = results.getString("st_curr_vill");
				student.state = results.getString("st_state");
				student.official = results.getInt("st_official");
				student.gradYear = results.getInt("st_grad_year");
				student.regYear = results.getInt("st_reg_year");
				student.image = results.getBytes("st_image");
				
				return student;
			}
		} 
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public String studMainAddr(int id)
	{
		String sql = "SELECT prov_name , dist_name , st_main_vill FROM students , province , district WHERE st_main_prov = prov_id AND st_main_dist = dist_id AND st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return String.format("%s - %s - %s", results.getString(1), results.getString(2), results.getString(3));
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}
	
	public String studCurrAddr(int id)
	{
		String sql = "SELECT prov_name , dist_name , st_curr_vill FROM students , province , district WHERE st_curr_prov = prov_id AND st_curr_dist = dist_id AND st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return String.format("%s - %s - %s", results.getString(1), results.getString(2), results.getString(3));
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}
	
	public int findGrade(int id){
		String sql = "SELECT st_grade , st_state FROM students WHERE st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return (results.getString("st_state").equals("g"))? 13 : results.getInt("st_grade");
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

	public boolean editStudent(int id, Student st) 
	{
		String sql = "UPDATE students SET st_code = ?, st_doc_no = ? , st_name = ?, st_lname = ?, st_fname = ?, st_gfname = ? ,"
				+ " st_mother_lang = ?, st_idcard = ?, st_phone = ?, st_father_job = ?, st_mother_job= ?, st_doc_date = ?, st_blood = ?, "
				+ " st_gender = ?, st_birth_date = ?, st_birth_place = ?, st_main_prov = ?, st_main_dist = ?, st_main_vill = ?,"
				+ " st_curr_prov = ?, st_curr_dist = ?, st_curr_vill = ?, st_grade = ?, st_official = ?, st_image = ? WHERE st_id = ? ";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			if(st.code != 0){
				pst.setInt(1, st.code);
			}else{
				pst.setObject(1, null);
			}
			String [] data = st.array();
			int i = 1;
			for(; i<data.length; i++){
				pst.setString(i+1, data[i]);
			}
			i++;
			
			Date doc = st.docDate != null ? new Date(st.docDate.getTime()) : null;
			Date birth = st.birthDate != null ? new Date(st.birthDate.getTime()) : null;
			
			pst.setDate(i++, doc);
			pst.setString(i++, st.bloodGroup);
			pst.setString(i++, st.gender);
			pst.setDate(i++, birth);
			pst.setString(i++, st.birthPlace);
			pst.setInt(i++, st.mainProv);
			pst.setInt(i++, st.mainDist);
			pst.setString(i++, st.mainVill);
			pst.setInt(i++, st.currProv);
			pst.setInt(i++, st.currDist);
			pst.setString(i++, st.currVill);
			pst.setInt(i++, st.grade);
			pst.setInt(i++, st.official);
			pst.setBytes(i++, st.image);
			pst.setInt(i++, id);
			
			return pst.executeUpdate() == 1;
		} 
		catch (SQLException e) {
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("st_code_duplicate_error");
			}else{
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
	
	public boolean deleteStudent(String id) 
	{
		String sql = "delete from students where st_id =" + id;
		try {
			return (statement.executeUpdate(sql) == 1);
		} 
		catch (SQLException e) {
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
	private ULocale locale = new ULocale("@calendar=persian");
	private DateFormat df = new SimpleDateFormat("yyyy/MM/dd", locale);
	
	public Vector<Object []> transferredStudents(String word, int type, int year, int grade, int page)
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT st_id, st_code, st_name, student_transfer.* FROM student_transfer JOIN students ON trans_st_id = st_id WHERE trans_year = " + year;
		if(grade != 0){
			sql = sql.concat(" AND trans_grade = " + grade);
		}
		if(type != 0){
			sql = sql.concat(String.format(" AND trans_type = '%s' ", type == 1 ? "in" : "out"));
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND (st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%' OR trans_school LIKE '%%%s%%') ", word, word, word));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY trans_year DESC LIMIT %d, %d", start, LIMIT));
		try
		{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				String type1 = results.getString("trans_type");
				type1 = type1.equals("in") ? Dic.w("entrance") : Dic.w("detach");
				Date transDate = results.getDate("trans_date");
				Date docDate = results.getDate("trans_doc_date");
				
				Object row [] = {
						results.getInt("trans_id"),
						results.getInt("st_id"),
						results.getInt("trans_no"),
						transDate == null ? "" : df.format(transDate),
						results.getString("trans_doc_no"),
						docDate == null ? "" : df.format(docDate),
						results.getString("trans_prev_st_code"),
						type1,
						results.getString("st_code"),
						results.getString("st_name"),
						results.getString("trans_grade"),
						results.getString("trans_year"),
						results.getString("trans_reason"),
						results.getString("trans_school"),
						results.getString("trans_desc"),
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
	
	public int countTransform(String word, int type, int year, int grade)
	{
		String sql = "SELECT COUNT(st_id) as num FROM student_transfer JOIN students ON trans_st_id = st_id WHERE trans_year = " + year;
		if(grade != 0){
			sql = sql.concat(" AND trans_grade = " + grade);
		}
		if(type != 0){
			sql = sql.concat(String.format(" AND trans_type = '%s'", type == 1 ? "in" : "out"));
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND (st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%' OR trans_school LIKE '%%%s%%')", word, word, word));
		}
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
	
	public Map<String, Object> studDocInfo(int id)
	{
		Map<String, Object> info = new HashMap<>();
		String sql = "SELECT st_doc_no, st_doc_date FROM students WHERE st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				info.put("doc_no", results.getString("st_doc_no"));
				info.put("doc_date", results.getDate("st_doc_date"));
				return info;
			}
		}catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public boolean transferStudent(int id, int g, Map<String, Object> values)
	{
		String sql = "INSERT INTO student_transfer (trans_st_id , trans_grade , trans_year , trans_type, trans_prev_st_code,"
				+ "  trans_no, trans_date, trans_doc_no, trans_doc_date, trans_reason, trans_school, trans_desc)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int y = Data.EDUC_YEAR;
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, g);
			pst.setInt(i++, y);
			pst.setObject(i++, values.get("type"));
			pst.setObject(i++, values.get("prev_code"));
			pst.setObject(i++, values.get("trans_no"));
			pst.setObject(i++, values.get("trans_date"));
			pst.setObject(i++, values.get("doc_no"));
			pst.setObject(i++, values.get("doc_date"));
			pst.setObject(i++, values.get("reason"));
			pst.setObject(i++, values.get("school"));
			pst.setObject(i++, values.get("desc"));
			
			if(pst.executeUpdate() > 0)
			{
				String s = values.get("type").equals("in") ? "a" : "p";
				String sql1 = "UPDATE students SET st_state = '%s' WHERE st_id = %d";
				sql1 = String.format(sql1, s, id);
				statement.executeUpdate(sql1);
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
	
	public boolean deleteTransform(int id)
	{
		String sql = "DELETE FROM student_transfer WHERE trans_id = " + id;
		try{
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean insertRelation(String[] values, int sid, String rel) 
	{
		String sql = "insert into relation (rel_name, rel_lname, rel_language, rel_birth_date, "
				+ "rel_educ_level, rel_educ_field, rel_job, rel_living_loc, rel_job_loc, rel_phone)"
				+ "values(? , ? , ? , ? , ? , ? , ? , ? , ? , ?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			for(int i=1; i<=values.length; i++)
			{
				pst.setString(i, values[i-1]);
			}
			if(pst.executeUpdate() == 1)
			{
				int id = insertId();
				String sql1 = "insert into student_relation (rel_st_id, rel_rel_id, rel_type)"
						+ "values (%d, %d, '%s')";
				sql1 = String.format(sql1, sid, id, rel);
				statement.executeUpdate(sql1);
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

	public Vector<String[]> studentRelations(int sid)
	{
		Vector<String []> rows = new Vector<String[]>();
		String sql = "select * from relation inner join student_relation "
				+ "on reL_id = rel_rel_id and rel_st_id = " + sid;
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				String [] row = {
						results.getString("rel_id"),	
						results.getString("rel_name"),	
						results.getString("rel_lname"),	
						results.getString("rel_phone"),	
						results.getString("rel_job"),
						Dic.w(results.getString("rel_type")),
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
	
	public String[] studExactRelation(int sid, String rel)
	{
		String sql = "SELECT CONCAT(rel_name, ' ', rel_lname) AS the_name, YEAR(CURDATE()) - YEAR(rel_birth_date) AS age, rel_language, rel_educ_level, rel_educ_field, rel_job, rel_living_loc "
				+ " FROM relation , student_relation WHERE rel_id = rel_rel_id AND rel_st_id = %d AND rel_type = '%s' LIMIT 1";
		sql = String.format(sql, sid, rel);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				String[] data = new String[7];
				for(int i=0; i<data.length; i++){
					data[i] = results.getString(i+1);
				}
				return data;
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

	public int countRelations(String term)
	{
		String sql = "select count(*) as num from relation where 1 ";
		if(term.length() > 0){
			sql = sql.concat(String.format(" and (rel_id like '%%%s%%' or rel_name like '%%%s%%' or rel_lname like '%%%s%%')", term, term, term));
		}
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

	public Vector<Object []> searchRelations(String term, int page)
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "select * from relation where 1 ";
		
		if(term.length() > 0){
			sql = sql.concat(String.format(" and (rel_id like '%%%s%%' or rel_name like '%%%s%%' or rel_lname like '%%%s%%')" , term, term, term));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" order by rel_id desc limit %d, %d", start, LIMIT));
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("rel_id"),
						results.getString("rel_name"),
						results.getString("rel_lname"),
						results.getString("rel_phone"),
						results.getString("rel_job"),
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

	public boolean assignRelation(int sid, int pid, String rel)
	{
		String sql = "replace into student_relation (rel_st_id, rel_rel_id, rel_type) values (%d, %d, '%s')";
		sql = String.format(sql, sid, pid, rel);
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

	public Object[] findRelation(int id) 
	{
		String sql = "select * from relation where rel_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				Object item [] = {
						results.getString("rel_id"),
						results.getString("rel_name"),
						results.getString("rel_lname"),
						results.getString("rel_language"),
						results.getDate("rel_birth_date"),
						results.getString("rel_educ_level"),
						results.getString("rel_educ_field"),
						results.getString("rel_job"),
						results.getString("rel_living_loc"),
						results.getString("rel_job_loc"),
						results.getString("rel_phone"),
				};
				return item;
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

	public boolean editRelation(String[] values, int pid) 
	{
		String sql = "update relation set rel_name = ? , rel_lname = ? , rel_language = ? , rel_birth_date = ? , rel_educ_level = ? ,"
				+ " rel_educ_field = ? , rel_job = ? , rel_living_loc = ? , rel_job_loc = ? , rel_phone = ? where rel_id = ? ";
		
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			for(int i=0; i<values.length; i++)
			{
				pst.setString(i+1, values[i]);
			}
			pst.setInt(11, pid);
			
			return pst.executeUpdate() > 0;
		} 
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean detachRelation(int id, int sid) 
	{
		String sql = "delete from student_relation where rel_st_id = %d and rel_rel_id = %d ";
		sql = String.format(sql, sid, id);
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

	public boolean deleteRelation(String id) 
	{
		String sql = "delete from relation where rel_id = " + id;
		try{
			return statement.executeUpdate(sql) > 0;
		}
		catch(SQLException e){
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

	public Map<String, Object> findTransfer(int tid)
	{
		String sql = "select * from student_transfer where trans_id = " + tid;
		try{
			results = statement.executeQuery(sql);
			if(results.next())
			{
				Map<String, Object> values = new HashMap<String, Object>();
				values.put("type", results.getString("trans_type"));
				values.put("prev_code", results.getString("trans_prev_st_code"));
				values.put("trans_no", results.getString("trans_no"));
				values.put("doc_no", results.getString("trans_doc_no"));
				values.put("reason", results.getString("trans_reason"));
				values.put("school", results.getString("trans_school"));
				values.put("desc", results.getString("trans_desc"));
				values.put("trans_date", results.getDate("trans_date"));
				values.put("doc_date", results.getDate("trans_doc_date"));
				
				return values;
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

	public boolean editTransfer(int tid, Map<String, Object> values) 
	{
		String sql = "update student_transfer set trans_type = ? , trans_prev_st_code = ? , trans_no = ? , trans_date = ? , "
				+ " trans_doc_no = ? , trans_doc_date = ? , trans_reason = ? , trans_school = ? , trans_desc = ? where trans_id = ? ";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setObject(i++, values.get("type"));
			pst.setObject(i++, values.get("prev_code"));
			pst.setObject(i++, values.get("trans_no"));
			pst.setObject(i++, values.get("trans_date"));
			pst.setObject(i++, values.get("doc_no"));
			pst.setObject(i++, values.get("doc_date"));
			pst.setObject(i++, values.get("reason"));
			pst.setObject(i++, values.get("school"));
			pst.setObject(i++, values.get("desc"));
			pst.setInt(i++, tid);
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean updateState(int sid, int tid, String state) 
	{
		String sql = "";
		if(sid != 0){
			sql = "update students set st_state = '%s' where st_id = %d";
			sql = String.format(sql, state, sid);
		}else{
			sql = "update students set st_state = '%s' where st_id = (select trans_st_id from student_transfer where trans_id = %d)";
			sql = String.format(sql, state, tid);
		}
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

	public int findStCode(int id) 
	{
		String sql = "select st_code from students where st_id = " + id;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("st_code");
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

	public String findStudentRel(int stid, int pid) 
	{
		String sql = "select rel_type from student_relation where rel_st_id = %d and rel_rel_id = %d";
		sql = String.format(sql, stid, pid);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getString("rel_type");
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}

	public boolean setStudentRel(int stid, int pid, String rel) 
	{
		String sql = "update student_relation set rel_type = '%s' where rel_st_id = %d and rel_rel_id = %d";
		sql = String.format(sql, rel, stid, pid);
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

	public Vector<Object[]> studentsOfRelation(int id) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		String sql = "SELECT st_id, st_code, st_name, st_lname, st_fname, st_grade, rel_type "
				+ " FROM student_relation , students WHERE st_id = rel_st_id AND rel_rel_id = " + id;
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("st_id"),
						results.getInt("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_lname"),
						results.getInt("st_grade"),
						Dic.w(results.getString("rel_type")),
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

	public Vector<Object[]> studAdvancedList(int from, int to)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT st_code, st_doc_no, st_doc_date, st_grade, st_name, st_fname, st_gfname, st_idcard, YEAR(CURDATE()) - YEAR(st_birth_date) AS age, "
				+ " st_mother_lang, st_father_job, st_mother_job, prov_name, dist_name, st_main_vill, st_curr_vill "
				+ " FROM students JOIN province ON st_main_prov = prov_id JOIN district ON st_main_dist = dist_id WHERE st_code BETWEEN %d AND %d ORDER BY st_code";
		sql = String.format(sql, from, to);
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Date date = results.getDate("st_doc_date");
				String document = "";
				if(date != null){
					document = String.format("%d %s", results.getInt("st_doc_no"), df.format(date));
				}else{
					document = results.getString("st_doc_no");
				}
				rows.add(new Object[]{
						results.getInt("st_code"),
						document,
						results.getInt("st_grade"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_gfname"),
						results.getString("st_idcard"),
						results.getInt("age"),
						results.getString("st_mother_lang"),
						results.getString("st_father_job"),
						results.getString("st_mother_job"),
						results.getString("prov_name"),
						results.getString("dist_name"),
						results.getString("st_main_vill"),
						results.getString("st_curr_vill"),
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
}



















