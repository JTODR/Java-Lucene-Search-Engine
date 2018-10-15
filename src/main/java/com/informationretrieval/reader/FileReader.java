package com.informationretrieval.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileReader {
	
	private static String commonWordsFile = "data/CommonWordsList.txt";
	
	public static List<String> getCommonWordList() {
		String commonWords = "";
		Path commonWordsPath = Paths.get(commonWordsFile);
		
		if (!Files.isReadable(commonWordsPath)) {
			System.err.println("Common words file '" + commonWordsPath.toAbsolutePath() + "' does not exist or is not readable, please check the path");
			System.exit(1);
		}
		else {
			commonWords = readFile(commonWordsPath);
		}
		return new ArrayList<String>(Arrays.asList(commonWords.split(" ")));
	}
	
	public static String readFile(Path path) {		
		
		InputStream stream = null;
		try {
			stream = Files.newInputStream(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line = null;
		StringBuilder fileContent = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		// read in file
		try {
			while((line = in.readLine()) != null) {
				fileContent.append(line + " ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println(fileContent.toString().substring(0,5000));
		System.out.println("Document read successfully...");
		return fileContent.toString();
	}
	
	public static List<String> splitFileOnDelimiters(String text, String[] delimiters){
		List<String> resultList = new ArrayList<String>();
	
		for(int i = 0; i < delimiters.length; i++) {
			
			// if the remaining text is the .W delimiter then the bibliography and words are empty
			if(text.equals(".W")) {	
				resultList.add("");
				resultList.add("");
			}
			else if(delimiters[i].equals(".W")) {	
				resultList.add(text.split(delimiters[i])[0]);	
				resultList.add(text.split(delimiters[i])[1]);  	// the words attribute is the remaining part of the text after split on ".W"
			}
			else {
				resultList.add(text.split(delimiters[i])[0]);
				text = text.split(delimiters[i])[1];
			}
		}
		
		return resultList;
	}


}
