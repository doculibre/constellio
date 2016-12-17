package com.constellio.app.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.vaadin.ui.UI;

public class ThemeUtils implements Serializable {

	private static Map<String, Boolean> cache = new HashMap<>();

	public static boolean resourceExists(String resourcePath) {
		Boolean resourceExists = cache.get(resourcePath);
		if (resourceExists == null) {
			UI ui = UI.getCurrent();
			InputStream resourceStream;
			try {
				resourceStream = ui.getSession().getService().getThemeResourceAsStream(ui, ui.getTheme(), resourcePath);
				if (resourceStream != null) {
					resourceExists = true;
					IOUtils.closeQuietly(resourceStream);
				} else {
					resourceExists = false;
				}
			} catch (IOException e) {
				resourceExists = false;
			}
			cache.put(resourcePath, resourceExists);
		}
		return resourceExists;
	}

}
