package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.JasperFilePrintableValidator;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by constellios on 2017-07-13.
 */
public class RMMigrationTo7_5 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new RMMigrationTo7_5.SchemaAlterationFor7_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).get(Task.LINKED_CONTAINERS).removeOldReferences()
					.defineReferencesTo(typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE));
            MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
            metadataSchemaBuilder.createSystemReserved(Folder.IS_RESTRICTED_ACCESS).setType(MetadataValueType.BOOLEAN).setUndeletable(true);

            MetadataSchemaBuilder builder = typesBuilder.getSchemaType(TemporaryRecord.SCHEMA_TYPE).createCustomSchema(SIParchive.SCHEMA_NAME);
            builder.createUndeletable(SIParchive.NAME).setType(MetadataValueType.STRING).defineDataEntry().asManual();
            builder.createUndeletable(SIParchive.CREATION_DATE).setType(MetadataValueType.DATE_TIME).setDefaultValue(new LocalDateTime()).setEssential(true);
            builder.createUndeletable(SIParchive.USER).setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE)).defineDataEntry().asManual();

            MetadataSchemaBuilder printableReportMetadataSchemaBuilder = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE).getDefaultSchema();
            printableReportMetadataSchemaBuilder.get(Printable.JASPERFILE).addValidator(JasperFilePrintableValidator.class);
        }

	}
}
