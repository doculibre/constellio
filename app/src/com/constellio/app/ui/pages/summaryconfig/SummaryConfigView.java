package com.constellio.app.ui.pages.summaryconfig;

import com.constellio.app.ui.entities.SummaryConfigElementVO;
import com.constellio.app.ui.pages.base.BaseView;
import org.vaadin.dialogs.ConfirmDialog;

public interface SummaryConfigView extends BaseView {
	void alterSummaryMetadata(SummaryConfigElementVO summaryConfigView);

	void deleteRow(SummaryConfigElementVO columnVO);

	boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener);

	SummaryConfigPresenter getSummaryConfigPresenter();
}