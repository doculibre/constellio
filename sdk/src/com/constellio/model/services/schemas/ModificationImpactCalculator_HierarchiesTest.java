package com.constellio.model.services.schemas;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.SpecialDependencies;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.asSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModificationImpactCalculator_HierarchiesTest extends ConstellioTest {

	@Mock SearchServices searchServices;

	@Mock RecordServices recordServices;

	@Mock MetadataSchemaTypes types;

	List<Taxonomy> taxonomies;

	SchemaUtils utils = new SchemaUtils();

	List<Metadata> returnedMetadatas;

	Metadata zeSchemaPath;
	Metadata zeSchemaAllAuthorizations;
	Metadata type1Path;
	Metadata type1AllAuthorizations;
	Metadata type2Path;
	Metadata type2AllAuthorizations;
	Metadata type3Path;
	Metadata type3AllAuthorizations;
	Metadata type4Path;
	Metadata type4AllAuthorizations;
	@Mock Metadata type1RelationToType1;
	@Mock Metadata type2RelationToType1;
	@Mock Metadata type2RelationToType2;
	@Mock Metadata type3RelationToType2;
	@Mock Metadata type3RelationToType2NotUsedInTaxonomies;
	@Mock Metadata type4CustomRelationToType2Custom;
	@Mock Metadata zeSchemaTaxos;
	@Mock Metadata anotherSchemaTaxos;

	@Mock Metadata type1ReferenceToType1NotBusedByTaxos;
	@Mock Metadata type2ReferenceToType1NotBusedByTaxos;
	@Mock Metadata type3ReferenceToType2NotBusedByTaxos;
	@Mock Metadata type4ReferenceToType3NotBusedByTaxos;
	@Mock Metadata zeSchemaReferenceToType3NotBusedByTaxos;
	@Mock Metadata zeSchemaReferenceToZeSchema;
	@Mock Metadata anotherSchemaReferenceToType3NotBusedByTaxos;
	@Mock Metadata anotherSchemaReferenceToZeSchema;

	@Mock Metadata automaticMetaInType1UsingTaxos;
	@Mock Metadata automaticMetaInType2UsingTaxos;
	@Mock Metadata automaticMetaInType3UsingTaxos;
	@Mock Metadata automaticMetaInType4UsingTaxos;
	@Mock Metadata automaticMetaInZeSchemaUsingTaxos;
	@Mock Metadata automaticMetaInAnotherSchemaUsingTaxos;

	@Mock MetadataSchema type1Schema;
	@Mock MetadataSchema type2Schema;
	@Mock MetadataSchema type2CustomSchema;
	@Mock MetadataSchema type3Schema;
	@Mock MetadataSchema type4Schema;
	@Mock MetadataSchema zeSchema;
	@Mock MetadataSchema anotherSchema;

	@Mock MetadataSchemaType type1;
	@Mock MetadataSchemaType type2;
	@Mock MetadataSchemaType type3;
	@Mock MetadataSchemaType type4;
	@Mock MetadataSchemaType zeType;
	@Mock MetadataSchemaType anotherType;

	boolean childOfRelation = true;
	boolean normalRelation = false;

	ModificationImpactCalculator calculator;

	@Before
	public void setUp()
			throws Exception {
		taxonomies = new ArrayList<>();

		zeSchemaPath = newPathMetadata("zeSchema_default");
		type1Path = newPathMetadata("type1_default");
		type2Path = newPathMetadata("type2_default");
		type3Path = newPathMetadata("type3_default");
		type4Path = newPathMetadata("type4_default");
		zeSchemaAllAuthorizations = newAllAuthorizationsMetadata("zeSchema_default");
		type1AllAuthorizations = newAllAuthorizationsMetadata("type1_default");
		type2AllAuthorizations = newAllAuthorizationsMetadata("type2_default");
		type3AllAuthorizations = newAllAuthorizationsMetadata("type3_default");
		type4AllAuthorizations = newAllAuthorizationsMetadata("type4_default");

		givenReferenceToSchemaType(type1RelationToType1, types, "type1_default_relationToType1", childOfRelation,
				"type1");
		givenReferenceToSchemaType(type2RelationToType1, types, "type2_default_relationToType1", childOfRelation,
				"type1");
		givenReferenceToSchemaType(type2RelationToType2, types, "type2_default_relationToType2", childOfRelation,
				"type2");
		givenReferenceToSchemaType(type3RelationToType2, types, "type3_default_relationToType2", childOfRelation,
				"type2");
		givenReferenceToSchemaType(type3RelationToType2NotUsedInTaxonomies, types, "type3_default_relationToType2NotUsed",
				normalRelation, "type2");

		givenReferenceToSchemas(type4CustomRelationToType2Custom, types, "type4_custom_relationToType2Custom",
				childOfRelation,
				"type2_custom");
		givenReferenceToSchemaType(zeSchemaTaxos, types, "zeSchema_default_zeSchemaTaxos", normalRelation,
				"type3");
		givenReferenceToSchemas(anotherSchemaTaxos, types, "anotherSchema_default_anotherSchemaTaxos", normalRelation,
				"type2_custom");

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "anotherTaxo");

		Map<Language, String> labelTitle2 = new HashMap<>();
		labelTitle1.put(Language.French, "zeTaxo");

		taxonomies.add(Taxonomy.createPublic("anotherTaxo", labelTitle1, "zeCollection", new ArrayList<String>()));
		taxonomies.add(Taxonomy.createPublic("zeTaxo", labelTitle2, "zeCollection", Arrays.asList("type1", "type2", "type3")));

		configureMetadataWithHierarchyDependency(automaticMetaInType1UsingTaxos, types,
				"type1_default_allRemovedAuths");
		configureMetadataWithHierarchyDependency(automaticMetaInType2UsingTaxos, types,
				"type2_default_allRemovedAuths");
		configureMetadataWithHierarchyDependency(automaticMetaInType3UsingTaxos, types,
				"type3_default_allRemovedAuths");
		configureMetadataWithHierarchyDependency(automaticMetaInType4UsingTaxos, types,
				"type4_custom_allRemovedAuths");
		configureMetadataWithHierarchyDependency(automaticMetaInZeSchemaUsingTaxos, types,
				"zeSchema_default_allRemovedAuths");
		configureMetadataWithHierarchyDependency(automaticMetaInAnotherSchemaUsingTaxos, types,
				"anotherSchema_default_allRemovedAuths");

		givenReferenceToSchemaType(type1ReferenceToType1NotBusedByTaxos, types, "type1_default_notUsed", normalRelation,
				"type1");
		givenReferenceToSchemas(type2ReferenceToType1NotBusedByTaxos, types, "type2_default_notUsed", normalRelation,
				"type1_default");
		givenReferenceToSchemas(type3ReferenceToType2NotBusedByTaxos, types, "type3_default_notUsed", normalRelation,
				"type2");
		givenReferenceToSchemas(type4ReferenceToType3NotBusedByTaxos, types, "type4_default_notUsed", normalRelation,
				"type3_default");
		givenReferenceToSchemas(zeSchemaReferenceToType3NotBusedByTaxos, types, "zeSchema_default_notUsed",
				normalRelation,
				"type3_default");
		givenReferenceToSchemaType(zeSchemaReferenceToZeSchema, types, "zeSchema_default_parent", childOfRelation,
				"zeSchema");
		givenReferenceToSchemas(anotherSchemaReferenceToType3NotBusedByTaxos, types, "anotherSchema_default_notUsed",
				normalRelation, "type3_default");
		givenReferenceToSchemas(anotherSchemaReferenceToZeSchema, types, "anotherSchema_default_parent",
				childOfRelation, "zeSchema_custom");

		configureMockedSchemaWithTaxonomyRelations(type1Schema, types, "type1_default", type1RelationToType1);
		configureMockedSchemaWithTaxonomyRelations(type2Schema, types, "type2_default", type2RelationToType1,
				type2RelationToType2);
		configureMockedSchemaWithTaxonomyRelations(type2CustomSchema, types, "type2_custom", type2RelationToType1,
				type2RelationToType2);
		configureMockedSchemaWithTaxonomyRelations(type3Schema, types, "type3_default", type3RelationToType2);
		configureMockedSchemaWithTaxonomyRelations(type4Schema, types, "type4_custom", type4CustomRelationToType2Custom);
		configureMockedSchemaWithTaxonomyRelations(zeSchema, types, "zeSchema_default");
		configureMockedSchemaWithTaxonomyRelations(anotherSchema, types, "anotherSchema_default");

		configureMockedSchemaTypeWithTaxonomyRelations(type1, types, "type1", type1RelationToType1);
		configureMockedSchemaTypeWithTaxonomyRelations(type2, types, "type2", type2RelationToType1, type2RelationToType2);
		configureMockedSchemaTypeWithTaxonomyRelations(type3, types, "type3", type3RelationToType2);
		configureMockedSchemaTypeWithTaxonomyRelations(type4, types, "type4", type4CustomRelationToType2Custom);
		configureMockedSchemaTypeWithTaxonomyRelations(zeType, types, "zeSchema");
		configureMockedSchemaTypeWithTaxonomyRelations(anotherType, types, "anotherSchema");

		when(type1.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(new ArrayList<Metadata>());
		when(type2.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(new ArrayList<Metadata>());
		when(type3.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(new ArrayList<Metadata>());
		when(type4.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(new ArrayList<Metadata>());
		when(zeType.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(Arrays.asList(zeSchemaTaxos));
		when(anotherType.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(Arrays.asList(anotherSchemaTaxos));

		when(type1.getAllParentReferences()).thenReturn(Arrays.asList(type1RelationToType1));
		when(type2.getAllParentReferences()).thenReturn(Arrays.asList(type2RelationToType1, type2RelationToType2));
		when(type3.getAllParentReferences()).thenReturn(Arrays.asList(type3RelationToType2));
		when(type4.getAllParentReferences()).thenReturn(Arrays.asList(type4CustomRelationToType2Custom));
		when(zeType.getAllParentReferences()).thenReturn(Arrays.asList(zeSchemaReferenceToZeSchema));
		when(anotherType.getAllParentReferences())
				.thenReturn(Arrays.asList(anotherSchemaReferenceToZeSchema));

		calculator = new ModificationImpactCalculator(types, taxonomies, searchServices, recordServices);
	}

	@Test
	public void givenZeSchemaPathModifiedWhenEvaluatingAutomaticMetaInZeSchemaThenHasImpactOnSubRecordsWithParentRelation()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, zeSchemaPath);
		assertThat(returnedMetadatas).containsOnly(zeSchemaReferenceToZeSchema);
	}

	@Test
	public void givenZeSchemaPathModifiedWhenEvaluatingAutomaticMetaInAnotherSchemaThenHasImpactAnotherSchemaRecordWithParentRelation()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos, zeSchemaPath);
		assertThat(returnedMetadatas).containsOnly(anotherSchemaReferenceToZeSchema);
	}

	@Test
	public void givenType1PathModifiedWhenEvaluatingAutomaticMetaInType1ThenImpactWithType1RelationToType1()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type1Path);
		assertThat(returnedMetadatas).containsOnly(type1RelationToType1);
	}

	@Test
	public void givenType1PathModifiedWhenEvaluatingAutomaticMetaInType2ThenImpactWithType2RelationToType1()
			throws Exception {
		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type1Path);
		assertThat(returnedMetadatas).containsOnly(type2RelationToType1);
	}

	@Test
	public void givenType1PathModifiedWhenEvaluatingAutomaticMetaInType3ThenNoImpact()
			throws Exception {
		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type1Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatingAutomaticMetaInType3ThenImpactWithType3RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type2Path);
		assertThat(returnedMetadatas).containsOnly(type3RelationToType2);
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatingAutomaticMetaInType1ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type2Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatingAutomaticMetaInType2ThenImpactWithType2RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type2Path);
		assertThat(returnedMetadatas).containsOnly(type2RelationToType2);
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatingAutomaticMetaInType4ThenImpactWithType3RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType4UsingTaxos, type2Path);
		assertThat(returnedMetadatas).containsOnly(type4CustomRelationToType2Custom);
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatingAutomaticMetaInType1ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type3Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatingAutomaticMetaInType2ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type3Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatingAutomaticMetaInType3ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type3Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatingAutomaticMetaInType4ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType4UsingTaxos, type3Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatinZeSchemaThenImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, type3Path);
		assertThat(returnedMetadatas).containsOnly(zeSchemaTaxos);
	}

	@Test
	public void givenType3PathModifiedWhenEvaluatinAnotherSchemaThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos, type3Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatinZeSchemaThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, type2Path);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2PathModifiedWhenEvaluatinAnotherSchemaThenImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos, type2Path);
		assertThat(returnedMetadatas).containsOnly(anotherSchemaTaxos);
	}

	@Test
	public void givenZeSchemaAllAuthorizationModifiedWhenEvaluatingAutomaticMetaInZeSchemaThenHasImpactOnSubRecordsWithParentRelation()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, zeSchemaAllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(zeSchemaReferenceToZeSchema);
	}

	@Test
	public void givenZeSchemaAllAuthorizationModifiedWhenEvaluatingAutomaticMetaInAnotherSchemaThenHasImpactAnotherSchemaRecordWithParentRelation()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos,
				zeSchemaAllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(anotherSchemaReferenceToZeSchema);
	}

	@Test
	public void givenType1AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType1ThenImpactWithType1RelationToType1()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type1AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(type1RelationToType1);
	}

	@Test
	public void givenType1AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType2ThenImpactWithType2RelationToType1()
			throws Exception {
		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type1AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(type2RelationToType1);
	}

	@Test
	public void givenType1AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType3ThenNoImpact()
			throws Exception {
		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type1AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType1ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType2ThenImpactWithType2RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(type2RelationToType2);
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType3ThenImpactWithType3RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(type3RelationToType2);
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType4ThenImpactWithType3RelationToType2()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType4UsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(type4CustomRelationToType2Custom);
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType1ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType1UsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType2ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType2UsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType3ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType3UsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatingAutomaticMetaInType4ThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInType4UsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatinZeSchemaThenImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(zeSchemaTaxos);
	}

	@Test
	public void givenType3AllAuthorizationModifiedWhenEvaluatinAnotherSchemaThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos, type3AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatinZeSchemaThenNoImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInZeSchemaUsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).isEmpty();
	}

	@Test
	public void givenType2AllAuthorizationModifiedWhenEvaluatinAnotherSchemaThenImpact()
			throws Exception {

		returnedMetadatas = calculator.getReferencesToMetadata(automaticMetaInAnotherSchemaUsingTaxos, type2AllAuthorizations);
		assertThat(returnedMetadatas).containsOnly(anotherSchemaTaxos);
	}

	private MetadataSchema configureMockedSchemaWithTaxonomyRelations(MetadataSchema schema, MetadataSchemaTypes types,
																	  String code,
																	  Metadata... taxonomyRelations) {
		when(schema.getCode()).thenReturn(code);
		List<Metadata> metadatas = Arrays.asList(taxonomyRelations);
		when(schema.getTaxonomyRelationshipReferences(taxonomies)).thenReturn(metadatas);

		when(types.getSchema(code)).thenReturn(schema);

		return schema;
	}

	private MetadataSchemaType configureMockedSchemaTypeWithTaxonomyRelations(MetadataSchemaType type,
																			  MetadataSchemaTypes types,
																			  String code,
																			  Metadata... taxonomyRelations) {
		when(type.getCode()).thenReturn(code);
		List<Metadata> metadatas = Arrays.asList(taxonomyRelations);
		when(type.getAllReferencesToTaxonomySchemas(taxonomies)).thenReturn(metadatas);

		when(types.getSchemaType(code)).thenReturn(type);

		return type;
	}

	private Metadata newPathMetadata(String schemaCode) {
		String code = schemaCode + "_path";
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getLocalCode()).thenReturn("path");
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		return metadata;
	}

	private Metadata newAllAuthorizationsMetadata(String schemaCode) {
		String code = schemaCode + "_allRemovedAuths";
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getLocalCode()).thenReturn("allRemovedAuths");
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		return metadata;
	}

	private Metadata configureMetadataWithHierarchyDependency(Metadata metadata, MetadataSchemaTypes types,
															  String code) {

		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(code.split("_")[2]);
		MetadataValueCalculator metadataValueCalculator = mock(MetadataValueCalculator.class);
		List dependencies = Arrays.asList(SpecialDependencies.HIERARCHY);
		when(metadataValueCalculator.getDependencies()).thenReturn(dependencies);
		when(metadata.getDataEntry()).thenReturn(new CalculatedDataEntry(metadataValueCalculator));
		return metadata;
	}

	private Metadata givenReferenceToSchemaType(Metadata metadata, MetadataSchemaTypes types,
												String code, boolean childOf, String type) {
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(code.split("_")[2]);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);

		AllowedReferences allowedReferences = new AllowedReferences(type, null);
		when(metadata.getAllowedReferences()).thenReturn(allowedReferences);
		when(types.getMetadata(code)).thenReturn(metadata);
		when(metadata.getReferencedSchemaTypeCode()).thenCallRealMethod();
		return metadata;
	}

	private Metadata givenReferenceToSchemas(Metadata metadata, MetadataSchemaTypes types, String code,
											 boolean childOf, String... schemas) {

		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(code.split("_")[2]);
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);

		AllowedReferences allowedReferences = new AllowedReferences(null, asSet(schemas));
		when(metadata.getAllowedReferences()).thenReturn(allowedReferences);
		when(types.getMetadata(code)).thenReturn(metadata);
		when(metadata.getReferencedSchemaTypeCode()).thenCallRealMethod();
		return metadata;
	}

}
