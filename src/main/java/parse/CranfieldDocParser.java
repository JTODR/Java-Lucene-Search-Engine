package parse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import document.CranfieldDoc;

public class CranfieldDocParser extends Parser {
	
	public CranfieldDocParser() {
		System.out.println("Index parser created...");
	}
	
	public List<CranfieldDoc> getDocuments(Path docDir) {
		
		String[] fileParts = readFile(docDir);

		List<CranfieldDoc> docList = new ArrayList<CranfieldDoc>();

		System.out.println("Starting to parse each individual document...");
		// parse each doc and return the object for each doc
		for (int i = 0; i < fileParts.length; i++) { 
			docList.add(parseDoc(fileParts[i]));
		}
		
		System.out.println("Finished parsing each document...");
		
		return docList;
	}

	private static CranfieldDoc parseDoc(String currDoc) {
	
		int docId;
		String title, authors, bibliography, words;
		
		String[] delimiters = {".T", ".A", ".B", ".W"};
		List<String> docContents = new ArrayList<String>();
		
		docContents = parse(currDoc, delimiters);
		
		docId = Integer.parseInt(docContents.get(0).trim().replaceAll(" +", " "));
		title = docContents.get(1).trim().replaceAll(" +", " ");
		authors = docContents.get(2).trim().replaceAll(" +", " ");
		bibliography = docContents.get(3).trim().replaceAll(" +", " ");
		words = docContents.get(4).trim().replaceAll(" +", " ");
		
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
