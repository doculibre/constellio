package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BatchProcessingPresenterService implements BatchProcessingPresenter {
    private final ModelLayerFactory modelLayerFactory;
    private final String collection;

    public BatchProcessingPresenterService(ModelLayerFactory modelLayerFactory, String collection) {
        this.modelLayerFactory = modelLayerFactory;
        this.collection = collection;
    }

    @Override
    public String getOriginSchema(String schemaType, List<String> selectedRecordIds) {
        if(selectedRecordIds == null || selectedRecordIds.isEmpty()){
            throw new ImpossibleRuntimeException("Batch processing should be done on at least one record");
        }
        RecordServices recordServices = modelLayerFactory.newRecordServices();
        String firstRecordSchema = getRecordSchemaCode(recordServices, selectedRecordIds.get(0));
        Boolean moreThanOneSchema = false;
        for(int i = 0; i < selectedRecordIds.size(); i++){
            String currentRecordSchema = getRecordSchemaCode(recordServices, selectedRecordIds.get(i));
            if(!currentRecordSchema.equals(firstRecordSchema)) {
                moreThanOneSchema = true;
                break;
            }
        }
        if(moreThanOneSchema) {
            return schemaType + "_" + "default";
        }else{
            return firstRecordSchema;
        }
    }

    private String getRecordSchemaCode(RecordServices recordServices, String recordId) {
        return recordServices.getDocumentById(recordId).getSchemaCode();
    }

    private String getSchemataType(Set<String> recordsSchemata) {
        String firstType = getSchemaType(recordsSchemata.iterator().next());
        ensureAllSchemataOfSameType(recordsSchemata, firstType);
        return firstType;
    }

    private String getSchemaType(String schemaCode) {
        return StringUtils.substringBefore(schemaCode, "_");
    }

    private void ensureAllSchemataOfSameType(Set<String> recordsSchemata, String firstType) {
        for(String schemaCode : recordsSchemata){
            String currentSchemaType = getSchemaType(schemaCode);
            if(!currentSchemaType.equals(firstType)) {
                throw new ImpossibleRuntimeException("Batch processing should be done on the same schema type :" +
                        StringUtils.join(recordsSchemata, ";"));
            }
        }
    }

    @Override
    public List<String> getDestinationSchemata(String schemaType) {
        List<String> schemataCodes = new ArrayList<>();
        List<MetadataSchema> schemata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
                .getSchemaType(schemaType).getAllSchemas();
        for(MetadataSchema currentSchema : schemata){
            schemataCodes.add(currentSchema.getCode());
        }
        return schemataCodes;
    }

    @Override
    public RecordVO newRecordVO(String schemaCode, SessionContext sessionContext) {
        MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchema(schemaCode);
        Record tmpRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
        return new RecordToVOBuilder().build(tmpRecord,  RecordVO.VIEW_MODE.FORM, sessionContext);
    }

    @Override
    public void simulateButtonClicked(RecordVO viewObject) {

    }

    @Override
    public void saveButtonClicked(RecordVO viewObject) {

    }
}
