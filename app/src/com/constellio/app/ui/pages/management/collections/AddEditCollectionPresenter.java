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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CannotSelectBothRMandES;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_MustSelectAtLeastOneModule;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_TasksCannotBeTheOnlySelectedModule;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class AddEditCollectionPresenter extends BasePresenter<AddEditCollectionView> {
	transient UserServices userServices;
	private boolean actionEdit = false;
	private Map<String, String> paramsMap;
	private String code;

	private transient CollectionsListManager collectionsListManager;
	private transient ConstellioModulesManager modulesManager;
	private transient CollectionsManager collectionsManager;
	private transient Collection collectionRecord;

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
		collectionsManager = appLayerFactory.getCollectionsManager();
		if (StringUtils.isNotBlank(code)) {
			actionEdit = true;
			collectionRecord = collectionsManager.getCollection(code);
		}
	}

	public CollectionVO getCollectionVO() {
		if (actionEdit) {
			return new CollectionVO(
					code, collectionRecord.getTitle(), collectionRecord.getLanguages().get(0), getInstalledModules());
		} else {
			return new CollectionVO();
		}
	}

	private List<String> getInstalledModules() {
		List<String> modules = new ArrayList<>();
		for (Module module : modulesManager.getEnabledModules(code)) {
			modules.add(module.getId());
		}
		return modules;
	}

	public List<String> getAvailableModules() {
		List<String> modules = new ArrayList<>();
		for (Module module : modulesManager.getAllModules()) {
			modules.add(module.getId());
		}
		return modules;
	}

	public void saveButtonClicked(CollectionVO entity)
			throws AddEditCollectionPresenterException {
		String code = entity.getCode();
		if (!getActionEdit()) {
			if (collectionsListManager.getCollections().contains(code)) {
				view.showErrorMessage($("AddEditCollectionView.codeNonAvailable"));
				return;
			} else {
				createCollection(code, entity.getModules());
			}
		} else {
			if (!getCode().equals(code)) {
				view.showErrorMessage($("AddEditCollectionView.codeChangeForbidden"));
				return;
			}
			updateCollection(entity);
		}
		view.showMessage($("AddEditCollectionPresenter.addConfirmation"));
		navigateToBackPage();
	}

	private void updateCollection(CollectionVO entity) {
		String collectionTitle = entity.getName();
		try {
			recordServices().update(collectionRecord.setTitle(collectionTitle));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private Record createCollection(String code, Set<String> modules)
			throws AddEditCollectionPresenterException {
		if (modules.contains("rm") && modules.contains("es")) {
			throw new AddEditCollectionPresenterException_CannotSelectBothRMandES();
		} else if (modules.isEmpty()) {
			throw new AddEditCollectionPresenterException_MustSelectAtLeastOneModule();
		} else if (modules.size() == 1 && modules.contains("tasks")) {
			throw new AddEditCollectionPresenterException_TasksCannotBeTheOnlySelectedModule();
		}
		String language = modelLayerFactory.getConfiguration().getMainDataLanguage();
		Record newCollectionRecord = collectionsManager.createCollectionInCurrentVersion(code, Arrays.asList(language));
		List<String> roles = new ArrayList<>();
		for (String moduleCode : modules) {
			Module module = modulesManager.getInstalledModule(moduleCode);
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installModule(module, collectionsListManager);
			}
			modulesManager.enableModule(code, module);
			roles.addAll(module.getRolesForCreator());
		}

		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential currentUser = userServices.getUser(getCurrentUser().getUsername());
		userServices.addUserToCollection(currentUser, newCollectionRecord.getId());
		User user = userServices.getUserInCollection(currentUser.getUsername(), code);
		try {
			recordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
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

	public String getModuleCaption(String moduleId) {
		StringBuilder moduleCaption = new StringBuilder($("ConstellioModule." + moduleId));
		List<String> dependencies = getModuleDependencies(moduleId);
		if (!dependencies.isEmpty()) {
			moduleCaption.append(" (");
			for (int i = 0; i < dependencies.size() - 1; i++) {
				String moduleDependency = dependencies.get(i);
				moduleCaption.append($("ConstellioModule." + moduleDependency) + ", ");
			}
			moduleCaption.append($("ConstellioModule." + dependencies.get(dependencies.size() - 1)) + ")");
		}
		return moduleCaption.toString();
	}

	private List<String> getModuleDependencies(String moduleId) {
		return modulesManager.getInstalledModule(moduleId).getDependencies();
	}
}
