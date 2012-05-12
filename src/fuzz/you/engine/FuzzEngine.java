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
import java.util.Random;
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
    private static Random random;
    private static ArrayList<String> allVectorStrings = (ArrayList<String>) FuzzVectors.getAllVectorStrings();

    public static void main(String[] args) {
    	random = new Random();
    	
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
        }
        	
        //fuzz logged out pages
        System.out.println("Fuzzing pages while not logged in.");
        fuzzPages(webClient, false);
        
        // try guessing usernames and passwords
        if(Boolean.parseBoolean(properties.getProperty("PasswordGuessing"))) {
        	System.out.println("Guessing Passwords");
        	String[] usernames = FuzzVectors.getAttackClass("usernames");
        	String[] passwords = FuzzVectors.getAttackClass("passwords");
        	String uri = properties.getProperty("LoginURI");
        	
        	for(String username : usernames) {
        		for(String password : passwords) {
        			FuzzyCrawler.login(webClient, uri, username, password, true);
        		}
        	}
        }
        
        //fuzz logged in pages
        System.out.println("Fuzzing pages while logged in.");
        fuzzPages(webClient, true);
        
        FuzzyLogger.outputAttackSurface();
    }
    
    private static void systemExit(){
    	System.exit(1);
    }
    
    private static void fuzzPages(WebClient webClient, Boolean loggedIn){
    	// for every page we found while logged in, fuzz it
    	// note that the webClient should still be logged in if loggedIn is TRUE.
    	if(loggedIn) {
    		FuzzyCrawler.webClientLogin(properties, webClient);
    	} else {
    		try {
	        	HtmlPage page = webClient.getPage(properties.getProperty("LogoutURI"));
				page.getWebResponse();
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
        	if(properties.getProperty("Completeness").equals("full")) {
        		randomStrings = allVectorStrings;
        	}
        	else if(properties.getProperty("Completeness").equals("random")) {
        		randomStrings.add(RandomFuzzer.getRandomString());
        	}
        	
        	for(String randomString : randomStrings) {
	            // basic fuzzed url with just this param
        		String baseUrl = page.getUnescapedPageURL() + "?" + urlParam;
	            String urlFuzzed = baseUrl + "=" + randomString;
	            checkFuzzedURLWithParams(baseUrl, urlFuzzed, webClient);
	
	            // test every possible combination of url parameters
	            if (properties.getProperty("FullUrlParamFuzzing") != null
	                    && Boolean.parseBoolean(properties
	                            .getProperty("FullUrlParamFuzzing"))) {
	            	fuzzAllPossibleURLParamCombos(urlCombinations, urlFuzzed, urlParam, randomString, webClient);
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
            String modifiedURL = existingURL + "&" + urlParam + "=" + randomString;
            checkFuzzedURLWithParams(modifiedURL, modifiedURL, webClient); // THIS WONT LOG PROPERLY!
            newURLCombinations.add(modifiedURL);
            if (!existingURL.contains(urlParam)) {
                checkFuzzedURLWithParams(modifiedURL, modifiedURL, webClient);
                newURLCombinations.add(modifiedURL);
            }
        }
        urlCombinations.addAll(newURLCombinations);
    }

    private static void checkFuzzedURLWithParams(String baseUrl, String urlWithParams,
            WebClient webClient) {
        try {
        	ResultsProcessor.setLastInput(urlWithParams);
            HtmlPage checkedPage = webClient.getPage(urlWithParams);
            ResultsProcessor.processWebResponse(checkedPage.getWebResponse(), false, "");
            Thread.sleep(Long.parseLong(properties.getProperty("TimeDelaySec")) * 1000);
        } catch (FailingHttpStatusCodeException e) {
        	if(Boolean.parseBoolean(properties.getProperty("Log400"))) {
        		String key = baseUrl+"::FailingHttpStatusCode::";
        		ResultsProcessor.logError(key, key+e.getMessage());
        	}
        } catch (MalformedURLException e) {
        	String key = baseUrl+"::MalformedUrl::";
        	ResultsProcessor.logError(key, key+e.getMessage());
        } catch (IOException e) {
        	if(Boolean.parseBoolean(properties.getProperty("LogBadURI"))) {
        		String key = baseUrl+"::IO Exception::";
        		ResultsProcessor.logError(key, key+e.getMessage());
        	}
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

            // fuzz all inputs at once!
            for(int i=0; i<Integer.parseInt(properties.getProperty("FullFormFuzzLoops")); i++) {
            	fuzzAllInputsRandomly(form);
            }
        }
    }
    
    private static void fuzzAllInputsRandomly(FuzzyForm form) { 
    	for(HtmlSubmitInput submit : form.getSubmitButton()) {
	    	String fuzzedInputNames = "";
	    	String fuzzedInputs = "";
	    	for(HtmlElement input : form.getAllInputs()) {
	    		String inputName = input.getAttribute("name") + ";";
	    		fuzzedInputNames += inputName;
	    		
	    		String fuzzedValue = allVectorStrings.get(random.nextInt(allVectorStrings.size()));
	    		fuzzedInputs += inputName + "=" + fuzzedValue + ";";
	    		input.setAttribute("value", fuzzedValue);
	    	}
	    	ResultsProcessor.setLastInputName(fuzzedInputNames);
	    	ResultsProcessor.setLastInput(fuzzedInputs);
	    	submitForm(submit, "");
    	}

    }

    private static void fuzzInputWithAllVectors(HtmlElement input,
            List<HtmlSubmitInput> submits) {
        for (String vectorName : FuzzVectors.getAllVectorClasses()) {
        	for(HtmlSubmitInput submit : submits) {
        		fuzzInputWithStrings(input, submit,
                    FuzzVectors.getAttackClass(vectorName), vectorName);
        	}
        }
    }

    private static void fuzzInputWithStrings(HtmlElement input,
            HtmlSubmitInput submit, String[] strings, String attackVector) {
        for (String randomInput : strings) {
        	ResultsProcessor.setLastInput(input.getAttribute("name") + "=" + randomInput);
        	ResultsProcessor.setLastInputName(input.getAttribute("name"));
        	input.setAttribute("value", randomInput);
        	
        	submitForm(submit, attackVector);
        }
    }

    private static void submitForm(HtmlSubmitInput submit, String attackVector) {
    	try {
	    	Thread.sleep(Long.parseLong(properties.getProperty("TimeDelaySec")) * 1000);
	    	
	    	boolean noErrorIsError = false;
	    	if(attackVector.equals("xss") || attackVector.equals("activeSQL") || attackVector.equals("passiveSQL")) {
	    		noErrorIsError = true;
	    	}
	        ResultsProcessor.processWebResponse(submit.<HtmlPage> click()
	                .getWebResponse(), noErrorIsError, attackVector);
	
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
