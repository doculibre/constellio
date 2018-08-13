package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.ui.entities.SummaryConfigElementVO;
import com.constellio.app.ui.pages.base.BaseView;
import org.vaadin.dialogs.ConfirmDialog;

public interface SummaryConfigView extends BaseView {
	void alterSummaryMetadata(SummaryConfigElementVO summaryColumnView);

	void deleteRow(SummaryConfigElementVO columnVO);

	boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener);

	SummaryConfigPresenter getSummaryColumnPresenter();
}