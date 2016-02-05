package com.constellio.app.services.schemas.bulkImport;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.schemas.MetadataValueType;

public class ImportedMetadataVOBuilder {
	public ImportedMetadata build(Map<String, String> metadataElements)
			throws FormMetadataVOBuilderException {
		String localCode = getStringValue("localCode", metadataElements, null);
		if (StringUtils.isBlank(localCode)) {
			throw new FormMetadataVOBuilderException();
		}
		String schemaTypeCode = getStringValue("schemaTypeCode", metadataElements, null);
		if (StringUtils.isBlank(schemaTypeCode)) {
			throw new FormMetadataVOBuilderException();
		}
		String schemaCode = getStringValue("schemaCode", metadataElements, null);
		if (StringUtils.isBlank(schemaCode)) {
			throw new FormMetadataVOBuilderException();
		}
		String typeString = getStringValue("type", metadataElements, "string");
		MetadataValueType type = MetadataValueType.valueOf(typeString.toUpperCase());
		String reference = getStringValue("reference", metadataElements, "");
		;
		String label = getStringValue("label", metadataElements, localCode);
		boolean required = getBooleanValue("required", metadataElements, false);
		boolean multivalue = getBooleanValue("multivalue", metadataElements, false);
		boolean searchable = getBooleanValue("searchable", metadataElements, false);
		boolean sortable = getBooleanValue("sortable", metadataElements, false);
		boolean advancedSearch = getBooleanValue("advancedSearch", metadataElements, false);
		boolean facet = getBooleanValue("facet", metadataElements, false);
		boolean highlight = getBooleanValue("highlight", metadataElements, false);
		boolean autocomplete = getBooleanValue("autocomplete", metadataElements, false);
		boolean enabled = getBooleanValue("enabled", metadataElements, true);
		String metadataGroup = getStringValue("metadataGroup", metadataElements, "default");
		String copyMetadata = getStringValue("copyMetadata", metadataElements, null);
		String usingReference = getStringValue("copiedUsingReference", metadataElements, null);
		String calculator = getStringValue("calculator", metadataElements, null);
		boolean displayInAllSchemas = getBooleanValue("displayInAllSchemas", metadataElements, false);
		if (metadataGroup == null) {
			throw new FormMetadataVOBuilderException();
		}
		MetadataInputType input = getMetadataInputType(metadataElements);
		if (input == null) {
			List<MetadataInputType> list = MetadataInputType.getAvailableMetadataInputTypesFor(type, multivalue);
			if (list.isEmpty()) {
				input = MetadataInputType.FIELD;
			} else {
				input = list.get(0);
			}
		}
		return new ImportedMetadata(schemaTypeCode, schemaCode, localCode, type, required, reference,
				label, searchable, multivalue, sortable, advancedSearch, facet,
				input, highlight, autocomplete, enabled, metadataGroup, copyMetadata, usingReference, calculator, displayInAllSchemas);
	}

	private MetadataInputType getMetadataInputType(Map<String, String> metadataElements) {
		String inputString = getStringValue("input", metadataElements, null);
		if (inputString != null) {
			return MetadataInputType.valueOf(inputString.toUpperCase());
		} else {
			return null;
		}
	}

	private String getStringValue(String key, Map<String, String> metadataElements, String defaultValue) {
		String value = metadataElements.get(key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return value.trim();
	}

	private boolean getBooleanValue(String key, Map<String, String> metadataElements, boolean defaultValue) {
		String value = metadataElements.get(key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		value = value.trim();
		if (value.equalsIgnoreCase("true")) {
			return true;
		} else if (value.equalsIgnoreCase("false")) {
			return false;
		}
		return defaultValue;
	}
}
