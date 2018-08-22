package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.*;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserCredentialAndGlobalGroupsMigration;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.data.conf.DigitSeparatorMode.THREE_LEVELS_OF_ONE_DIGITS;
import static com.constellio.data.conf.DigitSeparatorMode.TWO_DIGITS;
import static com.constellio.data.conf.HashingEncoding.BASE64;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;

public class CoreMigrationTo_8_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFor8_1(collection, provider, appLayerFactory).migrate();
		}
	}

	private class CoreSchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(SolrUserCredential.SCHEMA_TYPE)
					.createUndeletable(SolrUserCredential.HAS_AGREED_TO_PRIVACY_POLICY).setType(MetadataValueType.BOOLEAN);
		}
	}
}
