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
package com.constellio.app.ui.pages.setup;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.rm.ui.builders.UserToVOBuilder;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_CannotSelectBothRMandES;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_MustSelectAtLeastOneModule;
import com.constellio.app.ui.pages.setup.ConstellioSetupPresenterException.ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule;
import com.constellio.model.entities.modules.Module;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class ConstellioSetupPresenter extends BasePresenter<ConstellioSetupView> {

	private ConstellioSetupView view;

	private UserToVOBuilder userToVOBuilder = new UserToVOBuilder();

	public ConstellioSetupPresenter(ConstellioSetupView view) {
		super(view);
		this.view = view;

		ConstellioFactories factories = view.getConstellioFactories();
		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();
		List<? extends Module> installedModules = modulesManager.getAllModules();

		List<String> localeCodes = i18n.getSupportedLanguages();

		List<String> moduleIds = new ArrayList<>();
		for (Module installedModule : installedModules) {
			String moduleId = installedModule.getId();
			moduleIds.add(moduleId);
		}

		view.setLocaleCodes(localeCodes);
		view.setModuleIds(moduleIds);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public String getLogoTarget() {
		SystemConfigurationsManager manager = modelLayerFactory.getSystemConfigurationsManager();
		String linkTarget = manager.getValue(ConstellioEIMConfigs.LOGO_LINK);
		if (StringUtils.isBlank(linkTarget)) {
			linkTarget = "http://www.constellio.com";
		}
		return linkTarget;
	}

	public void saveRequested(String setupLocaleCode, List<String> modules, String collectionTitle, String collectionCode,
			String adminPassword)
			throws ConstellioSetupPresenterException {
		if (modules.contains("rm") && modules.contains("es")) {
			throw new ConstellioSetupPresenterException_CannotSelectBothRMandES();
		} else if (modules.isEmpty()) {
			throw new ConstellioSetupPresenterException_MustSelectAtLeastOneModule();
		} else if(modules.size() == 1 && modules.contains("tasks")) {
			throw new ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule();
		}
		view.showMessage($("ConstellioSetupView.setupInProgress"));
		
		ConstellioFactories factories = view.getConstellioFactories();
		
		
		setSystemLanguage(setupLocaleCode);
		Record collectionRecord = factories.getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(
				collectionCode, Arrays.asList(setupLocaleCode));
		Collection collection = new Collection(collectionRecord,
				modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectionCode));
		collection.setTitle(collectionTitle);

		ConstellioModulesManager modulesManager = factories.getAppLayerFactory().getModulesManager();

		List<String> roles = new ArrayList<>();
		for (String moduleCode : modules) {
			Module module = modulesManager.getInstalledModule(moduleCode);
			modulesManager.installModule(module, factories.getModelLayerFactory().getCollectionsListManager());
			modulesManager.enableModule(collectionCode, module);
			roles.addAll(module.getRolesForCreator());
			((InstallableModule) module).addDemoData(collectionCode, appLayerFactory);
		}

		ModelLayerFactory modelLayerFactory = factories.getModelLayerFactory();

		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential adminCredential = new UserCredential("admin", "System", "Admin", "admin@administration.com",
				new ArrayList<String>(), Arrays.asList(collectionCode),
				UserCredentialStatus.ACTIVE).withSystemAdminPermission();
		userServices.addUpdateUserCredential(adminCredential);
		userServices.addUserToCollection(adminCredential, collectionCode);
		User user = userServices.getUserRecordInCollection("admin", collectionCode);
		if (StringUtils.isBlank(adminPassword)) {
			adminPassword = "password";
		}
		modelLayerFactory.getPasswordFileAuthenticationService().changePassword("admin", adminPassword);
		try {
			modelLayerFactory.newRecordServices().update(user.setUserRoles(roles).setCollectionAllAccess(true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		UserVO userVO = userToVOBuilder.build(user.getWrappedRecord(), VIEW_MODE.DISPLAY);
		SessionContext sessionContext = view.getSessionContext();
		sessionContext.setCurrentCollection(collectionCode);
		sessionContext.setCurrentLocale(new Locale(setupLocaleCode));
		sessionContext.setCurrentUser(userVO);

		view.updateUI();
	}

	public void setSystemLanguage(String languageCode) {
		modelLayerFactory.getConfiguration().setMainDataLanguage(languageCode);
	}

}
