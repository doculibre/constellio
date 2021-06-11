package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ApprovalTask;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.calculators.RolesCalculator;
import com.constellio.model.services.schemas.calculators.UserTokensCalculator2;
import com.constellio.model.services.schemas.validators.DecisionValidator;
import com.constellio.model.services.schemas.validators.EmailValidator;
import org.apache.commons.lang3.StringUtils;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_5_0_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor5_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}
}

class CoreSchemaAlterationFor5_0_1 extends MetadataSchemasAlterationHelper {

	protected CoreSchemaAlterationFor5_0_1(String collection,
										   MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
		super(collection, migrationResourcesProvider, appLayerFactory);
	}

	@Override
	protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder groupSchemaType = createGroupSchemaType(typesBuilder);

		MetadataSchemaTypeBuilder userSchemaType = createUserSchemaType(typesBuilder, groupSchemaType);

		MetadataSchemaTypeBuilder eventSchemaType = createEventSchemaType(typesBuilder);

		MetadataSchemaTypeBuilder userDocument = createUserDocumentType(typesBuilder, userSchemaType);

		createCollectionSchemaType(typesBuilder);
		createTaskSchemaType(typesBuilder, userSchemaType);
	}

	private MetadataSchemaTypeBuilder createUserDocumentType(MetadataSchemaTypesBuilder typesBuilder,
															 MetadataSchemaTypeBuilder userSchemaType) {
		MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(UserDocument.SCHEMA_TYPE);
		MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
		type.setSecurity(false);
		defaultSchema.createUndeletable(UserDocument.USER).defineReferencesTo(userSchemaType);
		defaultSchema.createUndeletable(UserDocument.CONTENT).setType(CONTENT).setSearchable(true);

		return type;
	}

	private MetadataSchemaTypeBuilder createEventSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder type = typesBuilder.createNewSchemaTypeWithSecurity(Event.SCHEMA_TYPE);
		MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();

		//FIXME labels
		MetadataBuilder metadataBuilder = defaultSchema.createUndeletable(Event.RECORD_ID).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.TYPE).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.USERNAME).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.EVENT_PRINCIPAL_PATH).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.USER_ROLES).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.DELTA).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.PERMISSION_DATE_RANGE).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.PERMISSION_ROLES).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.PERMISSION_USERS).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.IP).setType(STRING);
		configureLabels(typesBuilder, metadataBuilder);
		metadataBuilder = defaultSchema.createUndeletable(Event.REASON).setType(STRING)
				.addLabel(Language.French, "Justification");
		configureLabels(typesBuilder, metadataBuilder);

		return type;
	}

	private void configureLabels(MetadataSchemaTypesBuilder typesBuilder, MetadataBuilder metadataBuilder) {
		for (Language language : typesBuilder.getLanguages()) {
			if (StringUtils.isBlank(metadataBuilder.getLabel(language))) {
				metadataBuilder.addLabel(language, metadataBuilder.getLocalCode());
			}
		}
	}

	private void createCollectionSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
		typesBuilder.createNewSchemaTypeWithSecurity(Collection.SCHEMA_TYPE);
		MetadataSchemaBuilder collectionSchema = typesBuilder.getSchemaType("collection").getDefaultSchema();
		collectionSchema.createUndeletable(Collection.NAME).setType(STRING);
		collectionSchema.createUndeletable(Collection.CODE).setType(STRING).setUniqueValue(true).setUnmodifiable(true);
		collectionSchema.createUndeletable(Collection.LANGUAGES).setType(STRING).setMultivalue(true).setUnmodifiable(true);
	}

	private MetadataSchemaTypeBuilder createUserSchemaType(MetadataSchemaTypesBuilder typesBuilder,
														   MetadataSchemaTypeBuilder groupSchemaType) {
		typesBuilder.createNewSchemaTypeWithSecurity(User.SCHEMA_TYPE);
		MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType("user");
		MetadataSchemaBuilder userSchema = userSchemaType.getDefaultSchema();
		userSchema.createUndeletable(User.USERNAME).setType(STRING).setUniqueValue(true)
				.setUnmodifiable(true);
		userSchema.createUndeletable(User.FIRSTNAME).setType(STRING);
		userSchema.createUndeletable(User.LASTNAME).setType(STRING);
		userSchema.createUndeletable(User.LAST_LOGIN).setType(DATE_TIME).setSystemReserved(true);
		userSchema.createUndeletable(User.LAST_IP_ADDRESS).setType(STRING).setSystemReserved(true);
		userSchema.createUndeletable(User.EMAIL).setType(STRING).setUniqueValue(true).addValidator(EmailValidator.class);
		userSchema.createUndeletable(User.ROLES).setType(STRING).setMultivalue(true);
		userSchema.createUndeletable(User.COLLECTION_READ_ACCESS).setType(BOOLEAN);
		userSchema.createUndeletable(User.COLLECTION_WRITE_ACCESS).setType(BOOLEAN);
		userSchema.createUndeletable(User.COLLECTION_DELETE_ACCESS).setType(BOOLEAN);
		userSchema.createUndeletable(User.SYSTEM_ADMIN).setType(BOOLEAN);
		MetadataBuilder groupsReference = userSchema.createUndeletable(User.GROUPS).setType(REFERENCE).setMultivalue(true)
				.defineReferencesTo(groupSchemaType);
		userSchema.createUndeletable(User.ALL_ROLES).setType(STRING).setMultivalue(true).defineDataEntry()
				.asCalculated(RolesCalculator.class);

		//		userSchema.createUndeletable(User.GROUPS_AUTHORIZATIONS).setType(STRING).setMultivalue(true).defineDataEntry()
		//				.asCopied(groupsReference, groupSchemaType.getMetadata("group_default_allauthorizations"));
		//
		//		userSchema.createUndeletable(User.ALL_USER_AUTHORIZATIONS).setType(STRING).setMultivalue(true).defineDataEntry()
		//				.asCalculated(AllUserAuthorizationsCalculator.class);
		userSchema.createUndeletable(User.USER_TOKENS).setType(STRING).setMultivalue(true).defineDataEntry()
				.asCalculated(UserTokensCalculator2.class);

		userSchema.createUndeletable(User.JOB_TITLE).setType(STRING);
		userSchema.createUndeletable(User.PHONE).setType(STRING);
		userSchema.createUndeletable(User.START_TAB).setType(STRING);
		userSchema.createUndeletable(User.DEFAULT_TAXONOMY).setType(STRING);
		//		userSchema.createUndeletable(User.STATUS).setType(STRING);
		userSchema.createUndeletable(User.STATUS).defineAsEnum(UserCredentialStatus.class);
		return userSchemaType;
	}

	private MetadataSchemaTypeBuilder createGroupSchemaType(MetadataSchemaTypesBuilder typesBuilder) {
		MetadataSchemaTypeBuilder groupSchemaType = typesBuilder.createNewSchemaTypeWithSecurity(Group.SCHEMA_TYPE);
		MetadataSchemaBuilder groupSchema = groupSchemaType.getDefaultSchema();
		groupSchema.get(Group.TITLE).setSchemaAutocomplete(true);
		groupSchema.createUndeletable(Group.CODE).setType(STRING).setUniqueValue(true).setSchemaAutocomplete(true);
		groupSchema.createUndeletable(Group.IS_GLOBAL).setType(BOOLEAN);
		groupSchema.createUndeletable(Group.ROLES).setType(STRING).setMultivalue(true);
		MetadataBuilder parentGroup = groupSchema.createUndeletable(Group.PARENT).setType(REFERENCE)
				.defineReferencesTo(groupSchema);
		return groupSchemaType;
	}

	private MetadataSchemaTypeBuilder createTaskSchemaType(MetadataSchemaTypesBuilder typesBuilder,
														   MetadataSchemaTypeBuilder userSchema) {
		MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.createNewSchemaTypeWithSecurity(WorkflowTask.SCHEMA_TYPE);
		MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
		taskSchema.createUndeletable(WorkflowTask.ASSIGNED_TO).setType(REFERENCE).defineReferencesTo(userSchema);
		taskSchema.createUndeletable(WorkflowTask.ASSIGNED_ON).setType(DATE_TIME);
		taskSchema.createUndeletable(WorkflowTask.ASSIGN_CANDIDATES).setType(REFERENCE).defineReferencesTo(userSchema)
				.setMultivalue(true);
		taskSchema.createUndeletable(WorkflowTask.FINISHED_BY).setType(REFERENCE).defineReferencesTo(userSchema);
		taskSchema.createUndeletable(WorkflowTask.FINISHED_ON).setType(DATE_TIME);
		taskSchema.createUndeletable(WorkflowTask.WORKFLOW_ID).setType(STRING);
		taskSchema.createUndeletable(WorkflowTask.WORKFLOW_RECORD_IDS).setType(STRING).setMultivalue(true);
		taskSchema.createUndeletable(WorkflowTask.DUE_DATE).setType(DATE_TIME);

		MetadataSchemaBuilder approvalTaskSchema = taskSchemaType.createCustomSchema(ApprovalTask.SCHEMA_LOCAL_CODE);
		approvalTaskSchema.createUndeletable(ApprovalTask.DECISION).setType(STRING).addValidator(DecisionValidator.class);

		return taskSchemaType;
	}
}
