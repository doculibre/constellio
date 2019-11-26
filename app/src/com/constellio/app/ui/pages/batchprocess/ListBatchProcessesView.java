package com.constellio.app.ui.pages.batchprocess;

import com.constellio.app.modules.rm.ui.pages.viewGroups.PersonnalSpaceViewGroup;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface ListBatchProcessesView extends BaseView, PersonnalSpaceViewGroup {

	void setUserBatchProcesses(BatchProcessDataProvider dataProvider);

	void setSystemBatchProcesses(BatchProcessDataProvider dataProvider);

}
