package com.constellio.app.ui.params;

import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.server.Page;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamUtils {

	public static final String PARAM_SEP = ";";
	public static final String NAME_VALUE_SEP = "=";

	public static String getParams() {
		String path = Page.getCurrent().getUriFragment();
		return getParams(path);
	}

	public static Map<String, String> getParamsMap() {
		String path = getParams();
		return getParamsMap(path);
	}

	public static String getParams(String path) {
		if (path == null) {
			return null;
		} else if (path.startsWith("!")) {
			path = path.substring(1);
		}

		path = urlDecode(path);
		String paramsStr = null;
		int indexOfEquals = path.indexOf("=");
		int indexOfSlash = path.lastIndexOf("/");
		if (indexOfSlash != -1 && (indexOfEquals == -1 || indexOfEquals > indexOfSlash)) {
			paramsStr = path.substring(indexOfSlash + 1);
		} else {
			paramsStr = path;
		}
		return paramsStr;
	}

	public static void setParams(String newParams) {
		String viewNameAndParams = UI.getCurrent().getNavigator().getState();
		String viewName = StringUtils.substringBefore(viewNameAndParams, "/");
		String viewNameAndNewParams = viewName + "/" + newParams;
		Page.getCurrent().setUriFragment("!" + viewNameAndNewParams, false);
	}

	public static void setParams(Map<String, ?> params) {
		String paramsAsString = addParams("", params);
		setParams(paramsAsString);
	}


	public static String addParams(String viewName, Map<String, ?> params) {
		String pathWithParams;
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<>();
		if (params != null) {
			keys.addAll(params.keySet());
		}
		Collections.sort(keys);
		if (params != null && !params.isEmpty()) {
			for (String paramName : keys) {
				if (sb.length() > 0) {
					sb.append(PARAM_SEP);
				}
				Object paramValue = params.get(paramName);
				sb.append(paramName);
				sb.append(NAME_VALUE_SEP);
				sb.append(paramValue);
			}
		}

		String encodedParams = urlEncode(sb.toString());
		if (StringUtils.isNotBlank(viewName)) {
			pathWithParams = viewName + "/" + encodedParams;
		} else {
			pathWithParams = encodedParams;
		}
		return pathWithParams;
	}

	public static Map<String, String> getParamsMap(String path) {
		Map<String, String> params = new HashMap<>();
		String paramsStr = getParams(path);
		if (paramsStr != null) {
			String[] paramsSeparated = paramsStr.split(PARAM_SEP);
			for (String paramNameAndValue : paramsSeparated) {
				int indexOfNameValueSep = paramNameAndValue.indexOf(NAME_VALUE_SEP);
				if (indexOfNameValueSep != -1) {
					String paramName = paramNameAndValue.substring(0, indexOfNameValueSep);
					String paramValue = paramNameAndValue.substring(indexOfNameValueSep + 1);
					params.put(paramName, paramValue);
				}
			}
		}
		return params;
	}

	private static String urlEncode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static String urlDecode(String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		} catch (IllegalArgumentException e) {
			return text;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public static Map<String, String> getCurrentParams() {
		Map<String, String> params = null;

		if (ConstellioUI.getCurrent() != null && ConstellioUI.getCurrent().getViewChangeEvent() != null) {
			params = ParamUtils.getParamsMap(ConstellioUI.getCurrent().getViewChangeEvent().getParameters());
		}
		return params;
	}
}
