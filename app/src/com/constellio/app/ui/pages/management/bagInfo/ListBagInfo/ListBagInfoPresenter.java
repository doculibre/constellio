package com.constellio.app.ui.pages.management.bagInfo.ListBagInfo;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;

public class ListBagInfoPresenter extends BasePresenter<ListBagInfoView> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListBagInfoPresenter.class);

	private RecordVODataProvider dataProvider;

	public ListBagInfoPresenter(ListBagInfoView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_BAG_INFO).globally();
	}

	protected RecordVODataProvider getBagInfoDataProvider() {
		if (null == dataProvider) {
			final MetadataSchemaType BagInfoSchemaType = types().getSchemaType(BagInfo.SCHEMA_TYPE);
			MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(BagInfoSchemaType.getSchema(BagInfo.DEFAULT_SCHEMA),
					RecordVO.VIEW_MODE.DISPLAY, view.getSessionContext());
			dataProvider = new RecordVODataProvider(
					schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
				@Override
				public LogicalSearchQuery getQuery() {
					OngoingLogicalSearchCondition FromCondition = from(BagInfoSchemaType.getSchema(BagInfo.DEFAULT_SCHEMA));
					LogicalSearchCondition condition = FromCondition.where(returnAll());
					return new LogicalSearchQuery().setCondition(condition);
				}
			};
		}
		return dataProvider;
	}

	public RecordVO getRecordsWithIndex(Object itemId) {
		return this.getBagInfoDataProvider().getRecordVO(Integer.parseInt(itemId + ""));
	}

	public void deleteButtonClicked(RecordVO record) {
		SchemaPresenterUtils utils = new SchemaPresenterUtils(Capsule.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
		try {
			utils.delete(utils.toRecord(record), null);
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		view.navigate().to().listBagInfo();
	}

	public void displayButtonClicked(RecordVO record) {
		view.navigate().to().displayBagInfo(record.getId());
	}

	public void editButtonClicked(RecordVO record) {
		view.navigate().to().editBagInfo(record.getId());
	}
}
