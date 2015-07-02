/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		assertThat(
				metadataSchemaTypes.getSchema(StorageSpace.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE).isDefaultRequirement())
				.isTrue();
		assertThat(metadataSchemaTypes.getSchema(UniformSubdivision.DEFAULT_SCHEMA).getMetadata(Schemas.TITLE_CODE)
				.isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenUpdatingFrom5_0_3ThenSetTreeVisibilityCorrectly()
			throws OptimisticLockingConfiguration {
		givenSystemAtVersion5_0_3();
		getAppLayerFactory().newMigrationServices().migrate(zeCollection);

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThat(searchServices.getResultsCount(
				from(new RMSchemasRecordsServices(zeCollection, getModelLayerFactory()).folderSchemaType())
						.returnAll())).isGreaterThan(0);
		assertThat(searchServices.getResultsCount(
				from(new RMSchemasRecordsServices(zeCollection, getModelLayerFactory()).folderSchemaType())
						.where(Schemas.VISIBLE_IN_TREES).isTrueOrNull())).isGreaterThan(0);
	}

	private void givenSystemAtVersion5_0_3() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, STATE + ".zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
