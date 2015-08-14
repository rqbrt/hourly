package com.rqbrt.hourly.dialogs;

import java.io.File;
import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalDateTextField;

public class ExportDialog extends Dialog {

	private TextField outputFilePath;
	private LocalDateTextField startDate;
	private LocalDateTextField stopDate;
	private CheckBox includeDescriptions;
	
	public ExportDialog(Stage parent, LocalDate from, LocalDate to) {
		super(parent);
		stage.setTitle("Export Sessions");
		
		BorderPane pane = new BorderPane();
		scene = new Scene(pane);
		
		GridPane form = new GridPane();
		form.setPadding(new Insets(10));
		form.setHgap(8);
		form.setVgap(8);
		
		form.add(new Label("Output file"), 0, 0);
		
		HBox outputControls = new HBox();
		outputControls.setSpacing(4);
		
		outputFilePath = new TextField();
		HBox.setHgrow(outputFilePath, Priority.ALWAYS);
		Button browse = new Button("Browse");
		browse.setOnAction(e -> browseAction());
		
		outputControls.getChildren().addAll(outputFilePath, browse);
		form.add(outputControls, 1, 0);
		
		form.add(new Label("From"), 0, 1);
		startDate = new LocalDateTextField(from);
		GridPane.setHgrow(startDate, Priority.ALWAYS);
		form.add(startDate, 1, 1);
		
		form.add(new Label("To"), 0, 2);
		stopDate = new LocalDateTextField(to);
		GridPane.setHgrow(stopDate, Priority.ALWAYS);
		form.add(stopDate, 1, 2);
		
		includeDescriptions = new CheckBox("Include descriptions");
		form.add(includeDescriptions, 0, 3, 2, 1);
		
		pane.setCenter(form);
		pane.setBottom(createButtons());
		
		stage.setScene(scene);
	}
	
	private void browseAction() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Select output file location");
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Text Files", "*.txt"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
		);
		fc.setInitialFileName("export_results.txt");
		File file = fc.showSaveDialog(stage);
		if (file != null)
			outputFilePath.setText(file.getPath());
	}
	
	public String getOutputFilePath() {
		return outputFilePath.getText();
	}
	
	public LocalDate getStartDate() {
		return startDate.getLocalDate();
	}
	
	public LocalDate getStopDate() {
		return stopDate.getLocalDate();
	}
	
	public boolean includeDescriptions() {
		return includeDescriptions.isSelected();
	}
}
