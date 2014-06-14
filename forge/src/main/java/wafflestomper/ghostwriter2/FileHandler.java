package wafflestomper.ghostwriter2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;

import wafflestomper.ghostwriter2.gbook.GBook;
import wafflestomper.ghostwriter2.gbook.GLine;
import wafflestomper.ghostwriter2.gbook.GPage;
import net.minecraft.client.Minecraft;

public class FileHandler {
	
	private File defaultPath;
	private File bookSavePath;
	private File signaturePath;
	
	public File currentPath;
	private List<File> lastListing = new ArrayList();
	private String lastCheckedPath = "";
	
	
	public FileHandler(){
		String path = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
		if (path.endsWith(".")){
			path = path.substring(0, path.length()-2);
		}
		this.defaultPath = new File(path, "mods" + File.separator + "Ghostwriter");
		if (!this.defaultPath.exists()) this.defaultPath.mkdirs();
		this.bookSavePath = new File(defaultPath, "SavedBooks");
		if (!this.bookSavePath.exists()) this.bookSavePath.mkdirs();
		this.signaturePath = new File(defaultPath, "Signatures");
		if (!this.signaturePath.exists()) this.signaturePath.mkdirs();
	}
	
	
	/** Returns a list of non-empty file system roots */
	public List<File> getValidRoots(){
		List<File> outList = new ArrayList();
		for (File root : File.listRoots()){
			if (root.listFiles() != null){
				outList.add(root);
			}
		}
		return outList;
	}
	
	
	/**
	 * Navigates into the parent folder (of this.currentPath)
	 * Used for browsing the file system
	 */
	public void navigateUp(){
		for (File root : File.listRoots()){
			if (this.currentPath.equals(root)){
				return;
			}
		}
		this.currentPath = this.currentPath.getParentFile();
	}
	
	
	/** Returns a directory listing. If the request is the same as the last request, a cached copy will be returned */
	public List<File> listFiles(File path){
		if (!path.getAbsolutePath().equals(this.lastCheckedPath)){
			this.lastCheckedPath = path.getAbsolutePath();
			this.lastListing.clear();
			File[] newList = path.listFiles();
			List<File> files = new ArrayList();
			for (File f : newList){
				if (f.isDirectory()){
					this.lastListing.add(f);
				}
				else{
					files.add(f);
				}
			}
			this.lastListing.addAll(files);
		}
		return this.lastListing;
	}
	
	
	public File getDefaultPath(){
		return this.defaultPath;
	}
	
	
	public File getSignaturePath(){
		return this.signaturePath;
	}
	
	
	/** 
	 * Attempt to load the file at <filePath> as a book.
	 * So far the analysis is very simple, but it works most of the time.
	 */
	public GBook loadBook(File filePath){
		if (filePath.getName().endsWith(".txt")){
			System.out.println("Loading bookworm book...");
			return loadBookwormBook(filePath);
		}
		else if (filePath.getName().endsWith(".ghb")){
			System.out.println("Loading GHB book..." + filePath);
			return loadBookFromGHBFile(filePath);
		}
		//This was not a valid book
		return null;
	}
	
	
	/**
	 * Attempts to load the bookworm book at filePath. If it's successful, the book will be returned as a GBook object (null otherwise)
	 */
	private GBook loadBookwormBook(File filePath){
		List<String> f = readFile(filePath);
		
		//Bookworm txt files are always at least 4 lines long
		//The first line is the ID number
		if (f.size() >= 4 && StringUtils.isNumeric(f.get(0))){
			GBook loadedBook = new GBook();
			loadedBook.clear();
			//There's a good chance this is a bookworm book
			loadedBook.title = GBook.truncateStringChars(f.get(1), "..", 16, false);
			loadedBook.author = GBook.truncateStringChars(f.get(2), "..", 16, false);
			String bookText = f.get(f.size()-1);
			
			//Split the string at any page break (two or more sets of two colons e.g. :: :: or :: :: :: ::)
			String[] largePages = bookText.split("(\\s::){2,}");
			
			//Add the text to the book, replacing any instances of the paragraph break (a single set of colons ::) 
			//with a newline character and two spaces
			for (String largePage : largePages){
				//Pad the last page before a page break with newlines
				if (loadedBook.totalPages() > 0){
					GPage currPage = loadedBook.pages.get(loadedBook.totalPages()-1);
					if (!currPage.asString().isEmpty()){
						currPage = GPage.pad(currPage);
					}
				}
				loadedBook.addText(loadedBook.totalPages(), 0, 0, largePage.replaceAll("\\s*::\\s*", "\n  "), true);
			}
			
			return loadedBook;
		}
		return null;
	}
	
	
	/** Reads a file and returns the contents as a list of strings */
	public List<String> readFile(File path){
		List<String> out = new ArrayList();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			Printer.gamePrint(Printer.RED + "File not found! " + path.getAbsolutePath());
			return null;
		}
	    try {
	        String line = br.readLine();
	        while (line != null) {
	        	out.add(line.replace("\u00C2\u00A7", "\u00A7")); //TODO: Find a way to read the file that doesn't generate these
	            line = br.readLine();
	        }
	    } catch (IOException e) {
			e.printStackTrace();
			Printer.gamePrint(Printer.RED + "Error reading file! " + path.getAbsolutePath());
			return null;
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		return out;
	}
	
	
	/** Writes a list of strings to file (one string per line) */
	public boolean writeFile(List<String> toWrite, File filePath){
		boolean failedFlag = false;
		
		//Create parent directories if any of them don't exist
		File path = filePath.getParentFile();
		if (!path.exists()){
			if (!path.mkdirs()){
				failedFlag = true;
			}
		}
		
		//Write file
		if (!failedFlag){
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			    for (String s : toWrite){
			    	out.println(s);
			    }
			    out.close();
			} 
			catch (IOException e) {
				failedFlag = true;
				System.out.println("Ghostwriter: Write failed!");
				System.out.println(e.getMessage());
				return false;
			}
		}
		
