import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/**
 * Lists duplicate files via md5
 * 
 * Given a root/starting directory, the program attempts to go through all directories and look at all files.
 * The files are all given a md5 run through.
 * 
 * @author Nick
 *
 */
public class App {

	/**
	 * sorted related links
	 * http://stackoverflow.com/questions/27677256/java-8-streams-to-find-the-duplicate-elements
	 * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
	 * http://stackoverflow.com/questions/22515889/how-to-sort-the-words-by-their-frequency
	 * https://webcache.googleusercontent.com/search?q=cache:3FMPEaD0YikJ:https://blog.jooq.org/2014/01/10/java-8-friday-goodies-java-io-finally-rocks/+&cd=4&hl=en&ct=clnk&gl=us
	 * http://webcache.googleusercontent.com/search?q=cache:Aeff9VGXshAJ:codingjunkie.net/globbing-directories-in-java/+&cd=5&hl=en&ct=clnk&gl=us
	 * 
	 */

	private static final List<Path> allFiles = new ArrayList<Path>();

	/**
	 * 
	 * @param args
	 * [0] = Folder to use as root folder to recursively explore
	 * [1] = save output to file
	 * 
	 * @throws Exception Multiple exceptions covered, this is more a useful way to reduce exception code in this snippet
	 */
	public static void main(String[] args) throws Exception {
		
		// to do: ??? replace duplicates and just link back to one file?
		// -------> could be troublesome tho if files get moved around...
		// Files.createSymbolicLink(link, reativeSrc);
		
		//args = new String[] { "C:\\Users\\Nick\\Documents\\School\\", "true" };
		args = new String[] { System.getProperty("user.home") + "\\Documents\\eclipse workspace external jars\\", "true" };
		if (args.length != 2) {
			System.err.println("Args length error");
			System.exit(-1);
		}
		boolean save = Boolean.valueOf(args[1]);

		Map<Path, String> result = new HashMap<Path, String>();

		listFiles(Paths.get(args[0]));

		for (Path entry : allFiles) {
			if (Files.isDirectory(entry))
				continue;
			MessageDigest md = MessageDigest.getInstance("MD5");
			//System.out.println(entry);
			try (InputStream is = Files.newInputStream(entry); DigestInputStream dis = new DigestInputStream(is, md)) {
				byte[] buffer = new byte[ 4096 ];
				while (dis.read(buffer) != -1) { /* Does nothing with contents */ };
				dis.close();
			}
			result.put(entry, getDigestHash(md.digest()));
		}

		Map<String, Integer> resultHits = new HashMap<String, Integer>();

		for (Map.Entry<Path, String> entry : result.entrySet()) {
			final String s = entry.getValue();
			if (resultHits.containsKey(s)) {
				final int count = resultHits.get(s) + 1;

				// do not need to save the old value when putting in the new count
				/* final int oldValue = */ resultHits.put(s, count);
			} else {
				resultHits.put(s, 1);
			}
		}

		List<String> dupes = null;
		if (save)
			dupes = new ArrayList<String>();

		int entryCount = 0;

		BigInteger fileSize = BigInteger.ZERO;

		for (Map.Entry<String, Integer> entry : resultHits.entrySet()) {

			if (entry.getValue() < 2)
				continue;

			StringBuilder sb = new StringBuilder();

			sb.append("Entry # = ");
			sb.append(entryCount);
			++entryCount;

			sb.append(System.getProperty("line.separator"));

			sb.append("Hash = ");
			sb.append(entry.getKey());
			sb.append(", hit count = ");
			sb.append(entry.getValue());

			sb.append(System.getProperty("line.separator"));

			sb.append("Associated File Paths = ");
			List<Path> temp = getKeysFromValue(entry.getKey(), result);
			for (Path p : temp) {
				sb.append(p.toString());
				sb.append(System.getProperty("line.separator"));
			}

			long fs = Files.size(temp.get(0));
			long ttl_fs = fs * (entry.getValue() - 1);
			fileSize = fileSize.add(BigInteger.valueOf(ttl_fs)); 
			sb.append(System.getProperty("line.separator"));

			if (save)
				dupes.add(sb.toString());
		}

		System.out.println("Excess disk space used: " + fileSize.toString() + " bytes");

		if (save) {
			Files.createFile(Paths.get(args[0] + "duplicates.txt"));
			Files.write(Paths.get(args[0] + "duplicates.txt"), dupes);
		}
	}

	// http://stackoverflow.com/questions/4005816/map-how-to-get-all-keys-associated-with-a-value
	private static List<Path> getKeysFromValue(String hashVal, Map<Path, String> map) {
		List<Path> result = new ArrayList<Path>();
		for (Map.Entry<Path, String> entry : map.entrySet()) {
			if (entry.getValue().equals(hashVal))
				result.add(entry.getKey());
		}
		return result;
	}

	private static void listFiles(Path path) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	        for (Path entry : stream) {
	            if (Files.isDirectory(entry))
	                listFiles(entry);
	            allFiles.add(entry);
	        }
	    }
	}

	private static final char[] base16 = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static String getDigestHash(final byte[] arr) {
		final int len = arr.length;
		StringBuilder result = new StringBuilder(len * 2);
	    for (int i = 0; i < len; ++i) {
	    	final byte b = arr[i];
	    	result.append(base16[(b >> 4) & 0xF]);
	    	result.append(base16[(b & 0xF)]);
	    }
	    return result.toString();
	}
}
