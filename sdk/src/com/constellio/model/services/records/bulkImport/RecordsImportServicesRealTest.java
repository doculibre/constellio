/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records.bulkImport;

import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.services.records.bulkImport.RecordsImportServices.INVALID_SCHEMA_TYPE_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.AUTOMATIC_METADATA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.DISABLED_METADATA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_BOOLEAN_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_DATETIME_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_DATE_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_ENUM_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_METADATA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_MULTIVALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_NUMBER_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_RESOLVER_METADATA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_SCHEMA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_SINGLEVALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.INVALID_STRING_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.LEGACY_ID_LOCAL_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.LEGACY_ID_NOT_UNIQUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.REQUIRED_IDS;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.REQUIRED_VALUE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.SYSTEM_RESERVED_METADATA_CODE;
import static com.constellio.model.services.records.bulkImport.RecordsImportValidator.UNRESOLVED_VALUE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSystemReserved;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnique;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionEventsListeners;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.bulkImport.RecordsImportServicesRuntimeException.RecordsImportServicesRuntimeException_CyclicDependency;
import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.builder.ImportDataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
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

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

	User admin;

	RecordImportExtension firstImportBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void validate(ImportDataErrors errors, ImportData importData) {
			Object stringMetadataObjectValue = importData.getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("z")) {
					errors.error("noZ", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void prevalidate(ImportDataErrors errors, ImportData importData) {
			Object stringMetadataObjectValue = importData.getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("toto")) {
					errors.error("noToto", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void build(Record record, MetadataSchemaTypes types, ImportData importData) {
			Map<String, String> structureFields = (Map<String, String>) importData.getFields().get("structureMetadata");

			if (structureFields != null && structureFields.containsKey("zeTitle")) {
				record.set(Schemas.TITLE, structureFields.get("zeTitle"));
			}
		}
	};
	RecordImportExtension secondImportBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void prevalidate(ImportDataErrors errors, ImportData importData) {
			Object stringMetadataObjectValue = importData.getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("tata")) {
					errors.error("noTata", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void validate(ImportDataErrors errors, ImportData importData) {
			Object stringMetadataObjectValue = importData.getFields().get("stringMetadata");
			if (stringMetadataObjectValue != null && stringMetadataObjectValue instanceof String) {
				String stringMetadataValue = ((String) stringMetadataObjectValue);
				if (stringMetadataValue.contains("y")) {
					errors.error("noY", asMap("zevalue", stringMetadataValue));
				}
			}
		}

		@Override
		public void build(Record record, MetadataSchemaTypes types, ImportData importData) {
		}
	};

	RecordImportExtension otherTypeBehavior = new RecordImportExtension() {

		@Override
		public String getDecoratedSchemaType() {
			return "anotherInexistentType";
		}

		@Override
		public void prevalidate(ImportDataErrors errors, ImportData importData) {
			errors.error("boom", asMap("zevalue", "boom boom"));
		}

		@Override
		public void validate(ImportDataErrors errors, ImportData importData) {
			errors.error("boom", asMap("zevalue", "boom boom"));
		}

		@Override
		public void build(Record record, MetadataSchemaTypes types, ImportData importData) {
			throw new Error("Should not be called");
		}
	};

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withAllTestUsers();
		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		data.put(zeSchema.typeCode(), zeSchemaTypeRecords);
		data.put(anotherSchema.typeCode(), anotherSchemaTypeRecords);
		data.put(thirdSchema.typeCode(), thirdSchemaTypeRecords);
		importDataProvider = new DummyImportDataProvider(data);

		contentManager = getModelLayerFactory().getContentManager();
		services = new RecordsImportServices(getModelLayerFactory());

		extensions = getModelLayerFactory().getExtensions();
		extensions.getCollectionListeners(zeCollection).recordImportBehaviors.add(firstImportBehavior);
		extensions.getCollectionListeners(zeCollection).recordImportBehaviors.add(secondImportBehavior);
		extensions.getCollectionListeners(zeCollection).recordImportBehaviors.add(otherTypeBehavior);
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
		assertThat(record.get(Schemas.TITLE)).isEqualTo("new title 2");
		assertThat((Boolean) record.get(zeSchema.booleanMetadata())).isFalse();
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(anotherDate);
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(anotherDateTime);
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(7.77);
		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(AValidEnum.FIRST_VALUE);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "new title 3"));
		bulkImport(importDataProvider, progressionListener, admin);
		record = recordWithLegacyId("1");
		assertThat(record.get(LEGACY_ID)).isEqualTo("1");
		assertThat(record.get(Schemas.TITLE)).isEqualTo("new title 3");
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
		assertThat(record.get(Schemas.TITLE)).isEqualTo("new title 3");
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
		assertThat(record.get(Schemas.TITLE)).isEqualTo("Record 1");
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
		assertThat(record.get(Schemas.TITLE)).isEqualTo("Record 1");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(asList(true, false));
		assertThat(record.get(zeSchema.dateMetadata())).isEqualTo(asList(aDate, anotherDate));
		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(asList(anotherDateTime, aDateTime));
		assertThat(record.get(zeSchema.numberMetadata())).isEqualTo(asList(6.66, 42.0));
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
			assertThat(record.get(Schemas.TITLE)).isEqualTo("Record " + i);
			assertThat((Boolean) record.get(zeSchema.booleanMetadata())).describedAs("Record " + i + " should be true")
					.isTrue();
		}

		for (int i = 14; i <= 19; i++) {
			Record record = recordWithLegacyId("" + i);
			assertThat(record).describedAs("Record " + i + " should exist").isNotNull();
			assertThat(record.get(LEGACY_ID)).isEqualTo("" + i);
			assertThat(record.get(Schemas.TITLE)).isEqualTo("Record " + i);
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
		};

		try {
			bulkImport(importDataProvider, progressionListener, admin);
			fail("An exception was expected");
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newValidationError(INVALID_SCHEMA_TYPE_CODE, asMap("schemaType", "chuckNorris")),
					newValidationError(INVALID_SCHEMA_TYPE_CODE, asMap("schemaType", anotherSchema.typeCode() + "s")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(REQUIRED_IDS, asMap("index", "2")),
					newZeSchemaValidationError(REQUIRED_IDS, asMap("index", "4")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(UNRESOLVED_VALUE, asMap("legacyIdentifier", "[42, 666]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(UNRESOLVED_VALUE, asMap("legacyIdentifier", "[42, 666]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(UNRESOLVED_VALUE, asMap("stringMetadata", "[42, 666]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_RESOLVER_METADATA_CODE,
							asMap("index", "3", "legacyId", "4", "metadata", "invalidMetadata")),
					newZeSchemaValidationError(INVALID_RESOLVER_METADATA_CODE,
							asMap("index", "4", "legacyId", "5", "metadata", "otherInvalidMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(REQUIRED_VALUE,
							asMap("index", "2", "legacyId", "3", "metadatas", "[booleanMetadata]")),
					newZeSchemaValidationError(REQUIRED_VALUE,
							asMap("index", "3", "legacyId", "4", "metadatas", "[stringMetadata]")),
					newZeSchemaValidationError(REQUIRED_VALUE,
							asMap("index", "4", "legacyId", "5", "metadatas", "[booleanMetadata, stringMetadata]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_BOOLEAN_VALUE,
							asMap("index", "2", "legacyId", "5", "invalidValue", "Oui monsieur", "metadata",
									"booleanMetadata")),
					newZeSchemaValidationError(INVALID_BOOLEAN_VALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "Oh yes", "metadata", "booleanMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_ENUM_VALUE,
							asMap("index", "1", "legacyId", "3", "invalidValue", "SECOND_VALUE", "metadata",
									"withAnEnumMetadata", "availableChoices", "[F, S]")),
					newZeSchemaValidationError(INVALID_ENUM_VALUE,
							asMap("index", "3", "legacyId", "5", "invalidValue", "FS", "metadata", "withAnEnumMetadata",
									"availableChoices", "[F, S]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_ENUM_VALUE,
							asMap("index", "1", "legacyId", "3", "invalidValue", "[S, SECOND_VALUE]", "metadata",
									"withAnEnumMetadata", "availableChoices", "[F, S]")),
					newZeSchemaValidationError(INVALID_ENUM_VALUE,
							asMap("index", "3", "legacyId", "5", "invalidValue", "[FS, F]", "metadata", "withAnEnumMetadata",
									"availableChoices", "[F, S]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(DISABLED_METADATA_CODE,
							asMap("index", "2", "legacyId", "3", "metadata", "stringMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(SYSTEM_RESERVED_METADATA_CODE,
							asMap("index", "2", "legacyId", "3", "metadata", "stringMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(AUTOMATIC_METADATA_CODE,
							asMap("index", "2", "legacyId", "3", "metadata", "copiedStringMeta")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_NUMBER_VALUE,
							asMap("index", "2", "legacyId", "5", "invalidValue", "5L", "metadata", "numberMetadata")),
					newZeSchemaValidationError(INVALID_NUMBER_VALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "5.0t", "metadata", "numberMetadata")),
					newZeSchemaValidationError(INVALID_NUMBER_VALUE,
							asMap("index", "4", "legacyId", "7", "invalidValue", "nan", "metadata", "numberMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_STRING_VALUE,
							asMap("index", "2", "legacyId", "5", "metadata", "stringMetadata", "invalidValue",
									aDate.toString())),
					newZeSchemaValidationError(INVALID_STRING_VALUE,
							asMap("index", "3", "legacyId", "6", "metadata", "stringMetadata", "invalidValue",
									aDateTime.toString())));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_STRING_VALUE,
							asMap("index", "2", "legacyId", "5", "metadata", "stringMetadata", "invalidValue",
									"[validValue, " + aDate.toString() + "]")),
					newZeSchemaValidationError(INVALID_STRING_VALUE,
							asMap("index", "3", "legacyId", "6", "metadata", "stringMetadata", "invalidValue",
									"[" + aDate.toString() + ", " + aDateTime.toString() + "]")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_DATE_VALUE,
							asMap("index", "2", "legacyId", "5", "invalidValue", aDateTime.toString(), "metadata",
									"dateMetadata")),
					newZeSchemaValidationError(INVALID_DATE_VALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "a text value", "metadata",
									"dateMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_DATETIME_VALUE,
							asMap("index", "1", "legacyId", "4", "invalidValue", aDate.toString(), "metadata",
									"dateTimeMetadata")),
					newZeSchemaValidationError(INVALID_DATETIME_VALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "a text value", "metadata",
									"dateTimeMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_MULTIVALUE,
							asMap("index", "2", "legacyId", "5", "invalidValue", "aValue", "metadata", "stringMetadata")),
					newZeSchemaValidationError(INVALID_MULTIVALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "anotherValue", "metadata", "stringMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_SINGLEVALUE,
							asMap("index", "1", "legacyId", "4", "invalidValue", "[aValue]", "metadata", "stringMetadata")),
					newZeSchemaValidationError(INVALID_SINGLEVALUE,
							asMap("index", "3", "legacyId", "6", "invalidValue", "[anotherValue, thirdValue]", "metadata",
									"stringMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_METADATA_CODE,
							asMap("index", "2", "legacyId", "5", "metadata", "zeChuckNorrisMetadata")),
					newZeSchemaValidationError(INVALID_METADATA_CODE,
							asMap("index", "3", "legacyId", "6", "metadata", "anInexistentMetadata")),
					newZeSchemaValidationError(INVALID_METADATA_CODE,
							asMap("index", "3", "legacyId", "6", "metadata", "anotherInexistentMetadata")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError(INVALID_SCHEMA_CODE,
							asMap("index", "1", "legacyId", "4", "schema", "anInvalidSchema")),
					newZeSchemaValidationError(INVALID_SCHEMA_CODE,
							asMap("index", "5", "legacyId", "8", "schema", "anotherSchemaType_default")));
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
		} catch (RecordsImportServicesRuntimeException_CyclicDependency e) {
			assertThat(e.getCyclicDependentIds()).containsOnly("1", "2", "3", "4");
		}
	}

	@Test
	public void whenValidatingThenBuildAMappingOfLegacyId()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(Schemas.TITLE, "title"));

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
		ResolverCache resolver = new ResolverCache(getModelLayerFactory().newSearchServices(), types);

		ProgressionHandler progressionHandler = new ProgressionHandler(new LoggerBulkImportProgressionListener());
		ModelLayerCollectionEventsListeners extensions = getModelLayerFactory().getExtensions()
				.getCollectionListeners(zeCollection);
		services.validate(importDataProvider, progressionHandler, types, resolver, extensions);

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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(newZeSchemaValidationError(LEGACY_ID_NOT_UNIQUE, asMap("legacyId", "42")));
		}

	}

	@Test
	public void whenImportingRecordsWithInvalidContentThenImportRecordsAndReturnWarnings()
			throws Exception {
		String testResource1 = getTestResourceFile("resource1.docx").getAbsolutePath().replace(".docx", ".dodocx");
		String testResource2 = getTestResourceFile("resource2.pdf").getAbsolutePath();
		String testResource3 = "http://www.perdu.com/edouardLechat.pdf";
		String testResource2Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new ContentImport(testResource1, "Ze document.docx", true)));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new ContentImport(testResource2, "Ze ultimate document.pdf", false),
						new ContentImport(testResource3, "Ze book.txt", true))));

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
	public void whenImportingRecordsWithContentThenContentUploadedAndAddedToRecord()
			throws Exception {
		String testResource1 = getTestResourceFile("resource1.docx").getAbsolutePath();
		String testResource2 = getTestResourceFile("resource2.pdf").getAbsolutePath();
		String testResource3 = "https://dl.dropboxusercontent.com/u/422508/pg338.txt";
		String testResource4 = getTestResourceFile("resource4.docx").getAbsolutePath();
		String testResource5 = getTestResourceFile("resource5.pdf").getAbsolutePath();
		String testResource1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
		String testResource2Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
		String testResource3Hash = "jLWaqQbCOSAPT4G3P75XnJJOmmo=";
		String testResource4Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";
		String testResource5Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAContentMetadata()
				.withAContentListMetadata());

		ContentImportVersion version1 = new ContentImportVersion(testResource1, "Ze document.docx", true);
		ContentImportVersion version2 = new ContentImportVersion(testResource4, "Ze document.docx", false);
		ContentImportVersion version3 = new ContentImportVersion(testResource5, "Ze document.docx", true);

		zeSchemaTypeRecords.add(defaultSchemaData().setId("1").addField("title", "Record 1")
				.addField("contentMetadata", new ContentImport(Arrays.asList(version1, version2, version3))));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("2").addField("title", "Record 2"));

		zeSchemaTypeRecords.add(defaultSchemaData().setId("3").addField("title", "Record 3")
				.addField("contentListMetadata", asList(
						new ContentImport(testResource2, "Ze ultimate document.pdf", false),
						new ContentImport(testResource3, "Ze book.txt", true))));

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

		assertThat(contentManager.getParsedContent(testResource1Hash).getParsedContent()).contains("My document");
		assertThat(contentManager.getParsedContent(testResource2Hash).getParsedContent()).contains("Gestion des documents");
		assertThat(contentManager.getParsedContent(testResource3Hash).getParsedContent())
				.contains("He is your friend, but his arrow will kill one of your kind! He is a\r\nDakota");//\nDakota");

		assertThat(results.getInvalidIds()).isEmpty();

	}

	@Test
	public void whenImportingRecordsWithReferencesInsideSameSchemaThenIterateMultipleTimeOverSameFile()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadataInCustomSchema()
				.withAParentReferenceFromZeSchemaToZeSchema()
				.withAReferenceFromAnotherSchemaToZeSchema());

		getModelLayerFactory().newRecordServices().add(new TestRecord(zeSchema, "previouslySavedRecordId")
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(Schemas.TITLE, "title"));

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
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(Schemas.TITLE, "title"));

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
				.set(LEGACY_ID, "previouslySavedRecordLegacyId").set(Schemas.TITLE, "title")
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError("noY", asMap("index", "2", "legacyId", "12", "zevalue", "Value with a y")));
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
		} catch (ValidationRuntimeException e) {
			List<ValidationError> errors = e.getValidationErrors().getValidationErrors();
			assertThat(errors).containsOnly(
					newZeSchemaValidationError("noTata", asMap("index", "2", "legacyId", "12", "zevalue", "Value with a tata")),
					newZeSchemaValidationError("noToto", asMap("index", "3", "legacyId", "13", "zevalue", "Value with a toto")));
		}
	}

	@Test
	public void whenImportingThenRunDecoratorBuild()
			throws Exception {

		defineSchemasManager().using(schemas.andCustomSchema()
				.withAStringMetadata(whichIsUnique)
				.withAStringMetadataInCustomSchema()
				.withAStructureMetadata());

		Map<String, String> record11Structure = asMap("zeTitle", "pouet");
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
		assertThat(record1.get(Schemas.TITLE)).isEqualTo("pouet");

		Record record2 = recordWithLegacyId("12");
		assertThat(record2.get(Schemas.TITLE)).isEqualTo("Record 2");

		Record record3 = recordWithLegacyId("13");
		assertThat(record3.get(Schemas.TITLE)).isEqualTo("Record 3");
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
		assertThat(record1.get(Schemas.TITLE)).isEqualTo("Record 1");
		assertThat(record1.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record2.getId());

		assertThat(record2.getId()).isNotEqualTo("2");
		assertThat(record2.get(LEGACY_ID)).isEqualTo("2");
		assertThat(record2.get(Schemas.TITLE)).isEqualTo("Record 2");
		assertThat(record2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record3.getId());

		assertThat(record3.getId()).isNotEqualTo("3");
		assertThat(record3.get(LEGACY_ID)).isEqualTo("3");
		assertThat(record3.get(Schemas.TITLE)).isEqualTo("Record 3");
		assertThat(record3.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo(record42.getId());

		assertThat(record42.getId()).isNotEqualTo("42");
		assertThat(record42.get(LEGACY_ID)).isEqualTo("42");
		assertThat(record42.get(Schemas.TITLE)).isEqualTo("Record 42");
		assertThat(record42.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isEqualTo("previouslySavedRecordId");

		assertThat(record43.getId()).isNotEqualTo("43");
		assertThat(record43.get(LEGACY_ID)).isEqualTo("43");
		assertThat(record43.get(Schemas.TITLE)).isEqualTo("Record 43");
		assertThat(record43.get(zeCustomSchemaMetadatas.customStringMetadata())).isEqualTo("customFieldValue");
		assertThat(record43.get(zeSchema.parentReferenceFromZeSchemaToZeSchema())).isNull();
		assertThat(record43.getSchemaCode()).isEqualTo("zeSchemaType_custom");

		assertThat(record666.getId()).isNotEqualTo("666");
		assertThat(record666.get(LEGACY_ID)).isEqualTo("666");
		assertThat(record666.get(Schemas.TITLE)).isEqualTo("Ze record");
		assertThat(record666.get(anotherSchema.referenceFromAnotherSchemaToZeSchema())).isEqualTo(record1.getId());
	}

	private ImportDataBuilder defaultSchemaData() {
		return new ImportDataBuilder().setSchema("default");
	}

	private Record recordWithLegacyId(String legacyId) {
		return getModelLayerFactory().newSearchServices().searchSingleResult(
				fromAllSchemasIn(zeCollection).where(LEGACY_ID).isEqualTo(legacyId));
	}

	private ValidationError newZeSchemaValidationError(String code, Map<String, String> parameters) {
		parameters.put("schemaType", zeSchema.typeCode());
		return new ValidationError(RecordsImportServices.class.getName() + "_" + code, parameters);
	}

	private ValidationError newValidationError(String code, Map<String, String> parameters) {
		return new ValidationError(RecordsImportServices.class.getName() + "_" + code, parameters);
	}

	private BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user) {
		BulkImportResults results = services.bulkImport(importDataProvider, bulkImportProgressionListener, user);

		zeSchemaTypeRecords.clear();
		anotherSchemaTypeRecords.clear();
		thirdSchemaTypeRecords.clear();

		return results;
	}

}
