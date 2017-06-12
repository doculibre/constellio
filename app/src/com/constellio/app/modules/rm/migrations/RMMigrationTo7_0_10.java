package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA_AUTOCOMPLETE_FIELD;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentAutocompleteFieldCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderAutocompleteFieldCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_0_10 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.0.10";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor7_0_10(collection, provider, factory).migrate();
	}

	public static class SchemaAlterationsFor7_0_10 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor7_0_10(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode())
					.defineDataEntry().asCalculated(FolderAutocompleteFieldCalculator.class);
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(SCHEMA_AUTOCOMPLETE_FIELD.getLocalCode())
					.defineDataEntry().asCalculated(DocumentAutocompleteFieldCalculator.class);
		}
	}

}
