package com.constellio.app.modules.robots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.robots.constants.RobotsPermissionsTo;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.search.criteria.CriterionFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;
import static java.util.Arrays.asList;

public class RobotsMigrationTo5_1_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor5_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplayConfig(collection, appLayerFactory, migrationResourcesProvider);
		setupRoles(collection, appLayerFactory.getModelLayerFactory());
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory,
									MigrationResourcesProvider provider) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		Language language = provider.getLanguage();

		String definition = "default:init.robot.tabs.definition";
		String criteria = "init.robot.tabs.criteria";
		String action = "init.robot.tabs.action";

		SchemaTypeDisplayConfig type = manager.getType(collection, Robot.SCHEMA_TYPE);
		transaction.add(type.withMetadataGroup(provider.getLanguageMap(asList(definition, criteria, action))));

		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.PARENT)
				.withInputType(MetadataInputType.HIDDEN));

		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.SCHEMA_FILTER).withMetadataGroup(criteria));
		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.SEARCH_CRITERIA).withMetadataGroup(criteria));

		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.ACTION).withMetadataGroup(action));
		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.ACTION_PARAMETERS).withMetadataGroup(action));
		transaction.add(manager.getMetadata(collection, Robot.DEFAULT_SCHEMA, Robot.EXCLUDE_PROCESSED_BY_CHILDREN)
				.withMetadataGroup(action));

		manager.execute(transaction);

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);
		transactionBuilder.in(Robot.SCHEMA_TYPE)
				.addToForm(Robot.ACTION, Robot.ACTION_PARAMETERS, Robot.EXCLUDE_PROCESSED_BY_CHILDREN).atTheEnd();
		transactionBuilder.in(ActionParameters.SCHEMA_TYPE).removeFromDisplay(
				CommonMetadataBuilder.CREATED_BY, CommonMetadataBuilder.CREATED_ON,
				CommonMetadataBuilder.MODIFIED_BY, CommonMetadataBuilder.MODIFIED_ON);
		manager.execute(transactionBuilder.build());
	}

	private void setupRoles(String collection, ModelLayerFactory modelLayerFactory) {
		RolesManager rolesManager = modelLayerFactory.getRolesManager();

		Role administrator = rolesManager.getRole(collection, CoreRoles.ADMINISTRATOR);

		List<String> newAdministratorPermissions = new ArrayList<>(administrator.getOperationPermissions());
		newAdministratorPermissions.addAll(RobotsPermissionsTo.PERMISSIONS.getAll());

		rolesManager.updateRole(administrator.withPermissions(withoutDuplicates(newAdministratorPermissions)));
	}

	static class SchemaAlterationFor5_1_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor5_1_2(String collection,
										   MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder actionParameters = createActionParametersSchemaType(typesBuilder);
			createRobotSchemaType(typesBuilder, actionParameters);
		}

		private MetadataSchemaTypeBuilder createActionParametersSchemaType(MetadataSchemaTypesBuilder types) {
			MetadataSchemaTypeBuilder type = types.createNewSchemaTypeWithSecurity(ActionParameters.SCHEMA_TYPE);

			type.getDefaultSchema().get(Schemas.TITLE_CODE).setEnabled(false).setDefaultRequirement(false);

			return type;
		}

		private void createRobotSchemaType(MetadataSchemaTypesBuilder types,
										   MetadataSchemaTypeBuilder actionParameters) {
			MetadataSchemaTypeBuilder robots = types.createNewSchemaTypeWithSecurity(Robot.SCHEMA_TYPE);
			MetadataSchemaBuilder schema = robots.getDefaultSchema();

			schema.get(Schemas.TITLE_CODE).setDefaultRequirement(true).setMultiLingual(true);
			schema.createUniqueCodeMetadata();
			schema.create(Robot.DESCRIPTION).setType(MetadataValueType.TEXT);
			schema.createUndeletable(Robot.PARENT).defineChildOfRelationshipToType(robots).setEssential(true);
			schema.createUndeletable(Robot.SCHEMA_FILTER).setType(MetadataValueType.STRING).setEssential(true).required();
			schema.createUndeletable(Robot.SEARCH_CRITERIA).setMultivalue(true).setEssential(true).required()
					.defineStructureFactory(CriterionFactory.class);
			schema.createUndeletable(Robot.ACTION).setType(MetadataValueType.STRING).setEssential(true);
			schema.createUndeletable(Robot.ACTION_PARAMETERS).defineReferencesTo(actionParameters).setEssential(true);
			schema.createUndeletable(Robot.EXCLUDE_PROCESSED_BY_CHILDREN).setType(MetadataValueType.BOOLEAN)
					.setEssential(true).setDefaultValue(false).required();
		}
	}
}
