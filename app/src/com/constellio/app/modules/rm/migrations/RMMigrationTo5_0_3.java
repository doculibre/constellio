/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.migrations;

import static com.constellio.data.utils.LangUtils.withoutDuplicates;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_0_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "5.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new SchemaAlterationFor5_0_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

		givenNewPermissionsToExistingRMRoles(collection, appLayerFactory.getModelLayerFactory());

		configureNewDocumentMetadatasDisplay(collection, migrationResourcesProvider, appLayerFactory);
	}

	class SchemaAlterationFor5_0_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_0_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.0.3";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.create(Document.AUTHOR).setType(MetadataValueType.STRING);
			documentSchema.create(Document.COMPANY).setType(MetadataValueType.STRING);
			documentSchema.create(Document.SUBJECT).setType(MetadataValueType.STRING);

			typesBuilder.getSchema(RetentionRule.DEFAULT_SCHEMA).get(Schemas.TITLE_CODE).setDefaultRequirement(true);
		}
	}

	private void givenNewPermissionsToExistingRMRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role rgdRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		Role userRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.USER);
		Role managerRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.MANAGER);

		List<String> userPermissions = new ArrayList<>(userRole.getOperationPermissions());
		userPermissions.add(RMPermissionsTo.SHARE_DOCUMENT);
		userPermissions.add(RMPermissionsTo.SHARE_FOLDER);

		List<String> managerPermissions = new ArrayList<>(managerRole.getOperationPermissions());
		managerPermissions.add(RMPermissionsTo.SHARE_DOCUMENT);
		managerPermissions.add(RMPermissionsTo.SHARE_FOLDER);
		managerPermissions.add(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS);
		managerPermissions.add(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS);

		List<String> newRgdPermissions = new ArrayList<>();
		newRgdPermissions.addAll(RMPermissionsTo.PERMISSIONS.getAll());
		newRgdPermissions.addAll(CorePermissions.PERMISSIONS.getAll());

		modelLayerFactory.getRolesManager().updateRole(userRole.withPermissions(withoutDuplicates(userPermissions)));
		modelLayerFactory.getRolesManager().updateRole(managerRole.withPermissions(withoutDuplicates(managerPermissions)));
		modelLayerFactory.getRolesManager().updateRole(rgdRole.withPermissions(withoutDuplicates(newRgdPermissions)));

	}

	private void configureNewDocumentMetadatasDisplay(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(types.getCollection(), Document.DEFAULT_SCHEMA);

		List<String> formMetadatas = new ArrayList<>(schemaDisplayConfig.getFormMetadataCodes());
		List<String> displayMetadatas = new ArrayList<>(schemaDisplayConfig.getDisplayMetadataCodes());

		formMetadatas.add(Document.DEFAULT_SCHEMA + "_" + Document.AUTHOR);
		formMetadatas.add(Document.DEFAULT_SCHEMA + "_" + Document.COMPANY);
		formMetadatas.add(Document.DEFAULT_SCHEMA + "_" + Document.SUBJECT);

		displayMetadatas.add(displayMetadatas.size() - 1, Document.DEFAULT_SCHEMA + "_" + Document.AUTHOR);
		displayMetadatas.add(displayMetadatas.size() - 1, Document.DEFAULT_SCHEMA + "_" + Document.COMPANY);
		displayMetadatas.add(displayMetadatas.size() - 1, Document.DEFAULT_SCHEMA + "_" + Document.SUBJECT);

		transaction.add(schemaDisplayConfig.withDisplayMetadataCodes(displayMetadatas).withFormMetadataCodes(formMetadatas));

		schemasDisplayManager.execute(transaction);
	}

}
