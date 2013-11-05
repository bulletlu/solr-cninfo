package com.cninfo.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.DictionaryGenerator;
import org.wltea.analyzer.dic.DictionaryWithZK;

public class WordWithZKServlet extends HttpServlet {
	private static final long serialVersionUID = -3362065005715783148L;
	
	public static Logger logger = Logger.getLogger(WordWithZKServlet.class);
	public static final int COMMIT_COUNT = 1000;
	
	private Dictionary dic;

	public WordWithZKServlet() {
		super();
	}
	
	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String dicType = request.getParameter("dic");
		String op = request.getRequestURI().replaceAll("^.*/", "").replaceAll("\\.wd", "");
		
		
		if("add".equals(op)){
			String words = request.getParameter("wd");
			Collection<String> set = new HashSet<String>();
			if(words != null){
				for(String word:words.split("[,;]")){
					set.add(word);
				}
			}
			this.addWords(set, dicType);
		}else if("del".equals(op)){
			String words = request.getParameter("wd");
			Collection<String> set = new HashSet<String>();
			if(words != null){
				for(String word:words.split("[,;]")){
					set.add(word);
				}
			}
			this.romoveWords(set, dicType);
		}else if("init".equals(op)){
			this.initWords(dicType);
		}
		
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.write("Dictionary:"+dicType+"    Operation:"+op );
		out.flush();
		out.close();
	}
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
	}
	
	
	/**
	 * @param set
	 * @param dicType
	 */
	private void addWords(Collection<String> set,String dicType){
		Connection conn = ConnectionManager.getConnection(ConnectionManager.POOL);
		PreparedStatement stat = null;
		String sql = "insert into tb_ext_dictionary(category,word,createtime,creator,enable) values(?,?,now(),?,?) on duplicate key update lastmodtime=now(),lastmodifier='modifier'";
		try {
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(sql);
			for(String word:set){
				stat.setString(1, dicType);
				stat.setString(2, word);
				stat.setString(3, "admin");
				stat.setString(4, "1");
				stat.executeUpdate();
			}
			if("ext".equals(dicType.toLowerCase())){
				dic.addWordsToMainDict(set);
			}else if("stop".equals(dicType.toLowerCase())){
				dic.addWordsToStopDict(set);
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally{
			ConnectionManager.closeState(stat);
			ConnectionManager.closeConn(conn);
		}
	}
	
	
	/**
	 * @param dicType
	 */
	private void initWords(String dicType){
		Connection conn = ConnectionManager.getConnection(ConnectionManager.POOL);
		Statement stat = null;
		ResultSet re = null;
		String sql = "select word from tb_ext_dictionary where category='"+dicType+"' and enable='1' order by wid";
		try {
			stat = conn.createStatement();
			re = stat.executeQuery(sql);
			Collection<String> set = new HashSet<String>();
			int i=0;
			while(re.next()){
				String word = re.getString("word");
				set.add(word);
				if(i >= COMMIT_COUNT){
					if("ext".equals(dicType.toLowerCase())){
						dic.addWordsToMainDict(set);
					}else if("stop".equals(dicType.toLowerCase())){
						dic.addWordsToStopDict(set);
					}
					i=0;
					set.clear();
				}
				i++;
			}
			
			if("ext".equals(dicType.toLowerCase())){
				dic.addWordsToMainDict(set);
			}else if("stop".equals(dicType.toLowerCase())){
				dic.addWordsToStopDict(set);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			ConnectionManager.closeState(stat);
			ConnectionManager.closeConn(conn);
		}
	}
	
	
	/**
	 * @param set
	 * @param dicType
	 */
	private void romoveWords(Collection<String> set,String dicType){
		Connection conn = ConnectionManager.getConnection(ConnectionManager.POOL);
		PreparedStatement stat = null;
		String sql = "delete from tb_ext_dictionary where category=? and word=?";
		try {
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(sql);
			for(String word:set){
				stat.setString(1, dicType);
				stat.setString(2, word);
				stat.executeUpdate();
			}
			if("ext".equals(dicType.toLowerCase())){
				dic.disableWordsFromMainDict(set);
			}else if("stop".equals(dicType.toLowerCase())){
				dic.disableWordsFromStopDict(set);
			}
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally{
			ConnectionManager.closeState(stat);
			ConnectionManager.closeConn(conn);
		}
	}
	
	public void init() throws ServletException {
		Configuration config = DefaultConfig.getInstance();
		DictionaryGenerator.initial(config);
		dic = DictionaryGenerator.generate();
		
		this.initWords("EXT");
		this.initWords("STOP");
	}

}
