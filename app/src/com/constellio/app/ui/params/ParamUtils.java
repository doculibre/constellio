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
package com.constellio.app.ui.params;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.Page;
import com.vaadin.ui.UI;

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
		if (path != null && path.startsWith("!")) {
			path = path.substring(1);
		}
		path = urlDecode(path);
		String paramsStr = null;
		int indexOfSlash = path.lastIndexOf("/");
		if (indexOfSlash != -1) {
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
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
