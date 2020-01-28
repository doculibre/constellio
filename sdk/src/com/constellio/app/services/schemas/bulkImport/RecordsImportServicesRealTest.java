package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportValidationErrorsBehavior;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.builder.ImportDataBuilder;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.UserSerializedContentFactory;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.RecordValidationServices;
import com.constellio.model.services.records.SimpleImportContent;
import com.constellio.model.services.records.StructureImportContent;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.MaskedMetadataValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.apache.solr.common.params.SolrParams;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.CONTINUE;
import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.CONTINUE_FOR_RECORD_OF_SAME_TYPE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.DISABLED_METADATA_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.LEGACY_ID_LOCAL_CODE;
import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.SYSTEM_RESERVED_METADATA_CODE;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.RecordCacheType.FULLY_CACHED;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.extractingWarningsSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasFixedSequence;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasInputMask;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasSequenceDefinedByMetadata;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultilingual;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsReferencing;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsScripted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSystemReserved;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

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
	DummyImportDataProvider importDataProvider;
	RecordsImportServices services;

	ModelLayerExtensions extensions;

	RecordServices recordServices;

	SearchServices searchServices;

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

	User admin;

	Users users = new Users();

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

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setContentImportThreadFolder(newTempFolder());
				configuration.setDeleteUnusedContentEnabled(false);
			}
		});

		givenHashingEncodingIs(BASE64_URL_ENCODED);
		prepareSystem(
				withZeCollection().withAllTest(users)
		);
		givenConfig(ConstellioEIMConfigs.LEGACY_IDENTIFIER_INDEXED_IN_MEMORY, true);
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
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("new title 2");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isFalse();
		assertThat(record.<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(anotherDate);
		assertThat(record.<LocalDateTime>get(zeSchema.dateTimeMetadata())).isEqualTo(anotherDateTime);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(7.77);
		assertThat(record.<Enum>get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.FIRST_VALUE);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "new title 3"));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("new title 3");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isFalse();
		assertThat(record.<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(anotherDate);
		assertThat(record.<LocalDateTime>get(zeSchema.dateTimeMetadata())).isEqualTo(anotherDateTime);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(7.77);
		assertThat(record.<Enum>get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.FIRST_VALUE);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), null)
				.addField(zeSchema.dateMetadata().getLocalCode(), null)
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), null)
				.addField(zeSchema.numberMetadata().getLocalCode(), null)
				.addField(zeSchema.enumMetadata().getLocalCode(), null));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("new title 3");
		assertThat(record.<Boolean>get(zeSchema.booleanMetadata())).isNull();
		assertThat(record.<LocalDate>get(zeSchema.dateMetadata())).isNull();
		assertThat(record.<LocalDateTime>get(zeSchema.dateTimeMetadata())).isNull();

		//assertThat(record.get(zeSchema.numberMetadata())).isNull();
		assertThat(record.<Enum>get(zeSchema.enumMetadata())).isNull();

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
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isTrue();
		assertThat(record.<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(aDate);
		assertThat(record.<LocalDateTime>get(zeSchema.dateTimeMetadata())).isEqualTo(aDateTime);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(6.66);
		assertThat(record.<Enum>get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.SECOND_VALUE);

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
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.<List<LocalDate>>get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.<List<LocalDateTime>>get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.<List<Double>>get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.<List<Enum>>get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

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
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.<List<LocalDate>>get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.<List<LocalDateTime>>get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.<List<Double>>get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.<List<Enum>>get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

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
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_custom");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.<List<LocalDate>>get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.<List<LocalDateTime>>get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.<List<Double>>get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.<String>get(zeCustomSchemaMetadatas.customStringMetadata())).isEqualTo("customMetadataValue");
		assertThat(record.<List<Enum>>get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));

		zeSchemaTypeRecords.clear();
		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").setSchema("default").addField("title", "Record 1")
				.addField(zeSchema.booleanMetadata().getLocalCode(), asList("yes", "FALSE"))
				.addField(zeSchema.dateMetadata().getLocalCode(), asList(aDate, anotherDate))
				.addField(zeSchema.dateTimeMetadata().getLocalCode(), asList(anotherDateTime, aDateTime))
				.addField(zeSchema.numberMetadata().getLocalCode(), asList("6.66", "42.0"))
				.addField(zeSchema.enumMetadata().getLocalCode(), asList("F", "S")));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat(record.getSchemaCode()).isEqualTo("zeSchemaType_default");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.<List<LocalDate>>get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.<List<LocalDateTime>>get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.<List<Double>>get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
		assertThat(record.<String>get(zeCustomSchemaMetadatas.customStringMetadata())).isNull();
		assertThat(record.<List<Enum>>get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE));
	}

	@Test
	public void whenImportingRecordsWithValidValueInBooleanMetadataThenCorrectlySaved()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
			assertThat(record.<String>get(LEGACY_ID)).isEqualTo("" + i);
			assertThat(record.<String>get(TITLE)).isEqualTo("Record " + i);
			assertThat((Boolean) record.get(zeSchema.booleanMetadata())).describedAs("Record " + i + " should be true")
					.isTrue();
		}

		for (int i = 14; i <= 19; i++) {
			Record record = recordWithLegacyId("" + i);
			assertThat(record).describedAs("Record " + i + " should exist").isNotNull();
			assertThat(record.<String>get(LEGACY_ID)).isEqualTo("" + i);
			assertThat(record.<String>get(TITLE)).isEqualTo("Record " + i);
			assertThat((Boolean) record.get(zeSchema.booleanMetadata())).describedAs("Record " + i + " should be false")
					.isFalse();
		}
	}

	@Test
	public void givenADataProviderReturnAListOfSchemaTypesWithInvalidSchemaTypesThenValidationErrors()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
					newValidationError(RecordsImportServicesExecutor.INVALID_SCHEMA_TYPE_CODE,
							asMap("schemaType", "chuckNorris")),
					newValidationError(RecordsImportServicesExecutor.INVALID_SCHEMA_TYPE_CODE,
							asMap("schemaType", anotherSchema.typeCode() + "s")));
			assertThat(frenchMessages(e)).containsOnly("Le type de schéma «chuckNorris» n'existe pas",
					"Le type de schéma «anotherSchemaTypes» n'existe pas");
		}
	}

	@Test
	public void givenAnImportDataHasANullIdThenValidationException()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(null).addField("title", "Record 3"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(null).addField("title", "Record 5"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredId", "2", "Ze type de schéma : "),
					tuple("RecordsImportServices_requiredId", "4", "Ze type de schéma : ")
			);

			assertThat(frenchMessages(e))
					.containsOnly("Ze type de schéma : Aucun identifiant n'a été défini pour l'enregistrement à la position 2",
							"Ze type de schéma : Aucun identifiant n'a été défini pour l'enregistrement à la position 4");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withTypeFrenchLabel("Ze type label")
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "value", "metadata", "metadataLabel",
					"referencedSchemaType", "referencedSchemaTypeLabel", "prefix"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42", "legacyIdentifier",
									"legacyIdentifier", "zeSchemaType", "Ze type label", "Ze type label 4 : "),
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "666", "legacyIdentifier",
									"legacyIdentifier", "zeSchemaType", "Ze type label", "Ze type label 5 : ")
					);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type label 4 : Aucun enregistrement de type «Ze type label» n'a la valeur «42» à la métadonnée «legacyIdentifier»",
					"Ze type label 5 : Aucun enregistrement de type «Ze type label» n'a la valeur «666» à la métadonnée «legacyIdentifier»");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordUsingLegacyIdResolverThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
			assertThat(extractingSimpleCodeAndParameters(e, "schemaType", "value", "metadata", "metadataLabel",
					"prefix"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42", "legacyIdentifier",
									"legacyIdentifier", "Ze type de schéma 4 : "),
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "666", "legacyIdentifier",
									"legacyIdentifier", "Ze type de schéma 5 : ")
					);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 4 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «42» à la métadonnée «legacyIdentifier»",
					"Ze type de schéma 5 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «666» à la métadonnée «legacyIdentifier»");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingAnInexistentRecordUsingOtherUniqueMetadataResolverThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
			assertThat(
					extractingSimpleCodeAndParameters(e, "schemaType", "value", "metadata", "metadataLabel", "prefix"))
					.containsOnly(
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "42", "stringMetadata",
									"A toAString metadata", "Ze type de schéma 4 : "),
							tuple("RecordsImportServices_unresolvedValue", "zeSchemaType", "666", "stringMetadata",
									"A toAString metadata", "Ze type de schéma 5 : ")
					);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 4 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «42» à la métadonnée «A toAString metadata»",
					"Ze type de schéma 5 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «666» à la métadonnée «A toAString metadata»");
		}
	}

	@Test
	public void givenAnImportedRecordIsReferencingUsingAnInvalidResolverMetadataCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
							"invalidMetadata", "Ze type de schéma 4 : "),
					tuple("RecordsImportServices_invalidResolverMetadataCode", "parentReferenceFromZeSchemaToZeSchema",
							"otherInvalidMetadata", "Ze type de schéma 5 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 4 : La métadonnée «parentReferenceFromZeSchemaToZeSchema» utilise une métadonnée «invalidMetadata» qui n'existe pas ou qui n'est pas acceptée pour obtenir un enregistrement référencé",
					"Ze type de schéma 5 : La métadonnée «parentReferenceFromZeSchemaToZeSchema» utilise une métadonnée «otherInvalidMetadata» qui n'existe pas ou qui n'est pas acceptée pour obtenir un enregistrement référencé");
		}
	}

	@Test
	public void givenPermisiveModeWhenImportRecordWithMissingValuesThenOnlyWarningsForUSR()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata(whichIsMultivalue, whichHasDefaultRequirement)
				.withABooleanMetadata(whichHasDefaultRequirement)
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchemaType("zeSchemaType").getDefaultSchema().create("USRuserMetadata")
								.setType(STRING).setDefaultRequirement(true);
					}
				}));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1")
				.addField("title", "Record 1")
				.addField("stringMetadata", asList("42"))
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2")
				.addField("title", "Record 2")
				.addField("stringMetadata", asList("42"))
				.addField("USRuserMetadata", "value 1")
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3")
				.addField("title", "Record 3")
				.addField("USRuserMetadata", "value 1")
				.addField("stringMetadata", asList("42")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4")
				.addField("title", "Record 4")
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5")
				.addField("title", "Record 5")
				.addField("USRuserMetadata", "value 1")
				.addField("stringMetadata", new ArrayList<>()));

		try {
			bulkImport(importDataProvider, progressionListener, admin, BulkImportParams.PERMISSIVE());
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();

			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "metadataLabel", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata",
							"Ze type de schéma 3 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata",
							"Ze type de schéma 4 : "),
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata",
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata",
							"Ze type de schéma 5 : ")
			);

			assertThat(extractingWarningsSimpleCodeAndParameters(e, "metadata", "metadataLabel", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredValue", "USRuserMetadata", "USRuserMetadata", "Ze type de schéma 4 : "),
					tuple("RecordsImportServices_requiredValue", "USRuserMetadata", "USRuserMetadata", "Ze type de schéma 1 : ")
			);

			assertThat(frenchMessages(e.getValidationErrors().getValidationErrors())).containsOnly(
					"Ze type de schéma 3 : La métadonnée «A boolean metadata» est requise.",
					"Ze type de schéma 4 : La métadonnée «A toAString metadata» est requise.",
					"Ze type de schéma 5 : La métadonnée «A boolean metadata» est requise.",
					"Ze type de schéma 5 : La métadonnée «A toAString metadata» est requise.");

			assertThat(frenchMessages(e.getValidationErrors().getValidationWarnings())).containsOnly(
					"Ze type de schéma 4 : La métadonnée «USRuserMetadata» est requise.",
					"Ze type de schéma 1 : La métadonnée «USRuserMetadata» est requise.");
		}

		Record record = recordWithLegacyId("1");
		assertThat(record).isNotNull();
	}

	@Test
	public void givenAnImportedRecordHasMissingValuesToFieldsThatAreAlwaysEnabledThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.withAStringMetadata(whichIsMultivalue, whichHasDefaultRequirement)
				.withABooleanMetadata(whichHasDefaultRequirement)
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchemaType("zeSchemaType").getDefaultSchema().create("USRuserMetadata")
								.setType(STRING).setDefaultRequirement(true);
					}
				}));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1")
				.addField("title", "Record 1")
				.addField("stringMetadata", asList("42"))
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2")
				.addField("title", "Record 2")
				.addField("stringMetadata", asList("42"))
				.addField("USRuserMetadata", "value 1")
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3")
				.addField("title", "Record 3")
				.addField("USRuserMetadata", "value 1")
				.addField("stringMetadata", asList("42")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4")
				.addField("title", "Record 4")
				.addField("booleanMetadata", "true"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5")
				.addField("title", "Record 5")
				.addField("USRuserMetadata", "value 1")
				.addField("stringMetadata", new ArrayList<>()));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();

			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "metadataLabel", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata",
							"Ze type de schéma 3 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata",
							"Ze type de schéma 4 : "),
					tuple("RecordsImportServices_requiredValue", "booleanMetadata", "A boolean metadata",
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_requiredValue", "stringMetadata", "A toAString metadata",
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_requiredValue", "USRuserMetadata", "USRuserMetadata", "Ze type de schéma 4 : "),
					tuple("RecordsImportServices_requiredValue", "USRuserMetadata", "USRuserMetadata", "Ze type de schéma 1 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 3 : La métadonnée «A boolean metadata» est requise.",
					"Ze type de schéma 4 : La métadonnée «A toAString metadata» est requise.",
					"Ze type de schéma 5 : La métadonnée «A boolean metadata» est requise.",
					"Ze type de schéma 5 : La métadonnée «A toAString metadata» est requise.",
					"Ze type de schéma 4 : La métadonnée «USRuserMetadata» est requise.",
					"Ze type de schéma 1 : La métadonnée «USRuserMetadata» est requise.");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInBooleanMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidBooleanValue", "A boolean metadata", "Oui monsieur",
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidBooleanValue", "A boolean metadata", "Oh yes", "Ze type de schéma 6 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La valeur «Oui monsieur» définie pour la métadonnée «A boolean metadata» n'est pas acceptée, seules les valeurs «true, false» sont acceptées",
					"Ze type de schéma 6 : La valeur «Oh yes» définie pour la métadonnée «A boolean metadata» n'est pas acceptée, seules les valeurs «true, false» sont acceptées");
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

			assertThat(extractingSimpleCodeAndParameters(e, "metadataLabel", "value", "acceptedValues", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidEnumValue", "withAnEnumMetadata", "SECOND_VALUE", "F, S",
							"Ze type de schéma 3 : "),
					tuple("RecordsImportServices_invalidEnumValue", "withAnEnumMetadata", "FS", "F, S", "Ze type de schéma 5 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 3 : La valeur «SECOND_VALUE» définie pour la métadonnée «withAnEnumMetadata» n'est pas acceptée, seules les valeurs «F, S» sont acceptées",
					"Ze type de schéma 5 : La valeur «FS» définie pour la métadonnée «withAnEnumMetadata» n'est pas acceptée, seules les valeurs «F, S» sont acceptées");
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
			assertThat(extractingSimpleCodeAndParameters(e, "index", "legacyId", "acceptedValues", "value",
					"metadataLabel")).containsOnly(
					tuple("RecordsImportServices_invalidEnumValue", "3", "5", "F, S", "FS", "withAnEnumMetadata"),
					tuple("RecordsImportServices_invalidEnumValue", "1", "3", "F, S", "SECOND_VALUE", "withAnEnumMetadata")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La valeur «FS» définie pour la métadonnée «withAnEnumMetadata» n'est pas acceptée, seules les valeurs «F, S» sont acceptées",
					"Ze type de schéma 3 : La valeur «SECOND_VALUE» définie pour la métadonnée «withAnEnumMetadata» n'est pas acceptée, seules les valeurs «F, S» sont acceptées"
			);
		}
	}

	@Test
	public void whenImportingValuesInAMultilingualMetadataThenWrittenForGivenLocalOrMainDataLanguage()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsMultilingual));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value1"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value2", Locale.FRENCH)
				.addField(zeSchema.stringMetadata().getLocalCode(), "value3", Locale.ENGLISH));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value5", Locale.ENGLISH));

		bulkImport(importDataProvider, progressionListener, admin);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.strictlyUsing(Locale.FRENCH)
				.extractingMetadatas("legacyIdentifier", "stringMetadata").containsOnly(
				tuple("3", "value1"),
				tuple("4", "value2"),
				tuple("5", "value4")
		);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.strictlyUsing(Locale.ENGLISH)
				.extractingMetadatas("legacyIdentifier", "stringMetadata").containsOnly(
				tuple("3", null),
				tuple("4", "value3"),
				tuple("5", "value5")
		);

		zeSchemaTypeRecords.clear();
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value6"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value7", Locale.FRENCH));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField(zeSchema.stringMetadata().getLocalCode(), "value8", Locale.ENGLISH));

		bulkImport(importDataProvider, progressionListener, admin);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.strictlyUsing(Locale.FRENCH)
				.extractingMetadatas("legacyIdentifier", "stringMetadata").containsOnly(
				tuple("3", "value6"),
				tuple("4", "value7"),
				tuple("5", "value4")
		);

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.strictlyUsing(Locale.ENGLISH)
				.extractingMetadatas("legacyIdentifier", "stringMetadata").containsOnly(
				tuple("3", null),
				tuple("4", "value3"),
				tuple("5", "value8")
		);

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
			assertThat(frenchMessages(e)).containsOnly("TODO");
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
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 3 : La métadonnée «Une métadonnée copiée» est calculée automatiquement, elle ne peut pas être définie lors de l'importation");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInNumberMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "5L", "Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "5.0t", "Ze type de schéma 6 : "),
					tuple("RecordsImportServices_invalidNumberValue", "A number metadata", "nan", "Ze type de schéma 7 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La valeur «5L» définie à la métadonnée «A number metadata» n'est pas un nombre",
					"Ze type de schéma 6 : La valeur «5.0t» définie à la métadonnée «A number metadata» n'est pas un nombre",
					"Ze type de schéma 7 : La valeur «nan» définie à la métadonnée «A number metadata» n'est pas un nombre");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInStringMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidStringValue", "A toAString metadata", aDateTime.toString(),
							"Ze type de schéma 6 : ")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La valeur «" + aDate.toString()
					+ "» définie pour la métadonnée «A toAString metadata» n'est pas une chaîne de caractères",
					"Ze type de schéma 6 : La valeur «" + aDateTime.toString()
					+ "» définie pour la métadonnée «A toAString metadata» n'est pas une chaîne de caractères");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInMultivalueStringMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDate.toString(),
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDate.toString(),
							"Ze type de schéma 6 : "),
					tuple("RecordsImportServices_invalidStringValue", "stringMetadata", aDateTime.toString(),
							"Ze type de schéma 6 : ")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 6 : La valeur «" + aDateTime.toString()
					+ "» définie pour la métadonnée «A toAString metadata» n'est pas une chaîne de caractères",
					"Ze type de schéma 6 : La valeur «" + aDate.toString()
					+ "» définie pour la métadonnée «A toAString metadata» n'est pas une chaîne de caractères",
					"Ze type de schéma 5 : La valeur «" + aDate.toString()
					+ "» définie pour la métadonnée «A toAString metadata» n'est pas une chaîne de caractères");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInDateMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidDateValue", "a date metadata", aDateTime.toString(),
							"Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidDateValue", "a date metadata", "a text value", "Ze type de schéma 6 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La valeur «" + aDateTime.toString()
					+ "» définie pour la métadonnée «a date metadata» n'est pas un date",
					"Ze type de schéma 6 : La valeur «a text value» définie pour la métadonnée «a date metadata» n'est pas un date");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidValueInDateTimeMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
							"Ze type de schéma 4 : "),
					tuple("RecordsImportServices_invalidDatetimeValue", "a date time metadata", "a text value",
							"Ze type de schéma 6 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 4 : La valeur «" + aDate.toString()
					+ "» définie pour la métadonnée «a date time metadata» n'est pas un date-heure",
					"Ze type de schéma 6 : La valeur «a text value» définie pour la métadonnée «a date time metadata» n'est pas un date-heure");
		}
	}

	@Test
	public void givenAnImportedRecordHasSingleValueInMultiValueMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidMultivalue", "A toAString metadata", "aValue", "Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidMultivalue", "A toAString metadata", "anotherValue",
							"Ze type de schéma 6 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : Une valeur simple a été définie pour la métadonnée à valeurs multiples «A toAString metadata»",
					"Ze type de schéma 6 : Une valeur simple a été définie pour la métadonnée à valeurs multiples «A toAString metadata»");
		}
	}

	@Test
	public void givenAnImportedRecordHasMultiValueInSinglevalueMetadataThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidSinglevalue", "stringMetadata", "[aValue]", "Ze type de schéma 4 : "),
					tuple("RecordsImportServices_invalidSinglevalue", "stringMetadata", "[anotherValue, thirdValue]",
							"Ze type de schéma 6 : ")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 4 : Une liste de valeurs a été définie pour la métadonnée à valeur simple «A toAString metadata»",
					"Ze type de schéma 6 : Une liste de valeurs a été définie pour la métadonnée à valeur simple «A toAString metadata»");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidMetadataCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withSchemaFrenchLabel("Ze default schema label")
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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

			assertThat(extractingSimpleCodeAndParameters(e, "metadata", "schema", "schemaLabel", "prefix")).containsOnly(
					tuple("RecordsImportServices_invalidMetadataCode", "zeChuckNorrisMetadata",
							"zeSchemaType_default", "Ze default schema label", "Ze type de schéma 5 : "),
					tuple("RecordsImportServices_invalidMetadataCode", "anInexistentMetadata",
							"zeSchemaType_default", "Ze default schema label", "Ze type de schéma 6 : "),
					tuple("RecordsImportServices_invalidMetadataCode", "anotherInexistentMetadata",
							"zeSchemaType_default", "Ze default schema label", "Ze type de schéma 6 : ")
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 5 : La métadonnée «zeChuckNorrisMetadata» n'existe pas dans le schéma de métadonnées «zeSchemaType_default» (Ze default schema label)",
					"Ze type de schéma 6 : La métadonnée «anotherInexistentMetadata» n'existe pas dans le schéma de métadonnées «zeSchemaType_default» (Ze default schema label)",
					"Ze type de schéma 6 : La métadonnée «anInexistentMetadata» n'existe pas dans le schéma de métadonnées «zeSchemaType_default» (Ze default schema label)");
		}
	}

	@Test
	public void givenAnImportedRecordHasInvalidSchemaCodeThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
					tuple("RecordsImportServices_invalidSchemaCode", "zeSchemaType", "anInvalidSchema", "Ze type de schéma 4 : "),
					tuple("RecordsImportServices_invalidSchemaCode", "zeSchemaType", "anotherSchemaType_default",
							"Ze type de schéma 8 : ")
			);

			assertThat(frenchMessages(e))
					.containsOnly("Ze type de schéma 4 : Le schéma de métadonnées «anInvalidSchema» est invalide",
							"Ze type de schéma 8 : Le schéma de métadonnées «anotherSchemaType_default» est invalide");
		}
	}

	@Test
	public void givenCyclicDependencyInRecordsParentMetadataThenExceptionDuringImport()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
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
		} catch (ValidationException e) {
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 1 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «2» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma 2 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «3» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma 3 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «4» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma 4 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «2» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma 4 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «2» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma : Il y a possiblement une dépendance cyclique entre les enregistrements importés avec ids «1, 2, 3, 4»");

		}
	}

	@Test
	public void givenCyclicDependencyInRecordsSecondaryMetadatasThenImportedInTwoPhases()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchema(zeSchema.code()).create("secondaryReferenceToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType(anotherSchema.typeCode()));
					}
				}));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("secondaryReferenceToAnotherSchema", "1a"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("secondaryReferenceToAnotherSchema", "2a"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("secondaryReferenceToAnotherSchema", "3a"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("1a").addField("title", "Another record 1")
				.addField("referenceFromAnotherSchemaToZeSchema", "1"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("2a").addField("title", "Another record 2")
				.addField("referenceFromAnotherSchemaToZeSchema", "2"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("3a").addField("title", "Another record 3")
				.addField("referenceFromAnotherSchemaToZeSchema", "3"));

		bulkImport(importDataProvider, progressionListener, admin, new BulkImportParams());
		//TODO Modifier le mécanisme d'import afin de supporter des dépendances cycliques pendant l'import

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas("legacyIdentifier", "secondaryReferenceToAnotherSchema.legacyIdentifier").containsOnly(
				tuple("1", "1a"),
				tuple("2", "2a"),
				tuple("3", "3a")
		);

		assertThatRecords(searchServices.search(query(from(anotherSchema.type()).returnAll())))
				.extractingMetadatas("legacyIdentifier", "referenceFromAnotherSchemaToZeSchema.legacyIdentifier").containsOnly(
				tuple("1a", "1"),
				tuple("2a", "2"),
				tuple("3a", "3")
		);
	}

	@Test
	public void givenInvalidDepedencyValueInSecondaryReferenceThenDetectedInSecondPhase()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema()
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchema(zeSchema.code()).create("secondaryReferenceToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType(anotherSchema.typeCode()));
					}
				}));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("secondaryReferenceToAnotherSchema", "1a"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("secondaryReferenceToAnotherSchema", "2a"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("secondaryReferenceToAnotherSchema", "4a"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("1a").addField("title", "Another record 1")
				.addField("referenceFromAnotherSchemaToZeSchema", "1"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("2a").addField("title", "Another record 2")
				.addField("referenceFromAnotherSchemaToZeSchema", "2"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("3a").addField("title", "Another record 3")
				.addField("referenceFromAnotherSchemaToZeSchema", "3"));


		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(extractingSimpleCodeAndParameters(e, "metadataCode", "referencedSchemaTypeCode", "uniqueMetadataCode", "value")).containsOnly(
					tuple("RecordsImportServicesExecutor_unresolvedDependencyDuringSecondPhase", "secondaryReferenceToAnotherSchema", "anotherSchemaType", "legacyIdentifier", "4a")
			);
			assertThat(frenchMessages(e)).containsOnly("Ze type de schéma 3 : Impossible de définir la métadonnée «secondaryReferenceToAnotherSchema» car aucun enregistrement de type «anotherSchemaType» n'a la valeur «4a» pour la métadonnée «legacyIdentifier».");
		}

		//Since the error occured in second phase, records are imported
		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas("legacyIdentifier", "secondaryReferenceToAnotherSchema.legacyIdentifier").containsOnly(
				tuple("1", "1a"),
				tuple("2", "2a"),
				tuple("3", null)
		);

		assertThatRecords(searchServices.search(query(from(anotherSchema.type()).returnAll())))
				.extractingMetadatas("legacyIdentifier", "referenceFromAnotherSchemaToZeSchema.legacyIdentifier").containsOnly(
				tuple("1a", "1"),
				tuple("2a", "2"),
				tuple("3a", "3")
		);
	}

	@Test
	public void whenValidatingThenBuildAMappingOfLegacyId()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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

		ModelLayerCollectionExtensions extensions = getModelLayerFactory().getExtensions()
				.forCollection(zeCollection);

		RecordsImportServicesExecutor executor = services
				.newExecutor(importDataProvider, new LoggerBulkImportProgressionListener(), admin, asList(types.getCollection()),
						new BulkImportParams());
		executor.initialize();
		executor.resolverCache = resolver;
		executor.validate(new ValidationErrors());

		assertThat(resolver.cache).hasSize(2).containsKey(zeSchema.typeCode()).containsKey(anotherSchema.typeCode());
		assertThat(resolver.getSchemaTypeCache(zeSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping)
				.hasSize(1).containsEntry("previouslySavedRecordLegacyId", "previouslySavedRecordId");
		assertThat(resolver.getSchemaTypeCache(zeSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedUniqueValues.getNestedMap())
				.isEmpty();

		assertThat(resolver.getSchemaTypeCache(anotherSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping).isEmpty();
		assertThat(resolver.getSchemaTypeCache(anotherSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedUniqueValues.getNestedMap())
				.isEmpty();

		assertThat(resolver.getSchemaTypeCache(thirdSchema.typeCode(), LEGACY_ID_LOCAL_CODE).idsMapping).isEmpty();
		assertThat(resolver.getSchemaTypeCache(thirdSchema.typeCode(), LEGACY_ID_LOCAL_CODE).unresolvedUniqueValues.getNestedMap())
				.isEmpty();
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
					tuple("RecordsImportServices_legacyIdNotUnique", "42", "Ze type de schéma 42 : ")
			);

			//			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			//			assertThat(errors).containsOnly(newZeSchemaValidationError(LEGACY_ID_NOT_UNIQUE, asMap("legacyId", "42")));
			assertThat(frenchMessages(e)).containsOnly("Ze type de schéma 42 : L'identifiant «42» n'est pas unique");
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
							"Ze type de schéma 43 : "),
					tuple("RecordsImportServices_metadataNotUnique", "stringMetadata", "A toAString metadata", "v1",
							"Ze type de schéma 45 : ")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 43 : La valeur «v1» pour la métadonnée «A toAString metadata» n'est pas unique",
					"Ze type de schéma 45 : La valeur «v1» pour la métadonnée «A toAString metadata» n'est pas unique");

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
					"Ze type de schéma 43 : La valeur «v2» à la métadonnée «A toAString metadata» est déjà utilisée pour un autre enregistrement",
					"Ze type de schéma 42 : La valeur «v1» à la métadonnée «A toAString metadata» est déjà utilisée pour un autre enregistrement"
			);
		}

	}

	@Test
	public void givenAnImportedRecordHaveSameUniqueMetadataThanOtherExistingRecordWhenImportingWithMergeModeThenMerged()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadata(whichIsUnique)
				.withAnotherSchemaStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchemaType(thirdSchema.typeCode()).getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType(zeSchema.typeCode()));
					}
				}));

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

		thirdSchemaTypeRecords.add(defaultSchemaData().setId("42ref").addField("title", "t1").addField("refToZeSchema", "42"));
		thirdSchemaTypeRecords.add(defaultSchemaData().setId("43ref").addField("title", "t2").addField("refToZeSchema", "43"));
		thirdSchemaTypeRecords.add(defaultSchemaData().setId("44ref").addField("title", "t3").addField("refToZeSchema", "44"));
		thirdSchemaTypeRecords
				.add(defaultSchemaData().setId("45ref").addField("title", "t4").addField("refToZeSchema", "stringMetadata:v4"));

		importDataProvider.dataOptionsMap
				.put(zeSchema.typeCode(), new ImportDataOptions().setMergeExistingRecordWithSameUniqueMetadata(true));
		bulkImport(importDataProvider, progressionListener, admin, new BulkImportParams());

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas("stringMetadata", "title", "legacyIdentifier").containsOnly(
				tuple("v1", "Record 1", "42"),
				tuple("v2", "Record 2", "43"),
				tuple("v3", "Record 3", "44"),
				tuple("v4", "existing record C", null)
		);

		assertThatRecords(searchServices.search(query(from(thirdSchema.type()).returnAll())))
				.extractingMetadatas("refToZeSchema.stringMetadata", "title", "legacyIdentifier").containsOnly(
				tuple("v1", "t1", "42ref"),
				tuple("v2", "t2", "43ref"),
				tuple("v3", "t3", "44ref"),
				tuple("v4", "t4", "45ref")
		);
	}


	@Test
	public void whenImportingAsIdThenOnlyImportedIfIdRespectsConditions()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadata(whichHasDefaultRequirement)
				.withAnotherSchemaStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchemaType(thirdSchema.typeCode()).getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType(zeSchema.typeCode()));
					}
				}));

		String unusedId = getModelLayerFactory().getDataLayerFactory().getUniqueIdGenerator().next();
		String adminIdInSystemCollection = getModelLayerFactory().getUserCredentialsManager().getUserCredential("admin").getId();

		recordServices
				.add(new TestRecord(anotherSchema, "00000111111").set(TITLE, "v0"));
		recordServices.add(new TestRecord(zeSchema, "00000222222").set(TITLE, "v0").set(zeSchema.stringMetadata(), "v1"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("00000111111").addField("title", "v1").addField("stringMetadata", "value"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("00000222222").addField("title", "v1"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("00000333333").addField("title", "v1").addField("stringMetadata", "value"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(unusedId).addField("title", "v1").addField("stringMetadata", "value"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId(adminIdInSystemCollection).addField("title", "v1").addField("stringMetadata", "value"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("mouhahahaha").addField("title", "v1").addField("stringMetadata", "value"));

		importDataProvider.dataOptionsMap
				.put(zeSchema.typeCode(), new ImportDataOptions().setImportAsLegacyId(false));

		try {
			bulkImport(importDataProvider, progressionListener, admin, new BulkImportParams());

		} catch (ValidationException e) {

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 00000333333 : L'identifiant «00000333333» est plus élevé que la table de séquence, ce qui pourrait éventuellement engendrer un conflit",
					"Ze type de schéma 00000111111 : L'identifiant «00000111111» est déjà utilisé pour un autre type d'enregistrement",
					"Ze type de schéma " + adminIdInSystemCollection + " : L'identifiant «" + adminIdInSystemCollection + "» est déjà utilisé dans une autre collection",
					"Ze type de schéma mouhahahaha : L'identifiant «mouhahahaha» ne respecte pas le standard d'identifiant de Constellio, soit un nombre composé de 11 chiffres (préfixé au besoin par des zéros)"
			);

		}

		assertThatRecords(searchServices.search(query(from(zeSchema.type()).returnAll())))
				.extractingMetadatas("id", "title", "legacyIdentifier").containsOnly(
				tuple("00000222222", "v1", null),
				tuple(unusedId, "v1", null)
		);

		assertThatRecords(searchServices.search(query(from(anotherSchema.type()).returnAll())))
				.extractingMetadatas("id", "title", "legacyIdentifier").containsOnly(
				tuple("00000111111", "v0", null)
		);

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
				.addField("contentMetadata", new SimpleImportContent(testResource1, "Ze document.docx", true, null, null)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new SimpleImportContent(testResource2, "Ze ultimate document.pdf", false, null, null),
						new SimpleImportContent(testResource3, "Ze book.txt", true, null, null))));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("Exception expected");

		} catch (ValidationException e) {

			assertThat(extractingWarningsSimpleCodeAndParameters(e.getValidationErrors(), "url")).containsOnly(
					tuple("RecordsImportServices_contentNotFound", testResource1),
					tuple("RecordsImportServices_contentNotFound", testResource3)
			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 1 : Le contenu à l'URL «" + testResource1 + "» ne peut pas être obtenu",
					"Ze type de schéma 3 : Le contenu à l'URL «http://www.perdu.com/edouardLechat.pdf» ne peut pas être obtenu"
			);
		}

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

	}

	@Test
	public void whenImportingRecordsWithInvalidContentPreimportedContentsThenImportWithValidationErrors()
			throws Exception {

		File toImport = new File(getModelLayerFactory().getConfiguration().getContentImportThreadFolder(), "toImport");

		System.out.println(toImport.getAbsolutePath());

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		FileUtils.copyFile(getTestResourceFile("resource1.docx"), new File(toImport, "file1.docx"));
		FileUtils.copyFile(getTestResourceFile("resource2.pdf"), new File(toImport, "file2.pdf"));
		FileUtils.copyFile(getTestResourceFile("resource4.docx"), new File(toImport, "folder" + separator + "file3.docx"));
		FileUtils.copyFile(getTestResourceFile("resource5.pdf"), new File(toImport, "file4.pdf"));
		FileUtils.copyFile(getTestResourceFile("resource5.pdf"), new File(toImport, "file5.pdf"));

		String file1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
		String file2Hash = "KN8RjbrnBgq1EDDV2U71a6_6gd4=";
		String file3Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";
		String file4Hash = "T-4zq4cGP_tXkdJp_qz1WVWYhoQ=";
		String file5Hash = "T-4zq4cGP_tXkdJp_qz1WVWYhoQ=";

		givenTimeIs(TimeProvider.getLocalDateTime().plusSeconds(60));
		contentManager.uploadFilesInImportFolder();

		deletingContentWithFilename(file1Hash);

		LocalDateTime now = TimeProvider.getLocalDateTime();

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new SimpleImportContent("imported://file1.docx", "File 1.docx", true, now)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("contentListMetadata", asList(
						new SimpleImportContent("imported://file2.pdf", "File 2a.pdf", true, now),
						new SimpleImportContent("imported://file5.pdf", "File 2b.pdf", true, now))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentMetadata", new SimpleImportContent("imported://inexistentFile.pdf", "File 3.pdf", true, now)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("title", "Record 4")
				.addField("contentMetadata", new SimpleImportContent("imported://folder/file3.docx", "File 4.docx", true, now)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("5").addField("title", "Record 5")
				.addField("contentMetadata", new SimpleImportContent("imported://folder\\file3.docx", "File 5.docx", true, now)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("6").addField("title", "Record 6")
				.addField("contentMetadata",
						new SimpleImportContent("imported://otherFolder/file3.docx", "File 6.docx", true, now)));

		SimpleImportContent contentImport7 = new SimpleImportContent("imported://file2.pdf", "File 7a.pdf", true, now);
		contentImport7.getVersions().add(new ContentImportVersion("imported://file4.pdf", "File 7b.pdf", false, now));
		contentImport7.getVersions().add(new ContentImportVersion("imported://file5.pdf", "File 7c.pdf", false, now));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("7").addField("title", "Record 7")
				.addField("contentMetadata", contentImport7));

		SimpleImportContent contentImport8 = new SimpleImportContent("imported://file2.pdf", "File 7a.pdf", true, now);
		contentImport8.getVersions().add(new ContentImportVersion("imported://fileZ.pdf", "File 7b.pdf", false, now));
		contentImport8.getVersions().add(new ContentImportVersion("imported://file5.pdf", "File 7c.pdf", false, now));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("8").addField("title", "Record 8")
				.addField("contentMetadata", contentImport8));

		try {
			bulkImport(importDataProvider, progressionListener, admin);

		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "prefix", "fileName", "filePath", "hash")).containsOnly(
					tuple("RecordsImportServices_contentNotImported", "Ze type de schéma 3 : ", "File 3.pdf",
							"inexistentFile.pdf", null),
					tuple("RecordsImportServices_contentNotImported", "Ze type de schéma 6 : ", "File 6.docx",
							"otherFolder/file3.docx", null),
					tuple("RecordsImportServices_contentNotImported", "Ze type de schéma 8 : ", "File 7a.pdf", "fileZ.pdf", null),
					tuple("RecordsImportServices_hashNotFoundInVault", "Ze type de schéma 1 : ", null, pathOf(file1Hash),
							file1Hash)

			);

			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 6 : Aucun contenu pré-importé «otherFolder/file3.docx»",
					"Ze type de schéma 8 : Aucun contenu pré-importé «fileZ.pdf»",
					"Ze type de schéma 3 : Aucun contenu pré-importé «inexistentFile.pdf»",
					"Ze type de schéma 1 : Le contenu «Fss7pKBafi8ok5KaOwEpmNdeGCE=» n'existe pas dans la voûte à l'emplacement «"
					+ pathOf(file1Hash) + "»");
		}

		Record record1 = recordWithLegacyId("1");
		Record record2 = recordWithLegacyId("2");
		Record record4 = recordWithLegacyId("4");
		Record record5 = recordWithLegacyId("5");
		Record record7 = recordWithLegacyId("7");

		assertThat(record1).isNull();

		assertThat(record2.<Content>get(zeSchema.contentMetadata())).isNull();
		List<Content> record2ContentList = record2.get(zeSchema.contentListMetadata());
		assertThat(record2ContentList.get(0).getCurrentVersion()).has(hashFilenameVersion(file2Hash, "File 2a.pdf", "1.0"));
		assertThat(record2ContentList.get(1).getCurrentVersion()).has(hashFilenameVersion(file5Hash, "File 2b.pdf", "1.0"));

		assertThat(record4.<Content>get(zeSchema.contentMetadata()).getCurrentVersion())
				.has(hashFilenameVersion(file3Hash, "File 4.docx", "1.0"));
		assertThat(record4.getList(zeSchema.contentListMetadata())).isEmpty();

		assertThat(record5.<Content>get(zeSchema.contentMetadata()).getCurrentVersion())
				.has(hashFilenameVersion(file3Hash, "File 5.docx", "1.0"));
		assertThat(record5.getList(zeSchema.contentListMetadata())).isEmpty();

		Content record7Content = record7.get(zeSchema.contentMetadata());
		assertThat(record7Content.getVersions().get(0)).has(hashFilenameVersion(file2Hash, "File 7a.pdf", "1.0"));
		assertThat(record7Content.getVersions().get(1)).has(hashFilenameVersion(file4Hash, "File 7b.pdf", "1.1"));
		assertThat(record7Content.getVersions().get(2)).has(hashFilenameVersion(file5Hash, "File 7c.pdf", "1.2"));

	}

	private String pathOf(String hash) {
		return ((FileSystemContentDao) getDataLayerFactory().getContentsDao()).getFileOf(hash).getAbsolutePath();

	}

	@Test
	public void whenImportingContentUsingPreexistingHashModeThenContentAddedEvenIfHashNotFoundReturningItInWarnings()
			throws Exception {

		File toImport = new File(getModelLayerFactory().getConfiguration().getContentImportThreadFolder(), "toImport");

		System.out.println(toImport.getAbsolutePath());

		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary summary1 = contentManager.upload(getTestResourceInputStream("resource1.docx"), "File1.docx")
				.getContentVersionDataSummary();
		ContentVersionDataSummary summary2 = contentManager.upload(getTestResourceInputStream("resource2.pdf"), "File2.pdf")
				.getContentVersionDataSummary();

		ContentVersionDataSummary inexistent1 = new ContentVersionDataSummary("inexistentHash1", "pdf", 45);
		ContentVersionDataSummary inexistent2 = new ContentVersionDataSummary("inexistentHash2", "doc", 56);
		ContentVersionDataSummary inexistent3 = new ContentVersionDataSummary("inexistentHash3", "pdf", 67);
		ContentVersionDataSummary inexistent4 = new ContentVersionDataSummary("inexistentHash4", "pdf", 78);

		LocalDateTime now = TimeProvider.getLocalDateTime();

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		Content content = contentManager.createMajor(users.adminIn(zeCollection), "File1a.pdf", inexistent1)
				.checkOut(users.sasquatchIn(zeCollection)).updateCheckedOutContent(summary1);

		Content content1 = contentManager.createMajor(users.adminIn(zeCollection), "File1b.pdf", inexistent1);
		content1.updateContent(users.dakotaIn(zeCollection), inexistent2, false);

		Content content2 = contentManager.createMajor(users.adminIn(zeCollection), "File1c.pdf", inexistent1);
		content2.updateContent(users.dakotaIn(zeCollection), inexistent3, false);
		content2.updateContent(users.dakotaIn(zeCollection), summary2, true);

		UserSerializedContentFactory contentFactory = new UserSerializedContentFactory(zeCollection, getModelLayerFactory());

		//
		//		List<ContentImportVersion> versions = new ArrayList<>();
		//		versions.add(new ContentImportVersion("hash://inexistentHash1", "File 1a.pdf", true, LocalDateTime.now()));
		//		versions.add(new ContentImportVersion("hash://" + hash1, "File 1b.pdf", false, LocalDateTime.now()));
		//		versions.add(new ContentImportVersion("hash://inexistentHash2", "File 1b.pdf", true, LocalDateTime.now()));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new StructureImportContent(contentFactory.toString(content))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("contentListMetadata", asList(
						new StructureImportContent(contentFactory.toString(content1)),
						new StructureImportContent(contentFactory.toString(content2)))));

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("Exception expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "filePath", "hash")).isEmpty();

			String contentPath = getDataLayerFactory().getDataLayerConfiguration().getContentDaoFileSystemFolder()
										 .getAbsolutePath() + File.separator;
			assertThat(extractingWarningsSimpleCodeAndParameters(e, "filePath", "hash")).containsOnly(
					tuple("RecordsImportServices_hashNotFoundInVault", pathOf("inexistentHash1"), "inexistentHash1"),
					tuple("RecordsImportServices_hashNotFoundInVault", pathOf("inexistentHash2"), "inexistentHash2"),
					tuple("RecordsImportServices_hashNotFoundInVault", pathOf("inexistentHash3"), "inexistentHash3")
			);

			assertThat(frenchMessages(e.getValidationErrors().getValidationWarnings())).containsOnly(
					"Ze type de schéma 1 : Le contenu «inexistentHash1» n'existe pas dans la voûte à l'emplacement «" + pathOf(
							"inexistentHash1") + "»",
					"Ze type de schéma 2 : Le contenu «inexistentHash3» n'existe pas dans la voûte à l'emplacement «" + pathOf(
							"inexistentHash3") + "»",
					"Ze type de schéma 2 : Le contenu «inexistentHash1» n'existe pas dans la voûte à l'emplacement «" + pathOf(
							"inexistentHash1") + "»",
					"Ze type de schéma 2 : Le contenu «inexistentHash2» n'existe pas dans la voûte à l'emplacement «" + pathOf(
							"inexistentHash2") + "»"
			);
		}

		Record record1 = recordWithLegacyId("1");
		Record record2 = recordWithLegacyId("2");

		assertThat(record1.getList(zeSchema.contentListMetadata())).isEmpty();
		Content record1Content = record1.get(zeSchema.contentMetadata());
		assertThat(record1Content.getCurrentVersion()).has(hashFilenameVersion("inexistentHash1", "File1a.pdf", "1.0"));
		assertThat(record1Content.getCurrentCheckedOutVersion())
				.has(hashFilenameVersion(summary1.getHash(), "File1a.pdf", "1.1"));
		assertThat(record1Content.getCheckoutUserId()).isEqualTo(users.sasquatchIn(zeCollection).getId());

		assertThat(record2.<Content>get(zeSchema.contentMetadata())).isNull();
		List<Content> record2ContentList = record2.get(zeSchema.contentListMetadata());
		assertThat(record2ContentList.get(0).getCurrentVersion())
				.has(hashFilenameVersion("inexistentHash2", "File1b.pdf", "1.1"));
		assertThat(record2ContentList.get(0).getHistoryVersions().get(0))
				.has(hashFilenameVersion("inexistentHash1", "File1b.pdf", "1.0"));
		assertThat(record2ContentList.get(1).getCurrentVersion())
				.has(hashFilenameVersion(summary2.getHash(), "File1c.pdf", "2.0"));
		assertThat(record2ContentList.get(1).getHistoryVersions().get(0))
				.has(hashFilenameVersion("inexistentHash1", "File1c.pdf", "1.0"));
		assertThat(record2ContentList.get(1).getHistoryVersions().get(1))
				.has(hashFilenameVersion("inexistentHash3", "File1c.pdf", "1.1"));

	}

	private void deletingContentWithFilename(String hash) {

		getDataLayerFactory().getContentsDao().delete(singletonList(hash));
		getDataLayerFactory().getContentsDao().delete(singletonList(hash + "__parsed"));

	}

	private Condition<? super Object> hashFilenameVersion(final String hash, final String fileName,
														  final String version) {
		return new Condition<Object>() {
			@Override
			public boolean matches(Object value) {
				ContentVersion contentVersion = (ContentVersion) value;
				assertThat(contentVersion.getHash()).describedAs("hash").isEqualTo(hash);
				assertThat(contentVersion.getFilename()).describedAs("fileName").isEqualTo(fileName);
				assertThat(contentVersion.getVersion()).describedAs("version").isEqualTo(version);
				return true;
			}
		};
	}

	@Test
	@InternetTest
	public void whenImportingRecordsWithContentThenContentUploadedAndAddedToRecord()
			throws Exception {
		String testResource1 = getTestResourceFile("resource1.docx").getAbsolutePath();
		String testResource2 = getTestResourceFile("resource2.pdf").getAbsolutePath();
		String testResource3 = "https://files.slack.com/files-pri/T027CK5P0-F4WQY394M/download/pg338?pub_secret=a76e89b2ab";
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
				.addField("contentMetadata", new SimpleImportContent(Arrays.asList(version1, version2, version3))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new SimpleImportContent(testResource2, "Ze ultimate document.pdf", false, null, null),
						new SimpleImportContent(testResource3, "Ze book.txt", true, null, null))));

		bulkImport(importDataProvider, progressionListener, admin);

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

		//Reimport the records changing the order of versions of record #1

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new SimpleImportContent(Arrays.asList(version3, version2, version1))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new SimpleImportContent(testResource2, "Ze ultimate document.pdf", false, null, null),
						new SimpleImportContent(testResource3, "Ze book.txt", true, null, null))));

		bulkImport(importDataProvider, progressionListener, admin);

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
							"zeSchemaType_default_stringMetadata", asMap("fr", "A toAString metadata", "en", "stringMetadata"),
							"[numberMetadata, booleanMetadata]"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "3", "4", "zeSchemaType",
							"zeSchemaType_default_stringMetadata", asMap("fr", "A toAString metadata", "en", "stringMetadata"),
							"[numberMetadata, booleanMetadata]")
			);
			assertThat(frenchMessages(e)).containsOnly(
					"Ze type de schéma 3 : Métadonnée «A toAString metadata» requise",
					"Ze type de schéma 4 : Métadonnée «A toAString metadata» requise");
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

	public static class CalculatorReturningBingoIf42 extends AbstractMetadataValueCalculator<String> {

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
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
	public void givenWarningsForInvalidFacultativeMetadatasWhenImportingRecordsWithInvalidReferencesThenWarningFor()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadataInCustomSchema()
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaTypeBuilder anotherSchemaTypeDefault = schemaTypes.getSchemaType("anotherSchemaType");
						anotherSchemaTypeDefault.getDefaultSchema().create("code").setType(STRING)
								.setDefaultRequirement(true).setUniqueValue(true);

						MetadataSchemaBuilder zeSchemaTypeDefault = schemaTypes.getSchemaType("zeSchemaType").getDefaultSchema();
						zeSchemaTypeDefault.create("requiredReference").defineReferencesTo(anotherSchemaTypeDefault)
								.setDefaultRequirement(true);
						zeSchemaTypeDefault.create("facultativeReference").defineReferencesTo(anotherSchemaTypeDefault);

						zeSchemaTypeDefault.create("facultativeReferenceUsedByFacultativeCalculatedMetadata")
								.defineReferencesTo(anotherSchemaTypeDefault);
						zeSchemaTypeDefault.create("facultativeReferenceUsedByRequiredCalculatedMetadata")
								.defineReferencesTo(anotherSchemaTypeDefault);

						zeSchemaTypeDefault.create("facultativeCalculatedMetadata").defineReferencesTo(anotherSchemaTypeDefault)
								.defineDataEntry().asJexlScript("facultativeReferenceUsedByFacultativeCalculatedMetadata");
						zeSchemaTypeDefault.create("requiredCalculatedMetadata").defineReferencesTo(anotherSchemaTypeDefault)
								.setDefaultRequirement(true)
								.defineDataEntry().asJexlScript("facultativeReferenceUsedByRequiredCalculatedMetadata");

					}
				}));

		recordServices.add(new TestRecord(anotherSchema).set(CODE, "ze code").set(TITLE, "Ze title").set(LEGACY_ID, "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1")
				.addField("requiredReference", "43")
				.addField("facultativeReferenceUsedByRequiredCalculatedMetadata", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2")
				.addField("requiredReference", "42")
				.addField("facultativeReference", "43")
				.addField("facultativeReferenceUsedByRequiredCalculatedMetadata", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3")
				.addField("requiredReference", "42")
				.addField("facultativeReferenceUsedByRequiredCalculatedMetadata", "43")
				.addField("facultativeReferenceUsedByFacultativeCalculatedMetadata", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("4")
				.addField("requiredReference", "42")
				.addField("facultativeReferenceUsedByRequiredCalculatedMetadata", "42")
				.addField("facultativeReferenceUsedByFacultativeCalculatedMetadata", "43"));

		try {
			bulkImport(importDataProvider, progressionListener, admin, BulkImportParams.PERMISSIVE());
			fail("Validation exception expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "legacyId", "metadataCode")).containsOnly(
					tuple("ValueRequirementValidator_requiredValueForMetadata", "1", "zeSchemaType_default_requiredReference"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "3",
							"zeSchemaType_default_requiredCalculatedMetadata")
			);
			assertThat(extractingWarningsSimpleCodeAndParameters(e, "legacyId", "metadata", "value")).containsOnly(
					tuple("RecordsImportServices_unresolvedValue", "1", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "2", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "3", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "4", "legacyIdentifier", "43")
			);
		}

		assertThat(idsOfAllRecordsWithLegacyId());

	}

	@Test
	public void whenImportingRecordsWithInvalidReferencesThenErrors()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadataInCustomSchema()
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaTypeBuilder anotherSchemaTypeDefault = schemaTypes.getSchemaType("anotherSchemaType");
						anotherSchemaTypeDefault.getDefaultSchema().create("code").setType(STRING)
								.setDefaultRequirement(true).setUniqueValue(true);

						MetadataSchemaBuilder zeSchemaTypeDefault = schemaTypes.getSchemaType("zeSchemaType").getDefaultSchema();
						zeSchemaTypeDefault.create("requiredReference").defineReferencesTo(anotherSchemaTypeDefault)
								.setDefaultRequirement(true);
						zeSchemaTypeDefault.create("facultativeReference").defineReferencesTo(anotherSchemaTypeDefault);

						zeSchemaTypeDefault.create("facultativeReferenceUsedByFacultativeCalculatedMetadata")
								.defineReferencesTo(anotherSchemaTypeDefault);
						zeSchemaTypeDefault.create("facultativeReferenceUsedByRequiredCalculatedMetadata")
								.defineReferencesTo(anotherSchemaTypeDefault);

						zeSchemaTypeDefault.create("facultativeCalculatedMetadata").defineReferencesTo(anotherSchemaTypeDefault)
								.defineDataEntry().asJexlScript("facultativeReferenceUsedByFacultativeCalculatedMetadata");
						zeSchemaTypeDefault.create("requiredCalculatedMetadata").defineReferencesTo(anotherSchemaTypeDefault)
								.setDefaultRequirement(true)
								.defineDataEntry().asJexlScript("facultativeReferenceUsedByRequiredCalculatedMetadata");

					}
				}));

		recordServices.add(new TestRecord(anotherSchema).set(CODE, "ze code").set(TITLE, "Ze title").set(LEGACY_ID, "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("requiredReference", "43"));
		zeSchemaTypeRecords
				.add(defaultSchemaData().setId("2").addField("requiredReference", "42").addField("facultativeReference", "43"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("requiredReference", "42")
				.addField("facultativeReferenceUsedByRequiredCalculatedMetadata", "43"));
		zeSchemaTypeRecords.add(defaultSchemaData().setId("4").addField("requiredReference", "42")
				.addField("facultativeReferenceUsedByFacultativeCalculatedMetadata", "43"));

		try {
			bulkImport(importDataProvider, progressionListener, admin, BulkImportParams.PERMISSIVE()
					.setWarningsForInvalidFacultativeMetadatas(false));
			fail("Validation exception expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "legacyId", "metadata", "value")).containsOnly(
					tuple("RecordsImportServices_unresolvedValue", "1", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "2", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "3", "legacyIdentifier", "43"),
					tuple("RecordsImportServices_unresolvedValue", "4", "legacyIdentifier", "43"),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "3", null, null),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "2", null, null),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "1", null, null),
					tuple("ValueRequirementValidator_requiredValueForMetadata", "4", null, null)
			);
			assertThat(extractingWarningsSimpleCodeAndParameters(e, "legacyId", "metadata")).isEmpty();
		}

		assertThat(idsOfAllRecordsWithLegacyId());

	}

	private List<String> idsOfAllRecordsWithLegacyId() {
		return null;
	}

	@Test
	public void whenImportingUsingUniqueMetadataResolversThenImportedCorrectly()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

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
	public void givenImportingUsingUniqueCalculatedMetadataOfCachedRecordsWhenFallbackOnTransactionCacheThenResolveDependencies()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique, whichIsScripted("'code' + anotherStringMetadata"))
				.withAnotherStringMetadata()
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(schemas.zeDefaultSchemaType().getCode()).setRecordCacheType(FULLY_CACHED);
			}
		});

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title")
				.set(zeSchema.anotherStringMetadata(), "0"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code2")
				.addField("anotherStringMetadata", "1"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code3")
				.addField("anotherStringMetadata", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code42")
				.addField("anotherStringMetadata", "3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code0")
				.addField("anotherStringMetadata", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("custom").setId("43").addField("title", "Record 43")
				.addField("customString", "customFieldValue")
				.addField("anotherStringMetadata", "43"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "stringMetadata:code1"));

		bulkImport(importDataProvider, progressionListener, admin);


	}


	@Test
	public void givenImportingUsingUniqueCalculatedMetadataOfCachedRecordsWhenCannotFindReferenceWhileFallbackingOnTransactionCacheThenValidationError()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique, whichIsScripted("'code' + anotherStringMetadata"))
				.withAnotherStringMetadata()
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAParentReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(schemas.zeDefaultSchemaType().getCode()).setRecordCacheType(FULLY_CACHED);
			}
		});

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(TITLE, "title")
				.set(zeSchema.anotherStringMetadata(), "0"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code666")
				.addField("anotherStringMetadata", "1"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code3")
				.addField("anotherStringMetadata", "2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code42")
				.addField("anotherStringMetadata", "3"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("42").addField("title", "Record 42")
				.addField("parentReferenceFromZeSchemaToZeSchema", "stringMetadata:code0")
				.addField("anotherStringMetadata", "42"));

		zeSchemaTypeRecords.add(defaultSchemaData().setSchema("custom").setId("43").addField("title", "Record 43")
				.addField("customString", "customFieldValue")
				.addField("anotherStringMetadata", "43"));

		anotherSchemaTypeRecords.add(defaultSchemaData().setId("666").addField("title", "Ze record")
				.addField("referenceFromAnotherSchemaToZeSchema", "stringMetadata:code1"));


		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationException e) {

			assertThat(frenchMessages(e.getValidationErrors().getValidationErrors())).containsOnly(
					"Ze type de schéma 1 : Aucun enregistrement de type «Ze type de schéma» n'a la valeur «code666» à la métadonnée «parentReferenceFromZeSchemaToZeSchema»",
					"Ze type de schéma : Il y a possiblement une dépendance cyclique entre les enregistrements importés avec ids «1»"
			);

		}

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
					tuple("RecordsImportServices_noY", "Value with a y", "12", "2", "Ze type de schéma 12 : ")
			);

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
					tuple("RecordsImportServices_noTata", "2", "12", "Value with a tata", "Ze type de schéma 12 : "),
					tuple("RecordsImportServices_noToto", "3", "13", "Value with a toto", "Ze type de schéma 13 : ")
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
		assertThat(record1.<String>get(TITLE)).isEqualTo("pouet");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.<String>get(TITLE)).isEqualTo("Record 2");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.<String>get(TITLE)).isEqualTo("Record 3");
	}

	@Test
	public void whenImportingValueOfFixedSequenceMetadatasThenSetAndIncrementSequences()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichHasFixedSequence("sequence1"))
				.withAnotherStringMetadata(whichHasFixedSequence("sequence2"), whichHasInputMask("9999")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("stringMetadata", "3").addField("anotherStringMetadata", "0003"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("stringMetadata", "4").addField("anotherStringMetadata", "0002"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("stringMetadata", "1").addField("anotherStringMetadata", "0042"));

		services.bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("11");
		assertThat(record1.<String>get(zeSchema.stringMetadata())).isEqualTo("3");
		assertThat(record1.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0003");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.<String>get(zeSchema.stringMetadata())).isEqualTo("4");
		assertThat(record2.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0002");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.<String>get(zeSchema.stringMetadata())).isEqualTo("1");
		assertThat(record3.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0042");

		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		assertThat(sequencesManager.getLastSequenceValue("sequence1")).isEqualTo(4);
		assertThat(sequencesManager.getLastSequenceValue("sequence2")).isEqualTo(42);

	}

	@Test
	public void givenSequencesHigherThanImportedValuesWhenImportingValueOfFixedSequenceMetadatasThenDoNotSetSequences()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		sequencesManager.set("sequence1", 10000);
		sequencesManager.set("sequence2", 20000);

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichHasFixedSequence("sequence1"))
				.withAnotherStringMetadata(whichHasFixedSequence("sequence2"), whichHasInputMask("9999")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("stringMetadata", "3").addField("anotherStringMetadata", "0003"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("stringMetadata", "4").addField("anotherStringMetadata", "0002"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("stringMetadata", "1").addField("anotherStringMetadata", "0042"));

		services.bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("11");
		assertThat(record1.<String>get(zeSchema.stringMetadata())).isEqualTo("3");
		assertThat(record1.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0003");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.<String>get(zeSchema.stringMetadata())).isEqualTo("4");
		assertThat(record2.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0002");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.<String>get(zeSchema.stringMetadata())).isEqualTo("1");
		assertThat(record3.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0042");

		assertThat(sequencesManager.getLastSequenceValue("sequence1")).isEqualTo(10000);
		assertThat(sequencesManager.getLastSequenceValue("sequence2")).isEqualTo(20000);

	}

	@Test
	public void whenImportingValueOfDynamicSequenceMetadatasThenSetAndIncrementSequences()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata()
				.withAnotherStringMetadata(whichHasSequenceDefinedByMetadata("stringMetadata"), whichHasInputMask("9999")));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("11").addField("title", "Record 1")
				.addField("stringMetadata", "sequence1").addField("anotherStringMetadata", "0042"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("12").addField("title", "Record 2")
				.addField("stringMetadata", "sequence2").addField("anotherStringMetadata", "0002"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("13").addField("title", "Record 3")
				.addField("stringMetadata", "sequence1").addField("anotherStringMetadata", "0003"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("14").addField("title", "Record 4")
				.addField("stringMetadata", "sequence2").addField("anotherStringMetadata", "0666"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("15").addField("title", "Record 5")
				.addField("stringMetadata", null).addField("anotherStringMetadata", "6666"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("16").addField("title", "Record 6")
				.addField("stringMetadata", "").addField("anotherStringMetadata", "7777"));

		services.bulkImport(importDataProvider, progressionListener, admin);

		Record record1 = recordWithLegacyId("11");
		assertThat(record1.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0042");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0002");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.<String>get(zeSchema.anotherStringMetadata())).isEqualTo("0003");

		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		assertThat(sequencesManager.getLastSequenceValue("sequence1")).isEqualTo(42);
		assertThat(sequencesManager.getLastSequenceValue("sequence2")).isEqualTo(666);
	}

	public static class NoZMetadataValidator implements RecordMetadataValidator<String> {

		@Override
		public void validate(Metadata metadata, String value, ConfigProvider configProvider,
							 ValidationErrors validationErrors) {
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
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "Ze type de schéma record142 : ")
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(0);
	}

	@Test
	public void givenImportingWithRemoveInvalidFacultativeMetadatasModeWhenInvalidValuesThenContinueImportAndReturnWarnings()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").defineValidators()
								.add(NoZMetadataValidator.class);

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		for (int i = 1; i <= 300; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + i));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin, BulkImportParams.PERMISSIVE());

			fail("ValidationException expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).isEmpty();
			assertThat(extractingWarningsSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "Ze type de schéma record142 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "188", "Ze type de schéma record188 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "244", "Ze type de schéma record244 : ")
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(300);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(300);
	}

	@Test
	public void whenImportingWithContinueRecordsOfSameTypeErrorModeThenContinue()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").defineValidators()
								.add(NoZMetadataValidator.class);

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		for (int i = 1; i <= 300; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + i));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					new BulkImportParams().setImportErrorsBehavior(CONTINUE_FOR_RECORD_OF_SAME_TYPE));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "Ze type de schéma record142 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "188", "Ze type de schéma record188 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "244", "Ze type de schéma record244 : ")
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(0);
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

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("refToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"));

						Map<Language, String> labels = new HashMap<Language, String>();
						labels.put(Language.French, "Autre type de schéma");
						schemaTypes.getSchemaType("anotherSchemaType").setLabels(labels);

						labels = new HashMap<Language, String>();
						labels.put(Language.French, "Troisième type de schéma");
						schemaTypes.getSchemaType("aThirdSchemaType").setLabels(labels);
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		for (int i = 1; i <= 302; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + (i > 300 ? 142 : i)));
		}

		for (int i = 1; i <= 303; i++) {
			thirdSchemaTypeRecords.add(defaultSchemaData().setId("thirdSchemaRecord" + i)
					.addField("refToAnotherSchema", "anotherSchemaRecord" + (i > 302 ? 301 : i)));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					new BulkImportParams().setImportErrorsBehavior(CONTINUE));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "Ze type de schéma record142 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "188", "Ze type de schéma record188 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "244", "Ze type de schéma record244 : ")
			);

			assertThat(extractingWarningsSimpleCodeAndParameters(e, "prefix", "impacts")).containsOnly(
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Autre type de schéma : ", "5"),
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Troisième type de schéma : ", "6")
			);

			assertThat(frenchMessages(e.getValidationErrors().getValidationWarnings())).containsOnly(
					"Troisième type de schéma : 6 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements",
					"Autre type de schéma : 5 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements"
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(thirdSchema.type()).returnAll()))).isEqualTo(297);
	}

	@Test
	public void givenMultipleAlreadyImportedRecordsWhenImportingRecordsOfAnotherTypeThen()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata");

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineChildOfRelationshipToType(schemaTypes.getSchemaType("zeSchemaType"));
					}
				}));

		Transaction transaction = new Transaction().setOptimisticLockingResolution(EXCEPTION);
		for (int i = 1; i <= 4000; i++) {
			transaction.add(new TestRecord(zeSchema).set(Schemas.LEGACY_ID, "record" + i));
		}
		recordServices.execute(transaction);

		for (int i = 1; i <= 4000; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + i));
		}

		final AtomicInteger queriesCount = new AtomicInteger();

		RecordsCache cache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			}
		});

		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(SolrParams solrParams, long qtime) {
						queriesCount.incrementAndGet();
					}
				});

		services.bulkImport(importDataProvider, progressionListener, admin,
				new BulkImportParams().setImportErrorsBehavior(CONTINUE));

		assertThat(queriesCount.get()).isLessThan(4500);
	}

	@Test
	public void givenPrevalidationErrorsWhenImportingWithContinueErrorModeThenContinue()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").setDefaultRequirement(true);

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("refToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"));

						Map<Language, String> labels = new HashMap<Language, String>();
						labels.put(Language.French, "Autre type de schéma");
						schemaTypes.getSchemaType("anotherSchemaType").setLabels(labels);

						labels = new HashMap<Language, String>();
						labels.put(Language.French, "Troisième type de schéma");
						schemaTypes.getSchemaType("aThirdSchemaType").setLabels(labels);
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? null : "value"));
		}

		for (int i = 1; i <= 302; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + (i > 300 ? 142 : i)));
		}

		for (int i = 1; i <= 303; i++) {
			thirdSchemaTypeRecords.add(defaultSchemaData().setId("thirdSchemaRecord" + i)
					.addField("refToAnotherSchema", "anotherSchemaRecord" + (i > 302 ? 301 : i)));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					new BulkImportParams().setImportErrorsBehavior(CONTINUE)
							.setImportValidationErrorsBehavior(ImportValidationErrorsBehavior.EXCLUDE_THOSE_RECORDS));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServices_requiredValue", "244", "Ze type de schéma record244 : "),
					tuple("RecordsImportServices_requiredValue", "142", "Ze type de schéma record142 : "),
					tuple("RecordsImportServices_requiredValue", "188", "Ze type de schéma record188 : ")
			);

			assertThat(extractingWarningsSimpleCodeAndParameters(e, "prefix", "impacts")).containsOnly(
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Autre type de schéma : ", "5"),
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Troisième type de schéma : ", "6")
			);

			assertThat(frenchMessages(e.getValidationErrors().getValidationWarnings())).containsOnly(
					"Troisième type de schéma : 6 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements",
					"Autre type de schéma : 5 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements"
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(thirdSchema.type()).returnAll()))).isEqualTo(297);
	}

	@Test
	public void givenPrevalidationErrorsWhenImportingWithWarningForInvalidFacultativeMetadatasModeThenContinue()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").setInputMask("999 999 999");

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("refToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"));

						Map<Language, String> labels = new HashMap<Language, String>();
						labels.put(Language.French, "Autre type de schéma");
						schemaTypes.getSchemaType("anotherSchemaType").setLabels(labels);

						labels = new HashMap<Language, String>();
						labels.put(Language.French, "Troisième type de schéma");
						schemaTypes.getSchemaType("aThirdSchemaType").setLabels(labels);
					}
				}));

		AtomicInteger maskValidationCount = mockRecordServicesForCountingMaskValidations();

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "12  456 789" : "123 456 789"));
		}

		for (int i = 1; i <= 302; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + (i > 300 ? 142 : i)));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin, BulkImportParams.PERMISSIVE());

			fail("ValidationException expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).isEmpty();

			assertThat(extractingWarningsSimpleCodeAndParameters(e, "value", "prefix")).containsOnly(
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record244 : "),
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record142 : "),
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record188 : ")
			);

		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(300);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(302);
		assertThat(maskValidationCount.get()).isEqualTo(300);
	}

	@Test
	public void givenPrevalidationErrorsWhenImportingWithoutWarningForInvalidFacultativeMetadatasModeThenErrors()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").setInputMask("999 999 999");

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("refToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"));

						Map<Language, String> labels = new HashMap<Language, String>();
						labels.put(Language.French, "Autre type de schéma");
						schemaTypes.getSchemaType("anotherSchemaType").setLabels(labels);

						labels = new HashMap<Language, String>();
						labels.put(Language.French, "Troisième type de schéma");
						schemaTypes.getSchemaType("aThirdSchemaType").setLabels(labels);
					}
				}));

		AtomicInteger maskValidationCount = mockRecordServicesForCountingMaskValidations();

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "12  456 789" : "123 456 789"));
		}

		for (int i = 1; i <= 302; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + (i > 300 ? 142 : i)));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					BulkImportParams.PERMISSIVE().setWarningsForInvalidFacultativeMetadatas(false));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			assertThat(extractingWarningsSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", null, "Autre type de schéma : ")
			);

			assertThat(extractingSimpleCodeAndParameters(e, "value", "prefix")).containsOnly(
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record244 : "),
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record142 : "),
					tuple("MaskedMetadataValidator_valueIncompatibleWithSpecifiedMask", "12  456 789",
							"Ze type de schéma record188 : ")
			);

		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(maskValidationCount.get()).isEqualTo(300);
	}

	private AtomicInteger mockRecordServicesForCountingMaskValidations()
			throws ValidationException {
		final AtomicInteger maskValidatorCount = new AtomicInteger();
		RecordServicesImpl spiedRecordServices = spy(getModelLayerFactory().newCachelessRecordServices());
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				RecordValidationServices recordValidationServicesServices = spy(
						(RecordValidationServices) invocation.callRealMethod());
				doAnswer(new Answer() {
					@Override
					public Object answer(InvocationOnMock invocation)
							throws Throwable {
						final MaskedMetadataValidator maskedMetadataValidator = spy(
								(MaskedMetadataValidator) invocation.callRealMethod());
						doAnswer(new Answer() {
							@Override
							public Object answer(InvocationOnMock invocation)
									throws Throwable {
								maskValidatorCount.incrementAndGet();
								return invocation.callRealMethod();
							}
						}).when(maskedMetadataValidator)
								.validateMetadata(any(ValidationErrors.class), any(Metadata.class), anyObject());
						return maskedMetadataValidator;
					}
				}).when(recordValidationServicesServices).newMaskedMetadataValidator(any(List.class));
				return recordValidationServicesServices;
			}
		}).when(spiedRecordServices).newRecordValidationServices(any(RecordProvider.class));
		services.recordServices = spiedRecordServices;
		services = spy(services);
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				RecordsImportServicesExecutor executor = spy((RecordsImportServicesExecutor) invocation.callRealMethod());
				doAnswer(new Answer() {
					@Override
					public Object answer(InvocationOnMock invocation)
							throws Throwable {
						maskValidatorCount.incrementAndGet();
						return invocation.callRealMethod();
					}
				}).when(executor).validateMask(any(Metadata.class), anyObject(), any(DecoratedValidationsErrors.class));

				return executor;
			}
		}).when(services)
				.newExecutor(any(ImportDataProvider.class), any(BulkImportProgressionListener.class), any(User.class),
						any(List.class), any(BulkImportParams.class));
		return maskValidatorCount;
	}

	@Test
	public void whenImportingWithMultipleThreadsThenContinueOnErrors()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getMetadata("zeSchemaType_default_stringMetadata").defineValidators()
								.add(NoZMetadataValidator.class);

						schemaTypes.getSchemaType("anotherSchemaType").getDefaultSchema().create("refToZeSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("refToAnotherSchema")
								.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"));

						schemaTypes.getSchemaType("aThirdSchemaType").getDefaultSchema().create("content").setType(CONTENT);

						Map<Language, String> labels = new HashMap<Language, String>();
						labels.put(Language.French, "Autre type de schéma");
						schemaTypes.getSchemaType("anotherSchemaType").setLabels(labels);

						labels = new HashMap<Language, String>();
						labels.put(Language.French, "Troisième type de schéma");
						schemaTypes.getSchemaType("aThirdSchemaType").setLabels(labels);
					}
				}));

		for (int i = 1; i <= 300; i++) {
			zeSchemaTypeRecords.add(defaultSchemaData().setId("record" + i)
					.addField("stringMetadata", (i == 142 || i == 188 || i == 244) ? "problem" : "value"));
		}

		for (int i = 1; i <= 302; i++) {
			anotherSchemaTypeRecords.add(defaultSchemaData().setId("anotherSchemaRecord" + i)
					.addField("refToZeSchema", "record" + (i > 300 ? 142 : i)));
		}

		for (int i = 1; i <= 303; i++) {
			thirdSchemaTypeRecords.add(defaultSchemaData().setId("thirdSchemaRecord" + i)
					.addField("refToAnotherSchema", "anotherSchemaRecord" + (i > 302 ? 301 : i)));
		}

		try {
			services.bulkImport(importDataProvider, progressionListener, admin,
					new BulkImportParams().setImportErrorsBehavior(CONTINUE).setThreads(5));

			fail("ValidationException expected");
		} catch (ValidationException e) {
			e.printStackTrace();
			assertThat(extractingSimpleCodeAndParameters(e, "index", "prefix")).containsOnly(
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "142", "Ze type de schéma record142 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "188", "Ze type de schéma record188 : "),
					tuple("RecordsImportServicesRealTest$NoZMetadataValidator_noP", "244", "Ze type de schéma record244 : ")
			);

			assertThat(extractingWarningsSimpleCodeAndParameters(e, "prefix", "impacts")).containsOnly(
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Autre type de schéma : ", "5"),
					tuple("SkippedRecordsImport_skipBecauseDependenceFailed", "Troisième type de schéma : ", "6")
			);

			assertThat(frenchMessages(e.getValidationErrors().getValidationWarnings())).containsOnly(
					"Troisième type de schéma : 6 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements",
					"Autre type de schéma : 5 enregistrements n'ont pu être importés à cause d'erreurs avec d'autres enregistrements"
			);
		}

		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(zeSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(anotherSchema.type()).returnAll()))).isEqualTo(297);
		assertThat(searchServices.getResultsCount(new LogicalSearchQuery(from(thirdSchema.type()).returnAll()))).isEqualTo(297);
	}

	@Test
	public void givenRecordPreparationErrorsThenValidationExceptionWithStackTrace1()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());

		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
					RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
				throw new RuntimeException("Mouhahahah!");
			}
		});

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);

			fail("exception expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getValidationErrors(), "message")).containsOnly(
					tuple("RecordsImportServices_recordPreparationError", "Mouhahahah!")
			);
			assertThat(e.getValidationErrors().getValidationErrors().get(0).getParameters()).containsKey("stacktrace");
			assertThat(frenchMessages(e).get(0)).startsWith(
					"Ze type de schéma 2 : Une erreur est survenue durant la préparation/validation de l'enregistrement : java.lang.RuntimeException: Mouhahahah!");
		}
	}

	@Test
	public void givenRecordPreparationErrorsThenValidationExceptionWithStackTrace2()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata());

		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {
			@Override
			public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
				throw new RuntimeException("Mouhahahah!");
			}
		});

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		try {
			bulkImport(importDataProvider, progressionListener, admin);

			fail("exception expected");
		} catch (ValidationException e) {
			assertThat(extractingSimpleCodeAndParameters(e.getValidationErrors(), "message")).containsOnly(
					tuple("RecordsImportServices_recordPreparationError", "Mouhahahah!")
			);
			assertThat(e.getValidationErrors().getValidationErrors().get(0).getParameters()).containsKey("stacktrace");
			assertThat(frenchMessages(e).get(0)).startsWith(
					"Ze type de schéma 2 : Une erreur est survenue durant la préparation/validation de l'enregistrement : java.lang.RuntimeException: Mouhahahah!");
		}
	}

	@Test
	public void givenAnImportDataHasALogicallyDeletedReferenceThenImported()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema().withAReferenceMetadata(whichIsReferencing("anotherSchemaType")));

		Record logicallyDeletedRecord = new TestRecord(anotherSchema).set(TITLE, "Logically deleted record").set(LEGACY_ID, "1");

		recordServices.add(logicallyDeletedRecord);
		recordServices.logicallyDelete(logicallyDeletedRecord, User.GOD);
		String logicallyDeletedRecordId = logicallyDeletedRecord.getId();

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2")
				.addField(zeSchema.referenceMetadata().getLocalCode(), "1"));

		bulkImport(importDataProvider, progressionListener, admin);
		Record importedRecord = recordWithLegacyId("2");
		String referencedRecordId = importedRecord.get(zeSchema.referenceMetadata());
		assertThat(referencedRecordId).isEqualTo(logicallyDeletedRecordId);
	}

	private void validateCorrectlyImported() {
		Record record43 = recordWithLegacyId("43");
		Record record42 = recordWithLegacyId("42");
		Record record3 = recordWithLegacyId("3");
		Record record2 = recordWithLegacyId("2");
		Record record1 = recordWithLegacyId("1");

		Record record666 = recordWithLegacyId("666");

		assertThat(record1.getId()).isNotEqualTo("1");
		assertThat(record1.<String>get(LEGACY_ID)).isEqualTo("1");
		assertThat(record1.<String>get(TITLE)).isEqualTo("Record 1");
		assertThat(record1.<String>get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record2.getId());

		assertThat(record2.getId()).isNotEqualTo("2");
		assertThat(record2.<String>get(LEGACY_ID)).isEqualTo("2");
		assertThat(record2.<String>get(TITLE)).isEqualTo("Record 2");
		assertThat(record2.<String>get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record3.getId());

		assertThat(record3.getId()).isNotEqualTo("3");
		assertThat(record3.<String>get(LEGACY_ID)).isEqualTo("3");
		assertThat(record3.<String>get(TITLE)).isEqualTo("Record 3");
		assertThat(record3.<String>get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record42.getId());

		assertThat(record42.getId()).isNotEqualTo("42");
		assertThat(record42.<String>get(LEGACY_ID)).isEqualTo("42");
		assertThat(record42.<String>get(TITLE)).isEqualTo("Record 42");
		assertThat(record42.<String>get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo("previouslySavedRecordId");

		assertThat(record43.getId()).isNotEqualTo("43");
		assertThat(record43.<String>get(LEGACY_ID)).isEqualTo("43");
		assertThat(record43.<String>get(TITLE)).isEqualTo("Record 43");
		assertThat(record43.<String>get(zeCustomSchemaMetadatas.customStringMetadata())).isEqualTo("customFieldValue");
		assertThat(record43.<String>get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isNull();
		assertThat(record43.<String>getSchemaCode()).isEqualTo("zeSchemaType_custom");

		assertThat(record666.getId()).isNotEqualTo("666");
		assertThat(record666.<String>get(LEGACY_ID)).isEqualTo("666");
		assertThat(record666.<String>get(TITLE)).isEqualTo("Ze record");
		assertThat(record666.<String>get(anotherSchema.referenceFromAnotherSchemaToZeSchema())).isEqualTo(record1.getId());
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

	private void bulkImport(ImportDataProvider importDataProvider,
							final BulkImportProgressionListener bulkImportProgressionListener, final User user)
			throws ValidationException {
		bulkImport(importDataProvider, bulkImportProgressionListener, user, new BulkImportParams());
	}

	private void bulkImport(ImportDataProvider importDataProvider,
							final BulkImportProgressionListener bulkImportProgressionListener,
							final User user, BulkImportParams params)
			throws ValidationException {
		params.setImportErrorsBehavior(CONTINUE_FOR_RECORD_OF_SAME_TYPE);
		services.bulkImport(importDataProvider, bulkImportProgressionListener, user, params);

		zeSchemaTypeRecords.clear();
		anotherSchemaTypeRecords.clear();
		thirdSchemaTypeRecords.clear();

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

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3,
									  String value3) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3,
									  String value3,
									  String key4, String value4) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		parameters.put(key3, value3);
		parameters.put(key4, value4);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2, String key3,
									  String value3,
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
