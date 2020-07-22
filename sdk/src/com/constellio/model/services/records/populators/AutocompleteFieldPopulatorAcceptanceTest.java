package com.constellio.model.services.records.populators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.api.ListAssert;
import org.junit.Test;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA_AUTOCOMPLETE_FIELD;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AutocompleteFieldPopulatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void givenFolderFieldsWithAutocompleteThenCopiedInChildFoldersAndDocuments()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction tx = new Transaction();

		tx.add(records.getCategory_X().setTitle("chat"));

		Folder heroesFolder = tx.add(records.newFolderWithValuesAndId("heroesFolderId").setTitle("Super héros")
				.setCategoryEntered(records.categoryId_X));
		Folder chuckFolder = tx.add(records.newChildFolderWithIdIn("chuckFolderId", heroesFolder).setTitle("Chuck Norris"));
		Folder dakotaFolder = tx.add(records.newChildFolderWithIdIn("dakotaFolderId", heroesFolder).setTitle("Dakota l'indien"));
		tx.add(records.newDocumentWithIdIn("roundhousekick", chuckFolder).setTitle("Round house kick"));
		tx.add(records.newDocumentWithIdIn("wololo", dakotaFolder).setTitle("Wololo"));
		recordServices.execute(tx);

		//		assertThatRecordsWithFullWordInAutocompleteField("chat").containsOnly(records.categoryId_X);
		assertThatRecordsWithFullWordInAutocompleteField("super")
				.containsOnly("heroesFolderId", "chuckFolderId", "dakotaFolderId", "roundhousekick", "wololo");
		assertThatRecordsWithFullWordInAutocompleteField("chuck").containsOnly("chuckFolderId", "roundhousekick");
		assertThatRecordsWithFullWordInAutocompleteField("dakota").containsOnly("dakotaFolderId", "wololo");
		assertThatRecordsWithFullWordInAutocompleteField("round").containsOnly("roundhousekick");
		assertThatRecordsWithFullWordInAutocompleteField("wololo").containsOnly("wololo");
	}

	@Test
	public void givenArabicTextThenAutocompleteIsWorking()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction tx = new Transaction();

		Folder folder = tx.add(records.newFolderWithValuesAndId("arabicFolderTest").setTitle("إدارة موقع الواجهة")
				.setCategoryEntered(records.categoryId_X));
		recordServices.execute(tx);

		assertThatRecordsWithFullWordInAutocompleteField("إدارة").containsOnly("arabicFolderTest");
		assertThatRecordsWithFullWordInAutocompleteField("موقع").containsOnly("arabicFolderTest");
		assertThatRecordsWithFullWordInAutocompleteField("إدموقعارة").isEmpty();
	}

	@Test
	public void whenFolderAutocompleteFieldIsPopulatedThenKeptInCache()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction tx = new Transaction();

		Folder folder = tx.add(records.newFolderWithValuesAndId("arabicFolderTest").setTitle("إدارة موقع الواجهة")
				.setCategoryEntered(records.categoryId_X));
		recordServices.execute(tx);

		MetadataSchema schema = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory()).folder.schema();
		Metadata metadata = schema.get(Schemas.ATTACHED_ANCESTORS.getLocalCode());

		assertThat(metadata.isStoredInSummaryCache()).isFalse();

		Record record = getModelLayerFactory().newCachelessRecordServices().getDocumentById("arabicFolderTest");
		assertThat(record.<String>getList(metadata)).isNotEmpty();

		assertThatRecordsWithFullWordInAutocompleteField("إدارة").containsOnly("arabicFolderTest");
		assertThatRecordsWithFullWordInAutocompleteField("موقع").containsOnly("arabicFolderTest");
		assertThatRecordsWithFullWordInAutocompleteField("إدموقعارة").isEmpty();
	}


	private ListAssert<String> assertThatRecordsWithFullWordInAutocompleteField(String word) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		return assertThat(searchServices.searchRecordIds(from(asList(rm.folderSchemaType(), rm.documentSchemaType()))
				.where(SCHEMA_AUTOCOMPLETE_FIELD).isEqualTo(word)));
	}
}
