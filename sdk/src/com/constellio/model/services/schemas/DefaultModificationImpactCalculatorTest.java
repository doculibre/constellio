package com.constellio.model.services.schemas;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.QueryBasedReindexingBatchProcessModificationImpact;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.CacheBasedTaxonomyVisitingServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.mockMetadata;
import static java.util.Arrays.asList;
import static org.apache.ignite.internal.util.lang.GridFunc.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DefaultModificationImpactCalculatorTest extends ConstellioTest {


	@Test
	public void rewriteMe() {
		throw new RuntimeException("Rewrite me!");
	}

		MetadataList modifiedMetadatas;
		Metadata modifiedMetadata = mockMetadata("zeSchema_default_anotherMetadata");
		@Mock Record record;
		String recordId = aString();

		ModificationImpactCalculator impactCalculator;

		@Mock TaxonomiesManager taxonomiesManager;
		@Mock List<Taxonomy> taxonomies;

		@Mock Metadata firstAutomaticMetadata;
		@Mock Metadata secondAutomaticMetadata;
		@Mock Metadata thirdAutomaticMetadata;

		Metadata firstReference = mockMetadata("zeType_default_firstReference");
		Metadata secondReference = mockMetadata("zeType_default_secondReference");
		Metadata thirdReference = mockMetadata("zeType_default_thirdReference");

		@Mock MetadataSchemaTypes schemaTypes;
		@Mock MetadataSchemaType schemaType;
		@Mock MetadataSchema defaultSchema;

		Metadata anotherSchemaCopiedMetadata = mockMetadata("anotherType_default_anotherMetadata");

		@Mock List<String> transactionRecordsList;
		@Mock SearchServices searchServices;
		@Mock RecordServices recordServices;

		String zeSchemaMetadataCode = aString();
		String anotherSchemaReferenceToZeSchemaMetadataCode = aString();

		@Mock RecordsModification recordsModification;

		List<Metadata> alreadyReindexedMetadata;

		List<Metadata> returnedMetadatas;

		@Mock MetadataValueCalculator<?> calculator;
		Metadata anotherSchemaTitle = mockMetadata("anotherType_default_title");
		Metadata anotherSchemaUnmodifiedField = mockMetadata("anotherType_default_unmodifiedField");
		Metadata notTheSameTypeSchemaTitle = mockMetadata("notTheSameType_default_title");

		Metadata calculatedMetadata = mockMetadata("zeType_default_calculated");
		Metadata copiedTitleUsingReferenceToAnotherSchema = mockMetadata("zeType_default_copiedTitle");
		Metadata referenceToAnotherSchema = mockMetadata("zeType_default_ref");

		Metadata anotherReferenceToAnotherSchema = mockMetadata("zeType_default_ref2");
		Metadata referenceToNotSameSchema = mockMetadata("zeType_default_refToDifferentSchema");

		@Mock MetadataSchemaTypes types;

		@Mock CacheBasedTaxonomyVisitingServices visitingServices;

		@Before
		public void setUp()
				throws Exception {

			taxonomies = new ArrayList<>();

			alreadyReindexedMetadata = new ArrayList<>();

			modifiedMetadatas = new MetadataList();
			modifiedMetadatas.add(modifiedMetadata);

			when(record.getId()).thenReturn(recordId);
			when(record.isSaved()).thenReturn(true);
			when(record.getSchemaCode()).thenReturn("zeSchema_default");
			when(record.getModifiedMetadatas(schemaTypes)).thenReturn(modifiedMetadatas);
			when(schemaType.getDefaultSchema()).thenReturn(defaultSchema);
			when(schemaTypes.getCollection()).thenReturn("zeCollection");

			when(firstReference.getType()).thenReturn(MetadataValueType.REFERENCE);
			when(secondReference.getType()).thenReturn(MetadataValueType.REFERENCE);
			when(thirdReference.getType()).thenReturn(MetadataValueType.REFERENCE);

			when(taxonomiesManager.getEnabledTaxonomies("zeCollection")).thenReturn(taxonomies);

			impactCalculator = spy(new ModificationImpactCalculator(schemaTypes, taxonomies, searchServices, recordServices, visitingServices));

			when(schemaTypes.getMetadata("anotherType_default_title")).thenReturn(anotherSchemaTitle);

			when(schemaTypes.getMetadata("anotherType_default_unmodifiedField")).thenReturn(anotherSchemaUnmodifiedField);

			when(schemaTypes.getMetadata("notTheSameType_default_title")).thenReturn(notTheSameTypeSchemaTitle);

			when(referenceToAnotherSchema.getAllowedReferences()).thenReturn(new AllowedReferences("anotherType", null));
			when(schemaTypes.getMetadata("zeType_default_ref")).thenReturn(referenceToAnotherSchema);

			when(anotherReferenceToAnotherSchema.getAllowedReferences())
					.thenReturn(new AllowedReferences(null, asSet("anotherType_default")));
			when(schemaTypes.getMetadata("zeType_default_ref2")).thenReturn(anotherReferenceToAnotherSchema);

			when(referenceToNotSameSchema.getAllowedReferences()).thenReturn(new AllowedReferences("differentType", null));
			when(schemaTypes.getMetadata("zeType_default_refToDifferentSchema")).thenReturn(referenceToNotSameSchema);

			when(copiedTitleUsingReferenceToAnotherSchema.getDataEntry())
					.thenReturn(new CopiedDataEntry("zeType_default_ref", "anotherType_default_title"));
			when(schemaTypes.getMetadata("zeType_default_copiedTitle")).thenReturn(copiedTitleUsingReferenceToAnotherSchema);
			when(calculatedMetadata.getDataEntry()).thenReturn(new CalculatedDataEntry(calculator));
			when(schemaTypes.getMetadata("zeType_default_calculated")).thenReturn(calculatedMetadata);
		}



		@Test
		public void whenCalculatingModificationImpactThenReturnModificationImpactBasedOnReferences()
				throws Exception {
			when(searchServices.getResultsCount(any(LogicalSearchCondition.class))).thenReturn(1L);
			List<Metadata> firstMetadataReferences = asList(firstReference, secondReference);
			List<Metadata> secondMetadataReferences = asList(secondReference, thirdReference);
			List<Metadata> thirdMetadataReferences = new ArrayList<>();
			doReturn(firstMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					firstAutomaticMetadata, modifiedMetadatas);
			doReturn(secondMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					secondAutomaticMetadata, modifiedMetadatas);
			doReturn(thirdMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					thirdAutomaticMetadata, modifiedMetadatas);

			List<Metadata> automaticMetadatas = asList(firstAutomaticMetadata, secondAutomaticMetadata, thirdAutomaticMetadata);
			when(schemaType.getAutomaticMetadatas()).thenReturn(automaticMetadatas);
			when(schemaType.getCacheType()).thenReturn(RecordCacheType.FULLY_CACHED);

			RecordsModification modification = new RecordsModification(asList(record), asList(modifiedMetadata),
					schemaType, new RecordUpdateOptions());
			List<ModificationImpact> modificationImpacts = impactCalculator.findImpactOfARecordsModification(
					modification, asList(recordId), "zeTitle");

			assertThat(modificationImpacts).hasSize(1);
			assertThat(modificationImpacts.get(0).getMetadataToReindex()).containsOnly(firstAutomaticMetadata,
					secondAutomaticMetadata);

			LogicalSearchCondition expectedCondition = from(schemaType).whereAny(
					asList(firstReference, secondReference, thirdReference)).isIn(asList(record)).andWhere(
					Schemas.IDENTIFIER).isNotIn(asList(recordId));

			assertThat(((QueryBasedReindexingBatchProcessModificationImpact)modificationImpacts.get(0)).getLogicalSearchCondition()).isEqualTo(expectedCondition);
		}

		@Test
		public void givenAMetadataAlreadyReindexedWhenCalculatingModificationImpactOfSchemaTypeThenReturnModificationImpactWithoutTheMetadata()
				throws Exception {
			when(searchServices.getResultsCount(any(LogicalSearchCondition.class))).thenReturn(1L);

			alreadyReindexedMetadata.add(firstAutomaticMetadata);

			List<Metadata> firstMetadataReferences = asList(firstReference, secondReference);
			List<Metadata> secondMetadataReferences = asList(secondReference, thirdReference);
			List<Metadata> thirdMetadataReferences = new ArrayList<>();
			doReturn(firstMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					firstAutomaticMetadata, modifiedMetadatas);
			doReturn(secondMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					secondAutomaticMetadata, modifiedMetadatas);
			doReturn(thirdMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					thirdAutomaticMetadata, modifiedMetadatas);
			when(schemaType.getCacheType()).thenReturn(RecordCacheType.FULLY_CACHED);

			List<Metadata> automaticMetadatas = asList(firstAutomaticMetadata, secondAutomaticMetadata, thirdAutomaticMetadata);
			when(schemaType.getAutomaticMetadatas()).thenReturn(automaticMetadatas);

			RecordsModification modification = new RecordsModification(asList(record), asList(modifiedMetadata),
					schemaType, new RecordUpdateOptions());
			List<ModificationImpact> modificationImpacts = impactCalculator.findImpactOfARecordsModification(
					modification, asList(recordId), "zeTitle");

			assertThat(modificationImpacts).hasSize(1);
			assertThat(modificationImpacts.get(0).getMetadataToReindex()).containsOnly(firstAutomaticMetadata,
					secondAutomaticMetadata);

			LogicalSearchCondition expectedCondition = from(schemaType)
					.whereAny(asList(firstReference, secondReference, thirdReference))
					.isIn(asList(record)).andWhere(Schemas.IDENTIFIER).isNotIn(asList(recordId));
			assertThat(((QueryBasedReindexingBatchProcessModificationImpact)modificationImpacts.get(0)).getLogicalSearchCondition()).isEqualTo(expectedCondition);
		}

		@Test
		public void givenAllMetadataAlreadyReindexedWhenCalculatingModificationImpactOfSchemaTypeThenReturnNull()
				throws Exception {

			transactionRecordsList = new ArrayList<>();
			alreadyReindexedMetadata.add(firstAutomaticMetadata);
			alreadyReindexedMetadata.add(secondAutomaticMetadata);

			List<Metadata> firstMetadataReferences = asList(firstReference, secondReference);
			List<Metadata> secondMetadataReferences = asList(secondReference, thirdReference);
			List<Metadata> thirdMetadataReferences = new ArrayList<>();
			doReturn(firstMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					firstAutomaticMetadata, modifiedMetadatas);
			doReturn(secondMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					secondAutomaticMetadata, modifiedMetadatas);
			doReturn(thirdMetadataReferences).when(impactCalculator).getReferenceMetadatasLinkingToModifiedMetadatas(
					thirdAutomaticMetadata, modifiedMetadatas);
			when(schemaType.getCacheType()).thenReturn(RecordCacheType.FULLY_CACHED);

			List<Metadata> automaticMetadatas = asList(firstAutomaticMetadata, secondAutomaticMetadata, thirdAutomaticMetadata);
			when(schemaType.getAutomaticMetadatas()).thenReturn(automaticMetadatas);

			RecordsModification modification = new RecordsModification(asList(record), asList(modifiedMetadata),
					schemaType, new RecordUpdateOptions());
			List<ModificationImpact> modificationImpacts = impactCalculator.findImpactOfARecordsModification(
					modification, asList(recordId), "zeTitle");

			assertThat(modificationImpacts).isEmpty();
		}

		@Test
		public void givenMetadataCopyingAUnmodifiedValueWhenGetReferenceMetadataLinkedToModifiedMetadatasThenEmptyList() {

			when(anotherSchemaCopiedMetadata.getDataEntry()).thenReturn(
					new CopiedDataEntry(anotherSchemaReferenceToZeSchemaMetadataCode, zeSchemaMetadataCode));

			List<Metadata> references = impactCalculator.getReferenceMetadatasLinkingToModifiedMetadatas(
					anotherSchemaCopiedMetadata, modifiedMetadatas);
			assertThat(references).isEmpty();

		}

		@Test
		public void givenMultipleModifiedMetadatasThenGetReferencesUsingThem() {
			Metadata reference1 = mockMetadata("zetype_default_reference1");
			Metadata reference2 = mockMetadata("zetype_default_reference2");

			Metadata modifiedMetadata1 = mockMetadata("zetype_default_modifiedMetadata1");
			Metadata modifiedMetadata2 = mockMetadata("zetype_default_modifiedMetadata2");

			Metadata automaticMetadata = mockMetadata("zetype_default_automaticMetadata");
			doReturn(asList(reference1)).when(impactCalculator)
					.getReferencesToMetadata(automaticMetadata, modifiedMetadata1);
			doReturn(asList(reference1, reference2)).when(impactCalculator)
					.getReferencesToMetadata(automaticMetadata, modifiedMetadata2);

			List<Metadata> modifiedMetadatas = asList(modifiedMetadata1, modifiedMetadata2);
			List<Metadata> references = impactCalculator.getReferenceMetadatasLinkingToModifiedMetadatas(
					automaticMetadata, modifiedMetadatas);

			assertThat(references).containsOnly(reference1, reference2).hasSize(2);
		}

		@Test
		public void whenGetCopiedMetadataReferencesToOtherMetadataWithSameCodeInAnotherSchemaThenEmptyList()
				throws Exception {

			List<Metadata> referenceMetadatas = impactCalculator
					.getReferencesToMetadata(copiedTitleUsingReferenceToAnotherSchema, notTheSameTypeSchemaTitle);
			assertThat(referenceMetadatas).isEmpty();
		}

		@Test
		public void whenGetCopiedMetadataReferencesToOtherMetadataWithSameCodeInSameSchemaThenReturnedInList()
				throws Exception {
			List<Metadata> referenceMetadatas = impactCalculator
					.getReferencesToMetadata(copiedTitleUsingReferenceToAnotherSchema, anotherSchemaTitle);
			assertThat(referenceMetadatas).containsOnly(referenceToAnotherSchema);
		}

		@Test
		public void givenCalculatorDefinedWithSimpleCodesWhenCalculatedMetadataWithReferencesToOtherMetadataThenReturnThem()
				throws Exception {

			List dependencies = new ArrayList<>();
			dependencies.add(ReferenceDependency.toAString("ref", "title"));
			dependencies.add(ReferenceDependency.toAString("ref2", "title"));
			dependencies.add(ReferenceDependency.toAString("ref1", "unmodifiedField"));
			dependencies.add(ReferenceDependency.toAString("refToDifferentSchema", "title"));
			dependencies.add(LocalDependency.toAString("notImportant"));
			when(calculator.getDependencies()).thenReturn(dependencies);

			List<Metadata> referenceMetadatas = impactCalculator.getReferencesToMetadata(calculatedMetadata, anotherSchemaTitle);
			assertThat(referenceMetadatas).containsOnly(referenceToAnotherSchema, anotherReferenceToAnotherSchema);
		}

		@Test
		public void givenCalculatorDefinedWithCompleteCodesWhenCalculatedMetadataWithReferencesToOtherMetadataThenReturnThem()
				throws Exception {

			List dependencies = new ArrayList<>();
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref", "anotherType_default_title"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref2", "anotherType_default_title"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref1", "anotherType_default_unmodifiedField"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_refToDifferentSchema", "notTheSameType_default_title"));
			dependencies.add(LocalDependency.toAString("notImportant"));
			when(calculator.getDependencies()).thenReturn(dependencies);

			List<Metadata> referenceMetadatas = impactCalculator.getReferencesToMetadata(calculatedMetadata, anotherSchemaTitle);
			assertThat(referenceMetadatas).containsOnly(referenceToAnotherSchema, anotherReferenceToAnotherSchema);
		}

		@Test
		public void givenCalculatorDefinedWithCompleteCodesWithoutCollectionWhenCalculatedMetadataWithReferencesToOtherMetadataThenReturnThem()
				throws Exception {

			List dependencies = new ArrayList<>();
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref", "anotherType_default_title"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref2", "anotherType_default_title"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_ref1", "anotherType_default_unmodifiedField"));
			dependencies.add(ReferenceDependency.toAString("zeType_default_refToDifferentSchema", "notTheSameType_default_title"));
			dependencies.add(LocalDependency.toAString("notImportant"));
			when(calculator.getDependencies()).thenReturn(dependencies);

			List<Metadata> referenceMetadatas = impactCalculator.getReferencesToMetadata(calculatedMetadata, anotherSchemaTitle);
			assertThat(referenceMetadatas).containsOnly(referenceToAnotherSchema, anotherReferenceToAnotherSchema);
		}

}
