package com.rqbrt.hourly.alerts;

import com.rqbrt.hourly.models.Session;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class DeleteSessionAlert extends Alert {

	public DeleteSessionAlert(Session session) {
		super(AlertType.WARNING);
		setTitle("Delete Session");
		setHeaderText("Are you sure you want to delete this session?");
		
		GridPane pane = new GridPane();
		pane.setPadding(new Insets(8));
		pane.setHgap(10);
		pane.setVgap(2);
		
		pane.add(new Label("ID"), 0, 0);
		pane.add(new Label(Integer.toString(session.getId())), 1, 0);
		pane.add(new Label("Description"), 0, 1);
		pane.add(new Label(session.getDescription()), 1, 1);
		pane.add(new Label("Start"), 0, 2);
		pane.add(new Label(session.getLocalStartTime().toString()), 1, 2);
		pane.add(new Label("Stop"), 0, 3);
		pane.add(new Label(session.getLocalStopTime().toString()), 1, 3);
		
		getDialogPane().setContent(pane);
		
		getButtonTypes().add(ButtonType.CANCEL);
	}
}
