package com.constellio.app.modules.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.tasks.extensions.TaskRecordAppExtension;
import com.constellio.app.modules.tasks.extensions.TaskRecordNavigationExtension;
import com.constellio.app.modules.tasks.extensions.TaskSchemasExtension;
import com.constellio.app.modules.tasks.extensions.TaskStatusSchemasExtension;
import com.constellio.app.modules.tasks.extensions.WorkflowRecordExtension;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_0_7;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_1_2;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_1_3;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo6_0;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;
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
				new TasksMigrationTo5_0_7(),
				new TasksMigrationTo5_1_2(),
				new TasksMigrationTo5_1_3(),
				new TasksMigrationTo6_0());
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new TasksNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		registerManagers(collection, appLayerFactory);
		setupAppLayerExtensions(collection, appLayerFactory);
		setupModelLayerExtensions(collection, appLayerFactory);

	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		extensions.recordAppExtensions.add(new TaskRecordAppExtension(collection, appLayerFactory));
		extensions.recordNavigationExtensions.add(new TaskRecordNavigationExtension());
	}

	private void setupModelLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerCollectionExtensions extensions = appLayerFactory.getModelLayerFactory().getExtensions()
				.forCollection(collection);
		extensions.recordExtensions.add(new TaskSchemasExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new TaskStatusSchemasExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new WorkflowRecordExtension(collection, appLayerFactory));

		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

		RecordsCache cache = appLayerFactory.getModelLayerFactory().getRecordsCaches().getCache(collection);
		cache.configureCache(CacheConfig.volatileCache(taskSchemas.userTask.schemaType(), 1000));
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
		// Tasks provide no demo data for now
	}

	@Override
	public boolean isComplementary() {
		return false;
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
