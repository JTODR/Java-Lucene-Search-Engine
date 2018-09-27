package index;

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
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
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

		parseCranfieldFile(docDir);

	}

	private static void parseCranfieldFile(Path path) {
		try (InputStream stream = Files.newInputStream(path)) {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			StringBuilder fileContent = new StringBuilder();

			// read in the whole cranfield file
			while((line = in.readLine()) != null) {
				fileContent.append(line);
			}
			
			// split the cranfield file using UniqueID as the delimiter
			String[] parts = fileContent.toString().split(".I");			
			
			// start for loop at i=1 as first string is empty for some reason...
			for(int i = 1; i < parts.length; i++) {
				parseDoc(parts[i]);
			}
			
		}
		catch (IOException e1) {

			e1.printStackTrace();
		} 
	}
	
	private static void parseDoc(String currDoc) {
		
		int docId;
		String title, authors, biblography, words, temp;
		
		// parse out ID of current doc
		docId = Integer.parseInt(currDoc.split(".T")[0].trim());
		temp = currDoc.split(".T")[1];
		
		// parse out title of current doc
		title = temp.split(".A")[0];
		temp = temp.split(".A")[1];
		
		// parse out authors of current doc
		authors = temp.split(".B")[0];
		temp = temp.split(".B")[1];
		
		// check if all that is left is ".W", if so, biblography and words are both empty
		if(temp.equals(".W")) {
			biblography = "";
			words = "";
		}
		else {
			// parse out bib and words of current doc
			biblography = temp.split(".W")[0];
			words = temp.split(".W")[1];
		}
		
		System.out.println("ID: " + docId);
		System.out.println("TITLE: " + title);
		System.out.println("AUTHORS: " + authors);
		System.out.println("BIBLOGRAPHY: " + biblography);
		System.out.println("WORDS: " + words + "\n\n");
		
		createNewCranfieldDoc(docId, title, authors, biblography, words);
	}
	
	private static void createNewCranfieldDoc(int docId, 
			String title, 
			String authors,
			String biblography, 
			String words) {
		
		
		
	}
}
