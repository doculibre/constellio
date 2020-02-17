package com.constellio.app.services.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemasDisplayReader1 {
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
	private static final String VISIBLE_IN_ADVANCED_SEARCH = "VisibleInAdvancedSearch";
	private static final String HIGHLIGHT = "Highlight";
	private static final String METADATA_GROUPS_LABELS = "MetadataGroupsLabels";
	private static final String METADATA_GROUP_NAME = "name";
	private static final String METADATA_GROUP = "metadataGroup";
	private static final String METADATA_GROUP_CODE = "code";
	private static final String LABELS = "labels";
	private static final String HELP_MESSAGE = "HelpMessage";

	public static final String HELP_MESSAGE_CODE_SEPARATOR = "_";

	MetadataSchemaTypes types;
	Document document;
	List<Language> languages;

	public SchemasDisplayReader1(Document document, MetadataSchemaTypes types, List<Language> languages) {
		this.document = document;
		this.types = types;
		this.languages = languages;
	}

	public SchemasDisplayManagerCache readSchemaTypesDisplay(String collection, boolean enableEssentialMetadataHiding) {

		Element rootElement = document.getRootElement();

		SchemasDisplayManagerCache schemasDisplayManagerCache = new SchemasDisplayManagerCache(collection, languages);

		setSchemaTypesDisplayConfig(collection, rootElement, schemasDisplayManagerCache);

		setSchemaTypeDisplayConfigs(collection, rootElement, schemasDisplayManagerCache);

		setSchemaDisplayConfigs(collection, rootElement, schemasDisplayManagerCache, enableEssentialMetadataHiding);

		setMetadataDisplayConfigs(collection, rootElement, schemasDisplayManagerCache);

		return schemasDisplayManagerCache;
	}

	private void setSchemaTypesDisplayConfig(String collection, Element rootElement,
											 SchemasDisplayManagerCache schemasDisplayManagerCache) {
		SchemaTypesDisplayConfig schemaTypesDisplayConfig = convertElementToSchemaTypesDisplayConfig(collection,
				rootElement);
		schemasDisplayManagerCache.set(schemaTypesDisplayConfig);
	}

	private SchemaTypesDisplayConfig convertElementToSchemaTypesDisplayConfig(String collection, Element rootElement) {
		Element schemaTypesDisplayConfigElement = rootElement.getChild(SCHEMA_TYPES_DISPLAY_CONFIG);

		List<String> codes = new ArrayList<>();
		if (schemaTypesDisplayConfigElement != null) {
			for (Element children : schemaTypesDisplayConfigElement.getChildren()) {
				String code = children.getName();
				codes.add(code);
			}
		}

		SchemaTypesDisplayConfig schemaTypesDisplayConfig = new SchemaTypesDisplayConfig(collection, codes);
		return schemaTypesDisplayConfig;
	}

	private void setSchemaTypeDisplayConfigs(String collection, Element rootElement,
											 SchemasDisplayManagerCache schemasDisplayManagerCache) {
		Map<String, SchemaTypeDisplayConfig> schemaTypeDisplayConfig = convertElementToSchemaTypeDisplayConfigs(
				collection, rootElement);
		schemasDisplayManagerCache.setSchemaTypeDisplayConfigs(schemaTypeDisplayConfig);
	}

	private Map<String, SchemaTypeDisplayConfig> convertElementToSchemaTypeDisplayConfigs(String collection,
																						  Element rootElement) {
		Element schemaTypeDisplayConfigsElement = rootElement.getChild(SCHEMA_TYPE_DISPLAY_CONFIGS);

		if (schemaTypeDisplayConfigsElement != null) {
			Map<String, SchemaTypeDisplayConfig> schemaTypeDisplayConfigMap = new HashMap<>();
			for (Element child : schemaTypeDisplayConfigsElement.getChildren()) {
				boolean manageable = new Boolean(child.getAttributeValue(MANAGEABLE));
				boolean simpleSearch = new Boolean(child.getAttributeValue(SIMPLE_SEARCH));
				boolean advancedSearch = new Boolean(child.getAttributeValue(ADVANCED_SEARCH));
				String schemaType = child.getName();

				Map<String, Map<Language, String>> metadataGroups = new LinkedHashMap<>();

				for (Element metadataGroup : child.getChild(METADATA_GROUPS_LABELS).getChildren()) {
					if (metadataGroup.getAttributeValue(METADATA_GROUP_CODE) == null) {
						Map<Language, String> labels = new HashMap<>();
						labels.put(Language.French, metadataGroup.getAttributeValue(METADATA_GROUP_NAME));
						metadataGroups.put(metadataGroup.getAttributeValue(METADATA_GROUP_NAME), labels);
					} else {
						String code = metadataGroup.getAttributeValue(METADATA_GROUP_CODE);
						Map<Language, String> labels = new HashMap<>();
						labels.putAll(getLabels(metadataGroup));
						metadataGroups.put(code, labels);
					}
				}

				SchemaTypeDisplayConfig schemaTypeDisplayConfig = new SchemaTypeDisplayConfig(collection, schemaType, manageable,
						advancedSearch, simpleSearch, metadataGroups);
				schemaTypeDisplayConfigMap.put(schemaType, schemaTypeDisplayConfig);
			}
			return schemaTypeDisplayConfigMap;
		} else {
			return new HashMap<>();
		}
	}

	private Map<Language, String> getLabels(Element element) {
		Map<Language, String> labels = new HashMap<>();
		String labelValue = element.getAttributeValue(LABELS);
		if (StringUtils.isNotBlank(labelValue)) {
			List<String> languagesLabels = Arrays
					.asList(labelValue.split(SchemasDisplayWriter.LABEL_SEPARATOR));
			for (String languagesLabel : languagesLabels) {
				String[] keyValue = languagesLabel.split("=");
				Language language = Language.withCode(keyValue[0]);
				labels.put(language, keyValue[1]);
			}
		}
		return labels;
	}

	private void setSchemaDisplayConfigs(String collection, Element rootElement,
										 SchemasDisplayManagerCache schemasDisplayManagerCache,
										 boolean enableEssentialMetadataHiding) {
		Map<String, SchemaDisplayConfig> schemaDisplayConfigs = convertElementToSchemaDisplayConfigs(collection,
				rootElement, enableEssentialMetadataHiding);
		schemasDisplayManagerCache.setSchemaDisplayConfigs(schemaDisplayConfigs);
	}

	private Map<String, SchemaDisplayConfig> convertElementToSchemaDisplayConfigs(String collection,
																				  Element rootElement,
																				  boolean enableEssentialMetadataHiding) {

		Map<String, SchemaDisplayConfig> map = new HashMap<>();

		List<Element> schemaDisplayConfigsElements = rootElement.getChildren(SCHEMA_DISPLAY_CONFIGS);

		for (Element schemaDisplayConfigsElement : schemaDisplayConfigsElements) {
			if (schemaDisplayConfigsElement != null) {
				String schemaCode = schemaDisplayConfigsElement.getAttributeValue(SCHEMA_CODE);
				if (types.hasSchema(schemaCode)) {
					MetadataSchema schema = types.getSchema(schemaCode);
					Element displayMetadataCodesElement = schemaDisplayConfigsElement.getChild(DISPLAY_METADATA_CODES);

					List<String> displayMetadataCodes = new ArrayList<>();
					addElementValuesToListRestrictingToMetadatasOfSchema(schema, displayMetadataCodesElement, displayMetadataCodes);

					Element formMetadataCodesElement = schemaDisplayConfigsElement.getChild(FORM_METADATA_CODES);

					List<String> formMetadataCodes = new ArrayList<>();
					List<String> formHiddenMetadataCodes = new ArrayList<>();
					addElementValuesToListRestrictingToMetadatasOfSchema(schema, formMetadataCodesElement, formMetadataCodes);

					for (Metadata metadata : SchemaDisplayUtils.getRequiredMetadatasInSchemaForm(schema)) {
						if (!formMetadataCodes.contains(metadata.getCode())) {
							if (!enableEssentialMetadataHiding || metadata.getDefaultValue() == null) {
								formMetadataCodes.add(metadata.getCode());
							} else {
								formHiddenMetadataCodes.add(metadata.getCode());
							}
						}
					}

					List<String> availables = SchemaDisplayUtils.getAvailableMetadatasInSchemaForm(schema).toMetadatasCodesList();
					for (Iterator<String> iterator = formMetadataCodes.iterator(); iterator.hasNext(); ) {
						if (!availables.contains(iterator.next())) {
							iterator.remove();
						}
					}
					for (Iterator<String> iterator = formHiddenMetadataCodes.iterator(); iterator.hasNext(); ) {
						if (!availables.contains(iterator.next())) {
							iterator.remove();
						}
					}

					Element searchResultsMetadataCodesElement = schemaDisplayConfigsElement
							.getChild(SEARCH_RESULTS_METADATA_CODES);

					List<String> searchResultsMetadataCodes = new ArrayList<>();
					addElementValuesToListRestrictingToMetadatasOfSchema(schema, searchResultsMetadataCodesElement, searchResultsMetadataCodes);

					Element tableMetadataCodesElement = schemaDisplayConfigsElement
							.getChild(TABLE_METADATA_CODES);

					List<String> tableMetadataCodes = new ArrayList<>();
					addElementValuesToListRestrictingToMetadatasOfSchemaType(schema.getSchemaType(), tableMetadataCodesElement, tableMetadataCodes);

					SchemaDisplayConfig schemaDisplayConfig = new SchemaDisplayConfig(collection, schemaCode,
							displayMetadataCodes, formMetadataCodes, formHiddenMetadataCodes,
							searchResultsMetadataCodes, tableMetadataCodes);

					map.put(schemaCode, schemaDisplayConfig);
				}
			}
		}
		return map;
	}

	private void addElementValuesToListRestrictingToMetadatasOfSchema(MetadataSchema schema, Element element,
																	  List<String> list) {
		if (element != null) {
			for (Element e : element.getChildren()) {
				Metadata metadata = schema.getMetadataWithCodeOrNull(e.getName());
				if (metadata != null) {
					if (schema.hasInheritance()) {
						list.add(metadata.getCode());
					} else {
						list.add(metadata.getInheritance() == null ? metadata.getCode() : metadata.getInheritance().getCode());
					}
				}
			}
		}
	}


	private void addElementValuesToListRestrictingToMetadatasOfSchemaType(MetadataSchemaType schemaType,
																		  Element element,
																		  List<String> list) {
		if (element != null) {
			for (Element e : element.getChildren()) {
				Metadata metadata = schemaType.getMetadataWithCodeOrNull(e.getName());
				if (metadata != null) {
					list.add(metadata.getInheritance() == null ? metadata.getCode() : metadata.getInheritance().getCode());
				}
			}
		}
	}

	private void setMetadataDisplayConfigs(String collection, Element rootElement,
										   SchemasDisplayManagerCache schemasDisplayManagerCache) {
		Map<String, MetadataDisplayConfig> metadataDisplayConfigs = convertElementToMetadataDisplayConfigs(collection,
				rootElement, schemasDisplayManagerCache);
		schemasDisplayManagerCache.setMetadataDisplayConfigs(metadataDisplayConfigs);
	}

	private Map<String, MetadataDisplayConfig> convertElementToMetadataDisplayConfigs(String collection,
																					  Element rootElement,
																					  SchemasDisplayManagerCache schemasDisplayManagerCache) {
		Map<String, MetadataDisplayConfig> metadataDisplayConfigs = new HashMap<>();

		List<Element> metadataDisplayConfigsElements = rootElement.getChildren(METADATA_DISPLAY_CONFIGS);

		for (Element metadataDisplayConfigsElement : metadataDisplayConfigsElements) {
			if (metadataDisplayConfigsElement != null) {
				for (Element metadataDisplayConfigElement : metadataDisplayConfigsElement.getChildren()) {

					MetadataDisplayConfig metadataDisplayConfig = convertElementToMetadataDisplayConfig(collection,
							metadataDisplayConfigElement, schemasDisplayManagerCache);

					metadataDisplayConfigs.put(metadataDisplayConfigElement.getName(), metadataDisplayConfig);

				}
			}
		}
		return metadataDisplayConfigs;
	}

	private MetadataDisplayConfig convertElementToMetadataDisplayConfig(String collection,
																		Element metadataDisplayConfigElement,
																		SchemasDisplayManagerCache schemasDisplayManagerCache) {
		String metadataCode = metadataDisplayConfigElement.getName();
		String visibleInAdvancedSearchString = metadataDisplayConfigElement
				.getAttributeValue(VISIBLE_IN_ADVANCED_SEARCH);
		boolean visibleInAdvancedSearch = new Boolean(visibleInAdvancedSearchString);

		String highlightString = metadataDisplayConfigElement
				.getAttributeValue(HIGHLIGHT);
		boolean highlight = new Boolean(highlightString);
		String metadataGroup = metadataDisplayConfigElement.getAttributeValue(METADATA_GROUP);

		String typeCode = new SchemaUtils().getSchemaTypeCode(metadataCode);
		Map<String, Map<Language, String>> groups = schemasDisplayManagerCache.getType(typeCode).getMetadataGroup();

		String inputTypeString = metadataDisplayConfigElement.getAttributeValue(INPUT_TYPE);
		String displayTypeString = metadataDisplayConfigElement.getAttributeValue(DISPLAY_TYPE);
		if (displayTypeString == null) {
			displayTypeString = "VERTICAL";
		}
		Map<Language, String> helpMessages = readHelpMessages(metadataDisplayConfigElement);
		MetadataInputType metadataInputType = MetadataInputType.valueOf(inputTypeString);
		MetadataDisplayType metadataDisplayType = MetadataDisplayType.valueOf(displayTypeString);

		MetadataDisplayConfig metadataDisplayConfig = new MetadataDisplayConfig(collection, metadataCode,
				visibleInAdvancedSearch, metadataInputType, highlight, metadataGroup, metadataDisplayType, helpMessages);
		return metadataDisplayConfig;
	}

	private Map<Language, String> readHelpMessages(Element metadataDisplayConfigElement) {
		Map<Language, String> helpMessages = new HashMap<>();

		for (Language language : languages) {
			helpMessages.put(language, metadataDisplayConfigElement
					.getAttributeValue(HELP_MESSAGE + HELP_MESSAGE_CODE_SEPARATOR + language.getCode()));
		}
		return helpMessages;
	}
}