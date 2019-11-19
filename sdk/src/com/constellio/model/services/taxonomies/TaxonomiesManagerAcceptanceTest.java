package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.TaxonomySchemaTypesHaveRecords;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class TaxonomiesManagerAcceptanceTest extends ConstellioTest {

	static String TAXONOMIES_CONFIG = "/taxonomies.xml";
	MetadataSchemasManager schemasManager;
	TaxonomiesManager taxonomiesManager;

	TaxonomiesSearchServices taxonomiesSearchServices;

	RecordServices recordServices;

	SearchServices searchServices;

	SearchServiceAcceptanceTestSchemas collection1Schema = new SearchServiceAcceptanceTestSchemas("collection1");
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas collection1ZeSchema = collection1Schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas collection1AnotherSchema = collection1Schema.new AnotherSchemaMetadatas();

	SearchServiceAcceptanceTestSchemas collection2Schema = new SearchServiceAcceptanceTestSchemas("collection2");
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas collection2ZeSchema = collection2Schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas collection2AnotherSchema = collection2Schema.new AnotherSchemaMetadatas();

	LogicalSearchCondition condition;

	Transaction transaction;

	Map<Language, String> labelTitle1;
	Map<Language, String> labelTitle2;

	@Before
	public void setup()
			throws Exception {
		this.searchServices = getModelLayerFactory().newSearchServices();
		this.recordServices = getModelLayerFactory().newRecordServices();
		this.transaction = new Transaction();

		this.schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		this.taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();
		this.taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		givenCollection("collection1");
		givenCollection("collection2");
		defineSchemasManager().using(collection1Schema.withAStringMetadata());
		defineSchemasManager().using(collection2Schema.withAMetadata());

		labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "1");

		labelTitle2 = new HashMap<>();
		labelTitle2.put(Language.French, "2");
	}

	@Test
	public void givenSchemasInMultipleCollectionsThenAllIndependent()
			throws Exception {


		Taxonomy collection1Taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy collection2Taxonomy = Taxonomy.createPublic("2", labelTitle2, "collection2", asList("anotherSchemaType"));

		taxonomiesManager.addTaxonomy(collection1Taxonomy, schemasManager);
		taxonomiesManager.addTaxonomy(collection2Taxonomy, schemasManager);

		assertThat(taxonomiesManager.getEnabledTaxonomies("collection1")).hasSize(1);
		assertThat(taxonomiesManager.getEnabledTaxonomies("collection1").get(0).getCode()).isEqualTo("1");
		assertThat(taxonomiesManager.getEnabledTaxonomies("collection2")).hasSize(1);
		assertThat(taxonomiesManager.getEnabledTaxonomies("collection2").get(0).getCode()).isEqualTo("2");

	}

	@Test
	public void givenXMLAlreadyExistingAndAuthorizationsThenManagerLoadThem()
			throws Exception {

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", labelTitle2, "collection1", asList("anotherSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);

		TaxonomiesManager newTaxonomiesManager = this.taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		List<Taxonomy> enableTaxonomies = newTaxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = newTaxonomiesManager.getDisabledTaxonomies("collection1");

		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(enableTaxonomies.get(1).getCode()).isEqualTo(taxonomy2.getCode());
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test
	public void whenNewTaxonomieThenItIsEmpty()
			throws Exception {

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies).isEmpty();
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test(expected = TaxonomySchemaTypesHaveRecords.class)
	public void givenRecordUsingTypeInTaxonomyWhenAddTaxonomyThenException()
			throws Exception {

		givenRecord();

		Map<Language, String> labelTitle3 = new HashMap<>();
		labelTitle3.put(Language.French, "zeTaxo");

		Taxonomy taxonomy1 = Taxonomy.createPublic("zeTaxo", labelTitle3, "collection1", asList("zeSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
	}

	@Test
	public void whenAddTaxonomiesThenCanReadIt()
			throws Exception {

		List<String> taxoUsers = asList("user1", "user2");
		List<String> taxoGroups = asList("group1", "group2");

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = new Taxonomy("2", labelTitle2, emptyMap(), "collection1", false, taxoUsers, taxoGroups, asList("anotherSchemaType"),
				true);

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(enableTaxonomies.get(0).isVisibleInHomePage()).isTrue();
		assertThat(enableTaxonomies.get(0).getUserIds()).isEmpty();
		assertThat(enableTaxonomies.get(0).getGroupIds()).isEmpty();
		assertThat(enableTaxonomies.get(0).isShowParentsInSearchResults()).isFalse();

		assertThat(enableTaxonomies.get(1).getCode()).isEqualTo(taxonomy2.getCode());
		assertThat(enableTaxonomies.get(1).isVisibleInHomePage()).isFalse();
		assertThat(enableTaxonomies.get(1).getUserIds()).isEqualTo(taxoUsers);
		assertThat(enableTaxonomies.get(1).getGroupIds()).isEqualTo(taxoGroups);
		assertThat(enableTaxonomies.get(1).isShowParentsInSearchResults()).isTrue();
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test(expected = TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists.class)
	public void whenAddTwoTaxonomiesWithSameCodeThenException()
			throws Exception {

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("anotherSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);
	}

	@Test(expected = TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists.class)
	public void whenAddTwoTaxonomiesWithSameSchemaTypeThenException()
			throws Exception {

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", labelTitle2, "collection1", asList("zeSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);
	}

	@Test
	public void whenDisableTaxonomyThenItIsDisable()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		taxonomiesManager.disable(taxonomy, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(disableTaxonomies.get(0).getCode()).isEqualTo(taxonomy.getCode());
		assertThat(enableTaxonomies).isEmpty();
	}

	@Test(expected = TaxonomySchemaTypesHaveRecords.class)
	public void givenRecordUsingTypeInTaxonomyWhenDisableTaxonomyThenException()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		givenRecord();

		taxonomiesManager.disable(taxonomy, schemasManager);
	}

	@Test
	public void whenDisableAnAlreadyDisableTaxonomyThenOk()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		taxonomiesManager.disable(taxonomy, schemasManager);
		taxonomiesManager.disable(taxonomy, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(disableTaxonomies.get(0).getCode()).isEqualTo(taxonomy.getCode());
		assertThat(enableTaxonomies).isEmpty();
	}

	@Test(expected = TaxonomySchemaTypesHaveRecords.class)
	public void givenRecordUsingTypeInTaxonomyWhenEnbaleTaxonomyThenItIsEnable()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		taxonomiesManager.disable(taxonomy, schemasManager);

		givenRecord();

		taxonomiesManager.enable(taxonomy, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy.getCode());
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test
	public void whenEnbaleTaxonomyThenItIsEnable()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		taxonomiesManager.disable(taxonomy, schemasManager);

		taxonomiesManager.enable(taxonomy, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy.getCode());
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test
	public void whenEnbaleAnAlreadyEnableTaxonomyThenOk()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);
		taxonomiesManager.disable(taxonomy, schemasManager);

		taxonomiesManager.enable(taxonomy, schemasManager);
		taxonomiesManager.enable(taxonomy, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy.getCode());
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test
	public void givenTwoTaxonomiesWhenGetTaxonomyForThenReturnTaxonomyWithTheSchemaPassed()
			throws Exception {
		Taxonomy taxonomy1 = Taxonomy.createPublic("1", labelTitle1, "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", labelTitle2, "collection1", asList("anotherSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);

		assertThat(taxonomiesManager.getTaxonomyFor("collection1", "zeSchemaType")).isEqualTo(taxonomy1);
		assertThat(taxonomiesManager.getTaxonomyFor("collection1", "anotherSchemaType")).isEqualTo(taxonomy2);
	}

	private void givenRecord()
			throws Exception {
		recordServices.add(recordServices.newRecordWithSchema(collection1ZeSchema.instance()).set(
				collection1ZeSchema.stringMetadata(), "zeRecord"));
	}

}