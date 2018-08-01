package com.constellio.app.modules.restapi.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class UrlUtils {

	public static String trimPort(String host) {
		return host.split(":")[0];
	}

	public static String replaceHost(String url, String host) {
		int hostStartIdx = url.indexOf("/") + 2;

		int portStartIdx = url.indexOf(":", hostStartIdx);
		int hostEndIdx = url.indexOf("/", hostStartIdx);

		return url.substring(0, hostStartIdx).concat(host).concat(url.substring(hostEndIdx));
	}

}
