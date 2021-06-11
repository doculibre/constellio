package com.constellio.app.modules.restapi.apis.v1.resource.service;

import com.constellio.app.modules.restapi.apis.v1.core.BaseRestfulService;
import com.constellio.app.modules.restapi.apis.v1.resource.dto.AceDto;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.Permissions;
import com.constellio.app.modules.restapi.core.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class ResourceRestfulService extends BaseRestfulService {

	private static Pattern FLUSH_WITHIN_PATTERN = Pattern.compile("^WITHIN_\\d+_SECONDS$");

	protected void validateRequiredParametersIncludingId(String id, String serviceKey, String method, String date,
														 Integer expiration, String signature) {
		validateRequiredParameter(id, "id");
		validateRequiredParameters(serviceKey, method, date, expiration, signature);
	}

	protected void validateRequiredParametersIncludingFolderId(String folderId, String serviceKey, String method,
															   String date, Integer expiration, String signature) {
		validateRequiredParameter(folderId, "folderId");
		validateRequiredParameters(serviceKey, method, date, expiration, signature);
	}

	protected void validateRequiredParameters(String serviceKey, String method, String date,
											  Integer expiration, String signature) {
		validateRequiredParameter(serviceKey, "serviceKey");
		validateRequiredParameter(method, "method");
		validateRequiredParameter(date, "date");
		validateRequiredParameter(expiration, "expiration");
		validateRequiredParameter(signature, "signature");
	}

	protected void validateHttpMethod(String method, String expectedMethod) {
		if (!method.equals(expectedMethod)) {
			throw new InvalidParameterException("method", method);
		}
	}

	protected void validateFlushValue(String value) {
		if (value.equals("NOW") || value.equals("LATER")) {
			return;
		}

		if (!FLUSH_WITHIN_PATTERN.matcher(value).matches()) {
			throw new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, value);
		}

		String seconds = value.split("_")[1];
		if (seconds.equals("0") || !StringUtils.isUnsignedInteger(seconds + "000")) {
			throw new InvalidParameterException(CustomHttpHeaders.FLUSH_MODE, value);
		}
	}

	protected void validateFilterValues(Class dtoObjectClass, Set<String> values) {
		for (String value : values) {
			try {
				dtoObjectClass.getDeclaredField(value);
			} catch (NoSuchFieldException e) {
				throw new InvalidParameterException("filter", value);
			}
		}
	}

	protected void validateAces(List<AceDto> aces) {
		for (int i = 0; i < ListUtils.nullToEmpty(aces).size(); i++) {
			AceDto ace = aces.get(i);
			for (String permission : ace.getPermissions()) {
				if (!Permissions.contains(permission)) {
					throw new InvalidParameterException(String.format("directAces[%d].permissions", i), permission);
				}
			}
		}
	}

	protected void validateETag(String eTag) {
		String unquotedEtag = unquoteETag(eTag);
		if (unquotedEtag != null && !StringUtils.isUnsignedLong(unquotedEtag)) {
			throw new InvalidParameterException("ETag", eTag);
		}
	}

	protected String unquoteETag(String eTag) {
		if (eTag == null) {
			return null;
		}

		String unquotedEtag = eTag;
		if (eTag.charAt(0) == '"' && eTag.charAt(eTag.length() - 1) == '"') {
			unquotedEtag = eTag.substring(1, eTag.length() - 1);
		}
		return unquotedEtag;
	}

}
