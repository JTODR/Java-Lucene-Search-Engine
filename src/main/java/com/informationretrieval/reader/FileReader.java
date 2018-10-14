package com.informationretrieval.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileReader {
	
	private static List<String> commonWordList = new ArrayList<String>();
	
	public static List<String> getCommonWordList() {
		commonWordList.add("must");
		commonWordList.add("above");
		commonWordList.add("what");
		commonWordList.add("would");
		commonWordList.add("should");
		commonWordList.add("when");
		commonWordList.add("if");
		commonWordList.add("far");
		commonWordList.add("above");
		commonWordList.add("do");
		commonWordList.add("we");
		commonWordList.add("can");
		commonWordList.add("does");
		commonWordList.add("have");
		commonWordList.add("over");
		commonWordList.add("how");
		commonWordList.add("can't");
		commonWordList.add("like");
		commonWordList.add("been");
		commonWordList.add("did");
		commonWordList.add("which");
		commonWordList.add("why");
		commonWordList.add("else");
		commonWordList.add("find");
		commonWordList.add("has");
		commonWordList.add("any");
		commonWordList.add("done");
		commonWordList.add("best");
		commonWordList.add("anyone");
		commonWordList.add("on");
		commonWordList.add("result");
		commonWordList.add("number");
		commonWordList.add("from");
		commonWordList.add("be");
		commonWordList.add("of");
		commonWordList.add("about");
		commonWordList.add("along");
		commonWordList.add("being");
		commonWordList.add("simple");
		commonWordList.add("practical");
		commonWordList.add("possible");
		commonWordList.add("information");
		commonWordList.add("pertaining");
		commonWordList.add("very");
		commonWordList.add("available");
		commonWordList.add("details");

		return commonWordList;
	}
	
	public String[] readFile(Path path) {		
		
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
		
		// split the file using UniqueID as the delimiter

		String[] parts = fileContent.toString().split(".I");
		String[] returnArray = Arrays.copyOfRange(parts, 1, parts.length);		// remove 1st item from array

		return returnArray;
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
