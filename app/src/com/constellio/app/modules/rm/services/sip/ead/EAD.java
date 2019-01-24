package com.constellio.app.modules.rm.services.sip.ead;

import com.constellio.app.api.extensions.params.ConvertStructureToMapParams;
import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.modules.rm.services.sip.xsd.XMLDocumentValidator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static java.util.Arrays.asList;

public class EAD {

	private SIPObject sipObject;

	private Namespace eadNamespace = Namespace.getNamespace("ead", "urn:isbn:1-931666-22-9");
	private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	private Document doc;

	private Element eadElement;

	private EADArchdesc archdesc;

	private static SAXBuilder builder;

	private AppLayerFactory factory;

	private String collection;

	private static XMLDocumentValidator validator = new XMLDocumentValidator();

	private Locale locale;

	private static List<String> XSDs = asList("xlink.xsd", "ead.xsd");

	private static final Logger LOGGER = LoggerFactory.getLogger(EAD.class);

	static {
		builder = new SAXBuilder();
	}

	public EAD(SIPObject sipObject, EADArchdesc archdesc, AppLayerFactory factory, String collection, Locale locale) {
		this.sipObject = sipObject;
		this.archdesc = archdesc;
		this.factory = factory;
		this.collection = collection;
		this.locale = locale;

		this.eadElement = new Element("ead", eadNamespace);
		this.eadElement.addNamespaceDeclaration(xsiNamespace);
		eadElement.setAttribute("schemaLocation", "urn:isbn:1-931666-22-9 http://www.loc.gov/ead/ead.xsd", xsiNamespace);

		this.doc = new Document(eadElement);
		addHeader();
		addArchdesc();
	}

	@SuppressWarnings("unchecked")
	public <T extends SIPObject> T getSIPObject() {
		return (T) sipObject;
	}

	private void addHeader() {
		Element eadheaderElement = new Element("eadheader", eadNamespace);

		Element eadidElement = new Element("eadid", eadNamespace);
		eadidElement.setAttribute("identifier", "Identifiant externe");
		eadidElement.setText(sipObject.getId());

		Element filedescElement = new Element("filedesc", eadNamespace);
		Element titlestmtElement = new Element("titlestmt", eadNamespace);
		Element titleproperElement = new Element("titleproper", eadNamespace);
		titleproperElement.setText(sipObject.getTitle());

		eadElement.addContent(eadheaderElement);
		eadheaderElement.addContent(eadidElement);
		eadheaderElement.addContent(filedescElement);
		filedescElement.addContent(titlestmtElement);
		titlestmtElement.addContent(titleproperElement);
	}

