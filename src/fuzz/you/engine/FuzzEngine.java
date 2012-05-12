package fuzz.you.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import utils.FuzzVectors;
import utils.FuzzyLogger;
import utils.RandomFuzzer;
import utils.ResultsProcessor;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import fuzz.you.crawler.FuzzyCrawler;
import fuzz.you.crawler.FuzzyForm;
import fuzz.you.crawler.FuzzyPage;

public class FuzzEngine {

    private static Properties properties;

    public static void main(String[] args) {
        System.getProperties().put(
                "org.apache.commons.logging.simplelog.defaultlog", "error");

        loadProperties();
        pageDiscovery();
    }
    
    private static void pageDiscovery(){
    	// create web client
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
        webClient.setJavaScriptEnabled(true);
        webClient.setThrowExceptionOnScriptError(false);
        webClient.setPrintContentOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        
        if (properties.getProperty("PageDiscovery").equals("True")) {
            try {
            	// scrape logged out
				FuzzyCrawler.generatePagesNotLoggedIn(properties, webClient);
				
				// scrape logged in
	            FuzzyCrawler.generatePagesLoggedIn(properties, webClient);
	            
	            // dump discovered URIs to file as serialized data
	            FuzzyCrawler.dumpDiscoveredURIsToFile();
	            
			} catch (URISyntaxException e) {
	            systemExit();
	        }
        }else{
	        try{
	        	FuzzyCrawler.loadURIsFromFile();
	        	
	        }catch(FileNotFoundException fnfe){
	     	   System.err.println("WARN: -----------------------------------------");
	     	   System.err.println("WARN: The file '" + FuzzyCrawler.URI_FILE + "' wansn't found.\n");
	     	   System.err.println("WARN: If this is the first time running this application,");
	     	   System.err.println("WARN: be sure page discovery has been set to 'True' in the");
	     	   System.err.println("WARN: property configuration file.\n");
	     	   System.err.println("WARN: Nothing to import.");
	     	   System.err.println("WARN: -----------------------------------------");
	     	   
	     	   try {
	 			Thread.sleep(5000);
	 			
	     	   }catch (InterruptedException e) {
		 			System.err.println("WARN: -----------------------------------------");
		 	    	System.err.println("WARN: Thread sleep error");
		 	    	e.printStackTrace();
		 	    	System.err.println("WARN: -----------------------------------------");
	     	   }
	     	   systemExit();
	        }
	        FuzzyCrawler.webClientLogin(properties, webClient);
        }
        	
        //fuzz logged out pages
        fuzzPages(webClient, false);
        //fuzz logged in pages
        fuzzPages(webClient, true);
    }
    
    private static void systemExit(){
    	System.exit(1);
    }
    
    private static void fuzzPages(WebClient webClient, Boolean loggedIn){
    	// for every page we found while logged in, fuzz it
    	// note that the webClient should still be logged in if loggedIn is TRUE.
    	for (FuzzyPage page : FuzzyCrawler.getFuzzyPageMap(loggedIn).values()) {
        	fuzzURLParams(page, webClient);
        	fuzzFormInputs(page);
    	}
    }

    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                properties.load(new FileInputStream(
                        "config/FuzzyCrawlerConfig.prop"));
            } catch (FileNotFoundException e) {
                System.err.println("Can't load properties file: "
                        + e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                System.err.println("IOException loading properties file: "
                        + e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void fuzzURLParams(FuzzyPage page, WebClient webClient) {
        List<String> urlCombinations = new ArrayList<String>();

        for (String urlParam : page.getAllURLParamsNoValues()) {
            // generate a random string for the uri
            List<String> randomStrings = new ArrayList<String>();
            if (properties.getProperty("Completeness").equals("full")) {
                randomStrings = FuzzVectors.getAllVectorStrings();
            } else if (properties.getProperty("Completeness")
                    .equals("random")) {
                randomStrings.add(RandomFuzzer.getRandomString());
            }

            for (String randomString : randomStrings) {
                // basic fuzzed url with just this param
                String pageToFuzz = page.getUnescapedPageURL();
                if (pageToFuzz.endsWith("/")) {
                    pageToFuzz = pageToFuzz.substring(0,
                            pageToFuzz.length() - 1);
                }

                String urlFuzzed = pageToFuzz + "?" + urlParam + "="
                        + randomString;

                checkFuzzedURLWithParams(urlFuzzed, webClient);

                // test every possible combination of url parameters
                if (properties.getProperty("FullUrlParamFuzzing") != null
                        && Boolean.parseBoolean(properties
                                .getProperty("FullUrlParamFuzzing"))) {
                    fuzzAllPossibleURLParamCombos(urlCombinations, urlFuzzed,
                            urlParam, randomString, webClient);
                }
            }
        }
    }

    private static void fuzzAllPossibleURLParamCombos(
            List<String> urlCombinations, String urlFuzzed, String urlParam,
            String randomString, WebClient webClient) {
        Set<String> newURLCombinations = new HashSet<String>();
        newURLCombinations.add(urlFuzzed);

        for (String existingURL : urlCombinations) {
            if (!existingURL.contains(urlParam)) {
                String modifiedURL = existingURL + "&" + urlParam + "="
                        + randomString;
                checkFuzzedURLWithParams(modifiedURL, webClient);
                newURLCombinations.add(modifiedURL);
            }
        }
        urlCombinations.addAll(newURLCombinations);
    }

    private static void checkFuzzedURLWithParams(String urlWithParams,
            WebClient webClient) {
        try {
            HtmlPage checkedPage = webClient.getPage(urlWithParams);
            ResultsProcessor.processWebResponse(checkedPage.getWebResponse());
            Thread.sleep(Long.parseLong(properties
                    .getProperty("TimeDelaySec")) * 1000);
        } catch (FailingHttpStatusCodeException e) {
            FuzzyLogger.logError(e.getMessage());
        } catch (MalformedURLException e) {
            FuzzyLogger.logError(e.getMessage());
        } catch (IOException e) {
            FuzzyLogger.logError(e.getMessage());
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void fuzzFormInputs(FuzzyPage page) {
        for (FuzzyForm form : page.getAllForms()) {
            // try fuzzing one input at a time
            for (HtmlElement input : form.getAllInputs()) {
                // fuzz all items of each attack class
                fuzzInputWithAllVectors(input, form.getSubmitButton());
            }

            // try fuzzing combinations of inputs
            // R2?
        }
    }

    private static void fuzzInputWithAllVectors(HtmlElement input,
            List<HtmlSubmitInput> submits) {
        for (String vectorName : FuzzVectors.getAllVectorClasses()) {
            for (HtmlSubmitInput submit : submits) {
                fuzzInputWithStrings(input, submit,
                        FuzzVectors.getAttackClass(vectorName));
            }
        }
    }

    private static void fuzzInputWithStrings(HtmlElement input,
            HtmlSubmitInput submit, String[] strings) {
        for (String randomInput : strings) {
            input.setAttribute("value", randomInput);
            try {
                ResultsProcessor.processWebResponse(submit.<HtmlPage> click()
                        .getWebResponse());
                Thread.sleep(Long.parseLong(properties
                        .getProperty("TimeDelaySec")) * 1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
