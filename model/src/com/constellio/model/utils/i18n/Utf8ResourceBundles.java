package com.constellio.model.utils.i18n;

import java.io.File;
import java.net.MalformedURLException;
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

	public static Utf8ResourceBundles forPropertiesFile(File propertiesFolder, String bundleName) {
		URL[] urls;
		try {
			urls = new URL[] { propertiesFolder.toURI().toURL() };
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return new Utf8ResourceBundles(bundleName, urls);
	}
}
