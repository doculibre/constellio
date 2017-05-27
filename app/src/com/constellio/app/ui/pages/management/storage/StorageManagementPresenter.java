package com.constellio.app.ui.pages.management.storage;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class StorageManagementPresenter extends BasePresenter<StorageManagementView> {

	public StorageManagementPresenter(StorageManagementView view) {
		super(view);
	}

    public List<String> getReplicatedVaultMountPoints() {
        return modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().getContentDaoReplicatedVaultMountPoints();
    }

    public void saveConfigurations(List<String> replicatedVaultMountPoints) {
        modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration().setContentDaoReplicatedVaultMountPoints(replicatedVaultMountPoints);

        modelLayerFactory.getDataLayerFactory().updateContentDao();
    }

	public void backButtonClick() {
		view.navigate().to().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_STORAGE);
	}

}
