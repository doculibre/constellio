package com.constellio.sdk.tests;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.restapi.ConstellioRestApiModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToInstall;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToStart;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.setups.Users;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doReturn;

public class ModulesAndMigrationsTestFeatures {

	Users users;

	FactoriesTestFeatures factoriesTestFeatures;

	String collection;

	List<InstallableModule> modules = new ArrayList<>();

	boolean mockedAvailableModules = true;

	public ModulesAndMigrationsTestFeatures(FactoriesTestFeatures factoriesTestFeatures, String collection) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.collection = collection;
	}

	public ModulesAndMigrationsTestFeatures withModule(Class<? extends InstallableModule> moduleClass) {

		AppLayerFactory mainAppLayerFactory = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory();
		SDKConstellioFactoriesInstanceProvider instanceProvider = (SDKConstellioFactoriesInstanceProvider) ConstellioFactories.instanceProvider;

		if (mockedAvailableModules) {
			try {
				this.modules.add(moduleClass.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		for (ConstellioFactories factories : instanceProvider.getAllInstances()) {

			AppLayerFactory appFactory = factories.getAppLayerFactory();
			ModelLayerFactory modelFactory = factories.getModelLayerFactory();
			ConstellioPluginManager pluginManager = appFactory.getPluginManager();

			if (mockedAvailableModules) {
				doReturn(modules).when(pluginManager).getRegistredModulesAndActivePlugins();
			}
		}
		ConstellioModulesManager modulesManager = mainAppLayerFactory.getModulesManager();
		try {
			InstallableModule module = moduleClass.newInstance();
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installValidModuleAndGetInvalidOnes(module, mainAppLayerFactory.getModelLayerFactory()
						.getCollectionsListManager());
			}
			if (!modulesManager.isModuleEnabled(collection, module)) {
				modulesManager.enableValidModuleAndGetInvalidOnes(collection, module);
			}

		} catch (FailedToInstall failedToStart) {
			throw new RuntimeException(failedToStart);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (FailedToStart failedToStart) {
			throw new RuntimeException(failedToStart);
		} catch (ConstellioModulesManagerException_ModuleInstallationFailed e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public ModulesAndMigrationsTestFeatures withMockedAvailableModules(boolean mockedAvailableModules) {
		this.mockedAvailableModules = mockedAvailableModules;
		return this;
	}

	public ModulesAndMigrationsTestFeatures withConstellioESModule() {
		return withModule(ConstellioESModule.class);
	}

	public ModulesAndMigrationsTestFeatures withConstellioRMModule() {
		withTaskModule();
		return withModule(ConstellioRMModule.class);
	}

	public ModulesAndMigrationsTestFeatures withConstellioRestApiModule() {
		return withModule(ConstellioRestApiModule.class);
	}

	public ModulesAndMigrationsTestFeatures withTaskModule() {
		return withModule(TaskModule.class);
	}

	public ModulesAndMigrationsTestFeatures withRobotsModule() {
		return withModule(ConstellioRobotsModule.class);
	}

	public ModulesAndMigrationsTestFeatures withAdmin() {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		UserPhotosServices userPhotosServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory()
				.newUserPhotosServices();
		if (users == null) {
			users = new Users();
			users.setUp(userServices).withPhotos(userPhotosServices);
		}
		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection(collection));
		return this;
	}

	public ModulesAndMigrationsTestFeatures withAllTestUsers() {
		return withAllTest(new Users());
	}

	public ModulesAndMigrationsTestFeatures withAllTest(Users usingUsers) {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		UserPhotosServices userPhotosServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory()
				.newUserPhotosServices();
		if (this.users == null) {
			users = usingUsers;
			users.setUp(userServices).withPhotos(userPhotosServices);
		}
		userServices.execute(users.admin().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.alice().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.bob().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.charles().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.dakotaLIndien().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.edouardLechat().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.gandalfLeblanc().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.robin().getUsername(), (req) -> req.addToCollection(collection));
		userServices.execute(users.sasquatch().getUsername(), (req) -> req.addToCollection(collection));
		return this;
	}

	public ModulesAndMigrationsTestFeatures andUsersWithReadAccess(String... usersWithReadAccess) {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		RecordServices recordServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		for (String userWithReadAccess : usersWithReadAccess) {
			User user = userServices.getUserInCollection(userWithReadAccess, collection);
			user.setCollectionReadAccess(true);
			transaction.add(user.getWrappedRecord());
		}
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	public ModulesAndMigrationsTestFeatures andUsersWithWriteAccess(String... usersWithWriteAccess) {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		RecordServices recordServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		for (String userWithReadAccess : usersWithWriteAccess) {
			User user = userServices.getUserInCollection(userWithReadAccess, collection);
			user.setCollectionReadAccess(true);
			user.setCollectionWriteAccess(true);
			transaction.add(user.getWrappedRecord());
		}
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	public ModulesAndMigrationsTestFeatures andUsersWithWriteAndDeleteAccess(String... usersWithWriteAndDeleteAccess) {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		RecordServices recordServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		for (String userWithReadAccess : usersWithWriteAndDeleteAccess) {
			User user = userServices.getUserInCollection(userWithReadAccess, collection);
			user.setCollectionReadAccess(true);
			user.setCollectionWriteAccess(true);
			user.setCollectionDeleteAccess(true);
			transaction.add(user.getWrappedRecord());
		}
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}
}
