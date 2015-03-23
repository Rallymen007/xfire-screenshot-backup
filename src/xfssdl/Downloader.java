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
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Downloader extends Observable {
	private static final String BASE_URL = "http://classic.xfire.com";

	private String username = null;
	private String downloadPath = null;
	private Boolean screens = true;
	private Boolean videos = true;
	
	public Downloader(String[] args){
		// CLI log observer
		addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				System.out.print(arg);
			}
		});
		
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
		} catch (Exception e) {}
	}
	
	public void setup() throws MalformedURLException, FileNotFoundException, IOException, InterruptedException, ParserConfigurationException, SAXException {
		if(username == null || downloadPath == null){
			// Not all the parameters are set, start the UI
			addObserver(new BiteFrame(this));
		} else {
			// CLI mode
			startDownload();
		}
	}
	
	public boolean setParameters(String u, String d, Boolean s, Boolean v){
		username = u;
		downloadPath = d;
		screens = s;
		videos = v;
		return u != null && d != null && !u.equals("") && !downloadPath.equals("");
	}
	
	public boolean startDownload() throws MalformedURLException, FileNotFoundException, IOException, InterruptedException, ParserConfigurationException, SAXException {
		if(username == null || downloadPath == null){return false;}
		
		if(screens){
			doDownload("screenshots", "screenshot", "id", ".jpg");
		}
		
		if(videos){
			doDownload("user_videos", "video", "videoid", ".mp4");
		}
		return true;
	}

	private void doDownload(String profileURLPath, String xmltag, String idstring, String extension) throws IOException,
	InterruptedException, MalformedURLException, FileNotFoundException, ParserConfigurationException, SAXException {
		String url = BASE_URL + "/xml/" + username + "/" + profileURLPath;
		
		// Retrieve XML data
		URLConnection connection;
		InputStream is = null;
		while (is == null) {
			try {
				// Fetch URL
				connection = new URL(url).openConnection();
				is = connection.getInputStream();
			} catch (IOException e) {
				eprint(e.getMessage() + "\nRetrying....\n");
				Thread.sleep(100);
			}
		}
		print(url + " retrieved successfully\n");
		
		// Build the DOM
		DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
		Document doc = objDocumentBuilder.parse(is);

		NodeList nl = doc.getElementsByTagName(xmltag);
		print("Found " + nl.getLength() + " "+ xmltag + "s\n");
		
		for (Integer index = 0; index < nl.getLength(); index++) {
			Node n = nl.item(index);
			if(n instanceof Element){
				Element game = (Element) n;
				String gameName = game.getElementsByTagName("game").item(0).getTextContent(),
						id = game.getElementsByTagName(idstring).item(0).getTextContent();
								
				print("Processing " + xmltag + " id " + id + " for " +  gameName  + "\n");
				
				// Create the directory
				File folder = new File(downloadPath + "/" + FileNameCleaner.cleanFileName(gameName));
				if(!folder.exists()) {
					folder.mkdirs();
				}
				
				String contentURL = "";
				if(xmltag.equals("screenshot")){
					contentURL = game.getElementsByTagName("url").item(4).getTextContent();
				} else {
					contentURL = game.getElementsByTagName("raw_url").item(0).getTextContent();
				}
				String completeFilePath = folder.getAbsolutePath() + "/" + id + extension;
				
				// Skip the file if it has already been downloaded
				if(!new File(completeFilePath).exists()){
					// Download the actual screenshot or video depending on the parameter passed
					URL website = new URL(contentURL);
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					FileOutputStream fos = new FileOutputStream(completeFilePath);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
				} else {
					print("File " + completeFilePath + " already downloaded");
				}
			}
		}
	}
	
	
	private void eprint(String s){
		print("ERROR : " + s);
	}
	
	private void print(String s){
		setChanged();
		notifyObservers(s);
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
