import com.constellio.app.start.ApplicationStarter;
import com.constellio.data.conf.FoldersLocator;

import java.io.File;

public class StartApplicationMain {

	private StartApplicationMain() {
	}

	public static void main(String[] args)
			throws Exception {

		File webContent = new FoldersLocator().getAppProjectWebContent();
		ApplicationStarter.startApplication(true, webContent, 7070);
	}
}
