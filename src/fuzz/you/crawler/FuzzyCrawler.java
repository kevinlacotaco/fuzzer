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
    private static HashSet<URI> siteURIs = new HashSet<URI>();
    private static URI baseURI;

    public static void generatePagesNotLoggedIn(Properties properties,
            WebClient webClient) throws URISyntaxException {
        baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
        generatePages(baseURI, webClient);
    }

    public static void generatePagesLoggedIn(Properties properties,
            WebClient webClient) throws URISyntaxException {
        DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient
                .getCredentialsProvider();
        credentialsProvider.addCredentials(
                properties.getProperty("UserName"),
                properties.getProperty("Password"));
        baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));

        System.out.println("Added credntials");
        generatePages(baseURI, webClient);
    }

    public static HashMap<URI, FuzzyPage> getFuzzyPageMap() {
        return fuzzyPageMap;
    }

    public static HashSet<URI> getSiteURIs() {
        return siteURIs;
    }

    private static void generatePages(URI pageURI, WebClient webClient) {
        // Page not yet scraped
        if (!fuzzyPageMap.containsKey(pageURI)) {
            try {
                FuzzyPage fuzzyPage = new FuzzyPage(webClient.getPage(pageURI
                        .toString()));
                fuzzyPageMap.put(pageURI, fuzzyPage);

                // Scrape page
                for (URI uri : fuzzyPage.getAllPageURIs()) {

                    // Check if it has been scraped already.
                    if (!fuzzyPageMap.containsKey(uri)) {
                        try {
                            siteURIs.add(generateBasicPageURI(uri));
                            System.out.println("URI: " + pageURI);
                            generatePages(uri, webClient);
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
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