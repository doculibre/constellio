package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

public class ContainersInFilingSpacePresenter extends BasePresenter<ContainersInFilingSpaceView> {

	public static final String DEPOSIT_PREFIX = "deposit";
	public static final String TRANSFER_PREFIX = "transfer";
	public static final String WITH_STORAGE_SPACE_SUFFIX = "WithStorageSpace";

	String filingSpaceId;
	String adminUnitId;
	String tabName;

	public ContainersInFilingSpacePresenter(ContainersInFilingSpaceView view) {
		super(view);
	}

	public RecordVODataProvider getContainersDataProvider() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(ContainerRecord.DEFAULT_SCHEMA), VIEW_MODE.TABLE);
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory) {
			@Override
			public LogicalSearchQuery getQuery() {
				DecommissioningSearchConditionFactory conditionFactory = new DecommissioningSearchConditionFactory(
						view.getCollection(), appLayerFactory);
				ContainerSearchParameters parameters = new ContainerSearchParameters();
				parameters.setAdminUnitId(adminUnitId);
				if (tabName.startsWith(DEPOSIT_PREFIX)) {
					parameters.setType(DecommissioningType.DEPOSIT);
				} else if (tabName.startsWith(TRANSFER_PREFIX)) {
					parameters.setType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE);
				}
				parameters.setUserId(view.getSessionContext().getCurrentUser().getId());
				parameters.setWithStorage(tabName.endsWith(WITH_STORAGE_SPACE_SUFFIX));
				return new LogicalSearchQuery(conditionFactory.getVisibleContainersCondition(parameters));
			}
		};
		return dataProvider;
	}

	public RecordVO getFilingSpace() {
		return new RecordToVOBuilder()
				.build(recordServices().getDocumentById(filingSpaceId), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void forParams(String params) {
		String[] splitParams = params.split("/");
		tabName = splitParams[0];
		adminUnitId = splitParams[1];
		filingSpaceId = splitParams[2];
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		boolean access;
		if (user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally()) {
			access = true;
		} else {
			List<String> adminUnitIdsWithPermissions = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.MANAGE_CONTAINERS);
			access = adminUnitIdsWithPermissions.contains(adminUnitId);
		}
		return access;
	}

	public void displayContainerButtonClicked(RecordVO container) {
		view.navigate().to(RMViews.class).displayContainer(container.getId());
	}

}
