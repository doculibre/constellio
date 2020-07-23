package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

import java.util.Arrays;

import static com.constellio.data.conf.DigitSeparatorMode.THREE_LEVELS_OF_ONE_DIGITS;
import static com.constellio.data.conf.DigitSeparatorMode.TWO_DIGITS;
import static com.constellio.data.conf.HashingEncoding.BASE64;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;

public class CoreMigrationTo_6_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

			if (!modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
					.hasType(UserCredential.SCHEMA_TYPE)) {

				new CoreSchemaAlterationFor6_0(collection, provider, appLayerFactory).migrate();

				SchemasRecordsServices schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
				RecordsCache cache = modelLayerFactory.getRecordsCaches().getCache(Collection.SYSTEM_COLLECTION);

			}

			try {
				modelLayerFactory.newUserServices().getUserInfos("admin");
			} catch (UserServicesRuntimeException_NoSuchUser e) {
				createAdminUser(modelLayerFactory);
			}

		}
	}

	public void createAdminUser(ModelLayerFactory modelLayerFactory) {
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		//String serviceKey = "adminkey";
		String password = "password";
		String username = "admin";
		String firstName = "System";
		String lastName = "Admin";
		String email = "admin@organization.com";
		UserCredentialStatus status = UserCredentialStatus.ACTIVE;
		String domain = "";
		boolean isSystemAdmin = true;

		UserServices userServices = modelLayerFactory.newUserServices();
		UserAddUpdateRequest request = userServices.addUpdate(username)
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setServiceKey(null)
				.setSystemAdmin(isSystemAdmin)
				.setStatus(status)
				.setDomain(domain)
				.setMsExchDelegateListBL(Arrays.asList(""))
				.setDn(null);
		userServices.execute(request);
		AuthenticationService authenticationService = modelLayerFactory.newAuthenticationService();
		if (authenticationService.supportPasswordChange()) {
			authenticationService.changePassword("admin", password);
		}

		if (modelLayerFactory.getCollectionsListManager().getCollections().size() == 1) {
			dataLayerFactory.getDataLayerConfiguration().setHashingEncoding(BASE64_URL_ENCODED);
			dataLayerFactory.getDataLayerConfiguration().setContentDaoFileSystemDigitsSeparatorMode(THREE_LEVELS_OF_ONE_DIGITS);
		} else {
			dataLayerFactory.getDataLayerConfiguration().setHashingEncoding(BASE64);
			dataLayerFactory.getDataLayerConfiguration().setContentDaoFileSystemDigitsSeparatorMode(TWO_DIGITS);
		}
	}

	private class CoreSchemaAlterationFor6_0 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
			createUserCredentialSchema(builder);
			createGlobalGroupSchema(builder);
		}

		private void createUserCredentialSchema(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypeBuilder credentialsTypeBuilder = builder.createNewSchemaType(UserCredential.SCHEMA_TYPE);
			credentialsTypeBuilder.setSecurity(false);
			MetadataSchemaBuilder credentials = credentialsTypeBuilder.getDefaultSchema();

			credentials.getMetadata(CommonMetadataBuilder.TITLE).defineDataEntry().asCalculated(UserTitleCalculator.class);

			credentials.createUndeletable(UserCredential.USERNAME).setType(MetadataValueType.STRING)
					.setDefaultRequirement(true).setUniqueValue(true).setUnmodifiable(true);
			credentials.createUndeletable(UserCredential.FIRST_NAME).setType(MetadataValueType.STRING);
			credentials.createUndeletable(UserCredential.LAST_NAME).setType(MetadataValueType.STRING);
			credentials.createUndeletable(UserCredential.EMAIL).setType(MetadataValueType.STRING)
					.setUniqueValue(false).addValidator(EmailValidator.class);
			credentials.createUndeletable(UserCredential.PERSONAL_EMAILS).setType(MetadataValueType.STRING)
					.setMultivalue(true);
			credentials.createUndeletable(UserCredential.SERVICE_KEY).setType(MetadataValueType.STRING).setEncrypted(true);
			credentials.createUndeletable(UserCredential.TOKEN_KEYS).setType(MetadataValueType.STRING).setMultivalue(true)
					.setEncrypted(true);
			credentials.createUndeletable(UserCredential.TOKEN_EXPIRATIONS).setType(MetadataValueType.DATE_TIME)
					.setMultivalue(true);
			credentials.createUndeletable(UserCredential.SYSTEM_ADMIN).setType(MetadataValueType.BOOLEAN)
					.setDefaultRequirement(true).setDefaultValue(false);
			credentials.createUndeletable(UserCredential.COLLECTIONS).setType(MetadataValueType.STRING).setMultivalue(true);
			credentials.createUndeletable(UserCredential.GLOBAL_GROUPS).setType(MetadataValueType.STRING).setMultivalue(true);
			credentials.createUndeletable(UserCredential.STATUS).defineAsEnum(UserCredentialStatus.class)
					.setDefaultRequirement(true);
			credentials.createUndeletable(UserCredential.DOMAIN).setType(MetadataValueType.STRING);
			credentials.createUndeletable(UserCredential.MS_EXCHANGE_DELEGATE_LIST).setType(MetadataValueType.STRING)
					.setMultivalue(true);
			credentials.createUndeletable(UserCredential.DN).setType(MetadataValueType.STRING);
		}

		private void createGlobalGroupSchema(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypeBuilder credentialsTypeBuilder = builder.createNewSchemaType(GlobalGroup.SCHEMA_TYPE);
			credentialsTypeBuilder.setSecurity(false);
			MetadataSchemaBuilder groups = credentialsTypeBuilder.getDefaultSchema();

			groups.createUniqueCodeMetadata();
			groups.createUndeletable(GlobalGroup.NAME).setType(MetadataValueType.STRING).setDefaultRequirement(true);
			groups.createUndeletable(GlobalGroup.COLLECTIONS).setType(MetadataValueType.STRING).setMultivalue(true);
			groups.createUndeletable(GlobalGroup.PARENT).setType(MetadataValueType.STRING);
			groups.createUndeletable(GlobalGroup.STATUS).defineAsEnum(GlobalGroupStatus.class).setDefaultRequirement(true);
			groups.createUndeletable(GlobalGroup.HIERARCHY).setType(MetadataValueType.STRING);
			groups.createUndeletable(GlobalGroup.LOCALLY_CREATED).setType(MetadataValueType.BOOLEAN);
		}
	}
}
