package com.informationretrieval.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.informationretrieval.document.CranfieldDoc;
import com.informationretrieval.reader.CranfieldDocumentReader;

public class IndexDocs {

	private static Analyzer analyzer;
	private static String docsPath = "data/cran.all.1400";
	private static String indexPath = "index";

	public static void main(String[] args) throws Exception {
		String usage = "java index.IndexDocs -analyzer ANALYZER\n"
				+ "Please specify an analyzer when running this program\n"
				+ "ANALYZER is either [Standard] or [English]";

		// Must specify an analyzer when running this program
		if (args.length < 2) {
			System.err.println("Usage: " + usage + "\nExiting...");
			System.exit(-1);
		} else {
			analyzer = getAnalyzer(args);
			if (analyzer == null) {
				System.err.println("Issue with the given analyzer arguments: " + args[0] + " " + args[1] + "\nUsage: " + usage + "\nExiting...");
				System.exit(-1);
			}
		}

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.err.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		// get a list of all docs as objects, each doc object has a uniqueID, title,
		// authors, bibliography and words.
		CranfieldDocumentReader documentReader = new CranfieldDocumentReader();
		List<CranfieldDoc> documentList = documentReader.getDocuments(docDir);

		if (documentList == null) {
			System.err.println("Document object list is null, check the reader. Exiting...");
			System.exit(2);
		}

		Directory dir = null;
		try {
			dir = FSDirectory.open(Paths.get(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// The following is used buy QueryDocs.java to get most frequent document terms
		FieldType ft = new FieldType(TextField.TYPE_STORED);
		ft.setTokenized(true);
		ft.setStoreTermVectors(true);
		ft.setStoreTermVectorPositions(true);
		ft.setStoreTermVectorOffsets(true);
		ft.setStoreTermVectorPayloads(true);

		// Indexer uses analyzer specified by the input args
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);

		System.out.println("Starting to index documents...");
		for (int i = 0; i < documentList.size(); i++) {
			indexDoc(writer, documentList.get(i), ft);
		}
		System.out.println("Finish indexing documents...");

		writer.close();
	}

	private static void indexDoc(IndexWriter writer, CranfieldDoc document, FieldType ft) throws IOException {

		Document doc = new Document();

		/* Cranfield Doc ID */
		doc.add(new StringField("id", String.valueOf(document.getId()), Field.Store.YES));

		/* Cranfield Doc Title */
		doc.add(new TextField("title", document.getTitle(), Field.Store.YES));

		/* Cranfield Doc Text */
		doc.add(new Field("contents", document.getWords(), ft));

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			writer.addDocument(doc);
		}
	}

	private static Analyzer getAnalyzer(String[] args) {

		if (!args[0].equals("-analyzer")) {
			return null;
		} else {
			if (args[1].toLowerCase().equals("standard")) {
				System.out.println("Standard analyzer chosen to index the documents...");
				return new StandardAnalyzer();
			} else if (args[1].toLowerCase().equals("english")) {
				System.out.println("English analyzer chosen to index the documents...");
				return new EnglishAnalyzer();
			} else
				return null;
		}
	}

}
