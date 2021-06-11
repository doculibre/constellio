package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TaskTypes;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;
import com.constellio.model.services.schemas.calculators.TokensCalculator5;

public class TasksMigrationFrom9_3_UpdateTokensCalculator extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.3.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataSchemaTypeBuilder typeBuilder : TaskTypes.taskSchemaTypes(typesBuilder)) {
				if (typeBuilder.getDefaultSchema().hasMetadata(Schemas.TOKENS.getLocalCode())) {
					MetadataBuilder tokens = typeBuilder.getDefaultSchema().getMetadata(Schemas.TOKENS.getLocalCode());
					if (tokens.getDataEntry().getType() == DataEntryType.CALCULATED && ((CalculatedDataEntry) tokens.getDataEntry()).getCalculator() instanceof TokensCalculator4) {
						tokens.defineDataEntry().asCalculated(TokensCalculator5.class);
					}
				}
			}
		}
	}
}
