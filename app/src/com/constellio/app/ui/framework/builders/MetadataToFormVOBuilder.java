package com.constellio.app.ui.framework.builders;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.SchemaUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("serial")
public class MetadataToFormVOBuilder implements Serializable {

	private SessionContext sessionContext;

	public MetadataToFormVOBuilder(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public FormMetadataVO build(Metadata metadata, SchemasDisplayManager configManager, String schemaTypeCode,
								SessionContext sessionContext) {
		return build(metadata, null, configManager, schemaTypeCode, sessionContext);
	}

	public FormMetadataVO build(Metadata metadata, MetadataSchemaVO schemaVO, SchemasDisplayManager configManager,
								String schemaTypeCode, SessionContext sessionContext) {
		MetadataDisplayConfig config = configManager.getMetadata(metadata.getCollection(), metadata.getCode());
		SchemaTypesDisplayConfig types = configManager.getTypes(metadata.getCollection());

		this.sessionContext = sessionContext;

		String code = metadata.getCode();
		MetadataValueType type = metadata.getType();
		boolean required = metadata.isDefaultRequirement();
		boolean multivalue = metadata.isMultivalue();
		Map<Language, String> labels = metadata.getLabels();
		MetadataInputType entry = config.getInputType();
		MetadataDisplayType displayType = config.getDisplayType();
		MetadataSortingType sortingType = config.getSortingType();
		boolean sortable = metadata.isSortable();
		boolean searchable = metadata.isSearchable();
		boolean isMultiLingual = metadata.isMultiLingual();
		Map<Language, String> helpMessages = config.getHelpMessages();

		boolean advancedSearch = config.isVisibleInAdvancedSearch();
		boolean highlight = config.isHighlight();
		boolean enabled = metadata.isEnabled();
		boolean facet = false;
		String metadataGroup = getValidMetadataGroup(config.getMetadataGroupCode(),
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
			Set<String> allowedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
			if (reference == null && allowedSchemas != null && allowedSchemas.size() == 1) {
				reference = SchemaUtils.getSchemaTypeCode((String) allowedSchemas.toArray()[0]);
			}
		}

		boolean autocomplete = metadata.isSchemaAutocomplete();
		boolean availableInSummary = metadata.isAvailableInSummary();

		Object defaultValue = metadata.getDefaultValue();
		String inputMask = metadata.getInputMask();

		Map<String, String> newLabels = new HashMap<>();
		for (Entry<Language, String> entryLabels : labels.entrySet()) {
			newLabels.put(entryLabels.getKey().getCode(), entryLabels.getValue());
		}
		Map<String, String> newHelpMessages = new HashMap<>();
		for (Entry<Language, String> entryHelpMessages : helpMessages.entrySet()) {
			newHelpMessages.put(entryHelpMessages.getKey().getCode(), entryHelpMessages.getValue());
		}

		boolean duplicable = metadata.isDuplicable();
		boolean uniqueValue = metadata.isUniqueValue();

		DataEntryType dataEntryType = metadata.getDataEntry().getType();
		String dataEntryRef = null;
		String dataEntrySource = null;
		if (dataEntryType == DataEntryType.COPIED) {
			CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();
			dataEntryRef = dataEntry.getReferenceMetadata();
			dataEntrySource = dataEntry.getCopiedMetadata();
		}


		String localCodeParsed = SchemaUtils.underscoreSplitWithCache(code)[2];
		if (localCodeParsed.contains("USR")) {
			localCodeParsed = localCodeParsed.split("USR", 2)[1];
		}

		FormMetadataVO formMetadataVO = FormMetadataVO.builder().id(metadata.getId()).code(code).localcode(localCodeParsed).valueType(type)
				.required(required).schema(schemaVO).reference(reference).labels(new HashMap<>(newLabels)).searchable(searchable)
				.multivalue(multivalue).sortable(sortable).advancedSearch(advancedSearch).facet(facet).input(entry)
				.displayType(displayType).sortingType(sortingType).highlight(highlight).autocomplete(autocomplete)
				.availableInSummary(availableInSummary).enabled(enabled).metadataGroup(metadataGroup)
				.defaultValue(defaultValue).inputMask(inputMask).duplicable(duplicable).uniqueValue(uniqueValue)
				.customAttributes(new HashSet<>(metadata.getCustomAttributes())).inheritance(null)
				.currentLanguageCode(sessionContext.getCurrentLocale().getLanguage())
				.isMultiLingual(isMultiLingual).maxLength(metadata.getMaxLength())
				.measurementUnit(metadata.getMeasurementUnit()).helpMessages(newHelpMessages)
				.dataEntryType(dataEntryType).dataEntryReference(dataEntryRef).dataEntrySource(dataEntrySource)
				.separatedStructure(metadata.isSeparatedStructure()).build();

		//		public FormMetadataVO(short id, String code, MetadataValueType valueType, boolean required, MetadataSchemaVO schema,
		//				String reference,
		//				Map<String, String> labels, boolean searchable, boolean multivalue, boolean sortable,
		//		boolean advancedSearch,
		//		boolean facet,
		//		MetadataInputType input, MetadataDisplayType displayType, MetadataSortingType sortingType,
		//		boolean highlight,
		//		boolean autocomplete, boolean availableInSummary, boolean enabled,
		//		String metadataGroup,
		//		Object defaultValue, String inputMask, boolean duplicable, boolean uniqueValue,
		//		Set<String> customAttributes, SessionContext sessionContext, boolean isMultiLingual,
		//		Integer maxLength, String measurementUnit,
		//				Map<String, String> helpMessages,
		//				DataEntryType dataEntryType, String dataEntryReference, String dataEntrySource) {

		//		FormMetadataVO formMetadataVO = new FormMetadataVO(metadata.getId(), code, type, required, schemaVO, reference, newLabels, searchable,
		//				multivalue, sortable,
		//				advancedSearch, facet, entry, displayType, sortingType, highlight, autocomplete, availableInSummary, enabled, metadataGroup, defaultValue,
		//				inputMask,
		//				duplicable, uniqueValue,
		//				metadata.getCustomAttributes(),
		//				sessionContext, isMultiLingual, metadata.getMaxLength(), metadata.getMeasurementUnit(), newHelpMessages,
		//				dataEntryType, dataEntryRef, dataEntrySource);

		if (metadata.getInheritance() != null) {
			formMetadataVO.setInheritance(
					this.build(metadata.getInheritance(), schemaVO, configManager, schemaTypeCode, sessionContext));
		}
		return formMetadataVO;
	}

	private String getValidMetadataGroup(String metadataGroupCode, SchemaTypeDisplayConfig config) {
		String validGroup = metadataGroupCode;
		boolean found = config.getMetadataGroup().keySet().contains(metadataGroupCode);

		if (!found) {
			validGroup = config.getMetadataGroup().keySet().iterator().next();
			for (String aGroup : config.getMetadataGroup().keySet()) {
				if (aGroup.startsWith("default")) {
					validGroup = aGroup;
				}
			}
		}

		return validGroup;
	}
}
