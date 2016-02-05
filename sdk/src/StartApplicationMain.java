import java.io.File;

import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.conf.FoldersLocator;

public class StartApplicationMain {

	private StartApplicationMain() {
	}

	public static void main(String[] args)
			throws Exception {

		File webContent = new FoldersLocator().getAppProjectWebContent();
		ApplicationStarter.startApplication(true, webContent, 7070);
	}
}
