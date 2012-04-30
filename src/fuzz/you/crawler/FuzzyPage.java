package fuzz.you.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

public class FuzzyPage {

	private Page fuzzyPage;
	private URI baseURI;
	private String UserName = null;
	private String Password = null;
	private HashSet<URI> fuzzyPageURIsSet;
	private List<FuzzyForm> fuzzyPageForms;

	// Each page -> URL Params as collection
	// UserName & Pass Params
	//

	protected FuzzyPage(Page page) {

		fuzzyPage = page;
		fuzzyPageForms = new ArrayList<FuzzyForm>();
		fuzzyPageURIsSet = new HashSet<URI>();

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
				if(link.getHrefAttribute().equals("")) {
					HtmlPage nextPage = link.click();
					discoveredURI = nextPage.getUrl().toURI();
					System.out.println(discoveredURI);
				}
				else {
					String uriHref = link.getHrefAttribute().replace(" ", "%20");
					discoveredURI = new URI(uriHref);
				}
				
				String discoveredHost = discoveredURI.getHost();

				if ((discoveredHost != null && baseHost.compareTo(discoveredHost) == 0)
						|| (discoveredHost == null && (discoveredURI.getPath() != null && discoveredURI.getPath().length() > 0))
						) {

					URI resolvedURI = baseURI.resolve(discoveredURI);
					URI strippedResolvedURI = new URI(resolvedURI.getScheme(), resolvedURI.getAuthority(), resolvedURI.getPath(), null, null);
					fuzzyPageURIsSet.add(strippedResolvedURI);
				}
			}
		} catch (ClassCastException cce) {
			System.err.println("Invalid Class Cast: " + cce.getMessage());
		} catch (Exception horriblePractice) {
			System.err.println("Yeah, we're screwed: " + horriblePractice.getMessage());
		}
	}
	// private Collection<T> inputs;

	// Property URL Parameters - ? &
	// Collection of Forms

}
