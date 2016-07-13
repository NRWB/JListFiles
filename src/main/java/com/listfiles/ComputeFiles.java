
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

		// obtain all files
		FileSearcher lf = new FileSearcher(this.origin);
		List<Path> allFiles = lf.getList();

		List<Record> result = new ArrayList<Record>();

		// compute hash for every file, also add file path + hash to list
		for (Path entry : allFiles) {
			if (Files.isDirectory(entry))
				continue;
			MessageDigest md = MessageDigest.getInstance("MD5");
			try (InputStream is = Files.newInputStream(entry); DigestInputStream dis = new DigestInputStream(is, md)) {
				byte[] buffer = new byte[4096]; // 4096 8192 16384 http://stackoverflow.com/questions/236861/how-do-you-determine-the-ideal-buffer-size-when-using-fileinputstream
				while (dis.read(buffer) != -1) {
					// Does nothing with contents
				}
				dis.close();
			}
			result.add(new Record(Digest.getDigestHash(md.digest()), entry));
		}

		// attempt to condense the results based on same hashes
		Map<String, List<Path>> redux = new HashMap<String, List<Path>>();
		for (Record r : result) {
			if (redux.containsKey(r.getHash())) {
				List<Path> indexed = redux.get(r.getHash());
				indexed.add(r.getFilePath());
				redux.put(r.getHash(), indexed);
			} else {
				List<Path> indexed = new ArrayList<Path>();
				indexed.add(r.getFilePath());
				redux.put(r.getHash(), indexed);
			}
		}

		if (logOutputToFile) {
			List<String> findings = new ArrayList<String>();

			final String nl = System.getProperty("line.separator");

			long diskSpaceSum = 0L;

			for (Map.Entry<String, List<Path>> entry : redux.entrySet()) {
				final List<Path> dupeFiles = entry.getValue();
				final int hits = dupeFiles.size();
				if (hits < 2) {
					continue; // b/c not a duplicate finding, when there are not 2 or more counts of the same hash
				}
				final String hash = entry.getKey();

				StringBuilder sb = new StringBuilder();
				sb.append("hash = " + hash + nl); // String concatenation = http://stackoverflow.com/questions/1532461/stringbuilder-vs-string-concatenation-in-tostring-in-java
				sb.append("hits = " + hits + nl);

				sb.append("original file size = ");
				final long fs = Files.size(dupeFiles.get(0));
				sb.append(fs);
				sb.append(nl);

				sb.append("total disk space = ");
				final long ttlfs = fs * hits;
				sb.append(ttlfs);
				sb.append(nl);

				diskSpaceSum += ttlfs;

				sb.append("associated file paths = ");
				sb.append(nl);
				for (Path p : dupeFiles) {
					sb.append(p.toString() + nl);
				}

				findings.add(sb.toString());
			}

			findings.add("Total disk space covered with duplicates: " + humanReadableByteCount(diskSpaceSum, true));

			StringBuilder fileName = new StringBuilder();
			fileName.append(this.origin.toString());
			fileName.append(System.getProperty("file.separator"));
			fileName.append("duplicates-");
			fileName.append(System.currentTimeMillis());
			fileName.append(".txt");

			Path filePath = Paths.get(fileName.toString());

			Files.createFile(filePath);

			Files.write(filePath, findings, StandardCharsets.UTF_8);
			//Files.write(filePath, findings, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		}
	}

	// http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	private static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
