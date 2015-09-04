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
