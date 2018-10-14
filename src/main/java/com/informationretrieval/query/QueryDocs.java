package com.informationretrieval.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.informationretrieval.document.CranfieldQuery;
import com.informationretrieval.reader.CranfieldQueryReader;
import com.informationretrieval.reader.FileReader;

public class QueryDocs {

	private static String queryPath = "data/cran.qry";
	private static String indexPath = "index";
	private static String outputResultsPath = "output/SearchEngineResults.txt";

	public static void main(String[] args) throws Exception {

		final Path queryDir = Paths.get(queryPath);
		if (!Files.isReadable(queryDir)) {
			System.out.println("Query directory '" + queryDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(2);
		}

		CranfieldQueryReader queryReader = new CranfieldQueryReader();

		List<CranfieldQuery> queries = queryReader.getQueries(queryDir);

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		//searcher.setSimilarity(new ClassicSimilarity());
		searcher.setSimilarity(new BM25Similarity());
		//Analyzer analyzer = new EnglishAnalyzer();
		Analyzer analyzer = new StandardAnalyzer();

		List<String> outputData = new ArrayList<String>();
		List<String> resultList = new ArrayList<String>();

		String queryText = "";
		int queryId = 0;

		System.out.println("Starting to query the index...");
		for (int i = 0; i < queries.size(); i++) {

			queryText = queries.get(i).getText().trim();
			queryId = queries.get(i).getId();

			if (queryText == null || queryText.length() == -1) {
				break;
			}

			queryText = removeCommonWords(queryText).trim().replaceAll("  ", " ");
			//System.out.println("QUERYTEXT: " + queryText);

			// Creating a single field query parser
			//QueryParser tempParser = new QueryParser("contents", analyzer);
			//Query tempQuery = tempParser.parse(queryText);
			//outputData.addAll(performQuerySearch(searcher, tempQuery, queryId));

			// Creating a multi field query parser
			HashMap<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("title", 0.8f);
			boosts.put("contents", 1.3f);
			MultiFieldQueryParser multiFieldQP = new MultiFieldQueryParser(new String[] { "title", "contents" }, analyzer, boosts);
			Query query = multiFieldQP.parse(queryText);

			outputData.addAll(performQuerySearch(searcher, query, queryId));

		}

		// for(int i = 0; i < outputData.size(); i++) {
		// System.out.println(outputData.get(i));
		// }
		Path file = Paths.get(outputResultsPath);
		Files.write(file, outputData, Charset.forName("UTF-8"));
		System.out.println("Query results are written to " + outputResultsPath + "...\nFinished...");
		reader.close();
	}

	private static String removeCommonWords(String query) {
		List<String> commonWords = FileReader.getCommonWordList();
		for (String commonWord : commonWords) {
			if (query.contains(commonWord))
				query = query.replaceAll("\\b" + commonWord + "\\b", "");
		}
		return query;
	}

	public static List<String> performQuerySearch(IndexSearcher searcher, Query query, int queryId) throws IOException {
		List<String> returnList = new ArrayList<String>();

		TopDocs results = searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		Document doc = null;

		for (int i = 0; i < hits.length; i++) {
			doc = searcher.doc(hits[i].doc);
			returnList.add(queryId + " Q0 " + doc.get("id") + " " + (i + 1) + " " + hits[i].score + " STANDARD");
		}
		return returnList;
	}

	private static List<String> getQueryTermFrequency(Query query, int queryId) throws IOException {
		List<String> returnList = new ArrayList<String>();
		DirectoryReader ireader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher isearcher = new IndexSearcher(ireader);

		System.out.println("PARSED QUERYTEXT: " + query.toString("contents"));
		System.out.println("QUERY ID: " + queryId);
		// String queryText = query.toString("contents");

		// Query queryTerm = new TermQuery(new Term("content",
		// query.toString("contents")));
		ScoreDoc[] hits = isearcher.search(query, 30).scoreDocs;

		// Make sure we actually found something
		if (hits.length <= 0) {
			System.out.println("Failed to retrieve a document");
			return null;
		}

		for (int i = 0; i < hits.length; i++) {
			// get the document ID of the first search result
			int docID = hits[i].doc;
			int tempDocId = docID + 1;
			int tempDocRank = i + 1;
			Document doc = isearcher.doc(hits[i].doc);
			System.out.println(
					"\n\nDOC ID: " + tempDocId + " || SIM SCORE: " + hits[i].score + " || RANK: " + tempDocRank);
			// Get the fields associated with the document (filename and content)
			Fields fields = ireader.getTermVectors(docID);

			for (String field : fields) {
				// For each field, get the terms it contains i.e. unique words
				Terms terms = fields.terms(field);

				// Iterate over each term in the field
				BytesRef termByte = null;
				TermsEnum termsEnum = terms.iterator();

				while ((termByte = termsEnum.next()) != null) {
					int id;

					// for each term retrieve its postings list
					PostingsEnum posting = null;
					posting = termsEnum.postings(posting, PostingsEnum.FREQS);

					while ((id = posting.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
						// convert the term from a byte array to a string
						String termString = termByte.utf8ToString();
						String tempStr = "\\b" + termString + "\\b";
						Pattern p = Pattern.compile(tempStr);
						Matcher m = p.matcher(query.toString("contents"));
						if (m.find()) {
							// extract some stats from the index
							Term term = new Term(field, termString);
							long freq = posting.freq(); // number of times current term is in current doc
							long docFreq = ireader.docFreq(term); // number of docs containing current term
							long totFreq = ireader.totalTermFreq(term); // total no. of occurrences of term across all
																		// docs
							System.out.printf("%-16s : freq = %4d : totfreq = %4d : docfreq = %4d\n", termString, freq,
									totFreq, docFreq);

							returnList.add(queryId + " Q0 " + doc.get("id") + " " + (i + 1) + " " + hits[i].score
									+ " STANDARD");
						}

					}
				}
			}
		}

		// close everything when we're done
		ireader.close();

		return returnList;
	}
}
