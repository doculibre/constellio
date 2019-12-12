package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecordAutomaticMetadataServicesAggregatedAcceptanceTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();
	private MetadataSchemasManager schemaManager;
	private RMSchemasRecordsServices rm;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		schemaManager = getModelLayerFactory().getMetadataSchemasManager();
	}

	@Test
	public void givenUnionAggregatedWithOnlySummaryMetadataThenMakeSureThatCacheIsUsed() throws Exception {
		MetadataSchemaTypesBuilder schemaTypesBuilder = schemaManager.modify(zeCollection);
		MetadataSchemaTypeBuilder folderSchemaType = schemaTypesBuilder.getSchemaType(Folder.SCHEMA_TYPE);
		MetadataSchemaTypeBuilder documentSchemaType = schemaTypesBuilder.getSchemaType(Document.SCHEMA_TYPE);
		folderSchemaType.createMetadata("subDocumentTitlesDescriptions")
				.setType(STRING).setMultivalue(true).defineDataEntry()
				.asUnion(documentSchemaType.getDefaultSchema().get(Document.FOLDER),
						documentSchemaType.getDefaultSchema().get(Document.TITLE),
						documentSchemaType.getDefaultSchema().get(Document.DESCRIPTION));
		schemaManager.saveUpdateSchemaTypes(schemaTypesBuilder);

		assertThat(SchemaUtils.areSummary(asList(
				rm.documentTypeSchema().getMetadata(Document.TITLE),
				rm.documentTypeSchema().getMetadata(Document.DESCRIPTION)))).isTrue();

		RecordProvider recordProvider = mock(RecordProvider.class);
		when(recordProvider.getRecord(records.folder_A01)).thenReturn(records.getFolder_A01().getWrappedRecord());

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(zeCollection));

		new RecordAutomaticMetadataServices(getModelLayerFactory())
				.updateAutomaticMetadatas((RecordImpl) records.getFolder_A01().getWrappedRecord(),
						recordProvider, singletonList("subDocumentTitlesDescriptions"),
						new Transaction(new RecordUpdateOptions().setUpdateAggregatedMetadatas(true)));

		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
	}

}
