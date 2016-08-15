package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.services.schemas.bulkImport.RecordsImportServices.INVALID_SCHEMA_TYPE_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.AUTOMATIC_METADATA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.DISABLED_METADATA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_DATETIME_VALUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_MULTIVALUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_RESOLVER_METADATA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_SCHEMA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_STRING_VALUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.LEGACY_ID_LOCAL_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.LEGACY_ID_NOT_UNIQUE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.REQUIRED_ID;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.SYSTEM_RESERVED_METADATA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.UNRESOLVED_VALUE;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSystemReserved;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.builder.ImportDataBuilder;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.bulkImport.ProgressionHandler;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordsImportServicesRealTest extends ConstellioTest {

	LocalDate aDate = new LocalDate();
	LocalDateTime aDateTime = new LocalDateTime();

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ZeCustomSchemaMetadatas zeCustomSchemaMetadatas = schemas.new ZeCustomSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();

	List<ImportDataBuilder> zeSchemaTypeRecords = new ArrayList<>();
	List<ImportDataBuilder> anotherSchemaTypeRecords = new ArrayList<>();
	List<ImportDataBuilder> thirdSchemaTypeRecords = new ArrayList<>();
	Map<String, List<ImportDataBuilder>> data = new HashMap<>();

	ContentManager contentManager;
	ImportDataProvider importDataProvider;
	RecordsImportServices services;

	ModelLayerExtensions extensions;

	RecordServices recordServices;

	SearchServices searchServices;

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

	User admin;

	RecordImportExtension firstImportBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void validate(ValidationParams params) {
			Object stringMetadataObjectValue = params.getImportRecord().getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("z")) {
					params.getErrors().add(RecordsImportServices.class, "noZ", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void prevalidate(PrevalidationParams params) {
			Object stringMetadataObjectValue = params.getImportRecord().getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("toto")) {
					params.getErrors().add(RecordsImportServices.class, "noToto", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void build(BuildParams buildParams) {
			Map<String, String> structureFields = (Map<String, String>) buildParams.getImportRecord().getFields().get(
					"structureMetadata");

			if (structureFields != null && structureFields.containsKey("zeTitle")) {
				buildParams.getRecord().set(TITLE, structureFields.get("zeTitle"));
			}
		}
	};
	RecordImportExtension secondImportBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void prevalidate(PrevalidationParams params) {
			Object stringMetadataObjectValue = params.getImportRecord().getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("tata")) {
					params.getErrors().add(RecordsImportServices.class, "noTata", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void validate(ValidationParams params) {
			Object stringMetadataObjectValue = params.getImportRecord().getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("y")) {
					params.getErrors().add(RecordsImportServices.class, "noY", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void build(BuildParams params) {
		}
	};

	RecordImportExtension otherTypeBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "anotherInexistentType";
		}

		@Override
		public void prevalidate(PrevalidationParams params) {
			params.getErrors().add(RecordsImportServices.class, "boom", asMap("zevalue", "boom boom"));
		}

		@Override
		public void validate(ValidationParams params) {
			params.getErrors().add(RecordsImportServices.class, "boom", asMap("zevalue", "boom boom"));
		}

		@Override
		public void build(BuildParams params) {
			throw new Error("Should not be called");
		}
	};

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(BASE64_URL_ENCODED);
		prepareSystem(
				withZeCollection().withAllTestUsers()
		);
		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		data.put(zeSchema.typeCode(), zeSchemaTypeRecords);
		data.put(anotherSchema.typeCode(), anotherSchemaTypeRecords);
		data.put(thirdSchema.typeCode(), thirdSchemaTypeRecords);
		importDataProvider = new DummyImportDataProvider(data);

		contentManager = getModelLayerFactory().getContentManager();
		services = new RecordsImportServices(getModelLayerFactory());

		extensions = getModelLayerFactory().getExtensions();
		extensions.forCollection(zeCollection).recordImportExtensions.add(firstImportBehavior);
		extensions.forCollection(zeCollection).recordImportExtensions.add(secondImportBehavior);
		extensions.forCollection(zeCollection).recordImportExtensions.add(otherTypeBehavior);
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test
	public void whenUpdatingPreviouslyAddedRecordThenUpdated()
			throws Exception {

		LocalDate aDate = new LocalDate().minusDays(1);
		LocalDate anotherDate = new LocalDate().minusDays(2);
		LocalDateTime aDateTime = new LocalDateTime().minusDays(1);
		LocalDateTime anotherDateTime = new LocalDateTime().minusDays(2);

		defineSchemasManager().using(schemas.andCustomSchema()
				.withABooleanMetadata()
				.withADateMetadata()
				.withADateTimeMetadata()
				.withANumberMetadata()
				.withAnEnumMetadata(AValidEnum.class));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "yes")
				.addField(zeSchema.dateMetadata().getLocalCode(), aDate)
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), aDateTime)
				.addField(zeSchema.numberMetadata().getLocalCode(), "6.66")
				.addField(zeSchema.enumMetadata().getLocalCode(), "S"));
		bulkImport(importDataProvider, progressionListener, admin);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "new title 2")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "no")
				.addField(zeSchema.dateMetadata().getLocalCode(), anotherDate)
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), anotherDateTime)
				.addField(zeSchema.numberMetadata().getLocalCode(), "7.77")
				.addField(zeSchema.enumMetadata().getLocalCode(), "F"));
		bulkImport(importDataProvider, progressionListener, admin);
		Record record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("new title 2");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isFalse();
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(anotherDate);
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(anotherDateTime);
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(7.77);
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.FIRST_VALUE);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "new title 3"));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("new title 3");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isFalse();
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(anotherDate);
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(anotherDateTime);
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(7.77);
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.FIRST_VALUE);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), null)
				.addField(zeSchema.dateMetadata().getLocalCode(), null)
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), null)
				.addField(zeSchema.numberMetadata().getLocalCode(), null)
				.addField(zeSchema.enumMetadata().getLocalCode(), null));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("new title 3");
		assertThat(record.get(zeSchema.booleanMetadata())).isNull();
		assertThat(record.get(zeSchema.dateMetadata())).isNull();
		assertThat(record.get(zeSchema.dateTimeMetadata())).isNull();

		//assertThat(record.get(zeSchema.numberMetadata())).isNull();
		assertThat(record.get(zeSchema.enumMetadata())).isNull();

	}

	@Test
	public void whenImportingRecordsWithVariousSingleValueTypesThenImportedCorrectly()
			throws Exception {

		LocalDate aDate = new LocalDate().minusDays(1);
		LocalDateTime aDateTime = new LocalDateTime().minusDays(1);

		defineSchemasManager().using(schemas.andCustomSchema()
				.withABooleanMetadata()
				.withADateMetadata()
				.withADateTimeMetadata()
				.withANumberMetadata()
				.withAnEnumMetadata(AValidEnum.class));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "yes")
				.addField(zeSchema.dateMetadata().getLocalCode(), aDate)
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), aDateTime)
				.addField(zeSchema.numberMetadata().getLocalCode(), "6.66")
				.addField(zeSchema.enumMetadata().getLocalCode(), "S"));

		bulkImport(importDataProvider, progressionListener, admin);

		Record record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("Record 1");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isTrue();
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(aDate);
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(aDateTime);
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(6.66);
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.SECOND_VALUE);

	}

	@Test
	public void whenImportingRecordsWithVariousMultiValueTypesThenImportedCorrectly()
			throws Exception {

		LocalDate aDate = new LocalDate().minusDays(1);
		LocalDate anotherDate = new LocalDate().minusDays(2);
		LocalDateTime aDateTime = new LocalDateTime().minusDays(1);
		LocalDateTime anotherDateTime = new LocalDateTime().minusDays(2);

		defineSchemasManager().using(schemas.andCustomSchema()
				.withABooleanMetadata(whichIsMultivalue)
				.withADateMetadata(whichIsMultivalue)
				.withADateTimeMetadata(whichIsMultivalue)
				.withANumberMetadata(whichIsMultivalue)
				.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), asList("yes", "FALSE"))
				.addField(zeSchema.dateMetadata().getLocalCode(), asList(aDate, anotherDate))
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), asList(anotherDateTime, aDateTime))
				.addField(zeSchema.numberMetadata().getLocalCode(), asList("6.66", "42.0"))
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));

		bulkImport(importDataProvider, progressionListener, admin);

		Record record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("Record 1");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

	}

	@Test
	public void whenUpdatingARecordSchemaThenModified()
			throws Exception {

		LocalDate aDate = new LocalDate().minusDays(1);
		LocalDate anotherDate = new LocalDate().minusDays(2);
		LocalDateTime aDateTime = new LocalDateTime().minusDays(1);
		LocalDateTime anotherDateTime = new LocalDateTime().minusDays(2);

		defineSchemasManager().using(schemas.andCustomSchema()
				.withABooleanMetadata(whichIsMultivalue)
				.withADateMetadata(whichIsMultivalue)
				.withADateTimeMetadata(whichIsMultivalue)
				.withANumberMetadata(whichIsMultivalue)
				.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue)
				.withAStringMetadataInCustomSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").setSchema("default").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), asList("yes", "FALSE"))
				.addField(zeSchema.dateMetadata().getLocalCode(), asList(aDate, anotherDate))
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), asList(anotherDateTime, aDateTime))
				.addField(zeSchema.numberMetadata().getLocalCode(), asList("6.66", "42.0"))
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));

		bulkImport(importDataProvider, progressionListener, admin);
		Record record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

		zeSchemaTypeRecords.clear();
		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").setSchema("custom").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), asList("yes", "FALSE"))
				.addField(zeSchema.dateMetadata().getLocalCode(), asList(aDate, anotherDate))
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), asList(anotherDateTime, aDateTime))
				.addField(zeSchema.numberMetadata().getLocalCode(), asList("6.66", "42.0"))
				.addField(zeCustomSchemaMetadatas.customStringMetadata().getLocalCode(), "customMetadataValue")
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_custom");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.get(zeCustomSchemaMetadatas.customStringMetadata())).isEqualTo("customMetadataValue");
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

		zeSchemaTypeRecords.clear();
		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").setSchema("default").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), asList("yes", "FALSE"))
				.addField(zeSchema.dateMetadata().getLocalCode(), asList(aDate, anotherDate))
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), asList(anotherDateTime, aDateTime))
				.addField(zeSchema.numberMetadata().getLocalCode(), asList("6.66", "42.0"))
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.get(zeCustomSchemaMetadatas.customStringMetadata())).isNull();
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));
	}

	@Test
	public void whenImportingRecordsWithValidValueInBooleanMetadataThenCorrectlySaved()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withABooleanMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "yes"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "true"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "oui"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "vrai"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("7").addField("title", "Record 7")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "YES"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("8").addField("title", "Record 8")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "TRuE"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("9").addField("title", "Record 9")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "OuI"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("10").addField("title", "Record 10")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "vRai"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 11")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "O"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 12")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "Y"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 13")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "T"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("14").addField("title", "Record 14")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "no"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("15").addField("title", "Record 15")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "false"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("16").addField("title", "Record 16")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "non"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("17").addField("title", "Record 17")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "faux"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("18").addField("title", "Record 18")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "N"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("19").addField("title", "Record 19")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "F"));

		bulkImport(importDataProvider, progressionListener, admin);

		for (int i = 3; i <= 13; i++) {
			Record record = recordWithLegacyId("" + i);
			assertThat(record).describedAs("Record " + i + " should exist").isNotNull();
			assertThat(record.get(LEGACY_ID)).isEqualTo("" + i);
			assertThat(record.get(TITLE)).isEqualTo("Record " + i);
			assertThat((Boolean) record.get(zeSchema.booleanMetadata())).describedAs("Record " + i + " should be true")
					.isTrue();
		}

		for (int i = 14; i <= 19; i++) {
			Record record = recordWithLegacyId("" + i);
			assertThat(record).describedAs("Record " + i + " should exist").isNotNull();
			assertThat(record.get(LEGACY_ID)).isEqualTo("" + i);
			assertThat(record.get(TITLE)).isEqualTo("Record " + i);
			assertThat((Boolean) record.get(zeSchema.booleanMetadata())).describedAs("Record " + i + " should be false")
					.isFalse();
		}
	}

	@Test
	public void givenADataProviderReturnAListOfSchemaTypesWithInvalidSchemaTypesThenValidationErrors()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		ImportDataProvider importDataProvider = new ImportDataProvider() {
			@Override
			public void initialize() {
			}

			@Override
			public void close() {
			}

			@Override
			public List<String> getAvailableSchemaTypes() {
				return Arrays.asList(zeSchema.typeCode(), "chuckNorris", anotherSchema.typeCode() + "s");
			}

			@Override
			public ImportDataIterator newDataIterator(String schemaType) {
				return null;
			}

			@Override
			public int size(String schemaType) {
				return 0;
			}
		};

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newValidationError(INVALID_SCHEMA_TYPE_CODE, asMap("schemaType", "chuckNorris")),
					newValidationError(INVALID_SCHEMA_TYPE_CODE, asMap("schemaType", anotherSchema.typeCode() + "s")));
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportDataHasANullIdThenValidationException()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(null).addField("title", "Record 3"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(null).addField("title", "Record 5"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredId", "2", "zeSchemaType : "),
					tuple("RecordsImportServices_requiredId", "4", "zeSchemaType : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("parentReferenceFromZeSchemaToZeSchema", "42"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("parentReferenceFromZeSchemaToZeSchema", "666"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "unresolvedValues", "metadata", "metadataLabel"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42, 666", "legacyIdentifier",
									"legacyIdentifier")
					);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordUsingLegacyIdResolverThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:42"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:666"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "unresolvedValues", "metadata", "metadataLabel"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42, 666", "legacyIdentifier",
									"legacyIdentifier")
					);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordUsingOtherUniqueMetadataResolverThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("stringMetadata", "code2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("stringMetadata", "code3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("stringMetadata", "code4")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:42"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("stringMetadata", "code5")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:666"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "unresolvedValues", "metadata", "metadataLabel"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42, 666", "stringMetadata",
									"A toAString metadata")
					);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingUsingAnInvalidResolverMetadataCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("stringMetadata", "code2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("stringMetadata", "code3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("stringMetadata", "code4")
				.addField("parentReferenceFromZeSchemaToZeSchema", "invalidMetadata:code2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("stringMetadata", "code5")
				.addField("parentReferenceFromZeSchemaToZeSchema", "otherInvalidMetadata:666"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "resolverMetadata", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidResolverMetadataCode", "parentReferenceFromZeSchemaToZeSchema",
							"invalidMetadata", "zeSchemaType 4 : "),
					tuple("RecordsImportServices_invalidResolverMetadataCode", "parentReferenceFromZeSchemaToZeSchema",
							"otherInvalidMetadata", "zeSchemaType 5 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasMissingValuesToFieldsThatAreAlwaysEnabledThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata(whichIsMultivalue, whichHasDefaultRequirement)
				.withABooleanMetadata(whichHasDefaultRequirement));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("stringMetadata", asList("42"))
				.addField("booleanMetadata", "true"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("stringMetadata", asList("42")));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("booleanMetadata", "true"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("stringMetadata", new ArrayList<>()));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();

			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "metadataLabel", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata", "zeSchemaType 3 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata", "zeSchemaType 4 : "),
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata", "zeSchemaType 5 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata", "zeSchemaType 5 : ")
			);

			//			assertThat(errors).containsOnly(
			//					newZeSchemaValidationError(REQUIRED_VALUE,
			//							asMap("index", "2", "legacyId", "3", "metadatas", "[booleanMetadata]")),
			//					newZeSchemaValidationError(REQUIRED_VALUE,
			//							asMap("index", "3", "legacyId", "4", "metadatas", "[stringMetadata]")),
			//					newZeSchemaValidationError(REQUIRED_VALUE,
			//							asMap("index", "4", "legacyId", "5", "metadatas", "[booleanMetadata, stringMetadata]")));
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInBooleanMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withABooleanMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "Oui monsieur"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.booleanMetadata().getLocalCode(), "Oh yes"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidBooleanValue", "A boolean metadata", "Oui monsieur", "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidBooleanValue", "A boolean metadata", "Oh yes", "zeSchemaType 6 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidEnumMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAnEnumMetadata(AValidEnum.class));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.enumMetadata().getLocalCode(), "SECOND_VALUE"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.enumMetadata().getLocalCode(), "S"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.enumMetadata().getLocalCode(), "FS"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "availableChoices", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidEnumValue", "withAnEnumMetadata", "SECOND_VALUE", "F, S",
							"zeSchemaType 3 : "),
					tuple("RecordsImportServices_invalidEnumValue", "withAnEnumMetadata", "FS", "F, S", "zeSchemaType 5 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidMultivalueEnumMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("S", "SECOND_VALUE")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("FS", "F")));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "legacyId", "availableChoices", "value",
					"metadataLabel")).containsOnly(
					tuple("RecordsImportServices_invalidEnumValue", "3", "5", "F, S", "FS", "withAnEnumMetadata"),
					tuple("RecordsImportServices_invalidEnumValue", "1", "3", "F, S", "SECOND_VALUE", "withAnEnumMetadata")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"zeSchemaType 3 : La valeur «SECOND_VALUE» de la métadonnée «withAnEnumMetadata» est invalide, seules les valeurs «F, S» sont acceptées",
					"zeSchemaType 5 : La valeur «FS» de la métadonnée «withAnEnumMetadata» est invalide, seules les valeurs «F, S» sont acceptées"
			);
		}
	}

	@Test
	@Ignore
	public void givenAnImportedRecordHasAValueToADisabledFieldThenValidationError()
			throws Exception {
		// We want to be able to import values in disabled fields
		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsDisabled));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(DISABLED_METADATA_CODE,
							asMap("index", "2", "legacyId", "3", "metadata", "stringMetadata")));
			assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	@Ignore
	public void givenAnImportedRecordHasAValueToASystemReservedFieldThenValidationError()
			throws Exception {
		// We import as the system. We are supposed to be able to set system-reserved fields
		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsSystemReserved));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(SYSTEM_RESERVED_METADATA_CODE,
							asMap("index", "2", "legacyId", "3", "metadata", "stringMetadata")));
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasAValueToAnAutomaticFieldThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.stringCopiedFromFirstReferenceStringMeta().getLocalCode(), "value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "metadata", "metadataLabel")).containsOnly(
					tuple("RecordsImportServices_automaticMetadataCode", "zeSchemaType", "copiedStringMeta",
							"Une métadonnée copiée")
			);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInNumberMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withANumberMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.numberMetadata().getLocalCode(), "1.0"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.numberMetadata().getLocalCode(), "5L"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.numberMetadata().getLocalCode(), "5.0t"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("7").addField("title", "Record 7")
				.addField(zeSchema.numberMetadata().getLocalCode(), "nan"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "5L", "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "5.0t", "zeSchemaType 6 : "),
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "nan", "zeSchemaType 7 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInStringMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "1.0"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), aDate));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getLocalCode(), aDateTime));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("7").addField("title", "Record 7")
				.addField(zeSchema.stringMetadata().getLocalCode(), "Ze value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidStringValue", "A toAString metadata", aDate.toString(),
							"zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidStringValue", "A toAString metadata", aDateTime.toString(),
							"zeSchemaType 6 : ")
			);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInMultivalueStringMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata(whichIsMultivalue));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList("1.0")));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList("validValue", aDate)));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList(aDate, aDateTime)));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("7").addField("title", "Record 7")
				.addField(zeSchema.stringMetadata().getLocalCode(), new ArrayList<>()));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDate.toString(), "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDate.toString(), "zeSchemaType 6 : "),
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDateTime.toString(), "zeSchemaType 6 : ")
			);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInDateMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withADateMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.dateMetadata().getLocalCode(), aDate));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.dateMetadata().getLocalCode(), aDateTime));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.dateMetadata().getLocalCode(), "a text value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidDateValue", "a date metadata", aDateTime.toString(), "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidDateValue", "a date metadata", "a text value", "zeSchemaType 6 : ")
			);

			//			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			//			assertThat(errors).containsOnly(
			//					newZeSchemaValidationError(INVALID_DATE_VALUE,
			//							asMap("index", "2", "legacyId", "5", "invalidValue", aDateTime.toString(), "metadata",
			//									"dateMetadata")),
			//					newZeSchemaValidationError(INVALID_DATE_VALUE,
			//							asMap("index", "3", "legacyId", "6", "invalidValue", "a text value", "metadata",
			//									"dateMetadata")));
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInDateTimeMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withADateTimeMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), aDate));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), aDateTime));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), "a text value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidDatetimeValue", "a date time metadata", aDate.toString(),
							"zeSchemaType 4 : "),
					tuple("RecordsImportServices_invalidDatetimeValue", "a date time metadata", "a text value",
							"zeSchemaType 6 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasSingleValueInMultiValueMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata(whichIsMultivalue));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList("zeValue", "anotherValue")));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), "aValue"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getLocalCode(), "anotherValue"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidMultivalue", "A toAString metadata", "aValue", "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidMultivalue", "A toAString metadata", "anotherValue", "zeSchemaType 6 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasMultiValueInSinglevalueMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList("aValue")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), "goodValue"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getLocalCode(), asList("anotherValue", "thirdValue")));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidSinglevalue", "stringMetadata", "[aValue]", "zeSchemaType 4 : "),
					tuple("RecordsImportServices_invalidSinglevalue", "stringMetadata", "[anotherValue, thirdValue]",
							"zeSchemaType 6 : ")
			);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidMetadataCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "aValue"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("zeChuckNorrisMetadata", "zeValue"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getCode(), "ze value")
				.addField("anInexistentMetadata", "ze value")
				.addField("anotherInexistentMetadata", "ze value"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();

			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidMetadataCode", "zeChuckNorrisMetadata", "zeSchemaType 5 : "),
					tuple("RecordsImportServices_invalidMetadataCode", "anInexistentMetadata", "zeSchemaType 6 : "),
					tuple("RecordsImportServices_invalidMetadataCode", "anotherInexistentMetadata", "zeSchemaType 6 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidSchemaCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("anInvalidSchema").setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "aValue"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("default").setId("5").addField("title", "Record 5"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("zeSchemaType_custom").setId("6").addField("title", "Record 6")
				.addField(zeSchema.stringMetadata().getCode(), "ze value"));

		zeSchemaTypeRecords
				.add(defaultSchemaData().setSchema("zeSchemaType_default").setId("7").addField("title", "Record 7")
						.addField(zeSchema.stringMetadata().getLocalCode(), "aValue"));

		zeSchemaTypeRecords
				.add(defaultSchemaData().setSchema("anotherSchemaType_default").setId("8").addField("title", "Record 7")
						.addField(zeSchema.stringMetadata().getLocalCode(), "aValue"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "schema", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidSchemaCode", "zeSchemaType", "anInvalidSchema", "zeSchemaType 4 : "),
					tuple("RecordsImportServices_invalidSchemaCode", "zeSchemaType", "anotherSchemaType_default",
							"zeSchemaType 8 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void givenCyclicDependencyInRecordsThenExceptionDuringImport()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "4"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("parentReferenceFromZeSchemaToZeSchema", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("parentReferenceFromZeSchemaToZeSchema", "6"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (RecordsImportServicesRuntimeException.RecordsImportServicesRuntimeException_CyclicDependency e) {
			assertThat(e.getCyclicDependentIds()).containsOnly("1", "2", "3", "4");
			fail("TODO Better message");
		}
	}

	@Test
	public void whenValidatingThenBuildAMappingOfLegacyId()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "previouslySavedRecordLegacyId"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("43").addField("title", "Record 43"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "1"));

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		ResolverCache resolver = new ResolverCache(getModelLayerFactory().newRecordServices(),
				getModelLayerFactory().newSearchServices(), types, importDataProvider);

		ProgressionHandler progressionHandler = new ProgressionHandler(new LoggerBulkImportProgressionListener());
		ModelLayerCollectionExtensions extensions = getModelLayerFactory().getExtensions()
				.forCollection(zeCollection);
		services.validate(importDataProvider, progressionHandler, admin, types, resolver, extensions, Language.French);

		assertThat(resolver.cache).hasSize(2).containsKey(zeSchema.typeCode()).containsKey(anotherSchema.typeCode());
		assertThat(resolver.getSchemaTypeCache(zeSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping)
				.hasSize(1).containsEntry("previouslySavedRecordLegacyId", "previouslySavedRecordId");
		assertThat(resolver.getSchemaTypeCache(zeSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedLegacyIds).isEmpty();

		assertThat(resolver.getSchemaTypeCache(anotherSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping).isEmpty();
		assertThat(resolver.getSchemaTypeCache(anotherSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedLegacyIds).isEmpty();

		assertThat(resolver.getSchemaTypeCache(thirdSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping).isEmpty();
		assertThat(resolver.getSchemaTypeCache(thirdSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedLegacyIds).isEmpty();
	}

	@Test
	public void givenTwoImportedRecordHaveSameLegacyInTwoDifferentSchemaTypeThenOK()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));
		anotherSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Ze record"));

		bulkImport(importDataProvider, progressionListener, admin);
	}

	@Test
	public void givenTwoImportedRecordHaveSameLegacyInSameSchemaTypeThenValidationException()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 2"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_legacyIdNotUnique", "42", "zeSchemaType 42 : ")
			);

			//			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			//			assertThat(errors).containsOnly(newZeSchemaValidationError(LEGACY_ID_NOT_UNIQUE, asMap("legacyId", "42")));
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}

	}

	@Test
	public void givenTwoImportedRecordHaveSameUniqueMetadataInSameSchemaTypeThenValidationException()
			throws Exception {

		defineSchemasManager()
				.using(schemas.andCustomSchema().withAStringMetadata(whichIsUnique).withAnotherSchemaStringMetadata());

		anotherSchemaTypeRecords
				.add(defaultSchemaData().setId("41").addField("title", "Record 1").addField("stringMetadata", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 1").addField("stringMetadata", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("43").addField("title", "Record 2").addField("stringMetadata", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("44").addField("title", "Record 3").addField("stringMetadata", "42"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("45").addField("title", "Record 2").addField("stringMetadata", "v1"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "metadataLabel", "value", "prefix")).containsOnly(
					tuple("RecordsImportServices_metadataNotUnique", "stringMetadata", "A toAString metadata", "v1",
							"zeSchemaType 43 : "),
					tuple("RecordsImportServices_metadataNotUnique", "stringMetadata", "A toAString metadata", "v1",
							"zeSchemaType 45 : ")
			);
			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");

		}

	}

	@Test
	public void givenAnImportedRecordHaveSameUniqueMetadataThanOtherExistingRecordThenException()
			throws Exception {

		defineSchemasManager()
				.using(schemas.andCustomSchema().withAStringMetadata(whichIsUnique).withAnotherSchemaStringMetadata());

		recordServices
				.add(new TestRecord(anotherSchema).set(TITLE, "existing record Z").set(anotherSchema.stringMetadata(), "v3"));
		recordServices.add(new TestRecord(zeSchema).set(TITLE, "existing record A").set(zeSchema.stringMetadata(), "v1"));
		recordServices.add(new TestRecord(zeSchema).set(TITLE, "existing record B").set(zeSchema.stringMetadata(), "v2"));
		recordServices.add(new TestRecord(zeSchema).set(TITLE, "existing record C").set(zeSchema.stringMetadata(), "v4"));

		anotherSchemaTypeRecords
				.add(defaultSchemaData().setId("41").addField("title", "Record 1").addField("stringMetadata", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 1").addField("stringMetadata", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("43").addField("title", "Record 2").addField("stringMetadata", "v2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("44").addField("title", "Record 3").addField("stringMetadata", "v3"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "metadataCode", "index", "legacyId", "value")).containsOnly(
					tuple("MetadataUniqueValidator_nonUniqueMetadata", "zeSchemaType_default_stringMetadata", "1", "42", "v1"),
					tuple("MetadataUniqueValidator_nonUniqueMetadata", "zeSchemaType_default_stringMetadata", "2", "43", "v2")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"zeSchemaType 42 : La métadonnée «A toAString metadata» doit avoir une valeur unique",
					"zeSchemaType 43 : La métadonnée «A toAString metadata» doit avoir une valeur unique"
			);
		}

	}

	@Test
	public void whenImportingRecordsWithInvalidContentThenImportRecordsAndReturnWarnings()
			throws Exception {
		String testResource1 = getTestResourceFile("resource1.docx").getAbsolutePath().replace(".docx", ".dodocx");
		String testResource2 = getTestResourceFile("resource2.pdf").getAbsolutePath();
		String testResource3 = "http://www.perdu.com/edouardLechat.pdf";
		String testResource2Hash = "KN8RjbrnBgq1EDDV2U71a6_6gd4=";

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new ContentImport(testResource1, "Ze document.docx", true, null, null)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new ContentImport(testResource2, "Ze ultimate document.pdf", false, null, null),
						new ContentImport(testResource3, "Ze book.txt", true, null, null))));

		BulkImportResults results = bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("1");
		Record record2 = recordWithLegacyId("2");
		Record record3 = recordWithLegacyId("3");

		Content record1Content = record1.get(zeSchema.contentMetadata());
		List<Content> record1ContentList = record1.get(zeSchema.contentListMetadata());

		Content record2Content = record2.get(zeSchema.contentMetadata());
		List<Content> record2ContentList = record2.get(zeSchema.contentListMetadata());

		Content record3Content = record3.get(zeSchema.contentMetadata());
		List<Content> record3ContentList = record3.get(zeSchema.contentListMetadata());

		assertThat(record1Content).isNull();
		assertThat(record1ContentList).hasSize(0);

		assertThat(record2Content).isNull();
		assertThat(record2ContentList).hasSize(0);

		assertThat(record3Content).isNull();
		assertThat(record3ContentList).hasSize(1);
		assertThat(record3ContentList.get(0).getCurrentVersion().getHash()).isEqualTo(testResource2Hash);
		assertThat(record3ContentList.get(0).getCurrentVersion().getFilename()).isEqualTo("Ze ultimate document.pdf");
		assertThat(record3ContentList.get(0).getCurrentVersion().getVersion()).isEqualTo("0.1");

		assertThat(contentManager.getParsedContent(testResource2Hash).getParsedContent()).contains("Gestion des documents");

		assertThat(results.getInvalidIds()).containsOnly(testResource1, testResource3);
	}

	@Test
	@InternetTest
	public void whenImportingRecordsWithContentThenContentUploadedAndAddedToRecord()
			throws Exception {
		String testResource1 = getTestResourceFile("resource1.docx").getAbsolutePath();
		String testResource2 = getTestResourceFile("resource2.pdf").getAbsolutePath();
		String testResource3 = "https://dl.dropboxusercontent.com/u/422508/pg338.txt";
		String testResource4 = getTestResourceFile("resource4.docx").getAbsolutePath();
		String testResource5 = getTestResourceFile("resource5.pdf").getAbsolutePath();
		String testResource1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
		String testResource2Hash = "KN8RjbrnBgq1EDDV2U71a6_6gd4=";
		String testResource3Hash = "jLWaqQbCOSAPT4G3P75XnJJOmmo=";
		String testResource4Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";
		String testResource5Hash = "T-4zq4cGP_tXkdJp_qz1WVWYhoQ=";

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		ContentImportVersion version1 = new ContentImportVersion(testResource1, "Ze document.docx", true, null, null);
		ContentImportVersion version2 = new ContentImportVersion(testResource4, "Ze document.docx", false, null, null);
		ContentImportVersion version3 = new ContentImportVersion(testResource5, "Ze document.docx", true, null, null);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new ContentImport(Arrays.asList(version1, version2, version3))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new ContentImport(testResource2, "Ze ultimate document.pdf", false, null, null),
						new ContentImport(testResource3, "Ze book.txt", true, null, null))));

		BulkImportResults results = bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("1");
		Record record2 = recordWithLegacyId("2");
		Record record3 = recordWithLegacyId("3");

		Content record1Content = record1.get(zeSchema.contentMetadata());
		List<Content> record1ContentList = record1.get(zeSchema.contentListMetadata());

		Content record2Content = record2.get(zeSchema.contentMetadata());
		List<Content> record2ContentList = record2.get(zeSchema.contentListMetadata());

		Content record3Content = record3.get(zeSchema.contentMetadata());
		List<Content> record3ContentList = record3.get(zeSchema.contentListMetadata());

		assertThat(record1Content.getCurrentVersion().getHash()).isEqualTo(testResource5Hash);
		assertThat(record1Content.getCurrentVersion().getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getCurrentVersion().getVersion()).isEqualTo("2.0");
		assertThat(record1Content.getHistoryVersions()).hasSize(2);

		assertThat(record1Content.getHistoryVersions().get(0).getHash()).isEqualTo(testResource1Hash);
		assertThat(record1Content.getHistoryVersions().get(0).getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getHistoryVersions().get(0).getVersion()).isEqualTo("1.0");

		assertThat(record1Content.getHistoryVersions().get(1).getHash()).isEqualTo(testResource4Hash);
		assertThat(record1Content.getHistoryVersions().get(1).getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getHistoryVersions().get(1).getVersion()).isEqualTo("1.1");

		assertThat(record1ContentList).hasSize(0);

		assertThat(record2Content).isNull();
		assertThat(record2ContentList).hasSize(0);

		assertThat(record3Content).isNull();
		assertThat(record3ContentList).hasSize(2);
		assertThat(record3ContentList.get(0).getCurrentVersion().getHash()).isEqualTo(testResource2Hash);
		assertThat(record3ContentList.get(0).getCurrentVersion().getFilename()).isEqualTo("Ze ultimate document.pdf");
		assertThat(record3ContentList.get(0).getCurrentVersion().getVersion()).isEqualTo("0.1");

		assertThat(record3ContentList.get(1).getCurrentVersion().getHash()).isEqualTo(testResource3Hash);
		assertThat(record3ContentList.get(1).getCurrentVersion().getFilename()).isEqualTo("Ze book.txt");
		assertThat(record3ContentList.get(1).getCurrentVersion().getVersion()).isEqualTo("1.0");

		assertThat(contentManager.isParsed(testResource1Hash)).isFalse();

		assertThat(contentManager.isParsed(testResource2Hash)).isTrue();
		assertThat(contentManager.getParsedContent(testResource2Hash).getParsedContent()).contains("Gestion des documents");

		assertThat(contentManager.isParsed(testResource3Hash)).isTrue();
		assertThat(contentManager.getParsedContent(testResource3Hash).getParsedContent())
				.contains("He is your friend, but his arrow will kill one of your kind! He is a\r\nDakota");//\nDakota");

		assertThat(contentManager.isParsed(testResource4Hash)).isFalse();

		assertThat(contentManager.isParsed(testResource5Hash)).isTrue();
		assertThat(contentManager.getParsedContent(testResource5Hash).getParsedContent())
				.contains("CONSTELLIO");

		assertThat(results.getInvalidIds()).isEmpty();

		//Reimport the records changing the order of versions of record #1

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new ContentImport(Arrays.asList(version3, version2, version1))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new ContentImport(testResource2, "Ze ultimate document.pdf", false, null, null),
						new ContentImport(testResource3, "Ze book.txt", true, null, null))));

		results = bulkImport(importDataProvider, progressionListener, admin);

		record1 = recordWithLegacyId("1");

		record1Content = record1.get(zeSchema.contentMetadata());
		assertThat(record1Content.getCurrentVersion().getHash()).isEqualTo(testResource1Hash);
		assertThat(record1Content.getCurrentVersion().getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getCurrentVersion().getVersion()).isEqualTo("2.0");
		assertThat(record1Content.getHistoryVersions()).hasSize(2);

		assertThat(record1Content.getHistoryVersions().get(0).getHash()).isEqualTo(testResource5Hash);
		assertThat(record1Content.getHistoryVersions().get(0).getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getHistoryVersions().get(0).getVersion()).isEqualTo("1.0");

		assertThat(record1Content.getHistoryVersions().get(1).getHash()).isEqualTo(testResource4Hash);
		assertThat(record1Content.getHistoryVersions().get(1).getFilename()).isEqualTo("Ze document.docx");
		assertThat(record1Content.getHistoryVersions().get(1).getVersion()).isEqualTo("1.1");

	}

	@Test
	public void givenAnImportedRecordIsMissingValueInARequiredCalculatedField()
			throws Exception {

		defineSchemasManager().using(schemas
				.withANumberMetadata()
				.withABooleanMetadata()
				.withAStringMetadata(whichHasDefaultRequirement, whichIsCalculatedFromNumberMetadata()));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("numberMetadata", "42.0"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("numberMetadata", "666.0"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "legacyId", "schemaType", "metadataCode", "metadataLabel",
					"basedOnMetadatas")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", "2", "3", zeSchema.typeCode(),
							"zeSchemaType_default_stringMetadata", asMap("fr", "A toAString metadata"),
							"[numberMetadata, booleanMetadata]"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "3", "4", "zeSchemaType",
							"zeSchemaType_default_stringMetadata", asMap("fr", "A toAString metadata"),
							"[numberMetadata, booleanMetadata]")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"zeSchemaType 3 : Métadonnée «A toAString metadata» requise",
					"zeSchemaType 4 : Métadonnée «A toAString metadata» requise");
		}
	}

	private MetadataBuilderConfigurator whichIsCalculatedFromNumberMetadata() {
		return new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineDataEntry().asCalculated(CalculatorReturningBingoIf42.class);
			}
		};
	}

	public static class CalculatorReturningBingoIf42 implements MetadataValueCalculator<String> {

		LocalDependency<Double> numberDependency = LocalDependency.toANumber("numberMetadata");
		LocalDependency<Boolean> booleanDependency = LocalDependency.toABoolean("booleanMetadata");

		@Override
		public String calculate(CalculatorParameters parameters) {
			Double number = parameters.get(numberDependency);
			return new Double(42.0).equals(number) ? "Bingo!" : null;
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(numberDependency, booleanDependency);
		}
	}

	@Test
	public void whenImportingRecordsWithReferencesInsideSameSchemaThenIterateMultipleTimeOverSameFile()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "previouslySavedRecordLegacyId"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("custom").setId("43").addField("title", "Record 43")
				.addField("customString", "customFieldValue"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "1"));

		bulkImport(importDataProvider, progressionListener, admin);

		validateCorrectlyImported();

	}

	@Test
	public void whenImportingUsingLegacyIdResolversThenImportedCorrectly()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "legacyIdentifier:previouslySavedRecordLegacyId"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("custom").setId("43").addField("title", "Record 43")
				.addField("customString", "customFieldValue"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "legacyIdentifier:1"));

		bulkImport(importDataProvider, progressionListener, admin);

		validateCorrectlyImported();

	}

	@Test
	public void whenImportingUsingUniqueMetadataResolversThenImportedCorrectly()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title")
				.set(zeSchema.stringMetadata(), "previouslySavedRecordCode"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code2")
				.addField("stringMetadata", "code1"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code3")
				.addField("stringMetadata", "code2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code42")
				.addField("stringMetadata", "code3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:previouslySavedRecordCode")
				.addField("stringMetadata", "code42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("custom").setId("43").addField("title", "Record 43")
				.addField("customString", "customFieldValue")
				.addField("stringMetadata", "code43"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "stringMetadata:code1"));

		bulkImport(importDataProvider, progressionListener, admin);

		validateCorrectlyImported();

	}

	@Test
	public void whenImportingThenRunDecoratorValidation()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("stringMetadata", "Value with a x"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("stringMetadata", "Value with a y"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("stringMetadata", "Value with a z"));

		try {
			services.bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "zevalue", "legacyId", "index", "prefix")).containsOnly(
					tuple("RecordsImportServices_noY", "Value with a y", "12", "2", "zeSchemaType 12 : ")
			);

			//TODO LANG assertThat(frenchMessages(e)).containsOnly("TODO");
		}
	}

	@Test
	public void whenImportingThenRunDecoratorPrevalidation()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("stringMetadata", "Value with a word"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("stringMetadata", "Value with a tata"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("stringMetadata", "Value with a toto"));

		try {
			services.bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "legacyId", "zevalue", "prefix")).containsOnly(
					tuple("RecordsImportServices_noTata", "2", "12", "Value with a tata", "zeSchemaType 12 : "),
					tuple("RecordsImportServices_noToto", "3", "13", "Value with a toto", "zeSchemaType 13 : ")
			);
		}
	}

	@Test
	public void whenImportingThenRunDecoratorBuild()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema()
				.withAStructureMetadata());

		Map<String, Object> record11Structure = asMap("zeTitle", "pouet");
		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("structureMetadata", record11Structure));

		Map<String, String> record12Structure = null;
		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("structureMetadata", record12Structure));

		Map<String, String> record13Structure = new HashMap<>();
		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("structureMetadata", record13Structure));

		services.bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("11");
		assertThat(record1.get(TITLE)).isEqualTo("pouet");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.get(TITLE)).isEqualTo("Record 2");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.get(TITLE)).isEqualTo("Record 3");
	}

	public static class NoZMetadataValidator implements RecordMetadataValidator<String> {

		@Override
		public void validate(Metadata metadata, String value, ConfigProvider configProvider, ValidationErrors validationErrors) {
			if (value != null && value.contains("p")) {
				validationErrors.add(NoZMetadataValidator.class, "noP");
			}
		}
	}

	@Test
	public void whenImportingWithStopErrorModeThenStopWithoutExecutingCurrentBatch()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").defineValidators()
								.add(NoZMetadataValidator.class);
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin);

			fail("ValidationException expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "zeSchemaType record142 : ")
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(100);
	}

	@Test
	public void whenImportingWithContinueErrorModeThenContinue()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").defineValidators()
								.add(NoZMetadataValidator.class);
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					new BulkImportParams().setStopOnFirstError(false));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "zeSchemaType record142 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "188", "zeSchemaType record188 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "244", "zeSchemaType record244 : ")
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
	}

	private void validateCorrectlyImported() {
		Record record43 = recordWithLegacyId("43");
		Record record42 = recordWithLegacyId("42");
		Record record3 = recordWithLegacyId("3");
		Record record2 = recordWithLegacyId("2");
		Record record1 = recordWithLegacyId("1");

		Record record666 = recordWithLegacyId("666");

		assertThat(record1.getId()).isNotEqualTo("1");
		assertThat(record1.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record1.get(TITLE)).isEqualTo("Record 1");
		assertThat(record1.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record2.getId());

		assertThat(record2.getId()).isNotEqualTo("2");
		assertThat(record2.get(LEGACY_ID)).isEqualTo("2");
		assertThat(record2.get(TITLE)).isEqualTo("Record 2");
		assertThat(record2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record3.getId());

		assertThat(record3.getId()).isNotEqualTo("3");
		assertThat(record3.get(LEGACY_ID)).isEqualTo("3");
		assertThat(record3.get(TITLE)).isEqualTo("Record 3");
		assertThat(record3.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record42.getId());

		assertThat(record42.getId()).isNotEqualTo("42");
		assertThat(record42.get(LEGACY_ID)).isEqualTo("42");
		assertThat(record42.get(TITLE)).isEqualTo("Record 42");
		assertThat(record42.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo("previouslySavedRecordId");

		assertThat(record43.getId()).isNotEqualTo("43");
		assertThat(record43.get(LEGACY_ID)).isEqualTo("43");
		assertThat(record43.get(TITLE)).isEqualTo("Record 43");
		assertThat(record43.get(zeCustomSchemaMetadatas.customStringMetadata())).isEqualTo("customFieldValue");
		assertThat(record43.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isNull();
		assertThat(record43.getSchemaCode()).isEqualTo("zeSchemaType_custom");

		assertThat(record666.getId()).isNotEqualTo("666");
		assertThat(record666.get(LEGACY_ID)).isEqualTo("666");
		assertThat(record666.get(TITLE)).isEqualTo("Ze record");
		assertThat(record666.get(anotherSchema.referenceFromAnotherSchemaToZeSchema())).isEqualTo(record1.getId());
	}

	private ImportDataBuilder defaultSchemaData() {
		return new ImportDataBuilder().setSchema("default");
	}

	private Record recordWithLegacyId(String legacyId) {
		return getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasIn(zeCollection).where(LEGACY_ID).isEqualTo(legacyId));
	}

	private ValidationError newZeSchemaValidationError(String code, Map<String, Object> parameters) {
		parameters.put("schemaType", zeSchema.typeCode());
		return new ValidationError(RecordsImportServices.class, code, parameters);
	}

	private ValidationError newValidationError(String code, Map<String, Object> parameters) {
		return new ValidationError(RecordsImportServices.class, code, parameters);
	}

	private BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user)
			throws ValidationException {
		BulkImportParams params = new BulkImportParams();
		params.setStopOnFirstError(false);
		BulkImportResults results = services.bulkImport(importDataProvider, bulkImportProgressionListener, user, params);

		zeSchemaTypeRecords.clear();
		anotherSchemaTypeRecords.clear();
		thirdSchemaTypeRecords.clear();

		return results;
	}

	private Map<String, Object> asMap(String key1, String value1) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3, String value3) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3, String value3,
			String key4, String value4) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		parameters.put(key4, value4);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3, String value3,
			String key4, String value4, String key5, String value5) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		parameters.put(key4, value4);
		parameters.put(key5, value5);
		return parameters;
	}

}
