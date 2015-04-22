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
package com.constellio.app.entities.modules;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataSchemasMigrationHelperTest extends ConstellioTest {

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypes metadataSchemaTypes;
	@Mock MetadataSchemaTypes newMetadataSchemaTypes;
	@Mock AppLayerFactory appLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock MigrationResourcesProvider migrationResourcesProvider;

	MetadataSchemasAlterationHelper script;

	@Before
	public void setUp()
			throws Exception {
		when(appLayerFactory.getModelLayerFactory()).thenReturn(modelLayerFactory);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(metadataSchemasManager.getSchemaTypes(zeCollection)).thenReturn(metadataSchemaTypes);

		script = spy(new MetadataSchemasAlterationHelper(zeCollection, migrationResourcesProvider, appLayerFactory) {

			@Override
			protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			}

		});

	}

	@Test
	public void givenOptimisticLockingWhenSavingSchemaThenRetry()
			throws Exception {

		doThrow(MetadataSchemasManagerException.OptimistickLocking.class).doThrow(
				MetadataSchemasManagerException.OptimistickLocking.class).doReturn(newMetadataSchemaTypes).when(
				metadataSchemasManager)
				.saveUpdateSchemaTypes(any(MetadataSchemaTypesBuilder.class));

		script.migrate();

		verify(script, times(3)).migrate(any(MetadataSchemaTypesBuilder.class));

	}

}
