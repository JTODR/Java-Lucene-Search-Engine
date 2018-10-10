package parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
	
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
				System.out.println("LINE: " + line );
				fileContent.append(line + " ");
				//fileContent.append("\n");
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
	
	public static List<String> parse(String text, String[] delimiters){
		List<String> resultList = new ArrayList<>();
	
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
