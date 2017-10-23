package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.*;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.HashMap;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class CoreMigrationTo_7_6_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_6_2 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			HashMap<Language, String> labels = new HashMap<>();
			labels.put(Language.French, "Jetons manuels");
			labels.put(Language.English, "Manual tokens");

			for(MetadataSchemaTypeBuilder type: typesBuilder.getTypes()) {
				MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
				defaultSchema.getMetadata(Schemas.MANUAL_TOKENS.getLocalCode())
						.setLabels(labels);
				if(defaultSchema.hasMetadata("errorStackTrace")) {
					defaultSchema.getMetadata(Schemas.MANUAL_TOKENS.getLocalCode())
							.addLabel(Language.French, "Fil d'exécution");
				}
			}
		}
	}
}
