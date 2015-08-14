package com.rqbrt.hourly.alerts;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.rqbrt.hourly.util.Log;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionAlert extends Alert {

	public ExceptionAlert(String headerText, String contentText, Exception e) {
		super(AlertType.ERROR);
		Log.exception(headerText + " - " + contentText, e);
		
		setTitle("Exception");
		setHeaderText(headerText);
		setContentText(contentText);
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		TextArea textArea = new TextArea(sw.toString());
		textArea.setEditable(false);
		textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		
		GridPane pane = new GridPane();
		pane.setMaxWidth(Double.MAX_VALUE);
		pane.add(textArea, 0, 0);
		
		getDialogPane().setExpandableContent(pane);
	}
}
