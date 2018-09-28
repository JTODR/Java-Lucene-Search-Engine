package parse;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import document.CranfieldQuery;

public class QueryParser extends Parser {
	
	public QueryParser() {
		System.out.println("Query parser created...");
	}
	
	public List<CranfieldQuery> getQueries(Path queryFilePath) {
		
		String[] fileParts = readFile(queryFilePath);

		List<CranfieldQuery> queryList = new ArrayList<CranfieldQuery>();

		System.out.println("Starting to parse each individual query...");
		// parse each doc and return the object for each doc
		for (int i = 1; i < fileParts.length; i++) { // start for loop at i=1 as first string is empty
			queryList.add(parseQuery(fileParts[i]));
		}
		
		System.out.println("Finished parsing each query...");
		
		return queryList;
	}
	
	private CranfieldQuery parseQuery(String query) {
		int id;
		String text;
		
		String[] delimiters = {".W"};
		List<String> queryContents = new ArrayList<>();
		
		queryContents = parse(query, delimiters);
		
		id = Integer.parseInt(queryContents.get(0).split(".I ")[1].trim());
		text = queryContents.get(1);
		
		
		// parse out text of current query
		/*text = query.split(".W")[1];
		String temp = query.split(".W")[0];
		
		// parse out id of current query
		id = Integer.parseInt(temp.split(".I ")[1].trim());*/
		
		System.out.println("ID: " + id);
		System.out.println("TEXT: " + text);
		
		return createQueryObj(id, text);
	}
	
	private CranfieldQuery createQueryObj(int id, String text) {
		
		CranfieldQuery queryObj = new CranfieldQuery();
		queryObj.setId(id);
		queryObj.setText(text);
		
		return queryObj;
	}
}
