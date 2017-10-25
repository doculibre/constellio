package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.List;
import java.util.Set;

public class RMMigrationTo7_6_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		List<MetadataSchemaType> schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaTypes();
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		for(MetadataSchemaType type: schemaTypes) {
			List<MetadataSchema> allSchemas = type.getAllSchemas();
			for(MetadataSchema schema: allSchemas) {
				MetadataList metadataList = schema.getMetadatas().onlySearchable();
				for(Metadata metadata: metadataList) {
					metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, metadata.getCode())
						.withHighlightStatus(true));
				}
			}
		}
	}
}
