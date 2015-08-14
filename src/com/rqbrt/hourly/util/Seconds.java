package com.rqbrt.hourly.util;

public class Seconds {

	public static String hms(long seconds) {
		int s = (int) (seconds % 60);
		int m = (int) (seconds / 60) % 60;
		int h = (int) (seconds / 3600);
		return String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s);
	}
}
