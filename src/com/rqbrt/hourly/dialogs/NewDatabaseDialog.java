package com.rqbrt.hourly.dialogs;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class NewDatabaseDialog extends Dialog {
	
	private TextField filePath;
	
	public NewDatabaseDialog(Stage parent) {
		super(parent);
		stage.setTitle("New Project");
		
		BorderPane pane = new BorderPane();
		scene = new Scene(pane, 400, 125);
		
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(4);
		
		HBox fileSelection = new HBox();
		fileSelection.setSpacing(4);
		
		Label label = new Label("Path to database file");
		Button browse = new Button("Browse");
		browse.setOnAction(e -> browse());
		
		filePath = new TextField();
		filePath.setMaxSize(Double.MAX_VALUE, 25);
		HBox.setHgrow(filePath, Priority.ALWAYS);
		
		fileSelection.getChildren().addAll(filePath, browse);
		vbox.getChildren().addAll(label, fileSelection);
		pane.setCenter(vbox);
		
		pane.setBottom(createButtons());
		stage.setScene(scene);
	}
	
	private void browse() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Select database location");
		fc.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Hourly Files", "*.hrs"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
		);
		fc.setInitialFileName("untitled.hrs");
		File file = fc.showSaveDialog(stage);
		if (file != null)
			filePath.setText(file.getPath());
	}
	
	public String getFilePath() {
		return filePath.getText();
	}
}
