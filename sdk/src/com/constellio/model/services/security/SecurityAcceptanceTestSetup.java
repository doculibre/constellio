package com.constellio.model.services.security;

import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions.codeMetadataFacultative;

/**
 * This schema setup can be used to test multiple taxonomy behaviors :
 * <p>
 * Taxonomy 1 : - composed of two types, folders can only be added in the second type, - the second type can be child of the first
 * type or second type, but not both (a validation should be done)
 * <p>
 * Taxonomy 2 : - composed of one type, but folders can only be added in the custom type
 * <p>
 * Folders : - can contains other folders and documents
 */
public class SecurityAcceptanceTestSetup extends SchemasSetup {

	public static final String TAXO1_FOND1 = "taxo1_fond1";
	public static final String TAXO1_FOND1_1 = "taxo1_fond1_1";
	public static final String TAXO1_CATEGORY1 = "taxo1_category1";
	public static final String TAXO1_CATEGORY2 = "taxo1_category2";
	public static final String TAXO1_CATEGORY2_1 = "taxo1_category2_1";
	public static final String TAXO2_UNIT1 = "taxo2_unit1";
	public static final String TAXO2_UNIT1_1 = "taxo2_unit1_1";
	public static final String TAXO2_STATION1 = "taxo2_station1";
	public static final String TAXO2_STATION2 = "taxo2_station2";
	public static final String TAXO2_STATION2_1 = "taxo2_station2_1";
	public static final String FOLDER1 = "folder1";
	public static final String FOLDER2 = "folder2";
	public static final String FOLDER2_1 = "folder2_1";
	public static final String FOLDER2_2 = "folder2_2";
	public static final String FOLDER3 = "folder3";
	public static final String FOLDER4 = "folder4";
	public static final String FOLDER4_1 = "folder4_1";
	public static final String FOLDER4_2 = "folder4_2";
	public static final String FOLDER5 = "folder5";
	public static final String FOLDER1_DOC1 = "folder1_doc1";
	public static final String FOLDER2_2_DOC1 = "folder2_2_doc1";
	public static final String FOLDER2_2_DOC2 = "folder2_2_doc2";
	public static final String FOLDER3_DOC1 = "folder3_doc1";
	public static final String FOLDER4_1_DOC1 = "folder4_1_doc1";
	public static final String FOLDER4_2_DOC1 = "folder4_2_doc1";
	public static final String FOLDER5_DOC1 = "folder5_doc1";

	public static final String FOLDER_TYPE1 = "folderType1";
	public static final String FOLDER_TYPE2 = "folderType2";
	public static final String FOLDER_TYPE3 = "folderType3";

	DocumentFond documentFond = new DocumentFond();
	Category category = new Category();
	AdministrativeUnit administrativeUnit = new AdministrativeUnit();
	ClassificationStation classificationStation = new ClassificationStation();
	public FolderTypeSchema folderTypeSchema = new FolderTypeSchema();
	public FolderSchema folderSchema = new FolderSchema();
	public DocumentSchema documentSchema = new DocumentSchema();
	UserSchema userSchema = new UserSchema();
	GroupSchema groupSchema = new GroupSchema();

	PermissionType permissionType = new PermissionType();

	private List<Taxonomy> taxonomies;

	public SecurityAcceptanceTestSetup(String collection) {
		super(collection);
	}

	public List<Taxonomy> getTaxonomies() {
		return Collections.unmodifiableList(taxonomies);
	}

	public Taxonomy getTaxonomy1() {
		return taxonomies.get(0);
	}

	public Taxonomy getTaxonomy2() {
		return taxonomies.get(1);
	}

