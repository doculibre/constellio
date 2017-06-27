package com.constellio.app.modules.rm.services.reports;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDate;

import com.constellio.app.api.extensions.params.AddFieldsInLabelXMLParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;

public class ReportXMLGenerator {
	public static final boolean DEV = true;

	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private int startingPosition, numberOfCopies;
	private ContentManager contentManager;
	private AppLayerFactory factory;
	private String collection;
	private String username;
	private UserServices userServices;
	private List<DataField> otherDataForContainer;

	public ReportXMLGenerator(String collection, AppLayerFactory appLayerFactory, String username) {
		this.factory = appLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, factory);
		this.searchServices = factory.getModelLayerFactory().newSearchServices();
		this.recordServices = factory.getModelLayerFactory().newRecordServices();
		this.startingPosition = 0;
		this.contentManager = factory.getModelLayerFactory().getContentManager();
		this.numberOfCopies = 1;
		this.username = username;
		this.userServices = factory.getModelLayerFactory().newUserServices();
		this.otherDataForContainer = getotherDataForContainer();

		JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		jasperReportsContext.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jasperReportsContext.setProperty("net.sf.jasperreports.default.font.name", "Arial");
	}

	private List<DataField> getotherDataForContainer() {
		List<DataField> data = new ArrayList<>();
		data.addAll(asList(
				new DataField("dispositionDate", DecommissioningService.class)
						.setMethod("getDispositionDate", ContainerRecord.class)
						.setInstance(new DecommissioningService(this.collection, this.factory)),
				new DataField("decommissioningLabel", DecommissioningService.class)
						.setMethod("getDecommissionningLabel", ContainerRecord.class)
						.setInstance(new DecommissioningService(this.collection, this.factory))
		));
		return data;
	}

	/**
	 * Cette méthode prend une liste de métadonnées en paramètre.
	 * On cherche tous les dossiers d'une collection et crée un XML à partir
	 * des métadonnées de ces fichiers.
	 *
	 * @param reportFields
	 * @return String - Le document XML
	 * @throws Exception
	 */
	public String convertFolderToXML(ReportField... reportFields)
			throws Exception {
		LogicalSearchCondition condition = from(rm.folder.schemaType()).where(ALL);
		List<Folder> foldersFound = rm.wrapFolders(searchServices.search(new LogicalSearchQuery(condition)));
		Document document = new Document();
		Element root = new Element("folders");
		document.setRootElement(root);
		for (int i = 0; i < this.getStartingPosition(); i++) {
			Element container = new Element("folder");
			Element metadatas = new Element("metadatas");
			container.setContent(metadatas);
			root.addContent(container);
		}
		if (reportFields == null) {
			List<ReportField> temp = new ArrayList<>();
			for (Metadata m : rm.folder.schema().getMetadatas()) {
				String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ?
						m.getReferencedSchemaType() :
						Folder.SCHEMA_TYPE;
				temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
			}
			reportFields = temp.toArray(new ReportField[0]);
		}
		for (int i = 0; i < this.numberOfCopies; i++) {
			for (Folder fol : foldersFound) {
				Element folder = new Element("folder");
				Element metadatas = new Element("metadatas");



				for (ReportField reportField : reportFields) {
					MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager()
							.getSchemaTypes(this.collection).getSchemaType(reportField.getSchema());
					if (reportField.getTypes().equals(MetadataValueType.REFERENCE)) {
						List<String> IdsList = fol.getSchema().get(reportField.getCode()).isMultivalue() ?
								asList(fol.getList(fol.getSchema().getMetadata(reportField.getCode())).toArray(new String[0])) :
								asList((String) fol.get(fol.getSchema().getMetadata(reportField.getCode())));
						List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
						for (Record refRecords : referenceRecords) {
							Element refElementCode = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_code");
							refElementCode.setText(refRecords.get(Schemas.CODE) + "");
							Element refElementTitle = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_title");
							refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else if (reportField.getTypes().equals(MetadataValueType.ENUM)) {
						if (fol.get(reportField.getCode()) != null) {
							Element refElementCode = new Element(escapeForXmlTag(reportField.getLabel()) + "_code");
							refElementCode.setText(((EnumWithSmallCode) fol.get(reportField.getCode())).getCode());
							Element refElementTitle = new Element(escapeForXmlTag(reportField.getLabel()) + "_title");
							refElementTitle.setText(fol.get(reportField.getCode()) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else {
						Element m = new Element(escapeForXmlTag(reportField.getLabel()));
						m.setText(reportField
								.formatData(fol.get(reportField.getCode()) != null ? fol.get(reportField.getCode()) + "" : null));
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
	 * On cherche le dossier correspondant au id dans la collection et crée un XML à partir
	 * des métadonnées de ce dossier.
	 *
	 * @param parameters
	 * @return String - Le document XML
	 * @throws Exception
	 */
	public String convertFolderWithIdentifierToXML(String id, ReportField... parameters)
			throws Exception {
		if (id == null)
			throw new NullArgumentException("The id is null !");
		return convertFolderWithIdentifierToXML(asList(id), parameters);
	}

	/**
	 * Cette méthode prend une liste de métadonnées en paramètre.
	 * On cherche les dossiers correspondant aux ids dans la collection et crée un XML à partir
	 * des métadonnées de ces dossiers.
	 *
	 * @param reportFields
	 * @return String - Le document XML
	 * @throws Exception
	 */
	public String convertFolderWithIdentifierToXML(List<String> ids, ReportField... reportFields)
			throws Exception {
		if (ids == null)
			throw new NullArgumentException("The ids list is null !");
		LogicalSearchCondition condition = from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(ids);
		List<Folder> foldersFound = rm.wrapFolders(searchServices.search(new LogicalSearchQuery(condition)));
		Document document = new Document();
		Element root = new Element("folders");
		document.setRootElement(root);
		for (int i = 0; i < this.getStartingPosition(); i++) {
			Element container = new Element("folder");
			Element metadatas = new Element("metadatas");
			container.setContent(metadatas);
			root.addContent(container);
		}
		if (reportFields == null) {
			List<ReportField> temp = new ArrayList<>();
			for (Metadata m : rm.folder.schema().getMetadatas()) {
				String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ?
						m.getReferencedSchemaType() :
						Folder.SCHEMA_TYPE;
				temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
			}
			reportFields = temp.toArray(new ReportField[0]);
		}
		for (int i = 0; i < this.numberOfCopies; i++) {
			for (Folder fol : foldersFound) {
				Element folder = new Element("folder");
				Element metadatas = new Element("metadatas");

				factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
						fol.getWrappedRecord(), folder, metadatas));

				for (ReportField reportField : reportFields) {
					MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager()
							.getSchemaTypes(this.collection).getSchemaType(reportField.getSchema());
					if (reportField.getTypes().equals(MetadataValueType.REFERENCE)) {
						List<String> IdsList = fol.getSchema().get(reportField.getCode()).isMultivalue() ?
								asList(fol.getList(fol.getSchema().getMetadata(reportField.getCode())).toArray(new String[0])) :
								asList((String) fol.get(fol.getSchema().getMetadata(reportField.getCode())));
						List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
						for (Record refRecords : referenceRecords) {
							Element refElementCode = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_code");
							refElementCode.setText(refRecords.get(Schemas.CODE) + "");
							Element refElementTitle = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_title");
							refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else if (reportField.getTypes().equals(MetadataValueType.ENUM)) {
						if (fol.get(reportField.getCode()) != null) {
							Element refElementCode = new Element(escapeForXmlTag(reportField.getLabel()) + "_code");
							refElementCode.setText(((EnumWithSmallCode) fol.get(reportField.getCode())).getCode());
							Element refElementTitle = new Element(escapeForXmlTag(reportField.getLabel()) + "_title");
							refElementTitle.setText(fol.get(reportField.getCode()) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else {
						Element m = new Element(reportField.getLabel() != null ?
								escapeForXmlTag(reportField.getLabel()) :
								reportField.getCode().split("_")[2]);
						m.setText(reportField
								.formatData(fol.get(reportField.getCode()) != null ? fol.get(reportField.getCode()) + "" : null));
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
	 * @param reportFields
	 * @return String - Le document XML
	 * @throws Exception
	 */
	public String convertContainerToXML(ReportField... reportFields)
			throws Exception {
		LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(ALL);
		List<ContainerRecord> containersFound = rm.wrapContainerRecords(searchServices.search(new LogicalSearchQuery(condition)
				.filteredWithUser(this.userServices.getUserInCollection(this.username, this.collection), Role.READ)));
		Document document = new Document();
		Element root = new Element("containers");
		document.setRootElement(root);
		for (int i = 0; i < this.getStartingPosition(); i++) {
			Element container = new Element("container");
			Element metadatas = new Element("metadatas");
			container.setContent(metadatas);
			root.addContent(container);
		}
		if (reportFields == null) {
			List<ReportField> temp = new ArrayList<>();
			for (Metadata m : rm.containerRecord.schema().getMetadatas()) {
				String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ?
						m.getReferencedSchemaType() :
						ContainerRecord.SCHEMA_TYPE;
				temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
			}
			reportFields = temp.toArray(new ReportField[0]);
		}

		for (int i = 0; i < this.numberOfCopies; i++) {
			for (ContainerRecord con : containersFound) {
				Element container = new Element("container");
				Element metadatas = new Element("metadatas");

				factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
						con.getWrappedRecord(), container, metadatas));

				for (ReportField reportField : reportFields) {
					MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager()
							.getSchemaTypes(this.collection).getSchemaType(reportField.getSchema());
					if (reportField.getTypes().equals(MetadataValueType.REFERENCE)) {
						List<String> IdsList = con.getSchema().get(reportField.getCode()).isMultivalue() ?
								asList(con.getList(con.getSchema().getMetadata(reportField.getCode())).toArray(new String[0])) :
								asList((String) con.get(con.getSchema().getMetadata(reportField.getCode())));
						List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
						for (Record refRecords : referenceRecords) {
							Element refElementCode = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_code");
							refElementCode.setText(refRecords.get(Schemas.CODE) + "");
							Element refElementTitle = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_title");
							refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else if (reportField.getTypes().equals(MetadataValueType.ENUM)) {
						if (con.get(reportField.getCode()) != null) {
							Element refElementCode = new Element(escapeForXmlTag(reportField.getLabel()) + "_code");
							refElementCode.setText(((EnumWithSmallCode) con.get(reportField.getCode())).getCode());
							Element refElementTitle = new Element(escapeForXmlTag(reportField.getLabel()) + "_title");
							refElementTitle.setText(con.get(reportField.getCode()) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else {
						Element m = new Element(escapeForXmlTag(reportField.getLabel()));
						m.setText(reportField
								.formatData(con.get(reportField.getCode()) != null ? con.get(reportField.getCode()) + "" : null));
						metadatas.addContent(m);
					}
				}
				for (DataField dataField : this.otherDataForContainer) {
					Element e = new Element(dataField.getKey());
					String value = dataField.calculate(new Object[] { con }) + "";
					e.setText(value);
					metadatas.addContent(e);
				}
				container.setContent(metadatas);
				root.addContent(container);
			}
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
	public String convertContainerWithIdentifierToXML(String id, ReportField... parameters)
			throws Exception {
		if (id == null)
			throw new NullArgumentException("The id is null !");
		return convertContainerWithIdentifierToXML(asList(id), parameters);
	}

	/**
	 * Cette méthode prend une liste de métadonnées en paramètre.
	 * On cherche les conteneurs correspondants aux ids dans la collection et crée un XML à partir
	 * des métadonnées de ces contenants.
	 *
	 * @param reportFields
	 * @return String - Le document XML
	 * @throws Exception
	 */
	public String convertContainerWithIdentifierToXML(List<String> ids, ReportField... reportFields)
			throws Exception {
		reportFields = null;
		if (ids == null)
			throw new NullArgumentException($("listNull"));
		LogicalSearchCondition condition = from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isIn(ids);
		List<ContainerRecord> containersFound = rm.wrapContainerRecords(searchServices.search(new LogicalSearchQuery(condition)
				.filteredWithUser(this.userServices.getUserInCollection(this.username, this.collection), Role.READ)));
		Document document = new Document();
		Element root = new Element("containers");
		document.setRootElement(root);
		for (int i = 0; i < this.getStartingPosition(); i++) {
			Element container = new Element("container");
			Element metadatas = new Element("metadatas");
			container.setContent(metadatas);
			root.addContent(container);
		}
		if (reportFields == null) {
			List<ReportField> temp = new ArrayList<>();
			for (Metadata m : rm.containerRecord.schema().getMetadatas()) {
				String schemaType = m.getType().equals(MetadataValueType.REFERENCE) ?
						m.getReferencedSchemaType() :
						ContainerRecord.SCHEMA_TYPE;
				temp.add(new ReportField(m.getType(), m.getCode().split("_")[2], schemaType, m.getCode(), factory));
			}
			reportFields = temp.toArray(new ReportField[0]);
		}
		for (int i = 0; i < this.numberOfCopies; i++) {
			for (ContainerRecord con : containersFound) {
				Element container = new Element("container");
				Element metadatas = new Element("metadatas");

				factory.getExtensions().forCollection(collection).addFieldsInLabelXML(new AddFieldsInLabelXMLParams(
						con.getWrappedRecord(), container, metadatas));

				Element collectionCodeElement = new Element("collection_code");
				collectionCodeElement.setText(collection);
				metadatas.addContent(collectionCodeElement);

				Element collectionTitleElement = new Element("collection_title");
				collectionTitleElement.setText(factory.getCollectionsManager().getCollection(collection).getName());
				metadatas.addContent(collectionTitleElement);

				Element extremeDatesElement = new Element("extremeDates");
				extremeDatesElement.setText(new DecommissioningService(collection, factory).getContainerRecordExtremeDates(con));
				metadatas.addContent(extremeDatesElement);

				for (ReportField reportField : reportFields) {
					MetadataSchemaType schema = factory.getModelLayerFactory().getMetadataSchemasManager()
							.getSchemaTypes(this.collection).getSchemaType(reportField.getSchema());
					if (reportField.getTypes().equals(MetadataValueType.REFERENCE)) {
						List<String> IdsList = con.getSchema().get(reportField.getCode()).isMultivalue() ?
								asList(con.getList(con.getSchema().getMetadata(reportField.getCode())).toArray(new String[0])) :
								asList((String) con.get(con.getSchema().getMetadata(reportField.getCode())));
						List<Record> referenceRecords = recordServices.getRecordsById(this.collection, IdsList);
						for (Record refRecords : referenceRecords) {
							Element refElementCode = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_code");
							refElementCode.setText(refRecords.get(Schemas.CODE) + "");
							Element refElementTitle = new Element(
									"ref_" + reportField.getCode().replace("_default", "") + "_title");
							refElementTitle.setText(refRecords.get(Schemas.TITLE) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else if (reportField.getTypes().equals(MetadataValueType.ENUM)) {
						if (con.get(reportField.getCode()) != null) {
							Element refElementCode = new Element(escapeForXmlTag(reportField.getLabel()) + "_code");
							refElementCode.setText(((EnumWithSmallCode) con.get(reportField.getCode())).getCode());
							Element refElementTitle = new Element(escapeForXmlTag(reportField.getLabel()) + "_title");
							refElementTitle.setText(con.get(reportField.getCode()) + "");
							metadatas.addContent(asList(refElementCode, refElementTitle));
						}
					} else {
						Element m = new Element(escapeForXmlTag(reportField.getLabel()));
						m.setText(reportField
								.formatData(con.get(reportField.getCode()) != null ? con.get(reportField.getCode()) + "" : null));
						metadatas.addContent(m);
					}
				}
				for (DataField dataField : this.otherDataForContainer) {
					Element e = new Element(dataField.getKey());
					String value = dataField.calculate(new Object[] { con }) + "";
					e.setText(value);
					metadatas.addContent(e);
				}
				container.setContent(metadatas);
				root.addContent(container);
			}
		}
		XMLOutputter xmlOutputter = new XMLOutputter(DEV ? Format.getPrettyFormat() : Format.getCompactFormat());
		return xmlOutputter.outputString(document);
	}

	/**
	 * function that takes a Jasper file and a XML file to create a PDF file.
	 *
	 * @param xml
	 * @param jasperFile
	 * @throws Exception
	 */
	public Content createPDFFromXmlAndJasperFile(String xml, File jasperFile, String format)
			throws Exception {

		Map<String, Object> params = new HashMap<>();
		org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
		String reportFile = JasperFillManager.fillReportToFile(jasperFile.getAbsolutePath(), params);
		String PDFFile = JasperExportManager.exportReportToPdfFile(reportFile);
		File file = new File(PDFFile);
		ContentVersionDataSummary upload = contentManager.upload(new FileInputStream(file), "Etiquette")
				.getContentVersionDataSummary();
		return contentManager.createFileSystem(escapeForXmlTag(format) + "-" + LocalDate.now(), upload);
	}

	/**
	 * Allow to select on which index the label will start printing
	 *
	 * @param start
	 */
	public void setStartingPosition(int start) {
		this.startingPosition = start;
	}

	/**
	 * @return starting position for label printing
	 */
	public int getStartingPosition() {
		return this.startingPosition;
	}

	/**
	 * allow to change the amount of copy of label to print
	 *
	 * @param nb
	 */
	public void setNumberOfCopies(int nb) {
		this.numberOfCopies = nb;
	}

	/**
	 * get the amount of copy of label to print.
	 *
	 * @return
	 */
	public int getNumberOfCopies() {
		return this.numberOfCopies;
	}

	/**
	 * Function that checks a string and replace if needed. Used to get valid XML tag.
	 *
	 * @param input String
	 * @return
	 */
	public static String escapeForXmlTag(String input) {
		return input.replace(" ", "_").replaceAll("[éèëê]", "e").replaceAll("[àâáä]", "a").replaceAll("[öòóô]", "o")
				.replace("'", "").replaceAll("-", "_").replaceAll("[üùúû]", "u").replaceAll("[îìíï]", "i")
				.replaceAll("[\\( \\)]", "").replaceAll("[&$%]", "").toLowerCase();
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

		public Object calculate(Object[] parameters)
				throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
			return this.clazz.getMethod(this.method, this.methodParams).invoke(this.instance, parameters);
		}
	}
}
