package com.aselisoftware.mysql;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionInstaller {

	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private Connection connection = null;
	private String connectionParams;
	private String name = "";
	
	private static final Logger log = LogManager.getLogger(ConnectionInstaller.class);

	public static String getHost(String connectionParams) {
		
		Pattern pattern = Pattern.compile("HOST:\\s*(\\S*?);", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(connectionParams);
		return matcher.find() ? matcher.group(1) : "";
	}
	
	public static String getLogin(String connectionParams) {
		
		Pattern pattern = Pattern.compile("LOGIN:\\s*(\\S*?);", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(connectionParams);
		return matcher.find() ? matcher.group(1) : "";
	}

	public static String getPassword(String connectionParams) {
		
		Pattern pattern = Pattern.compile("PASSWORD:\\s*(\\S*?);", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(connectionParams);
		return matcher.find() ? matcher.group(1) : "";
	}
		
	public static String getDatabase(String connectionParams) {
		
		Pattern pattern = Pattern.compile("DATABASE:\\s*(\\S*?);", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(connectionParams);
		return matcher.find() ? matcher.group(1) : "";
	}
	
	public static void checkParams(String name, String host, String login, String password, String database) throws Exception {
		
		String s = (!name.isEmpty()) ? String.format("for the %s ", name) : ""; 
		
		if (host.isEmpty())
			throw new Exception(String.format("Host %sis not specified.", s));
		if (login.isEmpty())
			throw new Exception(String.format("Login %sis not specified.", s));
		if (password.isEmpty())
			throw new Exception(String.format("Password %sis not specified.", s));
		if (database.isEmpty())
			throw new Exception(String.format("Database %sis not specified.", s));
	}

	/**
	 * Constructor.
	 * @param connectionParams
	 */
	public ConnectionInstaller(String connectionParams) {
		
		this.connectionParams = connectionParams;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getConnectionParams() {
		
		return connectionParams;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof ConnectionInstaller) {
			
			ConnectionInstaller con = (ConnectionInstaller) obj;
			return (getHost().equals(con.getHost()) &&
				getLogin().equals(con.getLogin()) &&
				getPassword().equals(con.getPassword()) &&
				getDatabase().equals(con.getDatabase()));
		} else
			return false;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void checkParams() throws Exception {
		
		checkParams(name, getHost(connectionParams), getLogin(connectionParams),
			getPassword(connectionParams), getDatabase(connectionParams));
	}

	
	public String getHost() {
		
		return getHost(connectionParams);
	}
	
	public String getLogin() {
		
		return getLogin(connectionParams);
	}
	
	public String getPassword() {
		
		return getPassword(connectionParams);
	}
	
	public String getDatabase() {
		
		return getDatabase(connectionParams);
	}

	/**
	 * Make connection to database.
	 * @param connectionParams Connection parameters as a string with ";" delimiters.
	 */
	public boolean connect() {
		
		try {
			
			Class.forName(JDBC_DRIVER);
			String host = "jdbc:mysql://" + getHost();
			connection = DriverManager.getConnection(host, getLogin(), getPassword());
			connection.setAutoCommit(false);
			return true;
		} catch (SQLException | ClassNotFoundException ex) {
			
			log.error(ex);
			return false;
		}
	}
	
	/**
	 * Close connection.
	 */
	public void close() {
		
		try {
			
			if (!isConnected())
				return;
			connection.close();
			connection = null;
		} catch (SQLException e) {
			
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Check connection.
	 * @return
	 */
	public boolean isConnected() {
		
		try {
			
			if (null != connection) 
				return !connection.isClosed();
			else
				return false;
		} catch (SQLException e) {
			
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * Check availability of the database.
	 * @param connectionParams Connection parameters as a string with ";" delimiters.
	 * @throws Exception "No connection." - no connection to host (need execute connect() method).
	 * "Database not found." - no database with specified name. 
	 */
	public void checkDatabase() throws Exception {
		
		checkDatabase(connectionParams);
	}
	
	public void checkDatabase(String connectionParams) throws Exception {
		
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			
			if (!isConnected())
				throw new Exception("No connection.");
			st = connection.prepareStatement("SHOW DATABASES LIKE ?");
			st.setString(1, getDatabase(connectionParams));
			rs = st.executeQuery();
			if (!rs.first())
				throw new Exception("Database \"" + getDatabase(connectionParams) + "\" not found.");
		} finally {
			
			try {
			
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			} catch (SQLException e) {
				
				System.out.println(e.getMessage());
			}
		}			
	}
	
	
	public String []RequestTables() throws Exception {
		
		String []list = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		int i = 0;
		
		try {
			
			if (!isConnected())
				throw new Exception("No connection.");
			st = connection.prepareStatement("SHOW TABLES FROM `" + getDatabase() + "`");
			rs = st.executeQuery();
			if (!rs.last())
				throw new Exception("No tables in database \"" + getDatabase() + "\".");
			list = new String[rs.getRow()];
			rs.first();
			do {
				list[i++] = rs.getString(1);
			} while (rs.next());
		} finally {
			
			try {
			
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			} catch (SQLException e) {
				
				System.out.println(e.getMessage());
			}
			
		}			
		return list;
	}
	
	public PreparedStatement prepareSQL(String SQL) throws SQLException {
		
		return connection.prepareStatement(SQL);
	}

	/**
	 * Get the connection name.
	 * @return
	 */
	public String getName() {
		
		return name;
	}

	/**
	 * Set the connection name.
	 * @param caption
	 */
	public void setName(String name) {
		
		this.name = name;
	}
}
