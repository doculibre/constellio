package com.constellio.app.ui.entities;

import java.io.Serializable;

public class ReportedMetadataVO implements Serializable{
    private String metadataCode;
    private int xPosition;
    private int yPosition;

    public String getMetadataCode() {
        return metadataCode;
    }

    public ReportedMetadataVO setMetadataCode(String metadataCode) {
        this.metadataCode = metadataCode;
        return this;
    }

    public int getXPosition() {
        return xPosition;
    }

    public ReportedMetadataVO setXPosition(int xPosition) {
        this.xPosition = xPosition;
        return this;
    }

    public int getYPosition() {
        return yPosition;
    }

    public ReportedMetadataVO setYPosition(int yPosition) {
        this.yPosition = yPosition;
        return this;
    }
}
