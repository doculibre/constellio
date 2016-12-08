package com.constellio.app.services.migrations.scripts;

import static com.constellio.app.services.migrations.CoreRoles.ADMINISTRATOR;
import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.HashMap;
import java.util.Map;

public class CoreMigrationTo_6_5_50 implements MigrationScript {
    private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_5_50.class);

    @Override
    public String getVersion() {
        return "6.5.50";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
            throws Exception {
        modifyEvents(collection, appLayerFactory.getModelLayerFactory().getMetadataSchemasManager(), appLayerFactory);
    }

    private void modifyEvents(final String collection, MetadataSchemasManager manager, AppLayerFactory appLayerFactory) {
        manager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                Map<Language, String> labels = new HashMap<>();
                labels.put(Language.French, "Version");
                labels.put(Language.English, "Version");
                types.getDefaultSchema(Event.SCHEMA_TYPE).create(Event.RECORD_VERSION).setType(MetadataValueType.STRING).setLabels(labels).setMultivalue(false);
            }
        });

        SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        schemasDisplayManager.saveMetadata(
                new MetadataDisplayConfig(collection, Event.DEFAULT_SCHEMA + "_" + Event.RECORD_VERSION, true, MetadataInputType.FIELD, true,
                        "default"));
    }

}
