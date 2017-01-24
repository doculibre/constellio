package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CoreMigrationTo_6_5_19 implements MigrationScript {

    @Override
	public String getVersion() {
		return "6.5.19";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		//
		new AddGlobalGroupLocallyCreatedMetadata(collection, provider, appLayerFactory).migrate();

        if (appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).hasSchema(SolrGlobalGroup.DEFAULT_SCHEMA)) {
            // Set metadata value
            final boolean locallyCreated = appLayerFactory.
                    getModelLayerFactory().
                    newUserServices().
                    canAddOrModifyUserAndGroup();

            final List<GlobalGroup> globalGroupList = appLayerFactory.
                    getModelLayerFactory().
                    getGlobalGroupsManager().
                    getAllGroups();

            for (final GlobalGroup globalGroup : globalGroupList) {
                appLayerFactory.
                        getModelLayerFactory().
                        getGlobalGroupsManager().
                        addUpdate(globalGroup.withLocallyCreated(locallyCreated));
            }
        }
    }

	private class AddGlobalGroupLocallyCreatedMetadata extends MetadataSchemasAlterationHelper {
		public AddGlobalGroupLocallyCreatedMetadata(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory) {
			super(collection, provider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder metadataSchemaTypesBuilder) {
            if (metadataSchemaTypesBuilder.hasSchemaType(SolrGlobalGroup.SCHEMA_TYPE)) {
                // Add metadata to schema
                final MetadataSchemaBuilder metadataSchemaBuilder = metadataSchemaTypesBuilder.getSchema(SolrGlobalGroup.DEFAULT_SCHEMA);
                if (!metadataSchemaBuilder.hasMetadata(SolrGlobalGroup.LOCALLY_CREATED)) {
                    metadataSchemaBuilder.createUndeletable(SolrGlobalGroup.LOCALLY_CREATED).setType(MetadataValueType.BOOLEAN);
                }
            }
		}
	}

}
