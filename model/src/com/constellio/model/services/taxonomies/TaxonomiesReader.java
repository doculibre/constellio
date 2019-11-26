package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class TaxonomiesReader {

	private static final String DISABLES = "disables";
	private static final String TITLE = "title";
	private static final String CODE = "code";
	private static final String ABBREVIATION = "abbreviation";
	private static final String USER_IDS = "userIds";
	private static final String GROUP_IDS = "groupIds";
	private static final String VISIBLE_IN_HOME_PAGE = "visibleInHomePage";
	private static final String SHOW_PARENTS_IN_SEARCH_RESULTS = "showParentsInSearchResults";
	private static final String COLLECTION = "collection";
	private static final String ENABLES = "enables";
	private static final String SCHEMA_TYPES = "schemaTypes";
	private final Document document;
	private final List<String> languageCollectionSupported;

	public TaxonomiesReader(Document document, List<String> languageCollectionSupported) {
		this.document = document;
		this.languageCollectionSupported = languageCollectionSupported;
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

		Map<Language, String> languageTitleMap = new HashMap<>();
		String title = taxonomyElement.getChildText(TITLE);
		extractLanguageValues(taxonomyElement, languageTitleMap, TITLE, title);

		Map<Language, String> languageAbbreviationMap = new HashMap<>();
		String abbreviation = taxonomyElement.getChildText(ABBREVIATION);
		if (abbreviation != null) {
			extractLanguageValues(taxonomyElement, languageAbbreviationMap, ABBREVIATION, abbreviation);
		}

		String collection = taxonomyElement.getChildText(COLLECTION);
		List<String> userIds = toIdsList(taxonomyElement.getChildText(USER_IDS));
		List<String> groupIds = toIdsList(taxonomyElement.getChildText(GROUP_IDS));

		boolean visibleInHomePage = "true".equals(taxonomyElement.getChildText(VISIBLE_IN_HOME_PAGE));
		boolean showParents = "true".equals(taxonomyElement.getChildText(SHOW_PARENTS_IN_SEARCH_RESULTS));
		List<String> taxonomySchemaTypes = new ArrayList<>();
		for (Element schemaTypeElement : taxonomyElement.getChild(SCHEMA_TYPES).getChildren()) {
			taxonomySchemaTypes.add(schemaTypeElement.getText());
		}

		return new Taxonomy(code, languageTitleMap, languageAbbreviationMap, collection, visibleInHomePage,
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

	private void extractLanguageValues(Element taxonomyElement, Map<Language, String> languageMap,
									   String prefix, String text) {
		List<Attribute> attributeList = taxonomyElement.getChild(prefix).getAttributes();
		if (attributeList.size() > 0) {
			for (Attribute currentAttribute : attributeList) {
				if (currentAttribute.getName().startsWith(prefix)) {
					String languageCode = currentAttribute.getName().replace(prefix, "");
					Language language = Language.withCode(languageCode);
					languageMap.put(language, currentAttribute.getValue());
				}
			}
		}
		if (languageMap.size() == 0) {
			for (String languageCollection : languageCollectionSupported) {
				Language language = Language.withCode(languageCollection);
				languageMap.put(language, text);
			}
		}
	}
}
