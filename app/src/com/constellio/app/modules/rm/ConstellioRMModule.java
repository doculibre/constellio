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
package com.constellio.app.modules.rm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.extensions.AppLayerCollectionEventsListeners;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.AdministrativeUnitRecordSynchronization;
import com.constellio.app.modules.rm.extensions.RMModulePageAccessExtension;
import com.constellio.app.modules.rm.extensions.RMSchemaTypeAccessExtension;
import com.constellio.app.modules.rm.extensions.RMTaxonomyTypeAccessExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_1;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_2;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_3;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_4;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_4_1;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.extensions.ModelLayerCollectionEventsListeners;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ConstellioRMModule implements InstallableModule {
	public static final String ID = "rm";
	public static final String NAME = "Constellio RM";

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
		return "DocuLibre";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		List<MigrationScript> migrationScripts = new ArrayList<>();

		migrationScripts.add(new RMMigrationTo5_0_1());
		migrationScripts.add(new RMMigrationTo5_0_2());
		migrationScripts.add(new RMMigrationTo5_0_3());
		migrationScripts.add(new RMMigrationTo5_0_4());
		migrationScripts.add(new RMMigrationTo5_0_4_1());

		return migrationScripts;
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return Collections.unmodifiableList(RMConfigs.configurations);
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return RMPermissionsTo.getGroupedPermissions();
	}

	@Override
	public List<String> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		setupModelLayerExtensions(collection, appLayerFactory.getModelLayerFactory());
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionEventsListeners listeners = appLayerFactory.getExtensions().getCollectionListeners(collection);

		listeners.schemaTypeAccessExtensions.add(new RMSchemaTypeAccessExtension());
		listeners.taxonomyAccessExtensions.add(new RMTaxonomyTypeAccessExtension());
		listeners.pageAccessExtensions.add(new RMModulePageAccessExtension());
	}

	private void setupModelLayerExtensions(String collection, ModelLayerFactory modelLayerFactory) {
		ModelLayerCollectionEventsListeners listeners = modelLayerFactory.getExtensions().getCollectionListeners(collection);

		AdministrativeUnitRecordSynchronization synchronization =
				new AdministrativeUnitRecordSynchronization(collection, modelLayerFactory);
		synchronization.registerTo(listeners);

		listeners.recordImportBehaviors.add(new RetentionRuleImportExtension(collection, modelLayerFactory));
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {

	}
}
