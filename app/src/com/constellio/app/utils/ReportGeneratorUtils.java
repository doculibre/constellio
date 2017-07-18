package com.constellio.app.utils;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.services.reports.JasperPdfGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ReportGeneratorUtils {
    public static Component saveButtonClick(AppLayerFactory factory, String collection, String schemaType, PrintableReportTemplate selectedTemplate, int numberOfCopies, List<String> ids) {
        InputStream selectedJasperFileContentInputStream = null;
        File temporaryJasperFile = null;
        try {
            IOServicesFactory ioServicesFactory = factory.getModelLayerFactory().getIOServicesFactory();
            ContentManager contentManager = factory.getModelLayerFactory().getContentManager();
            XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(
                    numberOfCopies);
            xmlGeneratorParameters.setElementWithIds(schemaType, ids);
            XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(factory, collection,
                    xmlGeneratorParameters);
            JasperPdfGenerator jasperPdfGenerator = new JasperPdfGenerator(xmlReportGenerator);
            selectedJasperFileContentInputStream = contentManager
                    .getContentInputStream(selectedTemplate.getJasperFile().getCurrentVersion().getHash(),
                            "ReportGeneratorButtonReport.GeneratorButtonForm#saveButtonClick");
            temporaryJasperFile = ioServicesFactory.newIOServices().newTemporaryFile("jasper.jasper");
            FileUtils.copyInputStreamToFile(selectedJasperFileContentInputStream, temporaryJasperFile);
            String title =
                    selectedTemplate.getTitle() + ISODateTimeFormat.dateTime().print(new LocalDateTime())
                            + ".pdf";
            File generatedJasperFile = jasperPdfGenerator.createPDFFromXmlAndJasperFile(temporaryJasperFile, title);
            VerticalLayout newLayout = new VerticalLayout();
            newLayout.addComponents(new LabelViewer(generatedJasperFile, title));
            newLayout.setWidth("100%");
            return newLayout;
        } catch (JRException | IOException e) {
            //JRException check what it is.
            e.printStackTrace();
        } finally {
            factory.getModelLayerFactory().getIOServicesFactory().newIOServices().closeQuietly(selectedJasperFileContentInputStream);
            factory.getModelLayerFactory().getIOServicesFactory().newIOServices().deleteQuietly(temporaryJasperFile);
        }
        return null;
    }

    public static  List<PrintableReportTemplate> getPrintableReportTemplate(AppLayerFactory factory, String collection, String recordSchema, PrintableReportListPossibleType currentSchema) {
        List<PrintableReportTemplate> printableReportTemplateList = new ArrayList<>();
        MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
        MetadataSchema printableReportSchemaType = metadataSchemasManager.getSchemaTypes(collection)
                .getSchemaType(Printable.SCHEMA_TYPE).getCustomSchema(PrintableReport.SCHEMA_NAME);
        LogicalSearchCondition conditionCustomSchema = from(printableReportSchemaType)
                .where(printableReportSchemaType.get(PrintableReport.RECORD_SCHEMA)).isEqualTo(recordSchema);
        LogicalSearchCondition conditionSchemaType = from(printableReportSchemaType)
                .where(printableReportSchemaType.getMetadata(PrintableReport.RECORD_TYPE)).isEqualTo(currentSchema.getSchemaTypeUpperCase());
        List<Record> records = factory.getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(
                from(printableReportSchemaType).whereAllConditions(conditionCustomSchema, conditionSchemaType)));
        for (Record record : records) {
            printableReportTemplateList.add(new PrintableReportTemplate(record.getId(), record.getTitle(),
                    record.<Content>get(printableReportSchemaType.getMetadata(PrintableReport.JASPERFILE))));
        }
        return printableReportTemplateList;
    }
}
