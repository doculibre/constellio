package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.ui.pages.folder.SummaryColumnVO;
import com.constellio.app.ui.pages.base.BaseView;

public interface SummaryColumnView extends BaseView {
    void alterSummaryMetadata(SummaryColumnVO summaryColumnView);
    void deleteSummaryMetadata(SummaryColumnVO summaryColumnVO);
}