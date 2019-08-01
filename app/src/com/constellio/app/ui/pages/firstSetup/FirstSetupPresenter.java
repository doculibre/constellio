package com.constellio.app.ui.pages.firstSetup;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.modules.Module;

import java.util.ArrayList;
import java.util.List;

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


	List<String> getRolesForCreator(Module module) {
		return (module.getRolesForCreator() == null) ? new ArrayList<String>() : module.getRolesForCreator();
	}

}
