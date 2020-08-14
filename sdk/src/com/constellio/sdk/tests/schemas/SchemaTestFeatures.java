package com.constellio.sdk.tests.schemas;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.sdk.tests.FactoriesTestFeatures;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.constellio.sdk.tests.SDKConstellioFactoriesInstanceProvider.DEFAULT_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
		final String collection = typesBuilder.getCollection();
		if (collection == null) {
			throw new RuntimeException("Collection cannot be null");
		}
		if (mocked) {
			ModelLayerFactory modelLayerFactory = mock(ModelLayerFactory.class);
			TaxonomiesManager taxonomiesManager = mock(TaxonomiesManager.class);
			MetadataSchemaTypes types = spy(typesBuilder.build(new FakeDataStoreTypeFactory()));
			typesBuilder = (new MetadataSchemaTypesBuilder(types.getCollectionInfo())).modify(types, modelLayerFactory, new DefaultClassProvider());
			reset(manager);
			when(manager.getSchemaTypes(collection)).thenReturn(types);
			when(manager.getSchemaTypeOf(any(Record.class))).then(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation)
						throws Throwable {

					Record record = (Record) invocation.getArguments()[0];

					if (record == null) {
						return null;
					}

					return manager.getSchemaTypes(collection).getSchemaType(record.getTypeCode());
				}
			});

			when(manager.getSchemaOf(any(Record.class))).then(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation)
						throws Throwable {

					Record record = (Record) invocation.getArguments()[0];

					if (record == null) {
						return null;
					}
					return manager.getSchemaTypes(collection).getSchema(record.getSchemaCode());
				}
			});

			return types;
		} else {
			try {
				if (!collectionsManager.getCollectionCodes().contains(collection)) {
					throw new RuntimeException("No such collection : " + collection);
				}
				manager.saveUpdateSchemaTypes(typesBuilder);
				MetadataSchemaTypes types = manager.getSchemaTypes(collection);
				typesBuilder = (new MetadataSchemaTypesBuilder(types.getCollectionInfo()))
						.modify(types, null, new DefaultClassProvider());
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
		ModelLayerFactory modelLayerFactory = mock(ModelLayerFactory.class);
		SchemasSetup.prepareSetups(manager, modelLayerFactory, null);
		try {
			reset(manager);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public SchemaTestFeatures use() {
		if (manager == null) {
			manager = factoriesTestFeatures.newModelServicesFactory(DEFAULT_NAME).getMetadataSchemasManager();
			collectionsManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getCollectionsManager();
			pluginManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getPluginManager();
			modulesManager = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory().getModulesManager();
		}
		SchemasSetup.prepareSetups(manager, factoriesTestFeatures.getConstellioFactories().getModelLayerFactory(), collectionsManager);
		return this;
	}

	public void afterTest(boolean firstClean) {
		if (!firstClean) {
			SchemasSetup.clearSetupList();
		}
		manager = null;
	}
}
