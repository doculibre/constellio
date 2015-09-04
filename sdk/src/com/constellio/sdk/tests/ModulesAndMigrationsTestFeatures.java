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
package com.constellio.sdk.tests;

import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToInstall;
import com.constellio.app.services.extensions.ConstellioModulesManagerRuntimeException.FailedToStart;
import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserPhotosServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.setups.Users;

public class ModulesAndMigrationsTestFeatures {

	Users users;

	FactoriesTestFeatures factoriesTestFeatures;

	String collection;

	List<InstallableModule> modules = new ArrayList<>();

	public ModulesAndMigrationsTestFeatures(FactoriesTestFeatures factoriesTestFeatures, String collection) {
		this.factoriesTestFeatures = factoriesTestFeatures;
		this.collection = collection;
	}

	public ModulesAndMigrationsTestFeatures withModule(Class<? extends InstallableModule> moduleClass) {
		AppLayerFactory appFactory = factoriesTestFeatures.getConstellioFactories().getAppLayerFactory();
		ModelLayerFactory modelFactory = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory();
		ConstellioPluginManager pluginManager = appFactory.getPluginManager();

		try {
			this.modules.add(moduleClass.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		doReturn(modules).when(pluginManager).getPlugins(InstallableModule.class);
		ConstellioModulesManager modulesManager = appFactory.getModulesManager();
		try {
			InstallableModule module = moduleClass.newInstance();
			if (!modulesManager.isInstalled(module)) {
				modulesManager.installModule(module, modelFactory.getCollectionsListManager());
			}
			modulesManager.enableModule(collection, module);

		} catch (FailedToInstall failedToStart) {
			throw new RuntimeException(failedToStart);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (FailedToStart failedToStart) {
			throw new RuntimeException(failedToStart);
		}
		return this;
	}

	public ModulesAndMigrationsTestFeatures withConstellioESModule() {
		return withModule(ConstellioESModule.class);
	}

	public ModulesAndMigrationsTestFeatures withConstellioRMModule() {
		withTaskModule();
		return withModule(ConstellioRMModule.class);
	}

	public ModulesAndMigrationsTestFeatures withTaskModule() {
		return withModule(TaskModule.class);
	}

	public ModulesAndMigrationsTestFeatures withAdmin() {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		UserPhotosServices userPhotosServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory()
				.newUserPhotosServices();
		if (users == null) {
			users = new Users();
			users.setUp(userServices).withPhotos(userPhotosServices);
		}
		userServices.addUserToCollection(users.admin(), collection);
		return this;
	}

	public ModulesAndMigrationsTestFeatures withAllTestUsers() {
		UserServices userServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory().newUserServices();
		UserPhotosServices userPhotosServices = factoriesTestFeatures.getConstellioFactories().getModelLayerFactory()
				.newUserPhotosServices();
		if (users == null) {
			users = new Users();
			users.setUp(userServices).withPhotos(userPhotosServices);
		}
		userServices.addUserToCollection(users.admin(), collection);
		userServices.addUserToCollection(users.alice(), collection);
		userServices.addUserToCollection(users.bob(), collection);
		userServices.addUserToCollection(users.charles(), collection);
		userServices.addUserToCollection(users.chuckNorris(), collection);
		userServices.addUserToCollection(users.dakotaLIndien(), collection);
		userServices.addUserToCollection(users.edouardLechat(), collection);
		userServices.addUserToCollection(users.gandalfLeblanc(), collection);
		userServices.addUserToCollection(users.robin(), collection);
		userServices.addUserToCollection(users.sasquatch(), collection);
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
