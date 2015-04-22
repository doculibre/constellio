/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
