package com.constellio.app.ui.util;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.vaadin.ui.UI;

public class ThemeUtils implements Serializable {

	private static Map<String, Boolean> cache = new HashMap<>();

	public static boolean resourceExists(String resourcePath) {
		if (!cache.containsKey(resourcePath)) {
			boolean resourceExists;
			UI ui = UI.getCurrent();
			InputStream resourceStream = ui.getSession().getService().getThemeResourceAsStream(ui, ui.getTheme(), resourcePath);
			if (resourceStream != null) {
				resourceExists = true;
				IOUtils.closeQuietly(resourceStream);
			} else {
				resourceExists = false;
			}
			cache.put(resourcePath, resourceExists);
		}

		return cache.containsKey(resourcePath);
	}

}
