package xfssdl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 20/03/2015 - 22:02
 * @author Franck
 *
 */
public class Main {
	private static final String BASE_URL = "http://social.xfire.com";
	
	private static String username;
	private static String downloadPath;

	public static void main(String[] args) throws Exception {
		// TODO: add actual parsing
		username = "rallymen007";
		downloadPath = "E:\\\\Xfire";

		String htmlString = getStringForURL(BASE_URL + "/users/" + username + "/screenshots");
	    
	    // regex to match screenshot urls and long names
	    Pattern patternURL = Pattern.compile("/users/"+username+"/games/[A-Za-z0-9]+/screenshots"),
	    		patternTitles = Pattern.compile("<h1>[^\\<]+</h1>");
	    Matcher matcherURL = patternURL.matcher(htmlString),
	    		matcherTitles = patternTitles.matcher(htmlString);
	    Map<String, String> games = new HashMap<String, String>();
	    System.out.print("Finding screenshot URLs...");
	    
	    // List all the games found
	    while(matcherURL.find()){
	    	matcherTitles.find();
	        games.put(matcherTitles.group().replaceAll("<[/]?h1>", ""), matcherURL.group());
	    }
	    System.out.println("done");
	    
	    // Print the list of names found
	    String print = "Found " + games.size() + " games -- make sure this is accurate\n";
	    Integer i = 0;
	    for(String e : games.keySet()){
	    	// Stupid print
	    	print+=e + ((++i%5==0)?'\n':'\t');
	    }
	    System.out.println(print);
	    
	    
	    for(Entry<String, String> game : games.entrySet()){
	    	// Create the directory
	    	File folder = new File(downloadPath + "/" + FileNameCleaner.cleanFileName(game.getValue()));
	    	folder.mkdirs();
	    	
	    	// Get the screenshots page
	    	htmlString = getStringForURL(BASE_URL + game.getKey());
	    	
	    	// List all the screenshots URLs
	    	Pattern patternScreenURL = Pattern.compile();
	    	Matcher matcherScreenURL
	    }
	}

	private static String getStringForURL(String url) throws IOException {
		URLConnection connection;
		String htmlString;
		InputStream is = null;
		while(is == null){
			try{
				// Fetch URL
				connection = new URL(url).openConnection();
				is = connection.getInputStream();
			} catch(IOException e){
				System.err.println(e.getMessage() + "\nRetrying....");
			}
		}
		System.out.println(url + " retrieved successfully");
		
		// Get the whole data in a String - this avoids losing connection midway through parsing
		System.out.print("Reading URL...");
		java.util.Scanner sc = new java.util.Scanner(is);
		sc.useDelimiter("\\A");
	    htmlString = sc.hasNext() ? sc.next() : "";
	    sc.close();
	    is.close();
	    System.out.println("done");
		return htmlString;
	}
	
	// Thanks StackOverflow <3
	public static class FileNameCleaner {
		final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
		static {
		    Arrays.sort(illegalChars);
		}
		public static String cleanFileName(String badFileName) {
		    StringBuilder cleanName = new StringBuilder();
		    for (int i = 0; i < badFileName.length(); i++) {
		        int c = (int)badFileName.charAt(i);
		        if (Arrays.binarySearch(illegalChars, c) < 0) {
		            cleanName.append((char)c);
		        }
		    }
		    return cleanName.toString();
		}
		}
}
