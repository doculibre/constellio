package com.constellio.app.modules.es.connectors.http.robotstxt;

public class URLDecoder {
	/**
	 * Decodes URL octets except %2f (i.e. / character)
	 *
	 * @param str string to encode
	 * @return encoded string
	 */
	public static String decode(String str) {
		if (str != null) {
			try {
				StringBuilder sb = new StringBuilder();
				for (int idx = str.toLowerCase().indexOf("%2f"); idx >= 0; idx = str.toLowerCase().indexOf("%2f")) {
					sb.append(java.net.URLDecoder.decode(str.substring(0, idx), "UTF-8")).append(str.substring(idx, idx + 3));
					str = str.substring(idx + 3);
				}
				sb.append(java.net.URLDecoder.decode(str, "UTF-8"));
				str = sb.toString();
			} catch (Exception ex) {
			}
		}
		return str;
	}
}
