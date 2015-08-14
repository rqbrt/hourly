package com.rqbrt.hourly.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.rqbrt.hourly.util.Seconds;

import javafx.beans.property.SimpleStringProperty;

public class Session {

	private static final String TIME_FORMAT = "h:mm:ss a";
	private static final String STOP_TIME_DIFFERENT_DAY_FORMAT = "MMM d h:mm:ss a";
	
	private int id;
	
	private Instant startTime;
	private Instant stopTime;
	
	private SimpleStringProperty description;
	private SimpleStringProperty start;
	private SimpleStringProperty stop;
	private SimpleStringProperty totalHours;
	
	public static Session fromResultSet(ResultSet rs) throws SQLException {
		Session result = new Session();
		result.setId(rs.getInt("id"));
		result.setDescription(rs.getString("description"));
		result.setStartTime(Instant.ofEpochMilli(rs.getLong("start")));
		result.setStopTime(Instant.ofEpochMilli(rs.getLong("stop")));
		return result;
	}
	
	public Session() {
		id = 0;
		startTime = Instant.now();
		stopTime = Instant.now();
		description = new SimpleStringProperty();
		start = new SimpleStringProperty();
		stop = new SimpleStringProperty();
		totalHours = new SimpleStringProperty();
	}
	
	public void refreshValues() {
		LocalDateTime startdt = getLocalStartTime();
		LocalDateTime stopdt = getLocalStopTime();
		
		setStart(startdt.format(DateTimeFormatter.ofPattern(TIME_FORMAT)));
		
		String stopFormat = TIME_FORMAT;
		if (stopdt.getDayOfYear() != startdt.getDayOfYear()) stopFormat = STOP_TIME_DIFFERENT_DAY_FORMAT; 
		setStop(stopdt.format(DateTimeFormatter.ofPattern(stopFormat)));
		
		setTotalHours(Seconds.hms(getPassedTime()));
	}
	
	public long getPassedTime() {
		return stopTime.getEpochSecond() - startTime.getEpochSecond();
	}
	
	public int getId() {
		return id;
	}
	
	public Instant getStartTime() {
		return startTime;
	}
	
	public LocalDateTime getLocalStartTime() {
		return LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
	}
	
	public Instant getStopTime() {
		return stopTime;
	}
	
	public LocalDateTime getLocalStopTime() {
		return LocalDateTime.ofInstant(stopTime, ZoneId.systemDefault());
	}
	
	public String getDescription() {
		return description.get();
	}
	
	public String getStart() {
		return start.get();
	}
	
	public String getStop() {
		return stop.get();
	}
	
	public String getTotalHours() {
		return totalHours.get();
	}
	
	public Session setId(int id) {
		this.id = id;
		return this;
	}
	
	public Session setStartTime(Instant startTime) {
		this.startTime = startTime;
		refreshValues();
		return this;
	}
	
	public Session setStopTime(Instant stopTime) {
		this.stopTime = stopTime;
		refreshValues();
		return this;
	}
	
	public Session setDescription(String description) {
		this.description.set(description);
		return this;
	}
	
	public Session setStart(String start) {
		this.start.set(start);
		return this;
	}
	
	public Session setStop(String stop) {
		this.stop.set(stop);
		return this;
	}
	
	public Session setTotalHours(String total) {
		this.totalHours.set(total);
		return this;
	}
	
	@Override
	public String toString() {
		return "<Session id=\"" + id + "\" description=\"" + description.get() + "\" start=\"" + getLocalStartTime() + "\" stop=\"" + getLocalStopTime() + "\"/>";
	}
}
