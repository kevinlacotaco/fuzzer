package fuzz.you.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;


public class FuzzyCrawler {
	/**
	 * A set containing all of the pages found on the system.
	 */
	private static HashMap<URI, FuzzyPage> fuzzyPageMap = new HashMap<URI, FuzzyPage>();	
	private static HashMap<URI, FuzzyPage> loggedinFuzzyPageMap = new HashMap<URI, FuzzyPage>();
	private static HashMap<Boolean, HashMap<URI, FuzzyPage>> mapForPagesFoundByLoginStatus = new HashMap<Boolean, HashMap<URI, FuzzyPage>>();
	
	static {
		mapForPagesFoundByLoginStatus.put(false, fuzzyPageMap);
		mapForPagesFoundByLoginStatus.put(true, loggedinFuzzyPageMap);
	}

	public static HashMap<URI, FuzzyPage> getFuzzyPageMap(boolean loggedIn) {
		if(loggedIn) {
			return loggedinFuzzyPageMap;
		} else {
			return fuzzyPageMap;
		}
	}
	
	public static void generatePagesNotLoggedIn(Properties properties, WebClient webClient) throws URISyntaxException {
		URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
		generatePages(baseURI, webClient, false);
	}
	
	public static void generatePagesLoggedIn(Properties properties,
            WebClient webClient) throws URISyntaxException {
        DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient
                .getCredentialsProvider();
        credentialsProvider.addCredentials(
                properties.getProperty("UserName"),
                properties.getProperty("Password"));
        URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));

        System.out.println("Added credntials");
        generatePages(baseURI, webClient, true);
    }
	
	private static void generatePages(URI pageURI, WebClient webClient, Boolean loggedIn) {
		// Page not yet scraped
		if (!mapForPagesFoundByLoginStatus.get(loggedIn).containsKey(pageURI)) {
			try {
				FuzzyPage fuzzyPage = new FuzzyPage(webClient.getPage(pageURI.toString()));
				mapForPagesFoundByLoginStatus.get(loggedIn).put(pageURI, fuzzyPage);
				System.out.println("adding a page: " + mapForPagesFoundByLoginStatus.get(loggedIn));

				// Scrape page
				for (URI uri : fuzzyPage.getAllPageURIs()) {
					
					//Check if it has been scraped already.
					if (!mapForPagesFoundByLoginStatus.get(loggedIn).containsKey(uri)) {
						System.out.println("URI: " + pageURI);
						generatePages(uri, webClient, loggedIn);
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