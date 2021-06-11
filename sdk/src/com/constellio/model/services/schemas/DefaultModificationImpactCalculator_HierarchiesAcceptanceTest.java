package com.constellio.model.services.schemas;

import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.List;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class DefaultModificationImpactCalculator_HierarchiesAcceptanceTest extends ConstellioTest {

	@Test
	public void rewriteMe() {
		throw new RuntimeException("Rewrite me!");
	}

//
//	List<Metadata> noAlreadyReindexedMetadata = Collections.emptyList();
//
//	RecordServices recordServices;
//
//	MetadataSchemasManager schemasManager;
//
//	ModificationImpactCalculator impactCalculator;
//
//	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas =
//			new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
//	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
//	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
//	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
//	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();
//	CollectionSchema collectionSchema = schemas.new CollectionSchema();
//	FolderSchema folderSchema = schemas.new FolderSchema();
//	DocumentSchema documentSchema = schemas.new DocumentSchema();
//
//	TaxonomyRecords records;
//
//	Record aFolderWithTaxonomy;
//	Record aFolderWithoutTaxonomy;
//	SearchServices searchServices;
//
//	@Before
//	public void setUp()
//			throws Exception {
//
//		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
//		searchServices = spy(getModelLayerFactory().newSearchServices());
//		doReturn(1L).when(searchServices).getResultsCount(any(LogicalSearchCondition.class));
//		doReturn(1L).when(searchServices).getResultsCount(any(LogicalSearchQuery.class));
//		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
//		recordServices = getModelLayerFactory().newRecordServices();
//
//		defineSchemasManager().using(schemas);
//
//		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
//			taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
//		}
//		records = schemas.givenTaxonomyRecords(recordServices);
//
//		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify("zeCollection");
//		typesBuilder.getSchema(taxonomy1FirstSchema.code()).create("taxo1FirstSchemaMetaWithTaxoDependency")
//				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
//		typesBuilder.getSchema(taxonomy1SecondSchema.code()).create("taxo1SecondSchemaMetaWithTaxoDependency")
//				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
//		typesBuilder.getSchema(folderSchema.code()).create("folderMetaWithTaxoDependency")
//				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
//		typesBuilder.getSchema(documentSchema.code()).create("documentMetaWithTaxoDependency")
//				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
//		schemas.onSchemaBuilt(schemasManager.saveUpdateSchemaTypes(typesBuilder));
//
//		aFolderWithTaxonomy = recordServices.newRecordWithSchema(folderSchema.instance());
//		aFolderWithTaxonomy.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1);
//		aFolderWithoutTaxonomy = recordServices.newRecordWithSchema(folderSchema.instance());
//
//		Transaction transaction = new Transaction();
//		transaction.addUpdate(aFolderWithTaxonomy);
//		transaction.addUpdate(aFolderWithoutTaxonomy);
//		recordServices.execute(transaction);
//
//		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
//		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
//		impactCalculator = new ModificationImpactCalculator(types, taxonomies, searchServices, recordServices,
//				new CacheBasedTaxonomyVisitingServices(getModelLayerFactory()));
//	}
//
//	@Test
//	public void givenPathModifiedOfTaxonomyConceptThenHasImpactOnTaxonomyChildren()
//			throws Exception {
//		givenNoCaches();
//		TestRecord record = (TestRecord) records.taxo1_firstTypeItem2_firstTypeItem1;
//
//		record.markAsModified(taxonomy1FirstSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertPathAndAuthorizationsImpactInFirstAndSecondSchema(record, response.getImpacts());
//	}
//
//	private void givenNoCaches() {
//		schemas.modify(new MetadataSchemaTypesAlteration() {
//			@Override
//			public void alter(MetadataSchemaTypesBuilder types) {
//				types.getSchemaType(taxonomy1FirstSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//				types.getSchemaType(taxonomy1SecondSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//				types.getSchemaType(folderSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//				types.getSchemaType(documentSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//				types.getSchemaType(taxonomy2DefaultSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//				types.getSchemaType(collectionSchema.type().getCode()).setRecordCacheType(NOT_CACHED);
//
//
//			}
//		});
//
//		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
//		MetadataSchemaTypes zeTypes = schemasManager.getSchemaTypes(zeCollection);
//		impactCalculator = new ModificationImpactCalculator(zeTypes, taxonomies, searchServices, recordServices, new CacheBasedTaxonomyVisitingServices(getModelLayerFactory()));
//	}
//
//	@Test
//	public void givenTrivialMetadataModifiedOfTaxonomyConceptThenNoImpactOnTaxonomyChildren()
//			throws Exception {
//
//		givenNoCaches();
//		TestRecord record = records.taxo1_firstTypeItem2_firstTypeItem1;
//
//		record.set(taxonomy1FirstSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenPathMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		givenNoCaches();
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.markAsModified(folderSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(2);
//		assertThat(response.getImpacts().get(1).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "folderMetaWithTaxoDependency", "attachedAncestors", "path",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "attachedAncestors", "path", "taxo1SecondSchemaMetaWithTaxoDependency",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(0)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
//						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(1)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
//						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//
//	}
//
//	@Test
//	public void givenAllRemovedAuthsMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		givenNoCaches();
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.markAsModified(folderSchema.allRemovedAuths());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//
//		Metadata folderAllRemovedAuths = folderSchema.allRemovedAuths();
//		Metadata taxo1SecondSchemaAllRemovedAuths = taxonomy1SecondSchema.allRemovedAuths();
//
//		assertThat(response.getImpacts()).extracting("metadataToReindex")
//				.containsExactly(asList(taxo1SecondSchemaAllRemovedAuths), asList(folderAllRemovedAuths));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(0)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
//						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(1)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
//						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//	}
//
//	@Test
//	public void givenTrivialMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasNoImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		givenNoCaches();
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.set(taxonomy1SecondSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenARecordPathModifiedThenHasImpactOnChildren()
//			throws Exception {
//
//		givenNoCaches();
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		record.markAsModified(folderSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(2);
//		assertThat(response.getImpacts().get(1).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("folderMetaWithTaxoDependency", ALL_REMOVED_AUTHS,
//						ATTACHED_ANCESTORS, "path", "attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds",
//						"principalAncestorsIntIds", "detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("documentMetaWithTaxoDependency", ALL_REMOVED_AUTHS,
//						ATTACHED_ANCESTORS, "path", "attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds",
//						"principalAncestorsIntIds", "detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(1)).getLogicalSearchCondition())
//				.usingComparator(comparatorBasedOnSolrQuery())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
//						asList(folderSchema.parent())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record)));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(0)).getLogicalSearchCondition())
//				.usingComparator(comparatorBasedOnSolrQuery())
//				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
//						.whereAny(asList(documentSchema.parent())).isIn(asList(record))
//						.andWhere(Schemas.IDENTIFIER).isNotIn(asList(record)));
//	}
//
//	@Test
//	public void givenARecordAttachedAncestorsModifiedThenHasImpactOnChildren()
//			throws Exception {
//
//		givenNoCaches();
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		record.markAsModified(folderSchema.attachedAncestors());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		Metadata folderAttachedAncestors = folderSchema.attachedAncestors();
//		Metadata documentAttachedAncestors = documentSchema.attachedAncestors();
//
//		assertThat(response.getImpacts()).hasSize(2);
//		assertThat(response.getImpacts().get(1).getMetadataToReindex())
//				.containsOnly(folderAttachedAncestors);
//		assertThat(response.getImpacts().get(0).getMetadataToReindex())
//				.containsOnly(documentAttachedAncestors);
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(1)).getLogicalSearchCondition())
//				.usingComparator(comparatorBasedOnSolrQuery())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
//						asList(folderSchema.parent())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record)));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)response.getImpacts().get(0)).getLogicalSearchCondition())
//				.usingComparator(comparatorBasedOnSolrQuery())
//				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
//						.whereAny(asList(documentSchema.parent())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record)));
//	}
//
//	@Test
//	public void givenTrivialMetadataOfRecordsModifiedThenHasNoImpactOnChildren()
//			throws Exception {
//
//		givenNoCaches();
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		record.set(folderSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//
//	@Test
//	public void givenPathModifiedOfCachedTaxonomyConceptWithoutChildrenThenHasNoImpact()
//			throws Exception {
//		TestRecord record = (TestRecord) records.taxo1_firstTypeItem2_firstTypeItem1;
//
//		record.markAsModified(taxonomy1FirstSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenTrivialMetadataModifiedOfCachedTaxonomyConceptWithoutChildremThenNoImpacts()
//			throws Exception {
//
//		TestRecord record = records.taxo1_firstTypeItem2_firstTypeItem1;
//
//		record.set(taxonomy1FirstSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenPathMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithoutChildrenThenHasNoImpact()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.markAsModified(folderSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenAllRemovedAuthsMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithoutChildrenThenHasImpact()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.markAsModified(folderSchema.allRemovedAuths());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenTrivialMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithoutChildrenThenHasNoImpact()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		record.set(taxonomy1SecondSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenACachedRecordPathModifiedOnRecordWithoutChildrenThenHasNoImpact()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		record.markAsModified(folderSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenARecordAttachedAncestorsOfCachedRecordWithoutChildrenModifiedThenHasNoImpact()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		record.markAsModified(folderSchema.attachedAncestors());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	@Test
//	public void givenTrivialMetadataOfCachedRecordsWithoutChildrenModifiedThenHasNoImpact()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//
//		record.set(folderSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//
//	@Test
//	public void givenPathModifiedOfCachedTaxonomyConceptWithChildrenThenHasImpactOnTaxonomyChildren()
//			throws Exception {
//		TestRecord record = (TestRecord) records.taxo1_firstTypeItem2_firstTypeItem2;
//
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "subfolder1"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "subfolder2"));
//
//
//		record.markAsModified(taxonomy1FirstSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(1);
//
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "taxo1SecondSchemaMetaWithTaxoDependency", "attachedAncestors", "path",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(0)).getIds())
//				.containsOnly(records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1.getId(),
//						records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2.getId());
//	}
//
//
//	@Test
//	public void givenTrivialMetadataModifiedOfCachedTaxonomyConceptWithChildrenThenNoImpactOnTaxonomyChildren()
//			throws Exception {
//
//		TestRecord record = records.taxo1_firstTypeItem2_firstTypeItem1;
//
//		record.set(taxonomy1FirstSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//
//	}
//
//	@Test
//	public void givenPathMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithChildrenThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "subfolder1"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "subfolder2"));
//
//		record.markAsModified(this.taxonomy1SecondSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(1);
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "folderMetaWithTaxoDependency", "attachedAncestors", "path",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(0)).getIds())
//				.containsOnly("subfolder1", "subfolder2");
//	}
//
//	@Test
//	public void givenAllRemovedAuthsMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithChildrenThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "subfolder1"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "subfolder2"));
//
//		record.markAsModified(folderSchema.allRemovedAuths());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(1);
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode").containsOnly("allRemovedAuths");
//
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(0)).getIds())
//				.containsOnly("subfolder1", "subfolder2");
//	}
//
//	@Test
//	public void givenTrivialMetadataModifiedOfTaxonomyConceptUsedByCachedFoldersWithChildrenThenHasNoImpactOnTaxonomyChildrenAndRecordsUsingIt()
//			throws Exception {
//		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;
//
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.taxonomy1(),
//				records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1.getId()));
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "subfolder1"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "subfolder2"));
//
//		record.set(taxonomy1SecondSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//	}
//
//	@Test
//	public void givenACachedRecordPathModifiedOnRecordWithChildrenThenHasImpactOnChildren()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.parent(), "zeFolder"));
//
//		record.markAsModified(folderSchema.path());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//
//		assertThat(response.getImpacts()).hasSize(2);
//		assertThat(response.getImpacts().get(1).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("folderMetaWithTaxoDependency", ALL_REMOVED_AUTHS,
//						ATTACHED_ANCESTORS, "path", "attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds",
//						"principalAncestorsIntIds", "detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(response.getImpacts().get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("documentMetaWithTaxoDependency", ALL_REMOVED_AUTHS,
//						ATTACHED_ANCESTORS, "path", "attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds",
//						"principalAncestorsIntIds", "detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(1)).getIds())
//				.containsOnly("subfolder1", "subfolder2");
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(0)).getIds())
//				.containsOnly("doc1", "doc2");
//	}
//
//	@Test
//	public void givenARecordAttachedAncestorsOfCachedRecordWithChildrensModifiedThenHasImpactOnChildren()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.parent(), "zeFolder"));
//
//		record.markAsModified(folderSchema.attachedAncestors());
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).hasSize(2);
//		assertThat(response.getImpacts().get(1).getMetadataToReindex())
//				.containsOnly(folderSchema.attachedAncestors());
//		assertThat(response.getImpacts().get(0).getMetadataToReindex())
//				.containsOnly(documentSchema.attachedAncestors());
//
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(1)).getIds())
//				.containsOnly("subfolder1", "subfolder2");
//		assertThat(((ReindexingRecordsModificationImpact)response.getImpacts().get(0)).getIds())
//				.containsOnly("doc1", "doc2");
//
//	}
//
//	@Test
//	public void givenTrivialMetadataOfCachedRecordsWithChildrensModifiedThenHasNoImpactOnChildren()
//			throws Exception {
//
//		TestRecord record = new TestRecord(folderSchema, "zeFolder");
//		recordServices.add(record);
//
//		recordServices.add(new TestRecord(documentSchema, "doc1").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(documentSchema, "doc2").set(documentSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder1").set(folderSchema.parent(), "zeFolder"));
//		recordServices.add(new TestRecord(folderSchema, "subfolder2").set(folderSchema.parent(), "zeFolder"));
//
//		record.set(folderSchema.title(), "newTitle");
//		ModificationImpactCalculatorResponse response = impactCalculator
//				.findTransactionImpact(new Transaction(record));
//
//		assertThat(response.getImpacts()).isEmpty();
//	}
//
//	// ------------------------------------------------------------------------------------------------------------
//
//	private void assertAllRemovedAuthImpactInSecondSchemaAndFolderSchema(TestRecord record,
//																		 List<ModificationImpact> impacts) {
//		Metadata folderAllRemovedAuths = folderSchema.allRemovedAuths();
//		Metadata taxo1SecondSchemaAllRemovedAuths = taxonomy1SecondSchema.allRemovedAuths();
//
//		assertThat(impacts).extracting("metadataToReindex")
//				.containsExactly(asList(folderAllRemovedAuths), asList(taxo1SecondSchemaAllRemovedAuths));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(1)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
//						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(0)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
//						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)));
//	}
//
//	private Comparator<? super LogicalSearchCondition> comparatorBasedOnSolrQuery() {
//		return new Comparator<LogicalSearchCondition>() {
//			@Override
//			public int compare(LogicalSearchCondition o1, LogicalSearchCondition o2) {
//				String q1 = o1.getSolrQuery(new SolrQueryBuilderContext(false, asList("fr"), "fr", schemas.getTypes(), Collections.emptyList(), zeCollection));
//				String q2 = o2.getSolrQuery(new SolrQueryBuilderContext(false, asList("fr"), "fr", schemas.getTypes(), Collections.emptyList(), zeCollection));
//				return q1.compareTo(q2);
//			}
//		};
//	}
//
//	private void assertPathAndAuthorizationsImpactInFirstAndSecondSchema(TestRecord record,
//																		 List<ModificationImpact> impacts) {
//
//		assertThat(impacts).hasSize(2);
//		assertThat(impacts.get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "attachedAncestors", "path", "taxo1FirstSchemaMetaWithTaxoDependency",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(impacts.get(1).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "taxo1SecondSchemaMetaWithTaxoDependency", "attachedAncestors", "path",
//						"attachedPrincipalAncestorsIntIds", "secondaryConceptsIntIds", "principalAncestorsIntIds",
//						"detachedPrincipalAncestorsIntIds", "principalConceptsIntIds");
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(0)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1FirstSchema.type()).whereAny(
//						asList(taxonomy1FirstSchema.parent())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(1)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type())
//						.whereAny(asList(taxonomy1SecondSchema.parentOfType1())).isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(record.getId())));
//	}
//
//	private void assertPathAndAuthorizationsImpactInSecondSchemaAndFolderSchema(TestRecord record,
//																				List<ModificationImpact> impacts) {
//
//		assertThat(impacts).hasSize(2);
//		assertThat(impacts.get(1).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "taxo1SecondSchemaMetaWithTaxoDependency", "attachedAncestors", "path");
//		assertThat(impacts.get(0).getMetadataToReindex()).extracting("localCode")
//				.containsOnly("allRemovedAuths", "folderMetaWithTaxoDependency", "attachedAncestors", "path");
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(1)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
//						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)));
//		assertThat(((QueryBasedReindexingBatchProcessModificationImpact)impacts.get(0)).getLogicalSearchCondition())
//				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
//						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)));
//	}
//
//
//	private Record newFolder(String title, Record parentFolder, Record taxonomy1, Record... taxonomy2s) {
//		Record record = recordServices.newRecordWithSchema(folderSchema.instance());
//		record.set(folderSchema.title(), title);
//		record.set(folderSchema.parent(), parentFolder);
//		record.set(folderSchema.taxonomy1(), taxonomy1);
//		record.set(folderSchema.taxonomy2(), asList(taxonomy2s));
//		return record;
//	}
//
//	private Record newDocument(String title, Record folder) {
//		Record record = recordServices.newRecordWithSchema(documentSchema.instance());
//		record.set(documentSchema.title(), title);
//		record.set(documentSchema.parent(), folder);
//		return record;
//	}
//
	public static class DummyCalculatorWithTaxonomyDependency extends AbstractMetadataValueCalculator<String> {

		SpecialDependency<HierarchyDependencyValue> taxonomies = SpecialDependencies.HIERARCHY;

		@Override
		public String calculate(CalculatorParameters parameters) {
			return null;
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
			return asList(taxonomies);
		}
	}

}
