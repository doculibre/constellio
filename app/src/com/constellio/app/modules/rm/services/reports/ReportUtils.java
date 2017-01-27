package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.reports.wrapper.ReportConfig;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.ConstellioEIM;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.utils.SimpleDateFormatSingleton;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
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
import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.provider.TemporaryFileStore;
import org.apache.tools.ant.taskdefs.Ant;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

/**
 * Created by Nicolas D'Amours & Charles Blanchette on 2017-01-16.
 */
public class ReportUtils {
    public static final boolean DEV = true;

    private RMSchemasRecordsServices rm;
    private SearchServices ss;
    private RecordServices recordServices;
    private int startingPosition, numberOfCopies;
    private ContentManager contentManager;
    private AppLayerFactory factory;
    private String collection;
    private String usr;
    private UserServices userServices;

    public ReportUtils(String collection, AppLayerFactory appLayerFactory, String usr) {
        this.factory = appLayerFactory;
        this.collection = collection;
        this.rm = new RMSchemasRecordsServices(collection, factory);
        this.ss = factory.getModelLayerFactory().newSearchServices();
        this.recordServices = factory.getModelLayerFactory().newRecordServices();
        this.startingPosition = 0;
        this.contentManager = factory.getModelLayerFactory().getContentManager();
        this.numberOfCopies = 1;
        this.usr = usr;
        this.userServices = factory.getModelLayerFactory().newUserServices();
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
                String code = "";
                if (m.getType().equals(MetadataValueType.DATE)) {
//                    code = m.get
                }
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
                Element m = new Element(metadonnee.split("_")[2]);
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
        for (int i = 0; i < this.numberOfCopies; i++) {
            for (Folder fol : foldersFound) {
                Element folder = new Element("folder");
                Element metadatas = new Element("metadatas");
                for (String metadonnee : parameters) {
                    if (!rm.folder.schema().hasMetadataWithCode(metadonnee))
                        throw new MetadataException("No such metadata " + metadonnee);
                    Element m = new Element(metadonnee.split("_")[2]);
                    m.setText(fol.get(metadonnee) + "");
                    metadatas.addContent(m);
                }
                folder.setContent(metadatas);
                root.addContent(folder);
            }
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
                Element m = new Element(metadonnee.split("_")[2]);
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
    public String convertContainerWithIdentifierToXML(String id, ReportField... parameters) throws Exception {
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
    public String convertContainerWithIdentifierToXML(List<String> ids, ReportField... parameters) throws Exception {
        if (ids == null) throw new NullArgumentException($("listNull"));
        LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(ids);
        List<ContainerRecord> containersFound = rm.wrapContainerRecords(ss.search(new LogicalSearchQuery(condition).filteredWithUser(this.userServices.getUserInCollection(this.usr, this.collection), Role.READ)));
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
            List<ReportField> temp = new ArrayList<>();
            for (Metadata m : rm.containerRecord.schema().getMetadatas()) {
                String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ? m.getReferencedSchemaType() : ContainerRecord.SCHEMA_TYPE;
                temp.add(new ReportField(m.getType(), m.getLabel(i18n.getLanguage()), schemaType, m.getCode()));
            }
            parameters = temp.toArray(new ReportField[0]);
        }
        for (ContainerRecord con : containersFound) {
            Element container = new Element("container");
            Element metadatas = new Element("metadatas");
            for (ReportField metadonnee : parameters) {
                MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(this.collection).getSchemaType(metadonnee.getSchema());
//                if (!schema.getSchema(schema.getDefaultSchema().getCode()).hasMetadataWithCode(schema.getDefaultSchema().getCode() + "_" +  metadonnee.getCode()))
//                    throw new MetadataException("No such metadata " + metadonnee.getCode() + " for schema " + metadonnee.getSchema());
                if (metadonnee.getTypes().equals(MetadataValueType.REFERENCE)) {
//                    recordServices.getDocumentById()
                    List<String> IdsList = con.getSchema().get(metadonnee.getCode()).isMultivalue() ? Arrays.asList(con.getList(con.getSchema().getMetadata(metadonnee.getCode())).toArray(new String[0])) : Arrays.asList((String) con.get(con.getSchema().getMetadata(metadonnee.getCode())));
                    List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
                    for (Record refRecords : referenceRecords) {
                        Element refElementCode = new Element("ref_" + metadonnee.getCode() + "_" + refRecords.getSchemaCode() + "_code");
                        refElementCode.setText(refRecords.get(Schemas.CODE) + "");
                        Element refElementTitle = new Element("ref_" + metadonnee.getCode() + "_" + refRecords.getSchemaCode() + "_title");
                        refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
                        metadatas.addContent(Arrays.asList(refElementCode, refElementTitle));
                    }
                } else {
                    Element m = new Element(escapeForXmlTag(metadonnee.getLabel()));
                    m.setText(metadonnee.formatData(con.get(metadonnee.getCode()) != null ? con.get(metadonnee.getCode()) + "" : null));
                    metadatas.addContent(m);
                }
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
     * Permet de définir le nombre de copies de l'étiquette à
     * imprimer
     *
     * @param nb
     */
    public void setNumberOfCopies(int nb) {
        this.numberOfCopies = nb;
    }

    /**
     * le nombre de copies choisi.
     *
     * @return
     */
    public int getNumberOfCopies() {
        return this.numberOfCopies;
    }


    /**
     * Méthode qui prend en paramètre un String et qui resort le même string seulement il est formater pour être valide dans une balise XML
     *
     * @param input String
     * @return
     */
    public static String escapeForXmlTag(String input) {
        return input.replace(" ", "_").replaceAll("[éèëê]", "e").replaceAll("[àâáä]", "a").replaceAll("[öòóô]", "o").replace("'", "").replaceAll("-", "_").replaceAll("[üùúû]", "u").replaceAll("[îìíï]", "i").replaceAll("[\\( \\)]", "").replaceAll("[&$%]", "").toLowerCase();
    }

    public class ReportField {

        private MetadataValueType types;
        private String label, schema, code;

        public ReportField(MetadataValueType type, String label, String schema, String code) {
            this.types = type;
            this.label = label;
            this.schema = schema;
            this.code = code;
        }

        public MetadataValueType getTypes() {
            return this.types;
        }

        public String getSchema() {
            return this.schema;
        }

        public String getLabel() {
            return this.label;
        }

        public String getCode() {
            return this.code;
        }

        public String formatData(String value) throws Exception {
            String formattedData = value;
            if (value != null) {
                ConstellioEIMConfigs configs = new ConstellioEIMConfigs(factory.getModelLayerFactory().getSystemConfigurationsManager());
                if (this.types.equals(MetadataValueType.BOOLEAN)) {
                    formattedData = $(value);
                } else if (this.types.equals(MetadataValueType.DATE)) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = df.parse(value);
                    formattedData = SimpleDateFormatSingleton.getSimpleDateFormat(configs.getDateFormat()).format(date);
                } else if (this.types.equals(MetadataValueType.DATE_TIME)) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    Date date = df.parse(value);
                    formattedData = SimpleDateFormatSingleton.getSimpleDateFormat(configs.getDateTimeFormat()).format(date);
                }
                formattedData = formattedData.replaceAll("[\\[\\]]", "");
            }
            return formattedData;
        }

    }
}
