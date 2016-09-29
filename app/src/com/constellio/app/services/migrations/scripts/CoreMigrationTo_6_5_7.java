package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CoreMigrationTo_6_5_7 implements MigrationScript {

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
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SystemGlobalConfigsManager systemGlobalConfigsManager = appLayerFactory.getSystemGlobalConfigsManager();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchemaType folderSchemaType = metadataSchemaTypes.getSchemaType(Folder.SCHEMA_TYPE);
		Metadata retentionRuleEntered = folderSchemaType.getMetadata(Folder.RETENTION_RULE_ENTERED);
		Metadata parentFolder = folderSchemaType.getMetadata(Folder.PARENT_FOLDER);
		
		LogicalSearchQuery query = new LogicalSearchQuery(from(folderSchemaType).where(retentionRuleEntered).isNotNull().andWhere(parentFolder).isNotNull());
		if (searchServices.hasResults(query)) {
			systemGlobalConfigsManager.setReindexingRequired(true);
		}
	}

}
