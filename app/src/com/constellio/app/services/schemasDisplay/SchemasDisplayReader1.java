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
package com.constellio.app.services.schemasDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;

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
	private static final String METADATA_DISPLAY_CONFIGS = "MetadataDisplayConfigs";
	private static final String INPUT_TYPE = "InputType";
	private static final String VISIBLE_IN_ADVANCED_SEARCH = "VisibleInAdvancedSearch";
	private static final String HIGHLIGHT = "Highlight";
	private static final String METADATA_GROUPS_LABELS = "MetadataGroupsLabels";
	private static final String METADATA_GROUP_NAME = "name";
	private static final String METADATA_GROUP = "metadataGroup";

	MetadataSchemaTypes types;
	Document document;

	public SchemasDisplayReader1(Document document, MetadataSchemaTypes types) {
		this.document = document;
		this.types = types;
	}

	public SchemasDisplayManagerCache readSchemaTypesDisplay(String collection) {

		Element rootElement = document.getRootElement();

		SchemasDisplayManagerCache schemasDisplayManagerCache = new SchemasDisplayManagerCache(collection);

		setSchemaTypesDisplayConfig(collection, rootElement, schemasDisplayManagerCache);

		setSchemaTypeDisplayConfigs(collection, rootElement, schemasDisplayManagerCache);

		setSchemaDisplayConfigs(collection, rootElement, schemasDisplayManagerCache);

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

				List<String> metadataGroups = new ArrayList<>();
				for (Element metadataGroup : child.getChild(METADATA_GROUPS_LABELS).getChildren()) {
					metadataGroups.add(metadataGroup.getAttributeValue(METADATA_GROUP_NAME));
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

	private void setSchemaDisplayConfigs(String collection, Element rootElement,
			SchemasDisplayManagerCache schemasDisplayManagerCache) {
		Map<String, SchemaDisplayConfig> schemaDisplayConfigs = convertElementToSchemaDisplayConfigs(collection,
				rootElement);
		schemasDisplayManagerCache.setSchemaDisplayConfigs(schemaDisplayConfigs);
	}

	private Map<String, SchemaDisplayConfig> convertElementToSchemaDisplayConfigs(String collection, Element rootElement) {

		Map<String, SchemaDisplayConfig> map = new HashMap<>();

		List<Element> schemaDisplayConfigsElements = rootElement.getChildren(SCHEMA_DISPLAY_CONFIGS);

		for (Element schemaDisplayConfigsElement : schemaDisplayConfigsElements) {
			if (schemaDisplayConfigsElement != null) {
				String schemaCode = schemaDisplayConfigsElement.getAttributeValue(SCHEMA_CODE);
				MetadataSchema schema = types.getSchema(schemaCode);
				Element displayMetadataCodesElement = schemaDisplayConfigsElement.getChild(DISPLAY_METADATA_CODES);

				List<String> displayMetadataCodes = new ArrayList<>();
				if (displayMetadataCodesElement != null) {
					for (Element e : displayMetadataCodesElement.getChildren()) {
						displayMetadataCodes.add(e.getName());
					}
				}

				Element formMetadataCodesElement = schemaDisplayConfigsElement.getChild(FORM_METADATA_CODES);

				List<String> formMetadataCodes = new ArrayList<>();
				if (formMetadataCodesElement != null) {
					for (Element e : formMetadataCodesElement.getChildren()) {
						formMetadataCodes.add(e.getName());
					}
				}

				for (Metadata metadata : SchemaDisplayUtils.getRequiredMetadatasInSchemaForm(schema)) {
					if (!formMetadataCodes.contains(metadata.getCode())) {
						formMetadataCodes.add(metadata.getCode());
					}
				}

				List<String> availables = SchemaDisplayUtils.getAvailableMetadatasInSchemaForm(schema).toMetadatasCodesList();
				for (Iterator<String> iterator = formMetadataCodes.iterator(); iterator.hasNext(); ) {
					if (!availables.contains(iterator.next())) {
						iterator.remove();
					}
				}

				Element searchResultsMetadataCodesElement = schemaDisplayConfigsElement
						.getChild(SEARCH_RESULTS_METADATA_CODES);

				List<String> searchResultsMetadataCodes = new ArrayList<>();
				if (searchResultsMetadataCodesElement != null) {
					for (Element e : searchResultsMetadataCodesElement.getChildren()) {
						searchResultsMetadataCodes.add(e.getName());
					}
				}

				SchemaDisplayConfig schemaDisplayConfig = new SchemaDisplayConfig(collection, schemaCode,
						displayMetadataCodes, formMetadataCodes, searchResultsMetadataCodes);

				map.put(schemaCode, schemaDisplayConfig);
			}
		}
		return map;
	}

	private void setMetadataDisplayConfigs(String collection, Element rootElement,
			SchemasDisplayManagerCache schemasDisplayManagerCache) {
		Map<String, MetadataDisplayConfig> metadataDisplayConfigs = convertElementToMetadataDisplayConfigs(collection,
				rootElement, schemasDisplayManagerCache);
		schemasDisplayManagerCache.setMetadataDisplayConfigs(metadataDisplayConfigs);
	}

	private Map<String, MetadataDisplayConfig> convertElementToMetadataDisplayConfigs(String collection,
			Element rootElement, SchemasDisplayManagerCache schemasDisplayManagerCache) {
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
			Element metadataDisplayConfigElement, SchemasDisplayManagerCache schemasDisplayManagerCache) {
		String metadataCode = metadataDisplayConfigElement.getName();
		String visibleInAdvancedSearchString = metadataDisplayConfigElement
				.getAttributeValue(VISIBLE_IN_ADVANCED_SEARCH);
		boolean visibleInAdvancedSearch = new Boolean(visibleInAdvancedSearchString);

		String highlightString = metadataDisplayConfigElement
				.getAttributeValue(HIGHLIGHT);
		boolean highlight = new Boolean(highlightString);
		String metadataGroup = metadataDisplayConfigElement.getAttributeValue(METADATA_GROUP);

		String typeCode = new SchemaUtils().getSchemaTypeCode(metadataCode);
		List<String> groups = schemasDisplayManagerCache.getType(typeCode).getMetadataGroup();

		if (StringUtils.isBlank(metadataGroup) || !groups.contains(metadataGroup)) {
			metadataGroup = groups.isEmpty() ? null : groups.get(0);
		}

		String inputTypeString = metadataDisplayConfigElement.getAttributeValue(INPUT_TYPE);
		MetadataInputType metadataInputType = MetadataInputType.valueOf(inputTypeString);
		MetadataDisplayConfig metadataDisplayConfig = new MetadataDisplayConfig(collection, metadataCode,
				visibleInAdvancedSearch, metadataInputType, highlight, metadataGroup);
		return metadataDisplayConfig;
	}
}