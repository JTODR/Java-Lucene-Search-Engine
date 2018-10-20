package com.informationretrieval.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import com.informationretrieval.document.CranfieldQuery;
import com.informationretrieval.reader.CranfieldQueryReader;
import com.informationretrieval.reader.FileReader;

public class QueryDocs {

	private static String queryPath = "data/cran.qry";
	private static String indexPath = "index";
	private static String outputResultsPath = "output/SearchEngineResults.txt";
	private static Similarity similarity;
	private static Analyzer analyzer;

	public static void main(String[] args) throws Exception {

		String usage = "java index.QueryDocs -analyzer ANALYZER -similarity SIMILARITY\n"
				+ "Please specify an analyzer and similarity when running this program\n"
				+ "ANALYZER is either [Standard] or [English]\n"
				+ "SIMILARITY is either [BM25] or [VSM]";

		// Must specify an analyzer when running this program
		if (args.length < 4) {
			System.err.println("Usage: " + usage + "\nExiting...");
			System.exit(-1);
		}
			
		final Path queryDir = Paths.get(queryPath);
		if (!Files.isReadable(queryDir)) {
			System.err.println("Query directory '" + queryDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(2);
		}
		
		// Get the analyzer that will be used by the query parser
		analyzer = getAnalyzer(args);
		if (analyzer == null) {
			System.err.println("Issue with the given analyzer arguments: " + args[0] + " " + args[1] + "\nUsage: " + usage + "\nExiting...");
			System.exit(-1);
		}
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// Get the similarity method that will be used by the index searcher to score the query results
		similarity = getSimilarity(args);
		if (similarity == null) {
			System.err.println("Issue with the given similarity arguments: " + args[2] + " " + args[3] + "\nUsage: " + usage + "\nExiting...");
			System.exit(-1);
		}
		else {
			searcher.setSimilarity(similarity);
		}

		// Read in the Cranfield queries as a list of CranfieldQuery objects
		CranfieldQueryReader queryReader = new CranfieldQueryReader();
		List<CranfieldQuery> queries = queryReader.getQueries(queryDir);
		
		// Get the list of common words that will be used to remove common words from each query
		List<String> commonWords = FileReader.getCommonWordList();
		
		List<String> outputData = new ArrayList<String>();
		String queryText = "";
		int queryId = 0;
		
		System.out.println("Starting to query the index...");
		for (int i = 0; i < queries.size(); i++) {

			queryText = queries.get(i).getText().trim();
			queryId = queries.get(i).getId();

			if (queryText == null || queryText.length() == -1) {
				break;
			}

			// Remove frequent words from the query from hard coded list, this helps increase the precision of the query
			queryText = removeCommonWords(queryText, commonWords).trim().replaceAll("  ", " ");

			// Creating a multi field query parser, boosting the index contents over the index title
			HashMap<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("title", 0.8f);
			boosts.put("contents", 1.3f);
			MultiFieldQueryParser multiFieldQP = new MultiFieldQueryParser(new String[] { "title", "contents" }, analyzer, boosts);
			Query query = multiFieldQP.parse(queryText);
			
			// Perform a query search on the indexed documents
			outputData.addAll(performQuerySearch(searcher, query, queryId));
		}
		
		Path file = Paths.get(outputResultsPath);
		Files.write(file, outputData, Charset.forName("UTF-8"));
		System.out.println("Query results are written to " + outputResultsPath + "...\nFinished...");
		reader.close();
	}
	
	private static Analyzer getAnalyzer(String[] args) {

		if (!args[0].equals("-analyzer")) {
			return null;
		} else {
			if (args[1].toLowerCase().equals("standard")) {
				System.out.println("Standard analyzer chosen to query the index...");
				return new StandardAnalyzer();
			} else if (args[1].toLowerCase().equals("english")) {
				System.out.println("English analyzer chosen to query the index...");
				return new EnglishAnalyzer();
			} else
				return null;
		}
	}
	
	private static Similarity getSimilarity(String[] args) {
		
		if (!args[2].equals("-similarity")) {
			return null;
		} else {
			if (args[3].equals("BM25")) {
				System.out.println("BM25 similarity chosen to score the query results...");
				return new BM25Similarity();
			} else if (args[3].toLowerCase().equals("vsm")) {
				System.out.println("VSM similarity chosen to score the query results...");
				return new ClassicSimilarity();
			} else
				return null;
		}
	}
	
	private static String removeCommonWords(String query, List<String> commonWords) {
		for (String commonWord : commonWords) {
			if (query.contains(commonWord))
				query = query.replaceAll("\\b" + commonWord + "\\b", "");	// Replace any common words with an empty string
		}
		return query;
	}

	public static List<String> performQuerySearch(IndexSearcher searcher, Query query, int queryId) throws IOException {
		List<String> queryResults = new ArrayList<String>();
		TopDocs results = searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		Document doc = null;

		for (int i = 0; i < hits.length; i++) {
			doc = searcher.doc(hits[i].doc);
			if(hits[i].score > 9) {
				queryResults.add(queryId + " Q0 " + doc.get("id") + " " + (i + 1) + " " + hits[i].score + " STANDARD");
			}
		}
		return queryResults;
	}
}
