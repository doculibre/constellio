package com.constellio.model.services.records;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;

public class CounterIndexAcceptanceTest extends ConstellioTest {

	List<String> noAncestors = new ArrayList<>();
	String childOfReference = "childOfReference";
	String anotherSchemaReferenceToItself = "anotherSchemaReferenceToItself";
	String anotherSchemaReferenceToZeSchema = "anotherSchemaReferenceToZeSchema";
	String zeSchemaReferenceToItself = "zeSchemaReferenceToItself";

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeCollectionSchema = zeCollectionSetup.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherCollectionSchema = zeCollectionSetup.new AnotherSchemaMetadatas();
	RecordServices recordServices;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withAllTest(users)
		);
		inCollection(zeCollection).giveWriteAccessTo(dakota, edouard);

		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	public void givenASchemasWithParentRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_singlevalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(false, false)));

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(zeCollectionSchema, "record3"))
				.set(zeCollectionSchema.metadata(childOfReference), "record1");
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record1"));

		recordServices.update(record3.set(zeCollectionSchema.metadata(childOfReference), "record2"));

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record2"));

	}

	@Test
	public void givenASchemasWithParentRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_multivalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(false, true)));

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(zeCollectionSchema, "record3"))
				.set(zeCollectionSchema.metadata(childOfReference), "record1");
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record1"));

		recordServices.update(record3.set(zeCollectionSchema.metadata(childOfReference), "record2"));

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record2"));

	}

	@Test
	public void givenTwoSchemasWithParentRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_singlevalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(false, false)));

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(anotherCollectionSchema, "record3"))
				.set(anotherCollectionSchema.metadata(childOfReference), "record1");
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record1"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), "record2"));

		assertCounterIndexForRecordWithValue("record1", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record2", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record3", 0, asList("record2"));

	}

	@Test
	public void givenASchemasWithTaxonomyRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_singlevalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(true, false)));
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		getModelLayerFactory().getTaxonomiesManager()
				.addTaxonomy(new Taxonomy("ze", "ze", zeCollection, zeCollectionSchema.typeCode()), manager);

		Transaction transaction = new Transaction();
		Record record0 = transaction.add(new TestRecord(zeCollectionSchema, "record0"));
		Record record1 = transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		Record record2 = transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(anotherCollectionSchema, "record3"))
				.set(anotherCollectionSchema.metadata(childOfReference), "record1")
				.set(anotherCollectionSchema.metadata(anotherSchemaReferenceToZeSchema), "record0");
		Record record4 = transaction.add(new TestRecord(anotherCollectionSchema, "record4"))
				.set(anotherCollectionSchema.metadata(anotherSchemaReferenceToItself), "record3");
		Record record5 = transaction.add(new TestRecord(zeCollectionSchema, "record5"))
				.set(zeCollectionSchema.metadata(zeSchemaReferenceToItself), "record2");

		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), "record2"));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 2, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record2"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		transaction = new Transaction();
		transaction.update(record3.set(anotherCollectionSchema.metadata(childOfReference), "record1"));
		transaction.update(record1.set(Schemas.TITLE, "New title!"));
		transaction.update(record2.set(Schemas.TITLE, "New title!"));
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(Schemas.TITLE, "New title!"));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), null));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, noAncestors);
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

	}

	@Test
	public void givenASchemasWithTaxonomyRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_multivalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(true, true)));
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		getModelLayerFactory().getTaxonomiesManager()
				.addTaxonomy(new Taxonomy("ze", "ze", zeCollection, zeCollectionSchema.typeCode()), manager);

		Transaction transaction = new Transaction();
		Record record0 = transaction.add(new TestRecord(zeCollectionSchema, "record0"));
		Record record1 = transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		Record record2 = transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(anotherCollectionSchema, "record3"))
				.set(anotherCollectionSchema.metadata(childOfReference), asList("record1", "record1"))
				.set(anotherCollectionSchema.metadata(anotherSchemaReferenceToZeSchema), "record0");
		Record record4 = transaction.add(new TestRecord(anotherCollectionSchema, "record4"))
				.set(anotherCollectionSchema.metadata(anotherSchemaReferenceToItself), "record3");
		Record record5 = transaction.add(new TestRecord(zeCollectionSchema, "record5"))
				.set(zeCollectionSchema.metadata(zeSchemaReferenceToItself), "record2");

		recordServices.execute(transaction);
		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), asList("record1")));
		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), asList("record2", "record2")));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 2, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record2"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		transaction = new Transaction();
		transaction.update(record3.set(anotherCollectionSchema.metadata(childOfReference), asList("record1")));
		transaction.update(record1.set(Schemas.TITLE, "New title!"));
		transaction.update(record2.set(Schemas.TITLE, "New title!"));
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		transaction = new Transaction();
		transaction.update(record3.set(anotherCollectionSchema.metadata(childOfReference), asList("record1", "record1")));
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(Schemas.TITLE, "New title!"));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, asList("ze", "record1"));
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), new ArrayList<>()));

		assertCounterIndexForRecordWithValue("record0", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 1, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 1, noAncestors);
		assertCounterIndexForRecordWithValue("record4", 0, noAncestors);
		assertCounterIndexForRecordWithValue("record5", 0, asList("ze"));

	}

	@Test
	public void givenASchemasWithPrincipalTaxonomyRelationshipWhenARecordIsMovedThenCounterIndexNotModifiedSinceTheParentChildRelationIsExcluded_singlevalued()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.with(childOfReferenceToSelfAndAnotherReferenceToSelf(true, false)));
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		Taxonomy taxonomy = new Taxonomy("ze", "ze", zeCollection, zeCollectionSchema.typeCode());
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, manager);
		getModelLayerFactory().getTaxonomiesManager().setPrincipalTaxonomy(taxonomy, manager);

		Transaction transaction = new Transaction();
		Record record1 = transaction.add(new TestRecord(zeCollectionSchema, "record1"));
		Record record2 = transaction.add(new TestRecord(zeCollectionSchema, "record2"));
		Record record3 = transaction.add(new TestRecord(anotherCollectionSchema, "record3"))
				.set(anotherCollectionSchema.metadata(childOfReference), "record1");
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 0, asList("ze", "record1"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), "record2"));

		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 0, asList("ze", "record2"));

		transaction = new Transaction();
		transaction.update(record3.set(anotherCollectionSchema.metadata(childOfReference), "record1"));
		transaction.update(record1.set(Schemas.TITLE, "New title!"));
		transaction.update(record2.set(Schemas.TITLE, "New title!"));
		recordServices.execute(transaction);

		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 0, asList("ze", "record1"));

		recordServices.update(record3.set(Schemas.TITLE, "New title!"));

		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 0, asList("ze", "record1"));

		recordServices.update(record3.set(anotherCollectionSchema.metadata(childOfReference), null));

		assertCounterIndexForRecordWithValue("record1", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record2", 0, asList("ze"));
		assertCounterIndexForRecordWithValue("record3", 0, noAncestors);
	}

	@Test
	public void givenModificationsToRecordsThenCounterIndexUpdated()
			throws Exception {
		defineSchemasManager().using(zeCollectionSetup.withAStringMetadata());

		Record zeRecord = new TestRecord(zeCollectionSchema, "zeRecord");
		zeRecord.set(zeCollectionSchema.stringMetadata(), "1");
		recordServices.add(zeRecord);
		assertCounterIndexForRecordWithValue(users.dakotaIn(zeCollection).getId(), 0, noAncestors);
		assertCounterIndexForRecordWithValue(users.bobIn(zeCollection).getId(), 0, noAncestors);

		Transaction transaction = new Transaction().setUser(users.dakotaLIndienIn(zeCollection));
		zeRecord.set(zeCollectionSchema.stringMetadata(), "2");
		transaction.add(zeRecord);
		recordServices.execute(transaction);
		assertThat(withId("zeRecord").get(Schemas.MODIFIED_BY)).isEqualTo(users.dakotaIn(zeCollection).getId());
		assertCounterIndexForRecordWithValue(users.dakotaIn(zeCollection).getId(), 1, noAncestors);
		assertCounterIndexForRecordWithValue(users.edouardIn(zeCollection).getId(), 0, noAncestors);

		transaction = new Transaction().setUser(users.edouardIn(zeCollection));
		zeRecord.set(zeCollectionSchema.stringMetadata(), "3");
		transaction.add(zeRecord);
		recordServices.execute(transaction);
		assertThat(withId("zeRecord").get(Schemas.MODIFIED_BY)).isEqualTo(users.edouardIn(zeCollection).getId());
		assertCounterIndexForRecordWithValue(users.dakotaIn(zeCollection).getId(), 0, noAncestors);
		assertCounterIndexForRecordWithValue(users.edouardIn(zeCollection).getId(), 1, noAncestors);

		transaction = new Transaction().setUser(null);
		zeRecord.set(zeCollectionSchema.stringMetadata(), "4");
		transaction.add(zeRecord);
		recordServices.execute(transaction);
		assertThat(withId("zeRecord").get(Schemas.MODIFIED_BY)).isNull();
		assertCounterIndexForRecordWithValue(users.dakotaIn(zeCollection).getId(), 0, noAncestors);
		assertCounterIndexForRecordWithValue(users.edouardIn(zeCollection).getId(), 0, noAncestors);

		getModelLayerFactory().newReindexingServices().reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		assertCounterIndexForRecordWithValue(users.dakotaIn(zeCollection).getId(), 0, noAncestors);
		assertCounterIndexForRecordWithValue(users.edouardIn(zeCollection).getId(), 0, noAncestors);

	}

	//TODO Test with multivalue

	private void assertCounterIndexForRecordWithValue(String recordId, double expectedValue, List<String> expectedAncestors) {
		try {
			RecordDTO recordDTO = getDataLayerFactory().newRecordDao().get("idx_rfc_" + recordId);
			Double wasValue = (Double) recordDTO.getFields().get("refs_d");
			assertThat(wasValue).isEqualTo(expectedValue);
			List<String> ancestors = (List) recordDTO.getFields().get("ancestors_ss");
			if (ancestors == null) {
				ancestors = new ArrayList<>();
			}
			assertThat(ancestors).isEqualTo(expectedAncestors);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			fail("No counter index for record id '" + recordId + "'");
		}
	}

	private MetadataSchemaTypesConfigurator childOfReferenceToSelfAndAnotherReferenceToSelf(final boolean taxonomyRelation,
			final boolean multivalued) {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeSchemaType = schemaTypes.getSchemaType("zeSchemaType");
				MetadataSchemaTypeBuilder anotherSchemaType = schemaTypes.getSchemaType("anotherSchemaType");
				MetadataSchemaBuilder zeSchema = zeSchemaType.getDefaultSchema();
				MetadataSchemaBuilder anotherSchema = anotherSchemaType.getDefaultSchema();
				zeSchema.create(childOfReference).defineChildOfRelationshipToType(zeSchemaType);
				zeSchema.create(zeSchemaReferenceToItself).defineReferencesTo(zeSchemaType);

				anotherSchema.create(anotherSchemaReferenceToItself).defineReferencesTo(anotherSchemaType);
				anotherSchema.create(anotherSchemaReferenceToZeSchema).defineReferencesTo(zeSchemaType);

				if (taxonomyRelation) {
					anotherSchema.create(childOfReference).defineTaxonomyRelationshipToType(zeSchemaType)
							.setMultivalue(multivalued);
				} else {
					anotherSchema.create(childOfReference).defineChildOfRelationshipToType(zeSchemaType);
				}
			}
		};
	}
}
