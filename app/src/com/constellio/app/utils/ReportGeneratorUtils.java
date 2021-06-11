package com.constellio.app.utils;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.services.reports.JasperReportServices;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.modules.rm.services.reports.printable.PrintableGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceType;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ReportGeneratorUtils {
	public static Component saveButtonClick(AppLayerFactory factory, String collection, String schemaType,
											PrintableReportTemplate selectedTemplate, int numberOfCopies,
											List<String> ids, LogicalSearchQuery query,
											Locale locale, UserVO user, PrintableExtension printableExtension) {
		try {
			JasperReportServices jasperReportServices = new JasperReportServices(collection, factory);
			PrintableGeneratorParams printableGeneratorParams = PrintableGeneratorParams.builder()
					.XMLDataSourceType(XMLDataSourceType.REPORT)
					.printableId(selectedTemplate.getId())
					.printableExtension(printableExtension)
					.numberOfCopies(numberOfCopies)
					.schemaType(schemaType)
					.recordIds(ids)
					.query(query)
					.locale(locale)
					.username(user.getUsername())
					.build();
			InputStream reportInputStream = jasperReportServices.generatePrintable(printableGeneratorParams);

			VerticalLayout newLayout = new VerticalLayout();
			String title = selectedTemplate.getTitle() + ISODateTimeFormat.dateTime().print(new LocalDateTime()) + printableExtension.getExtension();
			newLayout.addComponents(new LabelViewer(reportInputStream, title,
					factory.getModelLayerFactory().getIOServicesFactory().newIOServices()));
			newLayout.setWidth("100%");
			return newLayout;
		} catch (Exception e) {
			//JRException check what it is.
			e.printStackTrace();
		}
		return null;
	}

	public static List<PrintableReportTemplate> getPrintableReportTemplate(AppLayerFactory factory, String collection,
																		   String recordSchema,
																		   PrintableReportListPossibleType currentSchema) {
		List<PrintableReportTemplate> printableReportTemplateList = new ArrayList<>();
		MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaType printableReportSchemaType = metadataSchemasManager.getSchemaTypes(collection)
				.getSchemaType(Printable.SCHEMA_TYPE);
		LogicalSearchCondition conditionCustomSchema = from(printableReportSchemaType)
				.where(printableReportSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE).get(PrintableReport.RECORD_SCHEMA))
				.isEqualTo(recordSchema);
		LogicalSearchCondition conditionSchemaType = from(printableReportSchemaType)
				.where(printableReportSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE).get(PrintableReport.RECORD_TYPE))
				.isEqualTo(currentSchema.getSchemaType());
		LogicalSearchCondition schemaCondition = from(printableReportSchemaType)
				.where(printableReportSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE).get(PrintableReport.RECORD_TYPE))
				.isEqualTo(PrintableReport.SCHEMA_NAME);
		LogicalSearchCondition notDisabledCondition = from(printableReportSchemaType)
				.where(printableReportSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE).get(PrintableReport.DISABLED))
				.isFalseOrNull();
		List<Record> records = factory.getModelLayerFactory().newSearchServices().cachedSearch(new LogicalSearchQuery(
				from(printableReportSchemaType).whereAllConditions(schemaCondition, conditionCustomSchema, conditionSchemaType, notDisabledCondition)));
		for (Record record : records) {
			printableReportTemplateList.add(new PrintableReportTemplate(record.getId(), record.getTitle(),
					record.<Content>get(printableReportSchemaType.getCustomSchema(PrintableReport.SCHEMA_TYPE)
							.get(PrintableReport.JASPERFILE))));
		}
		return printableReportTemplateList;
	}
}
