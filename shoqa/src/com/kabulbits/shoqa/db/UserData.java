package com.kabulbits.shoqa.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Vector;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;

public class UserData extends Data {
	
	public UserData(boolean setupConn){
		super(setupConn);
	}

	public boolean insertUser(String full, String user, String pass, int[] perms) 
	{
		String sql = "INSERT INTO users (`user_fullname`,`user_username`,`user_password`,`user_sts`,`user_emps`,"
				+ "`user_courses`,`user_finance`,`user_library`,`user_users`,`user_settings`) VALUES (?,?,MD5(?),?,?,?,?,?,?,?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setString(i++, full);
			pst.setString(i++, user);
			pst.setString(i++, pass);
			for(int perm : perms){
				pst.setInt(i++, perm);
			}
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("duplicate_username_error");
			}else {
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public Object[] findUser(int id) 
	{
		String sql = "SELECT * FROM users WHERE user_id = " + id;
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				Object info [] = {
						results.getString("user_fullname"),
						results.getString("user_username"),
						results.getInt("user_sts"),
						results.getInt("user_emps"),
						results.getInt("user_courses"),
						results.getInt("user_finance"),
						results.getInt("user_library"),
						results.getInt("user_users"),
						results.getInt("user_settings"),
				};
				return info;
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

	public boolean editUser(int id, String full, String user, int[] perms) 
	{
		String sql = "UPDATE users SET `user_fullname` = ?, `user_username` = ?, `user_sts` = ?, `user_emps` = ?, `user_courses` = ?,"
				+ "`user_finance` = ?, `user_library` = ?, `user_users` = ?, `user_settings` = ?  WHERE user_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setString(i++, full);
			pst.setString(i++, user);
			for(int perm : perms){
				pst.setInt(i++, perm);
			}
			pst.setInt(i++, id);
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("duplicate_username_error");
			}else {
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public int countUsers(String term) {
		String sql = "select count(*) as num from users where 1 ";
		if(term.length() > 0){
			sql = sql.concat(String.format(" and (user_id like '%%%s%%' or user_username like '%%%s%%')", term, term));
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

	public Vector<Object[]> searchUsers(String term, int page) 
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "select * from users where user_id != 1 ";
		
		if(term.length() > 0){
			sql = sql.concat(String.format(" and (user_id like '%%%s%%' or user_fullname like '%%%s%%')" , term, term));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" order by user_id desc limit %d, %d", start, LIMIT));
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("user_id"),
						results.getString("user_fullname"),
						results.getString("user_username"),
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

	public boolean deleteUser(String id) 
	{
		String sql = "delete from users where user_id = " + id;
		try {
			return statement.executeUpdate(sql) == 1;
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

	public boolean changePass(int id, String pass) 
	{
		String sql = "update users set user_password = md5(?) where user_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, pass);
			pst.setInt(2, id);
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

	public Vector<Option> financePeriods() 
	{
		Vector<Option> options = new Vector<Option>();
		String sql = "SELECT period_id, period_name, period_active FROM financial_period ORDER BY period_id DESC";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				int id = results.getInt("period_id");
				int active = results.getInt("period_active");
				String name = results.getString("period_name");
				Option option = new Option(id, active, name);
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

	public boolean authenticate(String user, String pass) 
	{
		String sql = "SELECT * FROM users WHERE user_username = ? AND user_password = MD5(?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, user);
			pst.setString(2, pass);
			results = pst.executeQuery();
			if(results.next())
			{
				USER_FULLNAME = results.getString("user_fullname");
				USER_ID = results.getInt("user_id");
				PERM_STUDENTS = results.getInt("user_sts");
				PERM_EMPLOYEES = results.getInt("user_emps");
				PERM_COURSES = results.getInt("user_courses");
				PERM_FINANCE = results.getInt("user_finance");
				PERM_LIBRARY = results.getInt("user_library");
				PERM_USERS = results.getInt("user_users");
				PERM_SETTINGS = results.getInt("user_settings");
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

	public boolean passwordMatch(String pass, int id) 
	{
		String sql = "SELECT user_password = MD5(?) AS yes FROM users WHERE user_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, pass);
			pst.setInt(2, id);
			results = pst.executeQuery();
			if(results.next()){
				return results.getBoolean("yes");
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

}





















