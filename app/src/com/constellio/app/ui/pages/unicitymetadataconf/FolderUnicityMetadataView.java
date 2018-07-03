package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.ui.entities.FolderUnicityVO;
import com.constellio.app.ui.pages.base.BaseView;
import org.vaadin.dialogs.ConfirmDialog;

public interface FolderUnicityMetadataView extends BaseView {
    void deleteRow(FolderUnicityVO columnVO);
    boolean showReindexationWarningIfRequired(ConfirmDialog.Listener confirmDialogListener);
    FolderUnicityMetadataPresenter getSummaryColumnPresenter();
}