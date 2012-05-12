package fuzz.you.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

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
        if (loggedIn) {
            return loggedinFuzzyPageMap;
        } else {
            return fuzzyPageMap;
        }
    }

    public static void generatePagesNotLoggedIn(Properties properties,
            WebClient webClient) throws URISyntaxException {
        URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
        generatePages(baseURI, webClient, false,
                Long.parseLong(properties.getProperty("TimeDelaySec")) * 1000);
    }

    public static void generatePagesLoggedIn(Properties properties,
            WebClient webClient) throws URISyntaxException {
        System.out.println("Added credentials");

        DefaultCredentialsProvider prov = (DefaultCredentialsProvider) webClient
                .getCredentialsProvider();
        prov.addCredentials(properties.getProperty("UserName"),
                properties.getProperty("Password"));

        webClient.setCredentialsProvider(prov);

        login(webClient, properties.getProperty("LoginURI"),
                properties.getProperty("UserName"),
                properties.getProperty("Password"));

        webClient.setJavaScriptEnabled(true);

        URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
        generatePages(baseURI, webClient, true,
                Long.parseLong(properties.getProperty("TimeDelaySec")) * 1000);
    }

    private static void login(WebClient webClient, String uri,
            String username, String password) {

        try {
            HtmlPage page = webClient.getPage(uri);
            HtmlElement elem = page.getElementById("username");
            elem.setAttribute("value", username);
            HtmlElement elem1 = page.getElementById("password");
            elem1.setAttribute("value", password);

            HtmlSubmitInput button = (HtmlSubmitInput) page.getByXPath(
                    "//input[@type='submit']").get(0);

            button.click();

        } catch (FailingHttpStatusCodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void generatePages(URI pageURI, WebClient webClient,
            Boolean loggedIn, Long delay) {
        // Page not yet scraped
        if (!mapForPagesFoundByLoginStatus.get(loggedIn).containsKey(pageURI)) {
            try {
                HtmlPage page = webClient.getPage(pageURI.toString());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                FuzzyPage fuzzyPage = new FuzzyPage(page);

                mapForPagesFoundByLoginStatus.get(loggedIn).put(pageURI,
                        fuzzyPage);

                // Scrape page
                for (URI uri : fuzzyPage.getAllPageURIs()) {
                    // Check if it has been scraped already.
                    if (!mapForPagesFoundByLoginStatus.get(loggedIn)
                            .containsKey(uri)) {
                        generatePages(uri, webClient, loggedIn, delay);
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