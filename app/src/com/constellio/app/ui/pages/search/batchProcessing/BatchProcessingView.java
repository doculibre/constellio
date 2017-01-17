package com.constellio.app.ui.pages.search.batchProcessing;

import com.constellio.app.ui.pages.base.SessionContext;

import java.util.List;

public interface BatchProcessingView {
    List<String> getSelectedRecordIds();

    List<String> getUnselectedRecordIds();

    String getSchemaType();

    SessionContext getSessionContext();

    void showErrorMessage(String error);

    void showMessage(String message);
}
