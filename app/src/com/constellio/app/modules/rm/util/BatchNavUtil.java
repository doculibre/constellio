package com.constellio.app.modules.rm.util;

import java.util.Map;

import com.jgoodies.common.base.Strings;

public class BatchNavUtil {
	public static boolean isBatchIdPresent(Map<String, String> params) {
		if (params == null) {
			return false;
		}
		return Strings.isNotBlank(params.get("batchId"));
	}

	public static String getId(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		return params.get("id");
	}

	public static String getBatchId(Map<String, String> params) {
		if (params == null) {
			return null;
		}

		return params.get("batchId");
	}
}
