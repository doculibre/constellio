package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.model.entities.Language;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.*;
import java.util.Map.Entry;

public class SchemasDisplayWriter {
	private static final String ROOT = "display";
	private static final String SCHEMA_TYPES_DISPLAY_CONFIG = "SchemaTypesDisplayConfig";
	private static final String COLLECTION = "collection";
	private static final String SCHEMA_TYPE_DISPLAY_CONFIGS = "SchemaTypeDisplayConfigs";
	private static final String MANAGEABLE = "manageable";
	private static final String SIMPLE_SEARCH = "simpleSearch";
	private static final String ADVANCED_SEARCH = "advancedSearch";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String SCHEMA_DISPLAY_CONFIGS = "SchemaDisplayConfigs";
	private static final String DISPLAY_METADATA_CODES = "DisplayMetadataCodes";
	private static final String SCHEMA_CODE = "SchemaCode";
	private static final String FORM_METADATA_CODES = "FormMetadataCodes";
	private static final String SEARCH_RESULTS_METADATA_CODES = "SearchResultsMetadataCodes";
	private static final String TABLE_METADATA_CODES = "TableMetadataCodes";
	private static final String METADATA_DISPLAY_CONFIGS = "MetadataDisplayConfigs";
	private static final String INPUT_TYPE = "InputType";
	private static final String DISPLAY_TYPE = "DisplayType";
	private static final String SORTING_TYPE = "SortingType";
	private static final String VISIBLE_IN_ADVANCED_SEARCH = "VisibleInAdvancedSearch";
	private static final String HIGHLIGHT = "Highlight";
	private static final String METADATA_GROUP_LABEL = "MetadataGroupLabel";
	private static final String METADATA_GROUPS_LABELS = "MetadataGroupsLabels";
	private static final String LABELS = "labels";
	private static final String METADATA_GROUP_CODE = "code";
	private static final String METADATA_GROUP = "metadataGroup";

	public static final String FORMAT_ATTRIBUTE = "format";
	public static final String FORMAT_VERSION = SchemasDisplayReader2.FORMAT_VERSION;

	public static final String LABEL_SEPARATOR = ";;";

	Document document;

	public SchemasDisplayWriter(Document document) {
		this.document = document;
	}

	public void writeEmptyDocument() {
		Element display = new Element(ROOT);
		document.setRootElement(display);
		document.getRootElement().setAttribute(FORMAT_ATTRIBUTE, FORMAT_VERSION);
	}

	public void saveTypes(SchemaTypesDisplayConfig config) {
		Element rootElement = document.getRootElement();

		Element schemaTypesDisplayConfigElement = getOrCreateElementFromParent(rootElement, SCHEMA_TYPES_DISPLAY_CONFIG);

		for (String facetMetadataCode : config.getFacetMetadataCodes()) {
			Element facetMetadataCodeElement = schemaTypesDisplayConfigElement.getChild(facetMetadataCode);

			if (facetMetadataCodeElement == null) {
				facetMetadataCodeElement = new Element(facetMetadataCode);
				schemaTypesDisplayConfigElement.addContent(facetMetadataCodeElement);
			}
		}

		Set<String> codesToKeep = new HashSet<>(config.getFacetMetadataCodes());
		for (Element elementToCheckForRemoval : schemaTypesDisplayConfigElement.getChildren()) {
			if (!codesToKeep.contains(elementToCheckForRemoval.getName())) {
				schemaTypesDisplayConfigElement.removeChild(elementToCheckForRemoval.getName());
			}
		}
	}

	private Element getOrCreateElementFromParent(Element rootElement, String elementName) {
		return getOrCreateElementFromParent(rootElement, elementName, "", "");
	}

	private void removeElementFromParent(Element rootElement, String elementName, String attributeName,
										 String attributeValue) {
		List<Element> elements = rootElement.getChildren(elementName);
		for (Element element : elements) {
			if (StringUtils.isNotBlank(attributeName)) {
				if (attributeValue.equals(element.getAttributeValue(attributeName))) {
					element.detach();
					return;
				}
			} else {
				element.detach();
				return;
			}
		}
	}

