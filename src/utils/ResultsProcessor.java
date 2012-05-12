package utils;

import java.util.HashSet;
import java.util.Set;

import com.gargoylesoftware.htmlunit.WebResponse;

public class ResultsProcessor {
	
	private static String lastInput = "";
	private static String lastInputName = "";
	private static Set<String> alreadyLogged = new HashSet<String>();
	
	public static void setLastInput(String input) {
		lastInput = input;
	}
	
	public static void setLastInputName(String input) {
		lastInputName = input;
	}
	
	private static boolean wasAlreadyFound(String message) {
		// message is in format:
		// URL::[exceptionName|param_name]
		return alreadyLogged.contains(message);
	}
	
	public static void logError(String key, String exceptionMessage) {
		if(!wasAlreadyFound(key)) {
			FuzzyLogger.logError(exceptionMessage);
			alreadyLogged.add(key);
		}
	}
	
	public static void processWebResponse(WebResponse response, boolean noErrorIsError, String attackVector) {
		String key = response.getWebRequest().getUrl() + "::" + lastInputName + "::";
		
		if(response.getStatusCode() > 399) {
			if (!wasAlreadyFound(key)) {
				FuzzyLogger.logError(key + lastInput + "::response time==" + response.getLoadTime() + "ms::status code=" + response.getStatusCode() + "::message=" + response.getStatusMessage());
				alreadyLogged.add(key);
			}
		}
		
		if(response.getContentAsString().contains("error")) {
			if(!wasAlreadyFound(key)){
				FuzzyLogger.logError(key + lastInput + "::response time==" + response.getLoadTime() + "ms:: Encountered 'System Error'");
				alreadyLogged.add(key);
			}
		} else {
			if(noErrorIsError) {
				key += "::" + attackVector;
				if(!wasAlreadyFound(key)){
					FuzzyLogger.logError(key + lastInput + "::response time==" + response.getLoadTime() + "ms:: NO SYSTEM ERROR when expected for attack vector: " + attackVector);
					alreadyLogged.add(key);
				}
			}
		}
	}
	
}
