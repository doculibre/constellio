package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;


public class RMMigrationFrom9_4_ExternalLinkSecuritySameAsLinkedFolder extends MigrationHelper implements MigrationScript {

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updpateExistingExterNalLinks(collection, appLayerFactory);
	}

	private void updpateExistingExterNalLinks(String collection, AppLayerFactory appLayerFactory)
			throws RecordServicesException {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		final Map<String, ExternalLink> externalLinkIdToRecordMap = rm.searchExternalLinks(from(rm.externalLink.schemaType()).returnAll())
				.stream().collect(Collectors.toMap(ExternalLink::getId, externalLink -> externalLink));

		final Transaction transaction = new Transaction();
		transaction.setSkippingReferenceToLogicallyDeletedValidation(true);

		rm.searchFolders(from(rm.folder.schemaType()).where(rm.folder.externalLinks()).isNotNull()).forEach(folder -> {
			folder.getExternalLinks()
					.stream().map(externalLinkIdToRecordMap::get)
					.filter(Objects::nonNull)
					.forEach(externalLink -> {

						externalLink.setLinkedto(folder);
						transaction.update(externalLink.getWrappedRecord());
					});
		});

		recordServices.executeInBatch(transaction);
	}


	class SchemaAlteration extends MetadataSchemasAlterationHelper {

		protected SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder externalLink = typesBuilder.getDefaultSchema(ExternalLink.SCHEMA_TYPE);
			MetadataSchemaBuilder folder = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);

			MetadataBuilder linkedTo = externalLink.create(ExternalLink.LINKED_TO)
					.defineReferencesTo(folder.getSchemaTypeBuilder());

			externalLink.getMetadata(Schemas.SECONDARY_CONCEPTS_INT_IDS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.SECONDARY_CONCEPTS_INT_IDS.getLocalCode()));
			externalLink.getMetadata(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS.getLocalCode()));
			externalLink.getMetadata(Schemas.PRINCIPALS_ANCESTORS_INT_IDS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.PRINCIPALS_ANCESTORS_INT_IDS.getLocalCode()));
			externalLink.getMetadata(Schemas.PRINCIPAL_CONCEPTS_INT_IDS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.PRINCIPAL_CONCEPTS_INT_IDS.getLocalCode()));
			externalLink.getMetadata(Schemas.DETACHED_PRINCIPAL_ANCESTORS_INT_IDS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.DETACHED_PRINCIPAL_ANCESTORS_INT_IDS.getLocalCode()));
			externalLink.getMetadata(Schemas.TOKENS.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.TOKENS.getLocalCode()));
			externalLink.getMetadata(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).defineDataEntry().asCopied(
					linkedTo, folder.getMetadata(Schemas.TOKENS.getLocalCode()));
		}
	}
}
