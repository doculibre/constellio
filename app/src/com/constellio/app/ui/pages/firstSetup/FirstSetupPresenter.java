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
package com.constellio.app.ui.pages.firstSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.CollectionVODataProvider.CollectionVO;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;

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
		Record collectionRecord = factories.getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(code,
				Arrays.asList(language));

		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();

		List<String> roles = new ArrayList<>();
		for (String moduleCode : modules) {
			Module module = modulesManager.getInstalledModule(moduleCode);
			modulesManager.enableModule(code, module);
			roles.addAll(module.getRolesForCreator());
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



}
