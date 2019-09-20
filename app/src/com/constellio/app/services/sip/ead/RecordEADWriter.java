package com.constellio.app.services.sip.ead;

import com.constellio.app.services.sip.ead.RecordEADWriterRuntimeException.RecordEADWriterRuntimeException_ErrorCreatingFile;
import com.constellio.app.services.sip.xsd.XMLDocumentValidator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static com.constellio.data.utils.LangUtils.isNotEmptyValue;
import static java.util.Arrays.asList;

public class RecordEADWriter {

	private Namespace eadNamespace = Namespace.getNamespace("ead", "urn:isbn:1-931666-22-9");
	private Namespace xsiNamespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	private Document doc;

	private static XMLDocumentValidator validator = new XMLDocumentValidator();

	private static List<String> XSDs = asList("xlink.xsd", "ead.xsd");

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordEADWriter.class);

	private Element eadElement;

	private Element metadatasElement;

	private Element archdescElement;

	public RecordEADWriter() {

		this.eadElement = new Element("ead", eadNamespace);
		this.eadElement.addNamespaceDeclaration(xsiNamespace);
		this.eadElement.setAttribute("schemaLocation", "urn:isbn:1-931666-22-9 http://www.loc.gov/ead/ead.xsd", xsiNamespace);

		this.doc = new Document(eadElement);
		this.metadatasElement = new Element("odd", eadNamespace);
		this.archdescElement = new Element("archdesc", eadNamespace);
	}

	public void build(String sipPath, ValidationErrors errors, File file) throws IOException {

		if (!metadatasElement.getChildren().isEmpty()) {
			archdescElement.addContent(metadatasElement);
		}

		validator.validate(sipPath, doc, errors, XSDs);
		OutputStream out = new FileOutputStream(file);
		try {
			XMLOutputter xml = new XMLOutputter();
			xml.setFormat(Format.getPrettyFormat());
			xml.output(doc, out);

			SAXBuilder builder = new SAXBuilder();
			builder.build(file);
		} catch (JDOMException e) {
			throw new RecordEADWriterRuntimeException_ErrorCreatingFile(sipPath, e);
		} finally {
			out.close();
		}

	}

	public void addHeader(CollectionInfo collectionInfo, String collectionName, String schemaCode,
						  String recordSchemaTypeLabel, String recordSchemaLabel) {
		Element eadheaderElement = new Element("eadheader", eadNamespace);

		Element eadidElement = new Element("eadid", eadNamespace);
		eadidElement.setAttribute("identifier", collectionInfo.getCode() + "_" + schemaCode);
		eadidElement.setText(collectionInfo.getCode() + ", " + schemaCode);

		Element filedescElement = new Element("filedesc", eadNamespace);
		Element titlestmtElement = new Element("titlestmt", eadNamespace);
		Element titleproperElement = new Element("titleproper", eadNamespace);
		titleproperElement.setText(collectionName + ", " + recordSchemaTypeLabel);
		titlestmtElement.addContent(titleproperElement);

		if (!recordSchemaLabel.equals(recordSchemaTypeLabel)) {
			Element subtitleElement = new Element("subtitle", eadNamespace);
			subtitleElement.setText(recordSchemaLabel);
			titlestmtElement.addContent(subtitleElement);
		}

		eadElement.addContent(eadheaderElement);
		eadheaderElement.addContent(eadidElement);
		eadheaderElement.addContent(filedescElement);
		filedescElement.addContent(titlestmtElement);


		Element profiledescElement = new Element("profiledesc", eadNamespace);
		Element langusageElement = new Element("langusage", eadNamespace);

		for (Locale locale : collectionInfo.getCollectionLocales()) {
			Element languageElement = new Element("language", eadNamespace);
			languageElement.setAttribute("langcode", locale.getISO3Language());
			languageElement.setText(locale.getDisplayName(locale));
			langusageElement.addContent(languageElement);
		}

		profiledescElement.addContent(langusageElement);
		eadheaderElement.addContent(profiledescElement);

	}


	public void addArchdesc(EADArchiveDescription archdesc, String recordId, String recordTitle) {
		archdescElement.setAttribute("level", "class");

		Element didElement = new Element("did", eadNamespace);

		Element unitidElement = new Element("unitid", eadNamespace);
		didElement.addContent(unitidElement);
		unitidElement.setAttribute("type", "Identifiant externe");
		unitidElement.setText(recordId);

		Element unittitleElement = new Element("unittitle", eadNamespace);
		didElement.addContent(unittitleElement);
		unittitleElement.setText(recordTitle);

		Map<String, ReadablePartial> didUnitDates = archdesc.getDidUnitDates();
		for (Entry<String, ReadablePartial> entry : didUnitDates.entrySet()) {
			if (entry.getValue() != null) {
				String datechar = entry.getKey();
				String unitdateValue = formatDate(entry.getValue());

				Element unitdateElement = new Element("unitdate", eadNamespace);
				didElement.addContent(unitdateElement);
				unitdateElement.setAttribute("datechar", datechar);
				unitdateElement.setText(unitdateValue);
			}
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

	}

	private String formatDate(ReadablePartial dateOrDateTime) {

		if (dateOrDateTime == null) {
			return "";

		} else if (dateOrDateTime instanceof LocalDate) {
			return ((LocalDate) dateOrDateTime).toString(ISODateTimeFormat.date());

		} else if (dateOrDateTime instanceof LocalDateTime) {
			return ((LocalDateTime) dateOrDateTime).toString(ISODateTimeFormat.dateTime());

		} else {
			throw new ImpossibleRuntimeException("Unsupported ReadablePartial of instance " + dateOrDateTime.getClass());
		}
	}

	private Element newMetadataElement(String localCode, MetadataValueType type) {
		Element metadataElement = new Element("odd", eadNamespace);
		metadataElement.setAttribute("id", localCode);
		metadataElement.setAttribute("type", type.name());
		metadatasElement.addContent(metadataElement);
		return metadataElement;
	}


	/**
	 * Build an EAD table based on extracted values of items.
	 */
	public Element newEADDefList(Map<String, Object> values) {
		Element listElement = new Element("list", eadNamespace);
		listElement.setAttribute("type", "deflist");

		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (isNotEmptyValue(entry.getValue())) {
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

	public void addMetadataEADTable(Metadata metadata, List<Map<String, Object>> tableRows) {
		Element element = newMetadataElement(metadata.getLocalCode(), metadata.getType());
		element.addContent(newEADTable(tableRows));
	}

	public void addMetadataEADDefList(Metadata metadata, Map<String, Object> values) {
		Element element = newMetadataElement(metadata.getLocalCode(), metadata.getType());
		element.addContent(this.<T>newEADDefList(values));
	}

	public void addMetadataWithSimpleValue(Metadata metadata, Object value) {
		Element element = newMetadataElement(metadata.getLocalCode(), metadata.getType());
		addValueToElement(element, value);
	}

	/**
	 * Build an EAD table based on extracted values of items.
	 */
	public Element newEADTable(List<Map<String, Object>> tableRows) {

		List<String> columnNamesInReceivedOrder = new ArrayList<>();

		for (Map<String, Object> tableRow : tableRows) {
			for (Map.Entry<String, Object> mapEntry : tableRow.entrySet()) {

				if (isNotEmptyValue(mapEntry.getValue()) && !columnNamesInReceivedOrder.contains(mapEntry.getKey())) {
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

	final String DATE_TIME_ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	private JsonSerializer<LocalDateTime> newLocalDateTimeTypeAdapter() {
		return new JsonSerializer<LocalDateTime>() {
			@Override
			public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
				final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATE_TIME_ISO_PATTERN);
				return new JsonPrimitive(formatter.print(src));
			}
		};
	}

	private JsonSerializer<EnumWithSmallCode> newEnumWithSmallCodeTypeAdapter() {
		return new JsonSerializer<EnumWithSmallCode>() {
			@Override
			public JsonElement serialize(EnumWithSmallCode src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.getClass().getName());
			}
		};
	}

	private Gson gson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EnumWithSmallCode.class, newEnumWithSmallCodeTypeAdapter());
		gsonBuilder = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, newLocalDateTimeTypeAdapter());
		return gsonBuilder.create();
	}

	public void addValueToElement(Element element, Object value) {
		if (isWrittenUsingPTag(value.getClass())) {
			String stringValue;

			if (value instanceof LinkedHashMap) {
				stringValue = gson().toJson(value);
			} else {
				stringValue = String.valueOf(value);
			}

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
		dateElement.setText(formatDate((ReadablePartial) value));
		return dateElement;
	}

	private boolean isWrittenUsingPTag(Class<?> clazz) {
		return clazz.equals(String.class) || clazz.equals(Boolean.class) || Number.class.isAssignableFrom(clazz)
			   || EnumWithSmallCode.class.isAssignableFrom(clazz) || LinkedHashMap.class.isAssignableFrom(clazz);
	}
}
