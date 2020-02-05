package com.constellio.app.ui.entities;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class FormMetadataVO implements Serializable {
	short id;
	String code;
	String localcode;
	MetadataValueType valueType;
	MetadataSchemaVO schema;
	String reference;
	Map<String, String> labels;
	boolean required;
	boolean multivalue;
	boolean searchable;
	boolean sortable;
	boolean advancedSearch;
	boolean facet;
	boolean highlight;
	boolean autocomplete;
	boolean availableInSummary;
	boolean enabled;
	String metadataGroup;
	MetadataInputType input;
	MetadataDisplayType displayType;
	Object defaultValue;
	String inputMask;
	String currentLanguageCode;
	boolean duplicable;
	Set<String> customAttributes;
	FormMetadataVO inheritance;
	boolean uniqueValue;
	boolean isMultiLingual;
	List<String> readAccessRoles;
	private Map<String, String> helpMessages;

	public FormMetadataVO(short id, String code, MetadataValueType type, boolean required, MetadataSchemaVO schemaVO,
						  String reference,
						  Map<String, String> labels, boolean searchable, boolean multivalue, boolean sortable,
						  boolean advancedSearch,
						  boolean facet,
						  MetadataInputType input, MetadataDisplayType displayType, boolean highlight,
						  boolean autocomplete, boolean availableInSummary, boolean enabled,
						  String metadataGroup,
						  Object defaultValue, String inputMask, boolean duplicable, boolean uniqueValue,
						  Set<String> customAttributes, SessionContext sessionContext, boolean isMultiLingual,
						  Map<String, String> helpMessages) {
		String localCodeParsed = SchemaUtils.underscoreSplitWithCache(code)[2];
		if (localCodeParsed.contains("USR")) {
			localCodeParsed = localCodeParsed.split("USR", 2)[1];
		}
		this.id = id;
		this.code = code;
		this.localcode = localCodeParsed;
		this.valueType = type;
		this.required = required;
		this.schema = schemaVO;
		this.labels = new HashMap<>(labels);
		this.multivalue = multivalue;
		this.searchable = searchable;
		this.sortable = sortable;
		this.advancedSearch = advancedSearch;
		this.facet = facet;
		this.reference = reference;
		this.input = input;
		this.displayType = displayType;
		this.highlight = highlight;
		this.autocomplete = autocomplete;
		this.availableInSummary = availableInSummary;
		this.enabled = enabled;
		this.metadataGroup = metadataGroup;
		this.defaultValue = defaultValue;
		this.inputMask = inputMask;
		this.currentLanguageCode = sessionContext.getCurrentLocale().getLanguage();
		this.duplicable = duplicable;
		this.customAttributes = new HashSet<>(customAttributes);
		this.inheritance = null;
		this.uniqueValue = uniqueValue;
		this.isMultiLingual = isMultiLingual;
		this.helpMessages = helpMessages;
	}

	public FormMetadataVO(SessionContext sessionContext) {
		super();
		this.id = 0;
		this.code = "";
		this.localcode = "";
		this.valueType = null;
		this.required = false;
		this.schema = null;
		this.labels = new HashMap<>();
		this.multivalue = false;
		this.searchable = false;
		this.sortable = false;
		this.advancedSearch = false;
		this.facet = false;
		this.reference = null;
		this.input = null;
		this.displayType = null;
		this.highlight = false;
		this.autocomplete = false;
		this.availableInSummary = false;
		this.enabled = true;
		this.metadataGroup = "";
		this.inputMask = "";
		this.currentLanguageCode = sessionContext.getCurrentLocale().getLanguage();
		this.duplicable = false;
		this.customAttributes = new HashSet<>();
		this.inheritance = null;
		this.isMultiLingual = false;
		this.helpMessages = new HashMap<>();
	}

	public short getId() {
		return id;
	}

	public FormMetadataVO setId(short id) {
		this.id = id;
		return this;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getReadAccessRoles() {
		return readAccessRoles;
	}

	public void setReadAccessRoles(List<String> readAccessRoles) {
		this.readAccessRoles = readAccessRoles;
	}

	public boolean isUniqueValue() {
		return uniqueValue;
	}

	public void setUniqueValue(boolean uniqueValue) {
		this.uniqueValue = uniqueValue;
	}

	public String getCode() {
		return code;
	}

	public String getLocalcode() {
		return localcode;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public boolean isSortable() {
		return sortable;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public boolean isFacet() {
		return facet;
	}


	public String getReference() {
		return reference;
	}

	public MetadataInputType getInput() {
		return input;
	}

	public MetadataDisplayType getDisplayType() {
		return displayType;
	}

	public MetadataValueType getValueType() {
		return valueType;
	}

	public MetadataSchemaVO getSchema() {
		return schema;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public boolean isAutocomplete() {
		return autocomplete;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public FormMetadataVO getInheritance() {
		return this.inheritance;
	}

	public boolean isInheritance() {
		return this.getInheritance() != null;
	}

	public void setInheritance(FormMetadataVO inheritance) {
		this.inheritance = inheritance;
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public String getMetadataGroup() {
		return this.metadataGroup;
	}

	public void setLocalcode(String localcode) {
		this.localcode = localcode;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setAdvancedSearch(boolean advancedSearch) {
		this.advancedSearch = advancedSearch;
	}

	public void setFacet(boolean facet) {
		this.facet = facet;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setInput(MetadataInputType input) {
		this.input = input;
	}

	public void setDisplayType(MetadataDisplayType displayType) {
		this.displayType = displayType;
	}

	public void setValueType(MetadataValueType type) {
		this.valueType = type;
	}

	public void setSchema(MetadataSchemaVO metadataSchemaVO) {
		this.schema = metadataSchemaVO;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = new HashMap<>(labels);
	}

	public String getLabel(String currentLanguageCode) {
		return labels.get(currentLanguageCode);
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public void setAutocomplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
	}

	public boolean isAvailableInSummary() {
		return availableInSummary;
	}

	public void setAvailableInSummary(boolean availableInSummary) {
		this.availableInSummary = availableInSummary;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setMetadataGroup(String metadataGroup) {
		this.metadataGroup = metadataGroup;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getInputMask() {
		return inputMask;
	}

	public void setInputMask(String inputMask) {
		this.inputMask = inputMask;
	}

	public boolean isDuplicable() {
		return duplicable;
	}

	public void setDuplicable(boolean duplicable) {
		this.duplicable = duplicable;
	}

	public Set<String> getCustomAttributes() {
		return customAttributes;
	}

	public FormMetadataVO setCustomAttributes(Set<String> customAttributes) {
		this.customAttributes = customAttributes;
		return this;
	}

	public boolean isMultiLingual() {
		return isMultiLingual;
	}

	public void setMultiLingual(boolean multiLangual) {
		isMultiLingual = multiLangual;
	}

	public void addCustomAttribute(String attribute) {
		customAttributes.add(attribute);
	}

	public void removeCustomAttribute(String attribute) {
		customAttributes.remove(attribute);
	}

	public String getHelpMessage(String currentLanguageCode) {
		return helpMessages.get(currentLanguageCode);
	}

	public Map<String, String> getHelpMessages() {
		return helpMessages;
	}

	public void setHelpMessage(String currentLanguageCode, String helpMessage) {
		this.helpMessages.put(currentLanguageCode, helpMessage);
	}

	public void setHelpMessages(Map<String, String> helpMessages) {
		this.helpMessages = new HashMap<>(helpMessages);
	}


	/*public boolean getDuplicable() {
		return duplicable;
	}

    public void setDuplicable(boolean duplicable) {
        this.duplicable = duplicable;
    }*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FormMetadataVO other = (FormMetadataVO) obj;
		if (code == null) {
			if (other.code != null) {
				return false;
			}
		} else if (!code.equals(other.code)) {
			return false;
		}
		if (schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!schema.equals(other.schema)) {
			return false;
		}
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel(currentLanguageCode);
		} catch (RuntimeException e) {
			toString = super.toString();
		}
		return toString;
	}
}
