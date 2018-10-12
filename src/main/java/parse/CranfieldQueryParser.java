package parse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import document.CranfieldQuery;

public class CranfieldQueryParser extends Parser {
	
	public CranfieldQueryParser() {
		System.out.println("Query parser created...");
	}
	
	public List<CranfieldQuery> getQueries(Path queryFilePath) {
		
		String[] fileParts = readFile(queryFilePath);

		List<CranfieldQuery> queryList = new ArrayList<CranfieldQuery>();

		System.out.println("Starting to parse each individual query...");
		// parse each doc and return the object for each doc
		for (int i = 0; i < fileParts.length; i++) { 
			queryList.add(parseQuery(fileParts[i], i+1));
		}
		
		System.out.println("Finished parsing each query...");
		
		return queryList;
	}
	
	private CranfieldQuery parseQuery(String query, int id) {
		String text;
		
		String[] delimiters = {".W"};
		List<String> queryContents = new ArrayList<String>();
		
		queryContents = parse(query, delimiters);
		
		text = queryContents.get(1).trim().replaceAll(" +", " ").replaceAll("\\?", "");
		
		System.out.println("ID: " + id);
		System.out.println("TEXT: " + text + "\n\n");
		
		return createQueryObj(id, text);
	}
	
	private CranfieldQuery createQueryObj(int id, String text) {
		
		CranfieldQuery queryObj = new CranfieldQuery();
		queryObj.setId(id);
		queryObj.setText(text);
		
		return queryObj;
	}
}
