package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo7_2AcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Test
	public void givenPreviousSystemWhereCategoryDescriptionIsAStringThenMigrateToText()
			throws Exception {
		givenPreviousSystemWhereCategoryDescriptionIsAString();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Category category = rm.getCategory(records.categoryId_X);
		Metadata descriptionMetadata = category.getSchema().getMetadata(Category.DESCRIPTION);
		assertThat(descriptionMetadata.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(descriptionMetadata.getDataStoreCode()).isEqualTo("description_t");
		assertThat(category.getDescription()).isEqualTo("Ze ultimate category X");
	}

	private void givenPreviousSystemWhereCategoryDescriptionIsAString() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "RMMigrationTo7_2AcceptanceTest_categoryDescriptionIsAString.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
