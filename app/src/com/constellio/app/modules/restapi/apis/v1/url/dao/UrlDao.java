package com.constellio.app.modules.restapi.apis.v1.url.dao;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.UrlUtils;
import com.constellio.data.utils.TimeProvider;
import com.google.common.base.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlDao extends BaseDao {

	public static UrlDao newUrlDao() {
		UrlDao instance = new UrlDao();
		instance.init();
		return instance;
	}

	public String getSignedUrl(String host, String token, String serviceKey, SchemaTypes schemaType, String method,
							   String id, String folderId, String expiration, String version, String physical,
							   String copySourceId) throws Exception {
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

		String signature = sign(token, data);

		String url = getResourcePath(host, schemaType)
				.concat(!Strings.isNullOrEmpty(version) ? "/content" : "");
		if (id != null) {
			url = url.concat("?id=" + id);
		} else if (folderId != null) {
			url = url.concat("?folderId=" + folderId);
		} else {
			url = url.concat("?serviceKey=" + encode(serviceKey));
		}
		url = url.concat(!url.contains("?serviceKey=") ? "&serviceKey=".concat(encode(serviceKey)) : "")
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
		return UrlUtils.replaceHost(getServerPath(), host);
	}

	private String encode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
	}
}
