package com.constellio.sdk.tests.schemas;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.FactoriesTestFeatures;

public class SchemaTestFeatures {

	ConstellioPluginManager pluginManager;
	CollectionsManager collectionsManager;
	MetadataSchemasManager manager;
	ConstellioModulesManager modulesManager;

	private FactoriesTestFeatures factoriesTestFeatures;
	private boolean mocked;

	public SchemaTestFeatures(FactoriesTestFeatures factoriesTestFeatures) {
		this.factoriesTestFeatures = factoriesTestFeatures;
	}

	public SchemaTestFeatures() {
		mocked = true;
	}

	public <S extends SchemasSetup> void using(S setup) {
		MetadataSchemaTypesBuilder typesBuilder = setup.getTypesBuilder();
		MetadataSchemaTypes types = save(typesBuilder);
		setup.onSchemaBuilt(types);
	}

	private MetadataSchemaTypes save(MetadataSchemaTypesBuilder typesBuilder) {
		if (manager == null) {
			manager = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
			collectionsManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getCollectionsManager();
			pluginManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getPluginManager();
			modulesManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getModulesManager();
		}
		String collection = typesBuilder.getCollection();
		if (collection == null) {
			throw new RuntimeException("Collection cannot be null");
		}
		if (mocked) {
			ModelLayerFactory modelLayerFactory = mock(ModelLayerFactory.class);
			TaxonomiesManager taxonomiesManager = mock(TaxonomiesManager.class);
			MetadataSchemaTypes types = spy(typesBuilder.build(new FakeDataStoreTypeFactory(), modelLayerFactory));
			typesBuilder = MetadataSchemaTypesBuilder.modify(types);
			reset(manager);
			when(manager.getSchemaTypes(collection)).thenReturn(types);
			return types;
		} else {
			try {
				if (!collectionsManager.getCollectionCodes().contains(collection)) {
					throw new RuntimeException("No such collection : " + collection);
				}
				manager.saveUpdateSchemaTypes(typesBuilder);
				MetadataSchemaTypes types = manager.getSchemaTypes(collection);
				typesBuilder = MetadataSchemaTypesBuilder.modify(types);
				return types;
			} catch (MetadataSchemasManagerException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public SchemaTestFeatures useWithMockedSchemaManager(MetadataSchemasManager metadataSchemaManager) {
		this.manager = metadataSchemaManager;

		this.mocked = isMockedManager(manager);
		return this;
	}

	private boolean isMockedManager(MetadataSchemasManager manager) {
		collectionsManager = mock(CollectionsManager.class, "collectionsServices");
		pluginManager = mock(ConstellioPluginManager.class, "pluginManager");
		SchemasSetup.prepareSetups(manager, null);
		try {
			reset(manager);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public SchemaTestFeatures use() {
		if (manager == null) {
			manager = factoriesTestFeatures.newModelServicesFactory().getMetadataSchemasManager();
			collectionsManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getCollectionsManager();
			pluginManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getPluginManager();
			modulesManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getModulesManager();
		}
		SchemasSetup.prepareSetups(manager, collectionsManager);
		return this;
	}

	public void afterTest(boolean firstClean) {
		if (!firstClean) {
			SchemasSetup.clearSetupList();
		}
		manager = null;
	}
}
