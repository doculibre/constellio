package com.constellio.app.ui.framework.components.fields.record;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;

import java.util.List;

public class WorkflowExecutionComboBox extends RecordComboBox {
    public WorkflowExecutionComboBox(String schemaCode) {
        super(schemaCode);
    }

    @Override
    public void setDataProvider(RecordVODataProvider dataProvider) {
        int size = dataProvider.size();
        List<RecordVO> records = dataProvider.listRecordVOs(0, size);
        for (RecordVO recordVO : records) {
            addItem(recordVO.getId());
            setItemCaption(recordVO.getId(), recordVO.getTitle());
        }
    }
}
