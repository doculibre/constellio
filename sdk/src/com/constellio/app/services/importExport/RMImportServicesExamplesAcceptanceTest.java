package com.constellio.app.services.importExport;

import static com.constellio.app.services.importExport.settings.model.ImportedDataEntry.asJEXLScript;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.importExport.settings.SettingsImportServices;
import com.constellio.app.services.importExport.settings.model.ImportedCollectionSettings;
import com.constellio.app.services.importExport.settings.model.ImportedDataEntry;
import com.constellio.app.services.importExport.settings.model.ImportedMetadataSchema;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileWriter;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@InDevelopmentTest
@UiTest
public class RMImportServicesExamplesAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void createSequentialNumberBasedOnCategoryAndWhichIsReusedWithChildFolders()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTestUsers());
		recordServices = getModelLayerFactory().newRecordServices();

		ImportedSettings importedSettings = new ImportedSettings();
		ImportedCollectionSettings zeCollectionSettings = importedSettings.newCollectionSettings(zeCollection);
		ImportedMetadataSchema folderSchema = zeCollectionSettings.newType(Folder.SCHEMA_TYPE).getDefaultSchema();
		folderSchema.newMetadata("USRcategorySequentialNumber").setLabel("Numéro séquentiel de la rubrique").setType(STRING)
				.setDataEntry(ImportedDataEntry.asMetadataProvidingSequence(Folder.CATEGORY_ENTERED)).setVisibleInDisplay(false);
		folderSchema.newMetadata("USRsequentialNumber").setLabel("Numéro séquentiel").setType(STRING)
				.setVisibleInDisplay(true).setVisibleInSearchResult(true).setVisibleInTables(true).setDataEntry(asJEXLScript("" +
				"\nif (parentFolder.USRsequentialNumber == null) {" +
				"\n    USRcategorySequentialNumber" +
				"\n} else {" +
				"\n    parentFolder.USRsequentialNumber" +
				"\n}"
		));

		getDataLayerFactory().getSequencesManager().set(records.categoryId_ZE42, 41);

		new SettingsImportServices(getAppLayerFactory()).importSettings(importedSettings);
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		System.out.println("Script to import : ");
		TestUtils.printDocument(new SettingsXMLFileWriter().writeSettings(importedSettings));

		Folder folder1 = validFolderWithCategoryFactory.get().setTitle("1").setCategoryEntered(records.categoryId_X13);
		Folder folder2 = validFolderWithCategoryFactory.get().setTitle("2").setCategoryEntered(records.categoryId_X13);
		Folder folder3 = validFolderWithCategoryFactory.get().setTitle("3").setCategoryEntered(records.categoryId_ZE42);

		Folder childOfFolder1 = newChildFolderFactory.get().setTitle("4").setParentFolder(folder1);
		Folder childOfFolder2 = newChildFolderFactory.get().setTitle("5").setParentFolder(folder2);
		Folder childOfFolder3 = newChildFolderFactory.get().setTitle("6").setParentFolder(folder3);

		recordServices.execute(new Transaction(folder1, folder2, folder3, childOfFolder1, childOfFolder2, childOfFolder3));

		TestUtils.assertThatRecords(folder1, folder2, folder3, childOfFolder1, childOfFolder2, childOfFolder3)
				.extractingMetadatas(Schemas.TITLE, rm.folder.schema().get("USRcategorySequentialNumber"),
						rm.folder.schema().get("USRsequentialNumber")).containsOnly(
				tuple("1", "1", "1"),
				tuple("2", "2", "2"),
				tuple("3", "42", "42"),
				tuple("4", null, "1"),
				tuple("5", null, "2"),
				tuple("6", null, "42")
		);

		newWebDriver();
		waitUntilICloseTheBrowsers();
	}

	Factory<Folder> validFolderWithCategoryFactory = new Factory<Folder>() {
		@Override
		public Folder get() {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
			return rm.newFolder().setOpenDate(TimeProvider.getLocalDate())
					.setAdministrativeUnitEntered(records.unitId_10)
					.setRetentionRuleEntered(records.ruleId_1);
		}
	};

	Factory<Folder> newChildFolderFactory = new Factory<Folder>() {
		@Override
		public Folder get() {
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
			return rm.newFolder().setOpenDate(TimeProvider.getLocalDate());
		}
	};
}
