package com.rqbrt.hourly.windows;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.rqbrt.hourly.Hourly;
import com.rqbrt.hourly.SessionTable;
import com.rqbrt.hourly.alerts.ExceptionAlert;
import com.rqbrt.hourly.alerts.QuitDuringTimerAlert;
import com.rqbrt.hourly.dialogs.EditSessionDialog;
import com.rqbrt.hourly.dialogs.ExportDialog;
import com.rqbrt.hourly.dialogs.NewDatabaseDialog;
import com.rqbrt.hourly.dialogs.Dialog.DialogResult;
import com.rqbrt.hourly.models.Session;
import com.rqbrt.hourly.util.FileIO;
import com.rqbrt.hourly.util.Log;
import com.rqbrt.hourly.util.Seconds;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class MainWindow extends Window {
	
	private SessionTable sessionTable;
	
	private Button timerButton;
	private Text timer;
	private long time;
	private boolean timerRunning = false;
	private Session timerSession;
	
	public MainWindow(Hourly hourly) {
		super(hourly);
		setTitle("Hourly - " + hourly.getDatabase().getFilePath());
		
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane, 480, 300);
		
		sessionTable = new SessionTable(hourly);
		pane.setCenter(sessionTable);
		
		// Create menubar
		Menu file = new Menu("File");
		
		MenuItem fileNew = new MenuItem("New");
		fileNew.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
		fileNew.setOnAction(e -> fileNewMenuAction());
		
		MenuItem fileOpen = new MenuItem("Open");
		fileOpen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
		fileOpen.setOnAction(e -> fileOpenMenuAction());
		
		MenuItem fileExport = new MenuItem("Export");
		fileExport.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
		fileExport.setOnAction(e -> fileExportMenuAction());
		
		MenuItem fileClose = new MenuItem("Close");
		fileClose.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
		fileClose.setOnAction(e -> fileCloseMenuAction());
		
		file.getItems().addAll(fileNew, fileOpen, new SeparatorMenuItem(), fileExport, new SeparatorMenuItem(), fileClose);
		
		Menu edit = new Menu("Edit");
		
		MenuItem editAddSession = new MenuItem("Add session");
		editAddSession.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
		editAddSession.setOnAction(e -> editAddSessionMenuAction());
		
		edit.getItems().addAll(editAddSession);
		
		MenuBar menubar = new MenuBar();
		menubar.getMenus().addAll(file, edit);
		pane.setTop(menubar);
		
		// Create timer controls
		HBox timerBox = new HBox();
		timerBox.setPadding(new Insets(10));
		timerBox.setSpacing(8);
		
		timerButton = new Button("Start");
		timerButton.setOnAction(e -> toggleTimerAction());
		
		timer = new Text("00:00:00");
		timer.setFont(Font.font(18));
		StackPane stack = new StackPane();
		stack.getChildren().addAll(timer);
		stack.setAlignment(Pos.CENTER_RIGHT);
		
		timerBox.getChildren().addAll(timerButton, stack);
		HBox.setHgrow(stack, Priority.ALWAYS);
		
		pane.setBottom(timerBox);
		
		setScene(scene);
		
		setOnCloseRequest(e -> {
			if (!quitWhileTimerRunning()) {
				e.consume();
			}
			
			timerRunning = false;
		});
	}
	
	private void addSession(Session s) {
		try {
			hourly.getDatabase().addSession(s);
			sessionTable.addSession(s);
			Log.msg("Added " + s);
		} catch (SQLException e) {
			new ExceptionAlert("Failed to add session", "An exception occurred while writing to the database", e).showAndWait();
		}
	}
	
	private void toggleTimerAction() {
		if (timerRunning) {
			// stop
			Log.msg("Stopping timer");
			timerRunning = false;
			timerSession.setStopTime(Instant.now());
			timerButton.setText("Start");
			addSession(timerSession);
		} else {
			// start
			Log.msg("Starting timer");
			timerSession = new Session();
			timerSession.setStartTime(Instant.now());
			timerButton.setText("Stop");
			timerRunning = true;
			time = 0;
			new Thread(new Runnable() {
				public void run() {
					while (timerRunning) {
						try {
							Thread.sleep(1000);
							if (timerRunning) {
								time++;
								timer.setText(Seconds.hms(time));
							}
						} catch (InterruptedException e) {
							Log.exception("Could not put thread to sleep", e);
						}
					}
				}
			}).start();
		}
	}
	
	private void fileNewMenuAction() {
		NewDatabaseDialog dlg = new NewDatabaseDialog(this);
		if (dlg.show() == DialogResult.OK)
			hourly.use(dlg.getFilePath());
	}
	
	private void fileOpenMenuAction() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Open existing database");
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Hourly files", "*.hrs"),
				new FileChooser.ExtensionFilter("All files", "*.*")
		);
		File file = fc.showOpenDialog(this);
		if (file != null)
			hourly.use(file.getPath());
	}
	
	private void exportSessions(String filePath, List<Session> sessions, boolean includeDescriptions) {
		StringBuilder builder = new StringBuilder();
		LocalDate currentDate = LocalDate.MIN;
		String newLine = System.lineSeparator();
		
		String dayFormatPattern = "E MMM dd, u";
		long dayTotal = 0;
		long total = 0;
		for (int i = 0; i < sessions.size(); i++) {
			Session s = sessions.get(i);
			if (!currentDate.equals(s.getLocalStartTime().toLocalDate())) {
				if (i > 0) {
					builder.append("Total: " + Seconds.hms(dayTotal) + newLine);
					builder.append(newLine);
				}
				dayTotal = 0;
				currentDate = s.getLocalStartTime().toLocalDate();
				builder.append(currentDate.format(DateTimeFormatter.ofPattern(dayFormatPattern)) + newLine);
				builder.append("----------------" + newLine);
			}
			
			String sessionString = "";
			if (includeDescriptions) sessionString += s.getDescription() + " ";
			sessionString += s.getStart() + " - " + s.getStop() + " | " + s.getTotalHours();
			builder.append(sessionString + newLine);
			
			dayTotal += s.getPassedTime();
			total += s.getPassedTime();
			
			if (i == sessions.size() - 1) {
				builder.append("Total: " + Seconds.hms(dayTotal) + newLine);
			}
		}
		
		builder.append(newLine + newLine + "Total hours overall: " + Seconds.hms(total) + newLine);
		try {
			FileIO.write(builder.toString(), filePath, false);
			Log.msg("Exported sessions");
		} catch (IOException e) {
			new ExceptionAlert("Could not export sessions", "An exception occurred while exporting sessions", e).showAndWait();
		}
	}
	
	private void fileExportMenuAction() {
		Session first = null, last = null;
		try {
			first = hourly.getDatabase().firstChronologicalSession();
			last = hourly.getDatabase().lastChronologicalSession();
		} catch (SQLException e) {
			new ExceptionAlert("Could not get first or last session", "An exception occurred while getting the first or last session", e).showAndWait();
			first = new Session();
			first.setStartTime(Instant.now());
			last = new Session();
			last.setStartTime(Instant.now());
		}
		
		ExportDialog dlg = new ExportDialog(this, first.getLocalStartTime().toLocalDate(), last.getLocalStartTime().toLocalDate());
		if (dlg.show() == DialogResult.OK) {
			try {
				long from = dlg.getStartDate().atTime(LocalTime.MIN).toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStartDate().atStartOfDay())).toEpochMilli();
				long to = dlg.getStopDate().atTime(LocalTime.MAX).toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStopDate().atStartOfDay())).toEpochMilli();
				exportSessions(dlg.getOutputFilePath(), hourly.getDatabase().getSessionsWithinDateRange(from, to), dlg.includeDescriptions());
			} catch (SQLException e) {
				new ExceptionAlert("Failed to get sessions for export", "An exception occurred while getting sessions for export", e).showAndWait();
			}
		}
	}
	
	private boolean quitWhileTimerRunning() {
		if (timerRunning) {
			Optional<ButtonType> result = new QuitDuringTimerAlert().showAndWait();
			if (result.get() == ButtonType.NO) return false;
			else if (result.get() == ButtonType.YES) return true;
			else return false;
		} else {
			return true;
		}
	}
	
	private void fileCloseMenuAction() {
		if (!quitWhileTimerRunning()) return;
		timerRunning = false;
		close();
		hourly.setWindow(new StartupWindow(hourly));
		hourly.getWindow().show();
	}
	
	private void editAddSessionMenuAction() {
		EditSessionDialog dlg = new EditSessionDialog(this, new Session().setStartTime(Instant.now()).setStopTime(Instant.now()));
		if (dlg.show() == DialogResult.OK) {
			Session s = new Session();
			s.setDescription(dlg.getDescription() == null ? "" : dlg.getDescription());
			s.setStartTime(dlg.getStartTime().toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStartTime())));
			s.setStopTime(dlg.getStopTime().toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStopTime())));
			addSession(s);
		}
	}
	
	public SessionTable getSessionTable() {
		return sessionTable;
	}
}
