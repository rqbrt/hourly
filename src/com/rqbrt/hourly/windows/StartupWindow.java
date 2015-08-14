package com.rqbrt.hourly.windows;

import java.io.File;

import com.rqbrt.hourly.Hourly;
import com.rqbrt.hourly.dialogs.NewDatabaseDialog;
import com.rqbrt.hourly.dialogs.Dialog.DialogResult;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class StartupWindow extends Window {
	
	private ListView<String> databaseList;
	
	public StartupWindow(Hourly hourly) {
		super(hourly);
		
		setTitle("Hourly");
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane, 480, 300);
		
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10);
		Text description = new Text("Select a database file");
		
		databaseList = new ListView<>(FXCollections.observableArrayList(hourly.getRecentDatabases()));
		databaseList.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) selectDatabaseAction();
		});
		databaseList.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) selectDatabaseAction();
		});
		VBox.setVgrow(databaseList, Priority.ALWAYS);
		
		vbox.getChildren().addAll(description, databaseList);
		
		HBox buttons = new HBox();
		buttons.setPadding(new Insets(10));
		buttons.setSpacing(8);
		buttons.setAlignment(Pos.TOP_RIGHT);
		
		Button create = new Button("Create");
		create.setOnAction(e -> creatDatabaseAction());
		Button open = new Button("Open");
		open.setOnAction(e -> openDatabaseAction());
		Button remove = new Button("Remove");
		remove.setOnAction(e -> removeDatabaseAction());
		
		StackPane stack = new StackPane();
		Button select = new Button("Select");
		select.setDefaultButton(true);
		select.setOnAction(e -> selectDatabaseAction());
		stack.getChildren().add(select);
		HBox.setHgrow(stack, Priority.ALWAYS);
		StackPane.setAlignment(select, Pos.CENTER_RIGHT);
		
		buttons.getChildren().addAll(create, open, remove, stack);
		
		pane.setCenter(vbox);
		pane.setBottom(buttons);
		
		setScene(scene);
		
		if (!hourly.getRecentDatabases().isEmpty()) databaseList.getSelectionModel().select(0);
	}
	
	private void creatDatabaseAction() {
		NewDatabaseDialog dlg = new NewDatabaseDialog(this);
		if (dlg.show() == DialogResult.OK)
			hourly.use(dlg.getFilePath());
	}
	
	private void openDatabaseAction() {
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
	
	private void removeDatabaseAction() {
		if (databaseList.getSelectionModel().isEmpty()) return;
		String path = databaseList.getSelectionModel().getSelectedItem();
		hourly.removeRecentDatabasePath(path);
		databaseList.getItems().remove(path);
	}
	
	private void selectDatabaseAction() {
		if (databaseList.getSelectionModel().isEmpty()) return;
		hourly.use(databaseList.getSelectionModel().getSelectedItem());
	}
}
