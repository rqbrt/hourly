package com.rqbrt.hourly.dialogs;

import java.time.LocalDateTime;

import com.rqbrt.hourly.models.Session;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalDateTimeTextField;

public class EditSessionDialog extends Dialog {

	private TextField description;
	private LocalDateTimeTextField start;
	private LocalDateTimeTextField stop;
	
	public EditSessionDialog(Stage parent, Session s) {
		super(parent);
		stage.setTitle("Edit Session");
		
		BorderPane pane = new BorderPane();
		scene = new Scene(pane, 400, 160);
		
		GridPane form = new GridPane();
		form.setPadding(new Insets(10));
		form.setHgap(8);
		form.setVgap(8);
		
		form.add(new Label("Description"), 0, 0);
		description = new TextField(s.getDescription());
		GridPane.setHgrow(description, Priority.ALWAYS);
		form.add(description, 1, 0);
		
		form.add(new Label("Start time"), 0, 1);
		start = new LocalDateTimeTextField(s.getLocalStartTime());
		GridPane.setHgrow(start, Priority.ALWAYS);
		form.add(start, 1, 1);
		
		form.add(new Label("Stop time"), 0, 2);
		stop = new LocalDateTimeTextField(s.getLocalStopTime());
		GridPane.setHgrow(stop, Priority.ALWAYS);
		form.add(stop, 1, 2);
		
		pane.setCenter(form);
		pane.setBottom(createButtons());
		
		stage.setScene(scene);
	}
	
	public String getDescription() {
		return description.getText();
	}
	
	public LocalDateTime getStartTime() {
		return start.getLocalDateTime();
	}
	
	public LocalDateTime getStopTime() {
		return stop.getLocalDateTime();
	}
}
