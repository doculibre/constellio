package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.ui.pages.containers.ContainersByAdministrativeUnitsView.ContainersViewTab;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ContainersByAdministrativeUnitsPresenter extends BasePresenter<ContainersByAdministrativeUnitsView> {

	public static final String TAB_TRANSFER_NO_STORAGE_SPACE = "transferNoStorageSpace";
	public static final String TAB_DEPOSIT_NO_STORAGE_SPACE = "depositNoStorageSpace";
	public static final String TAB_TRANSFER_WITH_STORAGE_SPACE = "transferWithStorageSpace";
	public static final String TAB_DEPOSIT_WITH_STORAGE_SPACE = "depositWithStorageSpace";

	private ContainersViewTab transferNoStorageSpace;
	private ContainersViewTab depositNoStorageSpace;
	private ContainersViewTab transferWithStorageSpace;
	private ContainersViewTab depositWithStorageSpace;

	private List<ContainersViewTab> tabs;

	private transient DecommissioningService decommissioningService;

	private String tab;

	public ContainersByAdministrativeUnitsPresenter(ContainersByAdministrativeUnitsView view) {
		super(view);
		init();
	}

	public void init() {
		transferNoStorageSpace = new ContainersViewTab(
				TAB_TRANSFER_NO_STORAGE_SPACE,
				getDataProvider());
		depositNoStorageSpace = new ContainersViewTab(
				TAB_DEPOSIT_NO_STORAGE_SPACE,
				getDataProvider());
		transferWithStorageSpace = new ContainersViewTab(
				TAB_TRANSFER_WITH_STORAGE_SPACE,
				getDataProvider());
		depositWithStorageSpace = new ContainersViewTab(
				TAB_DEPOSIT_WITH_STORAGE_SPACE,
				getDataProvider());

		tabs = Arrays.asList(transferNoStorageSpace, depositNoStorageSpace, transferWithStorageSpace, depositWithStorageSpace);
	}

	RecordVODataProvider getDataProvider() {
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		List<String> metadataCodes = new ArrayList<String>();
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_id");
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_code");
		metadataCodes.add(AdministrativeUnit.DEFAULT_SCHEMA + "_title");

		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(AdministrativeUnit.DEFAULT_SCHEMA), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				User user = getCurrentUser();
				if (!user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).globally()) {
					List<String> adminUnitIds = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS);
					return new LogicalSearchQuery(from(rm.administrativeUnit.schemaType())
							.where(Schemas.IDENTIFIER).isIn(adminUnitIds)
							.andWhere(
									Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull());
				} else {
					return new LogicalSearchQuery(from(rm.administrativeUnit.schemaType())
							.where(rm.administrativeUnit.parent()).isNull()
							.andWhere(
									Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull());
				}
			}
		};
		return dataProvider;
	}

	public void forParams(String params) {
		if (view.getTabs().isEmpty()) {
			view.setTabs(tabs);
		}

		String tabName = params;
		this.tab = params;
		ContainersViewTab initialTab;
		if (TAB_TRANSFER_NO_STORAGE_SPACE.equals(tabName)) {
			initialTab = transferNoStorageSpace;
		} else if (TAB_DEPOSIT_NO_STORAGE_SPACE.equals(tabName)) {
			initialTab = depositNoStorageSpace;
		} else if (TAB_TRANSFER_WITH_STORAGE_SPACE.equals(tabName)) {
			initialTab = transferWithStorageSpace;
		} else if (TAB_DEPOSIT_WITH_STORAGE_SPACE.equals(tabName)) {
			initialTab = depositWithStorageSpace;
		} else {
			initialTab = null;
		}
		if (initialTab != null) {
			view.selectTab(initialTab);
		}
	}

	public String getTab() {
		return tab;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		DecommissioningSecurityService securityServices = new DecommissioningSecurityService(collection, appLayerFactory);
		return securityServices.hasAccessToManageContainersPage(user);
	}

	public void displayButtonClicked(String tabName, RecordVO entity) {
		view.navigate().to(RMViews.class).displayAdminUnitWithContainers(tabName, entity.getId());
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).archiveManagement();
	}

}
