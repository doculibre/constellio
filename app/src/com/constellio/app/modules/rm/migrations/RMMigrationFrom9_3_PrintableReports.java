package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.JasperFilesPrintableValidator;

import static com.constellio.app.entities.modules.MigrationHelper.order;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;

public class RMMigrationFrom9_3_PrintableReports implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public String getResourcesDirectoryName() {
		return getVersion().replace(".", "_");
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_4(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

		transaction.add(order(collection, appLayerFactory, "form", displayManager.getSchema(collection, PrintableReport.SCHEMA_NAME),
				PrintableReport.TITLE, PrintableReport.JASPERFILE, PrintableReport.JASPER_SUBREPORT_FILES,
				PrintableReport.RECORD_TYPE, PrintableReport.RECORD_SCHEMA, PrintableReport.SUPPORTED_EXTENSIONS)
				.withNewDisplayMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.JASPER_SUBREPORT_FILES)
				.withNewDisplayMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.SUPPORTED_EXTENSIONS)
				.withNewDisplayMetadataQueued(Printable.DEFAULT_SCHEMA + "_" + Printable.TEMPLATE_VERSION));

		displayManager.execute(transaction.build());
	}

	private static class SchemaAlterationFor9_4 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
							   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder printableReportSchema = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE)
					.getCustomSchema(PrintableReport.SCHEMA_TYPE);
			printableReportSchema.createUndeletable(PrintableReport.JASPER_SUBREPORT_FILES).setEssential(true)
					.setType(MetadataValueType.CONTENT).setMultivalue(true).defineDataEntry().asManual()
					.addValidator(JasperFilesPrintableValidator.class);
			printableReportSchema.createUndeletable(PrintableReport.SUPPORTED_EXTENSIONS).setEssential(true)
					.setType(ENUM).setMultivalue(true).defineAsEnum(PrintableExtension.class).setDefaultRequirement(true);

			MetadataSchemaBuilder printableSchema = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE).getDefaultSchema();
			printableSchema.createUndeletable(PrintableLabel.TEMPLATE_VERSION)
					.setType(ENUM).defineAsEnum(TemplateVersionType.class).setDefaultRequirement(true);
		}
	}
}