	private void addArchdesc() {
		Element archdescElement = new Element("archdesc", eadNamespace);
		archdescElement.setAttribute("level", "class");

		Element didElement = new Element("did", eadNamespace);

		Element unitidElement = new Element("unitid", eadNamespace);
		didElement.addContent(unitidElement);
		unitidElement.setAttribute("type", "Identifiant externe");
		unitidElement.setText(sipObject.getId());

		Element unittitleElement = new Element("unittitle", eadNamespace);
		didElement.addContent(unittitleElement);
		unittitleElement.setText(sipObject.getTitle());

		Map<String, String> didUnitDates = archdesc.getDidUnitDates();
		for (Entry<String, String> entry : didUnitDates.entrySet()) {
			String datechar = entry.getKey();
			String unitdateValue = entry.getValue();

			Element unitdateElement = new Element("unitdate", eadNamespace);
			didElement.addContent(unitdateElement);
			unitdateElement.setAttribute("datechar", datechar);
			unitdateElement.setText(unitdateValue);
		}

		List<String> didLangmaterials = archdesc.getDidLangmaterials();
		for (String didLangmaterial : didLangmaterials) {
			Element langmaterialElement = new Element("langmaterial", eadNamespace);
			didElement.addContent(langmaterialElement);
			langmaterialElement.setText(didLangmaterial);
		}

		List<String> didAbstracts = archdesc.getDidAbstracts();
		for (String didAbstract : didAbstracts) {
			Element didAbstractElement = new Element("abstract", eadNamespace);
			didElement.addContent(didAbstractElement);
			didAbstractElement.setText(didAbstract);
		}

		String didOriginationCorpname = archdesc.getDidOriginationCorpname();
		if (StringUtils.isNotBlank(didOriginationCorpname)) {
			Element originationElement = new Element("origination", eadNamespace);
			didElement.addContent(originationElement);
			Element corpnameElement = new Element("corpname", eadNamespace);
			originationElement.addContent(corpnameElement);
			corpnameElement.setText(didOriginationCorpname);
		}

		List<String> didNotePs = archdesc.getDidNotePs();
		for (String didNoteP : didNotePs) {
			Element didNoteElement = new Element("note", eadNamespace);
			didElement.addContent(didNoteElement);

			Element didNotePElement = new Element("p", eadNamespace);
			didNoteElement.addContent(didNotePElement);
			didNotePElement.setText(didNoteP);
		}

		archdescElement.addContent(didElement);

		String accessrestrictLegalstatus = archdesc.getAccessRestrictLegalStatus();
		if (StringUtils.isNotBlank(accessrestrictLegalstatus)) {
			Element accessrestrictElement = new Element("accessrestrict", eadNamespace);
			archdescElement.addContent(accessrestrictElement);
			Element legalstatusElement = new Element("legalstatus", eadNamespace);
			accessrestrictElement.addContent(legalstatusElement);
			legalstatusElement.setText(accessrestrictLegalstatus);
		}

		List<String> controlaccessSubjects = archdesc.getControlAccessSubjects();
		if (!controlaccessSubjects.isEmpty()) {
			Element controlaccessElement = new Element("controlaccess", eadNamespace);
			archdescElement.addContent(controlaccessElement);
			for (String controlaccessSubject : controlaccessSubjects) {
				Element subjectElement = new Element("subject", eadNamespace);
				controlaccessElement.addContent(subjectElement);
				subjectElement.setText(controlaccessSubject);
			}
		}

		List<List<String>> relatedmaterialLists = archdesc.getRelatedmaterialLists();
		for (List<String> relatedmaterialList : relatedmaterialLists) {
			Element relatedmaterialElement = new Element("relatedmaterial", eadNamespace);
			archdescElement.addContent(relatedmaterialElement);

			Element listElement = new Element("list", eadNamespace);
			relatedmaterialElement.addContent(listElement);
			for (String relatedmaterial : relatedmaterialList) {
				Element itemElement = new Element("item", eadNamespace);
				listElement.addContent(itemElement);
				itemElement.setText(relatedmaterial);
			}
		}

		eadElement.addContent(archdescElement);

		List<String> fileplanPs = archdesc.getFileplanPs();
		if (!fileplanPs.isEmpty()) {
			Element fileplanElement = new Element("fileplan", eadNamespace);
			archdescElement.addContent(fileplanElement);
			for (String fileplanP : fileplanPs) {
				Element fileplanPElement = new Element("p", eadNamespace);
				fileplanElement.addContent(fileplanPElement);
				fileplanPElement.setText(fileplanP);
			}
		}

		List<String> altformavailPs = archdesc.getAltformavailPs();
		if (!altformavailPs.isEmpty()) {
			Element altformavailElement = new Element("altformavail", eadNamespace);
			archdescElement.addContent(altformavailElement);
			for (String altformavailP : altformavailPs) {
				Element altformavailPElement = new Element("p", eadNamespace);
				altformavailElement.addContent(altformavailPElement);
				altformavailPElement.setText(altformavailP);
			}
		}

		Element metadatasElement = new Element("odd", eadNamespace);
		for (Metadata metadata : sipObject.getMetadataList()) {

			if (isMetadataIncludedInEAD(metadata) && isNotEmpty(sipObject.getRecord().getValues(metadata))) {
				addMetadata(sipObject.getRecord(), metadata, metadatasElement);
			}
		}
		if (!metadatasElement.getChildren().isEmpty()) {
			archdescElement.addContent(metadatasElement);
		}

	}

	private boolean isMetadataIncludedInEAD(Metadata metadata) {
		//		if (metadata.getType() == MetadataValueType.STRUCTURE && metadata.getDataEntry().getType() != DataEntryType.MANUAL) {
		//			return false;
		//		}

		return true;
	}

