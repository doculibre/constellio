package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static java.util.Arrays.asList;

public class RMMigrationTo7_6_9 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.9";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_9(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		manager.saveSchema(manager.getSchema(collection, BagInfo.DEFAULT_SCHEMA).withFormMetadataCodes(asList(
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.TITLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.ARCHIVE_TITLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.ID_ORGANISME_VERSEUR_OU_DONATEUR,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.REGION_ADMINISTRATIVE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.IDENTIFICATION_RESPONSABLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.NUMERO_TELEPHONE_RESPONSABLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.COURRIEL_RESPONSABLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.ENTITE_RESPONSABLE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.CATEGORIE_DOCUMENT,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.ENCODAGE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.METHODE_TRANSFERE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.DESCRIPTION_SOMMAIRE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.RESTRICTION_ACCESSIBILITE,
				BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.NOTE
		)));

		manager.saveMetadata(manager.getMetadata(collection, BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.RESTRICTION_ACCESSIBILITE)
				.withInputType(MetadataInputType.RICHTEXT));
		manager.saveMetadata(manager.getMetadata(collection, BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.NOTE)
				.withInputType(MetadataInputType.RICHTEXT));
		manager.saveMetadata(manager.getMetadata(collection, BagInfo.DEFAULT_SCHEMA + "_" + BagInfo.DESCRIPTION_SOMMAIRE)
				.withInputType(MetadataInputType.RICHTEXT));
	}

	class SchemaAlterationFor7_6_9 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_9(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//            MetadataSchemaTypeBuilder administrativeRegionBuilder = new ValueListItemSchemaTypeBuilder(types())
			//                    .createValueListItemSchema(AdministrativeRegion.SCHEMA_TYPE, new HashMap<Language, String>() {{
			//                                put(Language.French, "Région administrative");
			//                                put(Language.English, "Administrative Region");
			//                            }},
			//                            ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions.codeMetadataDisabled());

			MetadataSchemaBuilder defaultSchemaBuilder;

			if (typesBuilder.hasSchemaType(BagInfo.SCHEMA_TYPE)) {
				defaultSchemaBuilder = typesBuilder.getSchemaType(BagInfo.SCHEMA_TYPE).getDefaultSchema();
			} else {
				MetadataSchemaTypeBuilder builder = typesBuilder.createNewSchemaTypeWithSecurity(BagInfo.SCHEMA_TYPE);
				defaultSchemaBuilder = builder.getDefaultSchema();
			}

			if (!defaultSchemaBuilder.hasMetadata(BagInfo.ARCHIVE_TITLE)) {
				defaultSchemaBuilder.create(BagInfo.ARCHIVE_TITLE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.NOTE)) {
				defaultSchemaBuilder.create(BagInfo.NOTE).setType(MetadataValueType.TEXT);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR)) {
				defaultSchemaBuilder.create(BagInfo.IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR)
						.setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.ID_ORGANISME_VERSEUR_OU_DONATEUR)) {
				defaultSchemaBuilder.create(BagInfo.ID_ORGANISME_VERSEUR_OU_DONATEUR).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.ADRESSE)) {
				defaultSchemaBuilder.create(BagInfo.ADRESSE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.REGION_ADMINISTRATIVE)) {
				defaultSchemaBuilder.create(BagInfo.REGION_ADMINISTRATIVE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.ENTITE_RESPONSABLE)) {
				defaultSchemaBuilder.create(BagInfo.ENTITE_RESPONSABLE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.IDENTIFICATION_RESPONSABLE)) {
				defaultSchemaBuilder.create(BagInfo.IDENTIFICATION_RESPONSABLE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.COURRIEL_RESPONSABLE)) {
				defaultSchemaBuilder.create(BagInfo.COURRIEL_RESPONSABLE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.NUMERO_TELEPHONE_RESPONSABLE)) {
				defaultSchemaBuilder.create(BagInfo.NUMERO_TELEPHONE_RESPONSABLE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.DESCRIPTION_SOMMAIRE)) {
				defaultSchemaBuilder.create(BagInfo.DESCRIPTION_SOMMAIRE).setType(MetadataValueType.TEXT);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.CATEGORIE_DOCUMENT)) {
				defaultSchemaBuilder.create(BagInfo.CATEGORIE_DOCUMENT).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.METHODE_TRANSFERE)) {
				defaultSchemaBuilder.create(BagInfo.METHODE_TRANSFERE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.RESTRICTION_ACCESSIBILITE)) {
				defaultSchemaBuilder.create(BagInfo.RESTRICTION_ACCESSIBILITE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.ENCODAGE)) {
				defaultSchemaBuilder.create(BagInfo.ENCODAGE).setType(MetadataValueType.STRING);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.RESTRICTION_ACCESSIBILITE)) {
				defaultSchemaBuilder.deleteMetadataWithoutValidation(BagInfo.RESTRICTION_ACCESSIBILITE);
			}
			if (!defaultSchemaBuilder.hasMetadata(BagInfo.REGION_ADMINISTRATIVE)) {
				defaultSchemaBuilder.deleteMetadataWithoutValidation(BagInfo.REGION_ADMINISTRATIVE);
			}

			if (!defaultSchemaBuilder.hasMetadata(BagInfo.RESTRICTION_ACCESSIBILITE)) {
				defaultSchemaBuilder.create(BagInfo.RESTRICTION_ACCESSIBILITE).setType(MetadataValueType.TEXT);
			}
		}
	}
}
