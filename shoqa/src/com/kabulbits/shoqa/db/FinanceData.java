package com.kabulbits.shoqa.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;
import com.kabulbits.shoqa.util.Dic;

public class FinanceData extends Data 
{
	//=================================== financial periods functions ============================================
	
	public Vector<Object[]> financialPeriods(int page)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM financial_period ORDER BY period_id DESC LIMIT %d, %d";
		int start = (page * LIMIT) - LIMIT;
		sql = String.format(sql, start, LIMIT);
		try{
			results = statement.executeQuery(sql);
			while (results.next())
			{
				Object row [] = {
					results.getInt("period_id"),
					results.getString("period_name"),
					df.format(results.getDate("period_start_date")),
					df.format(results.getDate("period_end_date")),
					results.getBoolean("period_active"),
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
	
	public boolean createFinanPeriod(String name, Date start, Date end) 
	{
		String sql = "INSERT INTO financial_period (`period_name`,`period_start_date`,`period_end_date`) VALUES (?,?,?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, name);
			pst.setObject(2, start);
			pst.setObject(3, end);
			
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

	public boolean editFinanPeriod(int id, String name, Date start, Date end) 
	{
		String sql = "UPDATE financial_period SET `period_name` = ?, `period_start_date` = ?, `period_end_date` = ? WHERE period_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setString(1, name);
			pst.setObject(2, start);
			pst.setObject(3, end);
			pst.setInt(4, id);
			
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

	public Object[] findFinanPeriod(int id) 
	{
		String sql = "SELECT * FROM financial_period WHERE period_id = " + id;
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				return new Object[] {
						results.getString("period_name"),
						results.getDate("period_start_date"),
						results.getDate("period_end_date"),
				};
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
	
	//===================================== cash functions ================================
	
	public Vector<Object[]> cashes() 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM cash ORDER BY cash_id ASC";
		try{
			results = statement.executeQuery(sql);
			while(results.next()) {
				Object row [] = {
						results.getInt("cash_id"),
						results.getString("cash_name"),
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
	
	public boolean saveCash(int id, String name)
	{
		String sql1 = "INSERT INTO cash (cash_name) VALUES ('%s')";
		String sql2 = "UPDATE cash SET cash_name = '%s' WHERE cash_id = %d";
		String sql = id == 0 ? String.format(sql1, name) : String.format(sql2, name, id);
		try {
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public Vector<Option> cashOptions(boolean firstOption)
	{
		Vector<Option> options = new Vector<>();
		if(firstOption){
			options.add(new Option(0, "---"));
		}
		String sql = "SELECT * FROM cash ORDER BY cash_id";
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Option option = new Option(results.getInt("cash_id"), results.getString("cash_name"));
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
	
	public boolean saveCashTrans(int type, int cash, int value, Date date, String desc)
	{
		String sql = "INSERT INTO cash_transaction (cash_trans_type, cash_trans_cash_id, cash_trans_value, "
				+ "cash_trans_date, cash_trans_desc, cash_trans_period) VALUES (?,?,?,?,?,?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, type);
			pst.setInt(i++, cash);
			pst.setInt(i++, value);
			pst.setObject(i++, date);
			pst.setString(i++, desc);
			pst.setInt(i++, FINAN_PRD);
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
	
	public Vector<Object[]> cashTransactions(Date from, Date to, int type, int page)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM cash_transaction JOIN cash ON cash_trans_cash_id = cash_id WHERE 1 AND cash_trans_date BETWEEN ? AND ?";
		if(type != 0){
			sql = sql.concat(" AND cash_trans_type = " + type);
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY cash_trans_date DESC LIMIT %d, %d", start, LIMIT));
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setDate(1, new java.sql.Date(from.getTime()));
			pst.setDate(2, new java.sql.Date(to.getTime()));
			results = pst.executeQuery();
			
			while(results.next())
			{
				int t = results.getInt("cash_trans_type");
				Object row [] = {
						results.getInt("cash_trans_id"),
						results.getInt("cash_trans_value"),
						df.format(results.getDate("cash_trans_date")),
						results.getString("cash_name"),
						Dic.w(t == 1 ? "credit" : "debit"),
						results.getString("cash_trans_desc")
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
	
	public boolean editCashTrans(int id, String col, Object value)
	{
		String sql = "UPDATE cash_transaction SET %s = ? WHERE cash_trans_id = ?";
		sql = String.format(sql, col);
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setObject(1, value);
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
	
	public boolean deleteCashTrans(int id)
	{
		String sql = "DELETE FROM cash_transaction WHERE cash_trans_id = " + id;
		try{
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	// ================================== revenue and expense functions ==========================================
	
	public Vector<Object[]> searchAccount(String word, int type, int page)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM account WHERE 1 ";
		if(type != 0){
			sql = sql.concat(" AND account_type = " + type);
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND account_name LIKE '%%%s%%' ", word));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY account_id DESC LIMIT %d, %d", start, LIMIT));
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int ac = results.getInt("account_type");
				Object row [] = {
						results.getInt("account_id"),
						results.getString("account_name"),
						Dic.w(ac == 1 ? "revenue" : "expense"),
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
	
	public boolean insertAccount(String name, int type)
	{
		String sql = "INSERT INTO account (account_name , account_type) VALUES ('%s','%d')";
		try {
			return statement.executeUpdate(String.format(sql, name, type)) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean editAccount(String name, int id)
	{
		String sql = "UPDATE account SET account_name = '%s' WHERE account_id = %d";
		try {
			return statement.executeUpdate(String.format(sql, name, id)) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public Vector<Option> accountOptions(int type)
	{
		Vector<Option> options = new Vector<>();
		String sql = "SELECT * FROM account WHERE account_type = %d ORDER BY account_id";
		sql = String.format(sql, type);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				int id = results.getInt("account_id");
				String name = results.getString("account_name");
				Option option = new Option(id, name);
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
	
	public boolean saveAccountTrans(int id, int cash, int val, Date date, String desc)
	{
		String sql = "INSERT INTO account_transaction(ac_trans_ac_id, ac_trans_cash_id, ac_trans_value, "
				+ "ac_trans_date, ac_trans_desc, ac_trans_period) VALUES (?,?,?,?,?,?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, cash);
			pst.setInt(i++, val);
			pst.setObject(i++, date);
			pst.setString(i++, desc);
			pst.setInt(i++, FINAN_PRD);
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
	
	public Vector<Object[]> accountTransactions(Date from, Date to, int type, int page)
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM account_transaction JOIN account ON ac_trans_ac_id = account_id "
				+ "JOIN cash ON ac_trans_cash_id = cash_id WHERE 1 AND ac_trans_date BETWEEN ? AND ? ";
		if(type != 0){
			sql = sql.concat(" AND account_type = " + type);
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY ac_trans_date DESC LIMIT %d, %d", start, LIMIT));
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setObject(1, from);
			pst.setObject(2, to);
			results = pst.executeQuery();
			
			while(results.next())
			{
				int t = results.getInt("account_type");
				Object row [] = {
						results.getInt("ac_trans_id"),
						results.getInt("ac_trans_value"),
						df.format(results.getDate("ac_trans_date")),
						results.getString("account_name"),
						results.getString("cash_name"),
						Dic.w(t == 1 ? "revenue" : "expense"),
						results.getString("ac_trans_desc"),
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
	
	public boolean editAccountTrans(String col, Object val, int id)
	{
		String sql = "UPDATE account_transaction SET %s = ? WHERE ac_trans_id = ?";
		sql = String.format(sql, col);
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setObject(1, val);
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
	
	public boolean deleteAccountTrans(int id)
	{
		String sql = "DELETE FROM account_transaction WHERE ac_trans_id = " + id;
		try{
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	// ============================================ student cost functions =============================================
	
	public Vector<Object []> studentCosts(int year, int grade)
	{
		Vector<Object []> rows = new Vector<>();
		
		String sql = "SELECT cost_id , cost_name , IFNULL(price_amount,0) AS amount FROM student_cost "
				+ "LEFT JOIN price ON cost_id = price_cost_id AND price_year = %d AND price_grade = %d ORDER BY cost_id ASC";
		
		sql = String.format(sql, year, grade);
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("cost_id"),
						results.getString("cost_name"),
						results.getString("amount"),
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
	
	public Vector<Object []> stCostCalc(int cid, int year, int month, int cost, String word)
	{
		String sql = "SELECT st_id , st_code , st_name , st_fname , st_lname , IFNULL(cost_assign_id,0) AS cost_assigned"
				+ " FROM enrolment JOIN students ON enrol_st_id = st_id "
				+ " LEFT JOIN cost_assign ON st_id = cost_st_id AND cost_year = %d AND cost_month = %d AND cost_code = %d "
				+ " WHERE 1 AND enrol_course_id = %d ";
		
		sql = String.format(sql, year, month, cost, cid);
		
		if(word.trim().length() > 0){
			String sqlFilter = String.format(" AND (st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%')", word, word, word);
			sql = sql.concat(sqlFilter);
		}
		Vector<Object []> rows = new Vector<Object[]>();
		
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("st_id"),
						results.getString("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_lname"),
						results.getBoolean("cost_assigned"),
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
	
	public Vector<Object []> studentsPayable(int cid, int year, int month, int grade, String word)
	{
		Vector<Object []> rows = new Vector<>();
		String str0 = "SELECT st_id, st_code, st_name, st_fname, st_lname, IFNULL(SUM(price_amount), 0) AS payable "
				+ " FROM enrolment JOIN students ON enrol_st_id = st_id "
				+ " LEFT JOIN cost_assign ON st_id = cost_st_id AND cost_year = %d AND cost_grade = %d ";
		
		StringBuffer buf = new StringBuffer(String.format(str0, year, grade));
		if(month != 0){
			buf.append(" AND cost_month = " + month);
		}
		String str1 = " LEFT JOIN price ON cost_code = price_cost_id AND price_year = cost_year AND price_grade = cost_grade "
				+ " WHERE 1 AND enrol_course_id = " + cid;
		buf.append(str1);
		
		if(word.length() > 0){
			String str2 = " AND (st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%') ";
			buf.append(str2);
		}
		buf.append(" GROUP BY st_id ORDER BY st_id ");
		String sql = buf.toString();
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("st_id"),
						results.getString("st_code"),
						results.getString("st_name"),
						results.getString("st_fname"),
						results.getString("st_lname"),
						results.getInt("payable"),
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
	
	public boolean insertCost(String name)
	{
		String sql = "INSERT INTO student_cost (cost_name) VALUES ('%s')";
		try{
			return statement.executeUpdate(String.format(sql, name)) == 1;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean editCost(String name, int id)
	{
		String q = "UPDATE student_cost SET cost_name = '%s' WHERE cost_id = %d";
		try{
			return (statement.executeUpdate(String.format(q, name, id)) == 1);
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean savePrice(int id, int year, int grade, String amount)
	{
		String sql = "REPLACE INTO price (`price_cost_id`,`price_year`,`price_grade`,`price_amount`) VALUES (?, ?, ?, ?)";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, id);
			pst.setInt(2, year);
			pst.setInt(3, grade);
			pst.setString(4, amount);
			
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public int stGradeAtYear(int id, int year)
	{
		String sql = "SELECT course_grade FROM course, enrolment WHERE 1 "
				+ " AND course_id = enrol_course_id AND enrol_st_id = %d AND course_year = %d LIMIT 1";
		sql = String.format(sql, id, year);
		try
		{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("course_grade");
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
	
	public boolean addCostForStudent(int sid, int year, int month, int cost, int grade)
	{
		String sql = "REPLACE INTO cost_assign (`cost_st_id`,`cost_year`,`cost_month`,`cost_code`, `cost_grade`) VALUES (?, ?, ?, ?, ?)";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, sid);
			pst.setInt(2, year);
			pst.setInt(3, month);
			pst.setInt(4, cost);
			pst.setInt(5, grade);
			
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
	
	public boolean removeCostFromStudent(int sid, int year, int month, int cost)
	{
		String sql = "DELETE FROM cost_assign WHERE `cost_st_id` = ? AND `cost_year` = ? AND `cost_month` = ? AND `cost_code` = ? ";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, sid);
			pst.setInt(2, year);
			pst.setInt(3, month);
			pst.setInt(4, cost);
			
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
	
	public int stTotalCost(int sid, int year, int month, int grade)
	{
		String sql = "SELECT SUM(price_amount) AS total FROM cost_assign , price "
				+ " WHERE cost_code = price_cost_id AND cost_year = price_year AND price_grade = %d "
				+ " AND cost_st_id = %d AND cost_year = %d ";
		sql = String.format(sql, grade, sid, year);
		
		if(month != 0){
			sql = sql.concat(" AND cost_month = " + month);
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
	
	public int stTotalDiscount(int id, int year, int month)
	{
		String sql = "SELECT SUM(discount_amount) total FROM discount WHERE 1 AND discount_st_id = %d AND discount_year = %d ";
		sql = String.format(sql, id, year);
		if(month != 0){
			sql = sql.concat(" AND discount_month = " + month);
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
	
	public int stTotalPayed(int id, int year, int month)
	{
		String sql = "SELECT SUM(pay_value) AS total FROM student_payment WHERE pay_st_id = %d AND pay_year = %d ";
		sql = String.format(sql, id, year);
		if(month != 0){
			sql = sql.concat(" AND pmonth(pay_date) = " + month);
		}
		try{
			ResultSet resp = statement.executeQuery(sql);
			if(resp.next()){
				return resp.getInt("total");
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
	
	public Vector<Object []> studentCostsList(int sid, int year, int month, int grade)
	{
		String sql = "SELECT cost_id, cost_name, price_amount, IFNULL(cost_assign_id, 0) AS cost_assign FROM student_cost "
				+ " LEFT JOIN cost_assign ON cost_id = cost_code AND cost_st_id = %d AND cost_year = %d AND cost_month = %d "
				+ " LEFT JOIN price ON cost_id = price_cost_id AND price_year = %d AND price_grade = %d";
		sql = String.format(sql, sid, year, month, year, grade);
		
		Vector<Object []> rows = new Vector<>();
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("cost_id"),
						results.getString("cost_name"),
						results.getObject("price_amount"),
						results.getBoolean("cost_assign"),
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
	
	public boolean saveStudentPayment(int id, int val, int cash, int year, Date date, String desc)
	{
		String sql = "INSERT INTO student_payment (pay_st_id, pay_cash_id, pay_value, pay_year, pay_date, pay_desc, pay_period_id ) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, cash);
			pst.setInt(i++, val);
			pst.setInt(i++, year);
			pst.setDate(i++, date != null ? new java.sql.Date(date.getTime()) : null);
			pst.setString(i++, desc);
			pst.setInt(i++, FINAN_PRD);
			
			return pst.executeUpdate() > 0;
		}
		catch(SQLException e){
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	ULocale locale = new ULocale("@calendar=persian");
	Calendar cal = Calendar.getInstance(locale);
	DateFormat df = new SimpleDateFormat("yyyy/MM/dd", locale);
	
	public Vector<Object []> studentPayments(int id, Date sDate, Date eDate)
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "SELECT pay_id , pay_value , pay_date AS pay_date , cash_name, pay_desc FROM student_payment , cash "
				+ " WHERE 1 AND pay_cash_id = cash_id AND pay_st_id = ? AND pay_date BETWEEN ? AND ? ORDER BY pay_date DESC";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, id);
			pst.setDate(2, sDate != null ? new java.sql.Date(sDate.getTime()) : null);
			pst.setDate(3, eDate != null ? new java.sql.Date(eDate.getTime()) : null);
			
			results = pst.executeQuery();
			
			while(results.next())
			{
				Object row [] = {
						results.getInt("pay_id"),
						results.getInt("pay_value"),
						df.format(results.getDate("pay_date")),
						results.getString("cash_name"),
						results.getString("pay_desc")
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
	
	public boolean editStudPaymentAmount(int val, int id)
	{
		String sql = "UPDATE student_payment SET pay_value = %d WHERE pay_id = %d";
		try{
			return statement.executeUpdate(String.format(sql, val, id)) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public boolean deleteStPayment(int id)
	{
		String sql = "DELETE FROM student_payment WHERE pay_id = " + id;
		try{
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	public Vector<Cost> costTypeList(int year, int grade) 
	{
		String sql = "SELECT cost_id , cost_name , IFNULL(price_amount,0) AS amount FROM student_cost "
				+ "LEFT JOIN price ON cost_id = price_cost_id AND price_year = %d AND price_grade = %d ORDER BY cost_id ASC";
		sql = String.format(sql, year, grade);
		
		Vector<Cost> rows = new Vector<>();
		try{
			results = statement.executeQuery(sql);
			while(results.next()){
				Cost cost = new Cost(results.getInt("cost_id"), results.getInt("amount"), results.getString("cost_name"));
				rows.add(cost);
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

	public Vector<Object[]> studentDiscounts(int sid, int year) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT * FROM discount WHERE discount_st_id = %d AND discount_year = %d ORDER BY discount_month DESC";
		sql = String.format(sql, sid, year);
		
		String months [] = {"", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola", 
				"mizan", "aqrab", "qaws", "jadi", "dalw", "hoot"};
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int m = results.getInt("discount_month");
				Option option = new Option(m, Dic.w(months[m]));
				
				Object [] row = {
						results.getInt("discount_id"),
						results.getInt("discount_amount"),
						option,
						results.getString("discount_desc"),
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

	public boolean insertDiscount(int sid, int year, int month, int amount, String desc) 
	{
		String sql = "INSERT INTO discount (`discount_st_id`,`discount_year`,`discount_month`,`discount_amount`,`discount_desc`) VALUES (?, ?, ?, ?, ?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, sid);
			pst.setInt(2, year);
			pst.setInt(3, month);
			pst.setInt(4, amount);
			pst.setString(5, desc);
			
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

	public boolean editDiscount(int id, int amount, int month, String desc) 
	{
		String sql = "UPDATE discount SET `discount_amount` = ? , `discount_month` = ? , `discount_desc` = ? WHERE `discount_id` = ? ";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, amount);
			pst.setInt(2, month);
			pst.setString(3, desc);
			pst.setInt(4, id);
			
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

	public boolean deleteDiscount(int id) 
	{
		String sql = "DELETE FROM discount where discount_id = " + id;
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

	public Vector<Object[]> costsReport(int year, int month, int grade) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT cost_id, cost_name, COUNT(cost_assign_id) AS num, SUM(price_amount) AS amount FROM student_cost "
				+ " LEFT JOIN cost_assign ON cost_id = cost_code AND cost_year = " + year;
		
		if(month != 0){
			sql = sql.concat(" AND cost_month = " + month);
		}
		if(grade != 0){
			sql = sql.concat(" AND cost_grade = " + grade);
		}
		String append = " LEFT JOIN price ON cost_id = price_cost_id AND price_year = cost_year AND price_grade = cost_grade GROUP BY cost_id ORDER BY cost_id";
		sql = sql.concat(append);
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("cost_id"),
						results.getString("cost_name"),
						results.getInt("num"),
						results.getInt("amount"),
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

	public int totalDiscount(int year, int month, int grade) 
	{
		String sql = "SELECT SUM(discount_amount) AS amount FROM discount ";
		if(grade != 0){
			sql = sql.concat(" JOIN enrolment ON discount_st_id = enrol_st_id JOIN course ON enrol_course_id = course_id AND course_grade = " + grade);
		}
		sql = sql.concat(" WHERE discount_year = " + year);
		if(month != 0){
			sql = sql.concat(" AND discount_month = " + month);
		}
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("amount");
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

	public int totalPayed(int year, int month, int grade) 
	{
		String sql = "SELECT SUM(pay_value) AS amount FROM student_payment ";
		if(grade != 0){
			sql = sql.concat(" JOIN enrolment ON pay_st_id = enrol_st_id JOIN course ON enrol_course_id = course_id AND course_grade = " + grade);
		}
		sql = sql.concat(" WHERE pay_year = " + year);
		if(month != 0){
			sql = sql.concat(" AND pmonth(pay_date) = " + month);
		}
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("amount");
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

	public int totalCost(int year, int month, int grade) 
	{
		String sql = "SELECT SUM(price_amount) AS amount FROM cost_assign , price WHERE 1 AND price_cost_id = cost_code "
				+ " AND price_year = cost_year AND price_grade = cost_grade AND cost_year = " + year;
		
		if(month != 0){
			sql = sql.concat(" AND cost_month = " + month);
		}
		if(grade != 0){
			sql = sql.concat(" AND cost_grade = " + grade);
		}
		try{
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("amount");
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

	public Vector<Object[]> allStudentsPayments(int year, Date from, Date to, int page) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT st_id, st_code, st_name, st_lname, st_fname, cash_id, cash_name, student_payment.* "
				+ " FROM student_payment JOIN students ON pay_st_id = st_id JOIN cash ON pay_cash_id = cash_id "
				+ " WHERE pay_year = ? AND pay_date BETWEEN ? AND ? ORDER BY pay_date DESC LIMIT ?, ?";
		int start = (page * LIMIT) - LIMIT;
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, year);
			pst.setDate(2, from == null ? null : new java.sql.Date(from.getTime()));
			pst.setDate(3, to == null ? null : new java.sql.Date(to.getTime()));
			pst.setInt(4, start);
			pst.setInt(5, LIMIT);
			results = pst.executeQuery();
			
			while(results.next())
			{
				Object row [] = {
						results.getInt("pay_id"),
						results.getInt("st_id"),
						results.getString("st_code"),
						results.getString("st_name"),
						results.getString("st_lname"),
						results.getString("st_fname"),
						results.getInt("pay_value"),
						df.format(results.getDate("pay_date")),
						results.getString("cash_name"),
						results.getString("pay_desc"),
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

	public Vector<Object[]> allStudentsDiscounts(int year, int month, int page) 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		
		String sql = "SELECT st_id, st_code, st_name, st_lname, st_fname, discount.* FROM discount "
				+ " JOIN students ON discount_st_id = st_id WHERE discount_year = " + year;
		if(month != 0){
			sql = sql.concat(" AND discount_month = " + month);
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY discount_month DESC LIMIT %d, %d", start, LIMIT));
		
		String months [] = {"", "hamal", "sawr", "jawza", "saratan", "asad", "sonbola", "mizan", "aqrab", "qaws", "jadi", "dalw", "hoot"};
		try{
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int m = results.getInt("discount_month");
				Object row [] = {
						results.getInt("discount_id"),
						results.getInt("st_id"),
						results.getInt("st_code"),
						results.getString("st_name"),
						results.getString("st_lname"),
						results.getString("st_fname"),
						new Option(m, Dic.w(months[m])),
						results.getInt("discount_amount"),
						results.getString("discount_desc"),
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

	public boolean editStudPaymentDate(int id, Date date) 
	{
		if(date == null) return false;
		String sql = "UPDATE student_payment SET pay_date = ? WHERE pay_id = ?";
		try
		{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setDate(1, new java.sql.Date(date.getTime()));
			pst.setInt(2, id);
			
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

	public boolean editStudPaymentDesc(int id, String value) 
	{
		String sql = "UPDATE student_payment SET pay_desc = '%s' WHERE pay_id = %d";
		sql = String.format(sql, value, id);
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

	public boolean editDiscountAmount(int id, int val) 
	{
		String sql = "UPDATE discount SET discount_amount = %d WHERE discount_id = %d";
		sql = String.format(sql, val, id);
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
	
	public boolean editDiscountDesc(int id, String desc)
	{
		String sql = "UPDATE discount SET discount_desc = '%s' WHERE discount_id = %d";
		sql = String.format(sql, desc, id);
		try {
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

	public boolean editDiscountMonth(int id, int month) 
	{
		String sql = "UPDATE discount SET discount_month = %d WHERE discount_id = %d";
		sql = String.format(sql, month, id);
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
	
	//===================================== employee finance functions =================================================
	
	public boolean saveEmployeeReceipt(int id, int val, int cash, Date date, String descr)
	{
		if(date == null) return false;
		
		String sql = "INSERT INTO employee_receipt (emp_rec_emp_id , emp_rec_cash_id , emp_rec_value , "
				+ " emp_rec_date, emp_rec_desc, emp_rec_period_id ) VALUES (?, ?, ?, ?, ?, ?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			pst.setInt(i++, id);
			pst.setInt(i++, cash);
			pst.setInt(i++, val);
			pst.setDate(i++, new java.sql.Date(date.getTime()));
			pst.setString(i++, descr);
			pst.setInt(i++, FINAN_PRD);
			
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
	
	public Vector<Object []> employeeReceipts(int id, Date from, Date to)
	{
		Vector<Object []> rows = new Vector<>();
		
		String sql = "SELECT emp_rec_id , emp_rec_value , emp_rec_date AS emp_rec_date , cash_name , emp_rec_desc FROM employee_receipt "
				+ " JOIN cash ON cash_id = emp_rec_cash_id WHERE emp_rec_emp_id = ? AND emp_rec_date BETWEEN ? AND ? ORDER BY emp_rec_date DESC";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setInt(1, id);
			pst.setDate(2, from == null ? null : new java.sql.Date(from.getTime()));
			pst.setDate(3, to == null ? null : new java.sql.Date(to.getTime()));
			
			results = pst.executeQuery();
			
			while(results.next())
			{
				Object row [] = {
						results.getInt("emp_rec_id"),
						results.getInt("emp_rec_value"),
						df.format(results.getDate("emp_rec_date")),
						results.getString("cash_name"),
						results.getString("emp_rec_desc"),
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

	public Vector<Object []> employeeTransactions(Date from, Date to, int page)
	{
		Vector<Object []> rows = new Vector<>();
		
		String sql = "SELECT emp_rec_id, emp_id, emp_name, emp_lname, emp_rec_value, cash_name, emp_rec_date AS emp_rec_date, emp_rec_desc FROM employee, employee_receipt, cash "
				+ " WHERE emp_id = emp_rec_emp_id AND cash_id = emp_rec_cash_id AND emp_rec_date BETWEEN ? AND ? ORDER BY emp_rec_date DESC LIMIT ?, ?";
		int start = (page * LIMIT) - LIMIT;
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setDate(1, from == null ? null : new java.sql.Date(from.getTime()));
			pst.setDate(2, to == null ? null : new java.sql.Date(to.getTime()));
			pst.setInt(3, start);
			pst.setInt(4, LIMIT);
			
			results = pst.executeQuery();
			while(results.next())
			{
				Object row [] = {
						results.getInt("emp_rec_id"),
						results.getInt("emp_id"),
						results.getString("emp_name"),
						results.getString("emp_lname"),
						results.getInt("emp_rec_value"),
						df.format(results.getDate("emp_rec_date")),
						results.getString("cash_name"),
						results.getString("emp_rec_desc"),
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
	
	public boolean editEmpReceiptAmount(int val, int id)
	{
		String sql = "UPDATE employee_receipt SET emp_rec_value = %d WHERE emp_rec_id = %d";
		sql = String.format(sql, val, id);
		try {
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
	
	public boolean editEmpReceiptDesc(int id, String desc)
	{
		String sql = "UPDATE employee_receipt SET emp_rec_desc = '%s' WHERE emp_rec_id = %d";
		sql = String.format(sql, desc, id);
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
	
	public boolean editEmpReceiptDate(int id, Date date)
	{
		if(date == null) return false;
		String sql = "UPDATE employee_receipt SET emp_rec_date = ? WHERE emp_rec_id = ?";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setDate(1, new java.sql.Date(date.getTime()));
			pst.setInt(2, id);
			
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
	
	public boolean deleteEmpReceipt(int id)
	{
		String sql = "DELETE FROM employee_receipt WHERE emp_rec_id = " + id;
		try{
			return statement.executeUpdate(sql) == 1;
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}
	
	//============================== financial period report functions ==========================

	public int totalStudFees(int cash) 
	{
		String sql = "SELECT SUM(pay_value) AS payed FROM student_payment WHERE pay_period_id = " + FINAN_PRD;
		if(cash != 0){
			sql = sql.concat(" AND pay_cash_id = " + cash);
		}
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("payed");
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

	public int totalAccountTrans(int cash, int type) 
	{
		String sql = "SELECT SUM(ac_trans_value) AS total FROM account_transaction JOIN account "
				+ " ON ac_trans_ac_id = account_id WHERE ac_trans_period = %d AND account_type = %d ";
		sql = String.format(sql, FINAN_PRD, type);
		if(cash != 0){
			sql = sql.concat(" AND ac_trans_cash_id = " + cash);
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

	public int totalCashTrans(int cash, int type) 
	{
		String sql = "SELECT SUM(cash_trans_value) AS total FROM cash_transaction WHERE cash_trans_period = %d AND cash_trans_type = %d ";
		sql = String.format(sql, FINAN_PRD, type);
		if(cash != 0){
			sql = sql.concat(" AND cash_trans_cash_id = " + cash);
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
	
	public int totalEmpSalary(int cash)
	{
		String sql = "SELECT SUM(emp_rec_value) AS total FROM employee_receipt WHERE emp_rec_period_id = " + FINAN_PRD;
		if(cash != 0){
			sql = sql.concat(" AND emp_rec_cash_id = " + cash);
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
	
}
