	private void addMetadata(Record record, Metadata metadata, Element metadatasElement) {

		Element metadataElement = new Element("odd", eadNamespace);
		metadataElement.setAttribute("id", metadata.getLocalCode());
		metadataElement.setAttribute("type", metadata.getType().name());

		//TODO Francis singleton
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isMultivalue()) {
			writeMultivalueReferenceMetadata(record, metadata, metadataElement, appLayerFactory);

		} else if (metadata.getType() == MetadataValueType.REFERENCE && !metadata.isMultivalue()) {
			writeSinglevalueReferenceMetadata(record, metadata, metadataElement, appLayerFactory);

		} else if (metadata.getType() == MetadataValueType.STRUCTURE && metadata.isMultivalue()) {
			writeMultivalueStructureMetadata(record, metadata, metadataElement, appLayerFactory);

		} else if (metadata.getType() == MetadataValueType.STRUCTURE && !metadata.isMultivalue()) {
			writeSinglevalueStructureMetadata(record, metadata, metadataElement, appLayerFactory);

		} else if (metadata.getType() == MetadataValueType.CONTENT) {
			writeContentMetadata(record, metadata, metadataElement, appLayerFactory);

		} else {
			if (metadata.isMultivalue()) {
				addValueToElement(metadataElement, record.getValues(metadata));

			} else {
				addValueToElement(metadataElement, record.get(metadata));
			}

		}

