package com.constellio.model.services.records.populators;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA_AUTOCOMPLETE_FIELD;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.ListAssert;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class AutocompleteFieldPopulatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	RecordServices recordServices;
	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	@Before
	public void before()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

	}

	@Test
	public void givenFolderFieldsWithAutocompleteThenCopiedInChildFoldersAndDocuments()
			throws Exception {

		//		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
		//			@Override
		//			public void alter(MetadataSchemaTypesBuilder types) {
		//				types.getSchemaType(Category.SCHEMA_TYPE).getMetadata(Category.TITLE).setSchemaAutocomplete(true);
		//			}
		//		});

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

	private ListAssert<String> assertThatRecordsWithFullWordInAutocompleteField(String word) {
		return assertThat(searchServices.searchRecordIds(from(asList(rm.folderSchemaType(), rm.documentSchemaType()))
				.where(SCHEMA_AUTOCOMPLETE_FIELD).isEqualTo(word)));
	}
}
