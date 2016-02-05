/**
 * Copyright © 2010 DocuLibre inc.
 *
 * This file is part of Constellio.
 *
 * Constellio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Constellio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Constellio.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.app.modules.es.connectors.http.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.protocol.javascript.JavaScriptURLConnection;

/**
 * A few utils for parsing HtmlAnchors.
 *
 * @author Nicolas Bélisle (nicolas.belisle@doculibre.com)
 */
public class HtmlAnchorUtils {

	public static boolean isJavascript(HtmlAnchor htmlAnchor) {
		String href = htmlAnchor.getHrefAttribute();
		if (href.length() > 0 && !href.startsWith("#")) {
			if (StringUtils.startsWithIgnoreCase(href,
					JavaScriptURLConnection.JAVASCRIPT_PREFIX)) {
				// Javascript anchor
				return true;
			}
		}
		return false;
	}

	public static boolean isMailto(HtmlAnchor htmlAnchor) {
		String href = htmlAnchor.getHrefAttribute();
		if (href.length() > 0 && href.startsWith("mailto:")) {
			return true;
		}
		return false;
	}

	public static String getUrl(HtmlAnchor htmlAnchor)
			throws MalformedURLException {
		String href = htmlAnchor.getHrefAttribute();
		if (href.length() > 0 && !href.startsWith("#")) {
			HtmlPage page = (HtmlPage) htmlAnchor.getPage();
			URL url = page.getFullyQualifiedUrl(href);
			return url.toExternalForm();
		}
		return null;
	}
}
