package fuzz.you.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;


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
        System.out.println("Added credntials");
        
        try {
	        webClient.setJavaScriptEnabled(false);
	        HtmlPage page1 = webClient.getPage("http://www.facebook.com");
	        HtmlForm form = page1.getForms().get(0);
	        HtmlSubmitInput button = (HtmlSubmitInput) form.getInputsByValue("Log In").get(0);
	        HtmlTextInput textField = form.getInputByName("email");
	        textField.setValueAttribute(properties.getProperty("UserName"));
	        HtmlPasswordInput textField2 = form.getInputByName("pass");
	        textField2.setValueAttribute(properties.getProperty("Password"));
			HtmlPage newindex = button.click();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        webClient.setJavaScriptEnabled(true);
//        DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient
//                .getCredentialsProvider();
//        credentialsProvider.addCredentials(
//                properties.getProperty("UserName"),
//                properties.getProperty("Password"));
//        
//		try {
//			HtmlPage loginPage = webClient.getPage(properties.getProperty("LoginURI"));
//	        List<HtmlForm> forms = loginPage.getForms();
//	        for (HtmlForm form : forms) {
//	                HtmlInput username = form.getInputByName("email");
//	                username.setValueAttribute(properties.getProperty("UserName"));
//	                HtmlInput password = form.getInputByName("pass");
//	                password.setValueAttribute(properties.getProperty("Password"));
//	                
//	                HtmlSubmitInput submit = (HtmlSubmitInput) form.getFirstByXPath("//input[@type='submit']");
//	                System.out.println(submit.<HtmlPage> click().getWebResponse().getContentAsString());
//	        }
//		} catch (FailingHttpStatusCodeException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        
        URI baseURI = generateBasicPageURI(properties.getProperty("BaseURI"));
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