/**
 * Copyright © 2010 DocuLibre inc.
 * <p>
 * This file is part of Constellio.
 * <p>
 * Constellio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Constellio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Constellio.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.app.modules.es.connectors.http.utils;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Creates a (SHA) digest from a binary content.
 *
 * @author Nicolas Bélisle (nicolas.belisle@doculibre.com)
 */
public class DigestUtil {

	public static String digest(final byte[] content)
			throws NoSuchAlgorithmException {
		MessageDigest shaDigester = MessageDigest.getInstance("SHA");
		shaDigester.update(content);
		byte[] shaDigest = shaDigester.digest();
		String digestString = new String(Base64.encodeBase64(shaDigest));

		return digestString;
	}
}
