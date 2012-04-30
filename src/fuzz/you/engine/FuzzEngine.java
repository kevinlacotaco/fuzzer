package fuzz.you.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import utils.FuzzVectors;
import utils.ResultsProcessor;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import fuzz.you.crawler.FuzzyCrawler;
import fuzz.you.crawler.FuzzyForm;
import fuzz.you.crawler.FuzzyPage;

public class FuzzEngine {

    private static Properties properties;

    public static void main(String[] args) {
        // create web client
        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
        webClient.setJavaScriptEnabled(true);
        webClient.setThrowExceptionOnScriptError(false);
        webClient
                .setAjaxController(new NicelyResynchronizingAjaxController());

        loadProperties();

        // scrape to find pages
        try {
            // scrape logged out
            FuzzyCrawler.generatePagesNotLoggedIn(properties, webClient);
            System.out.println("Pages found not logged in:"
                    + FuzzyCrawler.getFuzzyPageMap(false));

            // for every page we found logged out, fuzz it
            for (FuzzyPage page : FuzzyCrawler.getFuzzyPageMap(false)
                    .values()) {
                fuzzURLParams(page, webClient);
                fuzzFormInputs(page);
            }

            // scrape logged in
            FuzzyCrawler.generatePagesLoggedIn(properties, webClient);

            // for every page we found while logged in, fuzz it
            // note that the webClient should still be logged in
            for (FuzzyPage page : FuzzyCrawler.getFuzzyPageMap(true).values()) {
                fuzzURLParams(page, webClient);
                fuzzFormInputs(page);
            }
        } catch (URISyntaxException e) {
            System.exit(1);
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
        loadProperties();

        List<String> urlCombinations = new ArrayList<String>();

        for (String urlParam : page.getAllURLParamsNoValues()) {
            // generate a random string for the uri
            String random_string = "asdf"; // TODO: get real random string

            // basic fuzzed url with just this param
            String urlFuzzed = page.getUnescapedPageURL() + "?" + urlParam
                    + "=" + random_string;
            checkFuzzedURLWithParams(urlFuzzed, webClient);

            // test every possible combination of url parameters
            if (properties.getProperty("FullUrlParamFuzzing") != null
                    && Boolean.parseBoolean(properties
                            .getProperty("FullUrlParamFuzzing"))) {
                List<String> newURLCombinations = new ArrayList<String>();
                newURLCombinations.add(urlFuzzed);
                for (String existingURL : urlCombinations) {
                    String modifiedURL = existingURL + "&" + urlParam + "="
                            + random_string;
                    checkFuzzedURLWithParams(modifiedURL, webClient);
                    newURLCombinations.add(modifiedURL);
                }

                urlCombinations.addAll(newURLCombinations);
            }
        }
    }

    private static void checkFuzzedURLWithParams(String urlWithParams,
            WebClient webClient) {
        try {
            HtmlPage checkedPage = webClient.getPage(urlWithParams);
            ResultsProcessor.process(checkedPage.asText());
        } catch (FailingHttpStatusCodeException e) {
            e.printStackTrace(); // TODO: if necessary, store results!
        } catch (MalformedURLException e) {
            e.printStackTrace(); // TODO: if necessary, store results!
        } catch (IOException e) {
            e.printStackTrace(); // TODO: if necessary, store results!
        }
    }

    private static void fuzzFormInputs(FuzzyPage page) {
        loadProperties();
        for (FuzzyForm form : page.getAllForms()) {
            // try fuzzing one input at a time
            for (HtmlInput input : form.getAllInputs()) {
                // fuzz all items of each attack class
                fuzzInputWithAllVectors(input, form.getSubmitButton());
            }

            // try fuzzing combinations of inputs
            // R2?
        }
    }

    private static void fuzzInputWithAllVectors(HtmlInput input,
            HtmlSubmitInput submit) {
        for (String vectorName : FuzzVectors.getAllVectors()) {
            fuzzInputWithStrings(input, submit,
                    FuzzVectors.getAttackClass(vectorName));
        }
    }

    private static void fuzzInputWithStrings(HtmlInput input,
            HtmlSubmitInput submit, String[] strings) {
        for (String randomInput : strings) {
            input.setValueAttribute(randomInput);
            try {
                ResultsProcessor.process(submit.<HtmlPage> click()
                        .getWebResponse().getContentAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
