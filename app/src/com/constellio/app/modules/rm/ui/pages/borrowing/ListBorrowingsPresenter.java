package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import static com.constellio.model.services.contents.ContentFactory.checkedOut;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListBorrowingsPresenter extends BasePresenter<ListBorrowingsView> {
	public ListBorrowingsPresenter(ListBorrowingsView view) {
		super(view);
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_BORROWINGS).globally();
	}

	// TODO::JOLA --> Fix data provider for folder and container
	// TODO::JOLA --> Include unit and overdue filters
	public RecordVODataProvider getDataProvider(String schemaTypeCode) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getSessionContext().getCurrentCollection(), appLayerFactory);
		MetadataSchemaType schemaType = rm.documentSchemaType();

		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				schemaType.getDefaultSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(schemaType)
						.where(rm.documentContent()).is(checkedOut()))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}
}
