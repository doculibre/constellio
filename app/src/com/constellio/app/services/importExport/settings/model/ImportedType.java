package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedType {

    private String code;
    private List<ImportedTab> tabs = new ArrayList<>();
    private ImportedMetadataSchema defaultSchema;
    private List<ImportedMetadataSchema> customSchemas = new ArrayList<>();
    private String label;

    public ImportedType setCode(String code) {
        this.code = code;
        return this;
    }

    public String getCode() {
        return code;
    }

    public ImportedType setTabs(List<ImportedTab> importedTabs) {
        this.tabs = importedTabs;
        return this;
    }

    public List<ImportedTab> getTabs(){
        return tabs;
    }

    public ImportedType setDefaultSchema(ImportedMetadataSchema defaultSchema) {
        this.defaultSchema = defaultSchema;
        return this;
    }

    public ImportedType addSchema(ImportedMetadataSchema customSchema) {
        this.customSchemas.add(customSchema);
        return this;
    }

    public List<ImportedMetadataSchema> getCustomSchemas() {
        return customSchemas;
    }

    public ImportedMetadataSchema getDefaultSchema() {
        return defaultSchema;
    }

    public String getLabel() {
        return label;
    }

    public ImportedType setLabel(String label) {
        this.label = label;
        return this;
    }
}
