package com.constellio.app.utils;

import java.util.List;

import static java.util.Arrays.asList;

public class PathUtils {

	public static String convertToHttpPath(final String uncPath) {
		boolean secure = uncPath.contains("@SSL") || uncPath.contains("@SSL");
		String path = uncPath.replace("@SSL", "").replace("@SSL", "").substring(2);

		List<String> parts = asList(path.split("\\\\"));
		List<String> dnsParts = asList(parts.get(0).split("@"));

		String httpPath = secure ? "https://" : "http://";
		httpPath = httpPath.concat(parts.get(0));
		if (dnsParts.size() > 1) {
			httpPath = httpPath.concat(":").concat(dnsParts.get(1));
		}

		for (int i = 1; i < parts.size(); i++) {
			httpPath = httpPath.concat("/").concat(parts.get(i));
		}

		return httpPath;
	}

	public static String convertToUncPath(final String httpPath) {
		String path;
		boolean secure = false;
		if (httpPath.toLowerCase().contains("https://")) {
			path = httpPath.replace("https://", "").replace("HTTPS://", "");
			secure = true;
		} else {
			path = httpPath.replace("http://", "").replace("HTTP://", "");
		}

		List<String> parts = asList(path.split("/"));
		List<String> dnsParts = asList(parts.get(0).split(":"));

		String uncPath = "\\\\".concat(dnsParts.get(0));
		if (dnsParts.size() > 1) {
			uncPath = uncPath.concat("@").concat(dnsParts.get(1));
		}
		if (secure) {
			uncPath = uncPath.concat("@SSL");
		}

		for (int i = 1; i < parts.size(); i++) {
			uncPath = uncPath.concat("\\").concat(parts.get(i));
		}

		return uncPath;
	}

}
