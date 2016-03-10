package com.constellio.model.services.schemas;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasInputMask;
import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class MetadataSchemasManagerInputMaskAcceptanceTest extends ConstellioTest {

	LocalDate zeUltimateDate = new LocalDate(2015, 06, 02);
	LocalDate anotherDate = new LocalDate(2014, 11, 25);

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.minusHours(42);

	MetadataSchemasManager otherMetadataSchemasManager;

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	String record1Id = "record1Id";
	String record2Id = "record2Id";
	String record3Id = "record3Id";
	String parentOfRecord12Id = "parentOfRecord12";
	String parentOfRecord3Id = "parentOfRecord3";

	@Test
	public void givenStringMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadata(whichHasInputMask("(###) ###-####")));

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("(###) ###-####");
		assertThat(anotherSchema.stringMetadata()).isEqualTo("(###) ###-####");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setInputMask("###.###.####");
			}
		});

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("###.###.####");
		assertThat(anotherSchema.stringMetadata()).isEqualTo("###.###.####");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(anotherSchema.code()).get(zeSchema.stringMetadata().getLocalCode())
						.setInputMask("(###) ### ####");
			}
		});

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("###.###.####");
		assertThat(anotherSchema.stringMetadata()).isEqualTo("(###) ### ####");
	}

	@Before
	public void setUp()
			throws Exception {

		otherMetadataSchemasManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));

	}

	private MetadataSchemaType zeType() {
		return otherMetadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType");
	}

	private MetadataSchema zeTypeDefaultSchema() {
		return otherMetadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType("zeSchemaType").getDefaultSchema();
	}

	private void deleteRecord(String id) {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		Record record = recordServices.getDocumentById(id);
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);
	}

	private Record newZeSchemaRecord() {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(zeSchema.instance());
	}

	private Record newAnotherSchemaRecord() {
		return getModelLayerFactory().newRecordServices().newRecordWithSchema(anotherSchema.instance());
	}

	private void givenTaxonomyOfZeSchema() {
		Taxonomy taxonomy = new Taxonomy("zeTaxo", "zeTaxo", zeCollection, zeSchema.typeCode());
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, getModelLayerFactory().getMetadataSchemasManager());
	}

	private void givenOtherTypeRecords()
			throws Exception {

		Record record1 = new TestRecord(zeSchema, record1Id);
		Record record2 = new TestRecord(zeSchema, record2Id);
		Record record3 = new TestRecord(zeSchema, record3Id);

		record1.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), parentOfRecord12Id);
		record2.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), parentOfRecord12Id);
		record3.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), parentOfRecord3Id);

		Record parentOfRecord12 = new TestRecord(zeSchema, parentOfRecord12Id);
		Record parentOfRecord3 = new TestRecord(zeSchema, parentOfRecord3Id);

		getModelLayerFactory().newRecordServices()
				.execute(new Transaction().addAll(record1, record2, record3, parentOfRecord12, parentOfRecord3));
	}
}
