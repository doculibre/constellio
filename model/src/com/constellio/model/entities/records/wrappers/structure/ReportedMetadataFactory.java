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
import com.constellio.model.entities.schemas.StructureFactory;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReportedMetadataFactory implements StructureFactory {
    private static final String ELEMENTS_SEPARATOR = "~#~";

    @Override
    public ModifiableStructure build(String string) {
        String[] tokens = StringUtils.split(string, ELEMENTS_SEPARATOR);

        String metadataCode = readString(tokens[0]);
        int xPosition = readInt(tokens[1]);
        int yPosition = readInt(tokens[2]);
        return new ReportedMetadata(metadataCode, xPosition, yPosition);
    }

    private int readInt(String token) {
        return Integer.valueOf(token);
    }

    @Override
    public String toString(ModifiableStructure structure) {
        ReportedMetadata reportedMetadata = (ReportedMetadata) structure;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(reportedMetadata.getMetadataCode() + ELEMENTS_SEPARATOR);
        stringBuilder.append(reportedMetadata.getXPosition() + ELEMENTS_SEPARATOR);
        stringBuilder.append(reportedMetadata.getYPosition() + ELEMENTS_SEPARATOR);
        return stringBuilder.toString();
    }

    private String readString(String value) {
        if ("null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }


    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
