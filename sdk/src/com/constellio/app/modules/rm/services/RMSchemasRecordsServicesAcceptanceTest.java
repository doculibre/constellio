package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class RMSchemasRecordsServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	RMSchemasRecordsServices rm;

	@Test
	public void validateLinkedSchemaUtilsMethods()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("meetingFolder"))).isEqualTo("folder_meetingFolder");
		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("employe"))).isEqualTo("folder_employe");
		assertThat(rm.getLinkedSchemaOf(rm.getFolderTypeWithCode("other"))).isEqualTo("folder_default");

		Folder folder = records.getFolder_A05();
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_default");

		folder.setType(rm.getFolderTypeWithCode("meetingFolder"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_meetingFolder");

		folder.setType(rm.getFolderTypeWithCode("employe"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_employe");

		folder.setType(rm.getFolderTypeWithCode("other"));
		assertThat(rm.getLinkedSchemaOf(folder)).isEqualTo("folder_default");

	}

	@Test
	public void givenMultilingualMetadatasThenLocalisedValueObtainedUsingSchemasRecordServices()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
				.withAllTest(users));

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(Category.SCHEMA_TYPE).getDefaultSchema().get(Category.TITLE).setMultiLingual(true);
			}
		});

		Record categoryIdX = recordServices.getDocumentById(records.categoryId_X);
		categoryIdX.set(Schemas.TITLE, Locale.CANADA_FRENCH, "{fr} Xe category");
		categoryIdX.set(Schemas.TITLE, Locale.ENGLISH, "{en} Xe category");
		recordServices.update(categoryIdX);

		assertThat((Object) recordServices.getDocumentById(records.categoryId_X).get(Schemas.TITLE)).isEqualTo("{fr} Xe category");
		assertThat((Object) recordServices.getDocumentById(records.categoryId_X).get(Schemas.TITLE, Locale.CANADA_FRENCH))
				.isEqualTo("{fr} Xe category");
		assertThat((Object) recordServices.getDocumentById(records.categoryId_X).get(Schemas.TITLE, Locale.ENGLISH))
				.isEqualTo("{en} Xe category");

		RMSchemasRecordsServices defaultRM = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMSchemasRecordsServices frenchRM = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory(),
				Locale.CANADA_FRENCH);
		RMSchemasRecordsServices englishRM = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory(), Locale.ENGLISH);

		assertThat(defaultRM.getCategory(records.categoryId_X).getCode()).isEqualTo("X");
		assertThat(frenchRM.getCategory(records.categoryId_X).getCode()).isEqualTo("X");
		assertThat(englishRM.getCategory(records.categoryId_X).getCode()).isEqualTo("X");

		assertThat(defaultRM.getCategory(records.categoryId_X).getTitle()).isEqualTo("{fr} Xe category");
		assertThat(frenchRM.getCategory(records.categoryId_X).getTitle()).isEqualTo("{fr} Xe category");
		assertThat(englishRM.getCategory(records.categoryId_X).getTitle()).isEqualTo("{en} Xe category");

	}
}
