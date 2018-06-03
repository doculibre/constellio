package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.ui.pages.folder.SummaryColumnVO;
import com.constellio.app.ui.pages.base.BaseView;
import org.vaadin.dialogs.ConfirmDialog;

public interface SummaryColumnView extends BaseView {
    void alterSummaryMetadata(SummaryColumnVO summaryColumnView);
    void deleteRow(SummaryColumnVO columnVO);
    boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener);
    SummaryColumnPresenter getSummaryColumnPresenter();
}