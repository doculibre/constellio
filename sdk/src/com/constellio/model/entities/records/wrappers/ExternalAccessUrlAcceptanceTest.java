package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus.CLOSED;
import static com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus.EXPIRED;
import static com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus.OPEN;
import static com.constellio.model.entities.schemas.RecordCacheType.FULLY_CACHED;
import static org.assertj.core.api.Assertions.assertThat;

public class ExternalAccessUrlAcceptanceTest extends ConstellioTest {

	SchemasRecordsServices recordsServices;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());
		recordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCreatingAccessThenCreated()
			throws Exception {

		ExternalAccessUrl access = recordsServices.newExternalAccessUrlWithId("zeAccessId")
				.setExpirationDate(LocalDate.now())
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("zeToken")
				.setFullname("Mister X")
				.setAccessRecord("zeRecordId");

		recordServices.add(access);

		ExternalAccessUrl newAccess = recordsServices.getExternalAccessUrl("zeAccessId");

		assertThat(newAccess).isEqualToComparingFieldByField(access);
	}


	@Test
	public void givenAccessExpiredWhenClosingExpiredThenClosed()
			throws Exception {

		assertThat(recordsServices.externalAccessUrl.schemaType().getCacheType()).isEqualTo(FULLY_CACHED);
		assertThat(recordsServices.externalAccessUrl.schemaType().getLabel(Language.French)).isEqualTo("Url d'accès externe");
		LocalDate now = LocalDate.now();

		givenTimeIs(now);

		ExternalAccessUrl access1 = recordsServices.newExternalAccessUrlWithId("access1")
				.setExpirationDate(now.minusDays(1))
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("token1")
				.setAccessRecord("zeRecordId");

		ExternalAccessUrl access2 = recordsServices.newExternalAccessUrlWithId("access2")
				.setExpirationDate(now)
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("token1")
				.setAccessRecord("zeRecordId");

		ExternalAccessUrl access3 = recordsServices.newExternalAccessUrlWithId("access3")
				.setExpirationDate(now.plusDays(1))
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("token1")
				.setAccessRecord("zeRecordId");

		recordServices.execute(new Transaction(access1, access2, access3));

		QueryCounter queryCounter = newQueryCounter();
		getModelLayerFactory().getModelLayerBackgroundThreadsManager().getExpireExternalAccessUrls().run();

		assertThat(recordsServices.getExternalAccessUrl("access1").getStatus()).isEqualTo(EXPIRED);
		assertThat(recordsServices.getExternalAccessUrl("access2").getStatus()).isEqualTo(EXPIRED);
		assertThat(recordsServices.getExternalAccessUrl("access3").getStatus()).isEqualTo(OPEN);

		//We expect this service to execute no solr queries
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

	}

	@Test
	public void givenAccessToCloseWhenClosingThenClosed()
			throws Exception {

		assertThat(recordsServices.externalAccessUrl.schemaType().getCacheType()).isEqualTo(FULLY_CACHED);
		assertThat(recordsServices.externalAccessUrl.schemaType().getLabel(Language.French)).isEqualTo("Url d'accès externe");
		LocalDate now = LocalDate.now();

		givenTimeIs(now);

		ExternalAccessUrl access1 = recordsServices.newExternalAccessUrlWithId("access1")
				.setExpirationDate(now.plusDays(1))
				.setStatus(ExternalAccessUrlStatus.OPEN)
				.setToken("token1")
				.setAccessRecord("zeRecordId");

		ExternalAccessUrl access2 = recordsServices.newExternalAccessUrlWithId("access2")
				.setExpirationDate(now.plusDays(1))
				.setStatus(ExternalAccessUrlStatus.TO_CLOSE)
				.setToken("token1")
				.setAccessRecord("zeRecordId");

		recordServices.execute(new Transaction(access1, access2));

		QueryCounter queryCounter = newQueryCounter();
		getModelLayerFactory().getModelLayerBackgroundThreadsManager().getExpireExternalAccessUrls().run();

		assertThat(recordsServices.getExternalAccessUrl("access1").getStatus()).isEqualTo(OPEN);
		assertThat(recordsServices.getExternalAccessUrl("access2").getStatus()).isEqualTo(CLOSED);

		//We expect this service to execute no solr queries
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

	}
}
