package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RMMigrationFrom9_4_PrintableLabelsAndReportsVersion implements MigrationScript {

	@Override
	public String getResourcesDirectoryName() {
		return getVersion().replace(".", "_");
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		migrateAllPrintableLabelRecords(collection, appLayerFactory);
		migrateAllPrintableReportRecords(collection, appLayerFactory);
	}

	private void migrateAllPrintableLabelRecords(String collection, AppLayerFactory appLayerFactory) throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.printable_label.schema()).returnAll());
		List<Record> records = appLayerFactory.getModelLayerFactory().newSearchServices().search(query);
		for (Record record : records) {
			PrintableLabel printableLabel = rm.wrapPrintableLabel(record);
			printableLabel.setTemplateVersion(TemplateVersionType.CONSTELLIO_5);
			transaction.add(printableLabel);
		}

		recordServices.executeInBatch(transaction);
	}

	private void migrateAllPrintableReportRecords(String collection, AppLayerFactory appLayerFactory) throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.printable_report.schema()).returnAll());
		List<Record> records = appLayerFactory.getModelLayerFactory().newSearchServices().search(query);
		for (Record record : records) {
			PrintableReport printableReport = rm.wrapPrintableReport(record);
			printableReport.setTemplateVersion(TemplateVersionType.CONSTELLIO_5);
			printableReport.setSupportedExtensions(Collections.singletonList(PrintableExtension.PDF));
			transaction.add(printableReport);
		}

		recordServices.executeInBatch(transaction);
	}
}
