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
package com.constellio.model.services.taxonomies;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServiceAcceptanceTestSchemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.TaxonomySchemaTypesHaveRecords;
import com.constellio.sdk.tests.ConstellioTest;

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
	}

	@Test
	public void givenSchemasInMultipleCollectionsThenAllIndependent()
			throws Exception {

		Taxonomy collection1Taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy collection2Taxonomy = Taxonomy.createPublic("2", "2", "collection2", asList("anotherSchemaType"));

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

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", "2", "collection1", asList("anotherSchemaType"));

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

		Taxonomy taxonomy1 = Taxonomy.createPublic("zeTaxo", "zeTaxo", "collection1", asList("zeSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
	}

	@Test
	public void whenAddTaxonomiesThenCanReadIt()
			throws Exception {

		List<String> taxoUsers = asList("user1", "user2");
		List<String> taxoGroups = asList("group1", "group2");

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = new Taxonomy("2", "2", "collection1", false, taxoUsers, taxoGroups, asList("anotherSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);

		List<Taxonomy> enableTaxonomies = taxonomiesManager.getEnabledTaxonomies("collection1");
		List<Taxonomy> disableTaxonomies = taxonomiesManager.getDisabledTaxonomies("collection1");
		assertThat(enableTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(enableTaxonomies.get(0).isVisibleInHomePage()).isTrue();
		assertThat(enableTaxonomies.get(0).getUserIds()).isEmpty();
		assertThat(enableTaxonomies.get(0).getGroupIds()).isEmpty();

		assertThat(enableTaxonomies.get(1).getCode()).isEqualTo(taxonomy2.getCode());
		assertThat(enableTaxonomies.get(1).isVisibleInHomePage()).isFalse();
		assertThat(enableTaxonomies.get(1).getUserIds()).isEqualTo(taxoUsers);
		assertThat(enableTaxonomies.get(1).getGroupIds()).isEqualTo(taxoGroups);
		assertThat(disableTaxonomies).isEmpty();
	}

	@Test(expected = TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists.class)
	public void whenAddTwoTaxonomiesWithSameCodeThenException()
			throws Exception {

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("1", "1", "collection1", asList("anotherSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);
	}

	@Test(expected = TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists.class)
	public void whenAddTwoTaxonomiesWithSameSchemaTypeThenException()
			throws Exception {

		Taxonomy taxonomy1 = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", "2", "collection1", asList("zeSchemaType"));

		taxonomiesManager.addTaxonomy(taxonomy1, schemasManager);
		taxonomiesManager.addTaxonomy(taxonomy2, schemasManager);
	}

	@Test
	public void whenDisableTaxonomyThenItIsDisable()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
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

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		taxonomiesManager.addTaxonomy(taxonomy, schemasManager);

		givenRecord();

		taxonomiesManager.disable(taxonomy, schemasManager);
	}

	@Test
	public void whenDisableAnAlreadyDisableTaxonomyThenOk()
			throws Exception {

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
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

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
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

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
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

		Taxonomy taxonomy = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
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
		Taxonomy taxonomy1 = Taxonomy.createPublic("1", "1", "collection1", asList("zeSchemaType"));
		Taxonomy taxonomy2 = Taxonomy.createPublic("2", "2", "collection1", asList("anotherSchemaType"));
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