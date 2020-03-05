package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeCodeChangeForbidden;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeShouldNotContainDash;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_CodeUnAvailable;
import com.constellio.app.ui.pages.management.collections.AddEditCollectionPresenterException.AddEditCollectionPresenterException_MustSelectAtLeastOneModule;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.modules.PluginUtil;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

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
			List<String> languages = collectionRecord.getLanguages();
			return new CollectionVO(
					code, collectionRecord.getName(), languages, getEnabledModules(code),
					collectionRecord.getOrganizationNumber(), collectionRecord.getConservationCalendarNumber());
		} else {
			return new CollectionVO(null, null, Arrays.asList(getMainDataLanguage()));
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
			throws AddEditCollectionPresenterException, NoMoreCollectionAvalibleException {
		String code = entity.getCode();
		validateCode(code);
		validateModules(entity.getModules());
		if (!getActionEdit()) {
			try {
				createCollection(entity);
				view.showMessage($("AddEditCollectionPresenter.addConfirmation"));
			} catch (ConstellioModulesManagerException_ModuleInstallationFailed e) {
				view.showMessage($("AddEditCollectionPresenter.addConfirmationWithInvalidModules" + e.getFailedModule()));
			}
		} else {
			try {
				updateCollection(entity);
				view.showMessage($("AddEditCollectionPresenter.updateConfirmation"));

			} catch (ConstellioModulesManagerException_ModuleInstallationFailed e) {
				view.showMessage($("AddEditCollectionPresenter.updateConfirmationWithInvalidModules" + e.getFailedModule()));
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
			List<String> collectionsExcludingSystem = collectionsListManager.getCollectionsExcludingSystem();
			for (String existingCollection : collectionsExcludingSystem) {
				if (existingCollection.toLowerCase().equals(code.toLowerCase())) {
					throw new AddEditCollectionPresenterException_CodeUnAvailable();
				}
			}
		}
	}

	void updateCollection(CollectionVO entity) throws ConstellioModulesManagerException_ModuleInstallationFailed {
		String collectionName = entity.getName();
		try {
			recordServices().update(collectionRecord.setName(collectionName).setTitle(collectionName));
			updateCollectionModules(entity, collectionRecord.getWrappedRecord(), entity.getCode(), entity.getModules());
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	void createCollection(CollectionVO entity)
			throws AddEditCollectionPresenterException, NoMoreCollectionAvalibleException, ConstellioModulesManagerException_ModuleInstallationFailed {

		Set<String> modules = entity.getModules();
		String collectionCode = entity.getCode();
		String collectionName = entity.getName();
		if (StringUtils.isBlank(collectionName)) {
			collectionName = collectionCode;
		}
		Set<String> languages = entity.getSupportedLanguages();
		Record record = collectionsManager
				.createCollectionInCurrentVersion(collectionCode, collectionName, new ArrayList<>(languages));
		updateCollectionModules(entity, record, collectionCode, modules);
		runScriptsFromConfigs(collectionCode);

	}

	public void runScriptsFromConfigs(String collection) {
		SystemConfigurationsManager systemManager = modelLayerFactory.getSystemConfigurationsManager();
		for (SystemConfiguration systemConfiguration : systemManager.getAllConfigurations()) {
			Object value = systemManager.getValue(systemConfiguration);
			Object defaultValue = systemConfiguration.getDefaultValue();
			Class<? extends SystemConfigurationScript> scriptClass = systemConfiguration.getScriptClass();
			if (value != null && !value.equals(defaultValue) && scriptClass != null) {
				try {
					SystemConfigurationScript systemConfigurationScript = scriptClass.newInstance();
					systemConfigurationScript.onNewCollection(value, collection, modelLayerFactory);
				} catch (Exception e) {
					throw new RuntimeException("Instanciation exception", e);
				}
			}
		}
	}

	void updateCollectionModules(CollectionVO entity, Record collectionRecord, String collectionCode,
								 Set<String> modules)
			throws ConstellioModulesManagerException_ModuleInstallationFailed {
		List<String> roles = new ArrayList<>();
		Set<String> invalidModules = new HashSet<>();

		List<String> sortedModules = new ArrayList<>(modules);

		if (sortedModules.contains(ConstellioRMModule.ID)) {
			sortedModules.remove(TaskModule.ID);
			sortedModules.add(0, TaskModule.ID);
		}

		for (String currentModule : sortedModules) {
			Module module = modulesManager.getInstalledModule(currentModule);
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installValidModuleAndGetInvalidOnes(module, collectionsListManager);
			}
			modulesManager.enableValidModuleAndGetInvalidOnes(collectionCode, module);
			roles.addAll(PluginUtil.getRolesForCreator(module));
		}

		boolean isRMCollection = sortedModules.contains(ConstellioRMModule.ID);
		String conservationCalendarNumber = isRMCollection ? entity.getConservationCalendarNumber() : null;
		String organizationNumber = isRMCollection ? entity.getOrganizationNumber() : null;
		try {
			recordServices().update(coreSchemas(collectionRecord.getCollection()).wrapCollection(collectionRecord)
					.setConservationCalendarNumber(conservationCalendarNumber).setOrganizationNumber(organizationNumber));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}

		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential currentUser = userServices.getUser(getCurrentUser().getUsername());
		userServices.addUserToCollection(currentUser, collectionRecord.getId());
		User user = userServices.getUserInCollection(currentUser.getUsername(), collectionCode);
		try {
			recordServices().update(user.addUserRoles(roles.toArray(new String[roles.size()])).setCollectionAllAccess(true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void cancelButtonClicked() {
		navigateToBackPage();
	}

	private void navigateToBackPage() {
		view.navigate().to().manageCollections();
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
		view.navigate().to().manageCollections();
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

	public List<String> getAllLanguages() {

		List<String> languages = new ArrayList<>();
		languages.addAll(appLayerFactory.getAppLayerConfiguration().getSupportedLanguageCodes());

		if (!languages.contains(getMainDataLanguage())) {
			languages.add(getMainDataLanguage());
		}

		return languages;
	}

	public boolean isLanguageEnabled(String languageCode) {
		String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();
		boolean isNotMainLanguage = !mainDataLanguage.equals(languageCode);
		return isNotMainLanguage;
	}

	public String getMainDataLanguage() {
		return modelLayerFactory.getConfiguration().getMainDataLanguage();
	}
}
