package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
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
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
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
		inCollection(zeCollection).giveWriteAccessTo(dakota);

		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		//		Taxonomy taxonomy = new Taxonomy(String code, String title, String collection, boolean visibleInHomePage,
		//		List<String> userIds, List<String> groupIds, String taxonomySchemaType)

		dakotaId = users.dakotaLIndienIn(zeCollection).getId();
	}

	@Test
	public void whenReindexingThenReindexChildRecordsAfterTheParent1()
			throws Exception {
		defineSchemasManager().using(schemas.with(childOfReferenceToSelfAndCopiedMetadataFromParent()));
		givenTimeIs(shishOClock);

		getDataLayerFactory().getDataLayerLogger().setMonitoredIds(asList("003002"));
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
			assertThat(record("00" + i).<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}

		List<String> ids = new ArrayList<>();
		for (int i = 3002; i <= 3010; i++) {
			ids.add("00" + i);
		}
		alterCalculedFieldIn(ids);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));
		for (int i = 3002; i <= 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
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

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));
		for (int i = 3001; i < 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}

		List<String> ids = new ArrayList<>();
		for (int i = 3001; i < 3010; i++) {
			ids.add("00" + i);
		}
		alterCalculedFieldIn(ids);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));
		for (int i = 3001; i < 3010; i++) {
			System.out.println(i);
			assertThat(record("00" + i).<String>get(zeSchema.metadata(calculatedMetadata))).isEqualTo("Shish O Clock!");
		}
	}

	private void alterCalculedFieldIn(List<String> ids)
			throws Exception {

		List<RecordDeltaDTO> deltas = new ArrayList<>();

		for (String id : ids) {
			RecordDTO record = getDataLayerFactory().newRecordDao().realGet(id);

			Map<String, Object> modifiedMetadatas = new HashMap<>();
			modifiedMetadatas.put(calculatedMetadata + "_s", "Rick rolled!");
			RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedMetadatas, record.getFields());
			deltas.add(recordDeltaDTO);

		}

		getDataLayerFactory().newRecordDao().execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(deltas));

		getModelLayerFactory().getRecordsCaches().reloadAllSchemaTypes(zeCollection);
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
		assertThat(record("000042").<String>get(Schemas.TITLE)).isNull();
		assertThat(record("000042").<String>get(zeSchema.metadata(childOfReference))).isEqualTo("000666");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));
		assertThat(record("000042").<String>get(Schemas.TITLE)).isNull();
		assertThat(record("000042").<String>get(zeSchema.metadata(childOfReference))).isEqualTo("000666");
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

	// ---------------------------------------------------

	private void givenPrincipalTaxonomyWithZeSchema() {
		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "ze taxo");

		Map<Language, String> labelAbv1 = new HashMap<>();
		labelTitle1.put(Language.French, "ze");

		Taxonomy taxonomy = new Taxonomy("taxo", labelTitle1, labelAbv1, zeCollection, zeSchema.typeCode());
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, metadataSchemasManager);
		getModelLayerFactory().getTaxonomiesManager().setPrincipalTaxonomy(taxonomy, metadataSchemasManager);
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

	public static class ReindexingServicesAcceptanceTest_Calculator2 extends AbstractMetadataValueCalculator<String> {

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
