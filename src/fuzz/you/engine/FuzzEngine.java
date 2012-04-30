package fuzz.you.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

import fuzz.you.crawler.FuzzyCrawler;
import fuzz.you.crawler.FuzzyPage;

public class FuzzEngine {

    private static Properties properties;

    public static void main(String[] args) {
        // create web client
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
        webClient.setJavaScriptEnabled(true);

        loadProperties();

        // scrape to find pages
        try {
            // scrape logged out
            FuzzyCrawler.generatePagesNotLoggedIn(properties, webClient);

            // scrape logged in
            FuzzyCrawler.generatePagesLoggedIn(properties, webClient);
            
            
            System.out.println(FuzzyCrawler.getFuzzyPageMap(false));
            System.out.println(FuzzyCrawler.getFuzzyPageMap(true));
        } catch (URISyntaxException e) {
            System.exit(1);
        }

        // Now that we have the pages and their inputs, fuzz them
        // if(properties.getProperty("Completeness").equals("full")) {
        // for (URI key: siteURIs) {
        // FuzzyPage page = fuzzyPageMap.get(key);
        // FuzzEngine.fuzzURLParams(page);
        // FuzzEngine.fuzzFormInputs(page);
        // }
        // } else if(properties.getProperty("Completeness") == "random") {
        //
        // }

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

    public static void fuzzURLParams(FuzzyPage page) {
        loadProperties();

        List<String> urlCombinations = new ArrayList<String>();

        // for (String urlParam: page url parameters) {
        // for(int i=0; i <
        // Integer.parseInt(properties.getProperty("URIIterations")); i++) {
        // // generate a random string for the uri
        // random_string = stuff from kevin's method
        //
        // // basic fuzzed url with just this param
        // String urlFuzzed = page.getUnescapedPageURL() + "?" + urlParam + "="
        // + random_string
        //
        // // append this to every other URL for extensive testing
        // }
        // }

    }

    public static void fuzzFormInputs(FuzzyPage page) {
        loadProperties();

    }

}
