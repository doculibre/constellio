package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.BaseViewImpl;

import javax.swing.text.View;

public class ListSchemaExtraCommandParams {
    MetadataSchemaVO schemaVO;
    BaseViewImpl view;

    public ListSchemaExtraCommandParams(MetadataSchemaVO schemaVO, BaseViewImpl view){
        this.schemaVO = schemaVO;
        this.view = view;
    }

    public MetadataSchemaVO getSchemaVO() {
        return schemaVO;
    }

    public void setSchemaVO(MetadataSchemaVO schemaVO) {
        this.schemaVO = schemaVO;
    }

    public BaseViewImpl getView() {
        return view;
    }

    public void setView(BaseViewImpl view) {
        this.view = view;
    }
}
