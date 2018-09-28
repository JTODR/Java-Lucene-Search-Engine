package document;


/*
 * POJO class to store the 5 attributes of each document
 * 
 * Each document has the following attributes: 
 * .I: The unique ID of the document. An integer from 1 to 1400.
 * .T: The title of the document. A string which may span multiple lines.
 * .A: The authors of a document. A string which may span multiple lines.
 * .B: Bibliographic information about the document. A string which may span multiple lines.
 * .W: Words. The actual text of the document. A string which may span multiple lines.
 */

public class CranfieldDoc {
	
	private int id;
	private String title;
	private String authors;
	private String bibliography;
	private String words;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	public String getBibliography() {
		return bibliography;
	}
	public void setBibliography(String bibliography) {
		this.bibliography = bibliography;
	}
	public String getWords() {
		return words;
	}
	public void setWords(String words) {
		this.words = words;
	}
	
	
}
