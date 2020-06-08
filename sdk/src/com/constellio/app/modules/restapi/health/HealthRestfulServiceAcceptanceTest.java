package com.constellio.app.modules.restapi.health;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthRestfulServiceAcceptanceTest extends ConstellioTest {

	private final static String ALLOWED_HEADERS =
			"X-Requested-With,Content-Type,Accept,Origin,Constellio-Flushing-Mode,Host,If-Match,ETag";
	private final static String EXPOSED_HEADERS = "ETag";

	@Test
	public void givenNotNullCorsAllowedOriginsConfigThenOptionsRequestIsDenied() {
		givenConfig(RestApiConfigs.CORS_ALLOWED_ORIGINS, "http://www.abc.com,https://www.abc.com");
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request()
				.header("Origin", "http://www.constellio.com")
				.header("Access-Control-Request-Method", "HEAD")
				.options();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Allow-Origin")).isNull();
		assertThat(response.getHeaderString("Access-Control-Allow-Methods")).isNull();
		assertThat(response.getHeaderString("Access-Control-Allow-Headers")).isNull();
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isNull();
	}

	@Test
	public void givenNullCorsAllowedOriginsConfigThenOptionsRequestIsAccepted() {
		givenConfig(RestApiConfigs.CORS_ALLOWED_ORIGINS, null);
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request()
				.header("Origin", "http://www.constellio.com")
				.header("Access-Control-Request-Method", "HEAD")
				.header("Access-Control-Request-Headers", "constellio-flushing-mode")
				.options();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Allow-Origin"))
				.isEqualTo("http://www.constellio.com");
		assertThat(response.getHeaderString("Access-Control-Allow-Methods"))
				.isEqualTo("GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS");
		assertThat(response.getHeaderString("Access-Control-Allow-Headers"))
				.isEqualTo(ALLOWED_HEADERS);
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isNull();
	}

	@Test
	public void givenWildcardCorsAllowedOriginsConfigThenOptionsRequestIsAccepted() {
		givenConfig(RestApiConfigs.CORS_ALLOWED_ORIGINS, "*");
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request()
				.header("Origin", "http://www.constellio.com")
				.header("Access-Control-Request-Method", "HEAD")
				.header("Access-Control-Request-Headers", "constellio-flushing-mode")
				.options();

		assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Allow-Origin"))
				.isEqualTo("http://www.constellio.com");
		assertThat(response.getHeaderString("Access-Control-Allow-Methods"))
				.isEqualTo("GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS");
		assertThat(response.getHeaderString("Access-Control-Allow-Headers"))
				.isEqualTo(ALLOWED_HEADERS);
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isNull();
	}

	@Test
	public void givenNullCorsAllowedOriginsConfigThenSimpleRequestIsAccepted() {
		givenConfig(RestApiConfigs.CORS_ALLOWED_ORIGINS, null);
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request()
				.header("Origin", "http://www.constellio.com")
				.head();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isEqualTo(EXPOSED_HEADERS);
	}

	@Test
	public void givenWildcardCorsAllowedOriginsConfigThenSimpleRequestIsAccepted() {
		givenConfig(RestApiConfigs.CORS_ALLOWED_ORIGINS, "*");
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request()
				.header("Origin", "http://www.constellio.com")
				.head();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isEqualTo(EXPOSED_HEADERS);
	}

	@Test
	public void testHeadHealth() {
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request().head();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isNull();
	}

	@Test
	public void testGetHealth() {
		prepareSystemWithoutHyperTurbo(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v1/health").request().get();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
		assertThat(response.getHeaderString("Access-Control-Expose-Headers")).isNull();
	}

}
