package com.constellio.app.modules.restapi.tenant;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.data.utils.TenantUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.stream.Stream;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

public class RestApiWithMultiTenancyAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	protected Users users = new Users();
	private RecordServices recordServices1, recordServices2;

	private WebTarget webTarget;
	private String token1 = "bobToken1", token2 = "bobToken2";
	private String serviceKey1 = "bobKey1", serviceKey2 = "bobKey2";

	ConstellioWebDriver driver;

	@Before
	public void setUp() {
		givenTwoTenants();

		Stream.of("1", "2").forEach(tenantId -> {
			TenantUtils.setTenant(tenantId);

			prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule()
					.withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus());

			givenTimeIs(new LocalDateTime());
			String host = tenantId.equals("1") ? "localhost:7070" : "127.0.0.1:7070";
			givenConfig(RestApiConfigs.REST_API_URLS, host);

			if (tenantId.equals("1")) {
				recordServices1 = getModelLayerFactory().newRecordServices();
			} else {
				recordServices2 = getModelLayerFactory().newRecordServices();
			}

			String token = tenantId.equals("1") ? token1 : token2;
			String serviceKey = tenantId.equals("1") ? serviceKey1 : serviceKey2;
			getModelLayerFactory().newUserServices().addUpdateUserCredential(users.bobAddUpdateRequest().setServiceKey(serviceKey)
					.addAccessToken(token, TimeProvider.getLocalDateTime().plusYears(1)));

			TenantUtils.setTenant(null);
		});

		webTarget = newWebTarget("/v1/folders");
	}

	@Test
	public void givenTwoTenantsThenRestApiRequestFromTenant1HostnameIsProcessedByTenant1() throws Exception {
		FolderDto folderUpdate = FolderDto.builder()
				.id(records.folder_A01)
				.title("NewTitleTenant1")
				.build();
		Response response = doPatchQuery(folderUpdate, "localhost:7070", serviceKey1, token1, webTarget);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		Record record = recordServices1.getDocumentById(folderUpdate.getId());
		assertThat(record.getTitle()).isEqualTo("NewTitleTenant1");

		record = recordServices2.getDocumentById(folderUpdate.getId());
		assertThat(record.getTitle()).isNotEqualTo("NewTitleTenant1");
	}

	@Test
	public void givenTwoTenantsThenRestApiRequestFromTenant2HostnameIsProcessedByTenant2() throws Exception {
		FolderDto folderUpdate = FolderDto.builder()
				.id(records.folder_A01)
				.title("NewTitleTenant2")
				.build();
		Response response = doPatchQuery(folderUpdate, "127.0.0.1:7070", serviceKey2, token2, webTarget);
		assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

		Record record = recordServices1.getDocumentById(folderUpdate.getId());
		assertThat(record.getTitle()).isNotEqualTo("NewTitleTenant2");

		record = recordServices2.getDocumentById(folderUpdate.getId());
		assertThat(record.getTitle()).isEqualTo("NewTitleTenant2");
	}

	@Test
	public void givenTwoTenantsAndTwoRequestsThenEachProcessedByCorrectTenant() throws Exception {
		givenTwoTenantsThenRestApiRequestFromTenant1HostnameIsProcessedByTenant1();
		givenTwoTenantsThenRestApiRequestFromTenant2HostnameIsProcessedByTenant2();
	}

	private Response doPatchQuery(FolderDto folder, String host, String serviceKey, String token, WebTarget webTarget)
			throws Exception {
		WebTarget webTargetParams = webTarget
				.queryParam("id", records.folder_A01)
				.queryParam("serviceKey", serviceKey)
				.queryParam("method", HttpMethods.PATCH)
				.queryParam("date", DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime()))
				.queryParam("expiration", "3600")
				.queryParam("signature", calculateSignature(token, serviceKey, host));
		Invocation.Builder webTargetBuilder = webTargetParams.request().header("host", host);
		return webTargetBuilder.build(HttpMethods.PATCH, entity(folder, APPLICATION_JSON)).invoke();
	}

	private String calculateSignature(String token, String serviceKey, String host) throws Exception {
		String data = host
				.concat(records.folder_A01)
				.concat(serviceKey)
				.concat(SchemaTypes.FOLDER.name())
				.concat(HttpMethods.PATCH)
				.concat(DateUtils.formatIsoNoMillis(TimeProvider.getLocalDateTime()))
				.concat("3600");
		return HashingUtils.hmacSha256Base64UrlEncoded(token, data);
	}

}
