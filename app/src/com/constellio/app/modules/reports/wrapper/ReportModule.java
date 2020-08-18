package com.constellio.app.modules.reports.wrapper;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.migrations.RMMigrationTo7_1;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.scripts.CoreMigrationTo_7_1;
import com.constellio.model.entities.configs.SystemConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @deprecated No longer a module, use ReportsPlugin instead.
 */
@Deprecated
public class ReportModule implements InstallableModule {
	public static final String ID = "ReportModule";
	public static final String TITLE = "Module de rapport";

	@Override
	public List<MigrationScript> getMigrationScripts() {
		List<MigrationScript> scripts = new ArrayList<>();
		scripts.add(new CoreMigrationTo_7_1());
		scripts.add(new RMMigrationTo7_1());
		return scripts;
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		ReportNavigationConfiguration.configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public boolean isComplementary() {
		return false;
	}

	@Override
	public List<String> getDependencies() {
		return Arrays.asList(ConstellioRMModule.ID);
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return new ArrayList<>();
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return null;
	}

	@Override
	public List<String> getRolesForCreator() {
		return null;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return TITLE;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}
}
