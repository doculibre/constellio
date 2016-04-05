package com.constellio.model.services.records.reindexing;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;

@SlowTest
public class ReindexingServicesOneSchemaWithMultipleSelfReferencesAcceptanceTest extends ConstellioTest {

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

	String childOfReference = "childOfReference";
	String anotherReference = "anotherReference";

	String textMetadata = "textMetadata";
	String calculatedMetadata = "calculatedMetadata";

	@Before
	public void setup()
			throws Exception {

		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withAllTest(users)
		);
		getDataLayerFactory().getDataLayerLogger().logAllTransactions();
		inCollection(zeCollection).giveWriteAccessTo(dakota);

		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		//		Taxonomy taxonomy = new Taxonomy(String code, String title, String collection, boolean visibleInHomePage,
		//		List<String> userIds, List<String> groupIds, String taxonomySchemaType)

		dakotaId = users.dakotaLIndienIn(zeCollection).getId();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run1()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run2()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run3()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run4()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run5()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run6()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run7()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run8()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1_run9()
			throws Exception {
		whenReindexingThenReindexChildRecordsAfterTheParent1();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndCopiedMetadataFromParent()));
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));

		transaction.add(new TestRecord(zeSchema, "003002").set(zeSchema.metadata(childOfReference), "003001"));
		transaction.add(new TestRecord(zeSchema, "003003").set(zeSchema.metadata(childOfReference), "003002"));
		transaction.add(new TestRecord(zeSchema, "003005").set(zeSchema.metadata(childOfReference), "003004"));
		transaction.add(new TestRecord(zeSchema, "003007").set(zeSchema.metadata(childOfReference), "003006"));
		transaction.add(new TestRecord(zeSchema, "003010").set(zeSchema.metadata(childOfReference), "003009"));
		transaction.add(new TestRecord(zeSchema, "003009").set(zeSchema.metadata(childOfReference), "003008"));
		transaction.add(new TestRecord(zeSchema, "003008").set(zeSchema.metadata(childOfReference), "003007"));
		transaction.add(new TestRecord(zeSchema, "003006").set(zeSchema.metadata(childOfReference), "003005"));
		transaction.add(new TestRecord(zeSchema, "003004").set(zeSchema.metadata(childOfReference), "003003"));
		transaction.add(new TestRecord(zeSchema, "003001").set(zeSchema.metadata(textMetadata), "Shish O Clock!"));
		recordServices.execute(transaction);

		for (int i = 3002; i <= 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}

		List<String> ids = new ArrayList<>();
		for (int i = 3002; i <= 3010; i++) {
			ids.add("00" + i);
		}
		alterCalculedFieldIn(ids);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));
		for (int i = 3002; i <= 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent2()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndCopiedMetadataFromParent()));
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));

		transaction.add(new TestRecord(zeSchema, "003001").set(zeSchema.metadata(childOfReference), "003002"));
		transaction.add(new TestRecord(zeSchema, "003002").set(zeSchema.metadata(childOfReference), "003003"));
		transaction.add(new TestRecord(zeSchema, "003003").set(zeSchema.metadata(childOfReference), "003004"));
		transaction.add(new TestRecord(zeSchema, "003004").set(zeSchema.metadata(childOfReference), "003005"));
		transaction.add(new TestRecord(zeSchema, "003005").set(zeSchema.metadata(childOfReference), "003006"));
		transaction.add(new TestRecord(zeSchema, "003006").set(zeSchema.metadata(childOfReference), "003007"));
		transaction.add(new TestRecord(zeSchema, "003007").set(zeSchema.metadata(childOfReference), "003008"));
		transaction.add(new TestRecord(zeSchema, "003008").set(zeSchema.metadata(childOfReference), "003009"));
		transaction.add(new TestRecord(zeSchema, "003009").set(zeSchema.metadata(childOfReference), "003010"));
		transaction.add(new TestRecord(zeSchema, "003010").set(zeSchema.metadata(textMetadata), "Shish O Clock!"));
		recordServices.execute(transaction);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));
		for (int i = 3001; i < 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}

		List<String> ids = new ArrayList<>();
		for (int i = 3001; i < 3010; i++) {
			ids.add("00" + i);
		}
		alterCalculedFieldIn(ids);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));
		for (int i = 3001; i < 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}
	}

	private void alterCalculedFieldIn(List<String> ids)
			throws Exception {

		List<RecordDeltaDTO> deltas = new ArrayList<>();

		for (String id : ids) {
			RecordDTO record = getDataLayerFactory().newRecordDao().get(id);

			Map<String, Object> modifiedMetadatas = new HashMap<>();
			modifiedMetadatas.put(calculatedMetadata + "_s", "Rick rolled!");
			RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedMetadatas, record.getFields());
			deltas.add(recordDeltaDTO);
		}

		getDataLayerFactory().newRecordDao().execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(deltas));
	}

	@Test
	public void whenReindexingThenNoValidation()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndAnotherReferenceToSelf()));
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata(childOfReference), "000666");

		transaction.add(new TestRecord(zeSchema, "000666"));

		recordServices.execute(transaction);

		makeTheTitleOfZeSchemaRequired();

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));
		assertThat(record("000042").get(Schemas.TITLE)).isNull();
		assertThat(record("000042").get(zeSchema.metadata(childOfReference))).isEqualTo("000666");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));
		assertThat(record("000042").get(Schemas.TITLE)).isNull();
		assertThat(record("000042").get(zeSchema.metadata(childOfReference))).isEqualTo("000666");
	}

	private void makeTheTitleOfZeSchemaRequired() {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		MetadataSchemaTypesBuilder typesBuilder = metadataSchemasManager.modify(zeCollection);
		typesBuilder.getSchema(zeSchema.code()).get(Schemas.TITLE_CODE).setDefaultRequirement(true);

		try {
			metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	@Test
	public void givenCyclicRecordDependencyWhenReindexingThenNoInfiniteLoop()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndAnotherReferenceToSelf()));
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata(childOfReference), "000666");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata(anotherReference), "000042");
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, null);
		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, null);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(100));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, null);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, null);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(100));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, null);

	}

	@Test
	public void givenCyclicRecordDependencyWithinATaxoWhenReindexingThenNoInfiniteLoop()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndAnotherReferenceToSelf()));
		givenPrincipalTaxonomyWithZeSchema();
		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata(childOfReference), "000666");

		TestRecord record666 =new TestRecord(zeSchema, "000666");
				transaction.add(record666);
		recordServices.execute(transaction);

		recordServices.update(record666.set(zeSchema.metadata(anotherReference), "000042"));

		givenTimeIs(shishOClock.plusHours(1));
		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("taxo", "000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, asList("taxo"));
		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("taxo", "000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, asList("taxo"));

		givenTimeIs(shishOClock.plusHours(2));
		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(100));

		assertCounterIndexForRecordWithValueAndAncestors("000042", 1, asList("taxo", "000666"));
		assertCounterIndexForRecordWithValueAndAncestors("000666", 0, asList("taxo"));

	}

	// ---------------------------------------------------

	private void givenPrincipalTaxonomyWithZeSchema() {
		Taxonomy taxonomy = new Taxonomy("taxo", "ze taxo", zeCollection, zeSchema.typeCode());
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, metadataSchemasManager);
		getModelLayerFactory().getTaxonomiesManager().setPrincipalTaxonomy(taxonomy, metadataSchemasManager);
	}

	private void modifyCounterIndex(String recordId, double value) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		try {
			RecordDTO record = recordDao.get("idx_rfc_" + recordId);

			Map<String, Object> modifiedFields = new HashMap<>();
			modifiedFields.put("refs_d", value);
			RecordDeltaDTO modifyValueDeltaDTO = new RecordDeltaDTO(record, modifiedFields, record.getFields());
			transaction = transaction.withModifiedRecords(asList(modifyValueDeltaDTO));
			recordDao.execute(transaction);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void deleteCounterIndex(String recordId) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		transaction = transaction.withDeletedByQueries(new ModifiableSolrParams().set("q", "id:idx_rfc_" + recordId));
		try {
			recordDao.execute(transaction);
		} catch (com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}
	}

	private void deleteActiveIndex(String recordId) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		transaction = transaction.withDeletedByQueries(new ModifiableSolrParams().set("q", "id:idx_act_" + recordId));
		try {
			recordDao.execute(transaction);
		} catch (com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}
	}

	private void assertActiveIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_act_" + recordId);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			fail("No active index for record id '" + recordId + "'");
		}
	}

	private void assertNoActiveIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_act_" + recordId);
			fail("No active index expected for record id '" + recordId + "'");
		} catch (NoSuchRecordWithId noSuchRecordWithId) {

		}
	}

	private void assertCounterIndexForRecordWithValueAndAncestors(String recordId, double expectedValue, List<String> ancestors) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_rfc_" + recordId);
			assertThat(recordDTO.getFields().get("refs_d")).isEqualTo(expectedValue);
			assertThat(recordDTO.getFields().get("ancestors_ss")).isEqualTo(ancestors);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			fail("No counter index for record id '" + recordId + "'");
		}
	}

	private void assertNoCounterIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_rfc_" + recordId);
			Double wasValue = (Double) recordDTO.getFields().get("refs_d");
			fail("No counter index expected for record id '" + recordId + "'");
		} catch (NoSuchRecordWithId noSuchRecordWithId) {

		}
	}

	private MetadataSchemaTypesConfigurator childOfReferenceToSelfAndAnotherReferenceToSelf() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeSchemaType = schemaTypes.getSchemaType("zeSchemaType");
				MetadataSchemaBuilder zeSchema = zeSchemaType.getDefaultSchema();
				zeSchema.create(childOfReference).defineChildOfRelationshipToType(zeSchemaType);
				zeSchema.create(anotherReference).defineReferencesTo(zeSchemaType);
			}
		};
	}

	private MetadataSchemaTypesConfigurator childOfReferenceToSelfAndCopiedMetadataFromParent() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeSchemaType = schemaTypes.getSchemaType("zeSchemaType");
				MetadataSchemaBuilder zeSchema = zeSchemaType.getDefaultSchema();
				zeSchema.create(childOfReference).defineChildOfRelationshipToType(zeSchemaType);
				zeSchema.create(textMetadata).setType(MetadataValueType.STRING);
				zeSchema.create(calculatedMetadata).setType(MetadataValueType.STRING)
						.defineDataEntry().asCalculated(ReindexingServicesAcceptanceTest_Calculator2.class);
			}
		};
	}

	public static class ReindexingServicesAcceptanceTest_Calculator implements MetadataValueCalculator<String> {

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

	public static class ReindexingServicesAcceptanceTest_Calculator2 implements MetadataValueCalculator<String> {

		ReferenceDependency<String> calcualtedMetadata = ReferenceDependency.toAString("childOfReference", "calculatedMetadata");
		LocalDependency<String> textMetadata = LocalDependency.toAString("textMetadata");

		@Override
		public String calculate(CalculatorParameters parameters) {
			String text = parameters.get(textMetadata);
			return text != null ? text : parameters.get(calcualtedMetadata);
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
			return asList(textMetadata, calcualtedMetadata);
		}
	}
}
