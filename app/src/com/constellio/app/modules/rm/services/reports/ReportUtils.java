package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.drew.metadata.MetadataException;
import com.itextpdf.text.DocumentException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import net.sf.jasperreports.export.SimpleReportExportConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.joda.time.LocalDate;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Nicolas D'Amours & Charles Blanchette on 2017-01-16.
 */
public class ReportUtils {
    public static final boolean DEV = true;

    private RMSchemasRecordsServices rm;
    private SearchServices ss;
    private RecordServices recordServices;
    private int startingPosition;
    private ContentManager contentManager;

    public ReportUtils(String collection, AppLayerFactory appLayerFactory) {
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        ss = appLayerFactory.getModelLayerFactory().newSearchServices();
        recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        startingPosition = 0;
        contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
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
        for (int i = 0; i < this.getStartingPosition(); i++) {
            Element container = new Element("folder");
            Element metadatas = new Element("metadatas");
            container.setContent(metadatas);
            root.addContent(container);
        }
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
        for (int i = 0; i < this.getStartingPosition(); i++) {
            Element container = new Element("folder");
            Element metadatas = new Element("metadatas");
            container.setContent(metadatas);
            root.addContent(container);
        }
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
        for (int i = 0; i < this.getStartingPosition(); i++) {
            Element container = new Element("container");
            Element metadatas = new Element("metadatas");
            container.setContent(metadatas);
            root.addContent(container);
        }
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
        for (int i = 0; i < this.getStartingPosition(); i++) {
            Element container = new Element("container");
            Element metadatas = new Element("metadatas");
            container.setContent(metadatas);
            root.addContent(container);
        }
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
     * Méthode qui prend en paramètre une chaine de caractère sous format XML et un ficher .jasper.
     * Il créer ensuite un PDF avec les deux.
     *
     * @param xml
     * @param jasperFile
     * @throws Exception
     */
    public Content createPDFFromXmlAndJasperFile(String xml, File jasperFile, String format) throws Exception {

        long startTime = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
//        params.put(JRXPathQueryExecuterFactory.P)
        String reportFile = JasperFillManager.fillReportToFile(jasperFile.getAbsolutePath(), params);
        String PDFFile = JasperExportManager.exportReportToPdfFile(reportFile);
        System.err.println("Filling time : " + (System.currentTimeMillis() - startTime));
        System.out.println(PDFFile);
//        JasperPrint jp = JasperFillManager.fillReport(new FileInputStream(jasperFile), new HashMap<String, Object>(), new JRXmlDataSource());
//        JasperExportManager.exportReportToPdfFile(jp, );
        File file = new File(PDFFile);
        ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file), "Etiquette");
        return contentManager.createFileSystem(escapeForXmlTag(format) + "-" + LocalDate.now(), upload);
    }

    /**
     * Permet de définir à quelle position les étiquettes commencent
     * a imprimer.
     *
     * @param start
     */
    public void setStartingPosition(int start) {
        this.startingPosition = start;
    }

    /**
     * @return la position de départ pour l'impression.
     */
    public int getStartingPosition() {
        return this.startingPosition;
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
