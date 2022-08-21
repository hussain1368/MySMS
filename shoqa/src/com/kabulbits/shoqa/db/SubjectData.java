package com.kabulbits.shoqa.db;

import java.sql.SQLException;
import java.util.Vector;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;

public class SubjectData extends Data {
	
	public boolean addSubject(String name, int pos, Vector<Integer> grades) 
	{
		String sql0 = "UPDATE subjects SET sub_pos = sub_pos + 1 WHERE sub_pos >= %d";
		String sql1 = "INSERT INTO subjects(sub_name, sub_pos)VALUES('%s', %d)";
		String sql2 = "REPLACE INTO subject_grade (sg_sub_id, sg_grade) VALUES (%d, %d)";
		try {
			connection.setAutoCommit(false);
			statement.executeUpdate(String.format(sql0, pos));
			statement.executeUpdate(String.format(sql1, name, pos));
			
			int id = insertId();
			for(int g : grades){
				statement.executeUpdate(String.format(sql2, id, g));
			}
			connection.commit();
			connection.setAutoCommit(true);
		} 
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			try{
				connection.rollback();
				connection.setAutoCommit(true);
			} 
			catch (SQLException e1) {
				if(App.LOG){
					logger.error(e1.getMessage(), e1);
				}
			}
			return false;
		}
		return true;
	}
	
	public boolean editSubject(String name, int id, boolean [] old, boolean [] now)
	{
		String sql0 = "UPDATE subjects SET sub_name = '%s' WHERE sub_id = %d";
		String sql1 = "REPLACE INTO subject_grade (sg_sub_id, sg_grade) VALUES (%d, %d)";
		String sql2 = "DELETE FROM subject_grade WHERE sg_sub_id = %d AND sg_grade = %d";
		try {
			connection.setAutoCommit(false);
			statement.executeUpdate(String.format(sql0, name, id));
			for(int i=0; i<old.length; i++){
				if(old[i] != now[i]){
					if(now[i]){
						statement.executeUpdate(String.format(sql1, id, i+1));
					}else{
						statement.executeUpdate(String.format(sql2, id, i+1));
					}
				}
			}
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} 
			catch (SQLException e1) {
				if(App.LOG){
					logger.error(e1.getMessage(), e1);
				}
			}
			return false;
		}
		return true;
	}
	
	public boolean move(int fid, int fpos, int sid, int spos)
	{
		String sql = "UPDATE subjects SET sub_pos = %d WHERE sub_id = %d";
		try {
			connection.setAutoCommit(false);
			statement.executeUpdate(String.format(sql, spos, fid));
			statement.executeUpdate(String.format(sql, fpos, sid));
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			try {
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

	public Vector<Object[]> subjectsList()
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM subjects ORDER BY sub_pos";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Subject subject = new Subject(
						results.getInt("sub_id"), 
						results.getInt("sub_pos"), 
						results.getString("sub_name"));
				rows.add(new Object []{subject});
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
	
	public boolean [] subjectGrades(int id)
	{
		boolean grades [] = new boolean [12];
		for(int i=0; i<grades.length; i++){
			grades[i] = false;
		}
		String sql = "SELECT sg_grade FROM subject_grade WHERE sg_sub_id = %d ORDER BY sg_grade";
		sql = String.format(sql, id);
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				grades[results.getInt("sg_grade")-1] = true;
			}
		}
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return grades;
	}
	
	public String subjectName(int id)
	{
		String sql = "SELECT sub_name FROM subjects WHERE sub_id = %d";
		sql = String.format(sql, id);
		try{
			results = statement.executeQuery(sql);
			if(results.next())
				return results.getString("sub_name");
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}
	
	public int gradeSubjCount(int grade)
	{
		String sql = "SELECT COUNT(DISTINCT sg_sub_id) AS subs FROM subject_grade WHERE sg_grade = " + grade;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("subs");
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
