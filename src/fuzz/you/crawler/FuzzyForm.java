package fuzz.you.crawler;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

public class FuzzyForm{

	private HtmlForm fuzzyForm;
	
	private List<HtmlElement> lostChildrenElements;
	//HTMLForm text to Input elements map
	
	protected FuzzyForm(HtmlForm htmlForms){
		
		 fuzzyForm = htmlForms;
		 discoverLostChildrenElements();
		 
	}
	
	private void discoverLostChildrenElements(){
		lostChildrenElements = fuzzyForm.getLostChildren();
	}
	
	
	//Form elements
	//Submit Button
	
	
}
