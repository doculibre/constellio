package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE;
import static com.constellio.model.services.records.reindexing.ReindexationMode.REWRITE;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;

// Confirm @SlowTest
public class ReindexingServicesTwoSchemasAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(5);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

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
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_1()
			throws Exception {

		givenDisabledAfterTestValidations();

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(anotherSchema, "000666"))
				.set(anotherSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertThatRecord(withId("000666"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		//This call is breaking the transactionnal log
		RecordDTO record = recordDao.get("000042");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection)
				.invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(REWRITE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_2()
			throws Exception {
		givenDisabledAfterTestValidations();
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(anotherSchema, "000042"))
				.set(anotherSchema.metadata("referenceToZeSchema"), "000666");
		recordServices.execute(transaction);

		assertThatRecord(withId("000042"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		//This call is breaking the transactionnal log
		RecordDTO record = recordDao.get("000666");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		getModelLayerFactory().getRecordsCaches().getCache(zeCollection)
				.invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(anotherSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(anotherSchema.metadata("calculatedMetadata"), "value4")
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

				MetadataSchemaBuilder anotherSchema = schemaTypes.getDefaultSchema("anotherSchemaType");
				MetadataBuilder referenceToZeSchema = anotherSchema.create("referenceToZeSchema")
						.defineReferencesTo(asList(zeSchema));
				anotherSchema.create("copiedMetadata").setType(STRING)
						.defineDataEntry().asCopied(referenceToZeSchema, copiedMetadataInput);
				anotherSchema.create("calculatedMetadata").setType(STRING)
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
