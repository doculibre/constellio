package com.constellio.app.services.importExport.settings.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        return "code: " + code + ", value: " + value;
    }
}