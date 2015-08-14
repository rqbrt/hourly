package com.rqbrt.hourly.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileIO {

	public static void write(String msg, String path, boolean append) throws IOException {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));
			writer.print(msg);
		} catch (IOException e) {
			throw new IOException("Could not write to file (" + e.getMessage() + ")");
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
	public static List<String> read(String path) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			List<String> lines = new ArrayList<String>();
			String line;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} catch (IOException e) {
			System.err.println("Could not read file: " + e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				System.err.println("Could not close file reader: " + e.getMessage());
			}
		}
		
		return null;
	}
}
