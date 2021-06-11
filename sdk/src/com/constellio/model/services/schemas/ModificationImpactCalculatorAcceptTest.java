package com.constellio.model.services.schemas;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.ImpactHandlingMode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.QueryBasedReindexingBatchProcessModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_LAZY;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class ModificationImpactCalculatorAcceptTest extends ConstellioTest {

	List<Metadata> alreadyReindexedMetadata;

	RecordServices recordServices;

	MetadataSchemasManager schemasManager;

	//ModificationImpactCalculator impactCalculator;

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
	}

	private ModificationImpactCalculator newImpactCalculator() {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
		return new ModificationImpactCalculator(types, taxonomies, searchServices, recordServices, new CacheBasedTaxonomyVisitingServices(getModelLayerFactory()));
	}

	@Test
	public void givenUnsavedRecordWhenCalculatingModificationImpactThenNothing()
			throws Exception {
		defineSchemasManager().using(schemas.withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas());
		Record newRecord = newRecordWithAStringAndADate();

		ModificationImpactCalculatorResponse response = newImpactCalculator()
				.findTransactionImpact(new Transaction(newRecord));

		assertThat(response.getImpacts()).isEmpty();
	}

	@Test
	public void givenSavedWithModificationsInMetadataNotUsedInOtherSchemasWhenCalculatingModificationImpactThenNothing()
			throws Exception {
		defineSchemasManager().using(schemas.withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas());
		Record record = newRecordWithAStringAndADate();
		recordServices.update(record);
		record.set(zeSchema.booleanMetadata(), true);

		ModificationImpactCalculatorResponse response = newImpactCalculator()
				.findTransactionImpact(new Transaction(record));

		assertThat(response.getImpacts()).isEmpty();
	}

	@Test
	public void givenRecordWithModificationsInMetadatasUsedInOtherSchemasWhenCalculatingModificationImpactHandlingInSameTransactionThenRequireReindexations()
			throws Exception {
		defineSchemasManager().using(schemas.withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas());
		Record record = newRecordWithAStringAndADate();
		Record anotherSchemaRecord = newAnotherSchemaRecordUsing(record);
		Record thirdSchemaRecord = newThirdSchemaRecordUsing(record);
		recordServices.execute(new Transaction(thirdSchemaRecord, anotherSchemaRecord, record));

		record.set(zeSchema.stringMetadata(), aNewString);
		record.set(zeSchema.dateTimeMetadata(), aNewDate);

		ModificationImpactCalculatorResponse response = newImpactCalculator()
				.findTransactionImpact(new Transaction(record));

		assertThat(response.getImpacts()).hasSize(2);

		ModificationImpact anotherSchemaImpact;
		ModificationImpact thirdSchemaImpact;

		if (response.getImpacts().get(0).getMetadataToReindex().iterator().next().getCode().startsWith("another")) {
			anotherSchemaImpact = response.getImpacts().get(0);
			thirdSchemaImpact = response.getImpacts().get(1);
		} else {
			anotherSchemaImpact = response.getImpacts().get(1);
			thirdSchemaImpact = response.getImpacts().get(0);
		}

		LogicalSearchCondition anotherSchemaCondition = from(anotherSchemaType.type())
				.whereAny(asList(anotherSchemaType.reference1ToZeSchema(), anotherSchemaType.reference2ToZeSchema()))
				.isIn(asList(record)).andWhere(Schemas.IDENTIFIER)
				.isNotIn(asList(record.getId()));
		assertThat(anotherSchemaImpact).isEqualTo(
				new QueryBasedReindexingBatchProcessModificationImpact(anotherSchemaType.type(), anotherSchemaType.metadataUsingZeSchemaDateAndString(),
						anotherSchemaCondition, 1, null, true));

		LogicalSearchCondition thirdSchemaCondition = from(thirdSchemaType.type())
				.whereAny(asList(thirdSchemaType.referenceToZeSchema())).isIn(asList(record))
				.andWhere(Schemas.IDENTIFIER)
				.isNotIn(asList(record.getId()));

		assertThat(thirdSchemaImpact).isEqualTo(
				new QueryBasedReindexingBatchProcessModificationImpact(thirdSchemaType.type(), thirdSchemaType.metadataUsingZeSchemaDateAndString(),
						thirdSchemaCondition, 1, null, true));

	}

	@Test
	public void givenRecordWithModificationsInMetadatasUsedInOtherSchemasWhenCalculatingModificationImpactHandlingInFutureTransactionsThenRequireReindexations()
			throws Exception {
		defineSchemasManager().using(schemas.withStringAndDateMetadataUsedForCopyAndCalculationInOtherSchemas());
		Record record = newRecordWithAStringAndADate();
		Record anotherSchemaRecord = newAnotherSchemaRecordUsing(record);
		Record thirdSchemaRecord = newThirdSchemaRecordUsing(record);
		recordServices.execute(new Transaction(thirdSchemaRecord, anotherSchemaRecord, record));

		record.set(zeSchema.stringMetadata(), aNewString);
		record.set(zeSchema.dateTimeMetadata(), aNewDate);

		Transaction tx = new Transaction(record);
		tx.getRecordUpdateOptions().setImpactHandlingMode(ImpactHandlingMode.DELEGATED);
		ModificationImpactCalculatorResponse response = newImpactCalculator()
				.findTransactionImpact(tx);

		assertThat(response.getImpacts()).hasSize(2);

		ModificationImpact anotherSchemaImpact;
		ModificationImpact thirdSchemaImpact;

		if (response.getImpacts().get(0).getMetadataToReindex().iterator().next().getCode().startsWith("another")) {
			anotherSchemaImpact = response.getImpacts().get(0);
			thirdSchemaImpact = response.getImpacts().get(1);
		} else {
			anotherSchemaImpact = response.getImpacts().get(1);
			thirdSchemaImpact = response.getImpacts().get(0);
		}

		LogicalSearchCondition anotherSchemaCondition = from(anotherSchemaType.type())
				.whereAny(asList(anotherSchemaType.reference1ToZeSchema(), anotherSchemaType.reference2ToZeSchema()))
				.isIn(asList(record));
		assertThat(anotherSchemaImpact).isEqualTo(
				new QueryBasedReindexingBatchProcessModificationImpact(anotherSchemaType.type(), anotherSchemaType.metadataUsingZeSchemaDateAndString(),
						anotherSchemaCondition, 1, null, false));

		LogicalSearchCondition thirdSchemaCondition = from(thirdSchemaType.type())
				.whereAny(asList(thirdSchemaType.referenceToZeSchema())).isIn(asList(record));

		assertThat(thirdSchemaImpact).isEqualTo(
				new QueryBasedReindexingBatchProcessModificationImpact(thirdSchemaType.type(), thirdSchemaType.metadataUsingZeSchemaDateAndString(),
						thirdSchemaCondition, 1, null, false));

	}

	@Test
	public void givenLazyTransientCalculatedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCalculatedInAnotherSchema(TRANSIENT_LAZY));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.referenceFromAnotherSchemaToZeSchema(), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.calculatedZeSchemaTitleLengthPlusTwo())).isEqualTo(17.0);

		recordServices.update(record("chat").set(TITLE, "Édouard"));
		assertThat(record("record").<Double>get(anotherSchemaType.calculatedZeSchemaTitleLengthPlusTwo())).isEqualTo(9.0);
	}

	@Test
	public void givenEagerTransientCalculatedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCalculatedInAnotherSchema(MetadataTransiency.TRANSIENT_EAGER));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.referenceFromAnotherSchemaToZeSchema(), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.calculatedZeSchemaTitleLengthPlusTwo())).isEqualTo(17.0);

		recordServices.update(record("chat").set(TITLE, "Édouard"));
		assertThat(record("record").<Double>get(anotherSchemaType.calculatedZeSchemaTitleLengthPlusTwo())).isEqualTo(9.0);
	}

	@Test
	public void givenLazyTransientCopiedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCopiedInAnotherSchema(TRANSIENT_LAZY));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.referenceFromAnotherSchemaToZeSchema(), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(15.0);

		recordServices.update(record("chat").set(TITLE, "Édouard"));
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(7.0);
	}

	@Test
	public void givenEagerTransientCopiedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCopiedInAnotherSchema(MetadataTransiency.TRANSIENT_EAGER));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.referenceFromAnotherSchemaToZeSchema(), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(15.0);

		recordServices.update(record("chat").set(TITLE, "Édouard"));
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(7.0);
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenLazyTransientReferenceMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCopiedInAnotherSchema(TRANSIENT_LAZY)
				.withReferenceFromAnotherSchemaToZeSchemaComputedFromStringMetadata(TRANSIENT_LAZY));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(15.0);

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), "chat2"));
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(7.0);
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenEagerTransientReferenceMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withComputedTitleSizeCopiedInAnotherSchema(MetadataTransiency.TRANSIENT_EAGER)
				.withReferenceFromAnotherSchemaToZeSchemaComputedFromStringMetadata(MetadataTransiency.TRANSIENT_EAGER));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), "chat"));
		recordServices.execute(tx);
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(15.0);

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), "chat2"));
		assertThat(record("record").<Double>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(7.0);
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenLazyTransientMultivalueReferenceMetadataUsedByCalculatedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withTransientMultivalueReferenceUsedByCalculatedMetadata(TRANSIENT_LAZY));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(zeSchema, "chat3").set(TITLE, "Tomcat"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), asList("chat", "chat2")));
		recordServices.execute(tx);
		assertThat(record("record").<List<Double>>get(anotherSchemaType.calculatedZeSchemaTitlesLength())).isEqualTo(asList(15.0, 7.0));

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), asList("chat2", "chat3")));
		assertThat(record("record").<List<Double>>get(anotherSchemaType.calculatedZeSchemaTitlesLength())).isEqualTo(asList(7.0, 6.0));
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenEagerTransientMultivalueReferenceMetadataUsedByCalculatedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withTransientMultivalueReferenceUsedByCalculatedMetadata(TRANSIENT_EAGER));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(zeSchema, "chat3").set(TITLE, "Tomcat"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), asList("chat", "chat2")));
		recordServices.execute(tx);
		assertThat(record("record").<List<Double>>get(anotherSchemaType.calculatedZeSchemaTitlesLength())).isEqualTo(asList(15.0, 7.0));

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), asList("chat2", "chat3")));
		assertThat(record("record").<List<Double>>get(anotherSchemaType.calculatedZeSchemaTitlesLength())).isEqualTo(asList(7.0, 6.0));
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenLazyTransientMultivalueReferenceMetadataUsedByCopiedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withTransientMultivalueReferenceUsedByCopiedMetadata(TRANSIENT_LAZY));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(zeSchema, "chat3").set(TITLE, "Tomcat"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), asList("chat", "chat2")));
		recordServices.execute(tx);
		assertThat(record("record").<List<Double>>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(asList(15.0, 7.0));

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), asList("chat2", "chat3")));
		assertThat(record("record").<List<Double>>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(asList(7.0, 6.0));
	}

	//TODO Disabled test  since the impact modification do a search on the reference metadata @Test
	public void givenEagerTransientMultivalueReferenceMetadataUsedByCopiedMetadataThenModificationPropagated()
			throws Exception {
		defineSchemasManager().using(schemas.withTransientMultivalueReferenceUsedByCopiedMetadata(TRANSIENT_EAGER));

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema, "chat").set(TITLE, "Vodka Framboise"));
		tx.add(new TestRecord(zeSchema, "chat2").set(TITLE, "Édouard"));
		tx.add(new TestRecord(zeSchema, "chat3").set(TITLE, "Tomcat"));
		tx.add(new TestRecord(anotherSchemaType, "record").set(anotherSchemaType.metadata("aString"), asList("chat", "chat2")));
		recordServices.execute(tx);
		assertThat(record("record").<List<Double>>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(asList(15.0, 7.0));

		recordServices.update(record("record").set(anotherSchemaType.metadata("aString"), asList("chat2", "chat3")));
		assertThat(record("record").<List<Double>>get(anotherSchemaType.copiedZeSchemaTitleLength())).isEqualTo(asList(7.0, 6.0));
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
