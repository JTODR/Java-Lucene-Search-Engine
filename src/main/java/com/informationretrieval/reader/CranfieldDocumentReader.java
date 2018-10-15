package com.informationretrieval.reader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.informationretrieval.document.CranfieldDoc;

public class CranfieldDocumentReader extends FileReader {
	
	public CranfieldDocumentReader() {
		System.out.println("Cranfield document reader created...");
	}
	
	public List<CranfieldDoc> getDocuments(Path docDir) {
		
		String fileContent = readFile(docDir);
		
		// split the file using UniqueID as the delimiter
		String[] parts = fileContent.split(".I");
		parts = Arrays.copyOfRange(parts, 1, parts.length);		// remove 1st item from array

		List<CranfieldDoc> docList = new ArrayList<CranfieldDoc>();

		System.out.println("Starting to read in each individual document...");
		// read in each doc and return the object for each doc
		for (int i = 0; i < parts.length; i++) { 
			docList.add(getSingleDoc(parts[i]));
		}
		
		System.out.println("Reading in of Cranfield documents complete...");
		
		return docList;
	}

	private static CranfieldDoc getSingleDoc(String currDoc) {
	
		int docId;
		String title, authors, bibliography, words;
		
		String[] delimiters = {".T", ".A", ".B", ".W"};
		List<String> docContents = new ArrayList<String>();
		
		docContents = splitFileOnDelimiters(currDoc, delimiters);
		
		docId = Integer.parseInt(docContents.get(0).trim().replaceAll(" +", " "));
		title = docContents.get(1).trim().replaceAll(" +", " ");
		authors = docContents.get(2).trim().replaceAll(" +", " ");
		bibliography = docContents.get(3).trim().replaceAll(" +", " ");
		words = docContents.get(4).trim().replaceAll(" +", " ");
		
		//System.out.println("ID: " + docId);
		//System.out.println("TITLE: " + title);
		//System.out.println("AUTHORS: " + authors);
		//System.out.println("BIBLIOGRAPHY: " + bibliography);
		//System.out.println("WORDS: " + words + "\n\n");
		
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
