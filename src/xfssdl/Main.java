package xfssdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 20/03/2015 - 22:02
 * 
 * @author Franck
 *
 */
public class Main {
	private static final String BASE_URL = "http://social.xfire.com";

	private static String username = null;
	private static String downloadPath = null;
	private static Boolean screens = true;
	private static Boolean videos = true;

	public static void main(String[] args) throws Exception {
		if (!processArgs(args)) {
			System.err
					.println("Incorrect parameters\n"
							+ "Usage: java -jar xfssdl.jar -username <username> -path <download path> [-videos (on|off) -screenshots (on|off)]\n"
							+ "Defaults: downloads both screenshots and videos for the username");
			System.exit(-1);
		}

		System.out.println("User: " + username + " Download path: " + downloadPath);

		if(screens){
			downloadScreenshots();
		}
		
		if(videos){
			downloadVideos();
		}
	}

	private static void downloadVideos() throws IOException, InterruptedException, MalformedURLException, FileNotFoundException {
		doDownload("videos");
	}
	
	private static void downloadScreenshots() throws IOException,
			InterruptedException, MalformedURLException, FileNotFoundException {
		doDownload("screenshots");
	}
	
	private static void doDownload(String profileURLPath) throws IOException,
	InterruptedException, MalformedURLException, FileNotFoundException {
		String htmlString = getStringForURL(BASE_URL + "/users/" + username + "/" + profileURLPath);

		// regex to match screenshot urls and long names
		Pattern patternURL = Pattern.compile("/users/" + username + "/games/[A-Za-z0-9]+/" + profileURLPath), 
				patternTitles = Pattern.compile("<h1>[^\\<]+</h1>");
		Matcher matcherURL = patternURL.matcher(htmlString), 
				matcherTitles = patternTitles.matcher(htmlString);
		Map<String, String> games = new HashMap<String, String>();
		System.out.print("Finding screenshot URLs...");

		// List all the games found
		while (matcherURL.find()) {
			matcherTitles.find();
			games.put(matcherTitles.group().replaceAll("<[/]?h1>", ""), matcherURL.group());
		}
		System.out.println("done");

		// Print the list of names found
		String print = "Found " + games.size() + " games -- make sure this is accurate\n";
		Integer i = 0;
		for (String e : games.keySet()) {
			// Stupid print
			print += e + ((++i % 5 == 0) ? '\n' : '\t');
		}
		System.out.println(print);

		for (Entry<String, String> game : games.entrySet()) {
			// Skip the page if there are no screenshots (poor detection, I know)
			if(htmlString.contains("There are no")){
				System.out.println("Skipping game " + game.getKey() + " because it has no screenshots.");
				continue;
			}
			
			System.out.println("Processing game " + game.getKey());
			// Create the directory
			File folder = new File(downloadPath + "/" + FileNameCleaner.cleanFileName(game.getKey()));
			folder.mkdirs();

			// Get the screenshots page
			htmlString = getStringForURL(BASE_URL + game.getValue());

			// List all the screenshots URLs
			Pattern patternScreenURL = Pattern.compile("/" + profileURLPath +"/[0-9a-z]+");
			Matcher matcherScreenURL = patternScreenURL.matcher(htmlString);

			while (matcherScreenURL.find()) {
				String id = matcherScreenURL.group().replaceFirst("/"+ profileURLPath + "/", "");
				// Download the actual screenshot or video depending on the parameter passed
				URL website = new URL("screenshots".equals(profileURLPath)
									?"http://screenshot.xfire.com/s/"+ id +"-4.jpg"
									:"http://video.xfire.com/" + id + ".mp4");
				ReadableByteChannel rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(folder.getAbsolutePath() + "/" + id + ".jpg");
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			}
		}
	}

	private static boolean processArgs(String[] args) {
		// Argument parsing
		try {
			for (int i = 0; i < args.length; i += 2) {
				switch (args[i]) {
				case "-username":
					username = args[i + 1];
					break;
				case "-path":
					downloadPath = args[i + 1];
					break;
				case "-videos":
					videos = "on".equals(args[i+1]);
					break;
				case "-screens":
					screens = "on".equals(args[i+1]);
					break;
				default:
					;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return username != null && downloadPath != null;
	}

	private static String getStringForURL(String url) throws IOException,
			InterruptedException {
		URLConnection connection;
		String htmlString;
		InputStream is = null;
		while (is == null) {
			try {
				// Fetch URL
				connection = new URL(url).openConnection();
				is = connection.getInputStream();
			} catch (IOException e) {
				System.err.println(e.getMessage() + "\nRetrying....");
				Thread.sleep(100);
			}
		}
		System.out.println(url + " retrieved successfully");

		// Get the whole data in a String - this avoids losing connection midway
		// through parsing
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
		final static int[] illegalChars = { 34, 60, 62, 124, 0, 1, 2, 3, 4, 5,
				6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
				23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47 };
		static {
			Arrays.sort(illegalChars);
		}

		public static String cleanFileName(String badFileName) {
			StringBuilder cleanName = new StringBuilder();
			for (int i = 0; i < badFileName.length(); i++) {
				int c = (int) badFileName.charAt(i);
				if (Arrays.binarySearch(illegalChars, c) < 0) {
					cleanName.append((char) c);
				}
			}
			return cleanName.toString();
		}
	}
}
