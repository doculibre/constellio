package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;

public interface BatchProcessingView {
    List<String> getSelectedRecordIds();

    String getSchemaType();

    SessionContext getSessionContext();
}
