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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ListRetentionRulesPresenter extends SingleSchemaBasePresenter<ListRetentionRulesView> {
	private RecordToVOBuilder voBuilder = new RecordToVOBuilder();
	private MetadataSchemaVO schemaVO;

	public ListRetentionRulesPresenter(ListRetentionRulesView view) {
		super(view, RetentionRule.DEFAULT_SCHEMA);
		schemaVO = new MetadataSchemaToVOBuilder().build(schema(), VIEW_MODE.TABLE, view.getSessionContext());
	}

	public void viewAssembled() {
		view.setDataProvider(new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchema schema = schema();
				return new LogicalSearchQuery(from(schema).where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
						.sortAsc(schema.getMetadata(RetentionRule.CODE));
			}
		});
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

	public void addButtonClicked() {
		view.navigateTo().addRetentionRule();
	}

	public void displayButtonClicked(RecordVO recordVO) {
		view.navigateTo().displayRetentionRule(recordVO.getId());
	}

	public void editButtonClicked(RecordVO recordVO) {
		view.navigateTo().editRetentionRule(recordVO.getId());
	}

	public void deleteButtonClicked(RecordVO recordVO) {
		Record record = getRecord(recordVO.getId());
		delete(record, false);
		view.navigateTo().listRetentionRules();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_RETENTIONRULE).globally();
	}
}
