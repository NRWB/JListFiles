import java.nio.file.Path;

/**
 * Record/Entry/Row/Tuple/etc.
 * 
 * @author Nick
 *
 */
public class Record {

	private final String hash;
	private final Path file;

	public Record(final String s, final Path p) {
		this.hash = s;
		this.file = p;
	}

	public String getHash() {
		return this.hash;
	}

	public Path getFilePath() {
		return this.file;
	}

	@Override
	public String toString() {
		return "{hash: " + getHash() + ", path: " + getFilePath() + "}";
	}
}
