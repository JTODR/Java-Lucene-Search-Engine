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
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
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
		
		
		
		searcher.setSimilarity(new BM25Similarity());
		Analyzer analyzer = new StandardAnalyzer();
		BufferedReader in = null;
		QueryParser parser = new QueryParser(field, analyzer);
		
		List<String> outputData = new ArrayList<>();
		
		String queryText = "";
		String stemmedLine = "";
		int queryId = 0;
 		
		for(int i = 0; i < queries.size(); i++){

			queryText = queries.get(i).getText().trim();
			queryId = queries.get(i).getId();

			if (queryText == null || queryText.length() == -1) {
				break;
			}
			
			stemmedLine = stemWords("words", queryText);

			Query query = parser.parse(stemmedLine);
			//System.out.println("\n\nOriginal query: ID: " + queries.get(i).getId() + ". \nText: " + line + ". \nStemmed Text: " + stemmedLine);
			System.out.println("Parsed query: " + query.toString(field));
			//System.out.println("QUERY: " + query);
			
			outputData.addAll(performQuerySearch(in, searcher, query, queryId));
		}
		
		for(int i = 0; i < outputData.size(); i++) {
			System.out.println(outputData.get(i));
		}
		Path file = Paths.get("output/SearchEngineResults.txt");
		Files.write(file, outputData, Charset.forName("UTF-8"));
		
		reader.close();
	}
	
	public static List<String> performQuerySearch(BufferedReader in, IndexSearcher searcher, Query query, int queryId) throws IOException {
		List<String> returnList = new ArrayList<>();  
	    TopDocs results = searcher.search(query, 30);
	    ScoreDoc[] hits = results.scoreDocs;
	    Document doc = null;
	    
	    for(int i = 0; i < hits.length; i++) {

	    	doc = searcher.doc(hits[i].doc);
	    	returnList.add(queryId + " Q0 " + doc.get("id") + " " +  (i + 1) + " "+ hits[i].score + " STANDARD");

	        //System.out.println("FOUND [ID: " + Integer.valueOf(doc.get("id"))+ "] [SCORE: " + hits[i].score + "]");
	    } 
	    return returnList;
	  }
	
	private static String stemWords(String fieldName, String text) throws IOException{
	    
	    Analyzer analyzer = new StandardAnalyzer();
	    TokenStream tokenizer = analyzer.tokenStream(fieldName, text);
	 
	    tokenizer = new LowerCaseFilter(tokenizer);
	    tokenizer = new PorterStemFilter(tokenizer);

	    CharTermAttribute token = tokenizer.getAttribute(CharTermAttribute.class);

	    StringBuilder stringBuilder = new StringBuilder();
	    tokenizer.reset();

	    while(tokenizer.incrementToken()) {
	        if(stringBuilder.length() > 0 ) {
	            stringBuilder.append(" ");
	        }

	        stringBuilder.append(token.toString());
	    }

	    tokenizer.end();
	    tokenizer.close();
	    analyzer.close();

	    return stringBuilder.toString();
	}
}