	private Element getOrCreateElementFromParent(Element rootElement, String elementName, String attributeName,
												 String attributeValue) {
		Element newElement = null;
		List<Element> elements = rootElement.getChildren(elementName);
		for (Element element : elements) {
			if (StringUtils.isNotBlank(attributeName)) {
				if (attributeValue.equals(element.getAttributeValue(attributeName))) {
					newElement = element.clone();
					rootElement.addContent(newElement);
					element.detach();
					return newElement;
				}
			} else {
				newElement = element.clone();
				rootElement.addContent(newElement);
				element.detach();
				return newElement;
			}
		}
		if (newElement == null) {
			newElement = createAndAddElement(rootElement, elementName, attributeName, attributeValue);
		}
		return newElement;
	}

	private Element createAndAddElement(Element rootElement, String elementName, String attributeName,
										String attributeValue) {
		Element newElement;
		newElement = new Element(elementName);
		if (StringUtils.isNotBlank(attributeName)) {
			newElement.setAttribute(attributeName, attributeValue);
		}
		rootElement.addContent(newElement);
		return newElement;
	}

	private Element createMetadataGroupLabel(Element rootElement, String codeValue, Map<Language, String> labelsValue) {
		Element newElement;
		newElement = new Element(METADATA_GROUP_LABEL);
		if (StringUtils.isNotBlank(METADATA_GROUP_CODE)) {
			newElement.setAttribute(METADATA_GROUP_CODE, codeValue != null ? codeValue : "");
			newElement.setAttribute(LABELS, labelToSemiColonStringSeparated(labelsValue));
		}
		rootElement.addContent(newElement);
		return newElement;
	}

	private String labelToSemiColonStringSeparated(Map<Language, String> labels) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Entry<Language, String>> entries = new ArrayList<>(labels.entrySet());
		Collections.sort(entries, new Comparator<Entry<Language, String>>() {
			@Override
			public int compare(Entry<Language, String> o1, Entry<Language, String> o2) {
				return o1.getKey().getCode().compareTo(o2.getKey().getCode());
			}
		});

