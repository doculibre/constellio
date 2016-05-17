package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.CopyRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.CopyRetentionRuleDependencyField;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.DocumentCopyRetentionRuleDependencyFieldImpl;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.retentionRule.FolderCopyRetentionRuleDependencyFieldImpl;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Field;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class RecordWithCopyRetentionRuleFieldFactory extends RecordFieldFactory implements RecordWithCopyRetentionRuleParametersFields {
    static final private List<String> folderSpecificFieldsMetadataLocaleCodes = Arrays.asList(Folder.RETENTION_RULE_ENTERED, Folder.MAIN_COPY_RULE_ID_ENTERED);
    static final private List<String> documentSpecificMetadataLocaleCodes = Arrays.asList(Document.FOLDER, Document.MAIN_COPY_RULE_ID_ENTERED);

    private FolderCopyRetentionRuleDependencyFieldImpl folderCopyRetentionruleDependencyField;

    private DocumentCopyRetentionRuleDependencyFieldImpl documentCopyRetentionRuleDependencyField;

    private CopyRetentionRuleFieldImpl copyRetentionRuleField;

    private RecordWithCopyRetentionRuleParametersPresenter presenter;

    private final String recordIdThatCopyRetentionRuleDependantOn, selectedTypeId, schemaType;

    public RecordWithCopyRetentionRuleFieldFactory(String schemaType, String recordIdThatCopyRetentionRuleDependantOn, String selectedTypeId) {
        this.presenter = new RecordWithCopyRetentionRuleParametersPresenter(this);
        this.schemaType = schemaType;
        this.recordIdThatCopyRetentionRuleDependantOn = recordIdThatCopyRetentionRuleDependantOn;
        this.selectedTypeId = selectedTypeId;
    }

    @Override
    public Field<?> build(RecordVO recordVO, MetadataVO metadataVO) {
        Field<?> field;
        String code = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
        if(schemaType.equals(Folder.SCHEMA_TYPE) &&
                folderSpecificFieldsMetadataLocaleCodes.contains(metadataVO.getLocalCode())){
            field = buildFolderSpecificFields(recordVO, metadataVO);
            super.postBuild(field, recordVO, metadataVO);
        }else if(schemaType.equals(Document.SCHEMA_TYPE) &&
                documentSpecificMetadataLocaleCodes.contains(metadataVO.getLocalCode())){
            field = buildDocumentSpecificFields(recordVO, metadataVO);
            super.postBuild(field, recordVO, metadataVO);
        }else{
            field = super.build(recordVO, metadataVO);
        }
        return field;
    }

    private Field<?> buildDocumentSpecificFields(RecordVO recordVO, MetadataVO metadataVO) {
        Field<?> field;
        if (documentCopyRetentionRuleDependencyField == null) {
            documentCopyRetentionRuleDependencyField = new DocumentCopyRetentionRuleDependencyFieldImpl(presenter.fields.getSessionContext().getCurrentCollection());
            copyRetentionRuleField = new CopyRetentionRuleFieldImpl();
            if(StringUtils.isNotBlank(recordIdThatCopyRetentionRuleDependantOn)){
                documentCopyRetentionRuleDependencyField.setFieldValue(recordIdThatCopyRetentionRuleDependantOn);
            }
            presenter.rmFieldsCreated();
        }
        if (Folder.MAIN_COPY_RULE_ID_ENTERED.equals(metadataVO.getLocalCode())) {
            field = copyRetentionRuleField;
        } else {
            field = documentCopyRetentionRuleDependencyField;
        }
        return field;
    }

    private Field<?> buildFolderSpecificFields(RecordVO recordVO, MetadataVO metadataVO) {
        Field<?> field;
            if (folderCopyRetentionruleDependencyField == null) {
                folderCopyRetentionruleDependencyField = new FolderCopyRetentionRuleDependencyFieldImpl(presenter.fields.getSessionContext().getCurrentCollection());
                copyRetentionRuleField = new CopyRetentionRuleFieldImpl();
                presenter.rmFieldsCreated();
            }
            if (Folder.MAIN_COPY_RULE_ID_ENTERED.equals(metadataVO.getLocalCode())) {
                field = copyRetentionRuleField;
            } else {
                field = folderCopyRetentionruleDependencyField;
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
    public CopyRetentionRuleDependencyField getCopyRetentionRuleDependencyField() {
        if(schemaType.equals(Folder.SCHEMA_TYPE)){
            return folderCopyRetentionruleDependencyField;
        }else if (schemaType.equals(Document.SCHEMA_TYPE)){
            return documentCopyRetentionRuleDependencyField;
        }else{
            throw new RuntimeException("Unsupported for record type " + schemaType);
        }
    }

    @Override
    public CopyRetentionRuleField getCopyRetentionRuleField() {
        return copyRetentionRuleField;
    }

    @Override
    public String getSchemaType() {
        return schemaType;
    }

    @Override
    public String getType() {
        return selectedTypeId;
    }
}

