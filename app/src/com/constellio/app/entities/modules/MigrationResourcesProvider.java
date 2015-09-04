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
package com.constellio.app.entities.modules;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.utils.i18n.Utf8ResourceBundles;

public class MigrationResourcesProvider {

	String module;
	String version;
	Utf8ResourceBundles bundles;
	IOServices ioServices;
	File moduleVersionFolder;
	Language language;

	public MigrationResourcesProvider(String module, Language language, String version, Locale defaultLocale,
			IOServices ioServices) {
		this.module = module;
		this.version = version;
		this.ioServices = ioServices;
		this.language = language;
		String versionWithUnderscores = version.replace(".", "_");
		File migrations = new File(new FoldersLocator().getI18nFolder(), "migrations");
		File moduleFolder = new File(migrations, module);
		moduleVersionFolder = new File(moduleFolder, versionWithUnderscores);
		String bundleName = module + "_" + versionWithUnderscores;
		File properties = new File(moduleVersionFolder, bundleName + ".properties");
		if (properties.exists()) {
			URL[] urls;
			try {
				urls = new URL[] { moduleVersionFolder.toURI().toURL() };
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			bundles = new Utf8ResourceBundles(bundleName, urls);
		}
	}

	public String get(String key) {
		return getDefaultLanguageString(key);
	}

	public String getDefaultLanguageString(String key) {
		ensureBundles();

		Locale locale = language.getLocale();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key) ? bundle.getString(key) : key;
	}

	private void ensureBundles() {
		if (bundles == null) {
			throw new RuntimeException("No such properties bundle for migration " + version + " of module " + module);
		}

	}

	public InputStream getStream(String key) {
		File file = new File(moduleVersionFolder, key);
		String streamName = "MigrationResourcesProvider-" + module + "-" + version + "-" + key;
		return ioServices.newBufferedFileInputStreamWithoutExpectableFileNotFoundException(file, streamName);
	}

	public String getString(String key, Locale locale) {
		ensureBundles();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key) ? bundle.getString(key) : key;
	}

	public boolean containsKey(String key) {
		ensureBundles();
		//TODO
		ResourceBundle bundle = bundles.getBundle(language.getLocale());

		return bundle.containsKey(key);
	}

	public boolean containsKey(String key, Locale locale) {
		ensureBundles();
		ResourceBundle bundle = bundles.getBundle(locale);

		return bundle.containsKey(key);
	}

}
