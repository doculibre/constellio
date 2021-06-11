package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleTransaction;
import com.constellio.app.modules.rm.model.validators.RuleDocumentTypeValidator;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.LegalReference;
import com.constellio.app.modules.rm.wrappers.LegalRequirement;
import com.constellio.app.modules.rm.wrappers.LegalRequirementReference;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.RequirementType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.MigrationUtil;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.Map;

import static java.util.Arrays.asList;

public class RMMigrationTo9_3_0_1 implements MigrationScript {

	private String collection;
	private SchemasDisplayManager displayManager;

	@Override
	public String getVersion() {
		return "9.3.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_3_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		this.collection = collection;
		displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		updateLegalRequirementDisplay();
		updateLegalReferenceDisplay();
		updateLegalRequirementReferenceDisplay();
	}

	private void updateLegalRequirementDisplay() {
		SchemaDisplayConfig config = displayManager.getSchema(collection, LegalRequirement.DEFAULT_SCHEMA);
		displayManager.saveSchema(config
				.withTableMetadataCodes(asList(
						LegalRequirement.DEFAULT_SCHEMA + "_" + LegalRequirement.TITLE,
						LegalRequirement.DEFAULT_SCHEMA + "_" + LegalRequirement.CODE,
						LegalRequirement.DEFAULT_SCHEMA + "_" + LegalRequirement.AUTHOR)));
	}

	private void updateLegalReferenceDisplay() {
		MetadataDisplayConfig metadataConfig = displayManager.getMetadata(collection, LegalReference.DEFAULT_SCHEMA, LegalReference.URL);
		displayManager.saveMetadata(metadataConfig.withInputType(MetadataInputType.URL));

		SchemaDisplayConfig schemaConfig = displayManager.getSchema(collection, LegalReference.DEFAULT_SCHEMA);
		displayManager.saveSchema(schemaConfig
				.withTableMetadataCodes(asList(
						LegalReference.DEFAULT_SCHEMA + "_" + LegalReference.TITLE,
						LegalReference.DEFAULT_SCHEMA + "_" + LegalReference.CODE,
						LegalReference.DEFAULT_SCHEMA + "_" + LegalReference.URL)));
	}

	private void updateLegalRequirementReferenceDisplay() {
		SchemaDisplayConfig config = displayManager.getSchema(collection, LegalRequirementReference.DEFAULT_SCHEMA);
		displayManager.saveSchema(config
				.withTableMetadataCodes(asList(
						LegalRequirementReference.DEFAULT_SCHEMA + "_" + LegalRequirementReference.RULE_REQUIREMENT,
						LegalRequirementReference.DEFAULT_SCHEMA + "_" + LegalRequirementReference.RULE_REFERENCE,
						LegalRequirementReference.DEFAULT_SCHEMA + "_" + LegalRequirementReference.DESCRIPTION)));
	}

	private class SchemaAlterationFor9_3_0_1 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_3_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
								 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			createRequirementTypeDDV();

			createNewSchema();

			createLegalReferenceMetadatas();
			createLegalRequirementMetadatas();
			createLegalRequirementRefMetadatas();
			createRuleDocumentTypeMetadatas();

