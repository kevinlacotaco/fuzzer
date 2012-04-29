package fuzz.you.crawler;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import com.gargoylesoftware.htmlunit.WebClient;


public class FuzzyCrawler {
	/**
	 * A set containing all of the pages found on the system.
	 */
	private static HashMap<URI, FuzzyPage> fuzzyPageMap = new HashMap<URI, FuzzyPage>();
	private static HashSet<URI> siteURIs = new HashSet<URI>();
	
	private static HashMap<URI, FuzzyPage> loggedinFuzzyPageMap = new HashMap<URI, FuzzyPage>();
	private static HashSet<URI> loggedinSiteURIs = new HashSet<URI>();
	
	private static HashMap<Boolean, HashMap<URI, FuzzyPage>> mapMap = new HashMap<Boolean, HashMap<URI, FuzzyPage>>();
	private static HashMap<Boolean, HashSet<URI>> setMap = new HashMap<Boolean, HashSet<URI>>();

	public static HashMap<URI, FuzzyPage> getFuzzyPageMap(boolean loggedIn) {
		if(loggedIn) {
			return loggedinFuzzyPageMap;
		} else {
			return fuzzyPageMap;
		}
	}
	
	public static HashSet<URI> getSiteURIs(boolean loggedIn) {
		if(loggedIn) {
			return loggedinSiteURIs;
		} else {
			return siteURIs;
		}
	}
	
	public static void generatePagesNotLoggedIn(Properties properties, WebClient webClient) throws URISyntaxException {
		URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
		generatePages(baseURI, webClient, false);
	}
	
	public static void generatePagesLoggedIn(Properties properties, WebClient webClient) throws URISyntaxException {
		
	}
	
	private static void generatePages(URI pageURI, WebClient webClient, Boolean loggedIn) {
		// Page not yet scraped
		if (!mapMap.get(loggedIn).containsKey(pageURI)) {
			try {
				FuzzyPage fuzzyPage = new FuzzyPage(webClient.getPage(pageURI.toString()));
				mapMap.get(loggedIn).put(pageURI, fuzzyPage);

				// Scrape page
				for (URI uri : fuzzyPage.getAllPageURIs()) {
					
					//Check if it has been scraped already.
					if (!mapMap.get(loggedIn).containsKey(uri)) {
						try {
							setMap.get(loggedIn).add(generateBasicPageURI(uri));
							System.out.println("URI: " + pageURI);
							generatePages(uri, webClient, loggedIn);
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static URI generateBasicPageURI(URI fullURI)
			throws URISyntaxException {
		// (scheme:)([user-info@]host[:port])([path][?query])
		URI baseURI = new URI(fullURI.getScheme(),
				fullURI.getSchemeSpecificPart(), null);
		return baseURI;
	}

	private static URI generateBasicPageURI(String fullURI)
			throws URISyntaxException {
		// (scheme:)([user-info@]host[:port])([path][?query])
		URI baseURI = new URI(fullURI);
		return baseURI;
	}
}