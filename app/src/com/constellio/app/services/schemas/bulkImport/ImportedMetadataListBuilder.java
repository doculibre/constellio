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

import com.constellio.model.entities.records.RecordRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportedMetadataListBuilder {
    private String schemaTypeCode;
    private String schemaCode;
    private final String collection;
    private List<ImportedMetadata> formMetadataList;

    ImportedMetadataListBuilder(String schemaTypeCode, String schemaCode, List<Map<String, String>> metadataList, String collection){
        this.schemaTypeCode = schemaTypeCode;
        if(schemaCode.isEmpty()){
            this.schemaCode = "default";
        }else{
            this.schemaCode = schemaCode;
        }
        this.collection = collection;
        init(metadataList);
    }

    private void init(List<Map<String, String>> metadataList){
        formMetadataList = new ArrayList<>();
        if(metadataList == null){
            return;
        }
        for(int i = 0; i< metadataList.size(); i++){
            Map<String, String> metadataElements = metadataList.get(i);
            try {
                metadataElements.put("schemaCode", schemaCode);
                metadataElements.put("schemaTypeCode", schemaTypeCode);
                ImportedMetadata currentFormMetadata = new ImportedMetadataVOBuilder().build(metadataElements);
                formMetadataList.add(currentFormMetadata);
            } catch (FormMetadataVOBuilderException e) {
                e.printStackTrace();
                throw new RecordRuntimeException.InvalidMetadata("code number " + i + " in schema " + schemaTypeCode);
            }
        }
    }

    public List<ImportedMetadata> getMetadataList() {
        return formMetadataList;
    }
}
