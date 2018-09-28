package parse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import document.CranfieldDoc;

public class IndexParser extends Parser {
	
	public IndexParser() {
		System.out.println("Index parser created...");
	}
	
	public List<CranfieldDoc> getDocuments(Path docDir) {
		
		String[] fileParts = readFile(docDir);

		List<CranfieldDoc> docList = new ArrayList<CranfieldDoc>();

		System.out.println("Starting to parse each individual document...");
		// parse each doc and return the object for each doc
		for (int i = 1; i < fileParts.length; i++) { // start for loop at i=1 as first string is empty
			docList.add(parseDoc(fileParts[i]));
		}
		
		System.out.println("Finished parsing each document...");
		
		return docList;
	}

	private static CranfieldDoc parseDoc(String currDoc) {
	
		int docId;
		String title, authors, bibliography, words;
		
		String[] delimiters = {".T", ".A", ".B", ".W"};
		List<String> docContents = new ArrayList<>();
		
		docContents = parse(currDoc, delimiters);
		
		docId = Integer.parseInt(docContents.get(0).trim());
		title = docContents.get(1);
		authors = docContents.get(2);
		bibliography = docContents.get(3);
		words = docContents.get(4);
		
		System.out.println("ID: " + docId);
		System.out.println("TITLE: " + title);
		System.out.println("AUTHORS: " + authors);
		System.out.println("BIBLIOGRAPHY: " + bibliography);
		System.out.println("WORDS: " + words + "\n\n");
		
		return createDocObj(docId, title, authors, bibliography, words);
	}
	
	private static CranfieldDoc createDocObj(int docId, String title, String authors, String bibliography, String words) {
		
		CranfieldDoc docObj = new CranfieldDoc();
		docObj.setId(docId);
		docObj.setTitle(title);
		docObj.setAuthors(authors);
		docObj.setBibliography(bibliography);
		docObj.setWords(words);
		
		return docObj;
	}
}
