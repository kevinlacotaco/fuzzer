package utils;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

import fuzz.you.crawler.FuzzyCrawler;
import fuzz.you.crawler.FuzzyForm;
import fuzz.you.crawler.FuzzyPage;

public class FuzzyLogger {

	public static void logError(String message) {
		System.err.println("SE331_LOGGED::" + new Date() + "::" + message);
	}
	
	public static void outputAttackSurface() {
		System.out.println("==========================");
		System.out.println("Application Attack Surface");
		
		HashMap<URI, FuzzyPage> loggedOutPages = FuzzyCrawler.getFuzzyPageMap(false);
		HashMap<URI, FuzzyPage> loggedInPages = FuzzyCrawler.getFuzzyPageMap(true);
				
		System.out.println("  Pages Found Without Login:");
		for (FuzzyPage page : loggedOutPages.values() ) {
			System.out.println("   " + page);
			System.out.println("     URL Params: ");
			for (String param : page.getAllURLParamsNoValues()) {
				System.out.println("       " + param);
			}
			System.out.println("     HTML Forms: ");
			for (FuzzyForm form : page.getAllForms()) {
				System.out.println("       " + form.toString());
				for (HtmlElement input : form.getAllInputs()) {
					System.out.println("         input: " + input.getAttribute("name"));
				}
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		System.out.println("  Pages Found With Login:");
		for (FuzzyPage page : loggedInPages.values() ) {
			System.out.println("   " + page);
			System.out.println("     URL Params: ");
			for (String param : page.getAllURLParamsNoValues()) {
				System.out.println("       " + param);
			}
			System.out.println("     HTML Forms: ");
			for (FuzzyForm form : page.getAllForms()) {
				System.out.println("       " + form.toString());
				for (HtmlElement input : form.getAllInputs()) {
					System.out.println("         input: " + input.getAttribute("name"));
				}
			}
			System.out.println();
		}

	}
	
}
