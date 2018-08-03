package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MetadataSchemasMigrationHelperTest extends ConstellioTest {

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock MetadataSchemaTypesBuilder metadataSchemaTypesBuilder;
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
		when(metadataSchemasManager.modify(zeCollection)).thenReturn(metadataSchemaTypesBuilder);

		script = spy(new MetadataSchemasAlterationHelper(zeCollection, migrationResourcesProvider, appLayerFactory) {

			@Override
			protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			}

		});

	}

	@Test
	public void givenOptimisticLockingWhenSavingSchemaThenRetry()
			throws Exception {

		doThrow(OptimisticLocking.class)
				.doThrow(OptimisticLocking.class)
				.doReturn(newMetadataSchemaTypes)
				.when(metadataSchemasManager).saveUpdateSchemaTypes(any(MetadataSchemaTypesBuilder.class));

		script.migrate();

		verify(script, times(3)).migrate(any(MetadataSchemaTypesBuilder.class));

	}

}
