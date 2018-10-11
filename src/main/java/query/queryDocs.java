package query;

import parse.CranfieldQueryParser;
import parse.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import document.CranfieldQuery;

public class QueryDocs {
	
	private static String queryPath = "data/cran.qry";
	private static String indexPath = "index";
	private static Parser parser = new Parser();
	
	public static void main(String[] args) throws Exception {


		final Path queryDir = Paths.get(queryPath);
		if (!Files.isReadable(queryDir)) {
			System.out.println("Query directory '" + queryDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(2);
		}
		
		CranfieldQueryParser cranfieldParser = new CranfieldQueryParser();
		
		List<CranfieldQuery> queries = cranfieldParser.getQueries(queryDir);
				
		String field = "contents";
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());
		Analyzer analyzer = new EnglishAnalyzer();
		
		List<String> outputData = new ArrayList<String>();
		
		String queryText = "";
		int queryId = 0;
 		
		for(int i = 0; i < queries.size(); i++){

			queryText = queries.get(i).getText().trim();
			queryId = queries.get(i).getId();

			if (queryText == null || queryText.length() == -1) {
				break;
			}
			
			queryText = removeCommonWords(queryText).trim().replaceAll("  ", " ");
			System.out.println("QUERYTEXT: " + queryText);
			
			queryText = getTermIndexFreq(queryText, reader, analyzer);

			// Creating a multi field query parser
			HashMap<String,Float> boosts = new HashMap<String,Float>();
			boosts.put("title", 2f);
			boosts.put("contents", 10f);
			MultiFieldQueryParser multiFieldQP = new MultiFieldQueryParser(new String[] {"title","contents"}, analyzer, boosts);
			Query query = multiFieldQP.parse(queryText);
			queryText = getTermIndexFreq(queryText, reader, analyzer);

			System.out.println("Parsed query: " + query.toString());

			outputData.addAll(performQuerySearch(searcher, query, queryId));
		}
		
		for(int i = 0; i < outputData.size(); i++) {
			System.out.println(outputData.get(i));
		}
		Path file = Paths.get("output/SearchEngineResults.txt");
		Files.write(file, outputData, Charset.forName("UTF-8"));
		
		reader.close();
	}
	

	private static String removeCommonWords(String query) {
		List<String> commonWords = Parser.getCommonWordList();
		for(String commonWord : commonWords) {
			if(query.contains(commonWord))
				query = query.replaceAll("\\b" + commonWord + "\\b", "");		
		}
		return query;
	}
	
	public static List<String> performQuerySearch(IndexSearcher searcher, Query query, int queryId) throws IOException {
		List<String> returnList = new ArrayList<String>();
		
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
	
	private static String getTermIndexFreq(String queryText, IndexReader reader, Analyzer analyzer) throws Exception {
		QueryParser parser = new QueryParser("contents", analyzer);
		Query query = parser.parse(queryText.replaceAll("\\)", "").replaceAll("\\(", ""));
		String[] queryTextWords = query.toString("contents").split(" ");
		System.out.println("Query String: " + query.toString("contents"));

		long wordFreq = 0;
		int maxWordFreq = 0;
		String newQueryStr = "";
		HashMap<String, Integer> wordFreqMap = new HashMap<String, Integer>();
		
		for(String word : queryTextWords) {
			Term term = new Term("contents", word);
			wordFreq = reader.docFreq(term);
			System.out.println(word + " freq = " + reader.docFreq(term));
			wordFreqMap.put(word, (int)wordFreq);
			
			if(wordFreq > maxWordFreq)
				maxWordFreq = (int)wordFreq;
		}
		
		int limit = maxWordFreq/2;
		boolean addedToNewString = false;
		for (Entry<String, Integer> entry : wordFreqMap.entrySet()){
			if(entry.getValue() < limit) {
				newQueryStr = newQueryStr + entry.getKey() + " ";
				addedToNewString = true;
			}
		}
		
		if(!addedToNewString) {
			return query.toString("contents");
		}
		
		System.out.println("NEW QUERY: " + newQueryStr + "\n");
		
		return newQueryStr;
	}
	
}
