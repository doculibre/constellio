package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

@SuppressWarnings("serial")
public class MetadataToFormVOBuilder implements Serializable {

	private Language language;
	private SessionContext sessionContext;

	//TODO Thiago
	//	@Deprecated
	//	public MetadataToFormVOBuilder() {
	//	}

	public MetadataToFormVOBuilder(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
		this.language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
	}

	public FormMetadataVO build(Metadata metadata, SchemasDisplayManager configManager, String schemaTypeCode) {
		return build(metadata, null, configManager, schemaTypeCode);
	}

	public FormMetadataVO build(Metadata metadata, MetadataSchemaVO schemaVO, SchemasDisplayManager configManager,
			String schemaTypeCode) {
		MetadataDisplayConfig config = configManager.getMetadata(metadata.getCollection(), metadata.getCode());
		SchemaTypesDisplayConfig types = configManager.getTypes(metadata.getCollection());

		String code = metadata.getCode();
		MetadataValueType type = metadata.getType();
		boolean required = metadata.isDefaultRequirement();
		boolean multivalue = metadata.isMultivalue();
		String label = metadata.getLabel(language);
		MetadataInputType entry = config.getInputType();
		boolean sortable = metadata.isSortable();
		boolean searchable = metadata.isSearchable();
		boolean advancedSearch = config.isVisibleInAdvancedSearch();
		boolean highlight = config.isHighlight();
		boolean enabled = metadata.isEnabled();
		boolean facet = false;
		String metadataGroup = getValidMetadataGroup(config.getMetadataGroup(),
				configManager.getType(metadata.getCollection(), schemaTypeCode));

		for (String codeFacet : types.getFacetMetadataCodes()) {
			if (codeFacet.equals(metadata.getCode())) {
				facet = true;
				break;
			}
		}

		String reference = null;
		if (metadata.getType().equals(MetadataValueType.REFERENCE)) {
			reference = metadata.getAllowedReferences().getAllowedSchemaType();
		}

		boolean autocomplete = metadata.isSchemaAutocomplete();

		Object defaultValue = metadata.getDefaultValue();
		String inputMask = metadata.getInputMask();

		return new FormMetadataVO(code, type, required, schemaVO, reference, label, searchable, multivalue, sortable,
				advancedSearch, facet, entry, highlight, autocomplete, enabled, metadataGroup, defaultValue, inputMask);
	}

	private String getValidMetadataGroup(String metadataGroup, SchemaTypeDisplayConfig config) {
		String validGroup = metadataGroup;
		boolean found = false;
		for (String group : config.getMetadataGroup().keySet()) {
			if (group.equals(metadataGroup)) {
				found = true;
				break;
			}
		}

		if (!found) {
			validGroup = config.getMetadataGroup().keySet().iterator().next();
		}

		return validGroup;
	}
}
