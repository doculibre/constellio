package com.constellio.app.utils;

import nl.bitwalker.useragentutils.OperatingSystem;
import nl.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;

/**
 * http://stackoverflow.com/questions/1326928/how-can-i-get-client-infomation-such-as-os-and-browser
 *
 * @author Vincent
 */
public class HttpRequestUtils {

	public static enum OPERATING_SYSTEM {WINDOWS, MAC_OS_X, LINUX, ANDROID, IOS, UNKNOWN}

	public static enum BROWSER {IE, CHROME, FIREFOX, SAFARI, OPERA, UNKNOWN}

	public static OPERATING_SYSTEM getOperatingSystem(HttpServletRequest request) {
		OPERATING_SYSTEM os;

		UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
		OperatingSystem userAgentOS = userAgent.getOperatingSystem();

		switch (userAgentOS.getGroup()) {
			case WINDOWS:
				os = OPERATING_SYSTEM.WINDOWS;
				break;
			case MAC_OS_X:
				os = OPERATING_SYSTEM.MAC_OS_X;
				break;
			case LINUX:
				os = OPERATING_SYSTEM.LINUX;
				break;
			case ANDROID:
				os = OPERATING_SYSTEM.ANDROID;
				break;
			case IOS:
				os = OPERATING_SYSTEM.IOS;
				break;
			default:
				os = OPERATING_SYSTEM.UNKNOWN;
				break;
		}
		return os;
	}

	public static boolean isWindows(HttpServletRequest request) {
		return getOperatingSystem(request).equals(OPERATING_SYSTEM.WINDOWS);
	}

	public static boolean isMacOsX(HttpServletRequest request) {
		return getOperatingSystem(request).equals(OPERATING_SYSTEM.MAC_OS_X);
	}

	public static boolean isLocalhost(HttpServletRequest request) {
		String address = request.getRemoteAddr();
		return "localhost".equals(address) || "127.0.0.1".equals(address);
	}

	public static String getBaseURL(HttpServletRequest request, boolean includeContextPath) {
		StringBuffer baseURL = new StringBuffer();
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();

		baseURL.append(scheme);
		baseURL.append("://");
		baseURL.append(serverName);
		if (serverPort != 80 && serverPort != 443) {
			baseURL.append(":" + serverPort);
		}
		if (includeContextPath && !"/".equals(contextPath)) {
			baseURL.append(contextPath);
		}
		return baseURL.toString();
	}

	public static String getBaseURI(HttpServletRequest request) {
		StringBuffer baseURI = new StringBuffer();
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String requestURI = request.getRequestURI();

		baseURI.append(scheme);
		baseURI.append("://");
		baseURI.append(serverName);
		if (serverPort != 80 && serverPort != 443) {
			baseURI.append(":" + serverPort);
		}
		if (!"/".equals(requestURI)) {
			baseURI.append(requestURI);
		}
		return baseURI.toString();
	}

}
