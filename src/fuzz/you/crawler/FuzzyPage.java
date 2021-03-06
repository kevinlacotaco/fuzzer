package fuzz.you.crawler;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

@SuppressWarnings("serial")
public class FuzzyPage implements Serializable {

    private Page fuzzyPage;
    private URI baseURI;
    private HashSet<URI> fuzzyPageURIsSet;
    private List<FuzzyForm> fuzzyPageForms;
    private Map<String, List<String>> urlParams;
    private boolean guessed;

    protected FuzzyPage(Page page) {

        fuzzyPage = page;
        fuzzyPageForms = new ArrayList<FuzzyForm>();
        fuzzyPageURIsSet = new HashSet<URI>();
        urlParams = new HashMap<String, List<String>>();
        guessed = false;

        try {
            baseURI = fuzzyPage.getUrl().toURI();
            discoverPageURIs();
            discoverForms();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO: Determine if needed
        // Map<String, String> namespaces = fuzzyPage.getNamespaces();
    }

    public void setGuessed(boolean guessed) {
        this.guessed = guessed;
    }

    public boolean getGuessed() {
        return guessed;
    }

    // private void debugging(){
    // System.out.println("URL: " + getPageURL());
    // System.out.println("URL: " + getUnescapedPageURL() + "\n");
    // for (HtmlAnchor link : pageAnchors) {
    // System.out.println("Link discovered: \n\t" + link.asText() +
    // "\n\t@URL=" + link.getHrefAttribute()+
    // "\n\t"
    //
    // );
    // }
    // }
    public String toString() {
        return baseURI.toString();
    }

    public URI getPageURI() {
        return baseURI;
    }

    public String getUnescapedPageURL() {
        return UrlUtils.decode(fuzzyPage.getUrl().toExternalForm());
    }

    public HashSet<URI> getAllPageURIs() {
        return fuzzyPageURIsSet;
    }

    public Set<String> getAllURLParamsNoValues() {
        return urlParams.keySet();
    }

    public List<FuzzyForm> getAllForms() {
        return fuzzyPageForms;
    }

    private void discoverForms() {
        try {
            HtmlPage castPage = (HtmlPage) fuzzyPage;
            for (HtmlForm f : castPage.getForms()) {
                fuzzyPageForms.add(new FuzzyForm(f));
            }
        } catch (ClassCastException cce) {
            System.err.println("Invalid Class Cast: " + cce.getMessage());
        }

    }

    private void discoverPageURIs() throws URISyntaxException {
        try {
            HtmlPage castPage = (HtmlPage) fuzzyPage;
            List<HtmlAnchor> pageAnchors = castPage.getAnchors();
            String baseHost = baseURI.getHost();

            for (HtmlAnchor link : pageAnchors) {
                URI discoveredURI;
                if (link.getHrefAttribute().equals("")) {
                    HtmlPage nextPage = link.click();
                    discoveredURI = nextPage.getUrl().toURI();
                    System.out.println(discoveredURI);
                } else {
                    String uriHref = link.getHrefAttribute().replace(" ",
                            "%20");
                    discoveredURI = new URI(uriHref);
                }

                String discoveredHost = discoveredURI.getHost();

                if ((discoveredHost != null && baseHost
                        .compareTo(discoveredHost) == 0)
                        || (discoveredHost == null && (discoveredURI
                                .getPath() != null && discoveredURI.getPath()
                                .length() > 0))) {

                    URI resolvedURI = baseURI.resolve(discoveredURI);
                    URI strippedResolvedURI = new URI(
                            resolvedURI.getScheme(),
                            resolvedURI.getAuthority(),
                            resolvedURI.getPath(), null, null);

                    String query = resolvedURI.getRawQuery();

                    if (query != null) {
                        String[] params = query.split("&");

                        for (String param : params) {
                            String[] values = param.split("=");

                            if (urlParams.containsKey(values[0])) {
                                urlParams.get(values[0]).add(values[1]);
                            } else {
                                ArrayList<String> valid = new ArrayList<String>();
                                valid.add(values[1]);

                                urlParams.put(values[0], valid);
                            }
                        }
                    }

                    fuzzyPageURIsSet.add(strippedResolvedURI);
                }
            }
        } catch (ClassCastException cce) {
            System.err.println("Invalid Class Cast: " + cce.getMessage());
        } catch (Exception horriblePractice) {
            System.err.println("Yeah, we're screwed: "
                    + horriblePractice.getMessage());
        }
    }
    // private Collection<T> inputs;

    // Property URL Parameters - ? &
    // Collection of Forms

}
