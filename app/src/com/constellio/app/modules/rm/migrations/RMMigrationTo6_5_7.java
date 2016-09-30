package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RMMigrationTo6_5_7 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		reindexAllSubfoldersWithEnteredRetentionRule(collection, appLayerFactory);
	}

	private void reindexAllSubfoldersWithEnteredRetentionRule(String collection, AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
        SystemGlobalConfigsManager systemGlobalConfigsManager = appLayerFactory.getSystemGlobalConfigsManager();
        SearchServices searchServices = modelLayerFactory.newSearchServices();

        LogicalSearchQuery query = new LogicalSearchQuery(from(rm.folder.schemaType())
                .where(rm.folder.retentionRuleEntered()).isNotNull()
                .andWhere(rm.folder.parentFolder()).isNotNull());
        
        if (searchServices.hasResults(query)) {
            systemGlobalConfigsManager.setReindexingRequired(true);
        }
    }

}
