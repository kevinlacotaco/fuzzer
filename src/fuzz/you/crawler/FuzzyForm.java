package fuzz.you.crawler;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.NamedNodeMap;

/**
 * Represents an HTML form within an HTML page.
 * 
 * @author
 *
 */
public class FuzzyForm{

	private HtmlForm fuzzyForm;
	private List<HtmlElement> lostChildrenElements;
	
	//Form Inputs ------FUTURE USE---------
//	private ArrayList<HtmlFileInput> fileInputs = new ArrayList<HtmlFileInput>();
//	private ArrayList<HtmlHiddenInput> hiddenInputs = new ArrayList<HtmlHiddenInput>();
//	private ArrayList<HtmlImageInput> imageInputs = new ArrayList<HtmlImageInput>();
//	private ArrayList<HtmlPasswordInput> passwordInputs = new ArrayList<HtmlPasswordInput>();
//	private ArrayList<HtmlTextInput> textInputs = new ArrayList<HtmlTextInput>();
	//------END FUTURE USE---------
	
	private ArrayList<HtmlInput> htmlInputs = new ArrayList<HtmlInput>();
	private ArrayList<HtmlSubmitInput> submitInputs = new ArrayList<HtmlSubmitInput>();
	
	/**
	 * @param htmlForms 
	 */
	protected FuzzyForm(HtmlForm htmlForms){
		
		 fuzzyForm = htmlForms;
		 getAllInputs();
		 for(DomNode n : fuzzyForm.getChildren()){
			 discoverInputs(n);
		 }
		 discoverLostChildrenElements();
	}
	
	/**
	 * Represents all of the inputs of concern for this HTML form.
	 * 
	 * @return list of HtmlInputs
	 */
	public List<HtmlInput> getAllInputs() {
		return htmlInputs;
	}
	
	/**
	 * Represents all of the submit inputs for this form.
	 * 
	 * @return list of HtmlSubmitInput
	 */
	public List<HtmlSubmitInput> getSubmitButton() {
		return submitInputs;
	}
	
	/**
	 * Finds all of the children elements which have not been properly removed from coded and are, therefore, lost.
	 * 
	 * @return list of HtmlElement
	 */
	public List<HtmlElement> getLostChildren(){
		return lostChildrenElements;
	}
	
	/**
	 * Finds all of the lost children within the page.
	 */
	private void discoverLostChildrenElements(){
		lostChildrenElements = fuzzyForm.getLostChildren();
	}
	
	/**
	 * Recursively finds HtmlInputs of interest within nodes.
	 * @param node
	 */
	private void discoverInputs(DomNode node){
		for( DomNode n : node.getChildren()){
			if( n instanceof HtmlTextInput){
//				textInputs.add((HtmlTextInput)n);------FUTURE USE---------
				htmlInputs.add((HtmlInput)n);
				
			}else if(n instanceof HtmlHiddenInput){
//				hiddenInputs.add((HtmlHiddenInput)n);------FUTURE USE---------
				htmlInputs.add((HtmlInput)n);
				
			}else if(n instanceof HtmlFileInput){
//				fileInputs.add((HtmlFileInput)n);------FUTURE USE---------
				htmlInputs.add((HtmlInput)n);
				
			}else if(n instanceof HtmlImageInput){
//				imageInputs.add((HtmlImageInput)n);------FUTURE USE---------
				htmlInputs.add((HtmlInput)n);
				
			}else if(n instanceof HtmlPasswordInput){
//				passwordInputs.add((HtmlPasswordInput)n);------FUTURE USE---------
				htmlInputs.add((HtmlInput)n);
				
			}else if(n instanceof HtmlSubmitInput){
				submitInputs.add((HtmlSubmitInput)n);
			}
			
			if (n.hasChildNodes()){
				discoverInputs(n);
			}
		}
	}
}
