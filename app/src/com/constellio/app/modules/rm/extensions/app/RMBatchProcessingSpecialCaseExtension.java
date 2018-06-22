package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.extensions.behaviors.BatchProcessingSpecialCaseExtension;
import com.constellio.model.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.model.entities.records.Record;
import  com.constellio.model.entities.schemas.*;
import com.constellio.model.services.records.RecordServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;

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
    public Map<String, Object> processSpecialCase(BatchProcessingSpecialCaseParams batchProcessingSpecialCaseParams) {
        RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
        Metadata metadata = batchProcessingSpecialCaseParams.getMetadata();
        Record record = batchProcessingSpecialCaseParams.getRecord();
        Map<String, Object> modifedMetadata = new HashMap<>();

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
                    if(!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), lastCopyRetentionRule.getId())) {
                        modifedMetadata.put( folder.getSchemaCode() + "_" + MAIN_COPY_RULE_ID_ENTERED, lastCopyRetentionRule.getId());
                        folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
                    }
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
                    if(!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), lastCopyRetentionRule.getId())) {
                        modifedMetadata.put(folder.getSchemaCode() + "_" + MAIN_COPY_RULE_ID_ENTERED, lastCopyRetentionRule.getId());
                        folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
                    }
                }
            }
        } else if (Folder.SCHEMA_TYPE.equals(metadata.getSchemaTypeCode()) && MAIN_COPY_RULE_ID_ENTERED.equals(metadata.getLocalCode())) {
            recordServices.recalculate(record);
            Folder folder = rmSchemasRecordsServices.wrapFolder(record);

            String sRetentionRule = folder.getRetentionRule();
            if(sRetentionRule != null) {
                RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(sRetentionRule);

                if (folder.getCopyStatus() == CopyType.SECONDARY) {
                    if(!record.get(metadata).equals(retentionRule.getId())) {
                        if(!LangUtils.areNullableEqual(record.get(metadata), retentionRule.getSecondaryCopy().getId())) {
                            modifedMetadata.put(metadata.getCode(), retentionRule.getSecondaryCopy().getId());
                            record.set(metadata, retentionRule.getSecondaryCopy().getId());
                        }
                    }
                }
            }
        } else if (Folder.SCHEMA_TYPE.equals(metadata.getSchemaTypeCode()) && Folder.RETENTION_RULE.equals(metadata.getLocalCode())) {
            recordServices.recalculate(record);
            Folder folder = rmSchemasRecordsServices.wrapFolder(record);

            String sRetentionRule = folder.getRetentionRule();
            if(sRetentionRule != null) {
                RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(sRetentionRule);
                if(isValidDelai(retentionRule.getCopyRetentionRules(), folder.getMainCopyRuleIdEntered())) {
                    if (folder.getCopyStatus() == CopyType.SECONDARY) {
                        if(!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), retentionRule.getSecondaryCopy().getId())) {
                            modifedMetadata.put(retentionRule.getSecondaryCopy().getId(), record.getSchemaCode() + "_" + Folder.MAIN_COPY_RULE_ID_ENTERED);
                            folder.setMainCopyRuleEntered(retentionRule.getSecondaryCopy().getId());
                        }
                    }
                }
            }
        }

        return modifedMetadata;
    }

    private boolean isValidDelai(List<CopyRetentionRule> copyRetentionRules, String id) {
        for(CopyRetentionRule copyRetentionRule : copyRetentionRules) {
            if(copyRetentionRule.equals(id)) {
                return true;
            }
        }

        return false;
    }
}
