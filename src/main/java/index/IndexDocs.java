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
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexDocs {

	private IndexDocs() {
	}

	public static void main(String[] args) throws Exception {

		String docsPath = "data/cran.all.1400";

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		CranfieldDocParser parser = new CranfieldDocParser();
		// get a list of all docs as objects, each doc object has a uniqueID, title, authors, bibliography and words.
		List<CranfieldDoc> documentList = parser.getDocuments(docDir);
		
		if(documentList == null) {
			System.out.println("Document object list is null, check the parser. Exiting...");
			System.exit(2);
		}
		
		Directory dir = null;
		try {
			dir = FSDirectory.open(Paths.get("index"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Analyzer analyzer = new EnglishAnalyzer();
		
		FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setTokenized(true); //done as default
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorPositions(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPayloads(true);
        
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);

		System.out.println("Starting to indexing documents...");

		for(int i = 0; i < documentList.size(); i++) {
			indexDoc(writer, documentList.get(i), ft);
		}
		System.out.println("Finish indexing documents...");
		
		writer.close();
	}
	
	/** Indexes a single document **/
	static void indexDoc(IndexWriter writer, CranfieldDoc document, FieldType ft) throws IOException {

		Document doc = new Document();	
		
		/* Cranfield Doc ID*/
		doc.add(new StringField("id", String.valueOf(document.getId()), Field.Store.YES));
		
		/* Cranfield Doc Title*/
		doc.add(new TextField("title", document.getTitle(), Field.Store.YES));
		
		/* Cranfield Doc Text*/
		//System.out.println("contents: " + document.getWords());
		doc.add(new Field("contents", document.getWords(), ft));
		
		
		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			writer.addDocument(doc);
		}
	}

}
