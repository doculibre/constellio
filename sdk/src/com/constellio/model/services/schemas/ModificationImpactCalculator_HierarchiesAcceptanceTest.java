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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
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
		doReturn(true).when(searchServices).hasResults(any(LogicalSearchCondition.class));
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();

		defineSchemasManager().using(schemas);

		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
			taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		}
		records = schemas.givenTaxonomyRecords(recordServices);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
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
		impactCalculator = new ModificationImpactCalculator(types, taxonomies, searchServices);
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
	public void givenAllAuthorizationsModifiedOfTaxonomyConceptThenHasImpactOnTaxonomyChildren()
			throws Exception {

		TestRecord record = (TestRecord) records.taxo1_firstTypeItem2_firstTypeItem1;

		record.markAsModified(taxonomy1FirstSchema.allAuthorizations());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertAuthorizationsImpactInFirstAndSecondSchema(record, impacts);
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
	public void givenAllAuthorizationsMetadataModifiedOfTaxonomyConceptUsedByFoldersThenHasImpactOnTaxonomyChildrenAndRecordsUsingIt()
			throws Exception {

		TestRecord record = records.taxo1_firstTypeItem2_secondTypeItem1;

		record.markAsModified(folderSchema.allAuthorizations());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertAuthorizationsImpactInSecondSchemaAndFolderSchema(record, impacts);
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
	public void givenARecordAllAuthorizationsModifiedThenHasImpactOnChildren()
			throws Exception {

		TestRecord record = new TestRecord(folderSchema, "zeFolder");
		recordServices.add(record);

		record.markAsModified(folderSchema.allAuthorizations());
		List<ModificationImpact> impacts = impactCalculator
				.findTransactionImpact(new Transaction(record), true);

		assertAuthorizationsImpactInFolderAndDocumentSchema(record, impacts);
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
						Arrays.asList(taxonomy1FirstSchema.parent())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type())
						.whereAny(Arrays.asList(taxonomy1SecondSchema.parentOfType1())).isIn(Arrays.asList(record)));
	}

	private void assertAuthorizationsImpactInSecondSchemaAndFolderSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderAuthorizations = folderSchema.inheritedAuthorizations();
		Metadata taxo1SecondSchemaAuthorizations = taxonomy1SecondSchema.inheritedAuthorizations();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(taxo1SecondSchemaAuthorizations);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(folderAuthorizations);
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
						Arrays.asList(taxonomy1SecondSchema.parentOfType2())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
						.whereAny(Arrays.asList(folderSchema.taxonomy1())).isIn(Arrays.asList(record)));
	}

	private void assertAuthorizationsImpactInFolderAndDocumentSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderAuthorizations = folderSchema.inheritedAuthorizations();
		Metadata documentAuthorizations = documentSchema.inheritedAuthorizations();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(folderAuthorizations);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(documentAuthorizations);
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
						Arrays.asList(folderSchema.parent())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
						.whereAny(Arrays.asList(documentSchema.parent())).isIn(Arrays.asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInFirstAndSecondSchema(TestRecord record, List<ModificationImpact> impacts) {
		Metadata taxo1FirstSchemaMetaWithTaxoDependency = schemas
				.getMetadata(taxonomy1FirstSchema.code() + "_taxo1FirstSchemaMetaWithTaxoDependency");
		Metadata taxo1SecondSchemaMetaWithTaxoDependency = schemas
				.getMetadata(taxonomy1SecondSchema.code() + "_taxo1SecondSchemaMetaWithTaxoDependency");
		Metadata taxo1FirstSchemaPath = taxonomy1FirstSchema.parentpath();
		Metadata taxo1FirstSchemaAuthorizations = taxonomy1FirstSchema.inheritedAuthorizations();
		Metadata taxo1SecondSchemaPath = taxonomy1SecondSchema.parentpath();
		Metadata taxo1SecondSchemaAuthorizations = taxonomy1SecondSchema.inheritedAuthorizations();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(taxo1FirstSchemaMetaWithTaxoDependency, taxo1FirstSchemaPath, taxo1FirstSchemaAuthorizations);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(taxo1SecondSchemaMetaWithTaxoDependency, taxo1SecondSchemaPath, taxo1SecondSchemaAuthorizations);
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1FirstSchema.type()).whereAny(
						Arrays.asList(taxonomy1FirstSchema.parent())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type())
						.whereAny(Arrays.asList(taxonomy1SecondSchema.parentOfType1())).isIn(Arrays.asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInSecondSchemaAndFolderSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderPath = folderSchema.parentpath();
		Metadata folderAuthorizations = folderSchema.inheritedAuthorizations();
		Metadata folderMetaWithTaxoDependency = schemas
				.getMetadata(folderSchema.code() + "_folderMetaWithTaxoDependency");
		Metadata taxo1SecondSchemaPath = taxonomy1SecondSchema.parentpath();
		Metadata taxo1SecondSchemaAuthorizations = taxonomy1SecondSchema.inheritedAuthorizations();
		Metadata taxo1SecondSchemaMetaWithTaxoDependency = schemas
				.getMetadata(taxonomy1SecondSchema.code() + "_taxo1SecondSchemaMetaWithTaxoDependency");

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(taxo1SecondSchemaMetaWithTaxoDependency, taxo1SecondSchemaPath, taxo1SecondSchemaAuthorizations);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(folderMetaWithTaxoDependency, folderPath, folderAuthorizations);
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(taxonomy1SecondSchema.type()).whereAny(
						Arrays.asList(taxonomy1SecondSchema.parentOfType2())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type())
						.whereAny(Arrays.asList(folderSchema.taxonomy1())).isIn(Arrays.asList(record)));
	}

	private void assertPathAndAuthorizationsImpactInFolderAndDocumentSchema(TestRecord record,
			List<ModificationImpact> impacts) {
		Metadata folderMetaWithTaxoDependency = schemas
				.getMetadata(folderSchema.code() + "_folderMetaWithTaxoDependency");
		Metadata documentMetaWithTaxoDependency = schemas
				.getMetadata(documentSchema.code() + "_documentMetaWithTaxoDependency");
		Metadata folderPath = folderSchema.parentpath();
		Metadata documentPath = documentSchema.parentpath();
		Metadata folderAuthorizations = folderSchema.inheritedAuthorizations();
		Metadata documentAuthorizations = documentSchema.inheritedAuthorizations();

		assertThat(impacts).hasSize(2);
		assertThat(impacts.get(1).getMetadataToReindex())
				.containsOnly(folderMetaWithTaxoDependency, folderPath, folderAuthorizations);
		assertThat(impacts.get(0).getMetadataToReindex())
				.containsOnly(documentMetaWithTaxoDependency, documentPath, documentAuthorizations);
		assertThat(impacts.get(1).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(folderSchema.type()).whereAny(
						Arrays.asList(folderSchema.parent())).isIn(Arrays.asList(record)));
		assertThat(impacts.get(0).getLogicalSearchCondition())
				.isEqualTo(LogicalSearchQueryOperators.from(documentSchema.type())
						.whereAny(Arrays.asList(documentSchema.parent())).isIn(Arrays.asList(record)));
	}

	private Record newFolder(String title, Record parentFolder, Record taxonomy1, Record... taxonomy2s) {
		Record record = recordServices.newRecordWithSchema(folderSchema.instance());
		record.set(folderSchema.title(), title);
		record.set(folderSchema.parent(), parentFolder);
		record.set(folderSchema.taxonomy1(), taxonomy1);
		record.set(folderSchema.taxonomy2(), Arrays.asList(taxonomy2s));
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
			return Arrays.asList(taxonomies);
		}
	}

}
