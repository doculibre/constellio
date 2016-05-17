package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Predicate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class RecordWithCopyRetentionRuleParametersPresenter {
    RecordWithCopyRetentionRuleParametersFields fields;

    public RecordWithCopyRetentionRuleParametersPresenter(RecordWithCopyRetentionRuleParametersFields fields) {
        this.fields = fields;
    }

    void rmFieldsCreated(RecordVO recordVO) {
        CopyRetentionRuleDependencyField retentionRuleField = fields.getCopyRetentionRuleDependencyField();
        final BatchProcessRequest request = toRequest(fields.getSelectedRecords(), recordVO);
        if (retentionRuleField != null) {
            retentionRuleField.addValueChangeListener(new CopyRetentionRuleDependencyField.RetentionValueChangeListener() {
                @Override
                public void valueChanged(String newValue) {
                    updateFields(newValue, request);
                }
            });
        }
    }


    private static List<String> excludedMetadatas = asList(Schemas.IDENTIFIER.getLocalCode(), Schemas.CREATED_ON.getLocalCode(),
            Schemas.MODIFIED_ON.getLocalCode(), RMObject.FORM_CREATED_ON, RMObject.FORM_MODIFIED_ON);

    private BatchProcessRequest toRequest(List<String> selectedRecords, RecordVO recordVO) {
        ConstellioFactories constellioFactories = fields.getConstellioFactories();
        SessionContext sessionContext = fields.getSessionContext();
        User user = presenterService(constellioFactories.getModelLayerFactory()).getCurrentUser(sessionContext);
        MetadataSchema schema = coreSchemas(sessionContext.getCurrentCollection(),
                constellioFactories.getModelLayerFactory()).getTypes().getSchema(recordVO.getSchema().getCode());
        Map<String, Object> fieldsModifications = new HashMap<>();
        for (MetadataVO metadataVO : recordVO.getMetadatas()) {
            Metadata metadata = schema.get(metadataVO.getLocalCode());
            Object value = recordVO.get(metadataVO);
            if (metadata.getDataEntry().getType() == DataEntryType.MANUAL
                    && value != null
                    && !metadata.isSystemReserved()
                    && !excludedMetadatas.contains(metadata.getLocalCode())) {
                fieldsModifications.put(metadataVO.getCode(), value);
            }
        }

        return new BatchProcessRequest(selectedRecords, user, fieldsModifications);
    }

    private SchemasRecordsServices coreSchemas(String collection, ModelLayerFactory modelLayerFactory) {
        return new SchemasRecordsServices(collection, modelLayerFactory);
    }

    private PresenterService presenterService(ModelLayerFactory model) {
        return new PresenterService(model);
    }

    private void updateFields(String dependencyRecordId, BatchProcessRequest  request) {
        CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
        if (StringUtils.isNotBlank(dependencyRecordId)) {
            List<CopyRetentionRule> copyRetentionRules = getOptions(dependencyRecordId, request);
            filterByType(copyRetentionRules);
            copyRetentionRuleField.setOptions(copyRetentionRules);
            if (copyRetentionRules.size() == 1) {
                copyRetentionRuleField.setFieldValue(copyRetentionRules.get(0).getId());
            }
            if(copyRetentionRules.size() == 0){
                copyRetentionRuleField.setVisible(false);
            }else{
                copyRetentionRuleField.setVisible(true);
            }
        } else {
            copyRetentionRuleField.setOptions(new ArrayList<CopyRetentionRule>());
            copyRetentionRuleField.setVisible(false);
        }
    }

    private void filterByType(final List<CopyRetentionRule> copyRetentionRules) {
        if(StringUtils.isBlank(fields.getType())){
            return;
        }
        CollectionUtils.filter(copyRetentionRules, new Predicate() {
            @Override
            public boolean evaluate(Object arg) {
                if(arg instanceof CopyRetentionRule){
                    if(((CopyRetentionRule) arg).getTypeId().equals(fields.getType())){
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private List<CopyRetentionRule> getOptions(String dependencyRecordId, BatchProcessRequest recordVo) {
        ConstellioFactories constellioFactories = fields.getConstellioFactories();
        SessionContext sessionContext = fields.getSessionContext();
        String collection = sessionContext.getCurrentCollection();

        AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

        if(fields.getSchemaType().equals(Folder.SCHEMA_TYPE)){
            RetentionRule retentionRule = rm.getRetentionRule(dependencyRecordId);
            return retentionRule.getCopyRetentionRules();
        }else{
            Folder parentFolder = rm.getFolder(dependencyRecordId);
            //TODO Francis
            return asList(parentFolder.getMainCopyRule());
        }
    }
}
