package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.services.schemas.builders.CommonMetadataBuilder.TOKENS_OF_HIERARCHY;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderTokensOfHierarchyCalculator;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.TokensCalculator4;

public class RMMigrationTo7_6_666 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_666(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_6_666 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).setSmallCode("f");
			typesBuilder.getSchemaType(Document.SCHEMA_TYPE).setSmallCode("d");
			typesBuilder.getSchemaType(Task.SCHEMA_TYPE).setSmallCode("t");
			typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).setSmallCode("c");

			if (typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.NON_TAXONOMY_AUTHORIZATIONS.getLocalCode())
					.getType() == MetadataValueType.REFERENCE) {

				typesBuilder.getSchema(Document.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);

				typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(Schemas.TOKENS.getLocalCode())
						.defineDataEntry().asCalculated(TokensCalculator4.class);

				MetadataBuilder folderParent = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.PARENT_FOLDER);
				MetadataBuilder documentFolder = typesBuilder.getSchema(Document.DEFAULT_SCHEMA).get(Document.FOLDER);
				MetadataBuilder documentTokensHierarchy = typesBuilder.getSchema(Document.DEFAULT_SCHEMA)
						.get(TOKENS_OF_HIERARCHY);

				MetadataBuilder tokensHierarchy = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).getMetadata(TOKENS_OF_HIERARCHY)
						.defineDataEntry().asCalculated(FolderTokensOfHierarchyCalculator.class);

				if (!typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).hasMetadata(Folder.SUB_FOLDERS_TOKENS)) {
					typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).create(Folder.SUB_FOLDERS_TOKENS)
							.setType(STRING).setMultivalue(true)
							.defineDataEntry().asUnion(folderParent, tokensHierarchy);

					typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).create(Folder.DOCUMENTS_TOKENS)
							.setType(STRING).setMultivalue(true)
							.defineDataEntry().asUnion(documentFolder, documentTokensHierarchy);
				}
			}
		}
	}
}
