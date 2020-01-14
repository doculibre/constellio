package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.document.DocumentExpectedTransferDateCalculator;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.AttachedAncestorsCalculator2;

public class RMMigrationTo9_0_52 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.52";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_52(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_52 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_52(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema();
			task.get(RMTask.LINKED_DOCUMENTS).setEssentialInSummary(true).setCacheIndex(true);

			MetadataSchemaBuilder folder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folder.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);
			folder.get(Schemas.MODIFIED_ON).setEssentialInSummary(true);
			folder.get(Folder.FORM_CREATED_ON).setEssentialInSummary(true);
			folder.get(Folder.FORM_MODIFIED_ON).setEssentialInSummary(true);
			folder.get(Folder.OPENING_DATE).setEssentialInSummary(true);
			folder.get(Folder.CLOSING_DATE).setEssentialInSummary(true);
			folder.get(Folder.CATEGORY).setTaxonomyRelationship(false);
			folder.get(Folder.CATEGORY_ENTERED).setTaxonomyRelationship(true);

			folder.get(Folder.ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);
			folder.get(Folder.ADMINISTRATIVE_UNIT_ENTERED).setTaxonomyRelationship(true);


			MetadataSchemaBuilder document = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			document.get(Schemas.TOKENS_OF_HIERARCHY).setEssentialInSummary(true);
			document.get(Schemas.MODIFIED_ON).setEssentialInSummary(true);
			document.get(Document.FORM_CREATED_ON).setEssentialInSummary(true);
			document.get(Document.FORM_MODIFIED_ON).setEssentialInSummary(true);

			document.get(Document.FOLDER_CATEGORY).setTaxonomyRelationship(false);
			document.get(Document.FOLDER_ADMINISTRATIVE_UNIT).setTaxonomyRelationship(false);

			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				if (typeBuilder.getDefaultSchema().hasMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)) {
					typeBuilder.getDefaultSchema().getMetadata(CommonMetadataBuilder.ATTACHED_ANCESTORS)
							.defineDataEntry().asCalculated(AttachedAncestorsCalculator2.class);
				}
			}

			if (Toggle.DOCUMENT_RETENTION_RULES.isEnabled() && hasCalculator(
					document.getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE), DocumentExpectedTransferDateCalculator.class)) {
				document.getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE).defineDataEntry()
						.asCopied(document.get(Document.FOLDER), folder.get(Folder.EXPECTED_TRANSFER_DATE));
				document.getMetadata(Document.FOLDER_EXPECTED_DEPOSIT_DATE).defineDataEntry()
						.asCopied(document.get(Document.FOLDER), folder.get(Folder.EXPECTED_DEPOSIT_DATE));
				document.getMetadata(Document.FOLDER_EXPECTED_DESTRUCTION_DATE).defineDataEntry()
						.asCopied(document.get(Document.FOLDER), folder.get(Folder.EXPECTED_DESTRUCTION_DATE));
			}
		}
	}

	private boolean hasCalculator(MetadataBuilder metadata, Class<?> expectedCalculatorClass) {
		return metadata.getDataEntry().getType() == DataEntryType.CALCULATED
			   && ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().getClass().equals(expectedCalculatorClass);

	}

}
