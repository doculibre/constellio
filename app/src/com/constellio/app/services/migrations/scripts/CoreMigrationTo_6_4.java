package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.search.SearchResultsViewMode;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_4 implements MigrationScript {
    @Override
    public String getVersion() {
        return "6.4";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {
        //FIXME is called several times for the different installed modules!!
        new CoreSchemaAlterationFor6_4(collection, provider, appLayerFactory).migrate();
        //initializeUsersLanguages(collection, appLayerFactory);
    }

    private class CoreSchemaAlterationFor6_4  extends MetadataSchemasAlterationHelper {
        public CoreSchemaAlterationFor6_4(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory) {
            super(collection, provider, appLayerFactory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(SavedSearch.SCHEMA_TYPE).createUndeletable(SavedSearch.RESULTS_VIEW_MODE)
                    .setType(MetadataValueType.STRING).setDefaultValue(SearchResultsViewMode.DETAILED);
        }
    }
}
