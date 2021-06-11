package com.constellio.app.ui.pages.trash;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2017-06-06.
 */
public class TrashPresenterAcceptanceTest extends ConstellioTest {
	@Mock
	TrashViewImpl view;
	MockedNavigation navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	TrashPresenter presenter;
	SessionContext sessionContext;
	RMSchemasRecordsServices rm;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		presenter = new TrashPresenter(view);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenMoreThan1000OrEqualToRecordForTypeThenUseSolrDirectly() throws RecordServicesException {
		Transaction transaction = new Transaction();

		for (int i = 0; i < 1000; i++) {
			ContainerRecord containerRecord = rm.newContainerRecord();
			containerRecord.setType(records.containerTypeId_boite22x22);
			containerRecord.setDecommissioningType(DecommissioningType.DEPOSIT);
			containerRecord.setAdministrativeUnits(asList(records.unitId_10a));
			containerRecord.setTitle("fdsfdsfsddfs");
			containerRecord.setIdentifier("container" + i);
			containerRecord.set(Schemas.LOGICALLY_DELETED_STATUS, true);

			transaction.add(containerRecord);
			if (i % 1000 == 0) {
				recordServices.execute(transaction);
				transaction = new Transaction();
			}
		}

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.execute(transaction);
		when(view.getSelectedType()).thenReturn(ContainerRecord.SCHEMA_TYPE);
		assertThat(presenter.getQuery().getQueryExecutionMethod()).isEqualTo(QueryExecutionMethod.USE_SOLR);
	}

	@Test
	public void whenLessThan1000RecordForTypeThenUseCache() throws RecordServicesException {
		Transaction transaction = new Transaction();

		for (int i = 0; i < 999; i++) {
			ContainerRecord containerRecord = rm.newContainerRecord();
			containerRecord.setType(records.containerTypeId_boite22x22);
			containerRecord.setDecommissioningType(DecommissioningType.DEPOSIT);
			containerRecord.setAdministrativeUnits(asList(records.unitId_10a));
			containerRecord.setTitle("fdsfdsfsddfs");
			containerRecord.setIdentifier("container" + i);
			containerRecord.set(Schemas.LOGICALLY_DELETED_STATUS, true);

			transaction.add(containerRecord);
			if (i % 1000 == 0) {
				recordServices.execute(transaction);
				transaction = new Transaction();
			}
		}

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.execute(transaction);
		when(view.getSelectedType()).thenReturn(ContainerRecord.SCHEMA_TYPE);
		assertThat(presenter.getQuery().getQueryExecutionMethod()).isEqualTo(QueryExecutionMethod.DEFAULT);
	}

	@Test
	public void givenSecuredSchemaTypeThenFindCorrectRecords() {
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(0);

		recordServices.logicallyDelete(records.getFolder_A01().getWrappedRecord(), User.GOD);
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(5);
	}

	@Test
	public void givenUnsecuredSchemaTypeThenFindCorrectRecords() {
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(0);

		recordServices.logicallyDelete(records.getContainerBac01().getWrappedRecord(), User.GOD);
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(1);
	}

	@Test
	public void givenBothSecuredAndUnsecuredSchemaTypeThenFindCorrectRecords() {
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(0);

		recordServices.logicallyDelete(records.getFolder_A01().getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(records.getContainerBac01().getWrappedRecord(), User.GOD);
		assertThat(presenter.getLogicallyDeletedRecordsCount()).isEqualTo(6);
	}
}
