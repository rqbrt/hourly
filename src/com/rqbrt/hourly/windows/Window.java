package com.rqbrt.hourly.windows;

import com.rqbrt.hourly.Hourly;

import javafx.stage.Stage;

public class Window extends Stage {

	protected Hourly hourly;
	
	public Window(Hourly hourly) {
		this.hourly = hourly;
	}
}
