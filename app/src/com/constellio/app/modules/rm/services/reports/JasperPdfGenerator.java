package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.joda.time.LocalDate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marco on 2017-06-28.
 */
public class JasperPdfGenerator {

    private String collection;

    private AppLayerFactory factory;

    private String xmlGenerated;

    private ContentManager contentManager;

    private ReportXMLGeneratorV2 reportXMLGeneratorV2;

    public JasperPdfGenerator(ReportXMLGeneratorV2 reportXMLGeneratorV2) throws Exception{
        this(reportXMLGeneratorV2.getCollection(), reportXMLGeneratorV2.getFactory(), reportXMLGeneratorV2.generateXML());
    }

    public JasperPdfGenerator(String collection, AppLayerFactory factory, String xmlGenerated) throws Exception {
        this.collection = collection;
        this.factory = factory;
        this.contentManager = this.factory.getModelLayerFactory().getContentManager();
        this.xmlGenerated = xmlGenerated;
        JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
        jasperReportsContext.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        jasperReportsContext.setProperty("net.sf.jasperreports.default.font.name", "Arial");
    }

    public Content createPDFFromXmlAndJasperFile(File jasperFile, String format) throws Exception {
        Map<String, Object> params = new HashMap<>();
        org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(this.xmlGenerated.getBytes("UTF-8")));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
        String reportFile = JasperFillManager.fillReportToFile(jasperFile.getAbsolutePath(), params);
        String PDFFile = JasperExportManager.exportReportToPdfFile(reportFile);
        File file = new File(PDFFile);
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file), "Etiquette")
                .getContentVersionDataSummary();
        return contentManager.createFileSystem(ReportXMLGeneratorV2.escapeForXmlTag(format) + "-" + LocalDate.now(), upload);
    }
}
