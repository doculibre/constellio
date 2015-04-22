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
		List<String> taxonomySchemaTypes = new ArrayList<>();
		for (Element schemaTypeElement : taxonomyElement.getChild(SCHEMA_TYPES).getChildren()) {
			taxonomySchemaTypes.add(schemaTypeElement.getText());
		}
		return new Taxonomy(code, title, collection, visibleInHomePage,
				userIds, groupIds, taxonomySchemaTypes);
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
