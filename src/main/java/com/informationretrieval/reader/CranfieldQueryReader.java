package com.informationretrieval.reader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.informationretrieval.document.CranfieldQuery;

public class CranfieldQueryReader extends FileReader {
	
	public CranfieldQueryReader() {
		System.out.println("Cranfield query reader created...");
	}
	
	public List<CranfieldQuery> getQueries(Path queryFilePath) {
		
		String fileContent = readFile(queryFilePath);
		
		// split the file using UniqueID as the delimiter
		String[] parts = fileContent.split(".I");
		parts = Arrays.copyOfRange(parts, 1, parts.length);		// remove 1st item from array

		List<CranfieldQuery> queryList = new ArrayList<CranfieldQuery>();

		System.out.println("Starting to read in each individual query...");
		// read in each query and return the object for each query
		for (int i = 0; i < parts.length; i++) { 
			queryList.add(getSingleQuery(parts[i], i+1));	// i+1 is the id of the current document according to the Cranfield relevance file ids
		}
		
		System.out.println("Reading in of Cranfield queries complete...");
		
		return queryList;
	}
	
	private CranfieldQuery getSingleQuery(String query, int id) {
		String text;
		
		String[] delimiters = {".W"};
		List<String> queryContents = new ArrayList<String>();
		
		queryContents = splitFileOnDelimiters(query, delimiters);
		
		text = queryContents.get(1).trim().replaceAll(" +", " ").replaceAll("\\?", "");		// some queries contain ? and +, so remove them
		
		//System.out.println("ID: " + id);
		//System.out.println("TEXT: " + text + "\n\n");
		
		return createQueryObj(id, text);
	}
	
	private CranfieldQuery createQueryObj(int id, String text) {
		CranfieldQuery queryObj = new CranfieldQuery();
		queryObj.setId(id);
		queryObj.setText(text);
		return queryObj;
	}
}
