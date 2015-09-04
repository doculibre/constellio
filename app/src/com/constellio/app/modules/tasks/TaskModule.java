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
package com.constellio.app.modules.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.modules.tasks.extensions.TaskSchemasExtension;
import com.constellio.app.modules.tasks.extensions.TaskStatusSchemasExtension;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_0_7;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;

public class TaskModule implements InstallableModule {
	public static final String ID = "tasks";
	public static final String NAME = "Tasks";

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return Arrays.asList(
				(MigrationScript) new TasksMigrationTo5_0_7());
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new TasksNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		registerManagers(collection, appLayerFactory);
		setupModelLayerExtensions(collection, appLayerFactory);
	}

	private void setupModelLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerCollectionExtensions extensions = appLayerFactory.getModelLayerFactory().getExtensions()
				.forCollection(collection);
		extensions.recordExtensions.add(new TaskSchemasExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new TaskStatusSchemasExtension(collection, appLayerFactory));

		RecordsCache cache = appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(collection);

		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		cache.configureCache(CacheConfig.volatileCache(taskSchemas.userTask.schemaType(), 1000));
		//cache.configureCache(CacheConfig.permanentCache(taskSchemas.ddvTaskStatus.schemaType()));
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
		// Tasks provide no demo data for now
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return TaskConfigs.configurations;
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return TasksPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getPublisher() {
		return DOCULIBRE;
	}

	private void registerManagers(String collection, AppLayerFactory appLayerFactory) {
		appLayerFactory.registerManager(collection, ID, TaskReminderEmailManager.ID,
				new TaskReminderEmailManager(appLayerFactory, collection));
	}
}
