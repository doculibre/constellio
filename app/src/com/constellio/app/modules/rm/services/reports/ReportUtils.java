package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.drew.metadata.MetadataException;
import com.itextpdf.text.DocumentException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import net.sf.jasperreports.engine.export.JRPdfExporter;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Marco on 2017-01-16.
 */
public class ReportUtils {
    public static final boolean DEV = true;

    RMSchemasRecordsServices rm;
    SearchServices ss;
    RecordServices recordServices;

    public ReportUtils(String collection, AppLayerFactory appLayerFactory) {
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        ss = appLayerFactory.getModelLayerFactory().newSearchServices();
        recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche tous les dossiers d'une collection et crée un XML à partir
     * des métadonnées de ces fichiers.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertFolderToXML(String... parameters) throws Exception {
        LogicalSearchCondition condition = from(rm.folder.schemaType()).where(ALL);
        List<Folder> foldersFound = rm.wrapFolders(ss.search(new LogicalSearchQuery(condition)));
        Document document = new Document();
        Element root = new Element("folders");
        document.setRootElement(root);
        if (parameters == null) {
            List<String> temp = new ArrayList<>();
            for (Metadata m : rm.folder.schema().getMetadatas()) {
                temp.add(m.getCode());
            }
            parameters = temp.toArray(new String[0]);
        }
        for (Folder fol : foldersFound) {
            Element folder = new Element("folder");
            Element metadatas = new Element("metadatas");
            for (String metadonnee : parameters) {
                if (!rm.folder.schema().hasMetadataWithCode(metadonnee))
                    throw new MetadataException("No such metadata " + metadonnee);
                Element m = new Element(metadonnee);
                m.setText(fol.get(metadonnee) + "");
                metadatas.addContent(m);
            }
            folder.setContent(metadatas);
            root.addContent(folder);
        }
        XMLOutputter xmlOutputter = new XMLOutputter(DEV ? Format.getPrettyFormat() : Format.getCompactFormat());
        return xmlOutputter.outputString(document);
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche le dossier correspondant au id dans la collection et crée un XML à partir
     * des métadonnées de ce dossier.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertFolderWithIdentifierToXML(String id, String... parameters) throws Exception {
        if (id == null) throw new NullArgumentException("The id is null !");
        return convertFolderWithIdentifierToXML(Arrays.asList(id), parameters);
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche les dossiers correspondant aux ids dans la collection et crée un XML à partir
     * des métadonnées de ces dossiers.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertFolderWithIdentifierToXML(List<String> ids, String... parameters) throws Exception {
        if (ids == null) throw new NullArgumentException("The ids list is null !");
        LogicalSearchCondition condition = from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(ids);
        List<Folder> foldersFound = rm.wrapFolders(ss.search(new LogicalSearchQuery(condition)));
        Document document = new Document();
        Element root = new Element("folders");
        document.setRootElement(root);
        if (parameters == null) {
            List<String> temp = new ArrayList<>();
            for (Metadata m : rm.folder.schema().getMetadatas()) {
                temp.add(m.getCode());
            }
            parameters = temp.toArray(new String[0]);
        }
        for (Folder fol : foldersFound) {
            Element folder = new Element("folder");
            Element metadatas = new Element("metadatas");
            for (String metadonnee : parameters) {
                if (!rm.folder.schema().hasMetadataWithCode(metadonnee))
                    throw new MetadataException("No such metadata " + metadonnee);
                Element m = new Element(metadonnee);
                m.setText(fol.get(metadonnee) + "");
                metadatas.addContent(m);
            }
            folder.setContent(metadatas);
            root.addContent(folder);
        }
        XMLOutputter xmlOutputter = new XMLOutputter(DEV ? Format.getPrettyFormat() : Format.getCompactFormat());
        return xmlOutputter.outputString(document);
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche tous les conteneurs d'une collection et crée un XML à partir
     * des métadonnées de ces fichiers.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertContainerToXML(String... parameters) throws Exception {
        LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(ALL);
        List<ContainerRecord> containersFound = rm.wrapContainerRecords(ss.search(new LogicalSearchQuery(condition)));
        Document document = new Document();
        Element root = new Element("containers");
        document.setRootElement(root);
        if (parameters == null) {
            List<String> temp = new ArrayList<>();
            for (Metadata m : rm.containerRecord.schema().getMetadatas()) {
                temp.add(m.getCode());
            }
            parameters = temp.toArray(new String[0]);
        }
        for (ContainerRecord con : containersFound) {
            Element container = new Element("container");
            Element metadatas = new Element("metadatas");
            for (String metadonnee : parameters) {
                if (!rm.containerRecord.schema().hasMetadataWithCode(metadonnee))
                    throw new MetadataException("No such metadata " + metadonnee);
                Element m = new Element(metadonnee);
                m.setText(con.get(metadonnee) + "");
                metadatas.addContent(m);
            }
            container.setContent(metadatas);
            root.addContent(container);
        }
        XMLOutputter xmlOutputter = new XMLOutputter(DEV ? Format.getPrettyFormat() : Format.getCompactFormat());
        return xmlOutputter.outputString(document);
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche le conteneur correspondant au id dans la collection et crée un XML à partir
     * des métadonnées de ce contenant.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertContainerWithIdentifierToXML(String id, String... parameters) throws Exception {
        if (id == null) throw new NullArgumentException("The id is null !");
        return convertContainerWithIdentifierToXML(Arrays.asList(id), parameters);
    }

    /**
     * Cette méthode prend une liste de métadonnées en paramètre.
     * On cherche les conteneurs correspondants aux ids dans la collection et crée un XML à partir
     * des métadonnées de ces contenants.
     *
     * @param parameters
     * @return String - Le document XML
     * @throws Exception
     */
    public String convertContainerWithIdentifierToXML(List<String> ids, String... parameters) throws Exception {
        if (ids == null) throw new NullArgumentException("The ids list is null !");
        LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(ids);
        List<ContainerRecord> containersFound = rm.wrapContainerRecords(ss.search(new LogicalSearchQuery(condition)));
        Document document = new Document();
        Element root = new Element("containers");
        document.setRootElement(root);
        if (parameters == null) {
            List<String> temp = new ArrayList<>();
            for (Metadata m : rm.containerRecord.schema().getMetadatas()) {
                temp.add(m.getCode());
            }
            parameters = temp.toArray(new String[0]);
        }
        for (ContainerRecord con : containersFound) {
            Element container = new Element("container");
            Element metadatas = new Element("metadatas");
            for (String metadonnee : parameters) {
                if (!rm.containerRecord.schema().hasMetadataWithCode(metadonnee))
                    throw new MetadataException("No such metadata " + metadonnee);
                Element m = new Element(metadonnee);
                m.setText(con.get(metadonnee) + "");
                metadatas.addContent(m);
            }
            container.setContent(metadatas);
            root.addContent(container);
        }
        XMLOutputter xmlOutputter = new XMLOutputter(DEV ? Format.getPrettyFormat() : Format.getCompactFormat());
        return xmlOutputter.outputString(document);
    }

    public void createPDFFromXmlAndJasperFile(String xml, File jasperFile) throws Exception {

        long start = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
//        params.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, "yyyy-MM-dd");
//        params.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, "#,##0.##");
//        params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.ENGLISH);
//        params.put(JRParameter.REPORT_LOCALE, Locale.US);

        String reportFile = JasperFillManager.fillReportToFile(jasperFile.getAbsolutePath(), params);
        JasperExportManager.exportReportToPdfFile(reportFile, "C:\\Users\\Marco\\Desktop\\test.pdf");
        System.err.println("Filling time : " + (System.currentTimeMillis() - start));
//        JasperPrint jp = JasperFillManager.fillReport(new FileInputStream(jasperFile), new HashMap<String, Object>(), new JRXmlDataSource());
//        JasperExportManager.exportReportToPdfFile(jp, );
    }


    /**
     * Méthode qui prend en paramètre un String et qui resort le même string seulement il est formater pour être valide dans une balise XML
     *
     * @param input String
     * @return
     */
    public static String escapeForXmlTag(String input) {
        return input.replace(" ", "_").replaceAll("[éèëê]", "e").replaceAll("[àâáä]", "a").replaceAll("[öòóô]", "o").replace("'", "").replaceAll("-", "_").replaceAll("[üùúû]", "u").replaceAll("[îìíï]", "i").toLowerCase();
    }
}
