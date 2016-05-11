package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;

public interface BatchProcessingPresenter {
    String getOriginSchema(String schemaType, List<String> selectedRecordIds);

    List<String> getDestinationSchemata(String originSchema);

    RecordVO newRecordVO(String schema, SessionContext sessionContext);

    void simulateButtonClicked(RecordVO viewObject);

    void saveButtonClicked(RecordVO viewObject);
}
