package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.lang.NullArgumentException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.joda.time.LocalDate;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

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
    private List<DataField> otherDataForContainer;

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
        this.otherDataForContainer = getotherDataForContainer();
    }

    private List<DataField> getotherDataForContainer() {
        List<DataField> data = new ArrayList<>();
        data.add(new DataField("dispositionDate", DecommissioningService.class).setMethod("getDispositionDate", ContainerRecord.class).setInstance(new DecommissioningService(this.collection, this.factory)));
        return data;
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
    public String convertFolderToXML(ReportField... parameters) throws Exception {
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
            List<ReportField> temp = new ArrayList<>();
            for (Metadata m : rm.folder.schema().getMetadatas()) {
                String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ? m.getReferencedSchemaType() : Folder.SCHEMA_TYPE;
                temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
            }
            parameters = temp.toArray(new ReportField[0]);
        }
        for (Folder fol : foldersFound) {
            Element folder = new Element("folder");
            Element metadatas = new Element("metadatas");
            for (ReportField metadonnee : parameters) {
                MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(this.collection).getSchemaType(metadonnee.getSchema());
//                if (!schema.getSchema(schema.getDefaultSchema().getCode()).hasMetadataWithCode(schema.getDefaultSchema().getCode() + "_" +  metadonnee.getCode()))
//                    throw new MetadataException("No such metadata " + metadonnee.getCode() + " for schema " + metadonnee.getSchema());
                if (metadonnee.getTypes().equals(MetadataValueType.REFERENCE)) {
//                    recordServices.getDocumentById()
                    List<String> IdsList = fol.getSchema().get(metadonnee.getCode()).isMultivalue() ? asList(fol.getList(fol.getSchema().getMetadata(metadonnee.getCode())).toArray(new String[0])) : asList((String) fol.get(fol.getSchema().getMetadata(metadonnee.getCode())));
                    List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
                    for (Record refRecords : referenceRecords) {
                        Element refElementCode = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_code");
                        refElementCode.setText(refRecords.get(Schemas.CODE) + "");
                        Element refElementTitle = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_title");
                        refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else if (metadonnee.getTypes().equals(MetadataValueType.ENUM)) {
                    if (fol.get(metadonnee.getCode()) != null) {
                        Element refElementCode = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_code");
                        refElementCode.setText(((EnumWithSmallCode) fol.get(metadonnee.getCode())).getCode());
                        Element refElementTitle = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_title");
                        refElementTitle.setText(fol.get(metadonnee.getCode()) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else {
                    Element m = new Element(escapeForXmlTag(metadonnee.getLabel()));
                    m.setText(metadonnee.formatData(fol.get(metadonnee.getCode()) != null ? fol.get(metadonnee.getCode()) + "" : null));
                    metadatas.addContent(m);
                }
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
    public String convertFolderWithIdentifierToXML(String id, ReportField... parameters) throws Exception {
        if (id == null) throw new NullArgumentException("The id is null !");
        return convertFolderWithIdentifierToXML(asList(id), parameters);
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
    public String convertFolderWithIdentifierToXML(List<String> ids, ReportField... parameters) throws Exception {
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
            List<ReportField> temp = new ArrayList<>();
            for (Metadata m : rm.folder.schema().getMetadatas()) {
                String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ? m.getReferencedSchemaType() : Folder.SCHEMA_TYPE;
                temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
            }
            parameters = temp.toArray(new ReportField[0]);
        }
        for (int i = 0; i < this.numberOfCopies; i++) {
            for (Folder fol : foldersFound) {
                Element folder = new Element("folder");
                Element metadatas = new Element("metadatas");
                for (ReportField metadonnee : parameters) {
                    MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(this.collection).getSchemaType(metadonnee.getSchema());
//                if (!schema.getSchema(schema.getDefaultSchema().getCode()).hasMetadataWithCode(schema.getDefaultSchema().getCode() + "_" +  metadonnee.getCode()))
//                    throw new MetadataException("No such metadata " + metadonnee.getCode() + " for schema " + metadonnee.getSchema());
                    if (metadonnee.getTypes().equals(MetadataValueType.REFERENCE)) {
//                    recordServices.getDocumentById()
                        List<String> IdsList = fol.getSchema().get(metadonnee.getCode()).isMultivalue() ? asList(fol.getList(fol.getSchema().getMetadata(metadonnee.getCode())).toArray(new String[0])) : asList((String) fol.get(fol.getSchema().getMetadata(metadonnee.getCode())));
                        List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
                        for (Record refRecords : referenceRecords) {
                            Element refElementCode = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_code");
                            refElementCode.setText(refRecords.get(Schemas.CODE) + "");
                            Element refElementTitle = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_title");
                            refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
                            metadatas.addContent(asList(refElementCode, refElementTitle));
                        }
                    } else if (metadonnee.getTypes().equals(MetadataValueType.ENUM)) {
                        if (fol.get(metadonnee.getCode()) != null) {
                            Element refElementCode = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_code");
                            refElementCode.setText(((EnumWithSmallCode) fol.get(metadonnee.getCode())).getCode());
                            Element refElementTitle = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_title");
                            refElementTitle.setText(fol.get(metadonnee.getCode()) + "");
                            metadatas.addContent(asList(refElementCode, refElementTitle));
                        }
                    } else {
                        Element m = new Element(metadonnee.getLabel() != null ? escapeForXmlTag(metadonnee.getLabel()) : metadonnee.getCode().split("_")[2]);
                        m.setText(metadonnee.formatData(fol.get(metadonnee.getCode()) != null ? fol.get(metadonnee.getCode()) + "" : null));
                        metadatas.addContent(m);
                    }
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
    public String convertContainerToXML(ReportField... parameters) throws Exception {
        LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(ALL);
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
                temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
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
                    List<String> IdsList = con.getSchema().get(metadonnee.getCode()).isMultivalue() ? asList(con.getList(con.getSchema().getMetadata(metadonnee.getCode())).toArray(new String[0])) : asList((String) con.get(con.getSchema().getMetadata(metadonnee.getCode())));
                    List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
                    for (Record refRecords : referenceRecords) {
                        Element refElementCode = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_code");
                        refElementCode.setText(refRecords.get(Schemas.CODE) + "");
                        Element refElementTitle = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_title");
                        refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else if (metadonnee.getTypes().equals(MetadataValueType.ENUM)) {
                    if (con.get(metadonnee.getCode()) != null) {
                        Element refElementCode = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_code");
                        refElementCode.setText(((EnumWithSmallCode) con.get(metadonnee.getCode())).getCode());
                        Element refElementTitle = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_title");
                        refElementTitle.setText(con.get(metadonnee.getCode()) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else {
                    Element m = new Element(escapeForXmlTag(metadonnee.getLabel()));
                    m.setText(metadonnee.formatData(con.get(metadonnee.getCode()) != null ? con.get(metadonnee.getCode()) + "" : null));
                    metadatas.addContent(m);
                }
            }
            for (DataField dataField : this.otherDataForContainer) {
                Element e = new Element(dataField.getKey());
                String value = dataField.calculate(new Object[]{con}) + "";
                System.out.println(value + " " + new DecommissioningService(this.collection, this.factory).getDispositionDate(con));
                e.setText(value);
                metadatas.addContent(e);
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
        return convertContainerWithIdentifierToXML(asList(id), parameters);
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
                temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
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
                    List<String> IdsList = con.getSchema().get(metadonnee.getCode()).isMultivalue() ? asList(con.getList(con.getSchema().getMetadata(metadonnee.getCode())).toArray(new String[0])) : asList((String) con.get(con.getSchema().getMetadata(metadonnee.getCode())));
                    List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
                    for (Record refRecords : referenceRecords) {
                        Element refElementCode = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_code");
                        refElementCode.setText(refRecords.get(Schemas.CODE) + "");
                        Element refElementTitle = new Element("ref_" + metadonnee.getCode().replace("_default", "") + "_title");
                        refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else if (metadonnee.getTypes().equals(MetadataValueType.ENUM)) {
                    if (con.get(metadonnee.getCode()) != null) {
                        Element refElementCode = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_code");
                        refElementCode.setText(((EnumWithSmallCode) con.get(metadonnee.getCode())).getCode());
                        Element refElementTitle = new Element(escapeForXmlTag(metadonnee.getLabel()) + "_title");
                        refElementTitle.setText(con.get(metadonnee.getCode()) + "");
                        metadatas.addContent(asList(refElementCode, refElementTitle));
                    }
                } else {
                    Element m = new Element(escapeForXmlTag(metadonnee.getLabel()));
                    m.setText(metadonnee.formatData(con.get(metadonnee.getCode()) != null ? con.get(metadonnee.getCode()) + "" : null));
                    metadatas.addContent(m);
                }
            }
            for (DataField dataField : this.otherDataForContainer) {
                Element e = new Element(dataField.getKey());
                String value = dataField.calculate(new Object[]{con}) + "";
                System.out.println(value + " " + new DecommissioningService(this.collection, this.factory).getDispositionDate(con));
                e.setText(value);
                metadatas.addContent(e);
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

    private class DataField {
        private String key;
        private Class clazz;
        private String method;
        private Object instance;
        private Class<?>[] methodParams;

        public DataField(String key, Class clazz) {
            this.key = key;
            this.clazz = clazz;
        }

        public DataField setMethod(String method, Class<?>... params) {
            this.method = method;
            this.methodParams = params;
            return this;
        }

        public DataField setInstance(Object instance) {
            this.instance = instance;
            return this;
        }

        public String getKey() {
            return this.key;
        }

        public Class getClazz() {
            return this.clazz;
        }

        public String getMethod() {
            return this.method;
        }

        public Object calculate(Object[] parameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
            return this.clazz.getMethod(this.method, this.methodParams).invoke(this.instance, parameters);
        }
    }
}
