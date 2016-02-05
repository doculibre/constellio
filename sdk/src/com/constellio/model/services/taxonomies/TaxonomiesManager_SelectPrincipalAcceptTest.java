package com.constellio.model.services.taxonomies;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.PrincipalTaxonomyCannotBeDisabled;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.PrincipalTaxonomyIsAlreadyDefined;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;

public class TaxonomiesManager_SelectPrincipalAcceptTest extends ConstellioTest {

	static String TAXONOMIES_CONFIG = "/taxonomies.xml";
	MetadataSchemasManager schemasManager;
	TaxonomiesManager taxonomiesManager;

	TaxonomiesSearchServices taxonomiesSearchServices;

	RecordServices recordServices;

	SearchServices searchServices;

	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas = new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType taxo1Type1 = schemas.new Taxonomy1FirstSchemaType();
	TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType taxo1Type2 = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy1SecondSchemaType taxo2Type = schemas.new Taxonomy1SecondSchemaType();

	LogicalSearchCondition condition;

	Transaction transaction;

	Taxonomy taxo1, taxo2, taxo3WithOnlyFirstType, taxo4WithOnlySecondType;

	@Before
	public void setUp()
			throws Exception {
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		defineSchemasManager().using(schemas);
		taxo1 = schemas.getTaxonomies().get(0);
		taxo2 = schemas.getTaxonomies().get(1);

	}

	@Test(expected = TaxonomiesManagerRuntimeException.TaxonomyMustBeAddedBeforeSettingItHasPrincipal.class)
	public void givenNoPrincipalTaxonomyWhenSettingNewTaxonomyAsPrincipalTaxonomyThenException()
			throws Exception {
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);
	}

	@Test
	public void givenNoPrincipalTaxonomyWhenSettingItUsingCorrectSchemaTypesThenSet()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		assertThat(taxonomiesManager.getPrincipalTaxonomy("zeCollection")).isEqualTo(taxo1);
	}

	@Test
	public void givenTaxo1DefinedAsPrincipalWhenCreatingASinglevalueMetadataReferencingItInAnotherSchemaTypeThenSaved()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaTypeBuilder taxo1Type2Builder = typesBuilder.getSchemaType("taxo1Type2");
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref")
				.defineReferencesTo(taxo1Type2Builder).setMultivalue(false);
		MetadataSchemaTypes types = schemasManager
				.saveUpdateSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("anotherSchema_default_ref").getAllowedReferences().getAllowedSchemaType())
				.isEqualTo("taxo1Type2");

	}

	@Test(expected = PrincipalTaxonomyCannotBeDisabled.class)
	public void givenDisabledTaxonomyWhenSettingItHasPrincipalThenException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.disable(taxo1, schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

	}

	@Test(expected = TaxonomiesManagerRuntimeException.PrincipalTaxonomyCannotBeDisabled.class)
	public void givenPrincipalTaxonomyWhenDisablingItThenException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);
		taxonomiesManager.disable(taxo1, schemasManager);

	}

	@Test
	public void givenTaxo1DefinedAsPrincipalWhenCreatingASinglevalueMetadataReferencingItInAnotherSchemaThenSaved()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaBuilder taxo1Type2Builder = typesBuilder.getSchemaType("taxo1Type2").getDefaultSchema();
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref")
				.defineReferencesTo(taxo1Type2Builder)
				.setMultivalue(false);
		MetadataSchemaTypes types = schemasManager.saveUpdateSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("anotherSchema_default_ref").getAllowedReferences().getAllowedSchemas())
				.containsOnly("taxo1Type2_default");

	}

	@Test(expected = MetadataBuilderRuntimeException.CannotCreateMultivalueReferenceToPrincipalTaxonomy.class)
	public void givenTaxo1DefinedAsPrincipalWhenCreatingAMultivalueMetadatWithTaxonomyReferenceInAnotherSchemaTypeThenBuildException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaTypeBuilder taxo1Type2 = typesBuilder.getSchemaType("taxo1Type2");
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref").defineTaxonomyRelationshipToType(
				taxo1Type2).setMultivalue(true);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);
	}

	@Test(expected = MetadataBuilderRuntimeException.CannotCreateMultivalueReferenceToPrincipalTaxonomy.class)
	public void givenTaxo1DefinedAsPrincipalWhenCreatingAMultivalueMetadataWithTaxonomyReferenceInAnotherSchemaTypeThenNoException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaTypeBuilder taxo1Type2 = typesBuilder.getSchemaType("taxo1Type2");
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref").defineTaxonomyRelationshipToType(
				taxo1Type2).setMultivalue(true);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);
	}

	@Test
	public void givenTaxo1DefinedAsPrincipalWhenCreatingAMultivalueMetadataReferencingItInAnotherSchemaThenNoException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaBuilder taxo1Type2 = typesBuilder.getSchemaType("taxo1Type2").getDefaultSchema();
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref").defineReferencesTo(taxo1Type2)
				.setMultivalue(true);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);
	}

	public void givenTaxo1DefinedAsPrincipalWhenCreatingAMultivalueMetadataReferencingItInAnotherSchemaThenBuildException()
			throws Exception {
		givenTaxo1And2();
		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("zeCollection"));
		MetadataSchemaBuilder taxo1Type2 = typesBuilder.getSchemaType("taxo1Type2").getDefaultSchema();
		typesBuilder.getOrCreateNewSchemaType("anotherSchema").getDefaultSchema().create("ref").defineReferencesTo(taxo1Type2)
				.setMultivalue(true);
		schemasManager.saveUpdateSchemaTypes(typesBuilder);
	}

	@Test
	public void givenNoPrincipalTaxonomyWhenSettingTaxoUsedByMultivalueMetadatasThenException()
			throws Exception {
		givenTaxo1And2();
		try {
			taxonomiesManager.setPrincipalTaxonomy(taxo2, schemasManager);
			fail("TaxonomySchemaIsReferencedInMultivalueReference expected");
		} catch (TaxonomiesManagerRuntimeException.TaxonomySchemaIsReferencedInMultivalueReference e) {
			//OK
		}
		assertThat(taxonomiesManager.getPrincipalTaxonomy("zeCollection")).isNull();
	}

	@Test
	public void givenTaxonomyAlreadyDefinedThenCannotDefineItAnotherTime()
			throws Exception {
		givenTaxo1And2();

		taxonomiesManager.setPrincipalTaxonomy(taxo1, schemasManager);

		try {
			taxonomiesManager.setPrincipalTaxonomy(taxo2, schemasManager);
			fail("PrincipalTaxonomyIsAlreadyDefined expected");
		} catch (PrincipalTaxonomyIsAlreadyDefined e) {
			//OK
		}
		assertThat(taxonomiesManager.getPrincipalTaxonomy("zeCollection")).isEqualTo(taxo1);
	}

	private void givenTaxo1And2() {
		taxonomiesManager.addTaxonomy(taxo1, schemasManager);
		taxonomiesManager.addTaxonomy(taxo2, schemasManager);
	}
}
