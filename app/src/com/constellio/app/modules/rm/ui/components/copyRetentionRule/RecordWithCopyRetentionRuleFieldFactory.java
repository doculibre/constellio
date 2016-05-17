package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.ui.components.actionParameters.fields.category.ActionParametersCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.RetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.RetentionRuleFieldImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;

public class RecordWithCopyRetentionRuleFieldFactory extends RecordFieldFactory implements RecordWithCopyRetentionRuleParametersFields {

    private RetentionRuleFieldImpl retentionRuleField;

    private CopyRetentionRuleFieldImpl copyRetentionRuleField;

    private RecordWithCopyRetentionRuleParametersPresenter presenter;

    public RecordWithCopyRetentionRuleFieldFactory() {
        this.presenter = new RecordWithCopyRetentionRuleParametersPresenter(this);
    }

    @Override
    public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
        Field<?> field;
        String code = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
        if (Folder.RETENTION_RULE.equals(code) || Folder.MAIN_COPY_RULE_ID_ENTERED.equals(code)) {
            if (retentionRuleField == null) {
                retentionRuleField = new RetentionRuleFieldImpl(presenter.fields.getSessionContext().getCurrentCollection());
                copyRetentionRuleField = new CopyRetentionRuleFieldImpl();
                presenter.rmFieldsCreated();
            }
            if (Folder.MAIN_COPY_RULE_ID_ENTERED.equals(code)) {
                field = copyRetentionRuleField;
            } else {
                field = retentionRuleField;
            }
            super.postBuild(field, recordVO, metadataVO);
        } else {
            field = super.build(recordVO, metadataVO);
        }
        return field;
    }

    @Override
    public SessionContext getSessionContext() {
        return ConstellioUI.getCurrentSessionContext();
    }

    @Override
    public ConstellioFactories getConstellioFactories() {
        return ConstellioUI.getCurrent().getConstellioFactories();
    }


    @Override
    public RetentionRuleField getRetentionField() {
        return retentionRuleField;
    }

    @Override
    public CopyRetentionRuleField getCopyRetentionRuleField() {
        return copyRetentionRuleField;
    }
}

