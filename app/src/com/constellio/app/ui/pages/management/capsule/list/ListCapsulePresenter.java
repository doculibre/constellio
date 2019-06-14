package com.constellio.app.ui.pages.management.capsule.list;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;

public class ListCapsulePresenter extends BasePresenter<ListCapsuleView> {
	public ListCapsulePresenter(ListCapsuleView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.ACCESS_SEARCH_CAPSULE).globally();
	}

	public RecordVODataProvider getCapsuleDataProvider() {
		final MetadataSchemaType CapsuleSchemaType = types().getSchemaType(Capsule.SCHEMA_TYPE);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(CapsuleSchemaType.getSchema(Capsule.DEFAULT_SCHEMA),
				RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
		return new RecordVODataProvider(
				schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				OngoingLogicalSearchCondition FromCondition = from(CapsuleSchemaType.getSchema(Capsule.DEFAULT_SCHEMA));
				LogicalSearchCondition condition = FromCondition.where(returnAll());
				return new LogicalSearchQuery().setCondition(condition);
			}
		};
	}

	public RecordVO getRecordsWithIndex(Object itemId) {
		return this.getCapsuleDataProvider().getRecordVO(Integer.parseInt(itemId + ""));
	}

	public void deleteButtonClicked(RecordVO record) {
		SchemaPresenterUtils utils = new SchemaPresenterUtils(Capsule.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
		utils.delete(utils.toRecord(record), null);
		view.navigate().to().listCapsule(); /* Capsule */
	}

	public void displayButtonClicked(RecordVO record) {
		view.navigate().to().displayCapsule(record.getId());
	}

	public void editButtonClicked(RecordVO record) {
		view.navigate().to().addEditCapsule(record.getId());
	}

	public void addButtonClicked() {
		view.navigate().to().addEditCapsule(null);
	}

	public void backButtonClicked() {
		view.navigate().to().searchConfiguration();
	}
}
