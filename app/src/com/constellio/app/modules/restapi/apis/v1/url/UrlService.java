package com.constellio.app.modules.restapi.apis.v1.url;

import com.constellio.app.modules.restapi.apis.v1.url.dao.UrlDao;
import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;

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
