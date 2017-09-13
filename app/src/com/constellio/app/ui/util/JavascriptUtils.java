package com.constellio.app.ui.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.vaadin.ui.JavaScript;

public class JavascriptUtils {
	
	public static void loadScript(String themeResourcePath) {
		try (InputStream in = ThemeUtils.getResource(themeResourcePath)) {
			String resourceContent = IOUtils.toString(in);
			JavaScript.eval(resourceContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
