package com.constellio.app.modules.restapi.cache;

import com.constellio.app.modules.restapi.core.util.CustomHttpHeaders;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.document.BaseDocumentRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static java.util.Arrays.asList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class RestApiWithDistributedCacheAcceptanceTest extends BaseDocumentRestfulServiceAcceptanceTest {

	private ModelLayerFactory modelLayerFactory;
	private ModelLayerFactory modelLayerFactory2;
	private RecordServices recordServices2;
	private UserServices userServices2;

	private String schemaType = SchemaTypes.DOCUMENT.name();
	private String token2 = "bobToken2";

	@Before
	public void setUp() throws Exception {
		super.setUp();

		modelLayerFactory = getModelLayerFactory();
		modelLayerFactory2 = getModelLayerFactory("other-instance");


		//linkEventBus(modelLayerFactory, modelLayerFactory2, 1);

		recordServices2 = modelLayerFactory2.newRecordServices();
		userServices2 = modelLayerFactory2.newUserServices();
	}

	@Test
	public void whenCacheIsNotInvalidatedThenRetrieveCorrectRecordFromSolr() throws Exception {
		waitForBatchProcess();

		// populate caches
		Record recordAtStart = recordServices.realtimeGetRecordById(fakeDocument.getId());
		Record recordAtStart2 = recordServices2.realtimeGetRecordById(fakeDocument.getId());
		assertThat(recordAtStart.getVersion()).isEqualTo(recordAtStart2.getVersion());

		DocumentDto documentToPatch = DocumentDto.builder().id(fakeDocument.getId()).title("patched title").build();
		Response response = doPatchQuery("LATER", documentToPatch);

		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		String eTag = response.getHeaderString("ETag").replace("\"", "");

		Record record = recordServices.realtimeGetRecordById(fakeDocument.getId(), Long.valueOf(eTag));
		assertThat(record.getVersion()).isEqualTo(Long.valueOf(eTag));

		record = recordServices2.realtimeGetRecordById(id);
		assertThat(record.getVersion()).isEqualTo(recordAtStart2.getVersion()).isNotEqualTo(Long.valueOf(eTag));

		record = recordServices2.realtimeGetRecordById(id, Long.valueOf(eTag));
		assertThat(record.getVersion()).isEqualTo(Long.valueOf(eTag));
	}

	@Test
	public void whenGetMatchIfAndCacheIsNotInvalidatedThenRetrieveCorrectRecord() throws Exception {
		waitForBatchProcess();

		// populate caches
		Record recordAtStart = recordServices.realtimeGetRecordById(fakeDocument.getId());
		Record recordAtStart2 = recordServices2.realtimeGetRecordById(fakeDocument.getId());
		assertThat(recordAtStart.getVersion()).isEqualTo(recordAtStart2.getVersion());

		recordServices2.update(recordServices2.getDocumentById(fakeDocument.getId()).set(Schemas.TITLE, "title2"));
		waitForBatchProcess();

		Record record = recordServices2.getDocumentById(fakeDocument.getId());
		String eTag = String.valueOf(record.getVersion());

		record = recordServices.getDocumentById(fakeDocument.getId());
		assertThat(record.getVersion()).isNotEqualTo(Long.valueOf(eTag));

		Response response = doGetQuery(record.getId(), "\"".concat(eTag).concat("\""));
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

		String getQueryEtag = response.getHeaderString("ETag").replace("\"", "");
		assertThat(getQueryEtag).isEqualTo(eTag);
	}

	@Test
	public void whenGenerateTokenAndCacheNotInvalidatedThenTokenRetrievable() throws Exception {
		waitForBatchProcess();

		// populate caches
		userServices.getUserInfos(users.bob().getUsername());
		userServices2.getUserInfos(users.bob().getUsername());

		userServices2.execute(
				users.bobAddUpdateRequest().setServiceKey(serviceKey)
						.addAccessToken(token2, TimeProvider.getLocalDateTime().plusYears(1)));

		SystemWideUserInfos user = userServices.getUserInfos(users.bob().getUsername());
		assertThat(user.getAccessTokens().size()).isEqualTo(2);
		SystemWideUserInfos user2 = userServices2.getUserInfos(users.bob().getUsername());
		assertThat(user2.getAccessTokens().size()).isEqualTo(3);

		Response response = doGetUrlQuery();
		assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
	}

	private Response doPatchQuery(String flushMode, DocumentDto document) throws Exception {
		id = document.getId();
		method = HttpMethods.PATCH;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).header(CustomHttpHeaders.FLUSH_MODE, flushMode)
				.build("PATCH", entity(buildMultiPart(document), MULTIPART_FORM_DATA_TYPE)).invoke();
	}

	private Response doGetQuery(String documentId, String eTag) throws Exception {
		id = documentId;
		method = HttpMethods.GET;
		return buildQuery(webTarget, true, asList("id", "serviceKey", "method", "date", "expiration", "signature"))
				.request().header("host", host).header("If-Match", eTag).get();
	}

	private Response doGetUrlQuery() throws Exception {
		webTarget = newWebTarget("v1/urls");

		method = HttpMethods.GET;
		token = token2;
		version = "last";
		folderId = null;

		return buildQuery(webTarget, false, asList("token", "serviceKey", "schemaType", "method", "id", "folderId", "expiration",
				"version", "physical")).request().header("host", host).get();
	}
}
