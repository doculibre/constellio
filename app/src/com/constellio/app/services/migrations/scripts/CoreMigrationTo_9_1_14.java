package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.users.UserServices;

import java.util.List;

public class CoreMigrationTo_9_1_14 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.14";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor_9_1_14(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_1_14 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_1_14(String collection,
											 MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			//USERS
			//Les UserCredential sans collection seront supprimés
			UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
			userServices.safePhysicalDeleteAllUserCredentialsWithEmptyCollections();

			//Les User auront 4 statuts : active, pending, suspended, disabled (au lieu de deleted) (nothing to do here)
			//Une métadonnée enum "syncMode" dans Usercredential avec les choix SYNCED, NOT_SYNCED, LOCALLY_CREATED
			MetadataSchemaBuilder userCredentialSchema = typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder userSyncModeBuilder = userCredentialSchema.createUndeletable("syncMode")
					.setType(MetadataValueType.STRING).setDefaultValue(UserSyncMode.LOCALLY_CREATED);

			//chaque utilisateur migré aura la valeur LOCALLY_CREATED si il existe une entrée dans le fichier de mot de passe, sinon SYNCED
			userServices.updateAllUserSyncMode();

			//Les métadonnées suivantes sont retirées de UserCredential, car l'information existe dans User :
			// firstname, lastname, email, personalEmails, collections, globalGroups, phone, fax, jobTitle, address
			userCredentialSchema.deleteMetadataWithoutValidation("firstname");
			userCredentialSchema.deleteMetadataWithoutValidation("lastname");
			userCredentialSchema.deleteMetadataWithoutValidation("email");
			userCredentialSchema.deleteMetadataWithoutValidation("personalEmails");
			userCredentialSchema.deleteMetadataWithoutValidation("collections");
			userCredentialSchema.deleteMetadataWithoutValidation("globalGroups");
			userCredentialSchema.deleteMetadataWithoutValidation("phone");
			userCredentialSchema.deleteMetadataWithoutValidation("fax");
			userCredentialSchema.deleteMetadataWithoutValidation("jobTitle");
			userCredentialSchema.deleteMetadataWithoutValidation("address");

			//Les métadonnées suivantes sont déplacées dans User par le script de migration : domain, msExchangeDelegateList
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			MetadataBuilder userCreateDomain = userCredentialSchema.createUndeletable("domain")
					.setType(MetadataValueType.STRING).setDefaultValue("");
			MetadataBuilder userCreateMsExchange = userCredentialSchema.createUndeletable("msExchangeDelegateList").setType(MetadataValueType.STRING)
					.setMultivalue(true);
			List<String> collections = appLayerFactory.getCollectionsManager().getCollectionCodes();
			userServices.transferDomainsAndMsDelegatesFromCredentialsToUser(collections);

			//Les utilisateurs qui ne sont pas actifs sont supprimés logiquement
			userServices.logicallyRemoveAllNonActiveUsers();
			//Les utilisateurs désactivés qui ne sont pas utilisés sont supprimé physiquement
			for (String collection :
					collections) {
				userServices.safePhysicalDeleteAllUnusedUsers(collection);
			}

			//GROUPS
			//GlobalGroup est supprimé.
			List<SystemWideGroup> glGroups = userServices.getAllGlobalGroups();
			for (SystemWideGroup group : glGroups) {
				userServices.physicallyRemoveGlobalGroup(group);
			}

			//la métadonnée "usersAutomaticallyAddedToCollections" est supprimée
			MetadataSchemaBuilder groupSchema = typesBuilder.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder glGroupSchema = typesBuilder.getSchemaType(GlobalGroup.SCHEMA_TYPE).getDefaultSchema();
			glGroupSchema.deleteMetadataWithoutValidation("usersAutomaticallyAddedToCollections");

			//les attributs "locallyCreated", "hierarchy" et "status" sont déplacés dans Group
			groupSchema.createUndeletable("status")
					.defineAsEnum(GlobalGroupStatus.class).setDefaultRequirement(true);
			groupSchema.createUndeletable("locallyCreated")
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(true);
			groupSchema.createUndeletable("hierarchy")
					.setType(MetadataValueType.STRING);

			List<SystemWideGroup> groups = userServices.getAllGroups();
			//Les groupes qui ne sont pas actifs sont supprimés logiquement
			//userServices.logicallyRemoveAllNonActiveGroups();
		}
	}
}
