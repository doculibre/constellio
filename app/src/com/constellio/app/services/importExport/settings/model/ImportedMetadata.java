package com.constellio.app.services.importExport.settings.model;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

public class ImportedMetadata {

	private String code;
	private Map<Language, String> labels = new HashMap<>();
	private String type;
	private String referencedType;
	private Boolean enabled;// = true;
	private List<String> enabledIn = new ArrayList<>();
	private Boolean required;// = true;
	private List<String> requiredIn = new ArrayList<>();
	private Boolean visibleInForm;// = true;
	private List<String> visibleInFormIn = new ArrayList<>();
	private Boolean visibleInDisplay;// = true;
	private List<String> visibleInDisplayIn = new ArrayList<>();
	private Boolean visibleInSearchResult;// = false;
	private List<String> visibleInResultIn = new ArrayList<>();
	private Boolean visibleInTables;// = false;
	private List<String> visibleInTablesIn = new ArrayList<>();
	private String sortingType;

	private String tab;
	private Boolean multiValue;

	private String inputMask;
	private Boolean duplicable;
	private Boolean encrypted;
	private Boolean essential;
	private Boolean essentialInSummary;
	private Boolean multiLingual;
	private Boolean unique;
	private Boolean recordAutoComplete;
	private Boolean searchable;
	private Boolean sortable;
	private Boolean unmodifiable;
	private Boolean advanceSearchable;
	private Boolean relationshipProvidingSecurity;
	private List<String> requiredReadRoles;

	private ImportedDataEntry dataEntry;

	private ImportedMetadataPopulateConfigs populateConfigs = null;

	public String getCode() {
		return code;
	}

	public ImportedMetadata setCode(String code) {
		this.code = code;
		return this;
	}

	public List<String> getRequiredReadRoles() {
		return requiredReadRoles;
	}

	public ImportedMetadata setRequiredReadRoles(List<String> requiredReadRoles) {
		this.requiredReadRoles = requiredReadRoles;
		return this;
	}

	public String getLabel() {
		return labels.get(Language.French);
	}

	public ImportedMetadata setLabel(String label) {
		labels.put(Language.French, label);
		return this;
	}

	public ImportedMetadata setLabel(Locale locale, String label) {
		labels.put(Language.withLocale(locale), label);
		return this;
	}

