package com.constellio.app.utils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class NamingEnumerationUtils {
	public static void closeQuietly(NamingEnumeration namingEnumeration) {
		if (namingEnumeration != null) {
			try {
				namingEnumeration.close();
			} catch (NamingException e) {

			}
		}
	}
}
