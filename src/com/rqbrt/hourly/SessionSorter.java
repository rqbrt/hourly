package com.rqbrt.hourly;

import java.time.LocalDateTime;
import java.util.Comparator;

import com.rqbrt.hourly.models.Session;

import javafx.scene.control.TreeItem;

public class SessionSorter implements Comparator<TreeItem<Session>> {

	@Override
	public int compare(TreeItem<Session> o1, TreeItem<Session> o2) {
		LocalDateTime ldt1 = o1.getValue().getLocalStartTime();
		LocalDateTime ldt2 = o2.getValue().getLocalStartTime();
		return ldt1.compareTo(ldt2);
	}
}
