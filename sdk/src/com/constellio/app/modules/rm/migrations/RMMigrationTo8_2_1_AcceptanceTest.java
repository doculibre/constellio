package com.constellio.app.modules.rm.migrations;

import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo8_2_1_AcceptanceTest extends ConstellioTest {
	@Before
	public void init() {
		givenSystemLanguageIs("en");
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8_2_with_tasks,rm_modules.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
		i18n.setLocale(null);
	}

	@Test
	public void whenMigrateThenOK() {
		//test

		assertThat(getAppLayerFactory().getModelLayerFactory().getCollectionsListManager()
				.getCollectionInfo(Collection.SYSTEM_COLLECTION).getCollectionLanguages()).contains(Language.English);


		for (MetadataSchemaType schemaType : getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(Collection.SYSTEM_COLLECTION).getSchemaTypes()) {

			for (MetadataSchema schema : schemaType.getAllSchemas()) {

				for (Metadata metadata : schema.getMetadatas()) {
					assertThat(metadata.getLabels().get(Language.English)).isNotNull();
				}

			}

		}

	}

}
