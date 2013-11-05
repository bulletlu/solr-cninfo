package com.cninfo.servlet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager {
	public static String POOL = "jdbc/solr_wd";
	
	public static Connection getConnection(String key){
		Connection con = null;
		try {
			Context ctx = new InitialContext();
			String pool = "java:comp/env/"+key;
			DataSource ds = (DataSource)ctx.lookup(pool);
			con = ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	public static void closeConn(Connection con){
		if(con != null)
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeState(Statement stat){
		if(stat != null)
		try {
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeResultset(ResultSet rs){
		if(rs != null)
		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
