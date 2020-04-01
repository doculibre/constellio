package com.constellio.app.modules.scanner;

import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.scanner.navigation.ScannerNavigationConfiguration;
import com.constellio.app.modules.scanner.servlets.ScanErrorServlet;
import com.constellio.app.modules.scanner.servlets.UploadScannedImageServlet;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.entities.configs.SystemConfiguration;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@PluginImplementation
public class ConstellioScannerModule implements InstallableSystemModule {

	private final static Logger LOGGER = LoggerFactory.getLogger(ConstellioScannerModule.class);

	public final static String ID = "scanner";

	public final static String NAME = "Scanner";

	public static final String SCANNER = "scanner";
	private static final String SCANNER_PATH = "/scanner";

	@Override
	public List<MigrationScript> getMigrationScripts() {
		List<MigrationScript> migrations = new ArrayList<>();

		return migrations;
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new ScannerNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		LOGGER.info("Scanner Module is starting for collection " + collection);

		setupAppLayerExtensions(collection, appLayerFactory);
	}

	private void registerServlets() {
		ApplicationStarter.registerServlet(SCANNER_PATH + "/scanError", new ScanErrorServlet());
		ApplicationStarter.registerServlet(SCANNER_PATH + "/uploadScannedImage", new UploadScannedImageServlet());
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		//		extensions.selectionPanelExtensions.add(new AgentSelectionPanelExtension(appLayerFactory, collection));
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public boolean isComplementary() {
		return true;
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>(asList(ConstellioRMModule.ID));
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return new ArrayList<>();
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return new HashMap<>();
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
		return "Constellio";
	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		registerServlets();
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {

	}
}
