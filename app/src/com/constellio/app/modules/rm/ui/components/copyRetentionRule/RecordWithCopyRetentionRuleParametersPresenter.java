package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Predicate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class RecordWithCopyRetentionRuleParametersPresenter {
    RecordWithCopyRetentionRuleParametersFields fields;

    public RecordWithCopyRetentionRuleParametersPresenter(RecordWithCopyRetentionRuleParametersFields fields) {
        this.fields = fields;
    }

    void rmFieldsCreated() {
        CopyRetentionRuleDependencyField retentionRuleField = fields.getCopyRetentionRuleDependencyField();
        if (retentionRuleField != null) {
            retentionRuleField.addValueChangeListener(new CopyRetentionRuleDependencyField.RetentionValueChangeListener() {
                @Override
                public void valueChanged(String newValue) {
                    updateFields(newValue);
                }
            });
        }
    }

    private void updateFields(String dependencyRecordId) {
        CopyRetentionRuleField copyRetentionRuleField = fields.getCopyRetentionRuleField();
        if (StringUtils.isNotBlank(dependencyRecordId)) {
            List<CopyRetentionRule> copyRetentionRules = getOptions(dependencyRecordId);
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

    private List<CopyRetentionRule> getOptions(String dependencyRecordId) {
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
