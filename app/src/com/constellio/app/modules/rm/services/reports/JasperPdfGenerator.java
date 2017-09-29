package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.services.contents.ContentManager;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.w3c.dom.Document;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JasperPdfGenerator {

    private String collection;

    private AppLayerFactory factory;

    private String xmlGenerated;

    private ContentManager contentManager;

    private ReportXMLGeneratorV2 reportXMLGeneratorV2;

    public JasperPdfGenerator(XmlGenerator reportXMLGeneratorV2) {
        this(reportXMLGeneratorV2.getCollection(), reportXMLGeneratorV2.getFactory(), reportXMLGeneratorV2.generateXML());
    }

    public JasperPdfGenerator(String collection, AppLayerFactory factory, String xmlGenerated) {
        this.collection = collection;
        this.factory = factory;
        this.contentManager = this.factory.getModelLayerFactory().getContentManager();
        this.xmlGenerated = xmlGenerated;
        JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
        jasperReportsContext.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        jasperReportsContext.setProperty("net.sf.jasperreports.default.font.name", "Arial");
    }

    //FIXME Utiliser autre chose qu'un content.
    public File createPDFFromXmlAndJasperFile(File jasperFile, String format) throws JRException {
        Map<String, Object> params = new HashMap<>();
        Document document;
        String PDFFile = "";
        String reportFile = "";
        IOServices ioServices = factory.getModelLayerFactory().getIOServicesFactory().newIOServices();
        try {
            document = JRXmlUtils.parse(new ByteArrayInputStream(this.xmlGenerated.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleRuntimeException(e);
        }
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
        File tempJasperFile = ioServices.newTemporaryFile("jasper");
        try{

            ioServices.copyFile(jasperFile, tempJasperFile);
            reportFile = JasperFillManager.fillReportToFile(tempJasperFile.getAbsolutePath(), params);
            PDFFile = JasperExportManager.exportReportToPdfFile(reportFile);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ioServices.deleteQuietly(new File(reportFile));
            ioServices.deleteQuietly(tempJasperFile);
        }
        return new File(PDFFile);
    }
}
