package com.constellio.app.services.importExport.settings.model;

public class ImportedTab {

    private String code;
    private String value;

    public String getCode() {
        return code;
    }

    public ImportedTab setCode(String code) {
        this.code = code;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ImportedTab setValue(String value) {
        this.value = value;
        return this;
    }
}