package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.RetentionRuleField;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RecordWithCopyRetentionRuleParametersPresenter {
    RecordWithCopyRetentionRuleParametersFields fields;

    public RecordWithCopyRetentionRuleParametersPresenter(RecordWithCopyRetentionRuleParametersFields fields) {
        this.fields = fields;
    }

    void rmFieldsCreated() {
        RetentionRuleField retentionRuleField = fields.getRetentionField();
        if (retentionRuleField != null) {
            retentionRuleField.addValueChangeListener(new RetentionRuleField.RetentionValueChangeListener() {
                @Override
                public void valueChanged(String newValue) {
                    updateFields(newValue);
                }
            });
        }
    }

    private void updateFields(String retentionRuleId) {
        CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();

        ConstellioFactories constellioFactories = fields.getConstellioFactories();
        SessionContext sessionContext = fields.getSessionContext();
        String collection = sessionContext.getCurrentCollection();

        AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

        if (StringUtils.isNotBlank(retentionRuleId)) {
            RetentionRule retentionRule = rm.getRetentionRule(retentionRuleId);
            List<CopyRetentionRule> copyRetentionRules = retentionRule.getCopyRetentionRules();
            copyRetentionRuleField.setOptions(copyRetentionRules);
            if (copyRetentionRules.size() == 1) {
                copyRetentionRuleField.setFieldValue(copyRetentionRules.get(0).getId());
            }
        } else {
            copyRetentionRuleField.setOptions(new ArrayList<CopyRetentionRule>());
        }
    }
}
