package com.constellio.app.modules.restapi.url;

import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.UrlUtils;
import com.constellio.app.modules.restapi.signature.SignatureService;
import com.constellio.app.modules.restapi.url.dao.UrlDao;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.data.utils.TimeProvider;
import com.google.common.base.Strings;

import javax.inject.Inject;

public class UrlService {

	@Inject
	private UrlDao urlDao;
	@Inject
	private SignatureService signatureService;
	@Inject
	private ValidationService validationService;

	public String getSignedUrl(String host, String token, String serviceKey, SchemaTypes schemaType, String method,
							   String id, String folderId, String expiration, String version, String physical,
							   String copySourceId) throws Exception {
		validationService.validateHost(host);
		validationService.validateAuthentication(token, serviceKey);

		String date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime());

		String data = host
				.concat(!Strings.isNullOrEmpty(id) ? id : !Strings.isNullOrEmpty(folderId) ? folderId : "")
				.concat(serviceKey)
				.concat(schemaType.name())
				.concat(method)
				.concat(date)
				.concat(expiration)
				.concat(!Strings.isNullOrEmpty(version) ? version : "")
				.concat(!Strings.isNullOrEmpty(physical) ? physical : "")
				.concat(!Strings.isNullOrEmpty(copySourceId) ? copySourceId : "");

		String signature = signatureService.sign(token, data);

		String url = getResourcePath(host, schemaType)
				.concat(!Strings.isNullOrEmpty(version) ? "/content" : "");
		if (id != null) {
			url = url.concat("?id=" + id);
		} else if (folderId != null) {
			url = url.concat("?folderId=" + folderId);
		} else {
			url = url.concat("?serviceKey=" + serviceKey);
		}
		url = url.concat(!url.contains("?serviceKey=") ? "&serviceKey=" .concat(serviceKey) : "")
				.concat("&method=").concat(method)
				.concat("&date=").concat(date)
				.concat("&expiration=").concat(expiration)
				.concat(!Strings.isNullOrEmpty(version) ? "&version=" + version : "")
				.concat(!Strings.isNullOrEmpty(physical) ? "&physical=" + physical : "")
				.concat("&signature=").concat(signature);
		return url;
	}

	private String getResourcePath(String host, SchemaTypes schemaType) {
		return getServerPath(host).concat("rest/v1/").concat(schemaType.getResource());
	}

	private String getServerPath(String host) {
		return UrlUtils.replaceHost(urlDao.getServerPath(), host);
	}

}
