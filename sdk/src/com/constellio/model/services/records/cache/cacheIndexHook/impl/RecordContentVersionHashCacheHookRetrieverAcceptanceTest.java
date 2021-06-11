package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.Assertions;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class RecordContentVersionHashCacheHookRetrieverAcceptanceTest extends ConstellioTest {

	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		this.recordServices = getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenDocumentsWithCurrentVersionContentThenInRetrivableByHookRetriver() throws RecordServicesException {
		Record recordA19 = recordServices.get(records.document_A19);
		Document documentA19 = rm.wrapDocument(recordA19);

		String hash = documentA19.getContent().getCurrentVersion().getHash();
		List<Record> recordList = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent(hash);

		Assertions.assertThat(recordList.size()).isEqualTo(1);
		Assertions.assertThat(rm.wrapDocument(recordList.get(0)).getContent().getCurrentVersion().getHash())
				.isEqualTo(hash);

		Record recordA49 = recordServices.get(records.document_A49);

		Document documentA49 = rm.wrapDocument(recordA49);

		Content contentA49 = documentA49.getContent();
		documentA19.setContent(contentA49);
		String newHash = contentA49.getCurrentVersion().getHash();

		recordServices.update(documentA19.getWrappedRecord());

		List<Record> recordListResponseWithOldHash = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent(hash);

		Assertions.assertThat(recordListResponseWithOldHash).isEmpty();

		List<Record> recordListResponseWithNewHash = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent(newHash);

		Assertions.assertThat(recordListResponseWithNewHash).hasSize(2);
		Assertions.assertThat(recordListResponseWithNewHash.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A49, records.document_A19);
	}

	@Test
	public void givenEventWithContentThenNotRetrivable() throws RecordServicesException {
		Event event = (Event) rm.newEvent().setTitle("event").setUsername(records.getAlice().getUsername()).setType(EventType.CREATE_DOCUMENT)
				.setCreatedOn(LocalDateTime.now());

		Record recordA19 = recordServices.get(records.document_A19);
		Document documentA19 = rm.wrapDocument(recordA19);

		event.setContent(documentA19.getContent());

		recordServices.update(event.getWrappedRecord());

		String hash = documentA19.getContent().getCurrentVersion().getHash();

		List<Record> recordList = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent(hash);

		Assertions.assertThat(recordList.size()).isEqualTo(1);
		Assertions.assertThat(rm.wrapDocument(recordList.get(0)).getContent().getCurrentVersion().getHash())
				.isEqualTo(hash);
	}

	@Test
	public void givenDocumentsWithTwoContentVersionWithDifferentDocumentsThenRetrivableByEachHashCodeInHookRetriver() throws RecordServicesException {
		Record recordA19 = recordServices.get(records.document_A19);
		Document documentA19 = rm.wrapDocument(recordA19);

		Content currentContentA19 = documentA19.getContent();

		Assertions.assertThat(currentContentA19.getVersions()).hasSize(2);

		Assertions.assertThat(currentContentA19.getVersion("0.1").getHash()).isEqualTo("KCJFO65QNCGUNRSY5VLETQQ3IOG4KDAS");
		Assertions.assertThat(currentContentA19.getVersion("1.0").getHash()).isEqualTo("RX3R2XF542QWCTIILGYAV2IDO3THIFVD");


		List<Record> recordListResponseWithV01 = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent("KCJFO65QNCGUNRSY5VLETQQ3IOG4KDAS");

		Assertions.assertThat(recordListResponseWithV01).hasSize(1);
		Assertions.assertThat(recordListResponseWithV01.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A19);

		List<Record> recordListResponseWithV10 = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent("RX3R2XF542QWCTIILGYAV2IDO3THIFVD");

		Assertions.assertThat(recordListResponseWithV10).hasSize(1);
		Assertions.assertThat(recordListResponseWithV10.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A19);
	}

	@Test
	public void givenDocumentsUpdatingNewContentVersionThenRetrivableByHookRetriverWithEachHashCode() throws RecordServicesException {
		Record recordA19 = recordServices.get(records.document_A19);
		Document documentA19 = rm.wrapDocument(recordA19);

		Content currentContentA19 = documentA19.getContent();

		Assertions.assertThat(currentContentA19.getVersions()).hasSize(2);

		Assertions.assertThat(currentContentA19.getVersion("0.1").getHash()).isEqualTo("KCJFO65QNCGUNRSY5VLETQQ3IOG4KDAS");
		Assertions.assertThat(currentContentA19.getVersion("1.0").getHash()).isEqualTo("RX3R2XF542QWCTIILGYAV2IDO3THIFVD");


		Document documentA49 = rm.getDocument(records.document_A49);
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersion currentVersionA49 = documentA49.getContent().getCurrentVersion();
		ContentVersionDataSummary contentVersionDataSummary = contentManager.upload(contentManager.getContentInputStream(currentVersionA49.getHash(), currentVersionA49.getFilename()));

		Content content = currentContentA19.updateContent(users.adminIn(zeCollection), contentVersionDataSummary, false);

		documentA19.setContent(content);

		recordServices.update(documentA19);

		List<Record> recordListResponseWithV01 = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent("KCJFO65QNCGUNRSY5VLETQQ3IOG4KDAS");

		Assertions.assertThat(recordListResponseWithV01).hasSize(1);
		Assertions.assertThat(recordListResponseWithV01.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A19);

		List<Record> recordListResponseWithV10 = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent("RX3R2XF542QWCTIILGYAV2IDO3THIFVD");

		Assertions.assertThat(recordListResponseWithV10).hasSize(1);
		Assertions.assertThat(recordListResponseWithV10.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A19);

		List<Record> recordListResponseWithV11 = getModelLayerFactory().getRecordContentVersionHashCacheHookRetriever(zeCollection)
				.getRecordsWithContent("F4MWX5FQEUTNZJ6EDBCB5UCTAPFJMPXP");

		Assertions.assertThat(recordListResponseWithV11).hasSize(2);
		Assertions.assertThat(recordListResponseWithV11.stream().map(record -> record.getId()).collect(Collectors.toList())).contains(records.document_A19, records.document_A49);
	}
}
