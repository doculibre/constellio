package com.constellio.app.modules.rm.ui.pages.containers;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory.ContainerSearchParameters;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ContainersInAdministrativeUnitPresenter extends BasePresenter<ContainersInAdministrativeUnitView> {

	public static final String DEPOSIT_PREFIX = "deposit";
	public static final String TRANSFER_PREFIX = "transfer";
	public static final String WITH_STORAGE_SPACE_SUFFIX = "WithStorageSpace";

	String adminUnitId;
	String tabName;

	public ContainersInAdministrativeUnitPresenter(ContainersInAdministrativeUnitView view) {
		super(view);
	}

	public RecordVODataProvider getChildrenAdminUnitsDataProvider() {
		final MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(AdministrativeUnit.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				User user = presenterService().getCurrentUser(view.getSessionContext());
				DecommissioningService service = new DecommissioningService(
						view.getCollection(), modelLayerFactory);
				List<String> visibleAdminUnitsId = service.getChildrenAdministrativeUnit(adminUnitId, user);
				MetadataSchema schema = schema(schemaVO.getCode());
				LogicalSearchCondition condition;
				if (!visibleAdminUnitsId.isEmpty()) {
					condition = LogicalSearchQueryOperators.from(schema)
							.where(schema.getMetadata(AdministrativeUnit.PARENT)).isEqualTo(adminUnitId);
				} else {
					condition = LogicalSearchQueryOperators.from(schema).where(Schemas.TOKENS).isContaining(Arrays.asList("A38"));
				}
				return new LogicalSearchQuery(condition);
			}
		};
		return dataProvider;
	}

	public RecordVODataProvider getContainersDataProvider() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(ContainerRecord.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				DecommissioningSearchConditionFactory conditionFactory = new DecommissioningSearchConditionFactory(
						view.getCollection(), modelLayerFactory);
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

	public RecordVO getAdministrativeUnit() {
		return new RecordToVOBuilder()
				.build(recordServices().getDocumentById(adminUnitId), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void forParams(String params) {
		String[] splitParams = params.split("/");
		tabName = splitParams[0];
		adminUnitId = splitParams[1];
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	public void displayAdminUnitButtonClicked(String tabName, RecordVO adminUnit) {
		view.navigateTo().displayAdminUnitWithContainers(tabName, adminUnit.getId());
	}

	public void displayFilingSpaceButtonClicked(String tabName, RecordVO filingSpace) {
		view.navigateTo().displayFilingSpaceWithContainers(tabName, adminUnitId, filingSpace.getId());
	}

	public void displayContainerButtonClicked(RecordVO container) {
		view.navigateTo().displayContainer(container.getId());
	}
}
