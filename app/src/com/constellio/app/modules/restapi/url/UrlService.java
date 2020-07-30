package com.constellio.app.modules.restapi.url;

import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.url.dao.UrlDao;
import com.constellio.app.modules.restapi.validation.ValidationService;

import javax.inject.Inject;

public class UrlService {

	@Inject
	private UrlDao urlDao;
	@Inject
	private ValidationService validationService;

	public String getSignedUrl(String host, String token, String serviceKey, SchemaTypes schemaType, String method,
							   String id, String folderId, String expiration, String version, String physical,
							   String copySourceId) throws Exception {
		validationService.validateHost(host);
		validationService.validateAuthentication(token, serviceKey);

		return urlDao.getSignedUrl(host, token, serviceKey, schemaType, method, id, folderId, expiration, version, physical, copySourceId);
	}
}
