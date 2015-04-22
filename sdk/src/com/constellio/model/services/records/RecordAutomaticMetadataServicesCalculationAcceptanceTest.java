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
package com.constellio.model.services.records;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordAutomaticMetadataServicesCalculationAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;

	RecordAutomaticMetadataServices services;

	RecordProvider recordProvider;

	String idReferencedRecordWithJan1DateValue, idReferencedRecordWithJan2DateValue, idReferencedRecordWithoutDateValue;
	RecordImpl record;

	LocalDateTime jan1 = new LocalDateTime(2014, 1, 1, 0, 0);
	LocalDateTime jan2 = new LocalDateTime(2014, 1, 2, 0, 0);
	LocalDateTime jan3 = new LocalDateTime(2014, 1, 3, 0, 0);

	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;

	@Mock Metadata firstReindexedMetadata, secondReindexedMetadata;
	TransactionRecordsReindexation reindexedMetadata;

	@Before
	public void setUp() {

		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();

		record = new TestRecord(zeSchema);

		services = new RecordAutomaticMetadataServices(getModelLayerFactory().getMetadataSchemasManager(),
				getModelLayerFactory().getTaxonomiesManager(), getModelLayerFactory().getSystemConfigurationsManager());

		recordServices = spy(getModelLayerFactory().newRecordServices());
		recordProvider = recordServices.newRecordProvider(null, new Transaction());

		DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.set(0);

		reindexedMetadata = new TransactionRecordsReindexation(Arrays.asList(firstReindexedMetadata, secondReindexedMetadata));
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordWhenUpdatingThenCalculateValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(2.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordWithMultivaluesWhenUpdatingThenCalculateValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(true);
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithJan1DateValue));
		record.set(zeSchema.dateTimeMetadata(), asList(jan1, jan2));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordMissingRequiredReferenceDependencyWhenUpdatingThenCalculatedValueIsDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithoutDateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordMissingRequiredLocalDependencyWhenUpdatingThenCalculatedValueIsDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), null);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	@Test
	public void givenExistingRecordDependenciesOfCalculatedMetadataNotModifiedWhenUpdatingThenNotRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(2.0);
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenExistingRecordReferenceDependencyOfCalculatedMetadataModifiedWhenUpdatingThenRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan2DateValue);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(2);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenExistingRecordLocalDependencyOfCalculatedMetadataModifiedWhenUpdatingThenRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.dateTimeMetadata(), jan2);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(2);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenExistingRecordReferenceDependencyOfCalculatedMetadataRemovedWhenUpdatingThenReplaceWithDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.secondReferenceToAnotherSchema(), null);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
		verifyZeroInteractions(recordServices);

	}

	@Test
	public void givenExistingRecordLocalDependencyOfCalculatedMetadataRemovedWhenUpdatingThenReplaceWithDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.dateTimeMetadata(), null);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata);

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);

	}

	private void add()
			throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(record);
	}

	protected void givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(boolean multivalue)
			throws Exception {
		defineSchemasManager().using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(multivalue));
		addRecords();
	}

	private void addRecords()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), jan1);
		recordServices.add(record);
		idReferencedRecordWithJan1DateValue = record.getId();

		record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), jan2);
		recordServices.add(record);
		idReferencedRecordWithJan2DateValue = record.getId();

		record = new TestRecord(anotherSchema);
		recordServices.add(record);
		idReferencedRecordWithoutDateValue = record.getId();

		reset(recordServices);
	}
}
