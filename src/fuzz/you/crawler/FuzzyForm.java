package fuzz.you.crawler;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class FuzzyForm{

	private HtmlForm fuzzyForm;
	
	private List<HtmlElement> lostChildrenElements;
	//HTMLForm text to Input elements map
	
	protected FuzzyForm(HtmlForm htmlForms){
		
		 fuzzyForm = htmlForms;
		 discoverLostChildrenElements();
		 
	}
	
	public List<HtmlInput> getAllInputs() {
		return new ArrayList<HtmlInput>();
	}
	
	public HtmlSubmitInput getSubmitButton() {
		return null;
	}
	
	private void discoverLostChildrenElements(){
		lostChildrenElements = fuzzyForm.getLostChildren();
	}
	
	
	//Form elements
	//Submit Button
	
	
}
