package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CoreMigrationFrom_9_4_updateServerPing extends MigrationHelper implements MigrationScript {

	private static final String publicVerificationKey = "-----BEGIN PUBLIC KEY-----\n" +
														"MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAnarbxzZPjnzeIacpp6Lb\n" +
														"xCHHJUnXsMMAD9AGBVha1evXwp2QS1lEIF9jkQ60AqpFGvofw1a6iOcSjc1OvCQ2\n" +
														"kRq8p2+IFup5EezIy0nR/nBu19p5WAaF06zzT23n2h32xkLsJrDahFrVlrds1MHN\n" +
														"9Nq55qRguB8Rr2hml06vrSwpSKqBAl4luZJTPXagjPPoTmzu3qmR4p2ab/cSB8wW\n" +
														"yyE9oPdSntq3Sad9DRItSKRqI2V0AO7c1WJr37MuKoCNyYP02a9OCzHc13bLMgMY\n" +
														"KHUmDLo22yvSI4nysjpuQNVPxrebicZmqEcPepjYIwOX0MKqyR3CIiWChjkxfvuF\n" +
														"SJsyF65jaY7TcYheR/ddmZt0RFOCj/UbCZb/mBzEAzsdtK9NqhERh6zxKK4QWz+i\n" +
														"mltMbWKLvyfeOrsEo+YCai4ZM5AxV4JRGZmbyKX7zGzULqq4jDDzjfsm0fazflp5\n" +
														"tp7CWLueeYmP1hflC+VGRy/2Tf1QadPINXJivu3t5EbHr/bbgFQ5ZI/T7lhVfeVP\n" +
														"9HcV1asPwWS+fZJ241R5BrqG36Y0bz1ALaUu0NetacUZ3rVLEYtrIrSeuWpXwSPw\n" +
														"P5Azid7i9QjfKc+lu7mQG3+6jXwHGC8etb2PhSH05L4cpaLtEprFKbh2X2x/o7gy\n" +
														"99vvDN0ohj/bwkYXOStJxNECAwEAAQ==\n" +
														"-----END PUBLIC KEY-----\n";

	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFrom_9_4(collection, migrationResourcesProvider, appLayerFactory).migrate();

			appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager()
					.setValue(ConstellioEIMConfigs.INTERNAL_SERVER_URL, "https://update.cloud.constellio.com/constellio/");
			try {
				FileUtils.writeStringToFile(appLayerFactory.getModelLayerFactory().getFoldersLocator().getVerificationKey(),
						publicVerificationKey, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException("Error encountered when trying to move the verification key to it's correct folder", e);
			}
		}
	}

	class CoreSchemaAlterationFrom_9_4 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFrom_9_4(String collection,
											MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userCredentialsSchema = typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE);
			userCredentialsSchema.createUndeletable(UserCredential.NOT_A_LTS_NOTIFICATION_VIEW_DATE)
					.setType(MetadataValueType.DATE);
			userCredentialsSchema.createUndeletable(UserCredential.NEW_VERSIONS_NOTIFICATION_VIEW_DATE)
					.setType(MetadataValueType.DATE);
			userCredentialsSchema.createUndeletable(UserCredential.LICENSE_NOTIFICATION_VIEW_DATE)
					.setType(MetadataValueType.DATE);
			userCredentialsSchema.createUndeletable(UserCredential.LTS_END_OF_LIFE_NOTIFICATION_VIEW_DATE)
					.setType(MetadataValueType.DATE);
		}
	}
}
