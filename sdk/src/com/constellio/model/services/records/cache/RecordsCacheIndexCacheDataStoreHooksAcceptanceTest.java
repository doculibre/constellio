package com.constellio.model.services.records.cache;

import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.GetByIdCounter;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RecordsCacheIndexCacheDataStoreHooksAcceptanceTest extends ConstellioTest {

	Transaction transaction;

	TestRecord uncachedRecord1, uncachedRecord2, permanentRecord1, permanentRecord2, permanentRecord3, volatileRecord3, volatileRecord4, volatileRecord1, volatileRecord2;

	TestsSchemasSetup collection1Schemas = new TestsSchemasSetup(zeCollection).withSecurityFlag(false);
	ZeSchemaMetadatas hookedCollection1Schema = collection1Schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas otherCollection1Schema = collection1Schemas.new AnotherSchemaMetadatas();

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup collection2Schemas = new TestsSchemasSetup(anotherCollection).withSecurityFlag(false);
	ZeSchemaMetadatas hookedCollection2Schema = collection2Schemas.new ZeSchemaMetadatas();

	RecordsCaches recordsCaches;
	RecordsCaches otherInstanceRecordsCaches;

	StatsBigVaultServerExtension queriesListener;
	StatsBigVaultServerExtension otherSystemQueriesListener;

	GetByIdCounter instanceGetByIdCounter;
	GetByIdCounter otherInstanceGetByIdCounter;

	RecordServices recordServices;
	RecordIdsHookDataIndexRetriever<Integer> zeHookDataRetriever;
	RecordIdsHookDataIndexRetriever<Integer> zeHookOtherInstanceDataRetriever;

	class HookedCollection1SchemaCharacterHook implements MetadataIndexCacheDataStoreHook<Integer> {

		int baseValue;

		public HookedCollection1SchemaCharacterHook(int baseValue) {
			this.baseValue = baseValue;
		}

		@Override
		public String getCollection() {
			return zeCollection;
		}

		@Override
		public boolean isHooked(MetadataSchemaType schemaType) {
			return schemaType.getCode().equals(hookedCollection1Schema.typeCode());
		}

		@Override
		public boolean requiresDataUpdate(Record record) {
			return record.isModified(hookedCollection1Schema.stringMetadata());

		}

		@Override
		public Set<Integer> getKeys(Record record) {
			String stringMetadata = record.get(hookedCollection1Schema.stringMetadata());
			Set<Integer> keys = new HashSet<>();
			for (char character : stringMetadata.toCharArray()) {
				keys.add(baseValue + letterId(character));
			}
			return keys;
		}

		@Override
		public Class<? extends Number> getKeyType() {
			return Integer.class;
		}
	}

	int letterId(char character) {
		return (int) character;
	}

	@Before
	public void setUp()
			throws Exception {

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setRecordsVolatileCacheMemorySize("10M");
			}
		});

		givenCollection(zeCollection).withAllTestUsers();
		givenCollection(anotherCollection).withAllTestUsers();

		inCollection(zeCollection).giveWriteAccessTo(admin);
		inCollection(anotherCollection).giveWriteAccessTo(admin);

		defineSchemasManager().using(collection1Schemas.withAStringMetadata(whichIsEssentialInSummary).withAnotherStringMetadata());
		defineSchemasManager().using(collection2Schemas.withAStringMetadata(whichIsEssentialInSummary));

		ModelLayerFactory otherModelLayerFactory = getModelLayerFactory("other");

		recordsCaches = getModelLayerFactory().getRecordsCaches();
		otherInstanceRecordsCaches = otherModelLayerFactory.getRecordsCaches();

		instanceGetByIdCounter = new GetByIdCounter(getClass()).listening(getModelLayerFactory());
		otherInstanceGetByIdCounter = new GetByIdCounter(getClass()).listening(otherModelLayerFactory);

		linkEventBus(getDataLayerFactory(), otherModelLayerFactory.getDataLayerFactory());

		collection1Schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(hookedCollection1Schema.type().getCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
				types.getSchemaType(otherCollection1Schema.type().getCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

		collection2Schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(hookedCollection2Schema.type().getCode()).setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

		zeHookDataRetriever = recordsCaches.registerRecordIdsHook(zeCollection, new HookedCollection1SchemaCharacterHook(0));
		zeHookOtherInstanceDataRetriever = otherInstanceRecordsCaches.registerRecordIdsHook(zeCollection, new HookedCollection1SchemaCharacterHook(0));

		DataLayerSystemExtensions extensions = getDataLayerFactory().getExtensions().getSystemWideExtensions();
		queriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(queriesListener);

		extensions = otherModelLayerFactory.getDataLayerFactory().getExtensions().getSystemWideExtensions();
		otherSystemQueriesListener = new StatsBigVaultServerExtension();
		extensions.getBigVaultServerExtension().add(otherSystemQueriesListener);

		tx = new Transaction();

		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCreatingModifyingOrDeletingRecordThenCacheUpdated() throws RecordServicesException {

		Record record1 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "ABC"));

		//Same key is returned only once
		Record record2 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "AAA"));

		Record record3 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "CD"));

		recordServices.execute(tx);

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(zeHookDataRetriever)) {
			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).containsOnly(
					record1.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).containsOnly(
					record1.getRecordId(), record3.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).containsOnly(
					record3.getRecordId());
		}

		recordServices.update(record2.set(hookedCollection1Schema.stringMetadata(), "BC"));

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(zeHookDataRetriever)) {
			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).containsOnly(
					record1.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId(), record3.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).containsOnly(
					record3.getRecordId());
		}

		recordServices.logicallyDelete(record2, User.GOD);

		//Nothing changed
		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(zeHookDataRetriever, zeHookOtherInstanceDataRetriever)) {
			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).containsOnly(
					record1.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId(), record3.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).containsOnly(
					record3.getRecordId());
		}

		recordServices.physicallyDeleteNoMatterTheStatus(record2, User.GOD, new RecordPhysicalDeleteOptions());

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(zeHookDataRetriever, zeHookOtherInstanceDataRetriever)) {
			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).containsOnly(
					record1.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).containsOnly(
					record1.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).containsOnly(
					record1.getRecordId(), record3.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).containsOnly(
					record3.getRecordId());
		}
	}

	@Test
	public void givenHookAddedAfterRecordLoadingThenNotEffectiveUntilReloaded()
			throws RecordServicesException {

		Record record1 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "ABC"));

		//Same key is returned only once
		Record record2 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "AAA"));

		Record record3 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "CD"));

		recordServices.execute(tx);

		RecordIdsHookDataIndexRetriever<Integer> retriever2 =
				recordsCaches.registerRecordIdsHook(zeCollection, new HookedCollection1SchemaCharacterHook(100000));
		RecordIdsHookDataIndexRetriever<Integer> otherInstanceRetriever2 =
				otherInstanceRecordsCaches.registerRecordIdsHook(zeCollection, new HookedCollection1SchemaCharacterHook(100000));

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(zeHookDataRetriever, zeHookOtherInstanceDataRetriever)) {
			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).containsOnly(
					record1.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).containsOnly(
					record1.getRecordId(), record3.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).containsOnly(
					record3.getRecordId());

			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('A')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('B')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('C')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('D')).list()).isEmpty();
		}

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(retriever2, otherInstanceRetriever2)) {

			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).isEmpty();

			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('A')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('B')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('C')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('D')).list()).isEmpty();
		}

		recordsCaches.getCache(zeCollection).invalidateVolatileReloadPermanent(asList(collection1Schemas.zeDefaultSchemaType().getCode()));

		for (RecordIdsHookDataIndexRetriever<Integer> retriever : asList(retriever2, otherInstanceRetriever2)) {

			assertThat(retriever.recordIdsStreamerWithKey(letterId('A')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('B')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('C')).list()).isEmpty();
			assertThat(retriever.recordIdsStreamerWithKey(letterId('D')).list()).isEmpty();

			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('A')).list()).containsOnly(
					record1.getRecordId(), record2.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('B')).list()).containsOnly(
					record1.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('C')).list()).containsOnly(
					record1.getRecordId(), record3.getRecordId());
			assertThat(retriever.recordIdsStreamerWithKey(100000 + letterId('D')).list()).containsOnly(
					record3.getRecordId());
		}

	}

	//@Test
	public void givenMultipleHooksThenNoConflicts() throws RecordServicesException {

		Record record1 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "ABC"));

		//Same key is returned only once
		Record record2 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "AAA"));

		Record record3 = tx.add(recordServices.newRecordWithSchema(hookedCollection1Schema.instance())
				.set(hookedCollection1Schema.stringMetadata(), "CD"));

		recordServices.execute(tx);

	}
}
