package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.File;
import java.util.Locale;

import static com.constellio.app.modules.rm.enums.TemplateVersionType.CONSTELLIO_10;
import static com.constellio.app.modules.rm.services.reports.printable.PrintableExtension.PDF;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Collections.singletonList;

public class RMMigrationFrom9_4_PrintableReports implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlteration(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

		transaction.add(displayManager.getSchema(collection, PrintableReport.SCHEMA_NAME)
				.withNewDisplayMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.ADD_CHILDREN)
				.withNewFormMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.ADD_CHILDREN)
				.withNewDisplayMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.ADD_PARENTS)
				.withNewFormMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.ADD_PARENTS)
				.withNewDisplayMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.DISABLED)
				.withNewFormMetadataQueued(Printable.SCHEMA_TYPE + "_" + PrintableReport.DISABLED)
		);

		displayManager.execute(transaction.build());

		addDefaultTemplates(collection, appLayerFactory);
	}

	private void addDefaultTemplates(String collection, AppLayerFactory appLayerFactory) throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();

		Transaction tx = new Transaction();
		File documentTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/DocumentMetadataReport.jasper");
		Content documentTemplateContent = contentManager.createSystemContentFrom(documentTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Document.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(documentTemplateContent).setIsDeletable(false).setCode("DocumentMetadataReport")
				.setTitle(Locale.FRENCH, "Métadonnée de document")
				.setTitle(Locale.ENGLISH, "Document metadata");

		File folderTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/FolderMetadataReport.jasper");
		Content folderTemplateContent = contentManager.createSystemContentFrom(folderTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Folder.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(folderTemplateContent).setIsDeletable(false).setCode("FolderMetadataReport")
				.setTitle(Locale.FRENCH, "Métadonnée de dossier")
				.setTitle(Locale.ENGLISH, "Folder metadata");

		File taskTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/TaskMetadataReport.jasper");
		Content taskTemplateContent = contentManager.createSystemContentFrom(taskTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Task.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(taskTemplateContent).setIsDeletable(false).setCode("TaskMetadataReport")
				.setTitle(Locale.FRENCH, "Métadonnée de tâche")
				.setTitle(Locale.ENGLISH, "Task metadata");

		File documentListTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/DocumentMetadataListReport.jasper");
		Content documentListTemplateContent = contentManager.createSystemContentFrom(documentListTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Document.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(documentListTemplateContent).setIsDeletable(false).setCode("DocumentMetadataListReport")
				.setTitle(Locale.FRENCH, "Liste de documents")
				.setTitle(Locale.ENGLISH, "Document list");

		File folderListTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/FolderMetadataListReport.jasper");
		Content folderListTemplateContent = contentManager.createSystemContentFrom(folderListTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Folder.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(folderListTemplateContent).setIsDeletable(false).setCode("FolderMetadataListReport")
				.setTitle(Locale.FRENCH, "Liste de dossiers")
				.setTitle(Locale.ENGLISH, "Folder list");

		File taskListTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "reports/TaskMetadataListReport.jasper");
		Content taskListTemplateContent = contentManager.createSystemContentFrom(taskListTemplate);
		tx.add(rm.newPrintableReport().setTemplateVersion(CONSTELLIO_10).setReportType(Task.SCHEMA_TYPE)
				.setSupportedExtensions(singletonList(PDF))).setOptimized(true)
				.setJasperFile(taskListTemplateContent).setIsDeletable(false).setCode("TaskMetadataListReport")
				.setTitle(Locale.FRENCH, "Liste de tâches")
				.setTitle(Locale.ENGLISH, "Task list");

		recordServices.execute(tx);
	}

	private static class SchemaAlteration extends MetadataSchemasAlterationHelper {
		SchemaAlteration(String collection, MigrationResourcesProvider migrationResourcesProvider,
						 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder printableSchema = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE).getDefaultSchema();
			printableSchema.createUndeletable(Printable.DISABLED).setType(BOOLEAN);
			printableSchema.createSystemReserved(Printable.CODE).setType(STRING);

			MetadataSchemaBuilder printableReportSchema = typesBuilder.getSchemaType(Printable.SCHEMA_TYPE)
					.getCustomSchema(PrintableReport.SCHEMA_TYPE);
			printableReportSchema.createUndeletable(PrintableReport.ADD_CHILDREN).setType(BOOLEAN);
			printableReportSchema.createUndeletable(PrintableReport.ADD_PARENTS).setType(BOOLEAN);
			printableSchema.createSystemReserved(PrintableReport.OPTIMIZED).setType(BOOLEAN);
			printableSchema.createSystemReserved(PrintableReport.DEPTH).setType(INTEGER);
		}
	}
}
