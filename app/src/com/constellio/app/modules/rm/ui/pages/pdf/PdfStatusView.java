package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface PdfStatusView extends BaseView {
    public void firePdfGenerationCompleted(DocumentVO documentVO);
}
