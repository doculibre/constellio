package com.constellio.app.services.importExport.settings;

import com.constellio.app.services.importExport.settings.model.ImportedMetadata;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ImportedTypeMetadata {

    private String type;
    private Set<String> schemata = new HashSet<>();
    private Map<String, Set<String>> visibleInFormIn = new HashMap<>();
    private Map<String, Set<String>> visibleInDisplayIn = new HashMap<>();
    private Map<String, Set<String>> visibleInResultIn = new HashMap<>();
    private Map<String, Set<String>> visibleInTablesIn = new HashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addMetadataIsVisibleInDisplay(String schema, ImportedMetadata importedMetadata) {
        if (!visibleInDisplayIn.containsKey(schema)) {
            visibleInDisplayIn.put(schema, new HashSet<String>());
        }
        visibleInDisplayIn.get(schema).add(importedMetadata.getCode());
    }

    public void addMetadataIsVisibleInForm(String schema, ImportedMetadata importedMetadata) {
        if (!visibleInFormIn.containsKey(schema)) {
            visibleInFormIn.put(schema, new HashSet<String>());
        }
        visibleInFormIn.get(schema).add(importedMetadata.getCode());
    }

    public void addMetadataIsVisibleInResult(String schema, ImportedMetadata importedMetadata) {
        if (!visibleInResultIn.containsKey(schema)) {
            visibleInResultIn.put(schema, new HashSet<String>());
        }
        visibleInResultIn.get(schema).add(importedMetadata.getCode());
    }

    public void addMetadataIsVisibleInTables(String schema, ImportedMetadata importedMetadata) {
        if (!visibleInTablesIn.containsKey(schema)) {
            visibleInTablesIn.put(schema, new HashSet<String>());
        }
        visibleInTablesIn.get(schema).add(importedMetadata.getCode());
    }

    public void addSchema(String schema) {
        if (StringUtils.isNotBlank(schema)) {
            schemata.add(schema);
        }
    }

    public Set<String> getSchemata() {
        return schemata;
    }

    public Set<String> getVisibleInDisplayFor(String schema) {
        if (visibleInDisplayIn.containsKey(schema)) {
            return visibleInDisplayIn.get(schema);
        }
        return new HashSet<>();
    }
}
