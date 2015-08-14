package com.rqbrt.hourly.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Dialog {
	
	public enum DialogResult {
		NONE, OK, CANCEL
	};
	
	protected Stage parent;
	
	protected Stage stage;
	protected Scene scene;
	
	protected DialogResult result;
	
	public Dialog(Stage parent) {
		this.parent = parent;
		
		stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(parent);
		
		result = DialogResult.NONE;
	}
	
	protected HBox createButtons() {
		HBox buttons = new HBox();
		buttons.setPadding(new Insets(10));
		buttons.setSpacing(8);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		
		Button cancel = new Button("Cancel");
		cancel.setOnAction(e -> exit(DialogResult.CANCEL));
		
		Button okay = new Button("OK");
		okay.setOnAction(e -> exit(DialogResult.OK));
		okay.setDefaultButton(true);
		
		buttons.getChildren().addAll(cancel, okay);
		return buttons;
	}
	
	protected void exit(DialogResult result) {
		this.result = result;
		if (stage.isShowing())
			stage.close();
	}
	
	public DialogResult show() {
		try {
			stage.showAndWait();
		} catch (Exception e) {
			System.err.println("Exception ocurred within dialog: " + e.getMessage());
			result = DialogResult.NONE;
		}
		return result;
	}
}
