package com.constellio.model.services.records;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.ModificationImpactCalculator_HierarchiesAcceptanceTest.DummyCalculatorWithTaxonomyDependency;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.DocumentSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2CustomSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2DefaultSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;
import org.junit.Before;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordAutomaticMetadataServices_TaxonomiesRealTest extends ConstellioTest {

	RecordUpdateOptions options = new RecordUpdateOptions();
	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas =
			new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();
	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();

	TestRecord rootFolderWithTaxonomy;
	TestRecord subFolderWithTaxonomy;
	TestRecord document;

	TaxonomyRecords records;

	RecordAutomaticMetadataServices services;

	MetadataSchemasManager schemaManager;
	RecordServices recordServices;
	ConfigManager configManager;
	@Mock RecordProvider recordProvider;

	Map<Dependency, Object> values;
	List<String> expectedPaths;
	List<String> authorizations = new ArrayList<>();

	private static String taxo1Path(Record... records) {
		String collection = ""; // = "/zeCollection"
		StringBuilder sb = new StringBuilder(collection + "/taxo1");
		for (Record record : records) {
			sb.append("/");
			sb.append(record.getId());
		}
		return sb.toString();
	}

	private static String taxo2Path(Record... records) {
		String collection = ""; // = "/zeCollection"
		StringBuilder sb = new StringBuilder(collection + "/taxo2");
		for (Record record : records) {
			sb.append("/");
			sb.append(record.getId());
		}
		return sb.toString();
	}

	@Before
	public void setUp()
			throws Exception {

		authorizations.add(aString());
		authorizations.add(aString());

		expectedPaths = new ArrayList<>();
		values = new HashMap<>();
		schemaManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		configManager = getDataLayerFactory().getConfigManager();

		defineSchemasManager().using(schemas);

		MetadataSchemaTypesBuilder types = schemaManager.modify(zeCollection);
		types.getSchema(taxonomy1FirstSchema.code()).create("taxo1FirstSchemaMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		types.getSchema(taxonomy1SecondSchema.code()).create("taxo1SecondSchemaMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		types.getSchema(folderSchema.code()).create("folderMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		types.getSchema(documentSchema.code()).create("documentMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		schemas.onSchemaBuilt(schemaManager.saveUpdateSchemaTypes(types));

		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
			getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, schemaManager);
		}

		records = schemas.givenTaxonomyRecords(recordServices);

		rootFolderWithTaxonomy = new TestRecord(folderSchema, "rootFolderWithTaxonomy");
		rootFolderWithTaxonomy.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1);
		rootFolderWithTaxonomy.set(folderSchema.conceptReferenceWithoutTaxonomyRelationship(),
				records.taxo1_firstTypeItem2_secondTypeItem1);

		subFolderWithTaxonomy = new TestRecord(folderSchema, "subFolderWithTaxonomy");
		subFolderWithTaxonomy.set(folderSchema.parent(), rootFolderWithTaxonomy);
		subFolderWithTaxonomy.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2);
		subFolderWithTaxonomy.set(folderSchema.conceptReferenceWithoutTaxonomyRelationship(),
				records.taxo1_firstTypeItem2_secondTypeItem2);

		document = new TestRecord(documentSchema, "document");
		document.set(documentSchema.parent(), subFolderWithTaxonomy);

		services = new RecordAutomaticMetadataServices(getModelLayerFactory());

		records.mockRecordProviderToReturnRecordsById(recordProvider);
		records.mockRecordProviderToReturnRecordById(recordProvider, rootFolderWithTaxonomy);
		records.mockRecordProviderToReturnRecordById(recordProvider, subFolderWithTaxonomy);
		records.mockRecordProviderToReturnRecordById(recordProvider, document);

		recordServices.execute(new Transaction(rootFolderWithTaxonomy, subFolderWithTaxonomy, document));

	}
	//
	//	@Test
	//	public void whenCalculatingValueForTaxonomyDependencyThenValueCalculated()
	//			throws Exception {
	//
	//		Metadata calculatedMetadata = schemas
	//				.getMetadata(taxonomy1FirstSchema.code() + "_taxo1FirstSchemaMetaWithTaxoDependency");
	//
	//		when(recordProvider.getRecord(records.taxo1_firstTypeItem1.getId())).thenReturn(records.taxo1_firstTypeItem1);
	//		TransactionExecutionContext context = new TransactionExecutionContext(mock(Transaction.class));
	//		TransactionExecutionRecordContext recordContext = new TransactionExecutionRecordContext(rootFolderWithTaxonomy, context);
	//		services.calculateValueInRecord(recordContext, (RecordImpl) rootFolderWithTaxonomy, calculatedMetadata,
	//				recordProvider, schemas.getTypes(), new Transaction(options));
	//
	//		assertThat(records.taxo1_firstTypeItem1.<String>get(calculatedMetadata)).isEqualTo("calculatedValue");
	//	}
	//
	//	@Test
	//	public void givenRootFolderWithAReferenceToAConceptWithoutTaxonomyRelationshipWhenPreparingParentPathsThenEmpty() {
	//
	//		records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1
	//				.updateAutomaticValue(taxonomy1SecondSchema.tokens(), authorizations);
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) rootFolderWithTaxonomy, recordProvider, values,
	//				SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1));
	//		String expectedTaxonomyCode = null;
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths, new ArrayList<String>(),
	//				expectedTaxonomyCode);
	//	}
	//
	//	@Test
	//	public void givenRootFolderUsingTaxonomiesWhenPreparingParentPathsThenReturnPathsOfTaxonomyElements() {
	//
	//		records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1
	//				.updateAutomaticValue(taxonomy1SecondSchema.tokens(), authorizations);
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) rootFolderWithTaxonomy, recordProvider, values,
	//				SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1));
	//		String expectedTaxonomyCode = null;
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths, new ArrayList<String>(),
	//				expectedTaxonomyCode);
	//	}
	//
	//	@Test
	//	public void givenChildFolderUsingTaxonomiesWhenPreparingParentPathsThenReturnPathsOfParentUsingIsChildOfRelationshipAndPathsOfTaxonomyElements() {
	//
	//		records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2
	//				.updateAutomaticValue(taxonomy1SecondSchema.tokens(), Arrays.asList("a", "b"));
	//		rootFolderWithTaxonomy
	//				.updateAutomaticValue(folderSchema.tokens(), Arrays.asList("c"));
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) subFolderWithTaxonomy, recordProvider, values,
	//				SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1, rootFolderWithTaxonomy));
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2));
	//		String expectedTaxonomyCode = null;
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths, Arrays.asList("rootFolderWithTaxonomy"),
	//				expectedTaxonomyCode);
	//
	//	}
	//
	//	@Test
	//	public void givenDocumentNotUsingTaxonomiesWhenPreparingParentPathsThenReturnPathsOfParentUsingIsChildOfRelationship() {
	//
	//		subFolderWithTaxonomy
	//				.updateAutomaticValue(folderSchema.tokens(), authorizations);
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) document, recordProvider, values,
	//				SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1, rootFolderWithTaxonomy, subFolderWithTaxonomy));
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2,
	//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2, subFolderWithTaxonomy));
	//		String expectedTaxonomyCode = null;
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths,
	//				asList("subFolderWithTaxonomy", "rootFolderWithTaxonomy"), expectedTaxonomyCode);
	//
	//	}
	//
	//	@Test
	//	public void givenFirstTypeTaxonomyElementWhenPreparingParentPathThenReturnPathsOfParentWithChildOfRelation() {
	//
	//		records.taxo1_firstTypeItem2.updateAutomaticValue(taxonomy1FirstSchema.tokens(), authorizations);
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) records.taxo1_firstTypeItem2_firstTypeItem2, recordProvider, values,
	//				SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2));
	//		String expectedTaxonomyCode = "taxo1";
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths,
	//				asList("zeCollection_taxo1_firstTypeItem2"), expectedTaxonomyCode);
	//
	//	}
	//
	//	@Test
	//	public void givenSecondTypeTaxonomyElementWhenPreparingParentPathThenReturnPathsOfParentWithChildOfRelation() {
	//
	//		records.taxo1_firstTypeItem2_firstTypeItem2
	//				.updateAutomaticValue(taxonomy1FirstSchema.tokens(), authorizations);
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1,
	//				recordProvider, values, SpecialDependencies.HIERARCHY);
	//
	//		expectedPaths.add(taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem2));
	//		String expectedTaxonomyCode = "taxo1";
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths,
	//				asList("zeCollection_taxo1_firstTypeItem2_firstTypeItem2", "zeCollection_taxo1_firstTypeItem2"),
	//				expectedTaxonomyCode);
	//
	//	}
	//
	//	@Test
	//	public void givenRootFirstTypeTaxonomyElementWhenPreparingParentPathThenReturnTaxonomyPathAndAuthorizations() {
	//
	//		services.addValueForTaxonomyDependency((RecordImpl) records.taxo1_firstTypeItem2,
	//				recordProvider, values, SpecialDependencies.HIERARCHY);
	//
	//		String expectedTaxonomyCode = "taxo1";
	//		assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(expectedPaths, new ArrayList<String>(),
	//				expectedTaxonomyCode);
	//	}
	//
	//	private void assertThatValuesOnlyContainsSpecialObjectWithPathsAndTaxonomies(List<String> expectedPaths,
	//																				 List<String> expectedAttachedAncestors,
	//																				 String expectedTaxonomyCode) {
	//
	//		assertThat(values).containsKey(SpecialDependencies.HIERARCHY).hasSize(1);
	//		HierarchyDependencyValue hierarchyDependencyValue = (HierarchyDependencyValue) values.get(SpecialDependencies.HIERARCHY);
	//
	//		if (expectedTaxonomyCode == null) {
	//			assertThat(hierarchyDependencyValue.getTaxonomy()).isNull();
	//		} else {
	//			assertThat(hierarchyDependencyValue.getTaxonomy()).isNotNull();
	//			assertThat(hierarchyDependencyValue.getTaxonomy().getCode()).isEqualTo(expectedTaxonomyCode);
	//		}
	//
	//		assertThat(hierarchyDependencyValue.getPaths()).containsAll(expectedPaths).hasSize(expectedPaths.size());
	//		assertThat(hierarchyDependencyValue.getAttachedAncestors())
	//				.containsOnly(expectedAttachedAncestors.toArray(new String[0]));
	//	}
	//
	//	private void assertThatPathIsEqualTo(Record record, String path) {
	//		Metadata pathMetadata = schemas.getMetadata(record.getSchemaCode() + "_path");
	//		assertThat(record.<List<String>>get(pathMetadata)).isEqualTo(Arrays.asList(path));
	//	}
	//
	//	private void assertThatPathIsEqualTo(Record record, List<String> paths) {
	//		Metadata pathMetadata = schemas.getMetadata(record.getSchemaCode() + "_path");
	//		assertThat(record.<List<String>>get(pathMetadata)).isEqualTo(paths);
	//	}
	//
	//	public static class DummyCalculatorWithTaxonomyDependency extends AbstractMetadataValueCalculator<String> {
	//
	//		SpecialDependency<HierarchyDependencyValue> taxonomies = SpecialDependencies.HIERARCHY;
	//
	//		@Override
	//		public String calculate(CalculatorParameters parameters) {
	//			return "calculatedValue";
	//		}
	//
	//		@Override
	//		public String getDefaultValue() {
	//			return null;
	//		}
	//
	//		@Override
	//		public MetadataValueType getReturnType() {
	//			return MetadataValueType.STRING;
	//		}
	//
	//		@Override
	//		public boolean isMultiValue() {
	//			return false;
	//		}
	//
	//		@Override
	//		public List<? extends Dependency> getDependencies() {
	//			return Arrays.asList(taxonomies);
	//		}
	//	}
}
