package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class RMMigrationTo5_0_4_AcceptanceTest extends ConstellioTest {
	public static String STATE = "given_system_in_5.0.3_with_rm_module__with_test_records";

	@Test
	public void whenInCurrentVersionThenCodeAndTitleEnabledAndRequired() {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);

		MetadataSchemaTypes metadataSchemaTypes = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		for (MetadataSchemaType type : metadataSchemaTypes.getSchemaTypes()) {
			if (type.getDefaultSchema().hasMetadataWithCode(Schemas.CODE.getCode())) {
				if (!type.getCode().equals(FilingSpace.SCHEMA_TYPE)) {
					assertThat(type.getDefaultSchema().getMetadata(Schemas.CODE.getCode()).isUniqueValue()).isTrue();
				}
				assertThat(type.getDefaultSchema().getMetadata(Schemas.CODE.getCode()).isEnabled()).isTrue();
				assertThat(type.getDefaultSchema().getMetadata(Schemas.CODE.getCode()).isDefaultRequirement()).isTrue();
			}
		}
		assertThat(metadataSchemaTypes.getSchema(AdministrativeUnit.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE)
				.isDefaultRequirement()).isTrue();
		assertThat(metadataSchemaTypes.getSchema(Category.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE).isDefaultRequirement())
				.isTrue();
		assertThat(
				metadataSchemaTypes.getSchema(FilingSpace.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE).isDefaultRequirement())
				.isTrue();
		assertThat(metadataSchemaTypes.getSchema(StorageSpace.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE).isDefaultRequirement())
				.isTrue();
		assertThat(metadataSchemaTypes.getSchema(UniformSubdivision.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE)
				.isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenUpdatingFrom5_0_3ThenSetTreeVisibilityCorrectly()
			throws OptimisticLockingConfiguration {
		givenDisabledAfterTestValidations();
		givenSystemAtVersion5_0_3();
		getAppLayerFactory().newMigrationServices().migrate(null, false);

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThat(searchServices.getResultsCount(
				from(new RMSchemasRecordsServices(zeCollection, getAppLayerFactory()).folderSchemaType())
						.returnAll())).isGreaterThan(0);
		assertThat(searchServices.getResultsCount(
				from(new RMSchemasRecordsServices(zeCollection, getAppLayerFactory()).folderSchemaType())
						.where(Schemas.VISIBLE_IN_TREES).isTrueOrNull())).isGreaterThan(0);
	}

	private void givenSystemAtVersion5_0_3() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder + File.separator + "olds", STATE + ".zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
