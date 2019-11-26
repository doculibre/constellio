package com.constellio.model.services.schemas;

import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultValue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MetadataSchemasManagerDefaultValueAcceptanceTest extends ConstellioTest {

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
	public void givenReferenceMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(record1Id);
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(record1Id);
		assertThat(newAnotherSchemaRecord().<String>get(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema")))
				.isEqualTo(record1Id);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(record2Id);
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(record2Id);
		assertThat(newAnotherSchemaRecord().<String>get(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema")))
				.isEqualTo(record2Id);

	}

	//@Test
	//No longer working that way, logically delete is not blocked when there is a default value
	public void givenReferenceMetadataWithDefaultValueWhenDeletingRecordThenRemoveDefaultValue()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(record2Id);
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(record2Id);

		deleteRecord(record2Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isNull();
	}

	@Test
	public void givenReferenceMetadataWithDefaultValueWhenDeletingParentRecordThenRemoveDefaultValue()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(record2Id);
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(record2Id);

		deleteRecord(parentOfRecord3Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(record2Id);

		deleteRecord(parentOfRecord12Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isNull();
	}

	@Test
	public void givenMultivalueReferenceMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(asList(record1Id, record2Id));
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList(record1Id, record2Id));
		assertThat(newAnotherSchemaRecord().<List<String>>get(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema")))
				.isEqualTo(asList(record1Id, record2Id));

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(asList(record1Id, record3Id));
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList(record1Id, record3Id));
		assertThat(newAnotherSchemaRecord().<List<String>>get(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema")))
				.isEqualTo(asList(record1Id, record3Id));

	}

	//@Test
	//No longer working that way, logically delete is not blocked when there is a default value
	public void givenMultivalueReferenceMetadataWithDefaultValueWhenDeletingRecordThenRemoveDefaultValue()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(asList(record1Id, record2Id));
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList(record1Id, record2Id));

		deleteRecord(record2Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(asList(record1Id));

		deleteRecord(record1Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isNull();
	}

	@Test
	public void givenMultivalueReferenceMetadataWithDefaultValueWhenDeletingParentRecordThenRemoveDefaultValue()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAParentReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAParentReferenceFromZeSchemaToZeSchema());
		//		givenTaxonomyOfZeSchema();
		givenOtherTypeRecords();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(anotherSchema.typeCode()).getDefaultSchema().get("referenceFromAnotherSchemaToZeSchema")
						.setDefaultValue(asList(record1Id, record3Id));
			}
		});
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue())
				.isEqualTo(asList(record1Id, record3Id));

		deleteRecord(parentOfRecord12Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isEqualTo(asList(record3Id));

		deleteRecord(parentOfRecord3Id);
		schemas.refresh();
		assertThat(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema").getDefaultValue()).isNull();
	}

	@Test
	public void givenStringMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichHasDefaultValue("<?Ze default valué:;&>")));

		assertThat(zeSchema.stringMetadata().getDefaultValue()).isEqualTo("<?Ze default valué:;&>");
		assertThat(newZeSchemaRecord().<String>get(zeSchema.stringMetadata())).isEqualTo("<?Ze default valué:;&>");
	}

	@Test
	public void givenMultivalueStringMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList("value1", "value2"))));

		assertThat(zeSchema.stringMetadata().getDefaultValue()).isEqualTo(asList("value1", "value2"));
		assertThat(newZeSchemaRecord().<List<String>>get(zeSchema.stringMetadata())).isEqualTo(asList("value1", "value2"));
	}

	@Test
	public void givenTextMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata(whichHasDefaultValue("<?Ze default valué:;&>")));

		assertThat(zeSchema.largeTextMetadata().getDefaultValue()).isEqualTo("<?Ze default valué:;&>");
		assertThat(newZeSchemaRecord().<String>get(zeSchema.largeTextMetadata())).isEqualTo("<?Ze default valué:;&>");
	}

	@Test
	public void givenMultivalueTextMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList("value1", "value2"))));

		assertThat(zeSchema.largeTextMetadata().getDefaultValue()).isEqualTo(asList("value1", "value2"));
		assertThat(newZeSchemaRecord().<List<String>>get(zeSchema.largeTextMetadata())).isEqualTo(asList("value1", "value2"));
	}

	@Test
	public void givenDateMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withADateMetadata(whichHasDefaultValue(zeUltimateDate)));

		assertThat(zeSchema.dateMetadata().getDefaultValue()).isEqualTo(zeUltimateDate);
		assertThat(newZeSchemaRecord().<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(zeUltimateDate);
	}

	@Test
	public void givenMultivalueDateMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withADateMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList(zeUltimateDate, anotherDate))));

		assertThat(zeSchema.dateMetadata().getDefaultValue()).isEqualTo(asList(zeUltimateDate, anotherDate));
		assertThat(newZeSchemaRecord().<List<LocalDate>>get(zeSchema.dateMetadata())).isEqualTo(asList(zeUltimateDate, anotherDate));
	}

	@Test
	public void givenDateTimeMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withADateTimeMetadata(whichHasDefaultValue(shishOClock)));

		assertThat(zeSchema.dateTimeMetadata().getDefaultValue()).isEqualTo(shishOClock);
		assertThat(newZeSchemaRecord().<LocalDateTime>get(zeSchema.dateTimeMetadata())).isEqualTo(shishOClock);
	}

	@Test
	public void givenMultivalueDateTimeMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withADateTimeMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList(shishOClock, tockOClock))));

		assertThat(zeSchema.dateTimeMetadata().getDefaultValue()).isEqualTo(asList(shishOClock, tockOClock));
		assertThat(newZeSchemaRecord().<List<LocalDateTime>>get(zeSchema.dateTimeMetadata())).isEqualTo(asList(shishOClock, tockOClock));
	}

	@Test
	public void givenBooleanMetadataWithFalseDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultValue(false)));

		assertThat(zeSchema.booleanMetadata().getDefaultValue()).isEqualTo(Boolean.FALSE);
		assertThat(newZeSchemaRecord().<Boolean>get(zeSchema.booleanMetadata())).isEqualTo(Boolean.FALSE);
	}

	@Test
	public void givenBooleanMetadataWithTrueDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultValue(true)));

		assertThat(zeSchema.booleanMetadata().getDefaultValue()).isEqualTo(Boolean.TRUE);
		assertThat(newZeSchemaRecord().<Boolean>get(zeSchema.booleanMetadata())).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void givenBooleanMetadataWithNullDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultValue(null)));

		assertThat(zeSchema.booleanMetadata().getDefaultValue()).isNull();
		assertThat(newZeSchemaRecord().<Boolean>get(zeSchema.booleanMetadata())).isNull();
	}

	@Test
	public void givenMultivalueBooleanMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList(true, false, true))));

		assertThat(zeSchema.booleanMetadata().getDefaultValue()).isEqualTo(asList(true, false, true));
		assertThat(newZeSchemaRecord().<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false, true));
	}

	//@Test
	public void givenIntegerMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAnIntegerMetadata(whichHasDefaultValue(12)));

		assertThat(zeSchema.integerMetadata().getDefaultValue()).isEqualTo(12);
		assertThat(newZeSchemaRecord().<Integer>get(zeSchema.integerMetadata())).isEqualTo(12);

	}

	//@Test
	public void givenMultivalueIntegerMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAnIntegerMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList(12, 34, 56))));

		assertThat(zeSchema.integerMetadata().getDefaultValue()).isEqualTo(asList(12, 34, 56));
		assertThat(newZeSchemaRecord().<List<Integer>>get(zeSchema.integerMetadata())).isEqualTo(asList(12, 34, 56));
	}

	@Test
	public void givenNumberMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withANumberMetadata(whichHasDefaultValue(12.34)));

		assertThat(zeSchema.numberMetadata().getDefaultValue()).isEqualTo(12.34);
		assertThat(newZeSchemaRecord().<Double>get(zeSchema.numberMetadata())).isEqualTo(12.34);
	}

	@Test
	public void givenNumberMetadataWithZeroDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withANumberMetadata(whichHasDefaultValue(0.0)));

		assertThat(zeSchema.numberMetadata().getDefaultValue()).isEqualTo(0.0);
		assertThat(newZeSchemaRecord().<Double>get(zeSchema.numberMetadata())).isEqualTo(0.0);
		assertThat(newZeSchemaRecord().isModified(zeSchema.numberMetadata())).isTrue();
	}

	@Test
	public void givenMultivalueNumberMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withANumberMetadata(whichIsMultivalue,
				whichHasDefaultValue(asList(12.34, 34.56, 56.78))));

		assertThat(zeSchema.numberMetadata().getDefaultValue()).isEqualTo(asList(12.34, 34.56, 56.78));
		assertThat(newZeSchemaRecord().<List<Double>>get(zeSchema.numberMetadata())).isEqualTo(asList(12.34, 34.56, 56.78));
	}

	@Test
	public void givenEnumMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAnEnumMetadata(AValidEnum.class, whichHasDefaultValue(AValidEnum.SECOND_VALUE)));

		assertThat(zeSchema.enumMetadata().getDefaultValue()).isEqualTo(AValidEnum.SECOND_VALUE);
		assertThat(newZeSchemaRecord().<Object>get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.SECOND_VALUE);
	}

	@Test
	public void givenMultivalueEnumMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue,
				whichHasDefaultValue(asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE))));

		assertThat(zeSchema.enumMetadata().getDefaultValue())
				.isEqualTo(asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));
		assertThat(newZeSchemaRecord().<Object>get(zeSchema.enumMetadata()))
				.isEqualTo(asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));
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
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "zeTaxo");

		Map<Language, String> labelAbv = new HashMap<>();
		labelTitle.put(Language.French, "ze");

		Taxonomy taxonomy = new Taxonomy("zeTaxo", labelTitle, labelAbv, zeCollection, zeSchema.typeCode());
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
