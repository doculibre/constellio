package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.BatchProcessingSpecialCaseExtension;
import com.constellio.app.api.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import  com.constellio.model.entities.schemas.*;
import com.constellio.model.services.records.RecordServices;

import java.util.List;

public class RMBatchProcessingSpecialCaseExtension extends BatchProcessingSpecialCaseExtension {
    String collection;
    AppLayerFactory appLayerFactory;
    RecordServices recordServices;


    public RMBatchProcessingSpecialCaseExtension(String collection, AppLayerFactory appLayerFactory) {
        this.collection = collection;
        this.appLayerFactory = appLayerFactory;
        this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
    }

    @Override
    public void processSpecialCase(BatchProcessingSpecialCaseParams batchProcessingSpecialCaseParams) {
        RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
        Metadata metadata = batchProcessingSpecialCaseParams.getMetadata();
        Record record = batchProcessingSpecialCaseParams.getRecord();

        if(Folder.SCHEMA_TYPE.equals(metadata.getSchemaTypeCode()) && Folder.ADMINISTRATIVE_UNIT_ENTERED.equals(metadata.getLocalCode())) {
            recordServices.recalculate(record);

            Folder folder = rmSchemasRecordsServices.wrapFolder(record);
            RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(folder.getRetentionRule());
            List<CopyRetentionRule> copyRetentionRuleList = retentionRule.getCopyRetentionRules();

            if(folder.getCopyStatus() == CopyType.PRINCIPAL) {
                int numberOfPrincipal = 0;
                CopyRetentionRule lastCopyRetentionRule = null;
                for(CopyRetentionRule copyRetentionRule : copyRetentionRuleList) {
                    if(copyRetentionRule.getCopyType() == CopyType.PRINCIPAL){
                        lastCopyRetentionRule = copyRetentionRule;
                        numberOfPrincipal++;
                    }
                }

                if(numberOfPrincipal == 1) {
                    folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
                }
            } else {
                CopyRetentionRule lastCopyRetentionRule = null;
                for(CopyRetentionRule copyRetentionRule : copyRetentionRuleList) {
                    if(copyRetentionRule.getCopyType() == CopyType.SECONDARY){
                        lastCopyRetentionRule = copyRetentionRule;
                        break;
                    }
                }

                if(lastCopyRetentionRule != null) {
                    folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
                }
            }
        }
    }
}
