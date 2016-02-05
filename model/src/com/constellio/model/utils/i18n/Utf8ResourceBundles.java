package com.constellio.model.utils.i18n;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Utf8ResourceBundles {

	private final Map<Locale, ResourceBundle> RESOURCE_BUNDLES = new HashMap<Locale, ResourceBundle>();

	private String bundleName;

	private URL[] urls;

	public Utf8ResourceBundles(String bundleName, URL[] urls) {
		this.bundleName = bundleName;
		this.urls = urls;
	}

	public ResourceBundle getBundle(Locale locale) {
		ResourceBundle bundle = RESOURCE_BUNDLES.get(locale);
		if (bundle == null) {
			ClassLoader loader = new URLClassLoader(urls);
			bundle = Utf8ResourceBundle.getBundle(bundleName, locale, loader);
			RESOURCE_BUNDLES.put(locale, bundle);
		}

		return bundle;
	}

}
