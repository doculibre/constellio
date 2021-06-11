package com.constellio.app.modules.restapi.apis.v2.core;

import com.constellio.app.modules.restapi.apis.v1.core.BaseRestfulService;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.util.StringUtils;

public class BaseRestfulServiceV2 extends BaseRestfulService {

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
