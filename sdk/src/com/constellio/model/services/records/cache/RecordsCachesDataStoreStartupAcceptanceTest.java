package com.constellio.model.services.records.cache;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.cache.dataStore.RecordsCachesDataStore;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.annotations.PerformanceTest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.data.dao.services.bigVault.solr.SolrUtils.atomicSet;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@PerformanceTest
public class RecordsCachesDataStoreStartupAcceptanceTest extends ConstellioTest {

	RecordsCachesDataStore dataStore;

	@Test
	public void given8MRecordsSystemWhenReopeningCacheUsingMapDBThenStable()
			throws Exception {
		doTest(false, false);
	}


	@Test
	public void given8MRecordsSystemWhenReopeningCacheUsingSolrThenStable()
			throws Exception {

		doTest(true, false);
	}


	@Test
	public void given8MRecordsSystemWithAlteredRecordsInSolrWhenReopeningCacheUsingMapDBThenStable()
			throws Exception {
		doTest(false, true);
	}


	@Test
	public void given8MRecordsSystemWithAlteredRecordsInSolrWhenReopeningCacheUsingSolrThenStable()
			throws Exception {

		doTest(true, true);
	}

	public void doTest(final boolean loadFromSolr, boolean alterDataWithoutUpdatingCache)
			throws Exception {

		Toggle.USE_MMAP_WITHMAP_DB_FOR_LOADING.enable();
		Toggle.USE_MMAP_WITHMAP_DB_FOR_RUNTIME.enable();
		Toggle.USE_FILESYSTEM_DB_FOR_LARGE_METADATAS_CACHE.enable();

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setForceCacheLoadingFromSolr(loadFromSolr);
			}
		});
		prepareSystem(withZeCollection(), withCollection("collection2"));
		String[] collections = new String[]{"zeCollection", "collection2"};

		String[] types = new String[]{"schemaType01", "schemaType02"};

		for (String collection : collections) {
			getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder typesBuilder) {
					for (String type : types) {
						MetadataSchemaTypeBuilder typeBuilder = typesBuilder.createNewSchemaTypeWithSecurity(type);
						typeBuilder.setRecordCacheType(RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE);
						//The number is kept in the memory byte array
						typeBuilder.getDefaultSchema().create("intMetadata").setType(INTEGER).setAvailableInSummary(true);

						//The number is kept in the filesystem byte array
						typeBuilder.getDefaultSchema().get("title").setSortable(true);
					}
				}
			});
		}

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		BulkRecordTransactionHandlerOptions options = new BulkRecordTransactionHandlerOptions();

		for (String collection : asList("zeCollection", "collection2")) {
			BulkRecordTransactionHandler handler = new BulkRecordTransactionHandler(
					recordServices, "RecordsCachesDataStoreStartupAcceptanceTest", options);
			for (String type : asList("schemaType01", "schemaType02")) {

				MetadataSchema schema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(type);
				Metadata intMetadata = schema.get("intMetadata");
				for (int i = 0; i < 6_000; i++) {
					Record record = recordServices.newRecordWithSchema(schema);
					int validationValue = record.getRecordId().intValue() + 42;
					record.set(Schemas.TITLE, "Hello, I am the record '" + record.getId() + ", my validation number is " + validationValue);
					record.set(intMetadata, validationValue);
					handler.append(record);
				}

			}
			handler.closeAndJoin();
		}


		validateIntegrity();
		if (alterDataWithoutUpdatingCache) {
			alterDataWithoutUpdatingCache();
		}
		assertThat(countSortValues()).isEqualTo(0);
		removeARecordInCacheMakingItLoseItsIntegrity();

		try {
			validateIntegrity();
			fail("Integrity failure expected");
		} catch (AssertionError ignored) {
		}

		restartLayers();


		while (!getModelLayerFactory().getRecordsCaches().areSummaryCachesInitialized()) {
			Thread.sleep(100);
		}

		validateIntegrity();
		assertThat(countSortValues()).isEqualTo(24000);

	}

	private void alterDataWithoutUpdatingCache() {
		for (String collection : asList("zeCollection", "collection2")) {

			List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
			for (String type : asList("schemaType01", "schemaType02")) {
				MetadataSchemaType schemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(type);
				getModelLayerFactory().getRecordsCaches().stream(schemaType).forEach((r) -> {
					SolrInputDocument doc = new SolrInputDocument();
					doc.setField("id", r.getRecordId().stringValue());
					doc.setField("test_s", atomicSet("Mouhahaha!"));
					solrInputDocuments.add(doc);
				});
			}

			try {
				getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer().add(solrInputDocuments);
				getDataLayerFactory().getRecordsVaultServer().flush();
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}
	}

	@After
	public void tearDown() throws Exception {
		clearTestSession();
	}

	private void removeARecordInCacheMakingItLoseItsIntegrity() {
		MetadataSchemaType schemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType("schemaType01");
		Record record = getModelLayerFactory().getRecordsCaches().stream(schemaType).skip(42).findFirst().get();
		getModelLayerFactory().getRecordsCaches().getRecordsCachesDataStore().remove(record.getRecordDTO());
	}

	private void validateIntegrity() {
		for (String collection : asList("zeCollection", "collection2")) {

			for (String type : asList("schemaType01", "schemaType02")) {
				MetadataSchemaType schemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(type);
				Metadata intMetadata = schemaType.getDefaultSchema().get("intMetadata");
				Set<Integer> idsIntValues = new HashSet<>();
				getModelLayerFactory().getRecordsCaches().stream(schemaType).forEach((r) -> {

					int validationValue = r.getRecordId().intValue() + 42;
					idsIntValues.add(r.getRecordId().intValue());

					assertThat(r.<Integer>get(intMetadata)).isEqualTo(validationValue);
					String expectedTitle = "Hello, I am the record '" + r.getId() + ", my validation number is " + validationValue;
					assertThat(r.<String>get(Schemas.TITLE)).isEqualTo(expectedTitle);


				});
				assertThat(idsIntValues).hasSize(6000);
			}

		}
	}


	private int countSortValues() {
		AtomicInteger countSortValues = new AtomicInteger();
		for (String collection : asList("zeCollection", "collection2")) {

			for (String type : asList("schemaType01", "schemaType02")) {
				MetadataSchemaType schemaType = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(type);
				getModelLayerFactory().getRecordsCaches().stream(schemaType).forEach((r) -> {

					if (r.getRecordDTO().getMainSortValue() > 0) {
						countSortValues.incrementAndGet();
					}

				});
			}

		}
		return countSortValues.get();
	}


}
