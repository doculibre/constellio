package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AttachedAncestorsCalculator2;

public class RMMigrationTo9_0_50 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.40";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_50(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_50 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_50(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema();
			task.get(RMTask.LINKED_DOCUMENTS).setEssentialInSummary(true).setCacheIndex(true);

			MetadataSchemaBuilder folder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folder.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);
			folder.get(Folder.CATEGORY).setTaxonomyRelationship(false);
			folder.get(Folder.CATEGORY_ENTERED).setTaxonomyRelationship(true);

			folder.get(Folder.ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);
			folder.get(Folder.ADMINISTRATIVE_UNIT_ENTERED).setTaxonomyRelationship(true);


			MetadataSchemaBuilder document = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			document.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);

			document.get(Document.FOLDER_CATEGORY).setTaxonomyRelationship(false);
			document.get(Document.FOLDER_ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				if (typeBuilder.getDefaultSchema().hasMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)) {
					typeBuilder.getDefaultSchema().getMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)
							.defineDataEntry().asCalculated(AttachedAncestorsCalculator2.class);
				}
			}
		}
	}
}