		metadatasElement.addContent(metadataElement);

	}

	private void writeContentMetadata(Record record, Metadata metadata, Element metadataElement,
									  AppLayerFactory appLayerFactory) {


		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (Content content : record.<Content>getValues(metadata)) {

			for (ContentVersion contentVersion : content.getVersions()) {
				tableRows.add(newContentVersionTableRow(contentVersion));
			}

			if (content.getCurrentCheckedOutVersion() != null) {
				Map<String, Object> row = newContentVersionTableRow(content.getCurrentCheckedOutVersion());
				row.put("checkedOutDate", content.getCheckoutDateTime());
				row.put("checkedOutByUser", content.getCheckoutUserId());
				row.put("version", null);
				tableRows.add(row);
			}


		}

		metadataElement.addContent(newEADTable(tableRows));
	}

	private void writeSinglevalueStructureMetadata(Record record, Metadata metadata, Element metadataElement,
												   AppLayerFactory appLayerFactory) {

		ModifiableStructure modifiableStructure = record.get(metadata);
		if (modifiableStructure != null) {
			Map<String, Object> infos = convertModifiableStructureToMap(record.getCollection(), metadata, modifiableStructure, appLayerFactory);
			metadataElement.addContent(newEADDefList(infos));
		}

	}


	private void writeMultivalueStructureMetadata(Record record, Metadata metadata, Element metadataElement,
												  AppLayerFactory appLayerFactory) {

		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (ModifiableStructure modifiableStructure : record.<ModifiableStructure>getValues(metadata)) {
			Map<String, Object> tableRow = convertModifiableStructureToMap(record.getCollection(), metadata, modifiableStructure, appLayerFactory);
			tableRows.add(tableRow);
		}

		metadataElement.addContent(this.<T>newEADTable(tableRows));

	}


	private Map<String, Object> convertModifiableStructureToMap(String collection,
																Metadata metadata,
																ModifiableStructure modifiableStructure,
																AppLayerFactory appLayerFactory) {

		Map<String, Object> mappedStructure = appLayerFactory.getExtensions().forCollection(collection).convertStructureToMap(
				new ConvertStructureToMapParams(modifiableStructure, metadata));

		if (mappedStructure == null) {

			if (modifiableStructure instanceof MapStringStringStructure) {
				mappedStructure = new TreeMap<String, Object>((MapStringStringStructure) modifiableStructure);
			}

			if (modifiableStructure instanceof MapStringListStringStructure) {
				mappedStructure = new TreeMap<String, Object>((MapStringStringStructure) modifiableStructure);
			}

		}

		if (mappedStructure == null) {
			throw new ImpossibleRuntimeException("Unsupported structure : " + modifiableStructure.getClass());
		}

		return mappedStructure;
	}

	private Map<String, Object> newContentVersionTableRow(ContentVersion version) {
		Map<String, Object> row = new HashMap<>();
		row.put("sha1", version.getHash());
		row.put("filename", version.getFilename());
		row.put("version", version.getVersion());
		row.put("mimetype", version.getMimetype());
		row.put("modifiedBy", version.getModifiedBy());
		row.put("length", version.getLength());
		row.put("lastModification", version.getLastModificationDateTime());
		row.put("comment", version.getComment());
		return row;
	}

	private void writeMultivalueReferenceMetadata(Record record, Metadata metadata, Element metadataElement,
												  AppLayerFactory appLayerFactory) {

		List<Map<String, Object>> tableRows = new ArrayList<>();
		for (String id : record.<String>getValues(metadata)) {
			try {
				Record referencedRecord = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);

				LinkedHashMap<String, Object> tableRow = new LinkedHashMap<>();
				tableRow.put("id", referencedRecord.getId());
				tableRow.put("code", referencedRecord.get(Schemas.CODE));
				tableRow.put("schema", referencedRecord.get(Schemas.SCHEMA));
				tableRow.put("title", referencedRecord.getTitle());
				tableRows.add(tableRow);

			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record '" + id + "' was not found");
			}
		}

		metadataElement.addContent(this.<T>newEADTable(tableRows));
	}

	private void writeSinglevalueReferenceMetadata(Record record, Metadata metadata, Element metadataElement,
												   AppLayerFactory appLayerFactory) {
		Record referencedRecord = null;

		String id = record.get(metadata);
		if (id != null) {
			try {
				referencedRecord = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(id);
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("Record '" + id + "' was not found");
			}
		}

		if (referencedRecord != null) {
			LinkedHashMap<String, Object> infos = new LinkedHashMap<>();
			infos.put("id", id);
			infos.put("code", referencedRecord.get(Schemas.CODE));
			infos.put("schema", referencedRecord.get(Schemas.SCHEMA));
			infos.put("title", referencedRecord.getTitle());
			metadataElement.addContent(newEADDefList(infos));
		}

	}

	/**
	 * Build an EAD table based on extracted values of items.
	 */
	private <T> Element newEADDefList(Map<String, Object> values) {
		Element listElement = new Element("list", eadNamespace);
		listElement.setAttribute("type", "deflist");

		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (isNotEmpty(entry.getValue())) {
				Element defitemElement = new Element("defitem", eadNamespace);

				Element labelElement = new Element("label", eadNamespace);
				labelElement.setText(entry.getKey());
				defitemElement.addContent(labelElement);

				Element itemElement = new Element("item", eadNamespace);
				defitemElement.addContent(itemElement);

				addValueToElement(itemElement, entry.getValue());

				listElement.addContent(defitemElement);
			}
		}

		return listElement;
	}

	private boolean isNotEmpty(Object value) {
		boolean writtenValue = value != null;
		if (value instanceof String) {
			writtenValue = StringUtils.isNotEmpty((String) value);
		}

		if (value instanceof Collection) {
			Iterator<Object> iterator = ((Collection) value).iterator();
			writtenValue = false;
			while (!writtenValue && iterator.hasNext()) {
				writtenValue = isNotEmpty(iterator.next());
			}

		}
		return writtenValue;
	}

	/**
	 * Build an EAD table based on extracted values of items.
	 */
	private <T> Element newEADTable(List<Map<String, Object>> tableRows) {

		List<String> columnNamesInReceivedOrder = new ArrayList<>();

		for (Map<String, Object> tableRow : tableRows) {
			for (Map.Entry<String, Object> mapEntry : tableRow.entrySet()) {

				if (isNotEmpty(mapEntry.getValue()) && !columnNamesInReceivedOrder.contains(mapEntry.getKey())) {
					columnNamesInReceivedOrder.add(mapEntry.getKey());
				}
			}
		}

		Element tableElement = new Element("table", eadNamespace);
		tableElement.setAttribute("frame", "none");

		Element tGroup = new Element("tgroup", eadNamespace);
		tGroup.setAttribute("cols", String.valueOf(columnNamesInReceivedOrder.size()));
		tableElement.addContent(tGroup);

		Element thead = new Element("thead", eadNamespace);
		tGroup.addContent(thead);

		Element theadRow = new Element("row", eadNamespace);
		thead.addContent(theadRow);

		for (int i = 0; i < columnNamesInReceivedOrder.size(); i++) {
			String column = columnNamesInReceivedOrder.get(i);
			Element theader = new Element("entry", eadNamespace);
			theader.setAttribute("colname", String.valueOf(i + 1));
			theader.setText(column);
			theadRow.addContent(theader);
		}

		Element tbody = new Element("tbody", eadNamespace);
		tGroup.addContent(tbody);

		for (Map<String, Object> tableLine : tableRows) {
			Element row = new Element("row", eadNamespace);
			tbody.addContent(row);

			for (int i = 0; i < columnNamesInReceivedOrder.size(); i++) {
				Element entry = new Element("entry", eadNamespace);
				entry.setAttribute("colname", String.valueOf(i + 1));
				row.addContent(entry);

				Object value = tableLine.get(columnNamesInReceivedOrder.get(i));
				if (value != null) {
					addValueToElement(entry, value);
				}

			}

		}

		return tableElement;
	}

	private boolean isWrittenUsingPTag(Class<?> clazz) {
		return clazz.equals(String.class) || clazz.equals(Boolean.class) || Number.class.isAssignableFrom(clazz)
			   || EnumWithSmallCode.class.isAssignableFrom(clazz);
	}

	private void addValueToElement(Element element, Object value) {
		if (isWrittenUsingPTag(value.getClass())) {
			String stringValue = String.valueOf(value);

			if ("odd".equals(element.getName())) {
				Element pElement = new Element("p", eadNamespace);
				pElement.setText(stringValue);
				element.addContent(pElement);

			} else {
				element.setText(stringValue);
			}

		} else if (value instanceof List) {
			element.addContent(buildListElement((List) value));

		} else if (value instanceof LocalDate || value instanceof LocalDateTime) {
			Element dateElement = buildDateElement(value);

			if ("odd".equals(element.getName())) {
				Element pElement = new Element("p", eadNamespace);
				pElement.addContent(dateElement);
				element.addContent(pElement);

			} else {
				element.addContent(dateElement);
			}

		} else {
			throw new ImpossibleRuntimeException("Unsupported type of class " + value.getClass());

		}
	}

	private Element buildListElement(List<Object> values) {
		Element listElement = new Element("list", eadNamespace);
		listElement.setAttribute("type", "ordered");

		for (Object value : values) {
			if (value != null) {
				Element itemElement = new Element("item", eadNamespace);
				listElement.addContent(itemElement);
				addValueToElement(itemElement, value);
			}
		}

		return listElement;
	}

	private Element buildDateElement(Object value) {
		Element dateElement = new Element("date", eadNamespace);

		if (value instanceof LocalDate) {
			dateElement.setText(((LocalDate) value).toString(ISODateTimeFormat.date()));
		}

		if (value instanceof LocalDateTime) {
			dateElement.setText(((LocalDateTime) value).toString(ISODateTimeFormat.dateTime()));
		}

		return dateElement;
	}

	public void build(String sipPath, ValidationErrors errors, File file) throws IOException {
		validator.validate(sipPath, doc, errors, XSDs);
		OutputStream out = new FileOutputStream(file);
		try {
			// Output as XML
			// create XMLOutputter
			XMLOutputter xml = new XMLOutputter();
			// we want to format the xml. This is used only for demonstration.
			// pretty formatting adds extra spaces and is generally not required.
			xml.setFormat(Format.getPrettyFormat());
			xml.output(doc, out);

			// Validating XML
			builder.build(file);
		} catch (JDOMException e) {
			//			String fileContent = FileUtils.readFileToString(file);
			//			System.out.println(fileContent);
			throw new RuntimeException("Exception for object of type " + sipObject.getType() + " (" + sipObject.getId() + ")", e);
		} finally {
			out.close();
		}
	}
}
