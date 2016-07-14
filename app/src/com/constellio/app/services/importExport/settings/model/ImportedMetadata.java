package com.constellio.app.services.importExport.settings.model;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.itextpdf.text.Meta;

import java.util.ArrayList;
import java.util.List;

public class ImportedMetadata {

    private String code;
    private String label;
    private MetadataValueType type;
    private boolean enabled = true;
    private boolean required;
    private String tabCode;
    private List<String> enabledIn = new ArrayList<>();
    private boolean multiValue;
    private List<String> behaviours = new ArrayList<>();
    private List<String> requiredIn = new ArrayList<>();

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

    public String getTabCode() {
        return tabCode;
    }

    public ImportedMetadata setTabCode(String tabCode) {
        this.tabCode = tabCode;
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
        this.behaviours = behaviours;
        return this;
    }

    public List<String> getBehaviours(){
        return behaviours;
    }

    public boolean isSearchable(){
        return behaviours.contains("searchable");
    }

    public boolean isAdvanceSearchable(){
        return behaviours.contains("advanced-search");
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
}
