package com.constellio.model.services.taxonomies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.Language;
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
		List<String> languageSupported = new ArrayList<>();
		languageSupported.add(Language.French.getCode());
		languageSupported.add(Language.English.getCode());
		reader = new TaxonomiesReader(document, languageSupported);
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

		List<Taxonomy> disablesTaxonomies = reader.readDisables();

		taxonomiesElement = document.getRootElement();
		assertThat(disablesTaxonomies).hasSize(2);
		assertThat(disablesTaxonomies.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(disablesTaxonomies.get(0).getTitle().get(Language.French)).isEqualTo("taxofr1");
		assertThat(disablesTaxonomies.get(0).getTitle().get(Language.English)).isEqualTo("taxoen1");
		assertThat(disablesTaxonomies.get(1).getCode()).isEqualTo(taxonomy3.getCode());
		assertThat(disablesTaxonomies.get(1).getTitle().get(Language.French)).isEqualTo("taxofr3");
		assertThat(disablesTaxonomies.get(1).getTitle().get(Language.English)).isEqualTo("taxoen3");
	}

	private Taxonomy newTaxonomy(int id) {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "taxofr" + id);
		labelTitle.put(Language.English, "taxoen" + id);

		ArrayList<String> taxonomySchemaTypes = new ArrayList<>();
		taxonomySchemaTypes.add("schemaType1" + id);
		taxonomySchemaTypes.add("schemaType2" + id);
		taxonomySchemaTypes.add("schemaType3" + id);
		taxonomySchemaTypes.add("schemaType4" + id);
		Taxonomy taxonomy = Taxonomy.createPublic("code" + id, labelTitle, "zeCollection", taxonomySchemaTypes);
		return taxonomy;
	}
}
