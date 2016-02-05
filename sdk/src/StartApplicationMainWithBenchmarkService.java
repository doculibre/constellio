import java.io.File;

import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.conf.FoldersLocator;

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
