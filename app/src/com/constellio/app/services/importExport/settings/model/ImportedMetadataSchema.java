package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedMetadataSchema {

    private String code;
    private List<ImportedMetadata> metadataList = new ArrayList<>();

    public ImportedMetadataSchema setCode(String code) {
        this.code = code;
        return this;
    }

    public String getCode(){
        return code;
    }

    public ImportedMetadataSchema addMetadata(ImportedMetadata importedMetadata) {
        metadataList.add(importedMetadata);
        return this;
    }

    public ImportedMetadataSchema setMetadata(List<ImportedMetadata> metadata){
        this.metadataList = metadata;
        return  this;
    }
}
