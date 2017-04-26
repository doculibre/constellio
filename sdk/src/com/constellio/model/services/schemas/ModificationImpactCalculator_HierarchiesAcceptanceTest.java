package com.constellio.model.services.schemas;

import static com.constellio.model.entities.schemas.Schemas.PATH;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ALL_REMOVED_AUTHS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.ATTACHED_ANCESTORS;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.INHERITED_AUTHORIZATIONS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.HierarchyDependencyValue;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.calculators.dependencies.SpecialDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.CollectionSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.DocumentSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2CustomSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2DefaultSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;

public class ModificationImpactCalculator_HierarchiesAcceptanceTest extends ConstellioTest {

	List<Metadata> noAlreadyReindexedMetadata = Collections.emptyList();

	RecordServices recordServices;

	MetadataSchemasManager schemasManager;

	ModificationImpactCalculator impactCalculator;

	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas =
			new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();
	CollectionSchema collectionSchema = schemas.new CollectionSchema();
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();

	TaxonomyRecords records;

	Record aFolderWithTaxonomy;
	Record aFolderWithoutTaxonomy;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = spy(getModelLayerFactory().newSearchServices());
		doReturn(1L).when(searchServices).getResultsCount(any(LogicalSearchCondition.class));
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		defineSchemasManager().using(schemas);

		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
			taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		}
		records = schemas.givenTaxonomyRecords(recordServices);

		MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify("zeCollection");
		typesBuilder.getSchema(taxonomy1FirstSchema.code()).create("taxo1FirstSchemaMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		typesBuilder.getSchema(taxonomy1SecondSchema.code()).create("taxo1SecondSchemaMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		typesBuilder.getSchema(folderSchema.code()).create("folderMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		typesBuilder.getSchema(documentSchema.code()).create("documentMetaWithTaxoDependency")
				.setType(MetadataValueType.STRING).defineDataEntry().asCalculated(DummyCalculatorWithTaxonomyDependency.class);
		schemas.onSchemaBuilt(schemasManager.saveUpdateSchemaTypes(typesBuilder));

		aFolderWithTaxonomy = recordServices.newRecordWithSchema(folderSchema.instance());
		aFolderWithTaxonomy.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem1);
		aFolderWithoutTaxonomy = recordServices.newRecordWithSchema(folderSchema.instance());

		Transaction transaction = new Transaction();
		transaction.addUpdate(aFolderWithTaxonomy);
		transaction.addUpdate(aFolderWithoutTaxonomy);
		recordServices.execute(transaction);

		List<Taxonomy> taxonomies = getModelLayerFactory().getTaxonomiesManager().getEnabledTaxonomies(zeCollection);
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		impactCalculator = new ModificationImpactCalculator(types, taxonomies, searchServices, recordServices);
	}

	@Test
	public void givenPathModifiedOfTaxonomyConceptThenHasImpactOnTaxonomyChildren()
			throws Exception {

		TestRecord record = (TestRecord) records.taxo1_firstTypeItem2_firstTypeItem1;

		record.markAsModified(taxonomy1FirstSchema.path());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertPathAndAuthorizationsImpactInFirstAndSecondSchema(record, impacts);
	}

	@Test
	public void givenTrivialMetadataModifiedOfTaxonomyConceptThenNoImpactOnTaxonomyChildren()
			throws Exception {

		TestRecord record = records.taxo1_firstTypeItem2_firstTypeItem1;

		record.set(taxonomy1FirstSchema.title(), "newTitle");
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertThat(impacts).isEmpty();
	}

	@Test
	public void givenPathMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
			throws Exception {

		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;

		record.markAsModified(folderSchema.path());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertPathAndAuthorizationsImpactInSecondSchemaAndFolderSchema(record, impacts);
	}

	@Test
	public void givenAllRemovedAuthsMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
			throws Exception {

		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;

		record.markAsModified(folderSchema.allRemovedAuths());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertAllRemovedAuthImpactInSecondSchemaAndFolderSchema(record, impacts);
	}

	@Test
	public void givenTrivialMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasNoImpactOnTaxonomyChildrenAndRecordsUsingIt()
			throws Exception {

		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;

		record.set(taxonomy1SecondSchema.title(), "newTitle");
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertThat(impacts).isEmpty();
	}

	@Test
	public void givenARecordPathModifiedThenHasImpactOnChildren()
			throws Exception {

		TestRecord record = new TestRecord(folderSchema, "zeFolder");
		recordServices.add(record);

		record.markAsModified(folderSchema.path());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertPathAndAuthorizationsImpactInFolderAndDocumentSchema(record, impacts);
	}

	@Test
	public void givenARecordAttachedAncestorsModifiedThenHasImpactOnChildren()
			throws Exception {

		TestRecord record = new TestRecord(folderSchema, "zeFolder");
		recordServices.add(record);

		record.markAsModified(folderSchema.attachedAncestors());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertAttachedAncestorsImpactInFolderAndDocumentSchema(record, impacts);
	}

	@Test
	public void givenTrivialMetadataOfRecordsModifiedThenHasNoImpactOnChildren()
			throws Exception {

		TestRecord record = new TestRecord(folderSchema, "zeFolder");
		recordServices.add(record);

		record.set(folderSchema.title(), "newTitle");
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertThat(impacts).isEmpty();
	}

	// ------------------------------------------------------------------------------------------------------------

	private void assertAuthorizationsImpactInFirstAndSecondSchema(TestRecord record, List<ModificationImpact> impacts) {
		Metadata taxo1FirstSchemaAuthorizations = taxonomy1FirstSchema.inheritedAuthorizations();
		Metadata taxo1SecondSchemaAuthorizations = taxonomy1SecondSchema.inheritedAuthorizations();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(taxo1FirstSchemaAuthorizations);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(taxo1SecondSchemaAuthorizations);
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1FirstSchema.type()).whereAny(
						asList(taxonomy1FirstSchema.parent())).isIn(asList(record)));
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type())
						.whereAny(asList(taxonomy1SecondSchema.parentOfType1())).isIn(asList(record)));
	}

	private void assertAllRemovedAuthImpactInSecondSchemaAndFolderSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderAllRemovedAuths = folderSchema.allRemovedAuths();
		Metadata taxo1SecondSchemaAllRemovedAuths = taxonomy1SecondSchema.allRemovedAuths();

		assertThat(impacts).extracting("metadataToReindex")
				.containsExactly(asList(folderAllRemovedAuths), asList(taxo1SecondSchemaAllRemovedAuths));
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)));
	}

	private void assertAttachedAncestorsImpactInFolderAndDocumentSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderAuthorizations = folderSchema.attachedAncestors();
		Metadata documentAuthorizations = documentSchema.attachedAncestors();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(folderAuthorizations);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(documentAuthorizations);
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
						asList(folderSchema.parent())).isIn(asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
						.whereAny(asList(documentSchema.parent())).isIn(asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInFirstAndSecondSchema(TestRecord record, List<ModificationImpact> impacts) {

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(0).getMetadataToReindex()).extracting("localCode")
				.containsOnly("allRemovedAuths", "attachedAncestors", "path", "taxo1FirstSchemaMetaWithTaxoDependency",
						"inheritedauthorizations");
		assertThat(impacts.get(1).getMetadataToReindex()).extracting("localCode")
				.containsOnly("allRemovedAuths", "taxo1SecondSchemaMetaWithTaxoDependency", "attachedAncestors", "path",
						"inheritedauthorizations");
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1FirstSchema.type()).whereAny(
						asList(taxonomy1FirstSchema.parent())).isIn(asList(record)));
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type())
						.whereAny(asList(taxonomy1SecondSchema.parentOfType1())).isIn(asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInSecondSchemaAndFolderSchema(TestRecord record,
			List<ModificationImpact> impacts) {

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex()).extracting("localCode")
				.containsOnly("allRemovedAuths", "taxo1SecondSchemaMetaWithTaxoDependency", "attachedAncestors", "path",
						"inheritedauthorizations");
		assertThat(impacts.get(0).getMetadataToReindex()).extracting("localCode")
				.containsOnly("allRemovedAuths", "folderMetaWithTaxoDependency", "attachedAncestors", "path",
						"inheritedauthorizations");
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
						asList(taxonomy1SecondSchema.parentOfType2())).isIn(asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
						.whereAny(asList(folderSchema.taxonomy1())).isIn(asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInFolderAndDocumentSchema(TestRecord record,
			List<ModificationImpact> impacts) {

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex()).extracting("localCode")
				.containsOnly("folderMetaWithTaxoDependency", ALL_REMOVED_AUTHS, INHERITED_AUTHORIZATIONS,
						ATTACHED_ANCESTORS, "path");
		assertThat(impacts.get(0).getMetadataToReindex()).extracting("localCode")
				.containsOnly("documentMetaWithTaxoDependency", ALL_REMOVED_AUTHS, INHERITED_AUTHORIZATIONS,
						ATTACHED_ANCESTORS, "path");
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
						asList(folderSchema.parent())).isIn(asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
						.whereAny(asList(documentSchema.parent())).isIn(asList(record)));
	}

	private Record newFolder(String title, Record parentFolder, Record taxonomy1, Record... taxonomy2s) {
		Record record = recordServices.newRecordWithSchema(folderSchema.instance());
		record.set(folderSchema.title(), title);
		record.set(folderSchema.parent(), parentFolder);
		record.set(folderSchema.taxonomy1(), taxonomy1);
		record.set(folderSchema.taxonomy2(), asList(taxonomy2s));
		return record;
	}

	private Record newDocument(String title, Record folder) {
		Record record = recordServices.newRecordWithSchema(documentSchema.instance());
		record.set(documentSchema.title(), title);
		record.set(documentSchema.parent(), folder);
		return record;
	}

	public static class DummyCalculatorWithTaxonomyDependency implements MetadataValueCalculator<String> {

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
