package com.constellio.app.ui.pages.search.batchProcessing;

import static com.constellio.app.modules.rm.model.enums.FolderStatus.INACTIVE_DEPOSITED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessResults;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class BatchProcessingPresenterServiceAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();
	BatchProcessingPresenterService presenterService;
	MetadataSchema folderSchema;
	MetadataSchemaType folderSchemaType;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder folderSchemaType = types.getSchemaType(Folder.SCHEMA_TYPE);
				MetadataSchemaBuilder schema1 = folderSchemaType.createCustomSchema("custom1", fr("Custom schema 1"));
				MetadataSchemaBuilder schema2 = folderSchemaType.createCustomSchema("custom2", fr("Custom schema 2"));
				MetadataSchemaBuilder schema3 = folderSchemaType.createCustomSchema("custom3", fr("Custom schema 3"));

				schema1.create("stringMeta1").setType(MetadataValueType.STRING);
				schema2.create("stringMeta2").setType(MetadataValueType.STRING);
				schema3.create("stringMeta2").setType(MetadataValueType.STRING);

				schema1.create("textMeta").setType(MetadataValueType.TEXT);
				schema1.create("dateMeta").setType(MetadataValueType.DATE);
				schema1.create("dateTimeMeta").setType(MetadataValueType.DATE_TIME);
				schema1.create("booleanMeta").setType(MetadataValueType.BOOLEAN);
				schema1.create("numberMeta").setType(MetadataValueType.NUMBER);
				schema1.create("enumMeta").setType(MetadataValueType.ENUM).defineAsEnum(FolderStatus.class);
				schema1.create("referencedFolderMeta").setType(MetadataValueType.REFERENCE).defineReferencesTo(folderSchemaType);
			}
		});
		folderSchemaType = rm.folderSchemaType();
		searchServices = getModelLayerFactory().newSearchServices();
		presenterService = new BatchProcessingPresenterService(zeCollection, getAppLayerFactory());
		//givenConfig(ConstellioEIMConfigs.DATE_FORMAT, "yyyy-MM-dd");

	}

	@Test
	public void givenValuesOfEveryTypeAreModifiedThenApplied()
			throws Exception {

		BatchProcessRequest request = new BatchProcessRequest().setUser(users.adminIn(zeCollection))
				.setSchema(rm.folderSchema("custom1")).setIds(asList(records.folder_A03, records.folder_A04))
				.addModifiedMetadata("default_folder_title", "Mon dossier")
				.addModifiedMetadata("stringMeta1", "zeStringValue")
				.addModifiedMetadata("textMeta", "zeTextValue")
				.addModifiedMetadata("dateMeta", date(2042, 4, 5))
				.addModifiedMetadata("dateTimeMeta", dateTime(2042, 4, 5, 6, 7, 8))
				.addModifiedMetadata("booleanMeta", true)
				.addModifiedMetadata("numberMeta", 66.6)
				.addModifiedMetadata("enumMeta", INACTIVE_DEPOSITED)
				.addModifiedMetadata("referencedFolderMeta", records.folder_A06);

		BatchProcessResults results = presenterService.simulate(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Folder 3"),
				tuple(records.folder_A04, "Folder 4")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).extracting("schemaType.code", "count")
				.containsOnly(tuple(Document.SCHEMA_TYPE, 3));
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("document_default_title", "Folder 3", "Mon dossier"),
				tuple("document_default_stringMeta1", null, "zeStringValue"),
				tuple("document_default_textMeta", null, "zeTextValue"),
				tuple("document_default_dateMeta", null, "2042-04-05"),
				tuple("document_default_dateTimeMeta", null, "2042-04-05-06-07-08"),
				tuple("document_default_booleanMeta", null, "Vrai"),
				tuple("document_default_numberMeta", null, "66.6"),
				tuple("document_default_enumMeta", null, "Versé"),
				tuple("document_default_referencedFolderMeta", null, "A06 - Folder 6")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).extracting("schemaType.code", "count")
				.containsOnly(tuple(Document.SCHEMA_TYPE, 3));
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("document_default_title", "Folder 4", "Mon dossier"),
				tuple("document_default_stringMeta1", null, "zeStringValue"),
				tuple("document_default_textMeta", null, "zeTextValue"),
				tuple("document_default_dateMeta", null, "2042-04-05"),
				tuple("document_default_dateTimeMeta", null, "2042-04-05-06-07-08"),
				tuple("document_default_booleanMeta", null, "Vrai"),
				tuple("document_default_numberMeta", null, "66.6"),
				tuple("document_default_enumMeta", null, "Versé"),
				tuple("document_default_referencedFolderMeta", null, "A06 - Folder 6"),
				tuple("document_default_", null, "A06 - Folder 6")
		);

		assertThat(rm.searchFolders(where(Schemas.SCHEMA).isEqualTo("folder_custom1"))).hasSize(0);
		assertThat(rm.searchFolders(where(Schemas.SCHEMA).isEqualTo("folder_default"))).hasSize(105);

		results = presenterService.execute(request);

		assertThat(results.getRecordModifications()).extracting("recordId", "recordTitle").containsOnly(
				tuple(records.folder_A03, "Folder 3"),
				tuple(records.folder_A04, "Folder 4")
		);

		assertThat(results.getRecordModifications(records.folder_A03).getImpacts()).extracting("schemaType.code", "count")
				.containsOnly(tuple(Document.SCHEMA_TYPE, 3));
		assertThat(results.getRecordModifications(records.folder_A03).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("document_default_title", "Folder 3", "Mon dossier"),
				tuple("document_default_stringMeta1", null, "zeStringValue"),
				tuple("document_default_textMeta", null, "zeTextValue"),
				tuple("document_default_dateMeta", null, "2042-04-05"),
				tuple("document_default_dateTimeMeta", null, "2042-04-05-06-07-08"),
				tuple("document_default_booleanMeta", null, "Vrai"),
				tuple("document_default_numberMeta", null, "66.6"),
				tuple("document_default_enumMeta", null, "Versé"),
				tuple("document_default_referencedFolderMeta", null, "A06 - Folder 6")
		);

		assertThat(results.getRecordModifications(records.folder_A04).getImpacts()).extracting("schemaType.code", "count")
				.containsOnly(tuple(Document.SCHEMA_TYPE, 3));
		assertThat(results.getRecordModifications(records.folder_A04).getFieldsModifications())
				.extracting("metadata.code", "valueBefore", "valueAfter").containsOnly(
				tuple("document_default_title", "Folder 4", "Mon dossier"),
				tuple("document_default_stringMeta1", null, "zeStringValue"),
				tuple("document_default_textMeta", null, "zeTextValue"),
				tuple("document_default_dateMeta", null, "2042-04-05"),
				tuple("document_default_dateTimeMeta", null, "2042-04-05-06-07-08"),
				tuple("document_default_booleanMeta", null, "Vrai"),
				tuple("document_default_numberMeta", null, "66.6"),
				tuple("document_default_enumMeta", null, "Versé"),
				tuple("document_default_referencedFolderMeta", null, "A06 - Folder 6"),
				tuple("document_default_", null, "A06 - Folder 6")
		);

		assertThat(rm.searchFolders(where(Schemas.SCHEMA).isEqualTo("folder_custom1"))).hasSize(2);
		assertThat(rm.searchFolders(where(Schemas.SCHEMA).isEqualTo("folder_default"))).hasSize(103);
	}

}
