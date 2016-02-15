package com.constellio.model.services.schemas;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEncrypted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssential;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class MetadataSchemasManagerMetadataFlagsAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager otherMetadataSchemasManager;

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	@Test
	public void whenAddUpdateSchemasThenSaveEssentialFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsEssential).withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().isEssential()).isTrue();
		assertThat(zeSchema.booleanMetadata().isEssential()).isFalse();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEssential(false);
				types.getSchema(zeSchema.code()).get(zeSchema.booleanMetadata().getLocalCode()).setEssential(true);
			}
		});

		assertThat(zeSchema.stringMetadata().isEssential()).isFalse();
		assertThat(zeSchema.booleanMetadata().isEssential()).isTrue();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveEncryptedFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata(whichIsEncrypted));

		assertThat(zeSchema.stringMetadata().isEncrypted()).isFalse();
		assertThat(zeSchema.anotherStringMetadata().isEncrypted()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEncrypted(true);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode()).setEncrypted(false);
			}
		});

		assertThat(zeSchema.stringMetadata().isEncrypted()).isTrue();
		assertThat(zeSchema.anotherStringMetadata().isEncrypted()).isFalse();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveEssentialInSummaryFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata(whichIsEssentialInSummary));

		assertThat(zeSchema.stringMetadata().isEssentialInSummary()).isFalse();
		assertThat(zeSchema.anotherStringMetadata().isEssentialInSummary()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEssentialInSummary(true);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode())
						.setEssentialInSummary(false);
			}
		});

		assertThat(zeSchema.stringMetadata().isEssentialInSummary()).isTrue();
		assertThat(zeSchema.anotherStringMetadata().isEncrypted()).isFalse();
	}

	@Before
	public void setUp()
			throws Exception {

		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		DataStoreTypesFactory typesFactory = new SolrDataStoreTypesFactory();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		otherMetadataSchemasManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));

	}
}
