/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
