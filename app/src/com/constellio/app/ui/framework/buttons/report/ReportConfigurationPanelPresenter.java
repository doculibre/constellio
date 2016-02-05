package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToFormVOBuilder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportConfigurationPanelPresenter {

    protected ReportConfigurationPresenter presenter;
    public ReportConfigurationPanelPresenter(ReportConfigurationPresenter presenter) {
        this.presenter = presenter;
    }

    public List<FormMetadataVO> getMetadata() {
        List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
        MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
        SchemasDisplayManager displayManager = this.presenter.getSchemasDisplayManager();
        for (Metadata metadata : presenter.getAllSchemaTypeMetadata()) {
            if (this.isAllowedMetadata(metadata)) {
                FormMetadataVO metadataVO = builder.build(metadata, displayManager, presenter.getSchemaTypeCode());
                formMetadataVOs.add(metadataVO);
            }
        }
        return formMetadataVOs;
    }

    private boolean isAllowedMetadata(Metadata metadata) {
        boolean result;
        List<Metadata> restrictedMetadata = Arrays.asList(Schemas.SCHEMA, Schemas.VERSION, Schemas.PATH, Schemas.PRINCIPAL_PATH,
                Schemas.PARENT_PATH, Schemas.AUTHORIZATIONS, Schemas.REMOVED_AUTHORIZATIONS, Schemas.INHERITED_AUTHORIZATIONS,
                Schemas.ALL_AUTHORIZATIONS, Schemas.IS_DETACHED_AUTHORIZATIONS, Schemas.TOKENS, Schemas.COLLECTION,
                Schemas.FOLLOWERS, Schemas.LOGICALLY_DELETED_STATUS, Schemas.TITLE);

        List<MetadataValueType> restrictedType = Arrays.asList(MetadataValueType.STRUCTURE, MetadataValueType.CONTENT);

        List<String> localCodes = new SchemaUtils().toMetadataLocalCodes(restrictedMetadata);

        result = !metadata.isMultivalue();
        //result = result && !restrictedType.contains(metadata.getValueType());
        result = result && !localCodes.contains(metadata.getCode());

        return result;
    }

    public void saveButtonClicked(List<FormMetadataVO> values) {
        //TODO
    }

    public void cancelButtonClicked() {
        //TODO
    }

    public List<FormMetadataVO> getValueMetadatas(String collection) {
        MetadataSchemasManager schemasManager = presenter.getMetadataSchemasManager();
        SchemasDisplayManager displayManager = presenter.getSchemasDisplayManager();
       // List<String> codeList = displayManager.getSchema(collection, presenter.getSchemaCode()).getSearchResultsMetadataCodes();

        List<FormMetadataVO> formMetadataVOs = new ArrayList<>();
        MetadataToFormVOBuilder builder = new MetadataToFormVOBuilder();
        /*for (String metadataCode : codeList) {
            Metadata metadata = schemasManager.getSchemaTypes(collection).getMetadata(metadataCode);
            formMetadataVOs.add(builder.build(metadata, displayManager, presenter.getSchemaTypeCode()));
        }*/
        return formMetadataVOs;
    }

}