	public ImportedMetadata setLabels(Map<Language, String> labels) {
		this.labels = labels;
		return this;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getType() {
		return type;
	}

	public ImportedMetadata setType(MetadataValueType type) {
		setType(type.name());
		return this;
	}

	public ImportedMetadata setType(String type) {
		this.type = type;
		return this;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public ImportedMetadata setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Boolean getRequired() {
		return required;
	}

	public ImportedMetadata setRequired(Boolean required) {
		this.required = required;
		return this;
	}

	public String getTab() {
		return tab;
	}

	public ImportedMetadata setTab(String tab) {
		this.tab = tab;
		return this;
	}

	public List<String> getEnabledIn() {
		return enabledIn;
	}

	public ImportedMetadata setEnabledIn(List<String> enabledIn) {
		this.enabledIn = enabledIn;
		return this;
	}

	public Boolean getMultiValue() {
		return multiValue;
	}

	public ImportedMetadata setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
		return this;
	}

	public Boolean getMultiLingual() {
		return multiLingual;
	}

	public ImportedMetadata setMultiLingual(Boolean multiLingual) {
		this.multiLingual = multiLingual;
		return this;
	}

	public Boolean getDuplicable() {
		return duplicable;
	}

	public ImportedMetadata setDuplicable(Boolean duplicable) {
		this.duplicable = duplicable;
		return this;
	}

	public Boolean getEncrypted() {
		return encrypted;
	}

	public ImportedMetadata setEncrypted(Boolean encrypted) {
		this.encrypted = encrypted;
		return this;
	}

	public Boolean getEssential() {
		return essential;
	}

	public ImportedMetadata setEssential(Boolean essential) {
		this.essential = essential;
		return this;
	}

	public Boolean getEssentialInSummary() {
		return essentialInSummary;
	}

	public ImportedMetadata setEssentialInSummary(Boolean essentialInSummary) {
		this.essentialInSummary = essentialInSummary;
		return this;
	}

	public Boolean getRelationshipProvidingSecurity() {
		return relationshipProvidingSecurity;
	}

	public ImportedMetadata setRelationshipProvidingSecurity(Boolean relationshipProvidingSecurity) {
		this.relationshipProvidingSecurity = relationshipProvidingSecurity;
		return this;
	}

	public String getInputMask() {
		return inputMask;
	}

	public ImportedMetadata setInputMask(String inputMask) {
		this.inputMask = inputMask;
		return this;
	}

	public List<String> getRequiredIn() {
		return requiredIn;
	}

	public ImportedMetadata setRequiredIn(List<String> requiredIn) {
		this.requiredIn = requiredIn;
		return this;
	}

	public Boolean getVisibleInForm() {
		return visibleInForm;
	}

	public ImportedMetadata setVisibleInForm(Boolean visibleInForm) {
		this.visibleInForm = visibleInForm;
		return this;
	}

	public Boolean getVisibleInDisplay() {
		return visibleInDisplay;
	}

	public ImportedMetadata setVisibleInDisplay(Boolean visibleInDisplay) {
		this.visibleInDisplay = visibleInDisplay;
		return this;
	}

	public Boolean getVisibleInSearchResult() {
		return visibleInSearchResult;
	}

	public ImportedMetadata setVisibleInSearchResult(Boolean visibleInSearchResult) {
		this.visibleInSearchResult = visibleInSearchResult;
		return this;
	}

	public Boolean getVisibleInTables() {
		return visibleInTables;
	}

	public ImportedMetadata setVisibleInTables(Boolean visibleInTables) {
		this.visibleInTables = visibleInTables;
		return this;
	}

	public List<String> getVisibleInFormIn() {
		return visibleInFormIn;
	}

	public ImportedMetadata setVisibleInFormIn(List<String> visibleInFormIn) {
		this.visibleInFormIn = visibleInFormIn;
		return this;
	}

	public List<String> getVisibleInDisplayIn() {
		return visibleInDisplayIn;
	}

	public ImportedMetadata setVisibleInDisplayIn(List<String> visibleInDisplayIn) {
		this.visibleInDisplayIn = visibleInDisplayIn;
		return this;
	}

	public List<String> getVisibleInResultIn() {
		return visibleInResultIn;
	}

	public ImportedMetadata setVisibleInResultIn(List<String> visibleInResultIn) {
		this.visibleInResultIn = visibleInResultIn;
		return this;
	}

	public List<String> getVisibleInTablesIn() {
		return visibleInTablesIn;
	}

	public ImportedMetadata setVisibleInTablesIn(List<String> visibleInTablesIn) {
		this.visibleInTablesIn = visibleInTablesIn;
		return this;
	}

	public Boolean getRecordAutoComplete() {
		return recordAutoComplete;
	}

	public ImportedMetadata setRecordAutoComplete(Boolean recordAutoComplete) {
		this.recordAutoComplete = recordAutoComplete;
		return this;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public ImportedMetadata setSearchable(Boolean searchable) {
		this.searchable = searchable;
		return this;
	}

	public Boolean getSortable() {
		return sortable;
	}

	public ImportedMetadata setSortable(Boolean sortable) {
		this.sortable = sortable;
		return this;
	}

	public Boolean getUnmodifiable() {
		return unmodifiable;
	}

	public ImportedMetadata setUnmodifiable(Boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
		return this;
	}

	public Boolean getUnique() {
		return unique;
	}

	public ImportedMetadata setUnique(Boolean unique) {
		this.unique = unique;
		return this;
	}

	public String getSortingType() {
		return sortingType;
	}

	public ImportedMetadata setSortingType(String sortingType) {
		this.sortingType = sortingType;
		return this;
	}

	public Boolean getAdvanceSearchable() {
		return advanceSearchable;
	}

	public ImportedMetadata setAdvanceSearchable(Boolean advanceSearchable) {
		this.advanceSearchable = advanceSearchable;
		return this;
	}

	public String getReferencedType() {
		return referencedType;
	}

	public ImportedMetadata setReferencedType(String referencedType) {
		this.referencedType = referencedType;
		return this;
	}

	public ImportedMetadataPopulateConfigs getPopulateConfigs() {
		return populateConfigs;
	}

	public ImportedMetadataPopulateConfigs newPopulateConfigs() {
		populateConfigs = new ImportedMetadataPopulateConfigs();
		return populateConfigs;
	}

	public ImportedMetadata setPopulateConfigs(
			ImportedMetadataPopulateConfigs populateConfigs) {
		this.populateConfigs = populateConfigs;
		return this;
	}

	public List<String> getVisibleInListInSchemas(ListType listType) {
		switch (listType) {

		case DISPLAY:
			return getVisibleInDisplayIn();

		case FORM:
			return getVisibleInFormIn();

		case SEARCH:
			return getVisibleInResultIn();

		case TABLES:
			return getVisibleInTablesIn();

		default:
			throw new RuntimeException("Unsupported list " + listType);

		}

	}

	public Boolean getVisible(ListType listType) {
		switch (listType) {

		case DISPLAY:
			return getVisibleInDisplay();

		case FORM:
			return getVisibleInForm();

		case SEARCH:
			return getVisibleInSearchResult();

		case TABLES:
			return getVisibleInTables();

		default:
			throw new RuntimeException("Unsupported list " + listType);

		}

	}

	public ImportedMetadata setDataEntry(ImportedDataEntry dataEntry) {
		this.dataEntry = dataEntry;
		return this;
	}

	public ImportedDataEntry getDataEntry() {
		return dataEntry;
	}

	public enum ListType {
		DISPLAY, FORM, SEARCH, TABLES
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}