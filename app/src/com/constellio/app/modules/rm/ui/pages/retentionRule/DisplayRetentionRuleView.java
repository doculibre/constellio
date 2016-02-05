package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;

public interface DisplayRetentionRuleView extends BaseView, AdminViewGroup {

	void setRetentionRule(RetentionRuleVO retentionRuleVO);

}
