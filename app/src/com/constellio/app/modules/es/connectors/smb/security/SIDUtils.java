/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.constellio.app.modules.es.connectors.smb.security;

/**
 * http://svn.apache.org/repos/asf/manifoldcf/trunk/connectors/activedirectory/connector/src/main/java/org/apache/manifoldcf/authorities/authorities/activedirectory/ActiveDirectoryAuthority.java
 */
public class SIDUtils {

	/** Convert a binary SID to a string */
	public static String sid2String(byte[] SID) {
		StringBuilder strSID = new StringBuilder("S");
		long version = SID[0];
		strSID.append("-").append(Long.toString(version));
		long authority = SID[4];
		for (int i = 0; i < 4; i++) {
			authority <<= 8;
			authority += SID[4 + i] & 0xFF;
		}
		strSID.append("-").append(Long.toString(authority));
		long count = SID[2];
		count <<= 8;
		count += SID[1] & 0xFF;
		for (int j = 0; j < count; j++) {
			long rid = SID[11 + (j * 4)] & 0xFF;
			for (int k = 1; k < 4; k++) {
				rid <<= 8;
				rid += SID[11 - k + (j * 4)] & 0xFF;
			}
			strSID.append("-").append(Long.toString(rid));
		}
		return strSID.toString();
	}
}