		for (Entry<Language, String> entry : entries) {
			stringBuilder.append(entry.getKey().getCode() + "=" + entry.getValue() + LABEL_SEPARATOR);
		}
		return stringBuilder.toString();
	}

	public void saveType(SchemaTypeDisplayConfig config) {
		Element rootElement = document.getRootElement();

		Element schemaTypeDisplayConfigs = getOrCreateElementFromParent(rootElement, SCHEMA_TYPE_DISPLAY_CONFIGS);

		Element schemaTypeDisplayConfig = schemaTypeDisplayConfigs.getChild(config.getSchemaType());

		if (schemaTypeDisplayConfig == null) {
			schemaTypeDisplayConfig = new Element(config.getSchemaType());
			schemaTypeDisplayConfigs.addContent(schemaTypeDisplayConfig);
		}
		schemaTypeDisplayConfig.setAttribute(MANAGEABLE, config.isManageable() ? TRUE : FALSE);
		schemaTypeDisplayConfig.setAttribute(SIMPLE_SEARCH, config.isSimpleSearch() ? TRUE : FALSE);
		schemaTypeDisplayConfig.setAttribute(ADVANCED_SEARCH, config.isAdvancedSearch() ? TRUE : FALSE);

		Element metadataGroups = getOrCreateElementFromParent(schemaTypeDisplayConfig, METADATA_GROUPS_LABELS);

		for (Element child : metadataGroups.getChildren()) {
			metadataGroups.removeChildren(METADATA_GROUP_LABEL);
		}

		for (String code : config.getMetadataGroup().keySet()) {
			createMetadataGroupLabel(metadataGroups, code, config.getMetadataGroup().get(code));
		}
	}

	public void saveSchema(SchemaDisplayConfig config) {
		Element rootElement = document.getRootElement();

		Element schemaDisplayConfigsElement = getOrCreateElementFromParent(rootElement, SCHEMA_DISPLAY_CONFIGS,
				SCHEMA_CODE, config.getSchemaCode());

		process(config, schemaDisplayConfigsElement, DISPLAY_METADATA_CODES, config.getDisplayMetadataCodes());
		process(config, schemaDisplayConfigsElement, FORM_METADATA_CODES, config.getFormMetadataCodes());
		process(config, schemaDisplayConfigsElement, SEARCH_RESULTS_METADATA_CODES,
				config.getSearchResultsMetadataCodes());
		process(config, schemaDisplayConfigsElement, TABLE_METADATA_CODES,
				config.getTableMetadataCodes());
	}

	private void process(SchemaDisplayConfig config, Element schemaDisplayConfigsElement, String metadataCodeName,
						 List<String> metadataCodes) {
		Element metadataCodesElement = getOrCreateElementFromParent(schemaDisplayConfigsElement, metadataCodeName);

		for (String metadataCode : metadataCodes) {
			getOrCreateElementFromParent(metadataCodesElement, metadataCode);
		}
		removeInvalidStringsFromElement(metadataCodes, metadataCodesElement);
	}

	private void removeInvalidStringsFromElement(List<String> validStrings, Element existingStringsElement) {
		List<Element> elementsToRemove = new ArrayList<>();
		Set<String> stringsToKeep = new HashSet<>(validStrings);
		for (Element elementToCheckForRemoval : existingStringsElement.getChildren()) {
			if (!stringsToKeep.contains(elementToCheckForRemoval.getName())) {
				//				existingStringsElement.removeChild(elementToCheckForRemoval.getName());
				elementsToRemove.add(existingStringsElement.getChild(elementToCheckForRemoval.getName()));
			}
		}
		for (Element elementoRemove : elementsToRemove) {
			elementoRemove.detach();
		}
	}

	public void saveMetadata(MetadataDisplayConfig config) {
		Element rootElement = document.getRootElement();

		Element metadataDisplayConfigs = getOrCreateElementFromParent(rootElement, METADATA_DISPLAY_CONFIGS);

		Element metadata = getOrCreateElementFromParent(metadataDisplayConfigs, config.getMetadataCode());
		metadata.setAttribute(VISIBLE_IN_ADVANCED_SEARCH, config.isVisibleInAdvancedSearch() ? TRUE : FALSE);
		metadata.setAttribute(INPUT_TYPE, config.getInputType().name());

		if (config.getDisplayType() != null && config.getDisplayType() != MetadataDisplayType.VERTICAL) {
			metadata.setAttribute(DISPLAY_TYPE, config.getDisplayType().name());
		} else {
			metadata.removeAttribute(DISPLAY_TYPE);
		}

		if (config.getSortingType() != null && config.getSortingType() != MetadataSortingType.ENTRY_ORDER) {
			metadata.setAttribute(SORTING_TYPE, config.getSortingType().name());
		} else {
			metadata.removeAttribute(SORTING_TYPE);
		}
		metadata.setAttribute(HIGHLIGHT, config.isHighlight() ? TRUE : FALSE);
		metadata.setAttribute(METADATA_GROUP,
				StringUtils.isBlank(config.getMetadataGroupCode()) ? "" : config.getMetadataGroupCode());
	}

	public void resetSchema(String code) {
		Element rootElement = document.getRootElement();
		removeElementFromParent(rootElement, SCHEMA_DISPLAY_CONFIGS, SCHEMA_CODE, code);
		Element metadatas = rootElement.getChild(METADATA_DISPLAY_CONFIGS);
		if (metadatas != null) {
			List<Element> elementsToDetach = new ArrayList<>();
			List<Element> elements = metadatas.getChildren();
			if (elements != null) {
				for (Element element : elements) {
					if (element.getName().startsWith(code)) {
						elementsToDetach.add(element);
					}
				}
			}
			for (Element elementToDetach : elementsToDetach) {
				elementToDetach.detach();
			}
		}
	}

	public void resetMetadata(String code) {
		Element rootElement = document.getRootElement();
		Element metadataDisplayConfigs = getOrCreateElementFromParent(rootElement, METADATA_DISPLAY_CONFIGS);
		metadataDisplayConfigs.removeChild(code);
	}
}