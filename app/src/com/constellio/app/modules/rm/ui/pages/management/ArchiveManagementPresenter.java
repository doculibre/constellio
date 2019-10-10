package com.constellio.app.modules.rm.ui.pages.management;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.entries.DataEntryType;

public class ArchiveManagementPresenter extends BasePresenter<ArchiveManagementView> {
	public ArchiveManagementPresenter(ArchiveManagementView view) {
		super(view);
	}

	public void decommissioningButtonClicked() {
		view.navigate().to(RMViews.class).decommissioning();
	}

	public void multipleContainersButtonClicked() {
		view.navigate().to(RMViews.class).addMultipleContainers();
	}

	public void newContainerButtonClicked() {
		view.navigate().to(RMViews.class).addContainer();
	}

	public void containersButtonClicked() {
		view.navigate().to(RMViews.class).containersByAdministrativeUnits();
	}

	public void reportsButtonClicked() {
		view.navigate().to(RMViews.class).reports();
	}

	public void baginfoButtonClick() {
		view.navigate().to().listBagInfo();
	}

	public void onViewAssembled() {
		User user = getCurrentUser();
		DecommissioningSecurityService securityServices = new DecommissioningSecurityService(collection, appLayerFactory);
		view.setDecommissioningButtonVisible(securityServices.hasAccessToDecommissioningMainPage(user));
		view.setNewContainerButtonVisible(securityServices.canCreateContainers(user));
		view.setContainersButtonVisible(securityServices.hasAccessToManageContainersPage(user));
		view.setPrintReportsButtonVisible(hasAccessToManageReportsPage());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		DecommissioningSecurityService securityServices = new DecommissioningSecurityService(collection, appLayerFactory);
		return securityServices.hasAccessToDecommissioningMainPage(user) || securityServices.hasAccessToManageContainersPage(user) || hasAccessToManageReportsPage();
	}

	private boolean hasAccessToManageReportsPage() {
		User user = getCurrentUser();
		return user.has(RMPermissionsTo.MANAGE_REPORTS).onSomething();
	}

	public boolean hasAccessToBagInfoPage() {
		return getCurrentUser().has(RMPermissionsTo.MANAGE_BAG_INFO).globally();
	}

	public boolean isMultipleContainersButtonVisible() {
		return (areContainersSequential() || Toggle.FORCE_MULTIPLE_CONTAINERS_VIEW_TO_DISPLAY.isEnabled())
			   && presenterService().getCurrentUser(view.getSessionContext()).has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}

	private boolean areContainersSequential() {
		return isMetadataSequential(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.IDENTIFIER) ||
			   isMetadataSequential(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.TEMPORARY_IDENTIFIER);
	}

	private boolean isMetadataSequential(String metadataCode) {
		return DataEntryType.SEQUENCE.equals(modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getMetadata(metadataCode).getDataEntry().getType());
	}
}
