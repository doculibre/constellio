package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface SearchRetentionRulesView extends BaseView, AdminViewGroup {

	void setDataProvider(RecordVODataProvider dataProvider);

}
