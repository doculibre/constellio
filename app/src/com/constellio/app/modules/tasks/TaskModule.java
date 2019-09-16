package com.constellio.app.modules.tasks;

import com.constellio.app.entities.modules.ComboMigrationScript;
import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleWithComboMigration;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.core.LockedRecordsExtension;
import com.constellio.app.modules.rm.extensions.imports.TaskImportExtension;
import com.constellio.app.modules.tasks.caches.IncompleteTasksUserCache;
import com.constellio.app.modules.tasks.caches.UnreadTasksUserCache;
import com.constellio.app.modules.tasks.extensions.TaskRecordAppExtension;
import com.constellio.app.modules.tasks.extensions.TaskRecordExtension;
import com.constellio.app.modules.tasks.extensions.TaskRecordNavigationExtension;
import com.constellio.app.modules.tasks.extensions.TaskSchemaTypesPageExtension;
import com.constellio.app.modules.tasks.extensions.TaskStatusSchemasExtension;
import com.constellio.app.modules.tasks.extensions.TaskUserProfileFieldsExtension;
import com.constellio.app.modules.tasks.extensions.WorkflowRecordExtension;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.extensions.schema.TaskTrashSchemaExtension;
import com.constellio.app.modules.tasks.migrations.TasksMigrationCombo;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_0_7;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_1_2;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo5_1_3;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo6_0;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo6_5_33;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_0;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_2;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_5;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_5_0_1;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_6_1;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_6_3;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_6_6;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_6_6_1;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_7;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_7_3;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_7_4;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo7_7_4_1;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo8_1_2;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo8_1_4;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo8_1_5;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo8_2_42;
import com.constellio.app.modules.tasks.migrations.TasksMigrationTo9_0;
import com.constellio.app.modules.tasks.model.TaskRecordsCachesHook;
import com.constellio.app.modules.tasks.model.managers.TaskReminderEmailManager;
import com.constellio.app.modules.tasks.navigation.TasksNavigationConfiguration;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.background.AlertOverdueTasksBackgroundAction;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.background.ModelLayerBackgroundThreadsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.data.threads.BackgroundThreadConfiguration.repeatingAction;
import static com.constellio.data.threads.BackgroundThreadExceptionHandling.CONTINUE;
import static org.joda.time.Duration.standardMinutes;

public class TaskModule implements InstallableSystemModule, ModuleWithComboMigration {
	public static final String ID = "tasks";
	public static final String NAME = "Tasks";

	@Override
	public List<MigrationScript> getMigrationScripts() {

		List<MigrationScript> scripts = new ArrayList<>();
		scripts.add(new TasksMigrationTo5_0_7());
		scripts.add(new TasksMigrationTo5_1_2());
		scripts.add(new TasksMigrationTo5_1_3());
		scripts.add(new TasksMigrationTo6_0());
		scripts.add(new TasksMigrationTo6_5_33());
		scripts.add(new TasksMigrationTo7_0());
		scripts.add(new TasksMigrationTo7_2());
		scripts.add(new TasksMigrationTo7_5());
		scripts.add(new TasksMigrationTo7_5_0_1());
		scripts.add(new TasksMigrationTo7_6_1());
		scripts.add(new TasksMigrationTo7_6_3());
		scripts.add(new TasksMigrationTo7_6_6());
		scripts.add(new TasksMigrationTo7_6_6_1());
		scripts.add(new TasksMigrationTo7_7());
		scripts.add(new TasksMigrationTo7_7_3());
		scripts.add(new TasksMigrationTo7_7_4());
		scripts.add(new TasksMigrationTo7_7_4_1());
		scripts.add(new TasksMigrationTo8_1_2());
		scripts.add(new TasksMigrationTo8_1_4());
		scripts.add(new TasksMigrationTo8_1_5());
		scripts.add(new TasksMigrationTo8_2_42());
		scripts.add(new TasksMigrationTo9_0());

		return scripts;
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		TasksNavigationConfiguration.configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		registerManagers(collection, appLayerFactory);
		setupAppLayerExtensions(collection, appLayerFactory);
		setupModelLayerExtensions(collection, appLayerFactory);
		setupBackgroundThreadsManager(collection, appLayerFactory);
	}

	private void setupBackgroundThreadsManager(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerBackgroundThreadsManager manager = appLayerFactory.getModelLayerFactory()
				.getModelLayerBackgroundThreadsManager();
		manager.configureBackgroundThreadConfiguration(repeatingAction("alertOverdueTasksBackgroundAction-" + collection,
				new AlertOverdueTasksBackgroundAction(appLayerFactory, collection))
				.executedEvery(standardMinutes(30)).handlingExceptionWith(CONTINUE));
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		extensions.recordAppExtensions.add(new TaskRecordAppExtension(collection, appLayerFactory));
		extensions.recordNavigationExtensions.add(new TaskRecordNavigationExtension(appLayerFactory, collection));
		extensions.schemaTypesPageExtensions.add(new TaskSchemaTypesPageExtension());
		extensions.pagesComponentsExtensions.add(new TaskUserProfileFieldsExtension(collection, appLayerFactory));

		extensions.registerModuleExtensionsPoint(ID, new TaskModuleExtensions(appLayerFactory));
	}

	private void setupModelLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerCollectionExtensions extensions = appLayerFactory.getModelLayerFactory().getExtensions()
				.forCollection(collection);
		extensions.recordImportExtensions.add(new TaskImportExtension(collection, appLayerFactory.getModelLayerFactory()));
		extensions.recordExtensions.add(new TaskRecordExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new TaskStatusSchemasExtension(collection, appLayerFactory));
		extensions.recordExtensions.add(new WorkflowRecordExtension(collection, appLayerFactory));
		extensions.schemaExtensions.add(new TaskTrashSchemaExtension());

		//TODO Francis : Move in Constellio core's init
		extensions.recordExtensions.add(new LockedRecordsExtension(collection, appLayerFactory));

		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);

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

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		TasksNavigationConfiguration.configureNavigation(appLayerFactory.getNavigatorConfigurationService());
		appLayerFactory.getModelLayerFactory().getCachesManager().register(new UnreadTasksUserCache(appLayerFactory));
		appLayerFactory.getModelLayerFactory().getCachesManager().register(new IncompleteTasksUserCache(appLayerFactory));
		appLayerFactory.getModelLayerFactory().getRecordsCaches().register(new TaskRecordsCachesHook());
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}

	@Override
	public ComboMigrationScript getComboMigrationScript() {
		return new TasksMigrationCombo();
	}
}
