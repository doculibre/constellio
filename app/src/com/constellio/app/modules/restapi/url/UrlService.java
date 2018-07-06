package com.constellio.app.modules.restapi.url;

import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
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

    public String getSignedUrl(String token, String serviceKey, SchemaTypes schemaType, String method, String id,
                               String folderId, String expiration, String version, String physical) throws Exception {
        validationService.validateAuthentication(token, serviceKey);

        String date = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime());

        String data = getHost()
                .concat(!Strings.isNullOrEmpty(id) ? id : folderId)
                .concat(serviceKey)
                .concat(schemaType.name())
                .concat(method)
                .concat(date)
                .concat(expiration)
                .concat(!Strings.isNullOrEmpty(version) ? version : "")
                .concat(!Strings.isNullOrEmpty(physical) ? physical : "");

        String signature = signatureService.sign(token, data);

        return getResourcePath(schemaType)
                .concat(!Strings.isNullOrEmpty(version) ? "/content" : "")
                .concat("?")
                .concat(!Strings.isNullOrEmpty(id) ? "id=" + id : "folderId=" + folderId)
                .concat("&serviceKey=").concat(serviceKey)
                .concat("&method=").concat(method)
                .concat("&date=").concat(date)
                .concat("&expiration=").concat(expiration)
                .concat(!Strings.isNullOrEmpty(version) ? "&version=" + version : "")
                .concat(!Strings.isNullOrEmpty(physical) ? "&physical=" + physical : "")
                .concat("&signature=").concat(signature);
    }

    private String getHost() {
        return urlDao.getServerHost();
    }

    private String getPath() {
        return urlDao.getServerPath();
    }

    private String getResourcePath(SchemaTypes schemaType) {
        return getPath().concat("rest/v1/").concat(schemaType.getResource());
    }

}
