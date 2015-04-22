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

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/*
 * UTF-8 friendly ResourceBundle support
 *
 * Utility that allows having multi-byte characters inside java .property files.
 * It removes the need for Sun's native2ascii application, you can simply have
 * UTF-8 encoded editable .property files.
 *
 * Use:
 * ResourceBundle bundle = Utf8ResourceBundle.getBundle("bundle_name");
 *
 * @author Tomas Varaneckas <tomas.varaneckas@gmail.com>
 */
public abstract class Utf8ResourceBundle {

	/*
	 * Gets the unicode friendly resource bundle
	 *
	 * @param baseName
	 * @see java.util.ResourceBundle#getBundle(String)
	 * @return Unicode friendly resource bundle
	 */
	public static final ResourceBundle getBundle(final String baseName) {
		return createUtf8PropertyResourceBundle(
				ResourceBundle.getBundle(baseName));
	}

	public static final ResourceBundle getBundle(final String baseName, Locale locale, ClassLoader loader) {
		return createUtf8PropertyResourceBundle(
				ResourceBundle.getBundle(baseName, locale, loader));
	}

	/*
	 * Creates unicode friendly {@link java.util.PropertyResourceBundle} if possible.
	 *
	 * @param bundle
	 * @return Unicode friendly property resource bundle
	 */
	private static ResourceBundle createUtf8PropertyResourceBundle(
			final ResourceBundle bundle) {
		if (!(bundle instanceof PropertyResourceBundle)) {
			return bundle;
		}
		return new Utf8PropertyResourceBundle((PropertyResourceBundle) bundle);
	}

	/*
	 * Resource Bundle that does the hard work
	 */
	private static class Utf8PropertyResourceBundle extends ResourceBundle {

		/*
		 * Bundle with unicode data
		 */
		private final PropertyResourceBundle bundle;

		/*
		 * Initializing constructor
		 *
		 * @param bundle
		 */
		private Utf8PropertyResourceBundle(final PropertyResourceBundle bundle) {
			this.bundle = bundle;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Enumeration getKeys() {
			return bundle.getKeys();
		}

		@Override
		protected Object handleGetObject(final String key) {
			final String value = bundle.getString(key);
			if (value == null)
				return null;
			try {
				return new String(value.getBytes("ISO-8859-1"), "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new RuntimeException("Encoding not supported", e);
			}
		}
	}
}
