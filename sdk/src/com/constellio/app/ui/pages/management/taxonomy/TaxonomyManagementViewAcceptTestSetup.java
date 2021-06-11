package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxonomyManagementViewAcceptTestSetup extends SchemasSetup {

	public static final String CLASSIFICATION = "classification";
	public static final String CLASSIFICATION_COMPLEX = "classificationComplex";
	public static final String CATEGORY = "category";
	public static final String SUB_CATEGORY = "subCategory";

	Category category = new Category();
	SubCategory subCategory = new SubCategory();
	private Map<String, Taxonomy> taxonomies;

	public TaxonomyManagementViewAcceptTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaTypeBuilder categoryType = typesBuilder.createNewSchemaTypeWithSecurity(CATEGORY);
		MetadataSchemaTypeBuilder subCategoryType = typesBuilder.createNewSchemaTypeWithSecurity(SUB_CATEGORY);

		setupClassificationTaxonomy(categoryType);
		setupClassificationComplexTaxonomy(categoryType, subCategoryType);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "Classification Plan");

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "Classification Plan");

		Taxonomy classificationTaxonomy = Taxonomy.createPublic(CLASSIFICATION, labelTitle1, collection,
				Arrays.asList("category"));
		Taxonomy classificationComplexTaxonomy = Taxonomy.createPublic(CLASSIFICATION_COMPLEX, labelTitle2, collection,
				Arrays.asList("category", "subCategory"));

		taxonomies = new HashMap<>();
		taxonomies.put(CLASSIFICATION, classificationTaxonomy);
		taxonomies.put(CLASSIFICATION_COMPLEX, classificationComplexTaxonomy);
	}

	public Taxonomy getClassificationTaxonomy() {
		return taxonomies.get(CLASSIFICATION);
	}

	public Taxonomy getClassificationComplexTaxonomy() {
		return taxonomies.get(CLASSIFICATION_COMPLEX);
	}

	private void setupClassificationTaxonomy(MetadataSchemaTypeBuilder categoryType) {
		categoryType.getDefaultSchema().create("parentCategory").defineChildOfRelationshipToType(categoryType);
	}

	private void setupClassificationComplexTaxonomy(MetadataSchemaTypeBuilder categoryType,
													MetadataSchemaTypeBuilder subCategoryType) {
		subCategoryType.getDefaultSchema().create("parentCategory").defineChildOfRelationshipToType(categoryType);
		subCategoryType.getDefaultSchema().create("parentSubCategory").defineChildOfRelationshipToType(subCategoryType);
	}

	public class Category implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get(CATEGORY);
		}

		@Override
		public String code() {
			return CATEGORY + "_default";
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

		public String parentCategoryCode() {
			return code() + "_parentCategory";
		}

		public Metadata parentCategory() {
			return getMetadata(parentCategoryCode());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

	}

	public class SubCategory implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get(SUB_CATEGORY);
		}

		@Override
		public String code() {
			return SUB_CATEGORY + "_default";
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

		public String parentCategoryCode() {
			return code() + "_parentCategory";
		}

		public String parentSubCategoryCode() {
			return code() + "_parentSubCategory";
		}

		public Metadata parentCategory() {
			return getMetadata(parentCategoryCode());
		}

		public Metadata parentSubCategory() {
			return getMetadata(parentSubCategoryCode());
		}

		public Metadata path() {
			return getMetadata(code() + "_path");
		}

		public Metadata parentpath() {
			return getMetadata(code() + "_parentpath");
		}

	}

	public BaseClassificationRecords givenBaseClassificationRecords(RecordServices recordServices) {
		try {
			return new BaseClassificationRecords(recordServices);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public BaseClassificationComplexRecords givenBaseClassificationComplexRecords(RecordServices recordServices) {
		try {
			return new BaseClassificationComplexRecords(recordServices);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public class BaseClassificationRecords {

		public final Record category_A;
		public final Record category_A_1;
		public final Record category_A_2;
		public final Record category_B;
		public final Record category_B_1;
		public final Record category_B_11;
		public final Record category_B_12;
		public final Record category_C;

		List<Record> records = new ArrayList<>();
		private RecordServices recordServices;

		private BaseClassificationRecords(RecordServices recordServices)
				throws RecordServicesException {
			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			records.add(category_A = createCategory(transaction, "category_A", null));
			records.add(category_A_1 = createCategory(transaction, "category_A_1", category_A));
			records.add(category_A_2 = createCategory(transaction, "category_A_2", category_A));
			records.add(category_B = createCategory(transaction, "category_B", null));
			records.add(category_B_1 = createCategory(transaction, "category_B_1", category_B));
			records.add(category_B_11 = createCategory(transaction, "category_B_11", category_B_1));
			records.add(category_B_12 = createCategory(transaction, "category_B_12", category_B_1));
			records.add(category_C = createCategory(transaction, "category_C", null));

			recordServices.execute(transaction);
		}

		private Record createCategory(Transaction transaction, String id, Record parentCategory) {
			Record record = new TestRecord(category, id);
			record.set(category.title(), id);
			record.set(category.parentCategory(), parentCategory);
			transaction.addUpdate(record);
			return record;
		}

	}

	public class BaseClassificationComplexRecords {

		public final Record category_A;
		public final Record subCategory_SA1;
		public final Record subCategory_SA2;
		public final Record subCategory_SA2_1;
		public final Record category_A_1;
		public final Record category_A_2;
		public final Record category_B;
		public final Record category_B_1;
		public final Record category_C;

		List<Record> records = new ArrayList<>();
		private RecordServices recordServices;

		private BaseClassificationComplexRecords(RecordServices recordServices)
				throws RecordServicesException {
			this.recordServices = recordServices;

			Transaction transaction = new Transaction();

			records.add(category_A = createCategory(transaction, "category_A", null));
			records.add(subCategory_SA1 = createSubCategory(transaction, "subCategory_SA1", category_A, null));
			records.add(subCategory_SA2 = createSubCategory(transaction, "subCategory_SA2", category_A, null));
			records.add(subCategory_SA2_1 = createSubCategory(transaction, "subCategory_SA2_1", null, subCategory_SA2));
			records.add(category_A_1 = createCategory(transaction, "category_A_1", category_A));
			records.add(category_A_2 = createCategory(transaction, "category_A_2", category_A));
			records.add(category_B = createCategory(transaction, "category_B", null));
			records.add(category_B_1 = createCategory(transaction, "category_B_1", category_B));
			records.add(category_C = createCategory(transaction, "category_C", null));

			recordServices.execute(transaction);
		}

		private Record createCategory(Transaction transaction, String id, Record parentCategory) {
			Record record = new TestRecord(category, id);
			record.set(category.title(), id);
			record.set(category.parentCategory(), parentCategory);
			transaction.addUpdate(record);
			return record;
		}

		private Record createSubCategory(Transaction transaction, String id, Record parentCategory,
										 Record parentSubCategory) {
			Record record = new TestRecord(subCategory, id);
			record.set(subCategory.title(), id);
			record.set(subCategory.parentCategory(), parentCategory);
			record.set(subCategory.parentSubCategory(), parentSubCategory);
			transaction.addUpdate(record);
			return record;
		}

	}
}
