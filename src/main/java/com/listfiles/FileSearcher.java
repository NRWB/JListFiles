
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSearcher {

	private final Path origin;
	private final List<Path> paths;

	public FileSearcher(final Path p) {
		this.origin = p;
		this.paths = new ArrayList<Path>();
	}

	private void listFilesRecursively(Path path) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
	        for (Path entry : stream) {
	            if (Files.isDirectory(entry))
	            	listFilesRecursively(entry);
	            this.paths.add(entry);
	        }
	    }
	}

	public List<Path> getList() {
		try {
			listFilesRecursively(this.origin);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.paths;
	}
}
