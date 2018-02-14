package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
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

    /**
     * Use a XMLGenerator to get the required value. Note that XML will be generated in the Constructor.
     * @param xmlGenerator
     */
    public JasperPdfGenerator(AbstractXmlGenerator xmlGenerator) {
        this(xmlGenerator.getCollection(), xmlGenerator.getFactory(), xmlGenerator.generateXML());
    }

    public JasperPdfGenerator(String collection, AppLayerFactory factory, String xmlGenerated) {
        this.collection = collection;
        this.factory = factory;
        this.xmlGenerated = xmlGenerated;
        JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
        jasperReportsContext.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        jasperReportsContext.setProperty("net.sf.jasperreports.default.font.name", "Arial");
    }

    /**
     * Method that takes a JasperReport (.jasper) file and the xml generated in the constructor to create a report.
     * @param jasperFile File jasper file
     * @param format unused by the app.
     * @return File with the report.
     * @throws JRException
     */
    public File createPDFFromXmlAndJasperFile(File jasperFile) throws JRException {
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
