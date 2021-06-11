package com.constellio.app.modules.restapi.apis.v1.url;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnallowedHostException;
import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterWithHttpMethodException;
import com.constellio.app.modules.restapi.core.exception.mapper.RestApiErrorResponse;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.app.modules.restapi.core.util.UrlUtils;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlRestfulServiceAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private String host = "localhost:7070";
	private String serviceKey = "bobKey";
	private String token = "bobToken";
	private String folderId = "A20";
	private String id = "docA19";
	private String schemaType = SchemaTypes.DOCUMENT.name();
	private String method = HttpMethods.POST;
	private LocalDateTime fakeDate = new LocalDateTime(2018, 3, 30, 0, 0, 0);
	private String date = DateUtils.formatIsoNoMillis(fakeDate);
	private String expiration = "2147483647";
	private String version;
	private String physical;

	private WebTarget webTarget;

	private QueryCounter queryCounter;
	private CommitCounter commitCounter;

	private static final String NOT_NULL_MESSAGE = "javax.validation.constraints.NotNull.message";

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		UserServices userServices = getModelLayerFactory().newUserServices();

		userServices.execute(
				users.bobAddUpdateRequest().setServiceKey(serviceKey)
						.addAccessToken(token, TimeProvider.getLocalDateTime().plusYears(1)));

		givenTimeIs(fakeDate);
		givenConfig(RestApiConfigs.REST_API_URLS, "localhost:7070; localhost2");

		webTarget = newWebTarget("v1/urls");

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory());
	}

	@Test
	public void testGetUrlDocumentGetMethod() throws Exception {
		method = HttpMethods.GET;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource())
				.concat(String.format("?folderId=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						folderId, serviceKey, method, date, expiration, calculateSignature(folderId))));

		Response response = buildRequest("id").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		assertThat(commitCounter.newCommitsCall()).isEmpty();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlDocumentGetMethodAndVersion() throws Exception {
		method = HttpMethods.GET;
		version = "1.0";
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource() + "/content")
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&version=%s&signature=%s",
						id, serviceKey, method, date, expiration, version, calculateSignature(id))));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlDocumentPutMethod() throws Exception {
		method = HttpMethods.PUT;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource()))
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						id, serviceKey, method, date, expiration, calculateSignature(id)));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlDocumentPatchMethod() throws Exception {
		method = HttpMethods.PATCH;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource()))
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						id, serviceKey, method, date, expiration, calculateSignature(id)));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlDocumentDeleteMethod() throws Exception {
		method = HttpMethods.DELETE;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource())
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						id, serviceKey, method, date, expiration, calculateSignature(id))));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlDocumentDeleteMethodAndPhysical() throws Exception {
		method = HttpMethods.DELETE;
		physical = "false";
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource())
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&physical=%s&signature=%s",
						id, serviceKey, method, date, expiration, physical, calculateSignature(id))));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetFolder() throws Exception {
		schemaType = SchemaTypes.FOLDER.name();
		method = HttpMethods.GET;
		id = "A01";
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.FOLDER.getResource())
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						id, serviceKey, method, date, expiration, calculateSignature(id))));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testPatchFolder() throws Exception {
		schemaType = SchemaTypes.FOLDER.name();
		method = HttpMethods.PATCH;
		id = "A01";
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.FOLDER.getResource())
				.concat(String.format("?id=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						id, serviceKey, method, date, expiration, calculateSignature(id))));

		Response response = buildRequest("folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testPostFolder() throws Exception {
		schemaType = SchemaTypes.FOLDER.name();
		method = HttpMethods.POST;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.FOLDER.getResource())
				.concat(String.format("?folderId=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						folderId, serviceKey, method, date, expiration, calculateSignature(folderId))));

		Response response = buildRequest("id").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testPostFolderWithoutFolderId() throws Exception {
		schemaType = SchemaTypes.FOLDER.name();
		method = HttpMethods.POST;
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.FOLDER.getResource())
				.concat(String.format("?serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						serviceKey, method, date, expiration, calculateSignature(null))));

		Response response = buildRequest("id", "folderId").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(trimPort(url)).isEqualTo(expectedUrl);
	}

	@Test
	public void testGetUrlWithInvalidMethodAndVersionParameters() throws Exception {
		version = "1.0";
		Response response = buildRequest("id").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new InvalidParameterWithHttpMethodException("version", method).getValidationError()));
	}

	@Test
	public void testGetUrlWithMissingTokenParameter() throws Exception {
		Response response = buildRequest("id", "token").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "token"));
	}

	@Test
	public void testGetUrlWithMissingServiceKeyParameter() throws Exception {
		Response response = buildRequest("id", "serviceKey").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "serviceKey"));
	}

	@Test
	public void testGetUrlWithMissingSchemaTypeParameter() throws Exception {
		Response response = buildRequest("id", "schemaType").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "schemaType"));
	}

	@Test
	public void testGetUrlWithInvalidSchemaTypeParameter() throws Exception {
		schemaType = "SCHEMA_TYPE";
		Response response = buildRequest("id").get();
		assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}

	@Test
	public void testGetUrlWithMissingMethodParameter() throws Exception {
		Response response = buildRequest("id", "method").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "method"));
	}

	@Test
	public void testGetUrlWithInvalidMethodParameter() throws Exception {
		method = "METHOD";
		Response response = buildRequest("id").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new InvalidParameterException("method", method).getValidationError()));
	}

	@Test
	public void testGetUrlWithMissingIdAndFolderIdParameters() throws Exception {
		Response response = buildRequest("id", "folderId").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new AtLeastOneParameterRequiredException("id", "folderId").getValidationError()));
	}

	@Test
	public void testGetUrlWithBothIdAndFolderIdParameters() throws Exception {
		Response response = buildRequest().get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new InvalidParameterCombinationException("id", "folderId").getValidationError()));
	}

	@Test
	public void testGetUrlWithIdAndWithoutFolderIdParametersForPostMethod() throws Exception {
		Response response = buildRequest("folderId").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "folderId"));
	}

	@Test
	public void testGetUrlWithMissingExpirationParameter() throws Exception {
		Response response = buildRequest("id", "expiration").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, "expiration"));
	}

	@Test
	public void testGetUrlWithMissingExpirationParameterAndLanguageHeader() throws Exception {
		Response response = buildRequest("id", "expiration")
				.header(HttpHeaders.ACCEPT_LANGUAGE, "en-gb").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, Locale.ENGLISH, "expiration"));
	}

	@Test
	public void testGetUrlWithMissingExpirationParameterAndMultipleLanguagesHeader() throws Exception {
		Response response = buildRequest("id", "expiration")
				.header(HttpHeaders.ACCEPT_LANGUAGE, "en-us, en;q=1.0,fr-ca, fr;q=0.5,es;q=0.5").get();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(NOT_NULL_MESSAGE, Locale.ENGLISH, "expiration"));
	}

	@Test
	public void testGetUrlWithUnallowedHostHeader() throws Exception {
		host = "www.fakedns.com";
		Response response = buildRequest("id").get();
		assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

		RestApiErrorResponse error = response.readEntity(RestApiErrorResponse.class);
		assertThat(error.getMessage()).doesNotContain("{").doesNotContain("}")
				.isEqualTo(i18n.$(new UnallowedHostException(host).getValidationError()));
	}

	@Test
	public void testGetUrlWithAllowedHostHeader() throws Exception {
		host = "localhost2";
		String expectedUrl = trimPort(webTarget.getUri().toString().replace("/urls", "/" + SchemaTypes.DOCUMENT.getResource())
				.concat(String.format("?folderId=%s&serviceKey=%s&method=%s&date=%s&expiration=%s&signature=%s",
						folderId, serviceKey, method, date, expiration, calculateSignature(folderId))));
		expectedUrl = UrlUtils.replaceHost(expectedUrl, host);

		Response response = buildRequest("id").get();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_PLAIN_TYPE);

		String url = response.readEntity(String.class);

		assertThat(url).isNotEmpty();
		assertThat(url).isEqualTo(expectedUrl);
	}

	private Invocation.Builder buildRequest(String... excludedParams) throws Exception {
		List<String> parameters = asList("token", "serviceKey", "schemaType", "method", "id", "folderId", "expiration",
				"version", "physical");
		List<String> excludedParameters = asList(excludedParams);

		for (String parameter : parameters) {
			if (excludedParameters.contains(parameter)) {
				continue;
			}

			Object value = getClass().getDeclaredField(parameter).get(this);
			if (value == null) {
				continue;
			}
			webTarget = webTarget.queryParam(parameter, value);
		}
		return webTarget.request().header(HttpHeaders.HOST, host);
	}

	private String trimPort(String url) {
		int hostStartIdx = url.indexOf("/") + 2;

		int portStartIdx = url.indexOf(":", hostStartIdx);
		int portEndIdx = url.indexOf("/", hostStartIdx);

		return new StringBuilder(url).replace(portStartIdx, portEndIdx, "").toString();
	}

	private String calculateSignature(String id) throws Exception {
		String data = StringUtils.concat(host, id, serviceKey, schemaType, method, date, expiration, version, physical);
		return HashingUtils.hmacSha256Base64UrlEncoded(token, data);
	}

}
