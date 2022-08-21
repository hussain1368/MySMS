package com.kabulbits.shoqa.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import com.kabulbits.shoqa.util.App;
import com.kabulbits.shoqa.util.Diags;

public class LibraryData extends Data {

	public boolean insertMember(Object[] info) 
	{
		String sql = "INSERT INTO library_member (`mem_name`,`mem_lname`,`mem_fname`,`mem_phone`,"
				+ "`mem_job`,`mem_introducer`,`mem_introducer_job`,`mem_image`) VALUES (?,?,?,?,?,?,?,?)";
		try{
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			for(; i<info.length; i++){
				pst.setString(i, info[i].toString());
			}
			pst.setObject(i++, info[0]);
			
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

	public boolean editMember(int id, Object[] info) 
	{
		String sql = "UPDATE library_member SET `mem_name` = ?, `mem_lname` = ?, `mem_fname` = ?, `mem_phone` = ?,"
				+ "`mem_job` = ?, `mem_introducer` = ?, `mem_introducer_job` = ?, `mem_image` = ? WHERE mem_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			for(; i<info.length; i++){
				pst.setString(i, info[i].toString());
			}
			pst.setObject(i++, info[0]);
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

	public Object[] findMember(int id) 
	{
		String sql = "select * from library_member where mem_id = " + id;
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				Object info [] = new Object[8];
				int i = 0;
				info[i++] = results.getBytes("mem_image");
				info[i++] = results.getString("mem_name");
				info[i++] = results.getString("mem_lname");
				info[i++] = results.getString("mem_fname");
				info[i++] = results.getString("mem_phone");
				info[i++] = results.getString("mem_job");
				info[i++] = results.getString("mem_introducer");
				info[i++] = results.getString("mem_introducer_job");
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

	public Vector<Object[]> bookCategories() 
	{
		Vector<Object []> rows = new Vector<Object[]>();
		String sql = "SELECT * FROM book_category ORDER BY cat_id ASC";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("cat_id"),
						results.getString("cat_name"),
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

	public boolean saveCategory(Object id, String name) 
	{
		String sql1 = "INSERT INTO book_category (cat_name) VALUES ('%s')";
		String sql2 = "UPDATE book_category SET cat_name = '%s' WHERE cat_id = %d";
		try {
			if(id == null){
				return statement.executeUpdate(String.format(sql1, name)) == 1;
			}else{
				return statement.executeUpdate(String.format(sql2, name, (int) id)) == 1;
			}
		} 
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public boolean deleteCategory(int id) 
	{
		String sql = "delete from book_category where cat_id = " + id;
		try {
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

	public Vector<Option> categoryOptions() 
	{
		Vector<Option> options = new Vector<>();
		options.add(new Option(0, "---"));
		
		String sql = "SELECT * FROM book_category ORDER BY cat_id ASC";
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Option option = new Option(results.getInt("cat_id"), results.getString("cat_name"));
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

	public int maxBookRegNo() 
	{
		String sql = "SELECT MAX(book_reg_no) AS reg_no FROM book";
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getInt("reg_no") + 1;
			}
		}
		catch(SQLException e){
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return 1;
	}

	public boolean insertBook(Object[] values) 
	{
		String sql = "INSERT INTO book (`book_reg_no`,`book_cat_id`,`book_title`,`book_author`,`book_translator`,`book_publisher`,"
				+ "`book_publish_period`,`book_publish_place`,`book_price`,`book_isbn`,`book_ddc`,`book_subject`,`book_publish_date`) "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			for(Object val : values){
				pst.setObject(i++, val);
			}
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("duplicate_book_reg_no");
			}else{
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public boolean insertCover(int book, String name) 
	{
		String sql = "INSERT INTO book_cover (`cover_book_id`,`cover_name`) VALUES (%d, '%s')";
		sql = String.format(sql, book, name);
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

	public Object[] findBook(int id) 
	{
		String sql = "select * from book where book_id = " + id;
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				Object info [] = {
						results.getInt("book_reg_no"),
						results.getInt("book_cat_id"),
						results.getString("book_title"),
						results.getString("book_author"),
						results.getString("book_translator"),
						results.getString("book_publisher"),
						results.getString("book_publish_period"),
						results.getString("book_publish_place"),
						results.getString("book_price"),
						results.getString("book_isbn"),
						results.getString("book_ddc"),
						results.getString("book_subject"),
						results.getDate("book_publish_date"),
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

	public Vector<Object[]> bookCovers(int id) 
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "SELECT * FROM book_cover WHERE cover_book_id = " + id;
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Option option = new Option(results.getInt("cover_id"), results.getString("cover_name"));
				rows.add(new Object []{option});
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

	public boolean editBook(int id, Object[] values) 
	{
		String sql = "UPDATE book SET `book_reg_no` = ?, `book_cat_id` = ?, `book_title` = ?, `book_author` = ?,"
				+ "`book_translator` = ?, `book_publisher` = ?, `book_publish_period` = ?, `book_publish_place` = ?,"
				+ "`book_price` = ?, `book_isbn` = ?, `book_ddc` = ?, `book_subject` = ?, `book_publish_date` = ? WHERE book_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			int i = 1;
			for(Object val : values){
				pst.setObject(i++, val);
			}
			pst.setInt(i++, id);
			
			return pst.executeUpdate() == 1;
		}
		catch(SQLException e){
			if(e.getErrorCode() == 1062){
				Diags.showErrLang("duplicate_book_reg_no");
			}else{
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}

	public boolean deleteCover(int id) 
	{
		String sql = "DELETE FROM book_cover WHERE cover_id = " + id;
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

	public boolean editCover(int id, String name) 
	{
		String sql = "UPDATE book_cover SET cover_name = '%s' WHERE cover_id = %d";
		sql = String.format(sql, name, id);
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

	public int countBook(String word, String col, int cat) 
	{
		String sql = "SELECT COUNT(book_id) AS num FROM book WHERE 1 ";
		if(cat != 0){
			sql = sql.concat(" AND book_cat_id = " + cat);
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%'", col, word));
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

	public Vector<Object[]> searchBooks(String word, String col, int cat, int page) 
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "SELECT book.*, COUNT(cover_id) AS covers, COUNT(IF(cover_exists=1,1,NULL)) AS available "
				+ " FROM book LEFT JOIN book_cover ON book_id = cover_book_id WHERE 1 ";
		if(cat != 0){
			sql = sql.concat(" AND book_cat_id = " + cat);
		}
		if(word.length() > 0){
			sql = sql.concat(String.format(" AND %s LIKE '%%%s%%'", col, word));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" GROUP BY book_id ORDER BY book_id DESC LIMIT %d, %d", start, LIMIT));
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				Object row [] = {
						results.getInt("book_id"),
						results.getInt("book_reg_no"),
						results.getString("book_title"),
						results.getString("book_author"),
						results.getString("book_translator"),
						results.getString("book_publisher"),
						results.getString("book_isbn"),
						results.getInt("covers"),
						results.getInt("available"),
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

	public boolean deleteBook(int id) 
	{
		String sql = "DELETE FROM book WHERE book_id = " + id;
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

	public int countMembers(String term) 
	{
		String sql = "SELECT COUNT(mem_id) AS num FROM library_member WHERE 1 ";
		if(term.length() > 0){
			String append = " AND (mem_id LIKE '%%%s%%' OR mem_name LIKE '%%%s%%' OR mem_lname LIKE '%%%s%%')";
			sql = sql.concat(String.format(append, term, term, term));
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

	public Vector<Object[]> searchMembers(String term, int page) 
	{
		Vector<Object []> rows = new Vector<>();
		String sql = "SELECT * FROM library_member WHERE 1 ";
		if(term.length() > 0){
			String append = " AND (mem_id LIKE '%%%s%%' OR mem_name LIKE '%%%s%%' OR mem_lname LIKE '%%%s%%')";
			sql = sql.concat(String.format(append, term, term, term));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY mem_id DESC LIMIT %d, %d", start, LIMIT));
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("mem_id"),
						results.getString("mem_name"),
						results.getString("mem_lname"),
						results.getString("mem_fname"),
						results.getString("mem_phone"),
						results.getString("mem_job"),
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

	public boolean deleteMember(int id) 
	{
		String sql = "DELETE FROM library_member WHERE mem_id = " + id;
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

	public Vector<Object[]> studentList(String term) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT st_id, st_code, st_name, st_lname, st_fname FROM students WHERE 1 ";
		if(term.length() > 0){
			String str = " AND (st_id LIKE '%%%s%%' OR st_code LIKE '%%%s%%' OR st_name LIKE '%%%s%%')";
			sql = sql.concat(String.format(str, term, term, term));
		}
		sql = sql.concat(" ORDER BY st_id DESC LIMIT 20");
		try {
			 results = statement.executeQuery(sql);
			 while(results.next()){
				 Object [] row = {
						 results.getInt("st_id"),
						 results.getString("st_name"),
						 results.getString("st_lname"),
						 results.getString("st_fname"),
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

	public Vector<Object[]> employeeList(String term) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT emp_id, emp_name, emp_lname, emp_fname FROM employee WHERE 1 ";
		if(term.length() > 0){
			String str = " AND (emp_id LIKE '%%%s%%' OR emp_name LIKE '%%%s%%' OR emp_lname LIKE '%%%s%%')";
			sql = sql.concat(String.format(str, term, term, term));
		}
		sql = sql.concat(" ORDER BY emp_id DESC LIMIT 20");
		try {
			 results = statement.executeQuery(sql);
			 while(results.next()){
				 Object [] row = {
						 results.getInt("emp_id"),
						 results.getString("emp_name"),
						 results.getString("emp_lname"),
						 results.getString("emp_fname"),
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

	public Vector<Object[]> memberList(String term) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT mem_id, mem_name, mem_lname, mem_fname FROM library_member WHERE 1 ";
		if(term.length() > 0){
			String str = " AND (mem_id LIKE '%%%s%%' OR mem_name LIKE '%%%s%%' OR mem_lname LIKE '%%%s%%')";
			sql = sql.concat(String.format(str, term, term, term));
		}
		sql = sql.concat(" ORDER BY mem_id DESC LIMIT 20");
		try {
			 results = statement.executeQuery(sql);
			 while(results.next()){
				 Object [] row = {
						 results.getInt("mem_id"),
						 results.getString("mem_name"),
						 results.getString("mem_lname"),
						 results.getString("mem_fname"),
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

	public Vector<Object[]> bookList(int cat, String term) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT cover_id, cover_name, book_id, book_reg_no, book_title "
				+ " FROM book_cover JOIN book ON cover_book_id = book_id WHERE cover_exists = 1 ";
		if(cat != 0){
			sql = sql.concat(" AND book_cat_id = " + cat);
		}
		if(term.length() > 0){
			String str = " AND (book_reg_no LIKE '%%%s%%' OR book_title LIKE '%%%s%%')";
			sql = sql.concat(String.format(str, term, term));
		}
		sql = sql.concat(" ORDER BY cover_id DESC LIMIT 20");
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("cover_id"),
						results.getString("cover_name"),
//						results.getInt("book_id"),
						results.getInt("book_reg_no"),
						results.getString("book_title"),
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

	public boolean borrowExists(int id, int type) 
	{
		String col = type == 0 ? "borrow_st_id" : type == 1 ? "borrow_emp_id" : "borrow_mem_id";
		String sql = "SELECT borrow_id FROM book_borrow WHERE borrow_returned = 0 AND %s = %d";
		sql = String.format(sql, col, id);
		try {
			results = statement.executeQuery(sql);
			if(results.next()){
				return results.getBoolean("borrow_id");
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

	public boolean insertBorrow(int bid, int pid, int type, Date date, int dur) 
	{
		String col = type == 0 ? "borrow_st_id" : type == 1 ? "borrow_emp_id" : "borrow_mem_id";
		String sql1 = "INSERT INTO book_borrow (`borrow_cover_id`,`%s`,`borrow_date`,`borrow_duration`) VALUES (?,?,?,?)";
		sql1 = String.format(sql1, col);
		String sql2 = "UPDATE book_cover SET cover_exists = 0 WHERE cover_id = " + bid;
		try{
			PreparedStatement pst = connection.prepareStatement(sql1);
			int i = 1;
			pst.setInt(i++, bid);
			pst.setInt(i++, pid);
			pst.setObject(i++, date);
			pst.setInt(i++, dur);
			
			connection.setAutoCommit(false);
			pst.executeUpdate();
			statement.executeUpdate(sql2);
			
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
				return false;
			}
			catch(SQLException e1){
				if(App.LOG){
					logger.error(e1.getMessage(), e1);
				}
			}
		}
		return true;
	}
	
	private ULocale locale = new ULocale("@calendar=persian");
	private Calendar cal = Calendar.getInstance(locale);
	private DateFormat df = new SimpleDateFormat("yyyy/MM/dd", locale);
	private java.text.DateFormat gdf = new java.text.SimpleDateFormat("yyyy/MM/dd");

	public Vector<Object[]> searchBorrow(int ptype, int state, int page, 
			Date deliverFrom, Date deliverTo, Date deadFrom, Date deadTo, Date returnFrom, Date returnTo) 
	{
		Vector<Object[]> rows = new Vector<>();
		
		String sql = "SELECT book_borrow.* , borrow_duration + IFNULL(SUM(ext_duration),0) AS duration, cover_name, "
				+ " book_id, book_title, book_reg_no, st_name, st_lname, emp_name, emp_lname, mem_name, mem_lname "
				+ " FROM book_borrow JOIN book_cover ON borrow_cover_id = cover_id "
				+ " JOIN book ON cover_book_id = book_id "
				+ " LEFT JOIN borrow_extension ON borrow_id = ext_borrow_id "
				+ " LEFT JOIN students ON borrow_st_id = st_id "
				+ " LEFT JOIN employee ON borrow_emp_id = emp_id "
				+ " LEFT JOIN library_member ON borrow_mem_id = mem_id WHERE 1 ";
		
		if(state != -1){
			sql = sql.concat(" AND borrow_returned = " + state);
		}
		if(ptype == 1){
			sql = sql.concat(" AND borrow_st_id IS NOT NULL ");
		}
		else if(ptype == 2){
			sql = sql.concat(" AND borrow_emp_id IS NOT NULL ");
		}
		else if(ptype == 3){
			sql = sql.concat(" AND borrow_mem_id IS NOT NULL ");
		}
		
		if (deliverFrom != null && deliverTo != null){
			String str = " AND borrow_date BETWEEN '%s' AND '%s' ";
			sql = sql.concat(String.format(str, gdf.format(deliverFrom), gdf.format(deliverTo)));
		}
		if (returnFrom != null && returnTo != null){
			String str = " AND borrow_return_date BETWEEN '%s' AND '%s'";
			sql = sql.concat(String.format(str, gdf.format(returnFrom), gdf.format(returnTo)));
		}
		sql = sql.concat(" GROUP BY borrow_id ");
		
		if (deadFrom != null && deadTo != null){
			String str = " HAVING DATE_ADD(borrow_date, INTERVAL duration DAY) BETWEEN '%s' AND '%s' ";
			sql = sql.concat(String.format(str, gdf.format(deadFrom), gdf.format(deadTo)));
		}
		int start = (page * LIMIT) - LIMIT;
		sql = sql.concat(String.format(" ORDER BY borrow_date DESC LIMIT %d, %d", start, LIMIT));
		
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int stid = results.getInt("borrow_st_id");
				int empid = results.getInt("borrow_emp_id");
				int memid = results.getInt("borrow_mem_id");
				
				int pty = stid != 0 ? 1 : empid != 0 ? 2 : 3;
				int pid = stid != 0 ? stid : empid != 0 ? empid : memid;
				String pname = "", plname = "";
				if(stid != 0){
					pname = results.getString("st_name");
					plname = results.getString("st_lname");
				}
				else if(empid != 0){
					pname = results.getString("emp_name");
					plname = results.getString("emp_lname");
				}
				else if(memid != 0){
					pname = results.getString("mem_name");
					plname = results.getString("mem_lname");
				}
				int days = results.getInt("duration");
				Date deliver = results.getDate("borrow_date");
				cal.setTime(deliver);
				cal.add(Calendar.DATE, days);
				Date deadline = cal.getTime();
				Date returnDate = results.getDate("borrow_return_date");
				
				Option person = new Option(pid, pty, pname);
				Option cover = new Option(results.getInt("borrow_cover_id"), results.getString("cover_name"));
				
				Object row[] = {
						results.getInt("borrow_id"),
						results.getInt("book_id"),
						pid,
						person,
						plname,
						results.getString("book_title"),
						results.getString("book_reg_no"),
						cover,
						df.format(deliver),
						results.getInt("duration"),
						df.format(deadline),
						returnDate != null ? df.format(returnDate) : null,
						results.getBoolean("borrow_returned"),
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

	public boolean saveExtension(Object id, int borrow, int dur) 
	{
		String sql1 = "INSERT INTO borrow_extension (`ext_borrow_id`,`ext_duration`) VALUES (%d, %d)";
		String sql2 = "UPDATE borrow_extension SET ext_duration = %d WHERE ext_id = %d";
		try {
			if(id == null){
				return statement.executeUpdate(String.format(sql1, borrow, dur)) == 1;
			}else{
				return statement.executeUpdate(String.format(sql2, dur, (int) id)) == 1;
			}
		} 
		catch (SQLException e) {
			Diags.showErr(Diags.ERROR);
			if(App.LOG){
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public Vector<Object[]> borrowExtensions(int id) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT * FROM borrow_extension WHERE ext_borrow_id = " + id;
		try {
			results = statement.executeQuery(sql);
			while(results.next()){
				Object row [] = {
						results.getInt("ext_id"),
						results.getInt("ext_duration"),
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

	public boolean deleteExtension(int val) 
	{ 
		String sql = "DELETE FROM borrow_extension WHERE ext_id = " + val;
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

	public boolean borrowReturn(int id, int cover, boolean returned, Date date) 
	{
		String sql1 = "UPDATE book_borrow SET borrow_returned = ?, borrow_return_date = ? WHERE borrow_id = ?";
		String sql2 = "UPDATE book_cover SET cover_exists = ? WHERE cover_id = ?";
		try {
			PreparedStatement pst1 = connection.prepareStatement(sql1);
			pst1.setBoolean(1, returned);
			pst1.setDate(2, date == null ? null : new java.sql.Date(date.getTime()));
			pst1.setInt(3, id);
			
			PreparedStatement pst2 = connection.prepareStatement(sql2);
			pst2.setBoolean(1, returned);
			pst2.setInt(2, cover);
			
			connection.setAutoCommit(false);
			pst1.executeUpdate();
			pst2.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
		}
		catch(SQLException e){
			try{
				connection.rollback();
				connection.setAutoCommit(true);
			}
			catch(SQLException e1){
				Diags.showErr(Diags.ERROR);
				if(App.LOG){
					logger.error(e.getMessage(), e);
				}
			}
			return false;
		}
		return true;
	}

	public boolean editBorrowDate(int id, Date date) 
	{
		String sql = "UPDATE book_borrow SET borrow_date = ? WHERE borrow_id = ?";
		try {
			PreparedStatement pst = connection.prepareStatement(sql);
			pst.setObject(1, date);
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

	public Vector<Object[]> personBorrows(int id, int type, boolean unreturend, Date from, Date to) 
	{
		Vector<Object[]> rows = new Vector<>();
		String col = type == 1 ? "borrow_st_id" : type == 2 ? "borrow_emp_id" : "borrow_mem_id";
		
		String sql = "SELECT book_borrow.* , borrow_duration + IFNULL(SUM(ext_duration),0) AS duration, "
				+ " cover_name, book_id, book_title, book_reg_no "
				+ " FROM book_borrow JOIN book_cover ON borrow_cover_id = cover_id "
				+ " JOIN book ON cover_book_id = book_id "
				+ " LEFT JOIN borrow_extension ON borrow_id = ext_borrow_id WHERE %s = %d ";

		sql = String.format(sql, col, id);
		if(unreturend){
			sql = sql.concat(" AND borrow_returned = 0 ");
		}
		if(from != null && to != null){
			sql = sql.concat(String.format(" AND borrow_date BETWEEN '%s' AND '%s' ", gdf.format(from), gdf.format(to)));
		}
		sql = sql.concat(" GROUP BY borrow_id ORDER BY borrow_date DESC LIMIT 50");
		
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int days = results.getInt("duration");
				Date deliver = results.getDate("borrow_date");
				cal.setTime(deliver);
				cal.add(Calendar.DATE, days);
				Date deadline = cal.getTime();
				Date returnDate = results.getDate("borrow_return_date");
				
				Option cover = new Option(results.getInt("borrow_cover_id"), results.getString("cover_name"));
				
				Object row[] = {
					results.getInt("borrow_id"),
					results.getString("book_title"),
					results.getInt("book_reg_no"),
					cover,
					df.format(deliver),
					days,
					df.format(deadline),
					returnDate != null ? df.format(returnDate) : null,
					results.getBoolean("borrow_returned"),
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

	public boolean deleteBorrow(int id, int cover) 
	{
		String sql1 = "DELETE FROM book_borrow WHERE borrow_id = " + id;
		String sql2 = "UPDATE book_cover SET cover_exists = 1 WHERE cover_id = " + cover;
		try {
			connection.setAutoCommit(false);
			statement.executeUpdate(sql1);
			if(cover != 0){
				statement.executeUpdate(sql2);
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
			catch(SQLException e1){
				if(App.LOG){
					logger.error(e1.getMessage(), e1);
				}
			}
			return false;
		}
		return true;
	}

	public Vector<Object[]> bookBorrows(int id, boolean unreturend, Date from, Date to) 
	{
		Vector<Object[]> rows = new Vector<>();
		String sql = "SELECT book_borrow.* , borrow_duration + IFNULL(SUM(ext_duration),0) AS duration,"
				+ " cover_name, st_name, st_lname, emp_name, emp_lname, mem_name, mem_lname "
				+ " FROM book_borrow JOIN book_cover ON borrow_cover_id = cover_id "
				+ " LEFT JOIN borrow_extension ON borrow_id = ext_borrow_id "
				+ " LEFT JOIN students ON borrow_st_id = st_id "
				+ " LEFT JOIN employee ON borrow_emp_id = emp_id "
				+ " LEFT JOIN library_member ON borrow_mem_id = mem_id WHERE cover_book_id = " + id;
		
		if(unreturend){
			sql = sql.concat(" AND borrow_returned = 0 ");
		}
		if(from != null && to != null){
			sql = sql.concat(String.format(" AND borrow_date BETWEEN '%s' AND '%s' ", gdf.format(from), gdf.format(to)));
		}
		sql = sql.concat(" GROUP BY borrow_id ORDER BY borrow_date DESC LIMIT 50");
		
		try {
			results = statement.executeQuery(sql);
			while(results.next())
			{
				int days = results.getInt("duration");
				Date deliver = results.getDate("borrow_date");
				cal.setTime(deliver);
				cal.add(Calendar.DATE, days);
				Date deadline = cal.getTime();
				Date returnDate = results.getDate("borrow_return_date");
				
				int stid = results.getInt("borrow_st_id");
				int empid = results.getInt("borrow_emp_id");
				int memid = results.getInt("borrow_mem_id");
				
				int pid = stid != 0 ? stid : empid != 0 ? empid : memid;
				String pname = "", plname = "";
				if(stid != 0){
					pname = results.getString("st_name");
					plname = results.getString("st_lname");
				}
				else if(empid != 0){
					pname = results.getString("emp_name");
					plname = results.getString("emp_lname");
				}
				else if(memid != 0){
					pname = results.getString("mem_name");
					plname = results.getString("mem_lname");
				}
				
				Option cover = new Option(results.getInt("borrow_cover_id"), results.getString("cover_name"));
				
				Object row[] = {
					results.getInt("borrow_id"),
					pid,
					pname,
					plname,
					cover,
					df.format(deliver),
					days,
					df.format(deadline),
					returnDate != null ? df.format(returnDate) : null,
					results.getBoolean("borrow_returned"),
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
}























