package query;

import parse.CranfieldQueryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import document.CranfieldQuery;

public class QueryDocs {
	public static void main(String[] args) throws Exception {
		
		String queryPath = "data/cran.qry";
		String indexPath = "index";

		final Path queryDir = Paths.get(queryPath);
		if (!Files.isReadable(queryDir)) {
			System.out.println("Query directory '" + queryDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(2);
		}
		
		CranfieldQueryParser cranfieldParser = new CranfieldQueryParser();
		
		List<CranfieldQuery> queries = cranfieldParser.getQueries(queryDir);
				
		String field = "words";
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		BufferedReader in = null;
		QueryParser parser = new QueryParser(field, analyzer);
		
		List<String> outputData = new ArrayList<>();
		List<String> returnList = new ArrayList<>();
		
		for(int i = 0; i < queries.size(); i++){

			String line = queries.get(i).getText();
			Integer queryId = queries.get(i).getId();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();

			Query query = parser.parse(line);
			//System.out.println("\n\nOriginal query: ID: " + queries.get(i).getId() + ". Text: " + line);
			//System.out.println("Parsed query: " + query.toString(field));
			
			outputData.addAll(performQuerySearch(in, searcher, query, queryId));
		}
		
		for(int i = 0; i < outputData.size(); i++) {
			System.out.println(outputData.get(i));
		}
		Path file = Paths.get("output/MyCranfieldResults.txt");
		Files.write(file, outputData, Charset.forName("UTF-8"));
		
		reader.close();
	}
	
	public static List<String> performQuerySearch(BufferedReader in, IndexSearcher searcher, Query query, int queryId) throws IOException {
		List<String> returnList = new ArrayList<>();  
	    TopDocs results = searcher.search(query, 20);
	    ScoreDoc[] hits = results.scoreDocs;
	    Document doc = null;
	    String title;
	    int docId;
	    
	    for(int i = 0; i < hits.length; i++) {
	    	doc = searcher.doc(hits[i].doc);
	        title = doc.get("title");
	        
	        docId = Integer.valueOf(doc.get("id"));
	        //System.out.println("FOUND [ID: " + id + "] [TITLE: " + title + "] [SCORE: " + hits[i].score + "]");
	        //System.out.println("FOUND [ID: " + docId + "] [SCORE: " + hits[i].score + "]");
	        returnList.add(queryId + " 0 " + docId + " " + hits[i].score);
	    }
	    
	    return returnList;
	  }
}
