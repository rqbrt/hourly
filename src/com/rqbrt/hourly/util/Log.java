package com.rqbrt.hourly.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

	public static String LOG_FILE_PATH = "log.txt";
	
	public static void write(String msg, String filePath) {
		msg = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + msg;
		System.out.println(msg);
		try {
			FileIO.write(msg + System.lineSeparator(), filePath, true);
		} catch (IOException e) {
			System.err.println("Could not write to log file (" + e.getMessage() + ")");
		}
	}
	
	public static void msg(String msg) {
		write(msg, LOG_FILE_PATH);
	}
	
	public static void exception(String msg, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		write("[EXCEPTION] " + msg + "\n" + sw.toString(), LOG_FILE_PATH);
	}
}
