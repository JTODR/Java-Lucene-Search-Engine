package index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import document.CranfieldDoc;

public class Parser {
	
	public Parser() {
		System.out.println("Parser created...");
	}
	
	public List<CranfieldDoc> parse(Path docDir) {
		System.out.println("Starting to parse document...");
		StringBuilder fileContent = readCranfieldFile(docDir);
		
		
		// split the cranfield file using UniqueID as the delimiter
		String[] parts = fileContent.toString().split(".I");

		List<CranfieldDoc> docList = new ArrayList<CranfieldDoc>();

		System.out.println("Starting to parse each individual document...");
		// parse each doc and return the object for each doc
		for (int i = 1; i < parts.length; i++) { // start for loop at i=1 as first string is empty
			docList.add(parseEachDoc(parts[i]));
		}
		
		System.out.println("Finished parsing each document...");
		
		return docList;
	}
	
	private static StringBuilder readCranfieldFile(Path path) {		
		try (InputStream stream = Files.newInputStream(path)) {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			StringBuilder fileContent = new StringBuilder();

			// read in the whole cranfield file
			while((line = in.readLine()) != null) {
				fileContent.append(line);
			}
			System.out.println("Document read successfully...");
			return fileContent;
		}
		catch (IOException e1) {
			e1.printStackTrace();
		} 
		return null;
	}
	
	
	private static CranfieldDoc parseEachDoc(String currDoc) {
		
		int docId;
		String title, authors, bibliography, words, temp;
		
		// parse out ID of current doc
		docId = Integer.parseInt(currDoc.split(".T")[0].trim());
		temp = currDoc.split(".T")[1];
		
		// parse out title of current doc
		title = temp.split(".A")[0];
		temp = temp.split(".A")[1];
		
		// parse out authors of current doc
		authors = temp.split(".B")[0];
		temp = temp.split(".B")[1];
		
		// check if all that is left is ".W", if so, bibliography and words are both empty
		if(temp.equals(".W")) {
			bibliography = "";
			words = "";
		}
		else {
			// parse out bib and words of current doc
			bibliography = temp.split(".W")[0];
			words = temp.split(".W")[1];
		}
		
		/*System.out.println("ID: " + docId);
		System.out.println("TITLE: " + title);
		System.out.println("AUTHORS: " + authors);
		System.out.println("bibliography: " + bibliography);
		System.out.println("WORDS: " + words + "\n\n");*/
		
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
