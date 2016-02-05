package com.constellio.model.services.taxonomies;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;

import com.constellio.model.entities.Taxonomy;

public class TaxonomiesWriter {

	private static final String SCHEMA_TYPE = "schemaType";
	private static final String SCHEMA_TYPES = "schemaTypes";
	private static final String TITLE = "title";
	private static final String CODE = "code";
	private static final String COLLECTION = "collection";
	private static final String USER_IDS = "userIds";
	private static final String GROUP_IDS = "groupIds";
	private static final String VISIBLE_IN_HOME_PAGE = "visibleInHomePage";
	private static final String TAXONOMY = "taxonomy";
	private static final String ENABLES = "enables";
	private static final String DISABLES = "disables";
	private static final String TAXONOMIES = "taxonomies";
	private final Document document;

	public TaxonomiesWriter(Document document) {
		this.document = document;
	}

	public void createEmptyTaxonomy() {
		Element disables = new Element(DISABLES);
		Element enables = new Element(ENABLES);
		Element taxonomies = new Element(TAXONOMIES);

		taxonomies.addContent(enables);
		taxonomies.addContent(disables);
		document.setRootElement(taxonomies);
	}

	public void addTaxonmy(Taxonomy taxonomy) {
		Element schemaTypesElements = new Element(SCHEMA_TYPES);
		for (String schemaType : taxonomy.getSchemaTypes()) {
			Element schemaTypeElement = new Element(SCHEMA_TYPE).setText(schemaType);
			schemaTypesElements.addContent(schemaTypeElement);
		}
		Element title = new Element(TITLE).setText(taxonomy.getTitle());
		Element collection = new Element(COLLECTION).setText(taxonomy.getCollection());
		Element taxonomyElement = new Element(TAXONOMY).setAttribute(CODE, taxonomy.getCode());
		Element visibleInHomePageElement = new Element(VISIBLE_IN_HOME_PAGE).setText(
				taxonomy.isVisibleInHomePage() ? "true" : "false");
		Element groupIdsElement = new Element(GROUP_IDS).setText(StringUtils.join(taxonomy.getGroupIds(), ","));
		Element userIdsElement = new Element(USER_IDS).setText(StringUtils.join(taxonomy.getUserIds(), ","));
		taxonomyElement.addContent(title);
		taxonomyElement.addContent(collection);
		taxonomyElement.addContent(schemaTypesElements);
		taxonomyElement.addContent(visibleInHomePageElement);
		taxonomyElement.addContent(groupIdsElement);
		taxonomyElement.addContent(userIdsElement);

		Element enables = document.getRootElement().getChild(ENABLES);
		enables.addContent(taxonomyElement);
	}

	public void setPrincipalCode(String code) {
		document.getRootElement().setAttribute("principal", code);
	}

	public void enable(String code) {
		switchStatusTaxonomy(code, "enable");
	}

	public void disable(String code) {
		switchStatusTaxonomy(code, "disable");
	}

	void switchStatusTaxonomy(String code, String action) {
		Element elementsFrom;
		Element elementsTo;
		Element disables = document.getRootElement().getChild(DISABLES).detach();
		Element enables = document.getRootElement().getChild(ENABLES).detach();
		if (action.equals("enable")) {
			elementsFrom = disables;
			elementsTo = enables;
		} else {
			elementsFrom = enables;
			elementsTo = disables;
		}

		List<Element> elementsToRemove = new ArrayList<>();
		for (Element taxonomyElement : elementsFrom.getChildren()) {
			if (taxonomyElement.getAttributeValue(CODE).equals(code)) {
				elementsToRemove.add(taxonomyElement);
				elementsTo.addContent(taxonomyElement.clone());
			}
		}
		for (Element element : elementsToRemove) {
			element.detach();
		}

		document.getRootElement().addContent(elementsFrom);
		document.getRootElement().addContent(elementsTo);
	}

	public void editTaxonmy(Taxonomy taxonomy) {
		Element root = document.getRootElement();
		removeIfExists(taxonomy.getCode(), root);
		addTaxonmy(taxonomy);
	}

	private void removeIfExists(String taxonomieCode, Element root) {
		Element elementToRemove = null;
		Filter<Element> filters = Filters.element(TAXONOMY);
		for (Element element : root.getDescendants(filters)) {
			if (element.getAttributeValue(CODE).equals(taxonomieCode)) {
				elementToRemove = element;
				break;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
	}
}
