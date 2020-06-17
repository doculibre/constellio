import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.conf.FoldersLocator;

import java.io.File;

public class StartApplicationMainWithBenchmarkService {

	private StartApplicationMainWithBenchmarkService() {
	}

	public static void main(String[] args)
			throws Exception {

		System.setProperty("benchmarkServiceEnabled", "true");
		File webContent = new FoldersLocator().getAppProjectWebContent();
		ApplicationStarter.startApplication(true, webContent, 7070);
	}

}
