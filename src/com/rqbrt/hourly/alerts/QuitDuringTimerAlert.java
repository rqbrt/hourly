package com.rqbrt.hourly.alerts;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class QuitDuringTimerAlert extends Alert {

	public QuitDuringTimerAlert() {
		super(AlertType.WARNING);
		setTitle("Quit without saving");
		setHeaderText("The current session's timer is still running and will not be saved");
		setContentText("Quit while timer is running?");
		getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
	}
}
