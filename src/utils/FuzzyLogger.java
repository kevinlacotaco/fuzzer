package utils;

import java.util.Date;

public class FuzzyLogger {

	public static void logError(String message) {
		System.err.println("SE331_LOGGED: " + new Date() + "::" + message);
	}
	
}
