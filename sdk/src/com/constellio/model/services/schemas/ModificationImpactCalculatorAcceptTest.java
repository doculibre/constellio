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
package com.constellio.model.services.schemas;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;

public class ModificationImpactCalculatorAcceptTest extends ConstellioTest {

	List<Metadata> alreadyReindexedMetadata;

	RecordServices recordServices;

	MetadataSchemasManager schemasManager;

	ModificationImpactCalculator impactCalculator;

	ModificationImpactCalculatorAcceptSetup schemas =
			new ModificationImpactCalculatorAcceptSetup();

	ModificationImpactCalculatorAcceptSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	ModificationImpactCalculatorAcceptSetup.AnotherSchemaMetadatas anotherSchemaType = schemas.new AnotherSchemaMetadatas();
	ModificationImpactCalculatorAcceptSetup.ThirdSchemaMetadatas thirdSchemaType = schemas.new ThirdSchemaMetadatas();

	String aString = aString();
	String aNewString = aString();

	LocalDateTime aDate = aDateTime();
	LocalDateTime aNewDate = aDateTime();

	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		searchServices = spy(getModelLayerFactory().newSearchServices());
		alreadyReindexedMetadata = new ArrayList<>();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = spy(getModelLayerFactory().newRecordServices());

		defineSchemasManager().using(schemas.withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas());

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
		impactCalculator = new ModificationImpactCalculator(types, taxonomies, searchServices);
	}

	@Test
	public void givenUnsavedRecordWhenCalculatingModificationImpactThenNothing()
			throws Exception {
		Record newRecord = newRecordWithAStringAndADate();

		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(newRecord), false);

		assertThat(impacts).isEmpty();
	}

	@Test
	public void givenSavedWithModificationsInMetadataNotUsedInOtherSchemasWhenCalculatingModificationImpactThenNothing()
			throws Exception {
		Record record = newRecordWithAStringAndADate();
		recordServices.update(record);
		record.set(zeSchema.booleanMetadata(), true);

		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), false);

		assertThat(impacts).isEmpty();
	}

	@Test
	public void givenRecordWithModificationsInMetadatasUsedInOtherSchemasWhenCalculatingModificationImpactHandlingInSameTransactionThenRequireReindexations()
			throws Exception {
		Record record = newRecordWithAStringAndADate();
		Record anotherSchemaRecord = newAnotherSchemaRecordUsing(record);
		Record thirdSchemaRecord = newThirdSchemaRecordUsing(record);
		recordServices.execute(new Transaction(thirdSchemaRecord, anotherSchemaRecord, record));

		record.set(zeSchema.stringMetadata(), aNewString);
		record.set(zeSchema.dateTimeMetadata(), aNewDate);

		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), false);

		assertThat(impacts).hasSize(2);

		ModificationImpact anotherSchemaImpact;
		ModificationImpact thirdSchemaImpact;

		if (impacts.get(0).getMetadataToReindex().iterator().next().getCode().startsWith("another")) {
			anotherSchemaImpact = impacts.get(0);
			thirdSchemaImpact = impacts.get(1);
		} else {
			anotherSchemaImpact = impacts.get(1);
			thirdSchemaImpact = impacts.get(0);
		}

		LogicalSearchCondition anotherSchemaCondition = from(anotherSchemaType.type())
				.whereAny(asList(anotherSchemaType.reference1ToZeSchema(), anotherSchemaType.reference2ToZeSchema()))
				.isIn(asList(record)).andWhere(Schemas.IDENTIFIER)
				.isNotIn(asList(record.getId()));
		assertThat(anotherSchemaImpact).isEqualTo(
				new ModificationImpact(anotherSchemaType.metadataUsingZeSchemaDateAndString(), anotherSchemaCondition));

		LogicalSearchCondition thirdSchemaCondition = from(thirdSchemaType.type())
				.whereAny(asList(thirdSchemaType.referenceToZeSchema())).isIn(asList(record))
				.andWhere(Schemas.IDENTIFIER)
				.isNotIn(asList(record.getId()));

		assertThat(thirdSchemaImpact).isEqualTo(
				new ModificationImpact(thirdSchemaType.metadataUsingZeSchemaDateAndString(), thirdSchemaCondition));

	}

	@Test
	public void givenRecordWithModificationsInMetadatasUsedInOtherSchemasWhenCalculatingModificationImpactHandlingInFutureTransactionsThenRequireReindexations()
			throws Exception {
		Record record = newRecordWithAStringAndADate();
		Record anotherSchemaRecord = newAnotherSchemaRecordUsing(record);
		Record thirdSchemaRecord = newThirdSchemaRecordUsing(record);
		recordServices.execute(new Transaction(thirdSchemaRecord, anotherSchemaRecord, record));

		record.set(zeSchema.stringMetadata(), aNewString);
		record.set(zeSchema.dateTimeMetadata(), aNewDate);

		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertThat(impacts).hasSize(2);

		ModificationImpact anotherSchemaImpact;
		ModificationImpact thirdSchemaImpact;

		if (impacts.get(0).getMetadataToReindex().iterator().next().getCode().startsWith("another")) {
			anotherSchemaImpact = impacts.get(0);
			thirdSchemaImpact = impacts.get(1);
		} else {
			anotherSchemaImpact = impacts.get(1);
			thirdSchemaImpact = impacts.get(0);
		}

		LogicalSearchCondition anotherSchemaCondition = from(anotherSchemaType.type())
				.whereAny(asList(anotherSchemaType.reference1ToZeSchema(), anotherSchemaType.reference2ToZeSchema()))
				.isIn(asList(record));
		assertThat(anotherSchemaImpact).isEqualTo(
				new ModificationImpact(anotherSchemaType.metadataUsingZeSchemaDateAndString(), anotherSchemaCondition));

		LogicalSearchCondition thirdSchemaCondition = from(thirdSchemaType.type())
				.whereAny(asList(thirdSchemaType.referenceToZeSchema())).isIn(asList(record));

		assertThat(thirdSchemaImpact).isEqualTo(
				new ModificationImpact(thirdSchemaType.metadataUsingZeSchemaDateAndString(), thirdSchemaCondition));

	}

	private Record newAnotherSchemaRecordUsing(Record record) {
		Record newRecord = recordServices.newRecordWithSchema(anotherSchemaType.instance());
		newRecord.set(anotherSchemaType.reference1ToZeSchema(), record);
		return newRecord;
	}

	private Record newThirdSchemaRecordUsing(Record record) {
		Record newRecord = recordServices.newRecordWithSchema(thirdSchemaType.instance());
		newRecord.set(thirdSchemaType.referenceToZeSchema(), record);
		return newRecord;
	}

	private Record newRecordWithAStringAndADate() {
		Record newRecord = recordServices.newRecordWithSchema(zeSchema.instance());
		newRecord.set(zeSchema.stringMetadata(), aString);
		newRecord.set(zeSchema.dateTimeMetadata(), aDate);
		return newRecord;
	}

}
