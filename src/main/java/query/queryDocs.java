package query;

import parse.CranfieldQueryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		
		for(int i = 0; i < queries.size(); i++){

			String line = queries.get(i).getText();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();

			Query query = parser.parse(line);
			System.out.println("\n\nOriginal query: ID: " + queries.get(i).getId() + ". Text: " + line);
			System.out.println("Parsed query: " + query.toString(field));
			
			performQuerySearch(in, searcher, query);
		}
		reader.close();
	}
	
	  public static void performQuerySearch(BufferedReader in, IndexSearcher searcher, Query query) throws IOException {
	 
	    TopDocs results = searcher.search(query, 20);
	    ScoreDoc[] hits = results.scoreDocs;
	    Document doc = null;
	    String title = "";
	    int id = -1;
	    
	    for(int i = 0; i < hits.length; i++) {
	    	doc = searcher.doc(hits[i].doc);
	        title = doc.get("title");
	        id = Integer.valueOf(doc.get("id"));
	        System.out.println("FOUND [ID: " + id + "] [TITLE: " + title + "]");
	    }
	  }
}
