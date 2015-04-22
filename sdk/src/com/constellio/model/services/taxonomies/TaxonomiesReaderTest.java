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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Taxonomy;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomiesReaderTest extends ConstellioTest {

	Document document;
	TaxonomiesWriter writer;
	TaxonomiesReader reader;
	Element taxonomiesElement;
	Taxonomy taxonomy1;
	Taxonomy taxonomy2;
	Taxonomy taxonomy3;

	@Before
	public void setup()
			throws Exception {

		document = new Document();
		writer = new TaxonomiesWriter(document);
		writer.createEmptyTaxonomy();
		taxonomy1 = newTaxonomy(1);
		taxonomy2 = newTaxonomy(2);
		taxonomy3 = newTaxonomy(3);
		writer.addTaxonmy(taxonomy1);
		writer.addTaxonmy(taxonomy2);
		writer.addTaxonmy(taxonomy3);
		reader = new TaxonomiesReader(document);
	}

	@Test
	public void givenThreeEnablesTaxonomieswhenReadEnablesThenTheyAreReturned()
			throws Exception {

		List<Taxonomy> enablesTaxonomies = reader.readEnables();

		taxonomiesElement = document.getRootElement();
		assertThat(enablesTaxonomies).hasSize(3);
		assertThat(enablesTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(enablesTaxonomies.get(1).getCode()).isEqualTo(taxonomy2.getCode());
		assertThat(enablesTaxonomies.get(2).getCode()).isEqualTo(taxonomy3.getCode());
	}

	@Test
	public void givenNoDisablesTaxonomiesWhenReadDisablesThenEmptyListIsReturned()
			throws Exception {

		List<Taxonomy> disablesTaxonomies = reader.readDisables();

		taxonomiesElement = document.getRootElement();
		assertThat(disablesTaxonomies).isEmpty();
	}

	@Test
	public void givenTwoDisablesTaxonomiesWhenReadDisablesThenTheyAreReturned()
			throws Exception {

		writer.disable("code1");
		writer.disable("code3");

		reader = new TaxonomiesReader(document);

		List<Taxonomy> disablesTaxonomies = reader.readDisables();

		taxonomiesElement = document.getRootElement();
		assertThat(disablesTaxonomies).hasSize(2);
		assertThat(disablesTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(disablesTaxonomies.get(1).getCode()).isEqualTo(taxonomy3.getCode());
	}

	private Taxonomy newTaxonomy(int id) {
		ArrayList<String> taxonomySchemaTypes = new ArrayList<>();
		taxonomySchemaTypes.add("schemaType1" + id);
		taxonomySchemaTypes.add("schemaType2" + id);
		taxonomySchemaTypes.add("schemaType3" + id);
		taxonomySchemaTypes.add("schemaType4" + id);
		Taxonomy taxonomy = Taxonomy.createPublic("code" + id, "taxo" + id, "zeCollection", taxonomySchemaTypes);
		return taxonomy;
	}
}
