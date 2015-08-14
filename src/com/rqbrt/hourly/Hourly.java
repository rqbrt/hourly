package com.rqbrt.hourly;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.rqbrt.hourly.alerts.ExceptionAlert;
import com.rqbrt.hourly.models.Session;
import com.rqbrt.hourly.util.FileIO;
import com.rqbrt.hourly.util.Log;
import com.rqbrt.hourly.windows.MainWindow;
import com.rqbrt.hourly.windows.StartupWindow;
import com.rqbrt.hourly.windows.Window;

import javafx.application.Application;
import javafx.stage.Stage;


public class Hourly extends Application {
	
	private Window window;
	private List<String> recentDatabases;
	
	private Database db;
	
	public static void main(String[] args) {
		try {
			launch(args);
		} catch (Exception e) {
			Log.exception("App exception", e);
		}
	}
	
	@Override
	public void start(Stage s) throws Exception {
		Log.msg("Starting");
		if (!new File("databases.txt").exists()) FileIO.write("", "databases.txt", false);
		recentDatabases = FileIO.read("databases.txt");
		window = new StartupWindow(this);
		window.show();
	}
	
	private void updateRecentDatabaseFile() {
		try {
			FileIO.write("", "databases.txt", false);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < recentDatabases.size(); i++) {
				builder.append(recentDatabases.get(i) + System.lineSeparator());
			}
			FileIO.write(builder.toString(), "databases.txt", true);
			Log.msg("Updated recently used database list");
		} catch (IOException e) {
			new ExceptionAlert("Could not update recently used databases file", "An exception occurred while updating recent databases", e).showAndWait();
		}
	}
	
	public void removeRecentDatabasePath(String path) {
		recentDatabases.remove(path);
		updateRecentDatabaseFile();
		Log.msg("Removed " + path + " from recently used databases");
	}
	
	private void appendDatabasePath(String path) {
		boolean isNew = !recentDatabases.contains(path);
		recentDatabases.remove(path);
		recentDatabases.add(0, path);
		updateRecentDatabaseFile();
		if (isNew) Log.msg("Added " + path + " to recently used databases");
	}
	
	public void use(String path) {
		Log.msg("Using " + path);
		db = new Database(path);
		try {
			db.init();
			Log.msg("Database initialized");
			appendDatabasePath(path);
			window.close();
			window = new MainWindow(this);
			window.show();
			
			try {
				List<Session> sessions = db.getSessions();
				sessions.forEach(session -> {
					((MainWindow)window).getSessionTable().addSession(session);
				});
				Log.msg("Sessions loaded");
			} catch (SQLException e) {
				new ExceptionAlert("Failed to get sessions", "An exception occurred while trying to read sessions from the database", e).showAndWait();
				window.close();
				window = new StartupWindow(this);
				window.show();
			}
		} catch (SQLException e) {
			new ExceptionAlert("Failed to initialize database", "An exception occurred while trying to create the database", e).showAndWait();
		}
	}
	
	public List<String> getRecentDatabases() {
		return recentDatabases;
	}
	
	public Window getWindow() {
		return window;
	}
	
	public Database getDatabase() {
		return db;
	}
	
	public void setWindow(Window window) {
		this.window = window;
	}
}
