package com.constellio.app.services.importExport.settings.model;

import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public class ImportedMetadata {

    private String code;
    private String label;
    private MetadataValueType type;
    private boolean enabled = true;
    private List<String> enabledIn = new ArrayList<>();
    private boolean required = true;
    private List<String> requiredIn = new ArrayList<>();
    private boolean visibleInForm = true;
    private List<String> visibleInFormIn = new ArrayList<>();
    private boolean visibleInDisplay = true;
    private List<String> visibleInDisplayIn = new ArrayList<>();
    private boolean visibleInSearchResult = false;
    private List<String> visibleInResultIn = new ArrayList<>();
    private boolean visibleInTables = false;
    private List<String> visibleInTablesIn = new ArrayList<>();

    private String tab;
    private boolean multiValue;
    private List<String> behaviours = new ArrayList<>();
    private String inputMask;
    private boolean searchable;
    private boolean advanceSearchable;
    private boolean unmodifiable;
    private boolean sortable;
    private boolean recordAutocomplete;
    private boolean essential;
    private boolean essentialInSummary;
    private boolean multiLingual;
    private boolean duplicable;
    private boolean encrypted;
    private boolean unique;

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

    public String getLabel(){
        return label;
    }

    public ImportedMetadata setType(MetadataValueType type) {
        this.type = type;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ImportedMetadata setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public ImportedMetadata setRequired(boolean required) {
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

    public List<String> getEnabledIn(){
        return  enabledIn;
    }

    public ImportedMetadata setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
        return this;
    }

    public boolean isMultiValue(){
        return  multiValue;
    }

    public ImportedMetadata setBehaviours(List<String> behaviours) {
        this.behaviours.clear();
        if (behaviours != null) {
            this.behaviours = behaviours;
        }
        return this;
    }

    public List<String> getBehaviours(){
        return behaviours;
    }

    public boolean isSearchable(){
        return searchable;
    }

    public ImportedMetadata setSearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public ImportedMetadata setAdvanceSearchable(boolean advanceSearchable) {
        this.advanceSearchable = advanceSearchable;
        return this;
    }

    public boolean isAdvanceSearchable(){
        return advanceSearchable;
    }

    public ImportedMetadata setRequiredIn(List<String> requiredIn) {
        this.requiredIn = requiredIn;
        return this;
    }

    public MetadataValueType getType() {
        return type;
    }

    public List<String> getRequiredIn() {
        return requiredIn;
    }

    public boolean isVisibleInForm() {
        return visibleInForm;
    }

    public ImportedMetadata setVisibleInForm(boolean visibleInForm) {
        this.visibleInForm = visibleInForm;
        return this;
    }

    public boolean isVisibleInDisplay() {
        return visibleInDisplay;
    }

    public ImportedMetadata setVisibleInDisplay(boolean visibleInDisplay) {
        this.visibleInDisplay = visibleInDisplay;
        return this;
    }

    public boolean isVisibleInSearchResult() {
        return visibleInSearchResult;
    }

    public ImportedMetadata setVisibleInSearchResult(boolean visibleInSearchResult) {
        this.visibleInSearchResult = visibleInSearchResult;
        return this;
    }

    public boolean isVisibleInTables() {
        return visibleInTables;
    }

    public ImportedMetadata setVisibleInTables(boolean visibleInTables) {
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

    public ImportedMetadata setInputMask(String inputMask) {
        this.inputMask = inputMask;
        return this;
    }

    public String getInputMask() {
        return inputMask;
    }

    public boolean isUnmodifiable() {
        return unmodifiable;
    }

    public ImportedMetadata setUnmodifiable(boolean unmodifiable) {
        this.unmodifiable = unmodifiable;
        return this;
    }

    public boolean isSortable() {
        return sortable;
    }

    public ImportedMetadata setSortable(boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    public boolean isRecordAutocomplete() {
        return recordAutocomplete;
    }

    public ImportedMetadata setRecordAutocomplete(boolean recordAutocomplete) {
        this.recordAutocomplete = recordAutocomplete;
        return this;
    }

    public boolean isEssential() {
        return essential;
    }

    public ImportedMetadata setEssential(boolean essential) {
        this.essential = essential;
        return this;
    }

    public boolean isEssentialInSummary() {
        return essentialInSummary;
    }

    public ImportedMetadata setEssentialInSummary(boolean essentialInSummary) {
        this.essentialInSummary = essentialInSummary;
        return this;
    }

    public boolean isMultiLingual() {
        return multiLingual;
    }

    public ImportedMetadata setMultiLingual(boolean multiLingual) {
        this.multiLingual = multiLingual;
        return this;
    }

    public boolean isDuplicable() {
        return duplicable;
    }

    public ImportedMetadata setDuplicable(boolean duplicable) {
        this.duplicable = duplicable;
        return this;
    }

    public ImportedMetadata setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public ImportedMetadata setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }
}
