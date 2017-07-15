package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo7_2_0_1AcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Test
	public void givenPreviousSystemWhereUniformSubdivisionDescriptionIsAStringThenMigrateToText()
			throws Exception {
		givenPreviousSystemWhereUniformSubdivisionDescriptionIsAString();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		UniformSubdivision uniformSubdivision = rm.getUniformSubdivision(records.subdivId_1);
		Metadata descriptionMetadata = uniformSubdivision.getSchema().getMetadata(UniformSubdivision.DESCRIPTION);
		assertThat(descriptionMetadata.getType()).isEqualTo(MetadataValueType.TEXT);
		assertThat(descriptionMetadata.getDataStoreCode()).isEqualTo("description_t");
		assertThat(uniformSubdivision.getDescription()).isEqualTo("description1");
	}

	private void givenPreviousSystemWhereUniformSubdivisionDescriptionIsAString() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_7.2_with_tasks,rm_modules__with_document_rules.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
