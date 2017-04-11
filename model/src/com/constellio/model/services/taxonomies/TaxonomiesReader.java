package com.constellio.model.services.taxonomies;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.Taxonomy;

public class TaxonomiesReader {

	private static final String DISABLES = "disables";
	private static final String TITLE = "title";
	private static final String CODE = "code";
	private static final String USER_IDS = "userIds";
	private static final String GROUP_IDS = "groupIds";
	private static final String VISIBLE_IN_HOME_PAGE = "visibleInHomePage";
	private static final String SHOW_PARENTS_IN_SEARCH_RESULTS = "showParentsInSearchResults";
	private static final String COLLECTION = "collection";
	private static final String ENABLES = "enables";
	private static final String SCHEMA_TYPES = "schemaTypes";
	private final Document document;

	public TaxonomiesReader(Document document) {
		this.document = document;
	}

	public String readPrincipalCode() {
		return document.getRootElement().getAttributeValue("principal");
	}

	public List<Taxonomy> readEnables() {
		List<Taxonomy> taxonomies = new ArrayList<>();
		Element enablesElement = document.getRootElement().getChild(ENABLES);
		for (Element taxonomyElement : enablesElement.getChildren()) {
			Taxonomy taxonomy = readTaxonomy(taxonomyElement);
			taxonomies.add(taxonomy);
		}
		return taxonomies;
	}

	public List<Taxonomy> readDisables() {
		List<Taxonomy> taxonomies = new ArrayList<>();
		Element enablesElement = document.getRootElement().getChild(DISABLES);
		for (Element taxonomyElement : enablesElement.getChildren()) {
			Taxonomy taxonomy = readTaxonomy(taxonomyElement);
			taxonomies.add(taxonomy);
		}
		return taxonomies;
	}

	private Taxonomy readTaxonomy(Element taxonomyElement) {
		String code = taxonomyElement.getAttributeValue(CODE);
		String title = taxonomyElement.getChildText(TITLE);
		String collection = taxonomyElement.getChildText(COLLECTION);
		List<String> userIds = toIdsList(taxonomyElement.getChildText(USER_IDS));
		List<String> groupIds = toIdsList(taxonomyElement.getChildText(GROUP_IDS));

		boolean visibleInHomePage = "true".equals(taxonomyElement.getChildText(VISIBLE_IN_HOME_PAGE));
		boolean showParents = "true".equals(taxonomyElement.getChildText(SHOW_PARENTS_IN_SEARCH_RESULTS));
		List<String> taxonomySchemaTypes = new ArrayList<>();
		for (Element schemaTypeElement : taxonomyElement.getChild(SCHEMA_TYPES).getChildren()) {
			taxonomySchemaTypes.add(schemaTypeElement.getText());
		}
		return new Taxonomy(code, title, collection, visibleInHomePage,
				userIds, groupIds, taxonomySchemaTypes, showParents);
	}

	private List<String> toIdsList(String idsStr) {
		List<String> ids;
		if (StringUtils.isNotBlank(idsStr)) {
			ids = asList(idsStr.split(","));
		} else {
			ids = Collections.emptyList();
		}
		return ids;
	}
}
