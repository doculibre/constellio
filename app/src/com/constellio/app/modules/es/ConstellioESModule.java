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
package com.constellio.app.modules.es;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.es.constants.ESPermissionsTo;
import com.constellio.app.modules.es.extensions.ESTaxonomyPageExtension;
import com.constellio.app.modules.es.migrations.ESMigrationTo5_0_7;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCache;

public class ConstellioESModule implements InstallableModule {
	public static final String ID = "es";
	public static final String NAME = "Constellio Enterprise Search (beta)";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return Arrays.asList(
				(MigrationScript) new ESMigrationTo5_0_7());
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return Collections.unmodifiableList(ESConfigs.configurations);
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return ESPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return new ArrayList<>();
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new ESNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {

		registerManagers(collection, appLayerFactory);

		setupModelLayerExtensions(collection, appLayerFactory.getModelLayerFactory());
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	private void registerManagers(String collection, AppLayerFactory appLayerFactory) {
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		appLayerFactory.registerManager(collection, ConstellioESModule.ID, ConnectorManager.ID, new ConnectorManager(es));
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
		// ES provides no demo data for now
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);

		extensions.taxonomyAccessExtensions.add(new ESTaxonomyPageExtension(collection));
	}

	private void setupModelLayerExtensions(String collection, ModelLayerFactory modelLayerFactory) {
		RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);
		cache.removeCache(ConnectorSmbFolder.SCHEMA_TYPE);
	}
}