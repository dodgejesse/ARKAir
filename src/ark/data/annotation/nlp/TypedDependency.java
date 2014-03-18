package ark.data.annotation.nlp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ark.data.annotation.Document;

/**
 * TypedDependency represents a typed semantic dependency between two tokens 
 * within a sentence.  The 'type' for the dependency can be any string value.
 * The lack of restriction on the 'type' allows this class to represent 
 * dependencies generated by many different NLP libraries.  For example,
 * it can be used to represent dependencies from Stanford CoreNLP given
 * at:
 * 
 * http://nlp.stanford.edu/software/dependencies_manual.pdf
 * 
 * Or it can be used to represent dependencies from FreeLing given at:
 * 
 * http://devel.cpl.upc.edu/freeling/svn/trunk/doc/grammars/ca+esLABELINGtags 
 * 
 * @author Bill McDowell
 *
 */
public class TypedDependency {
	private static Pattern dependencyPattern = Pattern.compile("(.*)\\((.*)\\-([0-9']*),(.*)\\-([0-9']*)\\)");
	
	private Document document;
	private int sentenceIndex;
	private int parentTokenIndex;
	private int childTokenIndex;
	
	private String type;
	
	public TypedDependency(Document document, int sentenceIndex, int parentTokenIndex, int childTokenIndex, String type) {
		this.document = document;
		this.sentenceIndex = sentenceIndex;
		this.parentTokenIndex = parentTokenIndex;
		this.childTokenIndex = childTokenIndex;
		this.type = type;
	}
	
	public Document getDocument() {
		return this.document;
	}
	
	public int getParentTokenIndex() {
		return this.parentTokenIndex;
	}
	
	public int getChildTokenIndex() {
		return this.childTokenIndex;
	}
	
	public int getSentenceIndex() {
		return this.sentenceIndex;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String toString() {
		return this.type + 
			   "(" 
				+ this.document.getToken(this.sentenceIndex, this.parentTokenIndex) + "-" + (this.parentTokenIndex + 1) +
				", " + this.document.getToken(this.sentenceIndex, this.childTokenIndex) + "-" + (this.childTokenIndex + 1) + 
				")";
	}
	
	public static TypedDependency fromString(String str, Document document, int sentenceIndex) {
		str = str.trim();
		
		Matcher m = TypedDependency.dependencyPattern.matcher(str);
		
		if (!m.matches())
			return null;
		
		String type = m.group(1).trim();
		int parentTokenIndex = Integer.parseInt(m.group(3).replace("'", "").trim());
		int childTokenIndex = Integer.parseInt(m.group(5).replace("'", "").trim());
		
		return new TypedDependency(document, sentenceIndex, parentTokenIndex-1, childTokenIndex-1, type);
	}
}