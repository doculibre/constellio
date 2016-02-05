package com.constellio.app.ui.tools.vaadin;

import java.awt.*;
import java.awt.event.KeyEvent;

public class WebElementUtils {

	public static void pressEscapeAndRelease()
			throws AWTException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ESCAPE);
		robot.keyRelease(KeyEvent.VK_ESCAPE);
	}
}
