package demo;

import java.io.File;

public class StartDemoWithSaveStateMain {

	public static void main(String[] argv)
			throws Exception {
		DemoUtils.printConfiguration();

		String language = "en";
		File saveState = new File("/Users/francisbaril/Workspaces/saveStates/savestates-demo-newyork/systemstate-newyork-v2.zip");
		DemoUtils.startDemoWithSaveState(7070, saveState, language);

	}

}
