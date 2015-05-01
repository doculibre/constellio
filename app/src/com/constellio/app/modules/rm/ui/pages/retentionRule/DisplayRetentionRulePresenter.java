/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class DisplayRetentionRulePresenter extends SingleSchemaBasePresenter<DisplayRetentionRuleView> {
	private RetentionRuleVO retentionRuleVO;

	public DisplayRetentionRulePresenter(DisplayRetentionRuleView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
	}

	public void forParams(String params) {
		Record record = getRecord(params);
		retentionRuleVO = new RetentionRuleToVOBuilder(appLayerFactory, types().getDefaultSchema(Category.SCHEMA_TYPE),
				schema(UniformSubdivision.DEFAULT_SCHEMA))
				.build(record, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setRetentionRule(retentionRuleVO);
	}

	public void viewAssembled() {
	}

	public void backButtonClicked() {
		view.navigateTo().listRetentionRules();
	}

	public void editButtonClicked() {
		view.navigateTo().editRetentionRule(retentionRuleVO.getId());
	}

	public void deleteButtonClicked() {
		Record record = getRecord(retentionRuleVO.getId());
		delete(record, false);
		view.navigateTo().listRetentionRules();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}
}
