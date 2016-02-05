package com.constellio.app.ui.pages.management.schemas.display.report;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface ReportConfigurationView extends BaseView, AdminViewGroup {
    String getSelectedReport();
}
