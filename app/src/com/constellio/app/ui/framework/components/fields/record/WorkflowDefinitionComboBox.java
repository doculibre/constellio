package com.constellio.app.ui.framework.components.fields.record;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

public class WorkflowDefinitionComboBox extends RecordComboBox {
    public WorkflowDefinitionComboBox(String schemaCode) {
        super(schemaCode);
    }

    @Override
    public void setDataProvider(RecordVODataProvider dataProvider) {
        int size = dataProvider.size();
        List<RecordVO> records = dataProvider.listRecordVOs(0, size);
        for (RecordVO recordVO : records) {
            addItem(recordVO.get(Schemas.CODE));
            setItemCaption(recordVO.get(Schemas.CODE), recordVO.getTitle());
        }
    }

    @Override
    public Object getValue() {
        return getItemCaption(super.getValue());
    }
}
