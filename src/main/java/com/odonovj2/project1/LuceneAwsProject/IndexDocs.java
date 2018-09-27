package com.odonovj2.project1.LuceneAwsProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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
		
		System.out.println(docDir);
		System.out.println(docDir.toAbsolutePath());
		System.out.println(docsPath);

		parseCranfieldDoc(docDir);
		  
		
	}
	
	private static void parseCranfieldDoc(Path path) {
		try (InputStream stream = Files.newInputStream(path)) {
			// make a new, empty document
			// Document doc = new Document();

			int streamByte = 0;

			while ((streamByte = stream.read()) != -1)
				System.out.print((char) streamByte);
		}
		catch (IOException e1) {

			e1.printStackTrace();
		} 
	}
}
