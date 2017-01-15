package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.CorePermissions.USE_EXTERNAL_APIS_FOR_COLLECTION;
import static java.util.Arrays.asList;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

public class RMMigrationTo6_5_54 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.54";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationsFor6_5_54(collection, provider, appLayerFactory).migrate();
		SchemasDisplayManager schemaDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = schemaDisplayManager.newTransactionBuilderFor(collection);
		transaction.in(ContainerRecord.SCHEMA_TYPE).addToDisplay(ContainerRecord.ADMINISTRATIVE_UNITS)
				.afterMetadata(ContainerRecord.ADMINISTRATIVE_UNITS);

		schemaDisplayManager.execute(transaction.build());
		setupRoles(collection, appLayerFactory.getModelLayerFactory().getRolesManager(), provider);

	}

	private void setupRoles(String collection, RolesManager manager, MigrationResourcesProvider provider) {
		manager.updateRole(
				manager.getRole(collection, RMRoles.RGD).withNewPermissions(asList(USE_EXTERNAL_APIS_FOR_COLLECTION)));
	}

	public static class SchemaAlterationsFor6_5_54 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_54(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder schemaType = types().getSchemaType(Folder.SCHEMA_TYPE);
			MetadataSchemaTypeBuilder adminUnitSchemaType = types().getSchemaType(AdministrativeUnit.SCHEMA_TYPE);

			copy(adminUnitSchemaType, schemaType, Folder.ADMINISTRATIVE_UNIT, AdministrativeUnit.CODE, Folder.ADMINISTRATIVE_UNIT_CODE);
		}

		private MetadataBuilder copy(MetadataSchemaTypeBuilder sourceSchemaType,
				MetadataSchemaTypeBuilder destinationSchemaType,

				String referenceLocalCode, String sourceMetadataLocalCode, String destinationMetadataLocalCode) {

			MetadataSchemaBuilder sourceDefaultSchema = sourceSchemaType.getDefaultSchema();
			MetadataSchemaBuilder destinationDefaultSchema = destinationSchemaType.getDefaultSchema();

			MetadataBuilder refMetadata = destinationDefaultSchema.getMetadata(referenceLocalCode);
			MetadataBuilder sourceMetadata = sourceDefaultSchema.getMetadata(sourceMetadataLocalCode);
			MetadataBuilder destinationMetadata = destinationDefaultSchema.createUndeletable(destinationMetadataLocalCode);

			destinationMetadata.setMultivalue(sourceMetadata.isMultivalue() || refMetadata.isMultivalue());
			destinationMetadata.setType(sourceMetadata.getType());
			destinationMetadata.defineDataEntry().asCopied(refMetadata, sourceMetadata);

			if (sourceMetadata.getType() == MetadataValueType.REFERENCE) {
				String referenceSchemaType = sourceMetadata.getAllowedReferencesBuilder().getSchemaType();
				destinationMetadata.defineReferences().setCompleteSchemaTypeCode(referenceSchemaType);
			} else if (sourceMetadata.getType() == MetadataValueType.ENUM) {
				destinationMetadata.defineAsEnum(sourceMetadata.getEnumClass());
			}

			return destinationMetadata;
		}
	}

}
