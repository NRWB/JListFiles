
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeFiles {

	private final Path origin;

	public ComputeFiles(final Path p) {
		this.origin = p;
	}

	public void compute(final boolean logOutputToFile) throws IOException, NoSuchAlgorithmException {
		Map<Path, String> result = new HashMap<Path, String>();
		FileSearcher lf = new FileSearcher(this.origin);
		List<Path> allFiles = lf.getList();
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
			result.put(entry, Digest.getDigestHash(md.digest()));
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
		if (logOutputToFile)
			dupes = new ArrayList<String>();

		int entryCount = 0;

		//BigInteger fileSize = BigInteger.ZERO;
		long fileSize = 0L;

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

			List<Path> temp = getKeysFromValue(entry.getKey(), result);

			long fs = Files.size(temp.get(0));
			long ttl_fs = fs * (entry.getValue() - 1);

			sb.append("Original File Size = " + fs);
			sb.append("Total Size = " + ttl_fs);

			sb.append(System.getProperty("line.separator"));

			sb.append("Associated File Paths = ");
			sb.append(System.getProperty("line.separator"));
			
			for (Path p : temp) {
				sb.append(p.toString());
				sb.append(System.getProperty("line.separator"));
			}

			//fileSize = fileSize.add(BigInteger.valueOf(ttl_fs));
			fileSize += ttl_fs;
			sb.append(System.getProperty("line.separator"));

			if (logOutputToFile)
				dupes.add(sb.toString());
		}

		//System.out.println("Excess disk space used: " + fileSize.toString() + " bytes");

		System.out.print("Excess disk space used: " + fileSize + " bytes");
		System.out.println(" or " + fileSize);

		if (logOutputToFile) {

			StringBuilder fileName = new StringBuilder();
			fileName.append(this.origin.toString());
			fileName.append(System.getProperty("file.separator"));
			fileName.append("duplicates-");
			fileName.append(System.currentTimeMillis());
			fileName.append(".txt");

			Path filePath = Paths.get(fileName.toString());

			Files.createFile(filePath);

			StringBuilder ln = new StringBuilder();
			ln.append("Excess disk space used: ");
			ln.append(String.valueOf(fileSize));
			ln.append(" bytes or ");
			ln.append(fileSize);
			ln.append(System.getProperty("line.separator"));

			final List<String> firstLine = new ArrayList<String>();
			firstLine.add(ln.toString());

			Files.write(filePath, firstLine, StandardCharsets.UTF_8);

			Files.write(filePath, dupes, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
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

}
