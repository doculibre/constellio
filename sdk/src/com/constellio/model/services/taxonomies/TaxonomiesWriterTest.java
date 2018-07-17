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

public class TaxonomiesWriterTest extends ConstellioTest {

	private static final String CODE = "code";
	private static final String TAXONOMY = "taxonomy";
	private static final String DISABLES = "disables";
	private static final String ENABLES = "enables";
	Document document;
	TaxonomiesWriter writer;
	Element taxonomiesElement;

	@Before
	public void setup()
			throws Exception {

		document = new Document();
		writer = new TaxonomiesWriter(document);
		writer.createEmptyTaxonomy();
	}

	@Test
	public void whenCreateEmptyTaxonomyThenItIsCreated()
			throws Exception {

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).isEmpty();
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).isEmpty();
	}

	@Test
	public void whenAddTaxonomyThenItIsInEnableList()
			throws Exception {

		Taxonomy taxonomy = newTaxonomy(1);

		writer.addTaxonmy(taxonomy);

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).isEmpty();
	}

	@Test
	public void whenAddTwoTaxonomiesThenTheyAreInEnableList()
			throws Exception {

		Taxonomy taxonomy = newTaxonomy(1);
		Taxonomy taxonomy2 = newTaxonomy(2);

		writer.addTaxonmy(taxonomy);
		writer.addTaxonmy(taxonomy2);

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren().get(0).getAttributeValue(CODE)).isEqualTo("code1");
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren().get(1).getAttributeValue(CODE)).isEqualTo("code2");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).isEmpty();
	}

	@Test
	public void whenDisableTaxonomyThenItIsDisable()
			throws Exception {
		Taxonomy taxonomy = newTaxonomy(1);
		writer.addTaxonmy(taxonomy);

		writer.disable("code1");

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(0);
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(DISABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
	}

	@Test
	public void givenTwoEnablesTaxonomiesWhenDisableOneTaxonomyThenItIsDisable()
			throws Exception {
		Taxonomy taxonomy = newTaxonomy(1);
		Taxonomy taxonomy2 = newTaxonomy(2);
		writer.addTaxonmy(taxonomy);
		writer.addTaxonmy(taxonomy2);

		writer.disable("code1");

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code2");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(DISABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
	}

	@Test
	public void whenDisableAndEnableTheTaxonomyThenItIsEnable()
			throws Exception {
		Taxonomy taxonomy = newTaxonomy(1);
		Taxonomy taxonomy2 = newTaxonomy(2);
		writer.addTaxonmy(taxonomy);
		writer.addTaxonmy(taxonomy2);
		writer.disable("code1");

		writer.enable("code1");

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren().get(0).getAttributeValue(CODE)).isEqualTo("code2");
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren().get(1).getAttributeValue(CODE)).isEqualTo("code1");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).isEmpty();
	}

	@Test
	public void whenDisableTwiceTheSameTaxonomyThenItIsDisable()
			throws Exception {
		Taxonomy taxonomy = newTaxonomy(1);
		Taxonomy taxonomy2 = newTaxonomy(2);
		writer.addTaxonmy(taxonomy);
		writer.addTaxonmy(taxonomy2);

		writer.disable("code1");
		writer.disable("code1");

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code2");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(DISABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
	}

	@Test
	public void whenEnableTwiceTheSameTaxonomyThenItIsEnable()
			throws Exception {
		Taxonomy taxonomy = newTaxonomy(1);
		Taxonomy taxonomy2 = newTaxonomy(2);
		writer.addTaxonmy(taxonomy);
		writer.addTaxonmy(taxonomy2);

		writer.disable("code1");
		writer.disable("code1");

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code2");
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(DISABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
	}

	@Test
	public void givenTaxonomyWhenEditTaxonomyThenItIsInEnableList()
			throws Exception {

		Taxonomy taxonomy = newTaxonomy(1);
		writer.addTaxonmy(taxonomy);
		List<String> userIds = new ArrayList<>();
		userIds.add("userId1");
		userIds.add("userId2");
		List<String> groupIds = new ArrayList<>();
		groupIds.add("groupId1");
		groupIds.add("groupId2");

		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "newTitle");

		taxonomy = taxonomy.withTitle(labelTitle).withUserIds(userIds).withGroupIds(groupIds);
		writer.editTaxonomy(taxonomy);

		taxonomiesElement = document.getRootElement();
		assertThat(taxonomiesElement.getChildren()).hasSize(2);
		assertThat(taxonomiesElement.getChild(ENABLES).getChildren()).hasSize(1);
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getAttributeValue(CODE)).isEqualTo("code1");
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getChild("userIds").getValue())
				.isEqualTo(userIds.get(0) + "," + userIds.get(1));
		assertThat(
				taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getChild("groupIds").getValue())
				.isEqualTo(groupIds.get(0) + "," + groupIds.get(1));
		assertThat(taxonomiesElement.getChild(DISABLES).getChildren()).isEmpty();
		assertThat(taxonomiesElement.getChild(ENABLES).getChild(TAXONOMY).getChild("title").getAttributeValue("titlefr")).isEqualTo("newTitle");
	}

	private Taxonomy newTaxonomy(int id) {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "taxo" + id);

		ArrayList<String> taxonomySchemaTypes = new ArrayList<>();
		taxonomySchemaTypes.add("schemaType1" + id);
		taxonomySchemaTypes.add("schemaType2" + id);
		taxonomySchemaTypes.add("schemaType3" + id);
		taxonomySchemaTypes.add("schemaType4" + id);
		Taxonomy taxonomy = Taxonomy.createPublic("code" + id, labelTitle, "zeCollection", taxonomySchemaTypes);

		return taxonomy;
	}
}
