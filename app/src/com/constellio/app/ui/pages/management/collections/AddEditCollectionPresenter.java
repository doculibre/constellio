package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeCodeChangeForbidden;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeShouldNotContainDash;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeUnAvailable;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_MustSelectAtLeastOneModule;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
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
					code, collectionRecord.getName(), collectionRecord.getLanguages().get(0), getEnabledModules(code));
		} else {
			return new CollectionVO();
		}
	}

	List<String> getEnabledModules(String code) {
		List<String> modules = new ArrayList<>();
		for (Module module : modulesManager.getEnabledModules(code)) {
			if (!module.isComplementary()) {
				modules.add(module.getId());
			}
		}
		return modules;
	}

	public List<String> getAvailableModules() {
		List<String> modules = new ArrayList<>();
		for (Module module : modulesManager.getAllModules()) {
			if (!module.isComplementary()) {
				modules.add(module.getId());
			}
		}
		return modules;
	}

	public void saveButtonClicked(CollectionVO entity)
			throws AddEditCollectionPresenterException {
		String code = entity.getCode();
		validateCode(code);
		validateModules(entity.getModules());
		if (!getActionEdit()) {
			Set<String> invalidModules = createCollection(entity);
			if (invalidModules.isEmpty()) {
				view.showMessage($("AddEditCollectionPresenter.addConfirmation"));
			} else {
				view.showMessage($("AddEditCollectionPresenter.addConfirmationWithInvalidModules" + StringUtils
						.join(invalidModules, "\n")));
			}
		} else {
			Set<String> invalidModules = updateCollection(entity);
			if (invalidModules.isEmpty()) {
				view.showMessage($("AddEditCollectionPresenter.updateConfirmation"));
			} else {
				view.showMessage($("AddEditCollectionPresenter.updateConfirmationWithInvalidModules" + StringUtils
						.join(invalidModules, "\n")));
			}
		}
		navigateToBackPage();
	}

	private void validateModules(Set<String> modules)
			throws AddEditCollectionPresenterException {
		if (modules == null || modules.isEmpty()) {
			throw new AddEditCollectionPresenterException_MustSelectAtLeastOneModule();
		}
	}

	private void validateCode(String code)
			throws AddEditCollectionPresenterException {
		if (code.contains("-")) {
			throw new AddEditCollectionPresenterException_CodeShouldNotContainDash();
		}
		if (getActionEdit()) {
			if (!getCode().equals(code)) {
				throw new AddEditCollectionPresenterException_CodeCodeChangeForbidden();
			}
		} else {
			if (collectionsListManager.getCollectionsExcludingSystem().contains(code)) {
				throw new AddEditCollectionPresenterException_CodeUnAvailable();
			}
		}
	}

	Set<String> updateCollection(CollectionVO entity) {
		String collectionName = entity.getName();
		try {
			recordServices().update(collectionRecord.setName(collectionName));
			return updateCollectionModules(collectionRecord.getWrappedRecord(), entity.getCode(), entity.getModules());
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	Set<String> createCollection(CollectionVO entity)
			throws AddEditCollectionPresenterException {
		Set<String> modules = entity.getModules();
		String collectionCode = entity.getCode();
		String collectionName = entity.getName();
		String language = modelLayerFactory.getConfiguration().getMainDataLanguage();
		Record record = collectionsManager
				.createCollectionInCurrentVersion(collectionCode, collectionName, Arrays.asList(language));
		return updateCollectionModules(record, collectionCode, modules);
	}

	Set<String> updateCollectionModules(Record collectionRecord, String collectionCode, Set<String> modules) {
		List<String> roles = new ArrayList<>();
		Set<String> invalidModules = new HashSet<>();
		for (String currentModule : modules) {
			Module module = modulesManager.getInstalledModule(currentModule);
			if (!modulesManager.isInstalled(module)) {
				invalidModules.addAll(modulesManager.installValidModuleAndGetInvalidOnes(module, collectionsListManager));
			}
			invalidModules.addAll(modulesManager.enableValidModuleAndGetInvalidOnes(collectionCode, module));
			roles.addAll(PluginUtil.getRolesForCreator(module));
		}
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential currentUser = userServices.getUser(getCurrentUser().getUsername());
		userServices.addUserToCollection(currentUser, collectionRecord.getId());
		User user = userServices.getUserInCollection(currentUser.getUsername(), collectionCode);
		try {
			recordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return invalidModules;
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
		/*List<String> dependencies = getModuleDependencies(moduleId);
		if (!dependencies.isEmpty()) {
			moduleCaption.append(" (");
			for (int i = 0; i < dependencies.size() - 1; i++) {
				String moduleDependency = dependencies.get(i);
				moduleCaption.append($("ConstellioModule." + moduleDependency) + ", ");
			}
			moduleCaption.append($("ConstellioModule." + dependencies.get(dependencies.size() - 1)) + ")");
		}*/
		return moduleCaption.toString();
	}

	private List<String> getModuleDependencies(String moduleId) {
		Module module = modulesManager.getInstalledModule(moduleId);
		return PluginUtil.getDependencies(module);
	}

	public boolean isModuleSelected(String moduleId, CollectionVO collectionVO) {
		return collectionVO.getModules().contains(moduleId);
	}
}
