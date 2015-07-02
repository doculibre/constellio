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
package com.constellio.app.services.schemas.bulkImport;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ImportedMetadataVOBuilder {
    public ImportedMetadata build(Map<String, String> metadataElements) throws FormMetadataVOBuilderException{
        String localCode = getStringValue("localCode", metadataElements, null);
        if(StringUtils.isBlank(localCode)){
            throw new FormMetadataVOBuilderException();
        }
        String schemaTypeCode = getStringValue("schemaTypeCode", metadataElements, null);
        if(StringUtils.isBlank(schemaTypeCode)){
            throw new FormMetadataVOBuilderException();
        }
        String schemaCode = getStringValue("schemaCode", metadataElements, null);
        if(StringUtils.isBlank(schemaCode)){
            throw new FormMetadataVOBuilderException();
        }
        String typeString = getStringValue("type", metadataElements, "string");
        MetadataValueType type = MetadataValueType.valueOf(typeString.toUpperCase());
        String reference = getStringValue("reference", metadataElements, "");;
        String label = getStringValue("label", metadataElements, localCode);
        boolean required = getBooleanValue("required", metadataElements, false);
        boolean multivalue = getBooleanValue("multivalue", metadataElements, false);
        boolean searchable = getBooleanValue("searchable", metadataElements, false);
        boolean sortable = getBooleanValue("sortable", metadataElements, false);
        boolean advancedSearch = getBooleanValue("advancedSearch", metadataElements, false);
        boolean facet = getBooleanValue("facet", metadataElements, false);
        boolean highlight = getBooleanValue("highlight", metadataElements, false);
        boolean autocomplete = getBooleanValue("autocomplete", metadataElements, false);
        boolean enabled = getBooleanValue("enabled", metadataElements, true);
        String metadataGroup = getStringValue("metadataGroup", metadataElements, "default");
        if(metadataGroup == null){
            throw new FormMetadataVOBuilderException();
        }
        MetadataInputType input = getMetadataInputType(metadataElements);
        if(input == null){
            List<MetadataInputType> list = MetadataInputType.getAvailableMetadataInputTypesFor(type, multivalue);
            if(list.isEmpty()){
                input = MetadataInputType.FIELD;
            }else{
                input = list.get(0);
            }
        }
        return new ImportedMetadata(schemaTypeCode, schemaCode, localCode, type, required, reference,
                label, searchable, multivalue, sortable, advancedSearch, facet,
        input, highlight, autocomplete, enabled, metadataGroup);
    }

    private MetadataInputType getMetadataInputType(Map<String, String> metadataElements) {
        String inputString = getStringValue("input", metadataElements, null);
        if (inputString != null){
            return MetadataInputType.valueOf(inputString.toUpperCase());
        }else{
            return null;
        }
    }

    private String getStringValue(String key, Map<String, String> metadataElements, String defaultValue) {
        String value = metadataElements.get(key);
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value.trim();
    }

    private boolean getBooleanValue(String key, Map<String, String> metadataElements, boolean defaultValue) {
        String value = metadataElements.get(key);
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        value = value.trim();
        if(value.equalsIgnoreCase("true")){
            return true;
        }else if(value.equalsIgnoreCase("false")){
            return false;
        }
        return defaultValue;
    }
}
