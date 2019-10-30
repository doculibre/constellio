package com.constellio.app.ui.util;

import com.vaadin.ui.JavaScript;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class JavascriptUtils {

	@SuppressWarnings("deprecation")
	public static void loadScript(String themeResourcePath) {
		try (InputStream in = ThemeUtils.getResource(themeResourcePath)) {
			String resourceContent = IOUtils.toString(in);
			JavaScript.eval(resourceContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
