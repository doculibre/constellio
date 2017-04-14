package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Test;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo6_5_1AcceptanceTest extends ConstellioTest {
	@Test
	public void givenPreviousSystemWithEmptyMetadataToDeleteWhenMigratingThenAllMetadataToDeleteAreDeleted()
			throws Exception {
		givenPreviousSystemWithEmptyMetadtaToDelete();
		MetadataSchemaTypes types = getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema folderSchema = types.getDefaultSchema(Folder.SCHEMA_TYPE);
		assertMetadataDeleted("calendarYearEntered", folderSchema);
		assertMetadataDeleted("calendarYear", folderSchema);
		MetadataSchema documentSchema = types.getDefaultSchema(Document.SCHEMA_TYPE);
		assertMetadataDeleted("calendarYearEntered", documentSchema);
		assertMetadataDeleted("calendarYear", documentSchema);
		assertFolderRangeDatesCreatedCorrectly();

		getModelLayerFactory().newReindexingServices().reindexCollections(RECALCULATE_AND_REWRITE);

		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		folderSchema = types.getDefaultSchema(Folder.SCHEMA_TYPE);
		assertMetadataDeleted("ruleAdminUnit", folderSchema);
	}

	@Test
	public void givenPreviousSystemWithMetadataToDeleteHavingValuesWhenMigratingThenAllMetadataToDeleteAreDisabled()
			throws Exception {
		givenPreviousSystemWithMetadtaToDeleteHavingValues();

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("q", "ruleAdminUnitId_ss:*");
		params.add("rows", "1");
		assertThat(getDataLayerFactory().newRecordDao().query(params).getNumFound()).isNotEqualTo(0);

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchema folderSchema = types.getDefaultSchema(Folder.SCHEMA_TYPE);
		MetadataSchema documentSchema = types.getDefaultSchema(Document.SCHEMA_TYPE);
		assertMetadataDisabledAndMarkForDeletion("calendarYearEntered", folderSchema);
		assertMetadataDisabledAndMarkForDeletion("calendarYear", folderSchema);
//		assertMetadataDisabledAndMarkForDeletion("calendarYearEntered", documentSchema);
//		assertMetadataDisabledAndMarkForDeletion("calendarYear", documentSchema);
		assertMetadataDisabledAndMarkForDeletion("ruleAdminUnit", folderSchema);
		assertFolderRangeDatesCreatedCorrectly();
		assertThat(getDataLayerFactory().newRecordDao().query(params).getNumFound()).isNotEqualTo(0);

		getModelLayerFactory().newReindexingServices().reindexCollections(RECALCULATE_AND_REWRITE);

		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		folderSchema = types.getDefaultSchema(Folder.SCHEMA_TYPE);
		documentSchema = types.getDefaultSchema(Document.SCHEMA_TYPE);
		assertMetadataDeleted("ruleAdminUnit", folderSchema);

		assertThat(getDataLayerFactory().newRecordDao().query(params).getNumFound()).isEqualTo(0);
	}

	private void assertMetadataDisabledAndMarkForDeletion(String metadataLocalCode, MetadataSchema schema) {
		assertThat(schema.getMetadata(metadataLocalCode).isEnabled()).isFalse();
		//assertThat(schema.getMetadata(metadataLocalCode).is()).isFalse();
	}

	private void assertMetadataDeleted(String metadataLocalCode, MetadataSchema schema) {
		try {
			schema.getMetadata(metadataLocalCode);
			fail("should be deleted");
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//OK
		}
	}

	private void assertFolderRangeDatesCreatedCorrectly() {
		MetadataSchema folderSchemaBuilder = getModelLayerFactory()
				.getMetadataSchemasManager().getSchemaTypes(zeCollection).getDefaultSchema(Folder.SCHEMA_TYPE);
		assertThat(folderSchemaBuilder.getMetadata(Folder.TIME_RANGE).isEnabled()).isFalse();
	}

	private void givenPreviousSystemWithMetadtaToDeleteHavingValues() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "6.4MetadataToDeleteWithValues.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	private void givenPreviousSystemWithEmptyMetadtaToDelete() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "6.4MetadataToDeleteWithoutValues.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
