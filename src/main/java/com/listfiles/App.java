import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class App {

	public static void main(String[] args) {
		args = new String[] { System.getProperty("user.home") + "\\Documents\\eclipse workspace external jars\\" };
		if (args.length != 1)
			return;
		ComputeFiles cf = new ComputeFiles(Paths.get(args[0]));
		long startTime = System.currentTimeMillis();
		try {
			cf.compute(true);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed time (MS): " + (endTime - startTime));
	}
}
