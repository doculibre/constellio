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

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.extensions.RMCheckInAlertsRecordExtension;
import com.constellio.app.modules.rm.extensions.RMDownloadContentVersionLinkExtension;
import com.constellio.app.modules.rm.extensions.RMEmailDocumentRecordExtension;
import com.constellio.app.modules.rm.extensions.RMGenericRecordPageExtension;
import com.constellio.app.modules.rm.extensions.RMModulePageExtension;
import com.constellio.app.modules.rm.extensions.RMOldSchemasBlockageRecordExtension;
import com.constellio.app.modules.rm.extensions.RMSchemasLogicalDeleteExtension;
import com.constellio.app.modules.rm.extensions.RMTaxonomyPageExtension;
import com.constellio.app.modules.rm.extensions.RMUserRecordExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_1;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_2;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_3;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_4;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_4_1;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_5;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_6;
import com.constellio.app.modules.rm.migrations.RMMigrationTo5_0_7;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;

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
		return "Constellio";
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return asList(
				new RMMigrationTo5_0_1(),
				new RMMigrationTo5_0_2(),
				new RMMigrationTo5_0_3(),
				new RMMigrationTo5_0_4(),
				new RMMigrationTo5_0_4_1(),
				new RMMigrationTo5_0_5(),
				new RMMigrationTo5_0_6(),
				new RMMigrationTo5_0_7());
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return Collections.unmodifiableList(RMConfigs.configurations);
	}

	@Override
	public Map<String, List<String>> getPermissions() {
		return RMPermissionsTo.PERMISSIONS.getGrouped();
	}

	@Override
	public List<String> getRolesForCreator() {
		return asList(RMRoles.RGD);
	}

	@Override
	public List<String> getDependencies() {
		return asList(TaskModule.ID);
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
		new RMNavigationConfiguration().configureNavigation(config);
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
		setupModelLayerExtensions(collection, appLayerFactory.getModelLayerFactory());
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		Transaction transaction = new Transaction();

		AdministrativeUnit adminUnit = rm.newAdministrativeUnit().setCode("1").setTitle($("RMDemoData.adminUnit"));
		transaction.add(adminUnit);

		CopyRetentionRule principal888_5_C = CopyRetentionRule.newPrincipal(asList(rm.PA(), rm.DM()), "888-5-C");
		CopyRetentionRule secondary888_0_D = CopyRetentionRule.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		RetentionRule retentionRule = rm.newRetentionRule().setCode("R1").setTitle($("RMDemoData.retentionRule"))
				.setAdministrativeUnits(asList(adminUnit.getId())).setApproved(true)
				.setCopyRetentionRules(asList(principal888_5_C, secondary888_0_D));
		transaction.add(retentionRule);

		Category category10 = rm.newCategory().setCode("10").setTitle($("RMDemoData.category10")).setRetentionRules(
				asList(retentionRule.getId()));
		transaction.add(category10);

		transaction.add(rm.newCategory().setCode("11").setTitle($("RMDemoData.category11"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		transaction.add(rm.newCategory().setCode("12").setTitle($("RMDemoData.category12"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		transaction.add(rm.newCategory().setCode("13").setTitle($("RMDemoData.category13"))
				.setParent(category10.getId()).setRetentionRules(asList(retentionRule.getId())));

		try {
			appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);

		extensions.schemaTypeAccessExtensions.add(new RMGenericRecordPageExtension());
		extensions.taxonomyAccessExtensions.add(new RMTaxonomyPageExtension(collection));
		extensions.pageAccessExtensions.add(new RMModulePageExtension());
		extensions.downloadContentVersionLinkExtensions.add(new RMDownloadContentVersionLinkExtension());
	}

	private void setupModelLayerExtensions(String collection, ModelLayerFactory modelLayerFactory) {
		ModelLayerCollectionExtensions extensions = modelLayerFactory.getExtensions().forCollection(collection);

		extensions.recordExtensions.add(new RMSchemasLogicalDeleteExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMUserRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMEmailDocumentRecordExtension(collection, modelLayerFactory));
		extensions.recordExtensions.add(new RMOldSchemasBlockageRecordExtension());
		extensions.recordExtensions.add(new RMCheckInAlertsRecordExtension(collection, modelLayerFactory));
		extensions.recordImportExtensions.add(new RetentionRuleImportExtension(collection, modelLayerFactory));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(collection);

		for (MetadataSchemaType type : rm.valueListSchemaTypes()) {
			if (!cache.isConfigured(type)) {
				cache.configureCache(CacheConfig.permanentCache(type));
			}
		}

		cache.configureCache(CacheConfig.permanentCache(rm.retentionRuleSchemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.uniformSubdivisionSchemaType()));
		cache.configureCache(CacheConfig.permanentCache(rm.containerRecordSchemaType()));
		cache.configureCache(CacheConfig.volatileCache(rm.folderSchemaType(), 1000));
		cache.configureCache(CacheConfig.volatileCache(rm.documentSchemaType(), 1000));

	}

}
