package demo;

import demo.scripts.Demo_feb_2015;

public class StartDemoMain {

	public static void main(String[] argv)
			throws Exception {
		DemoUtils.printConfiguration();

		String language = "fr";
		DemoUtils.startDemoOn(8080, new Demo_feb_2015(), language);

	}

}
