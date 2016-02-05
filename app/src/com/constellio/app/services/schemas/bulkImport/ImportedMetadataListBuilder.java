package com.constellio.app.services.schemas.bulkImport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.RecordRuntimeException;

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
