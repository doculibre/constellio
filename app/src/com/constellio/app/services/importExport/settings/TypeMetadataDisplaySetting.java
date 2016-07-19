package com.constellio.app.services.importExport.settings;

import java.util.*;

public class TypeMetadataDisplaySetting {

    private String schemaType;
    private List<String> schemata = new ArrayList<>();
    private String metadata;

    public String getSchemaType() {
        return schemaType;
    }

    public TypeMetadataDisplaySetting setSchemaType(String schemaType) {
        this.schemaType = schemaType;
        return this;
    }

    public List<String> getSchemata() {
        return schemata;
    }

    public String getMetadata() {
        return metadata;
    }

    public TypeMetadataDisplaySetting setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public void addSchema(String code) {
        if (!schemata.contains(code)) {
            schemata.add(code);
        }
    }
}
