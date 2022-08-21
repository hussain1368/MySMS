package com.kabulbits.shoqa.db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class Data 
{
	private final String URL = "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";
	protected Connection connection = null;
	protected Statement statement = null;
	protected ResultSet results = null;
	
	protected Logger logger;
	
	public static final int LIMIT = 20;
	public static int FINAN_PRD = 1;
	public static int EDUC_YEAR = 1394;
	public static int START_YEAR = 1387;
	
	// Global Settings
	public static String SCHOOL_TITLE = "";
	public static String MINISTRY_TITLE = "";
	public static String HEAD_TITLE = "";
	public static String ADMINISTER = "";
	
	// Academic Settings
	public static int FULL_GRADE_MID = 40;
	public static int PASS_GRADE_MID = 16;
	public static int FULL_GRADE_FINAL = 100;
	public static int PASS_GRADE_FINAL = 40;
	public static int FAIL_SUBJ_COUNT = 4;
	public static int ATTEND_MID = 80;
	public static int ATTEND_FINAL = 85;
	
	// User Info
	public static String USER_FULLNAME = "Yasin";
	public static int USER_ID = 1;
	public static int PERM_STUDENTS = 2;
	public static int PERM_EMPLOYEES = 2;
	public static int PERM_COURSES = 2;
	public static int PERM_FINANCE = 2;
	public static int PERM_LIBRARY = 2;
	public static int PERM_USERS = 2;
	public static int PERM_SETTINGS = 2;

	public Data(){
		this(true);
	}
	public Data(boolean setupConn)
	{
		logger = Logger.getLogger(Data.class);
		PropertyConfigurator.configure("resources/log4j.properties");
		if(setupConn){
			setupConn();
		}
	}
	public boolean setupConn()
	{
		try{
			Properties conn = new Properties();
			conn.load(new FileInputStream(new File("resources/connection.properties")));
			String host = conn.getProperty("hostname");
			String port = conn.getProperty("port");
			String dbname = conn.getProperty("dbname");
			String username = conn.getProperty("username");
			String password = conn.getProperty("password");
			
			Class.forName("com.mysql.jdbc.Driver");
			String url = String.format(URL, host, port, dbname);
			connection = DriverManager.getConnection(url, username, password);
			statement = connection.createStatement();
		} 
		catch (Exception e){
			Diags.showErrLang("cant_connect_to_database");
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
			return false;
		}
		return true;
	}
	
	public void closeConn(){
		try{
			if(results != null){
				results.close();
			}
			if(connection != null){
				connection.close();
			}
			if(statement != null){
				statement.close();
			}
		}
		catch (Exception e) {
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public static boolean isTrial(){
		return App.TRIAL;
	}
	
	public boolean recordLimit(String table)
	{
		int recordLimit = 2;
		String sql = "select count(*) as num from " + table;
		try{
			ResultSet res = statement.executeQuery(sql);
			if(res.next()){
				return res.getInt("num") >= recordLimit;
			}
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return true;
	}
	
//	public boolean saveConnHost()
//	{
//		try{
//			File file = new File("resources/connection.properties");
//			Properties conn = new Properties();
//			conn.load(new FileInputStream(file));
//			conn.setProperty("hostname", HOST);
//			OutputStream out = new FileOutputStream(file);
//			conn.store(out, "Connection Settings");
//			out.close();
//		}
//		catch (FileNotFoundException e) {
//			if(App.LOG){
//				logger.error(e.getMessage(), e);
//			}
//			return false;
//		} 
//		catch (IOException e) {
//			if(App.LOG){
//				logger.error(e.getMessage(), e);
//			}
//			return false;
//		}
//		return true;
//	}
	
	public boolean findPerson(String table, String col, String id)
	{
		String sql = "SELECT %s FROM %s WHERE %s = %s";
		sql = String.format(sql, col, table, col, id);
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
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
	
	public Vector<Option> provinces()
	{
		Vector<Option> options = new Vector<>();
		options.add(new Option(0, Dic.w("province")));
		
		String sql = "select * from province";
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int id = results.getInt("prov_id");
				String name = results.getString("prov_name");
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
	
	public Vector<Option> districts(int pid)
	{
		Vector<Option> options = new Vector<>();
		String sql = String.format("select * from district where dist_prov_id = %d", pid);
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int id = results.getInt("dist_id"); 
				String name = results.getString("dist_name");
				options.add(new Option(id, name));
			}
		}
		catch(SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return options;
	}
	
	public int insertId()
	{
		try {
			results = statement.executeQuery("SELECT LAST_INSERT_ID() AS id");
			if(results.next()){
				return results.getInt("id");
			}
		}
		catch(SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 0;
	}

	public String finanPeriodName() 
	{
		String sql = "SELECT period_name FROM financial_period WHERE period_id = " + FINAN_PRD;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getString("period_name");
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

	public int[] getYearSettings() 
	{
		String sql = "SELECT * FROM year_settings WHERE educ_year = " + EDUC_YEAR;
		try{
			results = statement.executeQuery(sql);
			if(results.next())
			{
				int values [] = {
						results.getInt("midterm_full"),
						results.getInt("midterm_pass"),
						results.getInt("final_pass"),
						results.getInt("fail_subj"),
						results.getInt("midterm_attend"),
						results.getInt("final_attend"),
				};
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
	
	public boolean saveYearSettings(int values[])
	{
		String sql = "REPLACE INTO year_settings (`educ_year`,`midterm_full`,`midterm_pass`,`final_pass`,`fail_subj`,`midterm_attend`,`final_attend`) VALUES (?,?,?,?,?,?,?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, EDUC_YEAR);
			for(int value : values){
				pst.setInt(i++, value);
			}
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
	
	public String[] getGeneralSettings()
	{
		String sql = "select * from general_settings limit 1";
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				String values [] = {
						results.getString("school_name"),
						results.getString("ministry_name"),
						results.getString("head_name"),
						results.getString("administer_name"),
						results.getString("start_year"),
				};
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
	
	public boolean saveGeneralSettings(String values[])
	{
		String sql = "UPDATE general_settings SET `school_name`=?, `ministry_name`=?, `head_name`=?, `administer_name`=?, `start_year`=? WHERE id = 1";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			for(String val : values){
				pst.setString(i++, val);
			}
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
	
	public boolean setEducYear(int year)
	{
		String sql = "update general_settings set educ_year = %d where id = 1";
		sql = String.format(sql, year);
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
	
	public boolean setCurrentFinanPeriod(int id) 
	{
		String sql1 = "UPDATE financial_period SET period_active = 0";
		String sql2 = "UPDATE financial_period SET period_active = 1 WHERE period_id = " + id;
		try {
			connection.setAutoCommit(false);
			statement.executeUpdate(sql1);
			statement.executeUpdate(sql2);
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
	
	public boolean loadConfigs()
	{
		String sql1 = "select * from general_settings limit 1";
		String sql2 = "select * from year_settings where educ_year = ";
		String sql3 = "SELECT period_id FROM financial_period WHERE period_active = 1";
		try{
			results = statement.executeQuery(sql1);
			if(results.next()){
				SCHOOL_TITLE = results.getString("school_name");
				MINISTRY_TITLE = results.getString("ministry_name");
				HEAD_TITLE = results.getString("head_name");
				ADMINISTER = results.getString("administer_name");
				START_YEAR = results.getInt("start_year");
				EDUC_YEAR = results.getInt("educ_year");
			}
			results = statement.executeQuery(sql2 + EDUC_YEAR);
			if(results.next()){
				FULL_GRADE_MID = results.getInt("midterm_full");
				PASS_GRADE_MID = results.getInt("midterm_pass");
				PASS_GRADE_FINAL = results.getInt("final_pass");
				FAIL_SUBJ_COUNT = results.getInt("fail_subj");
				ATTEND_MID = results.getInt("midterm_attend");
				ATTEND_FINAL = results.getInt("final_attend");
			}
			results = statement.executeQuery(sql3);
			if(results.next()){
				FINAN_PRD = results.getInt("period_id");
			}
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
	
	public int[] findConfigs(int year)
	{
		int[] configs = {0, 0, 0, 0, 0, 0};
		String sql = "select * from year_settings where educ_year = " + year;
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				int i = 0;
				configs[i++] = results.getInt("midterm_full");
				configs[i++] = results.getInt("midterm_pass");
				configs[i++] = results.getInt("final_pass");
				configs[i++] = results.getInt("fail_subj");
				configs[i++] = results.getInt("midterm_attend");
				configs[i++] = results.getInt("final_attend");
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return configs;
	}
	
	public boolean truncateSchedules()
	{
		String sql = "TRUNCATE course_schedule";
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
	
	public boolean resetStudentsState()
	{
		String sql = "UPDATE students SET st_state = 'p' WHERE st_state = 'a'";
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
	
	public Course findCourse(int cid) {
		String sql = "select * from course where course_id = " + cid;
		try {
			results = statement.executeQuery(sql);
			if (results.next()) {
				int id = results.getInt("course_id");
				int year = results.getInt("course_year");
				int grade = results.getInt("course_grade");
				int shift = results.getInt("course_shift");
				String name = results.getString("course_name");
				return new Course(id, year, grade, shift, name);
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
	public Vector<Course> searchCourse(int y, int g) 
	{
		Vector<Course> courses = new Vector<>();

		String sql = "SELECT * FROM course WHERE course_year = %d AND course_grade = %d";
		try {
			results = statement.executeQuery(String.format(sql, y, g));
			while (results.next()) 
			{
				int id = results.getInt("course_id");
				int year = results.getInt("course_year");
				int grade = results.getInt("course_grade");
				String name = results.getString("course_name");
				courses.add(new Course(id, year, grade, name));
			}
		} 
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return courses;
	}
	@Override
	protected void finalize() throws Throwable{
		closeConn();
		super.finalize();
	}
}






























