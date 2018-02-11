package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.ui.pages.base.BaseView;

public interface PdfStatusView extends BaseView {
    public void firePdfGenerationCompleted(String documentId);
    public void notifyGlobalProgressMessage(String message);
}
