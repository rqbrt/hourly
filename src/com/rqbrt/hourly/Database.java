package com.rqbrt.hourly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.rqbrt.hourly.models.Session;
import com.rqbrt.hourly.util.Log;

public class Database {

	private String filePath;
	private boolean ready;
	
	public Database(String filePath) {
		this.filePath = filePath;
		ready = false;
	}
	
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:sqlite:" + filePath);
	}
	
	private void closeConnection(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			Log.exception("Could not close database connection", e);
		}
	}
	
	public void init() throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			
			stmt.executeUpdate("create table if not exists sessions(id integer primary key, description string, start unsigned integer, stop unsigned integer)");
			stmt.executeUpdate("create index if not exists start_index on sessions(start)");
			stmt.executeUpdate("create index if not exists stop_index on sessions(stop)");
			
			ready = true;
		} catch (SQLException e) {
			throw new SQLException("Could not create database (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public void addSession(Session s) throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into sessions(description, start, stop) values(?, ?, ?)");
			stmt.setString(1, s.getDescription());
			stmt.setLong(2, s.getStartTime().toEpochMilli());
			stmt.setLong(3, s.getStopTime().toEpochMilli());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException("Could not add session to database (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public List<Session> getSessions() throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from sessions");
			List<Session> sessions = new ArrayList<>();
			while (rs.next()) {
				sessions.add(Session.fromResultSet(rs));
			}
			return sessions;
		} catch (SQLException e) {
			throw new SQLException("Could not get sessions from database (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public void updateSession(Session s) throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("update sessions set description=?, start=?, stop=? where id=?");
			stmt.setString(1, s.getDescription());
			stmt.setLong(2, s.getStartTime().toEpochMilli());
			stmt.setLong(3, s.getStopTime().toEpochMilli());
			stmt.setInt(4, s.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException("Could not update session in database (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public void deleteSession(Session s) throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("delete from sessions where id=?");
			stmt.setInt(1, s.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new SQLException("Could not delete session in database (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public Session firstChronologicalSession() throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from sessions order by start asc limit 1");
			rs.next();
			return Session.fromResultSet(rs);
		} catch (SQLException e) {
			throw new SQLException("Could not get first chronological session (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public Session lastChronologicalSession() throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from sessions order by start desc limit 1");
			rs.next();
			return Session.fromResultSet(rs);
		} catch (SQLException e) {
			throw new SQLException("Could not get last chronological session (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public List<Session> getSessionsWithinDateRange(long from, long to) throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from sessions where start >= ? and stop <= ? order by start asc");
			stmt.setLong(1, from);
			stmt.setLong(2, to);
			ResultSet rs = stmt.executeQuery();
			List<Session> sessions = new ArrayList<Session>();
			while(rs.next()) {
				sessions.add(Session.fromResultSet(rs));
			}
			return sessions;
		} catch (SQLException e) {
			throw new SQLException("Could not get sessions within date range (" + e.getMessage() + ")");
		} finally {
			closeConnection(connection);
		}
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public String getFilePath() {
		return filePath;
	}
}
