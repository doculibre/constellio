package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddNewContainerPresenter extends AddEditContainerPresenter {
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient RMConfigs rmConfigs;

	String listId;

	public AddNewContainerPresenter(AddNewContainerView view) {
		super(view);
	}

	@Override
	public AddNewContainerPresenter forParams(String parameters) {
		container = new RecordToVOBuilder().build(newContainerRecord(), VIEW_MODE.FORM, view.getSessionContext());
		listId = parameters;
		editMode = false;
		return this;
	}

	@Override
	public boolean canEditAdministrativeUnit() {
		return rmConfigs().areMixedContainersAllowed();
	}

	@Override
	public boolean canEditDecommissioningType() {
		return false;
	}

	@Override
	public void saveButtonClicked(RecordVO recordVO) {
		DecommissioningList decommissioningList = rmRecordsServices().getDecommissioningList(listId);
		ContainerRecord container = rmRecordsServices().wrapContainerRecord(toRecord(recordVO));
		if (!canEditAdministrativeUnit()) {
			container.setAdministrativeUnit(decommissioningList.getAdministrativeUnit());
		}
		decommissioningList.addContainerDetailsFrom(Arrays.asList(container));

		Transaction transaction = new Transaction().setUser(getCurrentUser());
		transaction.addAll(container, decommissioningList);
		try {
			recordServices().execute(transaction);
			view.navigate().to(RMViews.class).displayDecommissioningList(listId);
		} catch (Exception e) {
			e.printStackTrace();
			view.showErrorMessage($("AddNewContainerView.failedToSave"));
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(params);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		DecommissioningList decommissioningList = rmRecordsServices().wrapDecommissioningList(restrictedRecord);
		AdministrativeUnit administrativeUnit = rmRecordsServices().getAdministrativeUnit(decommissioningList.getAdministrativeUnit());
		DecommissioningSecurityService decommissioningSecurityService = new DecommissioningSecurityService(view.getCollection(), appLayerFactory);
		return user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).on(administrativeUnit) || decommissioningSecurityService.hasPermissionToCreateTransferOnList(decommissioningList, user);
	}

	private RMSchemasRecordsServices rmRecordsServices() {
		if (rmRecordsServices == null) {
			rmRecordsServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordsServices;
	}

	private RMConfigs rmConfigs() {
		if (rmConfigs == null) {
			rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		}
		return rmConfigs;
	}
}
