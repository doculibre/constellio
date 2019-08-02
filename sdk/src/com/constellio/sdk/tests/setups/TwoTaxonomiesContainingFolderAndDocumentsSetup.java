package com.constellio.sdk.tests.setups;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * This schema setup can be used to test multiple taxonomy behaviors :
 * <p>
 * Taxonomy 1 :
 * - composed of two types, folders can only be added in the second type,
 * - the second type can be child of the first type or second type, but not both (a validation should be done)
 * <p>
 * Taxonomy 2 :
 * - composed of one type, but folders can only be added in the custom type
 * - Folders can have multiple values of taxonomy2
 * <p>
 * Folders :
 * - can contains other folders and documents
 */
public class TwoTaxonomiesContainingFolderAndDocumentsSetup extends SchemasSetup {

	Taxonomy1FirstSchemaType taxonomy1FirstSchemaType = new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchemaType = new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = new Taxonomy2CustomSchema();
	UserSchema userSchema = new UserSchema();
	FolderSchema folderSchema;
	DocumentSchema documentSchema;
	private List<Taxonomy> taxonomies;
	private Taxonomy taxo1;
	private Taxonomy taxo2;

	public TwoTaxonomiesContainingFolderAndDocumentsSetup(String collection) {
		super(collection);
	}

	public List<Taxonomy> getTaxonomies() {
		return Collections.unmodifiableList(taxonomies);
	}

	public Taxonomy getTaxo1() {
		return taxo1;
	}

	public Taxonomy getTaxo2() {
		return taxo2;
	}

	public UserSchema getUserSchema() {
		return userSchema;
	}

	@Override
	public void setUp() {

		MetadataSchemaTypeBuilder taxo1Type1 = typesBuilder.createNewSchemaType("taxo1Type1");
		MetadataSchemaTypeBuilder taxo1Type2 = typesBuilder.createNewSchemaType("taxo1Type2");
		MetadataSchemaTypeBuilder taxo2Type = typesBuilder.createNewSchemaType("taxo2Type");
		MetadataSchemaTypeBuilder folderType = typesBuilder.createNewSchemaType("zefolder");
		MetadataSchemaTypeBuilder documentType = typesBuilder.createNewSchemaType("zedocument");

		setupTaxonomy1(taxo1Type1, taxo1Type2);
		setupTaxonomy2(taxo2Type);
		setupFolderType(folderType, taxo1Type2, taxo2Type);
		setupDocumentType(documentType, folderType);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo1");

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "taxo2");

		taxo1 = Taxonomy.createPublic("taxo1", labelTitle1, collection,
				Arrays.asList("taxo1Type1", "taxo1Type2"));
		taxo2 = Taxonomy.createPublic("taxo2", labelTitle2, collection, Arrays.asList("taxo2Type"));

