package com.constellio.app.services.importExport.settings;

import java.util.*;

public class TypeMetadataDisplaySetting {

    private String schemaType;
    private Map<String, List<MetadataDisplaySetting>> schema = new HashMap<>();
    private String metadata;

    public String getSchemaType() {
        return schemaType;
    }

    public TypeMetadataDisplaySetting setSchemaType(String schemaType) {
        this.schemaType = schemaType;
        return this;
    }

    public Set<String> getSchema() {
        return schema.keySet();
    }

    public List<MetadataDisplaySetting> getMetadata(String schemaCode) {
        return schema.get(schemaCode);
    }

    public TypeMetadataDisplaySetting setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public void addSchema(String code) {
        if (!schema.containsKey(code)) {
            schema.put(code, new ArrayList<MetadataDisplaySetting>());
        }
    }
}
