package fuzz.you.crawler;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

public class FuzzyCrawler {

	private static Properties properties = new Properties();

	/**
	 * A set containing all of the pages found on the system.
	 */
	private static HashMap<URI, FuzzyPage> fuzzyPageMap = new HashMap<URI, FuzzyPage>();

	private static HashSet<URI> siteURIs = new HashSet<URI>();

	private static URI baseURI;

	public static void main(String[] args) {

		try {
			properties.load(new FileInputStream(
					"config/FuzzyCrawlerConfig.prop"));

			try {
				baseURI = generateBasicPageURI(properties
						.getProperty("BaseURI"));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			generatePages(baseURI);

//			for (URI u : siteURIs) {
//				System.out.println(u.toString());
//			}
//
//			System.out.println(siteURIs.size());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void generatePages(URI pageURI) {

		// Page not yet scraped
		if (!fuzzyPageMap.containsKey(pageURI)) {
			try {
				WebClient webClient = new WebClient();
				webClient.setJavaScriptEnabled(true);
				FuzzyPage fuzzyPage = new FuzzyPage(webClient.getPage(pageURI.toString()));

				fuzzyPageMap.put(fuzzyPage.getPageURI(), fuzzyPage);

				// Scrape page
				for (URI uri : fuzzyPage.getAllPageURIs()) {
					
					//Check if it has been scraped already.
					if (!fuzzyPageMap.containsKey(uri)) {
						try {
							siteURIs.add(generateBasicPageURI(uri));
							System.out.println("URI: " + pageURI);
							generatePages(uri);
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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