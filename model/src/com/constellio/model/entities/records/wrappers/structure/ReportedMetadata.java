package com.constellio.model.entities.records.wrappers.structure;


import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang.StringUtils;

public class ReportedMetadata implements ModifiableStructure {
    private String metadataCode;
    private int xPosition;
    private int yPosition;
    private boolean dirty = false;

    public ReportedMetadata(String metadataCode, int xPosition){
        this(metadataCode, xPosition, 0);
    }

    public ReportedMetadata(String metadataCode, int xPosition, int yPosition) {
        this.metadataCode = metadataCode;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public ReportedMetadata setMetadataCode(String metadataCode) {
        this.dirty = true;
        this.metadataCode = metadataCode;
        return this;
    }

    public ReportedMetadata setXPosition(int xPosition) {
        this.dirty = true;
        this.xPosition = xPosition;
        return this;
    }

    public ReportedMetadata setYPosition(int yPosition) {
        this.dirty = true;
        this.yPosition = yPosition;
        return this;
    }

    public String getMetadataCode() {
        return metadataCode;
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public String getMetadataLocaleCode() {
        return StringUtils.substringAfterLast(getMetadataCode(), "_") ;
    }
}
