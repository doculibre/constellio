package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.events.EventBusManagerExtension;
import com.constellio.data.events.ReceivedEventParams;
import com.constellio.data.events.SentEventParams;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class ReindexingServicesOneSchemaAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(5);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	//TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;
	ReindexingServices reindexingServices;
	RecordDao recordDao;

	Users users = new Users();
	String dakotaId;

	@Before
	public void setup()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withAllTest(users)
		);
		inCollection(zeCollection).giveWriteAccessTo(dakota);

		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		defineSchemasManager().using(schemas.with(copiedAndCalculatedMetadatas()));

		dakotaId = users.dakotaLIndienIn(zeCollection).getId();
	}

	@Test
	public void whenReindexingThenKeepPrivateKey()
			throws Exception {
		byte[] keyBefore = EncryptionKeyFactory.getApplicationKey(getModelLayerFactory()).getEncoded();
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);
		byte[] keyAfter = EncryptionKeyFactory.getApplicationKey(getModelLayerFactory()).getEncoded();
		assertThat(keyAfter).isEqualTo(keyBefore);

	}


	@Test
	public void whenReindexingThenDoNotSendEvents()
			throws Exception {

		AtomicInteger received = new AtomicInteger();
		AtomicInteger sent = new AtomicInteger();
		getDataLayerFactory().getExtensions().getSystemWideExtensions().eventBusManagerExtensions.add(new EventBusManagerExtension() {
			@Override
			public void onEventReceived(ReceivedEventParams params) {
				received.incrementAndGet();
			}

			@Override
			public void onEventSent(SentEventParams params) {
				sent.incrementAndGet();
			}
		});
		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);
		assertThat(received.get()).isEqualTo(0);
		assertThat(sent.get()).isEqualTo(0);

	}

	@Test
	public void whenReindexingThenFlushBeforeReindexing()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		Record record666 = new TestRecord(zeSchema, "000666")
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		transaction.add(record666);
		recordServices.execute(transaction);

		Transaction transaction2 = new Transaction();
		transaction2.add(record666.set(zeSchema.metadata("referenceToZeSchema"), null));
		transaction2.setRecordFlushing(RecordsFlushing.LATER());
		recordServices.execute(transaction2);
		assertThat(getModelLayerFactory().newCachelessRecordServices().getDocumentById("000666").<String>get(zeSchema.metadata("referenceToZeSchema"))).isEqualTo("000042");

		reindexingServices.reindexCollection(zeCollection, ReindexationMode.RECALCULATE);
		assertThat(getModelLayerFactory().newCachelessRecordServices().getDocumentById("000666").<String>get(zeSchema.metadata("referenceToZeSchema"))).isNull();
	}

	@Test
	public void givenAutomaticMetadataMarkedForDeletionWhenReindexingThenValuesRemovedAndMetadataDeleted()
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "calculatedMetadata_s:*");

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		assertThat(recordDao.query(params).getNumFound()).isNotEqualTo(0);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).getMetadata("calculatedMetadata").setMarkedForDeletion(true);
			}
		});

		assertThat(zeSchema.metadata("calculatedMetadata").isMarkedForDeletion()).isTrue();
		assertThat(recordDao.query(params).getNumFound()).isNotEqualTo(0);

		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		schemas.refresh();
		assertThat(recordDao.query(params).getNumFound()).isEqualTo(0);
		assertThat(zeSchema.instance().hasMetadataWithCode("calculatedMetadata")).isFalse();

	}

	@Test
	public void givenManualMetadataMarkedForDeletionWhenReindexingThenValuesRemovedAndMetadataDeleted()
			throws Exception {

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("stringMetadata").setType(STRING);
			}
		});

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "stringMetadata_s:*");

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("stringMetadata"), "value1");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("stringMetadata"), "value2");
		recordServices.execute(transaction);

		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		assertThat(recordDao.query(params).getNumFound()).isNotEqualTo(0);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).getMetadata("stringMetadata").setMarkedForDeletion(true);
			}
		});

		assertThat(zeSchema.metadata("stringMetadata").isMarkedForDeletion()).isTrue();
		assertThat(recordDao.query(params).getNumFound()).isNotEqualTo(0);

		reindexingServices.reindexCollections(RECALCULATE_AND_REWRITE);

		schemas.refresh();
		assertThat(recordDao.query(params).getNumFound()).isEqualTo(0);
		assertThat(zeSchema.instance().hasMetadataWithCode("stringMetadata")).isFalse();

	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_1()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		RecordDTO record = recordDao.get("000042");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection)
				.invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	@Test
	public void givenExtensionThrowingExceptionsWhenReindexingThenContinue()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		RecordDTO record = recordDao.get("000042");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {
			@Override
			public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList("zeSchemaType"));
		reindexingServices.reindexCollections(new ReindexationParams(RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)

				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_2()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000666");
		recordServices.execute(transaction);

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		RecordDTO record = recordDao.get("000666");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection)
				.invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	// ---------------------------------------------------

	private MetadataSchemaTypesConfigurator copiedAndCalculatedMetadatas() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaBuilder zeSchema = schemaTypes.getDefaultSchema("zeSchemaType");
				MetadataBuilder copiedMetadataInput = zeSchema.create("copiedMetadataInput").setType(STRING);
				zeSchema.create("calculatedMetadataInput").setType(STRING);

				//MetadataSchemaBuilder anotherSchema = schemaTypes.getDefaultSchema("anotherSchemaType");
				MetadataBuilder referenceToZeSchema = zeSchema.create("referenceToZeSchema")
						.defineReferencesTo(asList(zeSchema));
				zeSchema.create("copiedMetadata").setType(STRING)
						.defineDataEntry().asCopied(referenceToZeSchema, copiedMetadataInput);
				zeSchema.create("calculatedMetadata").setType(STRING)
						.defineDataEntry().asCalculated(ReindexingServicesAcceptanceTest_Calculator.class);

			}
		};
	}

	public static class ReindexingServicesAcceptanceTest_Calculator extends AbstractMetadataValueCalculator<String> {

		ReferenceDependency<String> inputDependency = ReferenceDependency
				.toAString("referenceToZeSchema", "calculatedMetadataInput");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(inputDependency);
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(inputDependency);
		}
	}
}
