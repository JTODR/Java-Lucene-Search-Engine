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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		/*if (create) {
			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}*/
		
		iwc.setOpenMode(OpenMode.CREATE);

		IndexWriter writer = null;
		try {
			writer = new IndexWriter(dir, iwc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		indexDocs(writer, docObjList);

		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void indexDocs(final IndexWriter writer, List<CranfieldDoc> docObjList){
	   
		for(int i = 0; i < docObjList.size(); i++) {
			indexDoc(writer, docObjList.get(i));
		}
	   
	}
	
	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, CranfieldDoc docObj) {
		//System.out.println("Indexing document: " + docObj.getId());
		// make a new, empty document
		Document doc = new Document();		
		
		/*
		 * Field.Store.YES vs Field.Store.NO:
		 * When the index files are queried, stored values will be presented back to the query
		 * Non stored values will not be presented
		 */
		
		doc.add(new StringField("id", String.valueOf(docObj.getId()), Field.Store.YES));
		
		InputStream inputStream = new ByteArrayInputStream( docObj.getTitle().getBytes( StandardCharsets.UTF_8 ) );
		doc.add(new StringField("title", docObj.getTitle(), Field.Store.YES));
		//doc.add(new TextField("title", new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), Field.Store.YES));
		
		inputStream = new ByteArrayInputStream( docObj.getAuthors().getBytes( StandardCharsets.UTF_8 ) );
		doc.add(new TextField("authors", new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
		
		inputStream = new ByteArrayInputStream( docObj.getBibliography().getBytes( StandardCharsets.UTF_8 ) );
		doc.add(new TextField("bibliography", new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
		
		inputStream = new ByteArrayInputStream( docObj.getWords().getBytes( StandardCharsets.UTF_8 ) );
		doc.add(new TextField("words", new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			// New index, so we just add the document (no old document can be there):
			//System.out.println("adding " + docObj.getTitle());
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