		if (failedFlag){
			Printer.gamePrint(Printer.RED + "WRITING TO DISK FAILED!");
			return false;
		}		
		return true;
	}
	
	
	/**
	 * Removes Java-style comments, whitespace preceding linebreak and pagebreak symbols, and newline characters (\n) from a string
	 */
	public static String cleanGHBString(String strIn){
		//Remove single-line comments (i.e. lines (or parts of lines) that start with //)
		strIn = strIn.replaceAll("(?s)//.*?((\\n)|(\\r\\n)|(\\Z))","\n");
		//Remove multi-line comments (anything between /* and */ symbols
		strIn = strIn.replaceAll("(?s)((/\\*).*?((\\*/)|(\\Z)))|(((/\\*)|(\\A)).*?(\\*/))", "");
		//remove whitespace preceding linebreak and pagebreak characters
		strIn = strIn.replaceAll("[\\t\\r\\n ]+(##|>>>>)", "$1");
		//remove newline and carriage return characters
		strIn = strIn.replaceAll("[\\r\\n]", "");
		return strIn;
	}
	
	
	private GBook loadBookFromGHBFile(File filePath){
		GBook loadedBook = new GBook();
		loadedBook.clear();
		
		//Read the file
		List<String> rawFile = readFile(filePath);
		if (rawFile == null || rawFile.isEmpty()){
			//File was not read successfully
			return null;
		}
		
		//Remove comments and anything else that can't be stored in a Minecraft book
		String concatFile = "";
		for (String line : rawFile){
			//TODO: Eliminate this code repetition
			if (line.toLowerCase().startsWith("title:") && loadedBook.title.isEmpty()){
				if (line.length() >= 7){
					loadedBook.title = loadedBook.title = GBook.truncateStringChars(cleanGHBString(line.substring(6)).trim(), "..", 16, false);
					if (line.contains("/*")){
						concatFile += line.substring(line.indexOf("/*")) + "\n";
					}
				}
			}
			else if (line.toLowerCase().startsWith("author:") && loadedBook.author.isEmpty()){
				if (line.length() >= 8){
					loadedBook.author = GBook.truncateStringChars(cleanGHBString(line.substring(7)).trim(), "..", 16, false);
					if (line.contains("/*")){
						concatFile += line.substring(line.indexOf("/*")) + "\n";
					}
				}
			}
			else{
				concatFile += line + "\n";
			}
		}
		
		//Remove comments and anything else that doesn't belong in a Minecraft book 
		concatFile = cleanGHBString(concatFile);
		
		//Convert all the linebreak characters (##) to newline characters (\n) and split into pages
		concatFile = concatFile.replaceAll("##", "\n");
		
		//Split the text at the pagebreak symbol then add it to the book
		String[] pageBroken = concatFile.split(">>>>");
		for (String largePage : pageBroken){
			//Pad the last page before a page break with newlines
			if (loadedBook.totalPages() > 0){
				GPage currPage = loadedBook.pages.get(loadedBook.totalPages()-1);
				if (!currPage.asString().isEmpty()){
					currPage = GPage.pad(currPage);
				}
			}
			loadedBook.addText(loadedBook.totalPages(), 0, 0, largePage, true);
		}
		
		return loadedBook;
	}
	
	
	/** 
	 * Write a GBook to a GHB format text file.
	 * Returns true if the save was successful, false otherwise.
	 */
	public boolean saveBookToGHBFile(GBook book){
		//TODO: Add a short primer on GHB markup to the start of every saved book
		Printer.gamePrint(Printer.GRAY + "Saving book to file...");
		List<String> toWrite = new ArrayList();
		String utcTime = getUTCString();
		
		//Add metadata
		toWrite.add("//Book saved in GHB format at " + utcTime);
		if (!book.title.isEmpty()){toWrite.add("title:" + book.title);}
		if (!book.author.isEmpty()){toWrite.add("author:" + book.author);}
		toWrite.add("//=======================================\n");
		
		//Add page contents
		GPage currPage;
		String pageMarker = "/////////// Page %d: ///////////";
		for (int i=0; i<book.totalPages(); i++){
			//Add the page number as a comment
			toWrite.add(String.format(pageMarker, (i+1)));
			
			//Add the contents of the current page, replacing newline characters with the GHB newline symbol (##)
			for (GLine line : book.pages.get(i).lines){
				toWrite.add(line.text.replaceAll("\n", "##"));
			}
			
			//Add page breaks
			if (i < book.totalPages()-1){
				toWrite.add(">>>>\n");
			}
		}
		
		String title = book.title.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
		String author = book.author.trim().replaceAll(" ", ".").replaceAll("[^a-zA-Z0-9\\.]", "");
		if (title.isEmpty()){title = "notitle";
		if (author.isEmpty()){author = "noauthor";}
		}
		File saveFile = new File(this.bookSavePath, title + "_" + author + "_" + utcTime + ".ghb");
		if (writeFile(toWrite, saveFile)){
			Printer.gamePrint(Printer.GREEN + "Book saved to: " + saveFile);
			return true;
		}
		else{
			Printer.gamePrint(Printer.RED + "WRITING BOOK TO DISK FAILED!");
			return false;
		}
	}
	
	
	/** Returns a (mostly) ISO8601 compliant date/time string representing the current Zulu time. */
	public String getUTCString(){
		TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HHmm.S'Z'");
	    df.setTimeZone(tz);
	    return df.format(new Date());
	}
}
