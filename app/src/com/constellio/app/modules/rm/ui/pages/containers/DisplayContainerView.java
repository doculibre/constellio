package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.ui.pages.viewGroups.ArchivesManagementViewGroup;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayContainerView extends BaseView, ArchivesManagementViewGroup {
    public void setBorrowedMessage(String borrowedMessage);
}