	@Override
	public void setUp() {

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo");

		if (typesBuilder != null && !typesBuilder.hasSchemaType("documentFond")) {

			MetadataSchemaTypeBuilder documentFondType = typesBuilder.createNewSchemaTypeWithSecurity("documentFond");
			MetadataSchemaTypeBuilder categoryType = typesBuilder.createNewSchemaTypeWithSecurity("category");
			MetadataSchemaTypeBuilder administrativeUnitType = typesBuilder.createNewSchemaTypeWithSecurity("administrativeUnit");
			MetadataSchemaTypeBuilder folderType = typesBuilder.createNewSchemaTypeWithSecurity("folder");
			MetadataSchemaTypeBuilder folderTypeType = new ValueListItemSchemaTypeBuilder(typesBuilder)
					.createValueListItemSchema("folderType", labelTitle1, codeMetadataFacultative());
			MetadataSchemaTypeBuilder documentType = typesBuilder.createNewSchemaTypeWithSecurity("document");

			setupTaxonomy1(documentFondType, categoryType);
			setupTaxonomy2(administrativeUnitType);

			setupFolderType(folderType, categoryType, administrativeUnitType, folderTypeType);
			setupDocumentType(documentType, folderType);

		}

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "taxo1");

		Map<Language, String> labelTitle3 = new HashMap<>();
		labelTitle3.put(Language.French, "taxo2");

		Taxonomy firstTaxonomy = Taxonomy
				.createPublic("taxo1", labelTitle2, collection, Arrays.asList("documentFond", "category"));
		Taxonomy secondTaxonomy = Taxonomy.createPublic("taxo2", labelTitle3, collection, Arrays.asList("administrativeUnit"));

		taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);
	}


	public void loadTaxonomies(TaxonomiesManager taxonomiesManager) {

		Taxonomy firstTaxonomy = taxonomiesManager.getTaxonomyFor(collection, "taxo1");
		Taxonomy secondTaxonomy = taxonomiesManager.getTaxonomyFor(collection, "taxo2");

		taxonomies = Arrays.asList(firstTaxonomy, secondTaxonomy);
	}

	private void setupFolderType(MetadataSchemaTypeBuilder folderType, MetadataSchemaTypeBuilder category,
								 MetadataSchemaTypeBuilder administrativeUnit,
								 MetadataSchemaTypeBuilder folderTypeType) {
		folderType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);
		folderType.getDefaultSchema().create("taxonomy1").defineTaxonomyRelationshipToType(category);
		folderType.getDefaultSchema().create("taxonomy2")
				.defineTaxonomyRelationshipToSchemas(administrativeUnit.getCustomSchema("classificationStation"));
		folderType.getDefaultSchema().create("linkToOtherFolders").setMultivalue(true).defineReferencesTo(folderType);
		folderType.getDefaultSchema().create("linkToOtherFolder").setMultivalue(false).defineReferencesTo(folderType);
		folderType.getDefaultSchema().create("firstReference").defineReferencesTo(folderTypeType)
				.setRelationshipProvidingSecurity(true);
		folderType.getDefaultSchema().create("secondReference").defineReferencesTo(folderTypeType)
				.setRelationshipProvidingSecurity(true);
		folderType.getDefaultSchema().create("thirdReference").defineReferencesTo(folderTypeType);

	}

	private void setupDocumentType(MetadataSchemaTypeBuilder documentType, MetadataSchemaTypeBuilder folderType) {
		documentType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);

	}

	private void setupTaxonomy1(MetadataSchemaTypeBuilder documentFond, MetadataSchemaTypeBuilder category) {
		documentFond.getDefaultSchema().create("parent").defineChildOfRelationshipToType(documentFond);
		category.getDefaultSchema().create("parentOfDocumentFond").defineChildOfRelationshipToType(documentFond);
		category.getDefaultSchema().create("parentOfCategory").defineChildOfRelationshipToType(category);
	}

	private void setupTaxonomy2(MetadataSchemaTypeBuilder administrativeUnit) {
		administrativeUnit.getDefaultSchema().create("parent").defineChildOfRelationshipToType(administrativeUnit);
		administrativeUnit.createCustomSchema("classificationStation");
	}

	public Records givenRecords(RecordServices recordServices) {
		return new Records(recordServices).setup();
	}
	//
	//	public Users givenUsers(RecordServices recordServices) {
	//		try {
	//			return new Users(recordServices);
	//		} catch (RecordServicesException e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

	public class PermissionType implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("permission");
		}

		@Override
		public String code() {
			return "permission_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		public Metadata read() {
			return getMetadata(code() + "_read");
		}

		public Metadata write() {
			return getMetadata(code() + "_write");
		}

		public Metadata delete() {
			return getMetadata(code() + "_delete");
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

	}

	public class DocumentFond implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("documentFond");
		}

		@Override
		public String code() {
			return "documentFond_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
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
	}

	public class Category implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("category");
		}

		@Override
		public String code() {
			return "category_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public String parentOfDocumentFondCode() {
			return code() + "_parentOfDocumentFond";
		}

		public String parentOfCategoryCode() {
			return code() + "_parentOfCategory";
		}

		public Metadata parentOfDocumentFond() {
			return getMetadata(parentOfDocumentFondCode());
		}

		public Metadata parentOfCategory() {
			return getMetadata(parentOfCategoryCode());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

	}

	public class AdministrativeUnit implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("administrativeUnit");
		}

		@Override
		public String code() {
			return "administrativeUnit_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
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

	}

	public class ClassificationStation implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("administrativeUnit");
		}

		@Override
		public String code() {
			return "administrativeUnit_classificationStation";
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
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

	}

	public class FolderTypeSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("folderType");
		}

		@Override
		public String code() {
			return "folderType_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

	}

	public class FolderSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("folder");
		}

		@Override
		public String code() {
			return "folder_default";
		}

		@Override
		public String collection() {
			return collection;
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

		public Metadata taxonomy1() {
			return getMetadata(code() + "_taxonomy1");
		}

		public Metadata taxonomy2() {
			return getMetadata(code() + "_taxonomy2");
		}

		public Metadata linkToOtherFolders() {
			return getMetadata(code() + "_linkToOtherFolders");
		}

		public Metadata linkToOtherFolder() {
			return getMetadata(code() + "_linkToOtherFolder");
		}

		public Metadata firstReferenceMetadataProvidingSecurity() {
			return getMetadata(code() + "_firstReference");
		}

		public Metadata secondReferenceMetadataProvidingSecurity() {
			return getMetadata(code() + "_secondReference");
		}

		public Metadata thirdReferenceMetadataWhichDoesNotProvideSecurity() {
			return getMetadata(code() + "_thirdReference");
		}
	}

	public class DocumentSchema implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("document");
		}

		public MetadataSchema instance() {
			return getSchema(code());
		}

		@Override
		public String code() {
			return "document_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}

	}

	public class UserSchema implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("user");
		}

		@Override
		public String code() {
			return "user_default";
		}

		@Override
		public String collection() {
			return collection;
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

	}

	public class GroupSchema implements SchemaShortcuts {

		public MetadataSchemaType type() {
			return get("group");
		}

		@Override
		public String code() {
			return "group_default";
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata groupCode() {
			return getMetadata(code() + "_code");
		}

		public Metadata name() {
			return getMetadata(code() + "_title");
		}
	}

	public class Records {

		private Record getRecord(String id) {
			String prefix = collection.equals("zeCollection") ? "" : "anotherCollection_";
			return recordServices.getDocumentById(prefix + id);
		}

		public final Record taxo1_fond1() {
			return getRecord(TAXO1_FOND1);
		}

		public final Record taxo1_fond1_1() {
			return getRecord(TAXO1_FOND1_1);
		}

		public final Record taxo1_category1() {
			return getRecord(TAXO1_CATEGORY1);
		}

		public final Record taxo1_category2() {
			return getRecord(TAXO1_CATEGORY2);
		}

		public final Record taxo1_category2_1() {
			return getRecord(TAXO1_CATEGORY2_1);
		}

		public final Record taxo2_unit1() {
			return getRecord(TAXO2_UNIT1);
		}

		public final Record taxo2_unit1_1() {
			return getRecord(TAXO2_UNIT1_1);
		}

		public final Record taxo2_station1() {
			return getRecord(TAXO2_STATION1);
		}

		public final Record taxo2_station2() {
			return getRecord(TAXO2_STATION2);
		}

		public final Record taxo2_station2_1() {
			return getRecord(TAXO2_STATION2_1);
		}

		public final Record folder1() {
			return getRecord(FOLDER1);
		}

		public final Record folder2() {
			return getRecord(FOLDER2);
		}

		public final Record folder2_1() {
			return getRecord(FOLDER2_1);
		}

		public final Record folder2_2() {
			return getRecord(FOLDER2_2);
		}

		public final Record folder3() {
			return getRecord(FOLDER3);
		}

		public final Record folder4() {
			return getRecord(FOLDER4);
		}

		public final Record folder4_1() {
			return getRecord(FOLDER4_1);
		}

		public final Record folder4_2() {
			return getRecord(FOLDER4_2);
		}

		public final Record folder5() {
			return getRecord(FOLDER5);
		}

		public final Record folder1_doc1() {
			return getRecord(FOLDER1_DOC1);
		}

		public final Record folder2_2_doc1() {
			return getRecord(FOLDER2_2_DOC1);
		}

		public final Record folder2_2_doc2() {
			return getRecord(FOLDER2_2_DOC2);
		}

		public final Record folder3_doc1() {
			return getRecord(FOLDER3_DOC1);
		}

		public final Record folder4_1_doc1() {
			return getRecord(FOLDER4_1_DOC1);
		}

		public final Record folder4_2_doc1() {
			return getRecord(FOLDER4_2_DOC1);
		}

		public final Record folder5_doc1() {
			return getRecord(FOLDER5_DOC1);
		}

		public final Record folderType1() {
			return getRecord(FOLDER_TYPE1);
		}

		public final Record folderType2() {
			return getRecord(FOLDER_TYPE2);
		}

		public final Record folderType3() {
			return getRecord(FOLDER_TYPE3);
		}

		List<Record> records = new ArrayList<>();

		private RecordServices recordServices;

		public Records(RecordServices recordServices) {
			this.recordServices = recordServices;
		}

		public Records setup() {

			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			String prefix = collection.equals("zeCollection") ? "" : "anotherCollection_";
			Record taxo1_fond1, taxo1_fond1_1, taxo1_category1, taxo1_category2, taxo1_category2_1, taxo2_unit1, taxo2_unit1_1,
					taxo2_station1, taxo2_station2, taxo2_station2_1, folder1, folder2, folder2_1, folder2_2, folder3, folder4, folder4_1, folder4_2, folder5;

			if (collection.equals("zeCollection")) {
				records.add(transaction.add(new TestRecord(folderTypeSchema, FOLDER_TYPE1).set(Schemas.TITLE, "Type 1")));
				records.add(transaction.add(new TestRecord(folderTypeSchema, FOLDER_TYPE2).set(Schemas.TITLE, "Type 2")));
				records.add(transaction.add(new TestRecord(folderTypeSchema, FOLDER_TYPE3).set(Schemas.TITLE, "Type 3")));
			}
			records.add(taxo1_fond1 =

					addFondRecord(transaction, prefix + "taxo1_fond1", null)

			);
			records.add(taxo1_fond1_1 =

					addFondRecord(transaction, prefix + "taxo1_fond1_1", taxo1_fond1)

			);
			records.add(taxo1_category1 =

					addCategoryRecord(transaction, prefix + "taxo1_category1", taxo1_fond1_1, null)

			);
			records.add(taxo1_category2 =

					addCategoryRecord(transaction, prefix + "taxo1_category2", taxo1_fond1, null)

			);
			records.add(taxo1_category2_1 =

					addCategoryRecord(transaction, prefix + "taxo1_category2_1", null, taxo1_category2)

			);

			records.add(taxo2_unit1 =

					addUnitRecord(transaction, prefix + "taxo2_unit1", null)

			);
			records.add(taxo2_unit1_1 =

					addUnitRecord(transaction, prefix + "taxo2_unit1_1", taxo2_unit1)

			);
			records.add(taxo2_station1 =

					addStationRecord(transaction, prefix + "taxo2_station1", taxo2_unit1_1)

			);
			records.add(taxo2_station2 =

					addStationRecord(transaction, prefix + "taxo2_station2", taxo2_unit1)

			);
			records.add(taxo2_station2_1 =

					addStationRecord(transaction, prefix + "taxo2_station2_1", taxo2_station2)

			);

			records.add(folder1 =

					addFolderRecord(transaction, prefix + "folder1", null, taxo1_category1, taxo2_station2)

			);
			records.add(folder2 =

					addFolderRecord(transaction, prefix + "folder2", null, taxo1_category1, taxo2_station2_1)

			);
			records.add(folder2_1 =

					addFolderRecord(transaction, prefix + "folder2_1", folder2, null, null)

			);
			records.add(folder2_2 =

					addFolderRecord(transaction, prefix + "folder2_2", folder2, null, null)

			);
			records.add(folder3 =

					addFolderRecord(transaction, prefix + "folder3", null, taxo1_category2_1, null)

			);
			records.add(folder4 =

					addFolderRecord(transaction, prefix + "folder4", null, taxo1_category2, null)

			);
			records.add(folder4_1 =

					addFolderRecord(transaction, prefix + "folder4_1", folder4, null, null)

			);
			records.add(folder4_2 =

					addFolderRecord(transaction, prefix + "folder4_2", folder4, null, null)

			);
			records.add(folder5 =

					addFolderRecord(transaction, prefix + "folder5", null, null, taxo2_station1)

			);

			records.add(

					addDocumentRecord(transaction, prefix + "folder1_doc1", folder1)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder2_2_doc1", folder2_2)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder2_2_doc2", folder2_2)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder3_doc1", folder3)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder4_1_doc1", folder4_1)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder4_2_doc1", folder4_2)

			);
			records.add(

					addDocumentRecord(transaction, prefix + "folder5_doc1", folder5)

			);

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			return this;
		}

		public List<Record> allRecords() {
			return Collections.unmodifiableList(records);
		}

		public List<String> allFoldersAndDocumentsIds() {
			String prefix = collection.equals("zeCollection") ? "" : "anotherCollection_";
			List<String> ids = new ArrayList<>();
			ids.add(prefix + "folder1");
			ids.add(prefix + "folder2");
			ids.add(prefix + "folder2_1");
			ids.add(prefix + "folder2_2");
			ids.add(prefix + "folder3");
			ids.add(prefix + "folder4");
			ids.add(prefix + "folder4_1");
			ids.add(prefix + "folder4_2");
			ids.add(prefix + "folder5");
			ids.add(prefix + "folder1_doc1");
			ids.add(prefix + "folder2_2_doc1");
			ids.add(prefix + "folder2_2_doc2");
			ids.add(prefix + "folder3_doc1");
			ids.add(prefix + "folder4_1_doc1");
			ids.add(prefix + "folder4_2_doc1");
			ids.add(prefix + "folder5_doc1");
			return ids;
		}

		public List<String> allRecordsId() {
			List<String> ids = new ArrayList<>();
			for (Record record : records) {
				ids.add(record.getId());
			}
			return ids;
		}

		private Record addFondRecord(Transaction transaction, String id, Record parent) {
			Record record = new TestRecord(documentFond, id);
			record.set(documentFond.title(), id);
			record.set(documentFond.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private Record addCategoryRecord(Transaction transaction, String id, Record documentFondParent,
										 Record categoryParent) {
			Record record = new TestRecord(category, id);
			record.set(category.title(), id);
			record.set(category.parentOfDocumentFond(), documentFondParent);
			record.set(category.parentOfCategory(), categoryParent);
			transaction.addUpdate(record);
			return record;
		}

		private Record addUnitRecord(Transaction transaction, String id, Record parent) {
			Record record = new TestRecord(administrativeUnit, id);
			record.set(administrativeUnit.title(), id);
			record.set(administrativeUnit.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private Record addStationRecord(Transaction transaction, String id, Record parent) {
			Record record = new TestRecord(classificationStation, id);
			record.set(classificationStation.title(), id);
			record.set(classificationStation.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private Record addFolderRecord(Transaction transaction, String id, Record parent, Record category,
									   Record station) {
			Record record = new TestRecord(folderSchema, id);
			record.set(folderSchema.title(), id);
			record.set(folderSchema.parent(), parent);
			record.set(folderSchema.taxonomy1(), category);
			record.set(folderSchema.taxonomy2(), station);
			transaction.addUpdate(record);
			return record;
		}

		private Record addDocumentRecord(Transaction transaction, String id, Record parent) {
			Record record = new TestRecord(documentSchema, id);
			record.set(documentSchema.title(), id);
			record.set(documentSchema.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		private Record addUserRecord(Transaction transaction, String id, Record parent) {
			Record record = new TestRecord(documentSchema, id);
			record.set(documentSchema.title(), id);
			record.set(documentSchema.parent(), parent);
			transaction.addUpdate(record);
			return record;
		}

		public List<Record> inFolder4Hierarchy() {

			return Arrays.asList(folder4(), folder4_1(), folder4_2(), folder4_1_doc1(),
					folder4_2_doc1());
		}

		public List<Record> inFolder4_2Hierarchy() {

			return Arrays.asList(folder4_2(), folder4_2_doc1());
		}
	}
/*
	public class Users {

		public final Record CHUCK;
		public final Record BOB;
		public final Record ALICE;
		public final Record EDOUARD;
		public final Record XAVIER;
		public final Record DAKOTA;
		public final Record GANDALF;

		public final Record LEGENDS;
		public final Record HEROES;
		List<Record> users = new ArrayList<>();
		private RecordServices recordServices;

		private Users(RecordServices recordServices)
				throws RecordServicesException {

			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			String prefix = collection.equals("zeCollection") ? "" : "anotherCollection_";

			users.add(LEGENDS = addGroupRecord(transaction, prefix + "LEGENDS"));
			users.add(HEROES = addGroupRecord(transaction, prefix + "HEROES"));

			users.add(CHUCK = addUserRecord(transaction, prefix + "CHUCK", null));
			users.add(BOB = addUserRecord(transaction, prefix + "BOB", null));
			users.add(ALICE = addUserRecord(transaction, prefix + "ALICE", Arrays.asList(LEGENDS)));
			users.add(EDOUARD = addUserRecord(transaction, prefix + "EDOUARD", Arrays.asList(LEGENDS)));
			users.add(XAVIER = addUserRecord(transaction, prefix + "XAVIER", Arrays.asList(HEROES)));
			users.add(DAKOTA = addUserRecord(transaction, prefix + "DAKOTA", Arrays.asList(HEROES)));
			users.add(GANDALF = addUserRecord(transaction, prefix + "GANDALF", Arrays.asList(LEGENDS, HEROES)));

			recordServices.execute(transaction);
		}

		private Record addUserRecord(Transaction transaction, String id, List<Record> groups) {
			Record record = new TestRecord(userSchema, id);
			record.set(userSchema.username(), id);
			record.set(userSchema.groups(), groups);
			transaction.addUpdate(record);
			return record;
		}

		private Record addGroupRecord(Transaction transaction, String code) {
			Record record = new TestRecord(groupSchema, code);
			record.set(groupSchema.groupCode(), code);
			record.set(groupSchema.name(), code);
			transaction.addUpdate(record);
			return record;
		}

		public List<Record> allUsers() {
			return users;
		}
	}
	*/

}