			updateCategorySchema();
			updateDocumentTypeSchema();
			updateRetentionRuleSchema();
		}

		private void createRequirementTypeDDV() {
			Map<Language, String> mapLanguage = MigrationUtil.getLabelsByLanguage(collection, modelLayerFactory,
					migrationResourcesProvider, "init.ddvRequirementType");

			MetadataSchemaTypeBuilder requirementType = new ValueListItemSchemaTypeBuilder(types())
					.createValueListItemSchema(RequirementType.SCHEMA_TYPE, mapLanguage,
							ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique());
		}

		private void createNewSchema() {
			typesBuilder.createNewSchemaTypeWithSecurity(LegalReference.SCHEMA_TYPE).setSecurity(false);
			typesBuilder.createNewSchemaTypeWithSecurity(LegalRequirement.SCHEMA_TYPE).setSecurity(false);
			typesBuilder.createNewSchemaTypeWithSecurity(LegalRequirementReference.SCHEMA_TYPE).setSecurity(false);
			typesBuilder.createNewSchemaTypeWithSecurity(RetentionRuleDocumentType.SCHEMA_TYPE).setSecurity(false);
		}

		private void createLegalReferenceMetadatas() {
			MetadataSchemaBuilder legalRefSchema = typesBuilder
					.getSchemaType(LegalReference.SCHEMA_TYPE).getDefaultSchema();

			legalRefSchema.createUndeletable(LegalReference.CODE).setType(MetadataValueType.STRING);
			legalRefSchema.createUndeletable(LegalReference.URL).setType(MetadataValueType.STRING);
		}

		private void createLegalRequirementMetadatas() {
			MetadataSchemaBuilder legalRequirementSchema = typesBuilder
					.getSchemaType(LegalRequirement.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder requirementTypeSchema = typesBuilder
					.getSchemaType(RequirementType.SCHEMA_TYPE).getDefaultSchema();

			legalRequirementSchema.createUndeletable(LegalRequirement.CODE).setType(MetadataValueType.STRING);
			legalRequirementSchema.createUndeletable(LegalRequirement.TYPES)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(requirementTypeSchema)
					.setMultivalue(true);
			legalRequirementSchema.createUndeletable(LegalRequirement.AUTHOR)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(userSchema);
			legalRequirementSchema.createUndeletable(LegalRequirement.DESCRIPTION).setType(MetadataValueType.TEXT);
			legalRequirementSchema.createUndeletable(LegalRequirement.CONSEQUENCES).setType(MetadataValueType.TEXT);
			legalRequirementSchema.createUndeletable(LegalRequirement.OBJECT_TYPES).setType(MetadataValueType.TEXT);
		}

		private void createLegalRequirementRefMetadatas() {
			MetadataSchemaBuilder legalRequirementRefSchema = typesBuilder
					.getSchemaType(LegalRequirementReference.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder legalRequirementSchema = typesBuilder
					.getSchemaType(LegalRequirement.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder legalRefSchema = typesBuilder
					.getSchemaType(LegalReference.SCHEMA_TYPE).getDefaultSchema();

			legalRequirementRefSchema.createUndeletable(LegalRequirementReference.DESCRIPTION)
					.setType(MetadataValueType.TEXT);
			legalRequirementRefSchema.createUndeletable(LegalRequirementReference.RULE_REQUIREMENT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(legalRequirementSchema);
			legalRequirementRefSchema.createUndeletable(LegalRequirementReference.RULE_REFERENCE)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(legalRefSchema);
		}

		private void createRuleDocumentTypeMetadatas() {
			MetadataSchemaBuilder ruleDocumentTypeSchema = typesBuilder
					.getSchemaType(RetentionRuleDocumentType.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder ruleSchema = typesBuilder.getSchemaType(RetentionRule.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder categorySchema = typesBuilder.getSchemaType(Category.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder documentTypeSchema = typesBuilder
					.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();

			ruleDocumentTypeSchema.createUndeletable(RetentionRuleDocumentType.RETENTION_RULE)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(ruleSchema);
			ruleDocumentTypeSchema.createUndeletable(RetentionRuleDocumentType.RETENTION_RULE_COPY)
					.setType(MetadataValueType.STRING);
			ruleDocumentTypeSchema.createUndeletable(RetentionRuleDocumentType.CATEGORY)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(categorySchema);
			ruleDocumentTypeSchema.createUndeletable(RetentionRuleDocumentType.DOCUMENT_TYPE)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(documentTypeSchema);

			ruleDocumentTypeSchema.defineValidators().add(RuleDocumentTypeValidator.class);
		}

		private void updateCategorySchema() {
			MetadataSchemaBuilder categorySchema = typesBuilder.getSchemaType(Category.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder legalRequirementSchema = typesBuilder
					.getSchemaType(LegalRequirement.SCHEMA_TYPE).getDefaultSchema();

			categorySchema.createUndeletable(Category.LEGAL_REQUIREMENTS)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(legalRequirementSchema)
					.setMultivalue(true);
		}

		private void updateDocumentTypeSchema() {
			MetadataSchemaBuilder documentTypeSchema = typesBuilder
					.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();

			documentTypeSchema.createUndeletable(DocumentType.PARENT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(documentTypeSchema);
		}

		private void updateRetentionRuleSchema() {
			MetadataSchemaBuilder ruleSchema = typesBuilder
					.getSchemaType(RetentionRule.SCHEMA_TYPE).getDefaultSchema();

			ruleSchema.createUndeletable(RetentionRule.TRANSACTION)
					.setType(MetadataValueType.ENUM).defineAsEnum(RetentionRuleTransaction.class);
			ruleSchema.createUndeletable(RetentionRule.APPLICATION_NOTES)
					.setType(MetadataValueType.TEXT);
		}
	}
}
