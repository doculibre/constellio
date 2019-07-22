package com.constellio.app.ui.pages.firstSetup;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.collections.exceptions.NoMoreCollectionAvalibleException;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FirstSetupPresenter {

	private ConstellioFactories factories;

	public FirstSetupPresenter(ConstellioFactories factories) {
		this.factories = factories;
	}

	public List<Module> getAvailableModules() {
		// NOTE: I may have to change the typecast here.
		return (List<Module>) factories.getAppLayerFactory().getModulesManager().getAllModules();
	}

	public List<Language> getAvailableSystemLanguages() {
		// NOTE: The initial collection will have the selected system langauge
		return Language.getAvailableLanguages();
	}

	public CollectionVO newCollectionVO() {
		return new CollectionVO();
	}

	public void setSystemLanguage(String languageCode) {
		FoldersLocator foldersLocator = new FoldersLocator();
		//		foldersLocator.
	}

	private void createCollection(String code, Set<String> modules, String language) {
		try {
			Record collectionRecord = factories.getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(code,
					Arrays.asList(language));
		} catch (NoMoreCollectionAvalibleException noMoreCollectionAvalibleException) {
			noMoreCollectionAvalibleException.printStackTrace();
			throw new ImpossibleRuntimeException("should not happen this method is not even called");
		}

		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();

		List<String> roles = new ArrayList<>();
		for (String moduleCode : modules) {
			Module module = modulesManager.getInstalledModule(moduleCode);
			modulesManager.enableValidModuleAndGetInvalidOnes(code, module);
			roles.addAll(getRolesForCreator(module));
		}

		ModelLayerFactory modelLayerFactory = factories.getModelLayerFactory();

		// TODO Create admin user with specified password
		//   |--> But how will the user provide the password?

		//		UserServices userServices = modelLayerFactory.newUserServices();
		//		User user = userServices.getUserInCollection("admin", code);
		//		try {
		//			modelLayerFactory.newRecordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
		//		} catch (RecordServicesException e) {
		//			throw new RuntimeException(e);
		//		}
	}

	List<String> getRolesForCreator(Module module) {
		return (module.getRolesForCreator() == null) ? new ArrayList<String>() : module.getRolesForCreator();
	}

}
