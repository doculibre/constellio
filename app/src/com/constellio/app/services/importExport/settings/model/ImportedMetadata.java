package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataValueType;

public class ImportedMetadata {

	private String code;
	private String label;
	private MetadataValueType type;
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

	private String tab;
	private Boolean multiValue;

	private String behaviours;
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

	public String getCode() {
		return code;
	}

	public ImportedMetadata setCode(String code) {
		this.code = code;
		return this;
	}

	public ImportedMetadata setLabel(String label) {
		this.label = label;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public ImportedMetadata setType(MetadataValueType type) {
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

	public ImportedMetadata setEnabledIn(List<String> enabledIn) {
		this.enabledIn = enabledIn;
		return this;
	}

	public List<String> getEnabledIn() {
		return enabledIn;
	}

	public ImportedMetadata setEssential(Boolean essential) {
		this.essential = essential;
		return this;
	}

	public ImportedMetadata setEssentialInSummary(Boolean essentialInSummary) {
		this.essentialInSummary = essentialInSummary;
		return null;
	}

	public ImportedMetadata setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
		return this;
	}

	public Boolean getMultiValue() {
		return multiValue;
	}

	public ImportedMetadata setBehaviours(String behaviours) {
		this.behaviours = behaviours;
		return this;
	}

	public String getBehaviours() {
		return behaviours;
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

	public Boolean getEssentialInSummary() {
		return essentialInSummary;
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

	public MetadataValueType getType() {
		return type;
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

	public Boolean getMultiLingual() {
		return multiLingual;
	}

	public Boolean getRecordAutoComplete() {
		return recordAutoComplete;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public Boolean getSortable() {
		return sortable;
	}

	public Boolean getUnmodifiable() {
		return unmodifiable;
	}

	public Boolean getUnique() {
		return unique;
	}

	public enum ListType {
		DISPLAY, FORM, SEARCH, TABLES
	}
}
