package index;

import document.CranfieldDoc;
import parse.CranfieldDocParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexDocs {

	private IndexDocs() {
	}

	public static void main(String[] args) {

		String docsPath = "data/cran.all.1400";

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		
		CranfieldDocParser parser = new CranfieldDocParser();
		
		/*
		 * get a list of all docs as objects
		 * each doc object has a uniqueID, title, authors, bibliography and words.
		 */
		List<CranfieldDoc> docObjList = parser.getDocuments(docDir);
		
		if(docObjList == null) {
			System.out.println("Document object list is null, check the parser. Exiting...");
			System.exit(2);
		}
		
		Directory dir = null;
		try {
			dir = FSDirectory.open(Paths.get("index"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = null;
		try {
			writer = new IndexWriter(dir, iwc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Starting to indexing documents...");

		for(int i = 0; i < docObjList.size(); i++) {
			indexDoc(writer, docObjList.get(i));
		}
		System.out.println("Finish indexing documents...");
		
		
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** Indexes a single document **/
	static void indexDoc(IndexWriter writer, CranfieldDoc docObj) {
		//System.out.println("Indexing document: " + docObj.getId());
		// make a new, empty document
		Document doc = new Document();	
		String stemmedText = "";
		
		/*
		 * Field.Store.YES vs Field.Store.NO:
		 * When the index files are queried, stored values will be presented back to the query
		 * Non stored values will not be presented
		 */
		
		doc.add(new StringField("id", String.valueOf(docObj.getId()), Field.Store.YES));
		
		
		/* Cranfield Doc Title*/
		try {
			stemmedText = stemWords("title", docObj.getTitle());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		doc.add(new TextField("title", stemmedText, Field.Store.YES));
		System.out.println("ORIG TEXT: " + docObj.getTitle());
		System.out.println("STEMMED TEXT: " + stemmedText);
		
		
		/* Cranfield Doc Bibliography*/
		/*try {
			stemmedText = stemWords("bibliography", docObj.getBibliography());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		doc.add(new TextField("bibliography", stemmedText, Field.Store.YES));
		System.out.println("ORIG TEXT: " + docObj.getBibliography());
		System.out.println("STEMMED TEXT: " + stemmedText);*/
		
		/* Cranfield Doc Text*/
		try {
			stemmedText = stemWords("title", docObj.getWords());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		doc.add(new TextField("words", stemmedText, Field.Store.YES));
		//System.out.println("ORIG TEXT: " + docObj.getWords());
		//System.out.println("STEMMED TEXT: " + stemmedText);
		
		
		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