		taxonomies = Arrays.asList(taxo1, taxo2);
	}

	private void setupFolderType(MetadataSchemaTypeBuilder folderType, MetadataSchemaTypeBuilder taxo1Type2,
								 MetadataSchemaTypeBuilder taxo2Type) {
		folderType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);
		folderType.getDefaultSchema().create("taxonomy1").defineTaxonomyRelationshipToType(taxo1Type2);
		folderType.getDefaultSchema().create("taxonomy2").setMultivalue(true).defineTaxonomyRelationshipToSchemas(
				taxo2Type.getCustomSchema("custom"));

		folderType.getDefaultSchema().create("conceptReferenceWithoutTaxonomyRelationship").defineReferencesTo(taxo1Type2);
	}

	private void setupDocumentType(MetadataSchemaTypeBuilder documentType, MetadataSchemaTypeBuilder folderType) {
		documentType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);

	}

	private void setupTaxonomy1(MetadataSchemaTypeBuilder taxo1Type1, MetadataSchemaTypeBuilder taxo1Type2) {
		taxo1Type1.getDefaultSchema().create("parent").defineChildOfRelationshipToType(taxo1Type1);
		taxo1Type2.getDefaultSchema().create("parentOfType1").defineChildOfRelationshipToType(taxo1Type1);
		taxo1Type2.getDefaultSchema().create("parentOfType2").defineChildOfRelationshipToType(taxo1Type2);
	}

	private void setupTaxonomy2(MetadataSchemaTypeBuilder taxo2Type) {
		taxo2Type.getDefaultSchema().create("parent").defineChildOfRelationshipToType(taxo2Type);
		taxo2Type.createCustomSchema("custom");
	}

	public String getCollection() {
		return collection;
	}

	public TaxonomyRecords givenTaxonomyRecords(RecordServices recordServices) {
		try {
			return new TaxonomyRecords(recordServices);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public class UserSchema implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("user");
		}

		public String code() {
			return "user_default";
		}

		public String collection() {
			return "zeCollection";
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata username() {
			return getMetadata(code() + "_username");
		}

		public Metadata groups() {
			return getMetadata(code() + "_groups");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class Taxonomy1FirstSchemaType implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("taxo1Type1");
		}

		public String code() {
			return "taxo1Type1_default";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public String parentCode() {
			return code() + "_parent";
		}

		public Metadata parent() {
			return getMetadata(parentCode());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class Taxonomy1SecondSchemaType implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("taxo1Type2");
		}

		public String code() {
			return "taxo1Type2_default";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public String parentOfType1Code() {
			return code() + "_parentOfType1";
		}

		public String parentOfType2Code() {
			return code() + "_parentOfType2";
		}

		public Metadata parentOfType1() {
			return getMetadata(parentOfType1Code());
		}

		public Metadata parentOfType2() {
			return getMetadata(parentOfType2Code());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata allRemovedAuths() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.ALL_REMOVED_AUTHS);
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class Taxonomy2DefaultSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("taxo2Type");
		}

		public String code() {
			return "taxo2Type_default";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public String parentCode() {
			return code() + "_parent";
		}

		public Metadata parent() {
			return getMetadata(parentCode());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}

	}

	public class Taxonomy2CustomSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("taxo2Type");
		}

		public String code() {
			return "taxo2Type_custom";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class CollectionSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("collection");
		}

		public String code() {
			return "collection_default";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class FolderSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("zefolder");
		}

		public String code() {
			return "zefolder_default";
		}

		public String collection() {
			return getCollection();
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata allRemovedAuths() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.ALL_REMOVED_AUTHS);
		}

		public Metadata attachedAncestors() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.ATTACHED_ANCESTORS);
		}

		public Metadata taxonomy1() {
			return getMetadata(code() + "_taxonomy1");
		}

		public Metadata taxonomy2() {
			return getMetadata(code() + "_taxonomy2");
		}

		public Metadata conceptReferenceWithoutTaxonomyRelationship() {
			return getMetadata(code() + "_conceptReferenceWithoutTaxonomyRelationship");
		}


		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}

	}

	public class DocumentSchema implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("zedocument");
		}

		public String code() {
			return "zedocument_default";
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public String collection() {
			return getCollection();
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata attachedAncestors() {
			return getMetadata(code() + "_attachedAncestors");
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}

		public Metadata tokens() {
			return getMetadata(code() + "_" + CommonMetadataBuilder.TOKENS);
		}
	}

	public class TaxonomyRecords {

		public final TestRecord taxo1_firstTypeItem1;
		public final TestRecord taxo1_firstTypeItem2;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem1;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem2;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1;
		public final TestRecord taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem2;
		public final TestRecord taxo1_firstTypeItem2_secondTypeItem1;
		public final TestRecord taxo1_firstTypeItem2_secondTypeItem2;
		public final TestRecord taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1;
		public final TestRecord taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2;

		public final TestRecord taxo2_defaultSchemaItem1;
		public final TestRecord taxo2_defaultSchemaItem2;
		public final TestRecord taxo2_defaultSchemaItem2_defaultSchemaItem1;
		public final TestRecord taxo2_defaultSchemaItem2_defaultSchemaItem2;
		public final TestRecord taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1;
		public final TestRecord taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2;
		public final TestRecord taxo2_defaultSchemaItem2_customSchemaItem1;
		public final TestRecord taxo2_defaultSchemaItem2_customSchemaItem2;

		private RecordServices recordServices;

		private TaxonomyRecords(RecordServices recordServices)
				throws RecordServicesException {

			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			taxo1_firstTypeItem1 = addTaxo1FirstTypeRecord(transaction, "taxo1_firstTypeItem1", null);
			taxo1_firstTypeItem2 = addTaxo1FirstTypeRecord(transaction, "taxo1_firstTypeItem2", null);
			taxo1_firstTypeItem2_firstTypeItem1 = addTaxo1FirstTypeRecord(transaction, "taxo1_firstTypeItem2_firstTypeItem1",
					taxo1_firstTypeItem2);
			taxo1_firstTypeItem2_firstTypeItem2 = addTaxo1FirstTypeRecord(transaction, "taxo1_firstTypeItem2_firstTypeItem2",
					taxo1_firstTypeItem2);
			taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1", taxo1_firstTypeItem2_firstTypeItem2, null);
			taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2", taxo1_firstTypeItem2_firstTypeItem2, null);
			taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1", null,
					taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2);
			taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem2 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem2", null,
					taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2);
			taxo1_firstTypeItem2_secondTypeItem1 = addTaxo1SecondTypeRecord(transaction, "taxo1_firstTypeItem2_secondTypeItem1",
					taxo1_firstTypeItem2, null);
			taxo1_firstTypeItem2_secondTypeItem2 = addTaxo1SecondTypeRecord(transaction, "taxo1_firstTypeItem2_secondTypeItem2",
					taxo1_firstTypeItem2, null);
			taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1", null, taxo1_firstTypeItem2_secondTypeItem2);
			taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2 = addTaxo1SecondTypeRecord(transaction,
					"taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2", null, taxo1_firstTypeItem2_secondTypeItem2);

			taxo2_defaultSchemaItem1 = addTaxo2DefaultSchema(transaction, "taxo2_defaultSchemaItem1", null);
			taxo2_defaultSchemaItem2 = addTaxo2DefaultSchema(transaction, "taxo2_defaultSchemaItem2", null);
			taxo2_defaultSchemaItem2_defaultSchemaItem1 = addTaxo2DefaultSchema(transaction,
					"taxo2_defaultSchemaItem2_defaultSchemaItem1", taxo2_defaultSchemaItem2);
			taxo2_defaultSchemaItem2_defaultSchemaItem2 = addTaxo2DefaultSchema(transaction,
					"taxo2_defaultSchemaItem2_defaultSchemaItem2", taxo2_defaultSchemaItem2);
			taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1 = addTaxo2CustomSchema(transaction,
					"taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1",
					taxo2_defaultSchemaItem2_defaultSchemaItem2);
			taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2 = addTaxo2CustomSchema(transaction,
					"taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2",
					taxo2_defaultSchemaItem2_defaultSchemaItem2);
			taxo2_defaultSchemaItem2_customSchemaItem1 = addTaxo2CustomSchema(transaction,
					"taxo2_defaultSchemaItem2_customSchemaItem1", taxo2_defaultSchemaItem2);
			taxo2_defaultSchemaItem2_customSchemaItem2 = addTaxo2CustomSchema(transaction,
					"taxo2_defaultSchemaItem2_customSchemaItem2", taxo2_defaultSchemaItem2);

			recordServices.execute(transaction);
		}

		private TestRecord addTaxo1FirstTypeRecord(Transaction transaction, String id, Record parent) {
			TestRecord record = new TestRecord(taxonomy1FirstSchemaType, collection + "_" + id);
			record.set(taxonomy1FirstSchemaType.title(), id);
			record.set(taxonomy1FirstSchemaType.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private TestRecord addTaxo1SecondTypeRecord(Transaction transaction, String id, Record firstTypeParent,
													TestRecord secondTypeParent) {
			TestRecord record = new TestRecord(taxonomy1SecondSchemaType, collection + "_" + id);
			record.set(taxonomy1SecondSchemaType.title(), id);
			record.set(taxonomy1SecondSchemaType.parentOfType1(), firstTypeParent);
			record.set(taxonomy1SecondSchemaType.parentOfType2(), secondTypeParent);
			transaction.addUpdate(record);
			return record;
		}

		private TestRecord addTaxo2DefaultSchema(Transaction transaction, String id, Record parent) {
			TestRecord record = new TestRecord(taxonomy2DefaultSchema, collection + "_" + id);
			record.set(taxonomy2DefaultSchema.title(), id);
			record.set(taxonomy2DefaultSchema.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private TestRecord addTaxo2CustomSchema(Transaction transaction, String id, Record parent) {
			TestRecord record = new TestRecord(taxonomy2CustomSchema, collection + "_" + id);
			record.set(taxonomy2CustomSchema.title(), id);
			record.set(taxonomy2CustomSchema.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		public void mockRecordProviderToReturnRecordsById(RecordProvider recordProvider) {

			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_firstTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_firstTypeItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2);
			mockRecordProviderToReturnRecordById(recordProvider,
					taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider,
					taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_secondTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_secondTypeItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2);

			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_defaultSchemaItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_defaultSchemaItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_customSchemaItem1);
			mockRecordProviderToReturnRecordById(recordProvider, taxo2_defaultSchemaItem2_customSchemaItem2);

		}

		public void mockRecordProviderToReturnRecordById(RecordProvider recordProvider, Record record) {
			when(recordProvider.getRecord(record.getId())).thenReturn(record);
		}
	}

}
