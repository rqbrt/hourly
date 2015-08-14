package com.rqbrt.hourly;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.rqbrt.hourly.alerts.DeleteSessionAlert;
import com.rqbrt.hourly.alerts.ExceptionAlert;
import com.rqbrt.hourly.dialogs.EditSessionDialog;
import com.rqbrt.hourly.dialogs.Dialog.DialogResult;
import com.rqbrt.hourly.models.RootSession;
import com.rqbrt.hourly.models.Session;
import com.rqbrt.hourly.util.Log;
import com.rqbrt.hourly.util.Seconds;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ContextMenuEvent;

public class SessionTable extends TreeTableView<Session> {
	
	private static final String GROUP_DATE_FORMAT = "MMM d, u";
	
	private Hourly hourly;
	private TreeItem<Session> root;
	
	private TreeTableColumn<Session, String> description;
	private TreeTableColumn<Session, String> start;
	private TreeTableColumn<Session, String> stop;
	private TreeTableColumn<Session, String> total;
	
	private Map<Integer, Session> rawSessions;
	private List<TreeItem<Session>> groups;
	
	@SuppressWarnings("unchecked")
	public SessionTable(Hourly hourly) {
		this.hourly = hourly;
		
		root = new TreeItem<>(null);
		
		description = new TreeTableColumn<>("Description");
		description.setCellValueFactory((TreeTableColumn.CellDataFeatures<Session, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getDescription()));
		
		start = new TreeTableColumn<>("Start");
		start.setCellValueFactory((TreeTableColumn.CellDataFeatures<Session, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getStart()));
		
		stop = new TreeTableColumn<>("Stop");
		stop.setCellValueFactory((TreeTableColumn.CellDataFeatures<Session, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getStop()));
		
		total = new TreeTableColumn<>("Total");
		total.setCellValueFactory((TreeTableColumn.CellDataFeatures<Session, String> param) -> new ReadOnlyStringWrapper(param.getValue().getValue().getTotalHours()));
		
		getColumns().setAll(description, start, stop, total);
		setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
		
		MenuItem edit = new MenuItem("Edit");
		edit.setOnAction(e -> editSessionContextMenuAction());
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(e -> deleteSessionContextMenuAction());
		setContextMenu(new ContextMenu(edit, delete));
		
		EventHandler<ContextMenuEvent> contextMenuFilter = new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				try {
					// consume the event if it is a group selected
					if (getSelectionModel().getSelectedItem().getChildren().size() > 0) {
						event.consume();
					}
				} catch (NullPointerException e) {
					event.consume();
				}
			}
		};
		addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuFilter);
		
		setRoot(root);
		setShowRoot(false);
		
		rawSessions = new HashMap<>();
		groups = new ArrayList<>();
	}
	
	private void tallyGroupTotal(TreeItem<Session> group) {
		long totalTime = 0;
		for (int i = 0; i < group.getChildren().size(); i++) {
			totalTime += group.getChildren().get(i).getValue().getPassedTime();
		}
		
		group.getValue().setTotalHours(Seconds.hms(totalTime));
	}
	
	private void assignSessionToGroup(Session s) {
		LocalDateTime sdt = s.getLocalStartTime();
		for (int i = 0; i < groups.size(); i++) {
			LocalDateTime gdt = groups.get(i).getValue().getLocalStartTime();
			if (sdt.getYear() == gdt.getYear() && sdt.getDayOfYear() == gdt.getDayOfYear()) {
				groups.get(i).getChildren().add(new TreeItem<>(s));
				groups.get(i).getChildren().sort(new SessionSorter());
				tallyGroupTotal(groups.get(i));
				return;
			}
		}
		
		// create a new group for the session
		TreeItem<Session> group = new TreeItem<>(new RootSession().setDescription(sdt.format(DateTimeFormatter.ofPattern(GROUP_DATE_FORMAT))).setStartTime(s.getStartTime()));
		group.getChildren().add(new TreeItem<>(s));
		groups.add(group);
		tallyGroupTotal(group);
		root.getChildren().add(group);
		root.getChildren().sort(new SessionSorter());
	}
	
	public void refresh() {
		clear();
		for (Map.Entry<Integer, Session> cursor : rawSessions.entrySet()) {
			assignSessionToGroup(cursor.getValue());
		}
	}
	
	private void editSessionContextMenuAction() {
		TreeItem<Session> selected = getSelectionModel().getSelectedItem();
		Session session = selected.getValue();
		if (session instanceof RootSession) return;
		
		EditSessionDialog dlg = new EditSessionDialog(hourly.getWindow(), session);
		if (dlg.show() == DialogResult.OK) {
			Session newSession = new Session();
			newSession.setId(session.getId());
			newSession.setDescription(dlg.getDescription() == null ? "" : dlg.getDescription());
			newSession.setStartTime(dlg.getStartTime().toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStartTime())));
			newSession.setStopTime(dlg.getStopTime().toInstant(ZoneOffset.systemDefault().getRules().getOffset(dlg.getStopTime())));
			
			try {
				hourly.getDatabase().updateSession(newSession);
				rawSessions.put(newSession.getId(), newSession);
				refresh();
				Log.msg("Edited " + session + " to " + newSession);
			} catch (SQLException e) {
				new ExceptionAlert("Failed to edit session", "An exception occurred while trying to update a session in the database", e).showAndWait();
			}
		}
	}
	
	private void deleteSessionContextMenuAction() {
		TreeItem<Session> selected = getSelectionModel().getSelectedItem();
		Session session = selected.getValue();
		if (session instanceof RootSession) return;
		Optional<ButtonType> result = new DeleteSessionAlert(session).showAndWait();
		if (result.get() == ButtonType.OK) {
			try {
				hourly.getDatabase().deleteSession(session);
				rawSessions.remove(session.getId());
				
				TreeItem<Session> parent = selected.getParent();
				parent.getChildren().remove(selected);
				if (parent.getChildren().size() == 0) {
					groups.remove(parent);
					parent.getParent().getChildren().remove(parent);
				} else {
					tallyGroupTotal(parent);
				}
				Log.msg("Deleted " + session);
			} catch (SQLException e) {
				new ExceptionAlert("Failed to delete session", "An exception occurred while trying to delete a session in the database", e).showAndWait();
			}
		}
	}
	
	public void addSession(Session s) {
		rawSessions.put(s.getId(), s);
		assignSessionToGroup(s);
	}
	
	/**
	 * Clears the table but keeps the sessions.
	 */
	public void clear() {
		root.getChildren().clear();
		groups.clear();
	}
	
	/**
	 * Clears the table and all of the sessions.
	 */
	public void clearAll() {
		clear();
		rawSessions.clear();
	}
}
