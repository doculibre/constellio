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
package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.migrations.MigrationServices;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class AddEditCollectionPresenter extends BasePresenter<AddEditCollectionView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditCollectionPresenter.class);
	transient UserServices userServices;
	private boolean actionEdit = false;
	private Map<String, String> paramsMap;
	private String code;
	private transient Record collectionRecord;

	private transient CollectionsListManager collectionsListManager;
	private transient ConstellioModulesManager modulesManager;
	private transient MigrationServices migrationServices;
	private transient CollectionsManager collectionsManager;

	public AddEditCollectionPresenter(AddEditCollectionView view, String code) {
		super(view);
		this.code = code;
		init();
	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		collectionsListManager = modelLayerFactory.getCollectionsListManager();
		modulesManager = appLayerFactory.getModulesManager();
		migrationServices = appLayerFactory.newMigrationServices();
		collectionsManager = appLayerFactory.getCollectionsManager();
		if (StringUtils.isNotBlank(code)) {
			actionEdit = true;
			this.collectionRecord = recordServices().getDocumentById(code);
		}
	}

	public CollectionVO getCollectionVO() {
		if (actionEdit) {
			return new CollectionVO(code, (String) collectionRecord.get(Schemas.TITLE));
		} else {
			return new CollectionVO();
		}
	}

	public void saveButtonClicked(CollectionVO entity) {
		String code = entity.getCode();
		if (!getActionEdit()) {
			if (collectionsListManager.getCollections().contains(code)) {
				view.showErrorMessage($("AddEditCollectionView.codeNonAvailable"));
				return;
			} else {
				this.collectionRecord = createCollection(code);
			}
		} else {
			if (!getCode().equals(code)) {
				view.showErrorMessage($("AddEditCollectionView.codeChangeForbidden"));
				return;
			}
		}
		updateCollection(entity);

		view.showMessage($("AddEditCollectionPresenter.addConfirmation"));
		navigateToBackPage();
	}

	private void updateCollection(CollectionVO entity) {
		String collectionTitle = entity.getName();
		try {
			recordServices().update(collectionRecord.set(Schemas.TITLE, collectionTitle));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private Record createCollection(String code) {
		Record newCollectionRecord = collectionsManager.createCollectionInCurrentVersion(code, Arrays.asList("fr"));

		InstallableModule module = new ConstellioRMModule();

		try {
			modulesManager.enableModule(code, module);
			migrationServices.migrate(null);
		} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
			throw new RuntimeException(optimisticLockingConfiguration);
		}
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential currentUser = userServices.getUser(getCurrentUser().getUsername());
		userServices.addUserToCollection(currentUser, newCollectionRecord.getId());
		User user = userServices.getUserInCollection(currentUser.getUsername(), collection);
		try {
			recordServices().update(user.setUserRoles(Arrays.asList(RMRoles.RGD)));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return newCollectionRecord;
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	private void navigateToBackPage() {
		view.navigateTo().manageCollections();
	}

	public boolean getActionEdit() {
		return actionEdit;
	}

	public String getCode() {
		return code;
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public Map<String, String> getParamsMap() {
		return paramsMap;
	}

	public void backButtonClick() {
		view.navigateTo().manageCollections();
	}
	
	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_COLLECTIONS).globally();
	}

}
