package com.constellio.app.modules.restapi.apis.v2.health;

import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthRestfulServiceV2AcceptanceTest extends ConstellioTest {

	@Test
	public void givenHeadHealthThenNoContentReturned() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v2/health").request().head();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
	}

	@Test
	public void givenGetHealthAndHealthySystemThenNoContentReturned() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule());

		Response response = newWebTarget("v2/health").request().get();
		assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());
	}

}
