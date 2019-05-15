package com.constellio.app.modules.restapi.url;

import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.signature.SignatureService;
import com.constellio.app.modules.restapi.url.dao.UrlDao;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UrlServiceTest {

	@Mock private SignatureService signatureService;
	@Mock private ValidationService validationService;
	@Mock private UrlDao urlDao;
	@Mock private SystemConfigurationsManager systemConfigurationsManager;

	@InjectMocks private UrlService urlService;

	private String host = "localhost";
	private String token = "token";
	private String serviceKey = "serviceKey";
	private SchemaTypes schemaType = SchemaTypes.DOCUMENT;
	private String method = HttpMethod.GET;
	private String id = "id";
	private String folderId = "folderId";
	private String expiration = "3600";
	private String version;
	private String physical;
	private String copySourceId;

	private String expectedSignedUrl = "http://localhost/constellio/rest/v1/documents?" +
									   "id=id&serviceKey=serviceKey&method=GET&date=&expiration=3600&signature=123456";
	private String signedUrl;

	@Before
	public void setUp() throws Exception {
		initMocks(this);

		when(signatureService.sign(anyString(), anyString())).thenReturn("123456");

		when(urlDao.getServerPath()).thenReturn("http://localhost/constellio/");
	}

	@Test
	public void testGetSignedUrl() throws Exception {
		signedUrl = urlService.getSignedUrl(host, token, serviceKey, schemaType, method, id, folderId, expiration,
				version, physical, copySourceId);
		adjustExpectedSignedUrl();

		assertThat(signedUrl).isEqualTo(expectedSignedUrl);
	}

	@Test
	public void testGetSignedUrlWithSwappedParameters() throws Exception {
		signedUrl = urlService.getSignedUrl(host, token, method, schemaType, serviceKey, id, folderId, expiration,
				version, physical, copySourceId);
		adjustExpectedSignedUrl();

		assertThat(signedUrl).isNotEqualTo(expectedSignedUrl);
	}

	@Test
	public void testGetSignedUrlWithContentPath() throws Exception {
		version = "1.0";
		expectedSignedUrl = expectedSignedUrl
				.replace("/documents", "/documents/content")
				.replace("&expiration=3600", "&expiration=3600&version=1.0");

		signedUrl = urlService.getSignedUrl(host, token, serviceKey, schemaType, method, id, folderId, expiration,
				version, physical, copySourceId);
		adjustExpectedSignedUrl();

		assertThat(signedUrl).isEqualTo(expectedSignedUrl);
	}

	@Test
	public void testGetSignedUrlWithPhysical() throws Exception {
		physical = "false";
		expectedSignedUrl = expectedSignedUrl
				.replace("&expiration=3600", "&expiration=3600&physical=false");

		signedUrl = urlService.getSignedUrl(host, token, serviceKey, schemaType, method, id, folderId, expiration,
				version, physical, copySourceId);
		adjustExpectedSignedUrl();

		assertThat(signedUrl).isEqualTo(expectedSignedUrl);
	}

	private void adjustExpectedSignedUrl() {
		String now = DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime());
		expectedSignedUrl = expectedSignedUrl.replace("date=", "date=".concat(now));
	}

}
